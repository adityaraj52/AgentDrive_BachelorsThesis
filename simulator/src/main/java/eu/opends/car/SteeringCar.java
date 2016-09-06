/*
 *  This file is part of OpenDS (Open Source Driving Simulator).
 *  Copyright (C) 2013 Rafael Math
 *
 *  OpenDS is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OpenDS is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with OpenDS. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.opends.car;

import java.util.ArrayList;
import java.util.Collection;

import com.jme3.scene.control.Control;
import cz.agents.alite.hmi.HMIControl;
import eu.opends.traffic.TrafficCarData;
import org.apache.log4j.Logger;

import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;

import cz.agents.alite.hmi.HMIProvider;
import cz.agents.alite.protobuf.PlanCallback;
import cz.agents.alite.protobuf.PlanExecuter;
import cz.agents.alite.protobuf.Util;
import cz.agents.highway.storage.plan.Action;
import cz.agents.highway.storage.plan.ManeuverAction;
import cz.agents.highway.storage.plan.WPAction;
import eu.opends.basics.SimulationBasics;
import eu.opends.drivingTask.DrivingTask;
import eu.opends.drivingTask.scenario.ScenarioLoader;
import eu.opends.drivingTask.scenario.ScenarioLoader.CarProperty;
import eu.opends.main.SimulationDefaults;
import eu.opends.main.Simulator;
import eu.opends.traffic.Waypoint;
import eu.opends.trafficObjectLocator.TrafficObjectLocator;

/**
 * Driving Car
 * 
 * @author Rafael Math
 */
public class SteeringCar extends Car {
    private TrafficObjectLocator trafficObjectLocator;

    private static final Logger logger = Logger.getLogger(SteeringCar.class);

    private ArrayList<Action> plan = new ArrayList<Action>();
    private Waypoint actWP = new Waypoint("", new Vector3f(), 0f, null);
    HMIProvider hmiProvider = null;
    PlanExecuter followBox = null;

    private final int id;

    public SteeringCar(int id, Vector3f initPos, Simulator sim, TrafficCarData trafficCarData) {
        this.id = id;
        this.sim = sim;

        DrivingTask drivingTask = SimulationBasics.getDrivingTask();
        ScenarioLoader scenarioLoader = drivingTask.getScenarioLoader();

        initialPosition = initPos;
//        initialPosition = scenarioLoader.getStartLocation();
//        if (initialPosition == null)
//            initialPosition = SimulationDefaults.initialCarPosition;

        this.initialRotation = scenarioLoader.getStartRotation();
        if (this.initialRotation == null)
            this.initialRotation = SimulationDefaults.initialCarRotation;

        // add start position as reset position
        Simulator.getResetPositionList().add(new ResetPosition(initialPosition, initialRotation));

        // TODO: Load as much as possible from traffic data instead of special driver data

//        mass = scenarioLoader.getChassisMass();
        mass = trafficCarData.getMass();

        minSpeed = scenarioLoader.getCarProperty(CarProperty.engine_minSpeed,
                SimulationDefaults.engine_minSpeed);
        maxSpeed = scenarioLoader.getCarProperty(CarProperty.engine_maxSpeed,
                SimulationDefaults.engine_maxSpeed);

        decelerationBrake = scenarioLoader.getCarProperty(CarProperty.brake_decelerationBrake,
                SimulationDefaults.brake_decelerationBrake);
        maxBrakeForce = 0.004375f * decelerationBrake * mass;

        decelerationFreeWheel = scenarioLoader.getCarProperty(
                CarProperty.brake_decelerationFreeWheel,
                SimulationDefaults.brake_decelerationFreeWheel);
        maxFreeWheelBrakeForce = 0.004375f * decelerationFreeWheel * mass;

        engineOn = scenarioLoader.getCarProperty(CarProperty.engine_engineOn,
                SimulationDefaults.engine_engineOn);
        showEngineStatusMessage(engineOn);

        transmission = new Transmission(this);
        powerTrain = new PowerTrain(this);

//        modelPath = scenarioLoader.getModelPath();
        modelPath = trafficCarData.getModelPath();

        init();

        // allows to place objects at current position
        trafficObjectLocator = new TrafficObjectLocator(sim, this);

        // followBox = new PB_WP_FollowBox(sim, this, null);

        // hmi navigation
        Box b = new Box(1f, 0.01f, 10f);
        final Geometry speedBox = new Geometry("WPBox", b);
        Vector3f pos = new Vector3f(0f, -0.35f, -12f);
        speedBox.setLocalTranslation(pos);

        Quaternion rot = new Quaternion(-0.015f, 0f, 0f, 1);
        speedBox.setLocalRotation(rot);
        Material mat = new Material(sim.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md"); // create
                                                                                                 // a
                                                                                                 // simple
                                                                                                 // material
        mat.setColor("Color", ColorRGBA.Blue);
        mat.setTransparent(true);
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        mat.setReceivesShadows(false);
        speedBox.setMaterial(mat);
        hmiProvider = new HMIProvider(speedBox);

        sim.getProtobufClient().registerCallback(id, new PlanCallback() {

            @Override
            public void uploadPlan(Collection<Action> newplan) {
                plan.clear();
                plan.addAll(newplan);
                Action action = plan.get(0);
                if (action.getClass().equals(ManeuverAction.class)) {
                    ManeuverAction manAction = (ManeuverAction)action;                    
                    actWP = generateWP(manAction);
                    
                } else if (action.getClass().equals(WPAction.class)) {
                WPAction act = (WPAction) action;
                actWP = new Waypoint("EGO wp", Util.highway2OpenDS(act.getPosition()), (float) act
                        .getSpeed() * 3.6f, null);
                }
                hmiProvider.setWP(actWP);

//                System.out.println("Steering Car new plan!");

            }
            

//            @Override
//            public void uploadPlan(Plan plan) {
//                // TODO Auto-generated method stub
//
//            }
        });

        Control arrowControl = new HMIControl(this, hmiProvider, sim.getLaneAgent());

        speedBox.addControl(arrowControl);

        this.getCarNode().attachChild(speedBox);

        
        
        
//        speedBox.addControl(new AbstractControl() {
//
//            @Override
//            protected void controlUpdate(float tpf) {
//
//                float k = (getCurrentSpeedKmh() - (actWP.getSpeed()));
//                k = k * 0.02f;
//
//                if (k > 0) {
//                    k = Math.min(0.8f, k);
//                    speedBox.getMaterial().setColor("Color", new ColorRGBA(1f, 0f, 0f, 0f + k));
//
//                } else {
//                    k = Math.max(-0.8f, k);
//
//                    speedBox.getMaterial().setColor("Color", new ColorRGBA(0f, 1f, 0f, 0f - k));
//
//                }
//            }
//
//            @Override
//            protected void controlRender(RenderManager rm, ViewPort vp) {
//                // TODO Auto-generated method stub
//
//            }
//        });
        //getCarNode().attachChild(speedBox);
    }

    public TrafficObjectLocator getObjectLocator() {
        return trafficObjectLocator;
    }

    // will be called, in every frame
    public void update(float tpf) {
        // accelerate
        float pAccel = 0;
        if (!engineOn) {
            // apply 0 acceleration when engine not running
            pAccel = powerTrain.getPAccel(tpf, 0) * 30f;
        } else if (isAutoAcceleration && (getCurrentSpeedKmh() < minSpeed)) {
            // apply maximum acceleration (= -1 for forward) to maintain minimum speed
            pAccel = powerTrain.getPAccel(tpf, -1) * 30f;
        } else {
            // apply acceleration according to gas pedal state
            pAccel = powerTrain.getPAccel(tpf, gasPedalPressIntensity) * 30f;
        }
        transmission.performAcceleration(pAccel);

        // brake lights
        setBrakeLight(brakePedalPressIntensity > 0);

        // brake
        float appliedBrakeForce = brakePedalPressIntensity * maxBrakeForce;
        float currentFriction = powerTrain.getFrictionCoefficient() * maxFreeWheelBrakeForce;
        carControl.brake(appliedBrakeForce + currentFriction);

        // lights
        leftHeadLight.setColor(ColorRGBA.White.mult(lightIntensity));
        leftHeadLight.setPosition(carModel.getLeftLightPosition());
        leftHeadLight.setDirection(carModel.getLeftLightDirection());

        rightHeadLight.setColor(ColorRGBA.White.mult(lightIntensity));
        rightHeadLight.setPosition(carModel.getRightLightPosition());
        rightHeadLight.setDirection(carModel.getRightLightDirection());

        trafficObjectLocator.update();

    }
    
    protected Waypoint generateWP(ManeuverAction manAction) {
        
        
        int lane = manAction.getLane();
        float speed = (float) manAction.getSpeed();
        float t = (float)manAction.getDuration(); // convert to seconds
        
        Vector3f currentPosition = getPosition();
        //Quaternion currentHeading = vehicle.getRotation();
        float currentSpeed = this.getCurrentSpeedMs();
        int currentLane = sim.getLaneAgent().getCarLane(sim.getCar().getId());
        float laneWidth = sim.getLaneAgent().getLaneWidth();
        
        float constantAcc = (speed - currentSpeed) / t;
        float s = 0.5f * constantAcc * t*t + currentSpeed * t;
//        System.out.println("s ="+s+" v="+speed+" a="+constantAcc);  
//        System.out.println("Position:"+currentPosition);
        //Map<String, LaneLimit> laneList = sim.getDrivingTask().getScenarioLoader().getLaneList();
       
       
        int laneChange = (currentLane - lane);
        
       Vector3f position =  new Vector3f(-(laneWidth/2)+lane*laneWidth, 0, s+getPosition().z);
        String name = "WayPoint_x";
//        System.out.println("New WP pos:"+position);
       return new Waypoint("wp"+manAction.getCarId(), position,  3.6f*speed, null);
    }

    public int getId() { return id; }

}
