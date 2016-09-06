package cz.agents.highway.agent;

import cz.agents.alite.common.entity.Entity;
import cz.agents.highway.storage.VehicleActuator;
import cz.agents.highway.storage.VehicleSensor;
import org.apache.log4j.Logger;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

public class Agent extends Entity {

    int id;
    private static final Logger logger = Logger.getLogger(Agent.class);

    protected VehicleSensor sensor;
    protected VehicleActuator actuator;
    protected final RouteNavigator navigator;


    public Agent(int id) {
        super("" + id);
        this.id = id;
        navigator = new RouteNavigator(id);
        logger.info("Agent " + id + " created");
    }

    public void addSensor(final VehicleSensor sensor) {
        this.sensor = sensor;
        logger.info("Sensor added: " + sensor);
    }

    public void addActuator(VehicleActuator actuator) {
        this.actuator = actuator;
        logger.info("Actuator added: " + actuator);
    }

    public RouteNavigator getNavigator() {
        return navigator;
    }

    public Point3f getInitialPosition() {
        return new Point3f(0, 0, 0);
    }

    public Vector3f getInitialVelocity() {
        return navigator.getInitialVelocity();
    }

}
