package cz.agents.agentdrive.simulator.lite.storage.vehicle;

import cz.agents.highway.environment.roadnet.Lane;
import cz.agents.highway.environment.roadnet.Network;
import cz.agents.highway.protobuf.generated.simplan.VectorProto;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

/**
 * Class representing a car
 *
 * Created by wmatex on 3.7.14.
 */
public class Car extends Vehicle {
    public Car(int id, int lane, Point3f position, Vector3f heading, float velocity) {
        super(id, lane, position, heading, velocity);
    }

    /**
     * Update the car's position based on it's speed and previous position
     *
     * @param deltaTimeMs Time since last update
     */
    @Override
    public void update(long deltaTimeMs) {
        //update direction
        countSteeringAngle();

        updateLane();

        float x = getPosition().x;
        float y = getPosition().y;
        float z = getPosition().y;

        /// Compute the new velocity vector based on these equations: http://planning.cs.uiuc.edu/node658.html
        double angleSpeed = getVelocity() / getAxeLength() * Math.tan(getSteeringAngle());
        double xAngle = Math.atan2(getHeading().y, getHeading().x) + angleSpeed * deltaTimeMs / 1000f;
        double velocitySize = getVelocity();
        Vector3f velocity = new Vector3f((float) (velocitySize * Math.cos(xAngle)), (float) (velocitySize * Math.sin(xAngle)),
                getHeading().z * getVelocity());
        if (Float.isNaN(velocity.x) || Float.isNaN(velocity.y) || Float.isNaN(velocity.z)) {
            velocity = new Vector3f(0f, 0f, 0f);
        }
        float c = x;
        float d = y;
        float e = z;

        x = x + velocity.x * deltaTimeMs / 1000f;
        y = y + velocity.y * deltaTimeMs / 1000f;
        z = z + velocity.z * deltaTimeMs / 1000f;


        setVelocity(velocity.length());
        velocity.normalize();
        if (Float.isNaN(velocity.x) || Float.isNaN(velocity.y) || Float.isNaN(velocity.z)) {
            // velocity = new Vector3f(0f,0f,0f);
        } else {
            setHeading(velocity);
        }

        setPosition(new Point3f(x, y, z));
    }

    private void updateLane() {
        Network network = Network.getInstance();
        Point2f position2D = new Point2f(getPosition().x, getPosition().y);
        Lane lane = network.getLane(position2D);
        setLane(lane.getIndex());
    }
}
