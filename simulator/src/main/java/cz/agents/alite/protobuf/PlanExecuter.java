package cz.agents.alite.protobuf;

import java.util.Collection;

import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import org.apache.log4j.Logger;

import com.jme3.cinematic.events.MotionEvent;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.control.AbstractControl;
import com.jme3.scene.shape.Box;

import cz.agents.highway.storage.plan.ActuatorsAction;
import cz.agents.highway.storage.plan.ManeuverAction;
import cz.agents.highway.storage.plan.WPAction;
import eu.opends.main.Simulator;
import eu.opends.traffic.FollowBox;
import eu.opends.traffic.FollowBoxSettings;
import eu.opends.traffic.TrafficCar;
import eu.opends.traffic.Waypoint;
import eu.opends.visualization.MaterialProvider;
import eu.opends.visualization.MaterialProvider.MaterialType;

public class PlanExecuter extends FollowBox {

    private static final Logger logger = Logger.getLogger(PlanExecuter.class);

    private static final float WP_DISTANCE = 1f;
    ProtobufClient protobufClient;
    Simulator sim;
    TrafficCar vehicle;
    private int actWPIndex = 0;
    private Waypoint actWP;

    public PlanExecuter(Simulator sim, final TrafficCar vehicle, FollowBoxSettings settings) {
        super(sim, vehicle, settings);

        this.sim = sim;
        protobufClient = sim.getProtobufClient();
        this.vehicle = vehicle;
        this.actWP = waypointList.get(actWPIndex);

        // Initial position is already set by AgentDrive server
        // performWayPointChange(actWPIndex);
        
        Box box =new Box(0.2f, 0.2f, 0.20f);
        final Geometry actWPSpatial = new Geometry("actWPGeom", box);
        actWPSpatial.setMaterial(MaterialProvider.getInstance(sim).getTransparentMaterial(200, 0, 0, 0.4f, MaterialType.LIGHT));
        actWPSpatial.addControl(new AbstractControl() {
            
            @Override
            protected void controlUpdate(float tpf) {
                actWPSpatial.setLocalTranslation(actWP.getPosition());                
            }
            
            @Override
            protected void controlRender(RenderManager rm, ViewPort vp) {
                           
            }

            @Override
            public Control cloneForSpatial(Spatial spatial) {
                return null;
            }
        });
       // sim.getSceneNode().attachChild(actWPSpatial);
        
        protobufClient.registerCallback(vehicle.getId(), new PlanCallback() {

            @Override
            public void uploadPlan(Collection<cz.agents.highway.storage.plan.Action> plan) {
                // System.out.println("New plan for "+PB_WP_FollowBox.this.vehicle.getName()+" : "+plan);
                logger.info("TIME DELAY: "
                        + (PlanExecuter.this.sim.getTimer().getTimeInSeconds() - plan.iterator()
                                .next().getTimeStamp()));
                Waypoint lastWP = waypointList.get(waypointList.size()-1);
//                if(lastWP.getPosition().x <0 && vehicle.getPosition().x>0)return;
//                if(lastWP.getPosition().x >0 && vehicle.getPosition().x<0)return;

                
                // System.out.println("Current pos: "+currentWP.getPosition());
                waypointList.clear();
                for (cz.agents.highway.storage.plan.Action action : plan) {
                    if (action.getClass().equals(ManeuverAction.class)) {
                        ManeuverAction manAction = (ManeuverAction)action;
                        
                        waypointList.add(generateWP(manAction));
                        logger.info("Added ManeuverAction to plan.");
                    } else if (action.getClass().equals(WPAction.class)) {
                        WPAction wpAction = (WPAction) action;
                        waypointList.add(new Waypoint("wp" + waypointList.size(), Util
                                .highway2OpenDS(wpAction.getPosition()), (float) wpAction
                                .getSpeed() * 3.6f, null));
                        logger.info("Added WPAction to plan.");

                    }else if(action.getClass().equals(ActuatorsAction.class)){
                        //TODO
                    }

                }
                waypointList.add(lastWP);
                actWPIndex = 0;
                actWP = waypointList.get(actWPIndex);
                
                logger.info("ACtual WP ="+actWP);
            }

            

        });

        logger.info("PB_WP_created fo vehicle" + vehicle.getId());
    }

    protected Waypoint generateWP(ManeuverAction manAction) {
        float timeStamp = (float) manAction.getTimeStamp();
        float currTime = sim.getTimer().getTimeInSeconds();
              
        int lane = manAction.getLane();
        float speed = (float) manAction.getSpeed();
        float t = (float)manAction.getDuration()/1000f ;//- (currTime - timeStamp); // convert to seconds
        
       
        
        Vector3f currentPosition = vehicle.getPosition();
        //Quaternion currentHeading = vehicle.getRotation();
        float currentSpeed = vehicle.getCurrentSpeedMs();
        int currentLane = sim.getLaneAgent().getCarLane(vehicle.getId());
        float laneWidth = sim.getLaneAgent().getLaneWidth();
        
        float constantAcc = (speed - currentSpeed) / t;
        float s = 0.5f * constantAcc * t*t + currentSpeed * t;
      
        float perfectX = -(laneWidth/2)+lane*laneWidth;
        float P_val = 1 - Math.max(0, ( 20f - s )/20f ); 
        
        float delta = (perfectX - currentPosition.x) * P_val;
       Vector3f position =  new Vector3f(currentPosition.x+delta, 0, currentPosition.z+s);
       
       return new Waypoint("wp"+manAction.getCarId(), position, 3.6f*speed, null);
    }

    @Override
    public Vector3f getPosition() {
        if (actWP == null) {
            actWP = waypointList.get(actWPIndex);
        }
        return actWP.getPosition();
    }

    @Override
    public void update(Vector3f vehiclePos) {

        if (vehiclePos.distance(actWP.getPosition()) < WP_DISTANCE
                && waypointList.size() > actWPIndex + 1) {
            actWP = waypointList.get(++actWPIndex);

        }

    }

    @Override
    public void setToWayPoint(int index) {
        // TODO Auto-generated method stub
        super.setToWayPoint(index);
    }

    @Override
    public int getIndexOfWP(String wayPointID) {
        // TODO Auto-generated method stub
        return super.getIndexOfWP(wayPointID);
    }

    @Override
    public float getHeadingAtWP(int index) {
        // TODO Auto-generated method stub
        return super.getHeadingAtWP(index);
    }

    @Override
    public Waypoint getCurrentWayPoint() {
        // TODO Auto-generated method stub
        return actWP;
        // return super.getCurrentWayPoint();
    }

    @Override
    public Waypoint getNextWayPoint() {
        if (waypointList.size() > actWPIndex + 1) {
            return waypointList.get(actWPIndex + 1);
        } else
            return null;
    }

    @Override
    public Waypoint getNextWayPoint(int index) {
        if (waypointList.size() > index + 1) {
            return waypointList.get(index + 1);
        } else
            return null;
    }

    @Override
    public float getSpeed() {
        return actWP.getSpeed();
        // TODO Auto-generated method stub
        // return super.getSpeed();
    }

    @Override
    public void setSpeed(float speedKmh) {
        // TODO Auto-generated method stub
        super.setSpeed(speedKmh);
    }

    @Override
    public MotionEvent getMotionControl() {
        // TODO Auto-generated method stub
        return super.getMotionControl();
    }

    @Override
    public float getReducedSpeed() {
        return getSpeed();
    }

    protected Waypoint getCurrentPositionWP() {
        return new Waypoint("WP-currPosition", vehicle.getPosition(), vehicle.getCurrentSpeedMs(),
                null);
    }
    
    

}
