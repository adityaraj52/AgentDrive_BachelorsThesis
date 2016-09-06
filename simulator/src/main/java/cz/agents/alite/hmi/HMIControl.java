package cz.agents.alite.hmi;



import com.jme3.math.ColorRGBA;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.control.AbstractControl;

import com.jme3.scene.control.Control;
import eu.opends.car.Car;
import eu.opends.laneAgent.LaneAgent;
import org.apache.log4j.Logger;

public class HMIControl extends AbstractControl {

    private static final Logger logger = Logger.getLogger(HMIControl.class);

    private static final boolean SHOW_HMI = false;
    
    private Car vehicle;
    private HMIProvider hmiProvider;
    private Geometry speedBox;
    
    int prevLane = 0;
    int lane = 0;

    private LaneAgent laneAgent;

    public HMIControl(Car vehicle, HMIProvider hmiProvider,LaneAgent laneAgent){
        this.vehicle  = vehicle;
        this.hmiProvider = hmiProvider;
        this.laneAgent = laneAgent;
        this.speedBox = hmiProvider.getSpeedBox();
    }
    @Override
    protected void controlRender(RenderManager arg0, ViewPort arg1) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void controlUpdate(float arg0) {
        
       
        
        prevLane = lane;
        lane = laneAgent.getLane(hmiProvider.getWPPos());
       // logger.info("Lane = "+lane);
//        System.out.println("HMI update "+lane+hmiProvider.getSpeed());
        laneAgent.showLaneSpatial(prevLane,false);
        if(SHOW_HMI){
            laneAgent.showLaneSpatial(lane,true);
            speedBox.setCullHint(CullHint.Never);
        }else{
        speedBox.setCullHint(CullHint.Always);
        }
        
        float k = (vehicle.getCurrentSpeedKmh()-(hmiProvider.getSpeed()));
        k = k *0.02f;
       
        if(k>0){
            k = Math.min(0.8f, k);
            speedBox.getMaterial().setColor("Color", new ColorRGBA(1f, 0f, 0f, 0f+k));

        }else{
            k = Math.max(-0.8f, k);
            
            speedBox.getMaterial().setColor("Color", new ColorRGBA(0f, 1f, 0f, 0f-k));

        }
    }


    @Override
    public Control cloneForSpatial(Spatial spatial) {
        return null;
    }
}
