package cz.agents.highway.environment.roadnet;

import ags.utils.dataStructures.KdTree;
import ags.utils.dataStructures.SquareEuclideanDistanceFunction;
import ags.utils.dataStructures.utils.MaxHeap;


import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton class holding the network data
 * It provides the following data: edges, junctions, lanes, connections, tunnels, bridges
 * It also provides a converter from x,y coordinates to a specific lane
 * <p/>
 * Created by pavel on 19.6.14.
 */
public class Network {
    private static Network instance = null;
    private HashMap<String, Edge> edges;
    private HashMap<String, Junction> junctions;
    private HashMap<String, Lane> lanes;
    private KdTree<Lane> kdTree;
    private SquareEuclideanDistanceFunction distanceFunction;
    private ArrayList<Connection> connections;
    private ArrayList<String> tunnels;
    private ArrayList<String> bridges;

    private Network() {
    }

    public static synchronized Network getInstance() {
        if (instance == null) {
            instance = new Network();
        }
        return instance;
    }


    /**
     * call this method to initialize the network structure
     *
     * @param edges
     * @param junctions
     * @param laneMap
     * @param connectionList
     * @param tunnelsRaw
     * @param bridgesRaw
     */
    public void init(HashMap<String, Edge> edges,
                     HashMap<String, Junction> junctions, HashMap<String, Lane> laneMap,
                     ArrayList<Connection> connectionList, ArrayList<String> tunnelsRaw, ArrayList<String> bridgesRaw) {
        this.edges = edges;
        this.junctions = junctions;
        this.lanes = laneMap;
        this.connections = connectionList;
        tunnels = createTunnelsAndBridges(tunnelsRaw);
        bridges = createTunnelsAndBridges(bridgesRaw);


        connectLanes();
        fillKdTree();
    }

    /**
     * converts raw data about bridges and tunnels from the osm file
     * to lists of edges which are actual bridges and tunnels in the network representation
     *
     * @param dataRaw
     * @return
     */
    private ArrayList<String> createTunnelsAndBridges(ArrayList<String> dataRaw) {
        ArrayList<String> ret = new ArrayList<String>();
        for (String t : dataRaw) {
            for (String k : edges.keySet()) {
                if (k.contains(t)) {
                    ret.add(k);
                }
            }
        }
        return ret;
    }


    /**
     * connects the created lanes using connections
     */
    private void connectLanes() {
        for (Connection c : connections) {
            lanes.get(createLaneId(c.getFrom(), c.getFromLane())).addOutgoingLane(lanes.get(createLaneId(c.getTo(), c.getToLane())));
            lanes.get(createLaneId(c.getTo(), c.getToLane())).addIncomingLane(lanes.get(createLaneId(c.getFrom(), c.getFromLane())));
        }
    }

    private String createLaneId(String edgeId, String laneIndex) {
        return edgeId + "_" + laneIndex;
    }


    /**
     * creates a kd-Tree and fills it with point - lane pairs
     * The kd-Tree is used for fast look up of the cars current lane based on its x,y coordinates only
     */
    private void fillKdTree() {
        kdTree = new KdTree(2);
        this.distanceFunction = new SquareEuclideanDistanceFunction();

        for (Map.Entry<String, Lane> entry : lanes.entrySet()) {
            for (Point2f p : entry.getValue().getInnerPoints()) {
                double[] point = new double[2];
                point[0] = p.x;
                point[1] = p.y;
                kdTree.addPoint(point, entry.getValue());
            }
        }
    }

    /**
     * returns the cars current lane based on its x,y coordinates
     * uses kd-Tree to obtain nearest neighbours of the given point
     *
     * @param position
     * @return
     */
    public Lane getLane(Point2f position) {
        double[] point = new double[2];
        point[0] = position.x;
        point[1] = position.y;
        MaxHeap<Lane> nearestNeighbour = kdTree.findNearestNeighbors(point, 1, distanceFunction);
        return nearestNeighbour.getMax();
    }

    /**
     * returns the cars current lane based on its x,y coordinates
     * uses kd-Tree to obtain nearest neighbours of the given point
     *
     * @param position
     * @return
     */
    public Lane getLane(Point3f position) {
        Point2f pos2d = new Point2f(position.x, position.y);
        return getLane(pos2d);
    }
    public int getLaneNum(Point3f position) {
       return getLane(position).getIndex();
    }

    public HashMap<String, Edge> getEdges() {
        return edges;
    }

    public HashMap<String, Junction> getJunctions() {
        return junctions;
    }

    public ArrayList<String> getBridges() {
        return bridges;
    }

    public HashMap<String, Lane> getLanes() {
        return lanes;
    }

    public ArrayList<String> getTunnels() {
        return tunnels;
    }

}
