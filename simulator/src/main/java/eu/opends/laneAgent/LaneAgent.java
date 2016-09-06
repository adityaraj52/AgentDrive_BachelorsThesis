package eu.opends.laneAgent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import cz.agents.alite.protobuf.Util;
import cz.agents.highway.environment.roadnet.Network;
import cz.agents.highway.environment.roadnet.XMLReader;
import cz.agents.highway.util.Utils;
import eu.opends.car.Car;
import org.apache.log4j.Logger;

import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Box;

import eu.opends.environment.LaneLimit;
import eu.opends.main.Simulator;
import eu.opends.traffic.TrafficCar;

public class LaneAgent {
    
    private final static Logger logger = Logger.getLogger(LaneAgent.class);
    Simulator sim;
    Map<String, LaneLimit> laneLimits;
    Map<String, Integer> mapNameToID;
    Map<Integer, String> mapIDToName;
    Map<String, Spatial> laneGeometries;

    final String roadNetFolder = "nets/highway-straight/";


    private String driverName;


    //TODO class has to reimplemented- some of the parts related to laneLimits are deprecated, it should be bouild on data from roadNetwork

    public LaneAgent(Simulator sim) {
        this.sim = sim;
        laneLimits = Simulator.getDrivingTask().getScenarioLoader().getLaneList();
        mapNameToID = new HashMap<String, Integer>(laneLimits.size());
        mapIDToName = new HashMap<Integer, String>(laneLimits.size());
        laneGeometries = new HashMap<String, Spatial>(laneLimits.size());
        driverName = Simulator.getDrivingTask().getScenarioLoader().getDriverCarName();

        for (Entry<String, LaneLimit> entry : laneLimits.entrySet()) {
            mapNameToID.put(entry.getKey(), entry.getValue().getID());
            mapIDToName.put(entry.getValue().getID(), entry.getKey());
            laneGeometries.put(entry.getKey(), getLaneGeometry(entry.getKey()));
        }
        logger.warn("RoadNetwork XML read from "+roadNetFolder);
        XMLReader reader = XMLReader.getInstance();
        reader.read(roadNetFolder);
    }

    public int getCarLane(int carId) {

        Car car;
        if (carId == sim.getCar().getId()) {
            car = sim.getCar();
        } else {
            car = sim.getPhysicalTraffic().getTrafficCar(carId);
        }
        Point3f positionInHighwayCoordinates = Util.openDS2Highway(car.getPosition());
        int lane = Network.getInstance().getLaneNum(positionInHighwayCoordinates);

        return lane;
    }

    private String getLane(float currentX){

        Iterator<Entry<String, LaneLimit>> it = laneLimits.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, LaneLimit> pairs = (Entry<String, LaneLimit>) it.next();
            String laneID = pairs.getKey();
            LaneLimit laneLimit = pairs.getValue();

            if (currentX >= laneLimit.getXMin() && currentX <= laneLimit.getXMax())
                return laneID;
        }

        return null;
    }

    public float getLaneWidth() {
        if (!laneLimits.isEmpty()) {
            LaneLimit lane = laneLimits.values().iterator().next();
            return lane.getXMax() - lane.getXMin();
        } else
            return 0;

    }

    public ArrayList<Point3d> getHighwayPoints() {
        boolean OBSTACLES = false;
        
        // Point3d X,Y, numOfLane
        double numOfLanes = 2;
        double obstacle1 = 300;
        double obstacle2 = 600;
        double obstacleLength = 20;

        ArrayList<Point3d> highwayPoints = new ArrayList<Point3d>();
        LaneLimit lane = laneLimits.get(mapIDToName.get(0)); //TODO should be 2 for AEB
        float x = lane.getXMax();
        
        
        
if(OBSTACLES){
        highwayPoints.add(new Point3d(0, x, numOfLanes));
        highwayPoints.add(new Point3d(obstacle1, x, numOfLanes - 1));
        highwayPoints.add(new Point3d(obstacle1 + obstacleLength, x, numOfLanes));
        highwayPoints.add(new Point3d(obstacle2, x, numOfLanes - 1));
        highwayPoints.add(new Point3d(obstacle2 + obstacleLength, x, numOfLanes));
        highwayPoints.add(new Point3d(obstacle2 + obstacleLength, x, numOfLanes));
}else{
   
    highwayPoints.add(new Point3d(0, x, numOfLanes));
    highwayPoints.add(new Point3d(24000, x, numOfLanes));
    
}

        return highwayPoints;
    }

    private Spatial getLaneGeometry(String lane) {
        LaneLimit laneLimit = laneLimits.get(lane);
        float width = laneLimit.getXMax() - laneLimit.getXMin();
        Box b = new Box(width/2, 0.1f, 10000f);
        Geometry box = new Geometry("LaneBox"+lane, b);
        box.setLocalTranslation( laneLimit.getXMin() + (width / 2), 0.05f, 0f);
        
        Material mat = new Material(sim.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md"); // create
                                                                                                 // a
                                                                                                 // simple
                                                                                                 // material
        mat.setColor("Color", new ColorRGBA(0f+box.getLocalTranslation().z, 0f, 1f, 0.2f));
        mat.setTransparent(true);
        mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        mat.setReceivesShadows(false);
        box.setMaterial(mat);
        box.setCullHint(CullHint.Always);
//        sim.getSceneNode().attachChild(box);
        return box;
    }

    public void showLaneSpatial(int lane, boolean show) {
        Spatial spatial = laneGeometries.get(mapIDToName.get(lane));
        if(spatial ==null){
            return;
        }
        if (show) {
            spatial.setCullHint(CullHint.Dynamic);
        } else {
            spatial.setCullHint(CullHint.Always);
        }
    }

    public int getLane(Vector3f wpPos) {
        if(wpPos ==null){
            return -1;
        }
      String lane = getLane(wpPos.getX());
      if(lane==null){
          return -1;
      }
        return mapNameToID.get(lane);
        
    }
}
