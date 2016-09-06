package cz.agents.highway.agent;

import cz.agents.highway.environment.roadnet.Edge;
import cz.agents.highway.environment.roadnet.Lane;
import cz.agents.highway.environment.roadnet.Network;
import cz.agents.highway.environment.roadnet.XMLReader;

import javax.vecmath.Point2f;
import javax.vecmath.Vector3f;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Class used for car navigation on given route
 * Created by wmatex on 15.7.14.
 */
public class RouteNavigator {
    private final int id;

    private int CP_pointPtr;
    private int CP_routePtr;
    private Lane CP_agentLane;
    private int pointPtr;
    private int routePtr;
    private Lane agentLane;

    /// Route represented as a list of edges, that the car should visit
    private final List<Edge> route = new ArrayList<Edge>();

    public RouteNavigator(int id) {
        this.id = id;
        initRoute(id);
    }

    public void reset() {
        pointPtr = 0;
        routePtr = 0;
        agentLane = route.get(0).getLaneByIndex(0);
    }

    /**
     * Generate list of edges from route definition
     *
     * @param id Id of the vehicle
     */
    private void initRoute(int id) {
        Network network = Network.getInstance();
        XMLReader reader = XMLReader.getInstance();
        Map<Integer, List<String>> routes = reader.getRoutes();
        Map<String, Edge> edges = network.getEdges();

        for (String edge : routes.get(id)) {
            route.add(edges.get(edge));
        }

        routePtr = 0;
        agentLane = route.get(0).getLaneByIndex(0);
    }

    public void changeLaneLeft() {
        Lane leftLane = agentLane.getLaneLeft();
        if (leftLane != null) {
            agentLane = leftLane;
        }
    }

    public void changeLaneRight() {
        Lane rightLane = agentLane.getLaneRight();
        if (rightLane != null) {
            agentLane = rightLane;
        }
    }

    /**
     * Method for advancing in route. First it checks if there is the end of the lane, if it is than try to switch lanes.
     * if this does not succeed than tries to switch to the another edge.
     */
    public void advanceInRoute() {
        if (pointPtr >= agentLane.getInnerPoints().size() - 1) {
            // We are at the end of the lane
            if (routePtr >= route.size() - 1) { // end of the plan
                routePtr = 0;
                pointPtr = 0;
                agentLane = route.get(0).getLaneByIndex(0);
            } else {
                Lane nextLane = getNeighbourLane(route.get(routePtr)); //check for neighbour lane

                int desiredPoint = getDesiredNeighbourLinePoint(nextLane);
                if (desiredPoint == -1)  //neighbour lane is shorter than my lane or does not exist
                {
                    nextLane = getFollowingLane(route.get(routePtr + 1));
                    if (nextLane != null) {
                        pointPtr = 0;
                        routePtr++;
                        agentLane = nextLane;
                    } else {
                        // TODO: This or neigbour lanes don't continue to the route edge
                    }
                } else {
                    pointPtr = desiredPoint;
                    agentLane = nextLane;
                }
            }
        } else {
            pointPtr++;
        }

    }

    private int getDesiredNeighbourLinePoint(Lane nextLane) {
        if (nextLane == null) return -1;
        int idealDistanceAroundMe = 4;
        int ii = 0;
        Point2f pp = agentLane.getInnerPoints().get(pointPtr);

        while (pp.distance(nextLane.getInnerPoints().get(ii)) > idealDistanceAroundMe) {
            ii++;
            if (ii == nextLane.getInnerPoints().size()) return -1;

        }
        while (pp.distance(nextLane.getInnerPoints().get(ii)) <= idealDistanceAroundMe) {
            ii++;
            if (ii == nextLane.getInnerPoints().size()) return -1;
        }
        return ii;
    }

    private Lane getNeighbourLane(Edge edge) {
        Lane neighbourLane = agentLane.getLaneLeft();
        if (neighbourLane == null) {
            // Try right lane
            neighbourLane = agentLane.getLaneRight();
        }
        return neighbourLane;
    }

    private Lane getFollowingLane(Edge edge) {
        Lane nextLane = agentLane.getNextLane(edge);
        if (nextLane == null) {
            // Lane doesn't continue to the edge in route, maybe we should change lane
            // Try left lane
            Lane changeLane = agentLane.getLaneLeft();
            // nextLane = changeLane;
            while (changeLane != null) {
                nextLane = changeLane.getNextLane(edge);
                if (nextLane != null) {
                    break;
                }
                changeLane = changeLane.getLaneLeft();
            }

            if (nextLane == null) {
                // Try right lane
                changeLane = agentLane.getLaneRight();
                while (changeLane != null) {
                    nextLane = changeLane.getNextLane(edge);
                    if (nextLane != null) {
                        break;
                    }
                    changeLane = changeLane.getLaneRight();
                }
            }
        }

        return nextLane;
    }

    public Point2f getRoutePoint() {
        return agentLane.getInnerPoints().get(pointPtr);
    }

    public Point2f getInitialPosition() {
        return route.get(0).getLanes().values().iterator().next().getInnerPoints().get(0);
    }

    public Vector3f getInitialVelocity() {
        Point2f p1 = route.get(0).getLanes().values().iterator().next().getInnerPoints().get(0);
        Point2f p2 = route.get(0).getLanes().values().iterator().next().getInnerPoints().get(1);
        return new Vector3f(p2.x - p1.x, p2.y - p1.y, 0);
    }

    public Point2f next() {
        Point2f p = getRoutePoint();
        advanceInRoute();
        return p;
    }

    public Point2f nextWithReset() {
        int OLDpointPtr = pointPtr;
        int OLDroutePtr = routePtr;
        Lane OLDagentLane = agentLane;
        Point2f p = getRoutePoint();
        advanceInRoute();
        pointPtr = OLDpointPtr;
        routePtr = OLDroutePtr;
        agentLane = OLDagentLane;
        return p;
    }

    public void setCheckpoint() {
        CP_agentLane = agentLane;
        CP_pointPtr = pointPtr;
        CP_routePtr = routePtr;
    }

    public void resetToCheckpoint() {
        agentLane = CP_agentLane;
        pointPtr = CP_pointPtr;
        routePtr = CP_routePtr;
    }

    public String getUniqueLaneIndex() {
        return agentLane.getLaneId();
    }

    public Lane getLane() {
        return agentLane;
    }

    public int getActualPointer() {
        return pointPtr;
    }

    public List<Edge> getFollowingEdgesInPlan() {
        List<Edge> rem = new ArrayList<Edge>();

        for (int i = routePtr + 1; i < route.size(); i++) {
            rem.add(route.get(i));
        }
        return rem;
    }

    public List<Edge> getRoute() {
        return route;
    }
}
