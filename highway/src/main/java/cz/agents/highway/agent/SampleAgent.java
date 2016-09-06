/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.agents.highway.agent;

import cz.agents.alite.common.event.Event;
import cz.agents.highway.maneuver.CarManeuver;
import cz.agents.highway.maneuver.LaneLeftManeuver;
import cz.agents.highway.maneuver.StraightManeuver;
import cz.agents.highway.storage.HighwayEventType;
import cz.agents.highway.storage.RoadObject;
import cz.agents.highway.storage.VehicleSensor;
import cz.agents.highway.storage.plan.Action;

import java.util.Collection;

/**
 * @author schaefer
 */

public class SampleAgent extends Agent {
    private static final int SAFETY_RADIUS = 20;
    protected final ManeuverTranslator maneuverTranslator;
    private static final double[] AGENT_SPEEDS = {23, 3, 14, 5};
    public static final int max_speed = 50;

    public SampleAgent(int id) {
        super(id);
        maneuverTranslator = new ManeuverTranslator(id, navigator);
    }

    private CarManeuver plan() {
        CarManeuver sm = new StraightManeuver(0, AGENT_SPEEDS[id], 5, 1);
        Collection<RoadObject> collection = this.sensor.senseCars();
        RoadObject myState = sensor.senseCurrentState();
        State state = new State(collection,myState);
        System.out.println(id+" : "+state);
        if(state.frontAhead != null && distance(state.frontAhead,myState) < SAFETY_RADIUS ){
            if(state.frontLeft == null || distance(state.frontLeft,myState) > SAFETY_RADIUS){
                return new LaneLeftManeuver(myState.getLaneIndex()+1, myState.getVelocity().length(),myState.getPosition().y,1);
            }
                return new StraightManeuver(myState.getLaneIndex(),state.frontAhead.getVelocity().length(),myState.getPosition().y,1);
        }

        return sm;
    }

    private double distance(RoadObject carA, RoadObject carB) {
        return Math.abs(carA.getPosition().y - carB.getPosition().y);
    }


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

    public Action agentReact() {
        return maneuverTranslator.translate(plan());
    }

    /**
     * Class representing the neighborhood of an object on highway, it stores the closest objects in 5 directions -
     * frontAhead, frontLeft, frontRight, rearRight, rearLeft.
     *
     * It can be used for reactive reasoning on highway, when only the nearest vehicles are considered (transitivity to next vehicles is assumed to avoid collisions)
     *
     */
    class State {
        RoadObject frontLeft;
        RoadObject frontAhead;
        RoadObject frontRight;
        RoadObject rearLeft;
        RoadObject rearRight;

        @Override
        public String toString() {
            return "State{" +
                    "frontLeft=" + frontLeft +
                    ", frontAhead=" + frontAhead +
                    ", frontRight=" + frontRight +
                    ", rearLeft=" + rearLeft +
                    ", rearRight=" + rearRight +
                    '}';
        }

        
        public State(Collection<RoadObject> cars, RoadObject ego_state) {
            for (RoadObject car : cars) {
                if(car.getId() == ego_state.getId()) continue; //do not consider yourself
                int myLane = ego_state.getLaneIndex();
                int otherCarLane = car.getLaneIndex();
                if (myLane == otherCarLane) { // same lane
                    if (isAhead(car, ego_state)) {
                        if (frontAhead == null || isAhead(frontAhead, car)) {
                            frontAhead = car;
                        }
                    }
                } else if (myLane + 1 == otherCarLane) { //left lane
                    if (isAhead(car, ego_state)) {
                        if( frontLeft == null || isAhead(frontLeft, car)){
                            frontLeft = car;
                        }
                    }else{
                        if( rearLeft == null || isAhead(car, rearLeft)){
                            rearLeft = car;
                        }
                    }
                } else if (myLane - 1 == otherCarLane) { //right lane
                    if (isAhead(car, ego_state)) {
                        if( frontRight == null || isAhead(frontRight, car)){
                            frontRight = car;
                        }
                    }else{
                        if( rearRight == null || isAhead(car, rearRight)){
                            rearRight = car;
                        }
                    }
                }
            }


        }

        /**
         * Determines what of two RoadObjects is ahead in the road - WORKING ONLY FOR STRAIGHT VERTICAL HIGHWAY
         * @param first
         * @param second
         * @return true is first vehicles is ahead the second vehicle, false otherwise
         */
        private boolean isAhead(RoadObject first, RoadObject second) {
            return (first.getPosition().y - second.getPosition().y) <= 0;
        }
    }

}
