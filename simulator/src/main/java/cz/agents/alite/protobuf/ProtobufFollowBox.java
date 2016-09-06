package cz.agents.alite.protobuf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.jme3.cinematic.MotionPathListener;
import com.jme3.cinematic.events.MotionEvent;
import com.jme3.math.Vector3f;

import cz.agents.highway.storage.plan.ManeuverAction;
import cz.agents.highway.storage.plan.WPAction;
import eu.opends.main.Simulator;
import eu.opends.traffic.FollowBox;
import eu.opends.traffic.FollowBoxSettings;
import eu.opends.traffic.TrafficCar;
import eu.opends.traffic.Waypoint;

public class ProtobufFollowBox extends FollowBox {

    ProtobufClient protobufClient;
    Simulator sim;
    TrafficCar vehicle;
    
    public ProtobufFollowBox(Simulator sim, TrafficCar vehicle, FollowBoxSettings settings, ProtobufClient protoClient) {
        this(sim, vehicle, settings);
        this.protobufClient = protoClient;
        
    }
    public ProtobufFollowBox(Simulator sim, final TrafficCar vehicle, FollowBoxSettings settings) {
        super(sim, vehicle, settings);
        this.sim = sim;
        protobufClient = sim.getProtobufClient();
        this.vehicle = vehicle;
        protobufClient.registerCallback(vehicle.getId(),new PlanCallback(){
          
//            @Override
//            public void uploadPlan(Plan plan){
//                System.out.println("New plan for "+ProtobufFollowBox.this.vehicle.getName()+" : "+plan);
//                System.out.println("TIME DELAY: unknown : needs to update protofile");
//                Waypoint currentWP = getCurrentPositionWP(); 
//               Waypoint lastWP = waypointList.get(waypointList.size()-1);
//               waypointList.add(currentWP);
//               waypointList = generateWPs(plan);
//               waypointList.add(lastWP);
//               
//               //setToWayPointIndex = -1;
//               
//               loadNewWPS();
//               //setToWayPoint(0);
//           }

//            @Override
//            public void uploadPlan(Collection<cz.agents.highway.storage.Action> plan) {
//                System.out.println("New plan for "+ProtobufFollowBox.this.vehicle.getName()+" : "+plan);
//                System.out.println("TIME DELAY: "+ (ProtobufFollowBox.this.sim.getTimer().getTimeInSeconds() - plan.iterator().next().getTimeStamp()) );
//                Waypoint currentWP = getCurrentPositionWP(); 
//               Waypoint lastWP = waypointList.get(waypointList.size()-1);
//               waypointList.add(currentWP);
//               waypointList = generateWPs(plan);
//               waypointList.add(lastWP);
//               
//               //setToWayPointIndex = -1;
//               vehicle.setGasPedalIntensity((float) plan.iterator().next().getGas());
//
//               loadNewWPS();
//               //setToWayPoint(0);                
//            }
            @Override
            public void uploadPlan(Collection<cz.agents.highway.storage.plan.Action> plan) {
                System.out.println("New plan for "+ProtobufFollowBox.this.vehicle.getId()+" : "+plan);
                System.out.println("TIME DELAY: "+ (ProtobufFollowBox.this.sim.getTimer().getTimeInSeconds() - plan.iterator().next().getTimeStamp()) );
                Waypoint currentWP = getCurrentPositionWP(); 
                int idxCurrWP = getIndexOfWP(getCurrentWayPoint().getName());
               Waypoint lastWP = waypointList.get(waypointList.size()-1);
             waypointList.clear();
            //   waypointList.add(currentWP);
              // System.out.println("current WP : "+currentWP);
               for (cz.agents.highway.storage.plan.Action action : plan) {
                   WPAction wpAction = (WPAction)action;
                   System.out.println("wp = "+wpAction.getPosition());
                   Waypoint wp = new Waypoint("wp", new Vector3f(currentWP.getPosition().y-(float)(wpAction.getPosition().y*100),0,currentWP.getPosition().z-(float)(wpAction.getPosition().x*100)),(float)wpAction.getSpeed()*10000, null);
                   waypointList.add(wp);
                  System.out.println("new WP : "+wp);
               }
               
              // waypointList = generateWPs(plan);
              // waypointList.add(lastWP);
              // System.out.println("last WP : "+lastWP);
               
               //setToWayPointIndex = -1;
               //vehicle.setGasPedalIntensity((float) plan.iterator().next().getGas());

               loadNewWPS();
               //setToWayPoint(0);                
            }

        });
        
    }

     protected Waypoint getCurrentPositionWP() {
      return new Waypoint("WP-currPosition",vehicle.getPosition(),vehicle.getCurrentSpeedMs(), null);
    }
    private void loadNewWPS() {
        //super.motionPath = new MotionPath();
        motionPath.clearWayPoints();
        //motionPath.setCycle(settings.isPathCyclic());
       
        for(Waypoint wayPoint : waypointList)
            motionPath.addWayPoint(wayPoint.getPosition());

     
       // motionPath.setPathSplineType(SplineType.CatmullRom); // --> default: CatmullRom
       // motionPath.setCurveTension(settings.getCurveTension());
        
//        if(settings.isPathVisible())
//           // motionPath.enableDebugShape(sim.getAssetManager(), sim.getSceneNode());
        
        motionPath.addListener(new MotionPathListener() 
        {
            public void onWayPointReach(MotionEvent control, int wayPointIndex) 
            {
                // set speed limit for next way point
                int index = wayPointIndex % waypointList.size();
                float speed = waypointList.get(index).getSpeed();
                setSpeed(speed*3.6f);
                
                // if last way point reached
                if (motionPath.getNbWayPoints() == wayPointIndex + 1) 
                {
                    // reset vehicle to first way point if not cyclic
                    if(!motionPath.isCycle())
                        setToWayPoint(0);
                }
            }

           
        });
//
//        followBox = createFollowBox() ;
       // motionControl = new MotionTrack(followBox,motionPath);
        
        // get start way point
       // int startWayPointIndex = 0;
        
        // set start speed
        //float initialSpeed = vehicle.getCurrentSpeedKmh();
        //setSpeed(initialSpeed);
        
        // set start position
       // setToWayPoint(startWayPointIndex);
        
        // move object along path considering rotation
        //motionControl.setDirectionType(MotionTrack.Direction.PathAndRotation);
        
        // loop movement of object
       // motionControl.setLoopMode(LoopMode.Loop);
        motionControl.setPath(motionPath);
        motionControl.setCurrentWayPoint(0);
        
        // place follow box at beginning of line currentWP-nextWP
        motionControl.setCurrentValue(0);
        setSpeed(waypointList.get(0).getSpeed()*3.6f);
    }

    protected List<Waypoint> generateWPs(Collection<cz.agents.highway.storage.plan.Action> plan) {
        ArrayList<Waypoint> wps = new ArrayList<Waypoint>();
        for (cz.agents.highway.storage.plan.Action action : plan) {
            ManeuverAction manAction = (ManeuverAction)action;
            int lane = manAction.getLane();
            float speed = (float) manAction.getSpeed();
            float t = ((float)manAction.getDuration())/1000.0f; // convert to seconds
            
            Vector3f currentPosition = super.getPosition();
            //Quaternion currentHeading = vehicle.getRotation();
            float currentSpeed = vehicle.getCurrentSpeedMs();
            int currentLane = sim.getLaneAgent().getCarLane(vehicle.getId());
            float laneWidth = sim.getLaneAgent().getLaneWidth();
            
            float constantAcc = (speed - currentSpeed) / t;
            float s = 0.5f * constantAcc * t*t + currentSpeed * t;
            System.out.println("s ="+s+" v="+speed+" a="+constantAcc);
            System.out.println("Position:"+currentPosition);
            //Map<String, LaneLimit> laneList = sim.getDrivingTask().getScenarioLoader().getLaneList();
           
            Vector3f position = new Vector3f(currentPosition);
            int laneChange = (currentLane - lane);
            
           position=  position.add(laneChange*laneWidth, 0, -s);
            String name = "WayPoint_x";
            System.out.println("New WP pos:"+position);
            wps.add(new Waypoint(name, position, speed, null));
        }
        return wps;
    }

}
