/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.highway.agent;

import cz.agents.alite.common.event.Event;
import cz.agents.highway.maneuver.CarManeuver;
import cz.agents.highway.maneuver.StraightManeuver;
import cz.agents.highway.storage.HighwayEventType;
import cz.agents.highway.storage.RoadObject;
import cz.agents.highway.storage.VehicleSensor;
import cz.agents.highway.storage.plan.Action;
import cz.agents.highway.storage.plan.WPAction;
import javax.vecmath.Point3f;
import java.util.Collection;
import pathfinding.ExampleFactory;
import pathfinding.ExampleNode;
import pathfinding.ExampleUsage;
import pathfinding.Map;

/**
 * @author adityaraj
 *
 * Implementation of cooperating driving agents which sense obstacle in defined
 * sensor radius and further plan new way to get rid of the obstacle so that
 * they can continue driving at maximum speed
 */
public class motionAgent extends Agent {//implements Runnable{

    //An instance of ManeuverTranslator class to change the reference of CarManeuver to the type of maneuver we want. For example if we need to change
    //to left lane an instance of leftlanemaneuver can be translated to waypoints using the object of ManeuverTranslator class.
    protected final ManeuverTranslator maneuverTranslator;

    // Defining the possible states an agent can achieve during its lifetime
    // DesiredS refered to desired state in which an object continues to travel at defined maximum speed
    // An agent comes to Platooning state when it senses an obstacle ahead of it and morever it senses a need to reduce its speed to the vehicle in front of it.
    // Change_left states is achieved when an agent finds a plan and further it has to turn left for maneuvering
    // Overtake state continues until an object has not gained signal to turn right after left lane change
    // Finally an object turns right and completes the maneuvering task.
    private enum States {

        DesiredS, Platooning, Change_left, Overtake, Change_right
    }

    // Initialising initial states as Desired state for all agents
    private States current = States.DesiredS;

    // Array of instances of add2RoadObjects are created to store the critical section value and range to reserve when an object has finished planning an overtake.
    // Critical section value is set when an agent gets the permission to execute maneuvering after finding no conflicts with any other agent plans to maneuver
    // Further once a plan to maneuver is found range values are set to reserve the region until maneuver completes, so that the plan with any other vehicle must not find 
    // conflicting space.
    public static add2RoadObject a2R[] = new add2RoadObject[50];

    // At the begining of simulation after RoadObjects are initialised we need to initialise all instances of add2RoadObject with the default critical section and range values.
    // Further this value is set to false, because this need not be initalised again and again.
    private boolean runOnce = true;

    //Here you can define the maximum and minimum speeds for the agents. maximum speed for the agents are chosen from this range defined
    public static int max_speed = 10, min_speed = 5;

    // Defining maximum speed for the agents 
    private static final float[] AGENT_SPEEDS = new float[50];

    // Defining the range of visibility as sensor_radius, limits to check if the agent has almost reached the given co-ordinate because of assymptotic curve problem, and the gap between two lanes
    private static final double sensor_radius = 20, limit = 0.1, lane_gap = 3.7;

    //variable time stores the time an agent has to spend in overtaking lane and 
    //the variable waiting_time stores the time an agent has to wait after finding no plans after consecutive trials
    private double time = 0, waiting_time = 0;

    // last_planning_time stores the current time when an agent tries to plan a maneuver. if no plans are found this timer delays the finding of next plan
    private long last_planning_time = 0;

    // Timer1 stores the current time when an agent starts changing to left lane. This variable is used for finding the time an agent spends in changing the lane
    double timer1;

    // These variables stores the waypoints after successfully finding a plan to execute maneuver. An agent follows these wypoint during the execution of maneuvers.
    float first_wayptX = -999, first_wayptY = -999, second_wayptX = -999, second_wayptY = -999, third_wayptX = -999, third_wayptY = -999, overtake_speed = 0;

    // Storing the actual lanes of all agents as agent lanes are mapped with their X Coordinates. And during their lane change there actual initial lane and their current lane will vary
    private int actualLane = -1;

    // This variable stores the number of samples we want while defining a lane change. As we follow a sigmoid path while changing lane, the number of samples required depends on our maximum speed. 
    // The more the maximum speed th more distance we need to cover in changing lane and further more samples are desired to follow the path.
    private int number_of_samples_inSigmoidMethod = 1;

    //For lane change variables
    // cs_val = critical section value for an agent, range1 and range2 points store the region needed to be reserved for executing maneuver and pfake returns points while changing lane as sigmoid path
    private int cs_val = 0;
    private Point3f range1 = new Point3f(0, 0, 0);
    private Point3f range2 = new Point3f(0, 0, 0);
    private Point3f pfake = new Point3f();
    private double a = -1, b = -1, original_pos = 0, temp = 0, limit2 = 0.1, lane_start_limit = -1, temp_var = -1;

    /* Variables for A star algorithm path finding (not implemented yet)
     private Map<ExampleNode> myMap = new Map<ExampleNode>(2, 50, new ExampleFactory());
     */
    //The class defining six road objects. Each RoadObject holds the information about frontLeft car, frontAhead car, frontnextAhead car, frontRight car, rearLeft car, rearRight car . 
    //These are relative to the current car.
    class State {

        RoadObject frontLeft;
        RoadObject frontAhead;
        RoadObject frontnextAhead;
        RoadObject frontRight;
        RoadObject rearLeft;
        RoadObject rearRight;

        @Override
        //tO string method to print the states of roadObjects corresponding to current Agent.
        public String toString() {
            return "State{"
                    + "frontLeft=" + frontLeft
                    + ", frontAhead=" + frontAhead
                    + ", frontRight=" + frontRight
                    + ", rearLeft=" + rearLeft
                    + ", rearRight=" + rearRight
                    + '}';
        }

        //Constructor to compute different RoadObjects given a collection of RoadObjects and the current roadobject/agent.
        public State(Collection<RoadObject> cars, RoadObject ego_state) {
            for (RoadObject car : cars) {
                if (car.getId() == ego_state.getId()) {
                    continue; //do not consider yourself
                }
                int myLane = ego_state.getLaneIndex();
                int otherCarLane = car.getLaneIndex();
                if (myLane == otherCarLane) { // same lane
                    if (isAhead(car, ego_state)) {
                        if (frontAhead == null || isAhead(frontAhead, car)) {
                            frontAhead = car;
                        }
                        if (frontnextAhead == null || (isAhead(car, frontAhead) && (isAhead(frontnextAhead, car)))) {
                            frontnextAhead = car;
                        }

                    }
                } else if (myLane + 1 == otherCarLane) { //left lane
                    if (isAhead(car, ego_state)) {
                        if (frontLeft == null || isAhead(frontLeft, car)) {
                            frontLeft = car;
                        }
                    } else {
                        if (rearLeft == null || isAhead(car, rearLeft)) {
                            rearLeft = car;
                        }
                    }
                } else if (myLane - 1 == otherCarLane) { //right lane
                    if (isAhead(car, ego_state)) {
                        if (frontRight == null || isAhead(frontRight, car)) {
                            frontRight = car;
                        }
                    } else {
                        if (rearRight == null || isAhead(car, rearRight)) {
                            rearRight = car;
                        }
                    }
                }
            }

        }

        private boolean isAhead(RoadObject first, RoadObject second) {
            return (first.getPosition().y - second.getPosition().y) <= 0;
        }
    }

    // This class defines the state of the current agent 
    class StateMachine {

        StateMachine(States state) {
            current = state;
        }

        // This method returns the wayPoints for an agent to move forward. the waypoints are returned according to the state of an agent.
        public Action do_action() {

            Collection<RoadObject> collection = sensor.senseCars();
            RoadObject myState = sensor.senseCurrentState();
            motionAgent.State state = new motionAgent.State(collection, myState);
            RoadObject fa = state.frontAhead;
            //System.out.println(state);
            Point3f pos = new Point3f(myState.getPosition());
            switch (current) {
                case DesiredS:
                    // A check is made if there is no obstacle ahead of the current agent in the sensor radius
                    //To do this we check for the front agent position (denoted here by RoadObject fa). if the position is in sensor radius we, need to solve
                    if (fa != null && Math.abs(myState.getPosition().y - fa.getPosition().y) < sensor_radius) {
                        //car is ahead and we need to solve it
                        if (myState.getVelocity().y + limit < AGENT_SPEEDS[id] && myState.getVelocity().length() > fa.getVelocity().length()) {
                            // Switch current state to platooning state so that either the agent can plan for a maneuver or platoon the vehicle ahead of it.
                            // So the waypoints are returned with speed equal to the platooning speed
                            current = States.Platooning;
                            pos.y -= fa.getVelocity().length();
                            return new WPAction(id, 1, pos, fa.getVelocity().length());
                        }
                    }
                   // System.out.println("For id "+id+" my current state is "+current);

                    ////If current state is desired state then the agent can move with maximum speed
                    pos.y -= AGENT_SPEEDS[id];
                    return new WPAction(id, 1, pos, AGENT_SPEEDS[id]);

                case Platooning:
                    // If we have come too close to the frontAhead agent, we need to reduce our speed very low, in this case I have assumed 1m/s
                    if (fa != null && Math.abs(myState.getPosition().y - fa.getPosition().y) < sensor_radius - 20) {
                        pos.y -= 2;
                        return new WPAction(id, 1, pos, 1);
                        
                    } 
                    // Else If we continue sensing a frontAhead obstacle not too close but also in the sensor radius, we do the following
                    else if (fa != null && Math.abs(myState.getPosition().y - fa.getPosition().y) < sensor_radius) {
                        
                        // We check if we already planned for a path previously, if yes we wait for our next palnning time 
                        // else try to plan a manuever
                        
                        if (System.currentTimeMillis() - last_planning_time >= 500 + Math.random() * 2000) {
                            //System.out.println(" for id "+ id +" "+last_planning_time/1000);
                            
                            // Time variable stores the time to plan a maneuver
                            time = Plan_manoeuvre();
                            
                            // This is used only if a plan is not found to store the last planning time so that we can compare to the current time and 
                            // check if we have crossed the delay period allotted to an agent
                            last_planning_time = System.currentTimeMillis();
                        } else {
                            // Time is set to zero to ensure that the agent does not go to execute maneuver even if a plan is not found
                            time = 0;
                            //System.out.println("For id "+id +" wait for "+ (((System.currentTimeMillis()-last_planning_time)/1000)));
                        }
                        
                        // Here we check if time returned by the plan_maneuver method is positive and also if the current agent does not show a conflict with other agents.
                        // this check is made so that no two agents can execute the maneuver within the same range so that they may not collide while maneuvering
                        
                        if (time > 0 && !get_conflict()) {
                            //System.out.println("Went for maneuver id " + id + " " + " lane index " + a2R[id].getActualLaneIndex() + " " + a2R[id].get_cs() + " range vals are " + a2R[id].get_range()[0].y + " " + a2R[id].get_range()[1].y + " pos is " + myState.getPosition().x + " " + myState.getPosition().y);
                            //System.out.println("before maneuvering pos of "+id+" is "+myState.getPosition().x+" "+myState.getPosition().y);
                            
                            // Here we set the last planning time again to zero because we have already found a plan and later when the agent again plans for a maneuver for the next time
                            // it must not need to wait atleast at its first attempt to plan
                            
                            last_planning_time = 0;
                            
                            // Alloting firstWaypoint to current X position - lane_gap. At this position, when the agent reaches, we can say that it has successfully made a lane change 
                            first_wayptX = (float) (pos.x - lane_gap);
                            pos.y -= AGENT_SPEEDS[id];
                            
                            // Changing the state to Change_left so that in the next cycle it switches to the Change_left and gets wayPoints accordingly
                            current = States.Change_left;
                            
                            // While changing lane it is ensured that the agent changes lane 
                            return new WPAction(id, 1, pos, AGENT_SPEEDS[id]);
                        }
                        //System.out.println("For id "+id+" my current state is "+current);
                        pos.y -= fa.getVelocity().length();
                        return new WPAction(id, 1, pos, fa.getVelocity().length());
                    } else {
                        // Change current to DesiredState if not platooning is required
                        current = States.DesiredS;
                        pos.y -= AGENT_SPEEDS[id];
                        return new WPAction(id, 1, pos, AGENT_SPEEDS[id]);
                    }

                case Change_left:
                    /*if (myState.getPosition().x <= first_wayptX + limit) {
                        
                     System.out.println("after left lane change of "+id+" is "+myState.getPosition().x+" "+myState.getPosition().y);

                     second_wayptY = (float) (pos.y - (time * overtake_speed) - 20);
                     pos.y -= overtake_speed;
                     current = States.Overtake;
                     return new WPAction(id, 1, pos, overtake_speed);
                     }
                     //CarManeuver maneuver1 = new LaneLeftManeuver(myState.getLaneIndex(), AGENT_SPEEDS[id], myState.getPosition().y, System.cu);
                     //return maneuverTranslator.translate(maneuver1);
                     pos.x = first_ wayptX;
                     pos.y -= AGENT_SPEEDS[id];
                     return new WPAction(id, 1, pos, AGENT_SPEEDS[id]);
                     */
                    
                    // This is to be executed only once wheh we start changing to left lane
                    if (lane_start_limit == -1) {
                        // Store current x and y to some temporary variables, these positions will be modified to provide new x and y co-ordinates at every S shaped lane change sigmoid method
                        a = myState.getPosition().y;
                        b = myState.getPosition().x;
                        
                        // TO store the current rrefernce of x co-ordinate 
                        original_pos = b;
                        
                        // Compute x value for the sigmoid method when limit tends to 0.1 for the sigmoid method.
                        lane_start_limit = compute_sigmoid_method_x(limit2);
                        temp = Math.abs(lane_start_limit);
                        
                        // Not required, just to check if lane change duration is the same as expected with maximum velocity
                        timer1 = System.currentTimeMillis();
                        //System.out.println("For id "+id+" my current state is "+current);
                        //System.out.println("For id "+ id+" before left lane change positions are "+myState.getPosition().x+" "+myState.getPosition().y+" and the speed and limits are "+AGENT_SPEEDS[id]+" "+lane_start_limit);
                        //System.out.println("for id "+id +" lane start limits are "+ lane_start_limit+" for position start "+b+" "+a+" speed in kmph " + AGENT_SPEEDS[id]*3.6 +" starting left lane maneuver");
                    }
                    
                    // The loop which checks the current position co-ordinates and checks if it is already these else it loops inside it
                    if (myState.getPosition().x > first_wayptX + 0.1) {
                        // Specifies the next waypoint, computed by sigmoid method, to an agent only if the agent has not reached the last waypoint specified waypoint by sigmoid method
                        if (b == -1 || myState.getPosition().x <= b + 0.1) {
                            lane_start_limit += (temp) * 2 / number_of_samples_inSigmoidMethod;
                            a -= (temp) * 2 / number_of_samples_inSigmoidMethod;
                            b = original_pos - Math.abs(compute_sigmoid_method_y(lane_start_limit));
                            pfake = new Point3f((float) b, (float) a, pos.z);
                        }
                        System.out.println("while planning left lane for " + id + " planned sigmoid method x and y are " + b + " " + a + " original x and y " + myState.getPosition().x + " " + myState.getPosition().y);
                        //System.out.println("For id "+id+" my current state is "+current);
                        
                        // Returns the computed waypoint for an agent currently in change_left state
                        return new WPAction(id, 0, pfake, AGENT_SPEEDS[id]);
                    } else {
                        //System.out.println("For id "+id+" actual after left lane change of is "+myState.getPosition().x+" "+myState.getPosition().y);
                        //second_wayptY = (float) (pos.y - (time * overtake_speed));
                        pos.y -= overtake_speed;
                        current = States.Overtake;
                        lane_start_limit = -1;
                        //System.out.println("For id "+id+" spent time in lane changing is "+((System.currentTimeMillis()-timer1)/1000));
                        return new WPAction(id, 1, pos, overtake_speed);
                    }

                case Overtake:
                    if (myState.getPosition().y <= second_wayptY + limit) {
                        //stop overtaking
                        //System.out.println("after overtaking for "+id+" is "+myState.getPosition().x+" "+myState.getPosition().y);
                        pos.x += lane_gap;
                        pos.y -= AGENT_SPEEDS[id];
                        third_wayptX = pos.x;
                        current = States.Change_right;
                        return new WPAction(id, 1, pos, AGENT_SPEEDS[id]);
                    }
                    //System.out.println("For id "+id+" my current state is "+current);
                    //keep overtaking
                    pos.y -= overtake_speed;
                    return new WPAction(id, 1, pos, overtake_speed);
                //CarManeuver maneuver2 = new StraightManeuver(myState.getLaneIndex(), overtake_speed, myState.getPosition().y, 0);
                //return maneuverTranslator.translate(maneuver2);

                case Change_right:
                    if (lane_start_limit == -1) {
                        a = myState.getPosition().y;
                        b = myState.getPosition().x;
                        original_pos = b;
                        lane_start_limit = compute_sigmoid_method_x(limit2);
                        temp = Math.abs(lane_start_limit);
                        //System.out.println("For id "+ id+" before right lane change positions are "+myState.getPosition().x+" "+myState.getPosition().y+" and the speed and limits are "+AGENT_SPEEDS[id]+" "+lane_start_limit);
                        //System.out.println("for id "+id +" lane start limits are "+ lane_start_limit+" for position start "+b+" "+a+" speed in kmph " + AGENT_SPEEDS[id]*3.6 +" starting left lane maneuver");
                    }
                    if (myState.getPosition().x + 0.01 < third_wayptX) {
                        if (b == -1 || myState.getPosition().x + 0.1 >= b) {
                            lane_start_limit += (temp) * 2 / number_of_samples_inSigmoidMethod;
                            a -= (temp) * 2 / number_of_samples_inSigmoidMethod;
                            b = original_pos + (compute_sigmoid_method_y(lane_start_limit));
                            pfake = new Point3f((float) b, (float) a, pos.z);
                        }
                        //System.out.println("For id "+id+" my current state is "+current);
                        System.out.println("while planning right lane for " + id + " planned sigmoid method x and y are " + b + " " + a + " original x and y " + myState.getPosition().x + " " + myState.getPosition().y);
                        return new WPAction(id, 0, pfake, AGENT_SPEEDS[id]);
                    } else {
                        //System.out.println("For id " + id + " actual after right lane change of is " + myState.getPosition().x + " " + myState.getPosition().y);
                        second_wayptY = (float) (pos.y - (time * overtake_speed) - 20);
                        pos.y -= overtake_speed;
                        current = States.Overtake;
                        lane_start_limit = -1;
                        current = States.DesiredS;
                        Point3f p = new Point3f(0, 0, 0);
                        range1 = p;
                        range2 = p;
                        cs_val = 0;
                        a2R[id].set_cs(0);
                        a2R[id].set_range(p, p);
                        time = 0;

                        return new WPAction(id, 1, pos, overtake_speed);
                    }
            }
            return null;
        }
    }

    public motionAgent(int id) {
        
        // Intialising every motionAgent with a given Id and this id is passed to the super class Agent.
        super(id);

        // An instance of maneuverTranslator class translates a maneuver to wayPoints. Say if we initalise an instance of StraightManeuver then this maneuverTranslator variable
        //  translates the straight maneuver to waypoints for OpenDS.
        maneuverTranslator = new ManeuverTranslator(id, navigator);
        
        //Initializes the number of sampling allowed in the sigmoid method
        number_of_samples_inSigmoidMethod = (int) Math.abs(compute_sigmoid_method_x(limit2) / AGENT_SPEEDS[id]);
        
        // If number of samples initailsed turns out less than two, then it is set to default value 2.
        if (number_of_samples_inSigmoidMethod < 2) {
            number_of_samples_inSigmoidMethod = 2;
        }
        // Random speed alloting to vehicles
        for (int i = 0; i < 50; ++i) 
            AGENT_SPEEDS[i] = (int) (Math.random() * (max_speed - min_speed) + min_speed);
        
        //Initialising critical section values and range values
        /*cs_val=0;
         range1 = new Point3f(0, 0, 0);
         range2 = new Point3f(0, 0, 0);*/
    }
    
    // This method is called by agentReact method in the same class for getting plans in the form of waypoints.
    private Action plan() {
        
        // Initialising an instance of Straight maneuver at begining for all agents. Then further calling maneuverTrannslator.translate(sm) will return waypoints to OpenDs.
        //CarManeuver sm = new StraightManeuver(0, AGENT_SPEEDS[id], 5, 1);
        
        // defining variable myState to sense current state of the agent.
        RoadObject myState = sensor.senseCurrentState();
        
        // an instance of innerClass Statemachine holds the current state of the RoadObject(This can be DesiredS, Chang_left and so on)
        motionAgent.StateMachine myActionState = new motionAgent.StateMachine(current);
        
        // Done only once to initialise the critical section values and range values to zero.
        // An instance of add2RoadObject holds the additional features of the RoadObject.
        // Although not the proper way to do so. We have defined an array of add2RoadObject instances with index = id of the RoadObject.
        // To access the methods we can fetch the variable for the add2R object with index equals to id of the Agent for which we need the information
        if (runOnce) {
            for (int i = 0; i < 50; ++i) {
                a2R[i] = new add2RoadObject(cs_val, range1, range2, 0);
            }
            // Initialising the actual lane for an agent equal to the starting lane of the agent on assumption that at t=0 none of the vehicle starts with a maneuver plan.
            actualLane = myState.getLaneIndex();
            runOnce = false;
        }
        //if(myState.get_cs())
        //  range1.y=myState.getPosition().y+10;
        //if(myState.get_cs())
        //  System.out.println("My range for "+id +" "+myState.get_range()[0].y+" "+myState.get_range()[1].y);

        /*if(myState.get_cs()==1 && myState.get_range()[0].y !=0){
         Point3f p1 = new Point3f(myState.get_range()[0].x, (myState.getPosition().y+30), myState.get_range()[0].z);
         Point3f p2 = new Point3f(myState.get_range()[1].x, myState.getPosition().y-150 , myState.get_range()[1].z);
            
         if(p2.y<myState.get_range()[1].y)
         p2.y=myState.get_range()[1].y;
         myState.setDynamicReservedArea(p1, p2);
         //System.out.println("For id "+id+" dynamic reserved areas are "+myState.getDynamicReservedArea()[0].y +" "+myState.getDynamicReservedArea()[1].y);
         }*/
        //System.out.println("routine update id "+ id + " lane " + myState.getLaneIndex()+ " cs val " +myState.get_cs()+ "  "+ myState.get_range()[0].y+ " "+myState.get_range()[1].y);
        //System.out.println("My id " + id + " " + current);
        return myActionState.do_action();
    }

    // This method searches for a maneuver before executing the plan.
    public double Plan_manoeuvre() {
        
        // collection variable senses all the cars on the lane and further can be used to fetch the properties of the car with satisfied parameters
        Collection<RoadObject> collection = this.sensor.senseCars();
        
        // myState senses the current state of the currently active agent
        RoadObject myState = sensor.senseCurrentState();
        
        // an instance of inner class State to hold the vehicles neighboring the current agent, here defined in terms of variables frontahead, frontleft, frontright and so on.
        motionAgent.State state = new motionAgent.State(collection, myState);

        // Time to lane change can be computed out by finding the range of sigmoid method, x value, when the limit reaches to a vlue say 0.1 divided by maximum speed of agent
        double time_to_lane_change = 2 * Math.abs(compute_sigmoid_method_x(limit2)) / AGENT_SPEEDS[id];//time_tochnglane(myState.getVelocity().length(), AGENT_SPEEDS[id], lane_gap);
        
        // initialsing all the variables aftr assumjing that the agent has made a lane change after time_to_lane_change
        double rearleft_aftr_time_to_lane_change = 0, frontLeft_aftr_time_to_lane_change = 0, time_taken_tocross = 0;

        // If RearLeft exists, calculating the predicted position of the rearleft agent after time_to_lane_change
        if (state.rearLeft != null) {
            rearleft_aftr_time_to_lane_change = state.rearLeft.getPosition().y - time_to_lane_change * state.rearLeft.getVelocity().length();
        }

        // If frontLeft exists, calculating the predicted position of the frontleft agent after time_to_lane_change
        if (state.frontLeft != null) {
            frontLeft_aftr_time_to_lane_change = state.frontLeft.getPosition().y - time_to_lane_change * state.frontLeft.getVelocity().length();
        }
        
        // Calculating the predicted position after current vehicle has changed to left lane
        double current_aftr_leftlaneChange = myState.getPosition().y - Math.abs(2 * compute_sigmoid_method_x(limit2));
        //System.out.println("For id "+id+" predicted after left lane change "+current_aftr_leftlaneChange);
        
        // Storing this has the final first_wayptY for the current agent
        first_wayptY = (float) current_aftr_leftlaneChange;
        
        // Calculating the predicted postion of the frontahead after time to lane change
        double frontahead_aftr_time_to_lane_change = state.frontAhead.getPosition().y - time_to_lane_change * state.frontAhead.getVelocity().length();

        if ((state.rearLeft == null) || (Math.abs(current_aftr_leftlaneChange - rearleft_aftr_time_to_lane_change) > sensor_radius) && ((state.frontLeft == null) || (Math.abs(frontLeft_aftr_time_to_lane_change - current_aftr_leftlaneChange) > sensor_radius))) {

            if (state.frontLeft == null) {
                overtake_speed = AGENT_SPEEDS[id];
            } else {
                overtake_speed = state.frontLeft.getVelocity().length();

                //Physics to predict overtake speed by equating the maximum time to collide and time to cover relative distance
                double rel_dis_ratio = (current_aftr_leftlaneChange - frontahead_aftr_time_to_lane_change) / (current_aftr_leftlaneChange - frontLeft_aftr_time_to_lane_change);
                overtake_speed = (float) (((state.frontLeft.getVelocity().length() * rel_dis_ratio) - state.frontLeft.getVelocity().length()) / (rel_dis_ratio - 1));
                if (overtake_speed > AGENT_SPEEDS[id]) {
                    overtake_speed = AGENT_SPEEDS[id];
                }
            }
            double relative_vel = overtake_speed - state.frontAhead.getVelocity().length();
            double relative_dis = current_aftr_leftlaneChange - frontahead_aftr_time_to_lane_change;

            try {
                time_taken_tocross = (relative_dis) / (relative_vel);
            } catch (Exception e) {
                //System.out.println("No plans found for "+ id+" as infinity error occured ");
                return 0;
            }

            double frontAhead_ahead_aftr_time = 0;

            if (state.frontnextAhead != null) {
                frontAhead_ahead_aftr_time = (state.frontnextAhead.getPosition().y - (time_to_lane_change * state.frontnextAhead.getVelocity().length()) - (time_taken_tocross * state.frontnextAhead.getVelocity().length()));
            }

            double predicted_output_position_for_cuurent_vehicle = (current_aftr_leftlaneChange - (time_taken_tocross * overtake_speed) - Math.abs(2 * compute_sigmoid_method_x(limit2)));

            if (time_taken_tocross > 0 && (state.frontnextAhead == null || Math.abs(frontAhead_ahead_aftr_time - predicted_output_position_for_cuurent_vehicle) > sensor_radius)) {
                //System.out.println("For id " + id + " time to lane change is " + time_to_lane_change);
                range1 = myState.getPosition();
                range2 = new Point3f(myState.getPosition().x, (float) predicted_output_position_for_cuurent_vehicle, 0);
                a2R[id].set_range(range1, range2);
                second_wayptY = (float) (current_aftr_leftlaneChange - (overtake_speed * time_taken_tocross));
                //System.out.println("For id "+id+" "+predicted_output_position_for_cuurent_vehicle);
                return (time_taken_tocross);
            } else {
                //System.out.println("No plans found for " + id+" as final position collision occurence found");
                return 0;
            }
        } else {
            //System.out.println("No plans found for " + id +" as there is a vehicle in front left or rear left ");
            return 0;
        }
    }

    // This method adds sensor to the current agent. For adding sensors it does this via an instance of VehicleSensor class.
    public void addSensor(final VehicleSensor sensor) {

        this.sensor = sensor;
        maneuverTranslator.setSensor(sensor);
        this.sensor.registerReaction(new Reaction() {
            public void react(Event event) {
                if (event.getType().equals(HighwayEventType.UPDATED)) {
                    actuator.act(agentReact());
                }
            }
        });

    }

    //For all agents we need to define agentreact method as this method returns the actual plan for an agent in the form of waypoints.
    public Action agentReact() {
        return plan();
        //return maneuverTranslator.translate(plan());
    }

    // While checking conflicts between the space region reserved while executing maneuver by another agent and the current agent willing to 
    // execute maneuver, we need to check if any of the two points to be reserved next lies between the already reserved region.
    public boolean Check_inbetween(double a, double b, double check) {
        if (check > b && check < a) {
            //.println("For id "+id +" val "+check+" lies between "+a+" and "+b);
            return true;
        }
        //.println("For id "+id +" val "+check+" doesnot lie between "+a+" and "+b);
        return false;
    }

    // While checking conflicts between the space region reserved while executing maneuver by another agent and the current agent willing to 
    // execute maneuver, we need to check if any of the two points to be reserved next crosses the already reserved region.
    public boolean Check_crossing(double a, double b, double check1, double check2) {
        if (check1 > a && check2 < b) {
            //.println("For id "+id +" val "+check1+" and "+check2+" lies between "+a+" and "+b );
            return true;
        }
        //.println("For id "+id +" val "+check1+" and "+check2+ " doesnot lie between "+a+" and "+b);
        return false;
    }

    // Checks for conflicts between the reserved regions and the wish to reserve new regions.
    public boolean get_conflict() {
        RoadObject myState = sensor.senseCurrentState();
        Collection<RoadObject> cars = sensor.senseCars();
        for (RoadObject car : cars) {
            //System.out.println("For car "+car.getId()+" csval "+a2R[car.getId()].get_cs()+" car range "+a2R[car.getId()].get_range()[0].y+ " "+a2R[car.getId()].get_range()[1].y+" lane index "+car.getLaneIndex());
            if (a2R[car.getId()].get_cs() == 1 && ((a2R[car.getId()].getActualLaneIndex() == a2R[id].getActualLaneIndex()) || (a2R[car.getId()].getActualLaneIndex() == (a2R[id].getActualLaneIndex() + 1)))) {
                //System.out.println("Checking conflicts between "+myState.getId()+" in lane "+a2R[id].getActualLaneIndex()+" with "+car.getId()+" in lane "+a2R[car.getId()].getActualLaneIndex());
                if ((Check_inbetween(a2R[car.getId()].get_range()[0].y, a2R[car.getId()].get_range()[1].y, a2R[id].get_range()[0].y))
                        || (Check_inbetween(a2R[car.getId()].get_range()[0].y, a2R[car.getId()].get_range()[1].y, a2R[id].get_range()[1].y))
                        || (Check_crossing(a2R[car.getId()].get_range()[0].y, a2R[car.getId()].get_range()[1].y, a2R[id].get_range()[0].y, a2R[id].get_range()[1].y))) {
                    // System.out.println("Conflict found for "+id);//+ " with "+car.getId() +" as "+a2R[id].get_range()[0].y +" or "+a2R[id].get_range()[1].y+" lies between "+a2R[car.getId()].get_range()[0].y+" and "+a2R[car.getId()].get_range()[1].y+" and also their lanes are "+a2R[id].getActualLaneIndex()+" "+a2R[car.getId()].getActualLaneIndex());
                    return true;
                }
            }
        }
        //System.out.println("No conflict found for "+id );
        a2R[id].set_cs(1);
        cs_val = 1;
        return false;
    }


    public double distance_to_lanechange(double curr_pos, double curr_speed, double max_speed, double rel_distance, double acc) {
        return 0;

        //double 
    }

    // Compute sigmoid method for the given x
    public double compute_sigmoid_method_y(double x) {
        return ((lane_gap) * (1 / (1 + (Math.pow(Math.E, ((-1 * x) / (AGENT_SPEEDS[id] / 2)))))));
    }

    // Compute sigmoid method for the given y
    public double compute_sigmoid_method_x(double y) {
        return AGENT_SPEEDS[id] / 2 * Math.log(y / ((lane_gap) - y));
    }

    // Compute the time an agent can take to reach maximum speed
    public double time_to_max_speed(float mySpeed) {
        return (AGENT_SPEEDS[id] - mySpeed) / 5;
    }

    // Compute the distance travelled by an agent for a given acceleration
    public double distance_with_acc(float mySpeed) {
        return (((AGENT_SPEEDS[id] * AGENT_SPEEDS[id]) - (mySpeed * mySpeed)) / (2 * 5));
    }

    // Compute sin inverse of hyperbolic function for a given value
    public double calc_sinhinverse(double z) {
        return Math.log(z + (Math.pow(((z * z) + 1), 0.5)));
    }

    // Based on mathematical integrations. As Integral of (1+(dy/dx)^2)^0.5 over the given range gives the length of the curve for the given limits.
    public double length_of_sigmoid_method(double b, double a, double limit1, double limit2) {

        //length of the curve is given by ((1+(dy/dx)^2))^0.5,  
        //y= (b)/(1+e^-(2x/a) ), dy/dx = (2b(e^(-2x/a)))/(a*(e^(-2x/a)+1)^2)
        // b = lane_gap, a=agentSpeed[id]
        // Integral of (1+(2*b*e^(-2*x/a)/(a(1+e^(-2*x/a))))^2)^0.5 = 
        double c = calc_sinhinverse((2 * b) / ((a * (Math.pow(Math.E, (2 * limit2 / a)))) + a));
        double d = calc_sinhinverse((Math.pow(Math.E, (-2 * limit2 / a)))) * (((a * a * (Math.pow(Math.E, (2 * limit2 / a))))) + (4 * b * b) + (a * a));
        double e = calc_sinhinverse((((Math.pow(Math.E, (2 * limit2 / a))) * a) + a) / (2 * b));

        double f = calc_sinhinverse((2 * b) / ((a * (Math.pow(Math.E, (2 * limit1 / a)))) + a));
        double g = calc_sinhinverse((Math.pow(Math.E, (-2 * limit1 / a)))) * (((a * a * (Math.pow(Math.E, (2 * limit1 / a))))) + (4 * b * b) + (a * a));
        double h = calc_sinhinverse((((Math.pow(Math.E, (2 * limit1 / a))) * a) + a) / (2 * b));

        return ((-1 * ((-2 * b * c) + ((Math.pow(((4 * b * b) + (a * a)), 0.5)) * d / (2 * a * b)) - (a * e)) / 2)
                - (-1 * ((-2 * b * f) + ((Math.pow(((4 * b * b) + (a * a)), 0.5)) * g / (2 * a * b)) - (a * h)) / 2));
    }

    // The time taken by agent to change lane. Calculated by considering the current speed and given the fact that the agent almost follows the sigmoid path.
    public double time_tochnglane(float curr_speed, float max_speed, double lane_width) {

        double c = Math.abs(compute_sigmoid_method_x(limit2));
        double total_distance_travelled = length_of_sigmoid_method(3.5, AGENT_SPEEDS[id], -c, c);
        
        // Calulate time to reach max speed by using the method defined above
        double ttRMax_Speed = time_to_max_speed(curr_speed);
        double distance_travelled_to_get_maxspeed = distance_with_acc(curr_speed);

        //return total_distance_travelled/AGENT_SPEEDS[id];
        if (ttRMax_Speed < 0) {
            ttRMax_Speed = 0;
        }

        if (distance_travelled_to_get_maxspeed < 0) {
            distance_travelled_to_get_maxspeed = 0;
        }

        double remaining_distance = total_distance_travelled - distance_travelled_to_get_maxspeed;
        if (remaining_distance < 0) {
            return ttRMax_Speed;
        } else {
            return ttRMax_Speed + (remaining_distance / AGENT_SPEEDS[id]);
        }

    }
}
