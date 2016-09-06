package cz.agents.alite.hmi;

import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;

import eu.opends.traffic.Waypoint;

public class HMIProvider {
    
    private Geometry speedBox;
    private Waypoint wp;

    public HMIProvider(Geometry arrow){
        this.speedBox = arrow;
        this.wp = new Waypoint("Default", new Vector3f(), 0.0f,null);
    }

    

    public void setWP(Waypoint actWP) {
        this.wp = actWP;        
    }
    
    public Vector3f getWPPos(){
        return wp.getPosition();
    }
    public float getSpeed(){
        return wp.getSpeed();
    }

    public Geometry getSpeedBox() {
        return speedBox;
    }
}
