package cz.agents.alite.protobuf;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import cz.agents.alite.protobuf.communicator.ClientCommunicator;
import cz.agents.highway.protobuf.generated.simplan.PlanMessage;
import cz.agents.highway.storage.plan.WPAction;
import eu.opends.traffic.TrafficCarData;
import org.apache.log4j.Logger;

import cz.agents.alite.protobuf.communicator.Communicator;
import cz.agents.alite.protobuf.factory.ProtobufFactory.ProtobufMessageHandler;
import cz.agents.alite.transport.SocketTransportLayer;
import cz.agents.highway.protobuf.factory.simplan.InitFactory;
import cz.agents.highway.protobuf.factory.simplan.PlansFactory;
import cz.agents.highway.protobuf.factory.simplan.UpdateFactory;
import cz.agents.highway.protobuf.generated.simplan.MessageContainer.Header;
import cz.agents.highway.protobuf.generated.simplan.MessageContainer.Message;
import cz.agents.highway.storage.InitIn;
import cz.agents.highway.storage.RadarData;
import cz.agents.highway.storage.RoadObject;
import cz.agents.highway.storage.plan.PlansOut;
import eu.opends.car.SteeringCar;
import eu.opends.main.Simulator;
import eu.opends.traffic.PhysicalTraffic;
import eu.opends.traffic.TrafficCar;

public class ProtobufClient {
    /*
     * ProtobufClient is responsible for communication via ProtocolBuffers When initialized init()
     * it connects to a server and sends Init Message. The update(CarsIn cars) sends Update message,
     * while message handler receives Plan messages.
     */

    // TODO closing, include in settings

    private final static Logger logger = Logger.getLogger(ProtobufClient.class);

    Communicator<Header, Message> comm;

    float UPDATE_INTERVAL = 0.2f;
    float lastUpdateTime = 0;

    private SocketTransportLayer transport;

    private String uri;
    private Simulator simulator;

    private boolean connected;

    private Map<Integer, PlanCallback> registeredCBs;

    public ProtobufClient(Simulator openDSSimulator, String uri) {
        this.uri = uri;
        simulator = openDSSimulator;

        registeredCBs = new HashMap<Integer, PlanCallback>();

    }

    private void sendUpdate(RadarData cars) throws IOException {
        if (this.connected) {
            try {
                comm.send(cars);
            } catch (IOException e) {
                logger.error("Proto Client disconnected");
                connected = false;
            }
        }
    }

    private void sendInit() throws IOException {
        ArrayList<Point3d> points = simulator.getLaneAgent().getHighwayPoints();
        InitIn inits = new InitIn(points);

        comm.send(inits);
    }

    public void init() {
        try {
            /*int id = 1;
            for (TrafficCar car : simulator.getPhysicalTraffic().getVehicleList()) {
                mapID2Name.put(id++, car.getName());
                logger.info("inserrted into map: " + car.getName() + " with id " + (id - 1)
                        + " at position: " + car.getPosition());

            }*/
            transport = new SocketTransportLayer();

            // The receiving thread will be start after initialization
            boolean receiveInThread = false;
            boolean sendInThread = true;

            comm = new ClientCommunicator<Header, Message>(uri, Header.getDefaultInstance(),
                    Message.getDefaultInstance(), transport, sendInThread, receiveInThread);
            comm.connect();
            comm.registerReceiveCallback(new PlansFactory(),
                    new ProtobufMessageHandler<PlansOut>() {
                        @Override
                        public void notify(PlansOut plans) {
                            // System.out.println("received Plans: \n");
                            logger.debug("Received plans...");
                            int i = 0;
                            PhysicalTraffic traffic = simulator.getPhysicalTraffic();
                            for (int carId : plans.getCarIds()) {
                                TrafficCar car = traffic.getTrafficCar(carId);
                                WPAction initAction = (WPAction) plans.getPlan(carId).iterator().next();

                                // This is the first plan (initialize)
                                if (simulator.getCar() == null && i == 0) {
                                    // First car will be the steering one
                                    Point3f pos = initAction.getPosition();
                                    SteeringCar steeringCar = new SteeringCar(carId,
                                            Util.highway2OpenDS(pos), simulator, PhysicalTraffic.getVehicleDataMap().get(carId));
                                    simulator.setSteeringCar(steeringCar);
                                } else if (car == null && i != 0) {
                                    TrafficCarData carData = PhysicalTraffic.getVehicleDataMap().get(carId);
                                    Point3f pos = initAction.getPosition();
                                    TrafficCar newCar = new TrafficCar(carId, simulator,
                                            Util.highway2OpenDS(pos), carData);
                                    traffic.getVehicleMap().put(carId, newCar);
                                } else if (carId != simulator.getCar().getId() && car != null) {
                                    logger.info("uploading plan for " + carId);
                                    registeredCBs.get(carId).uploadPlan(
                                            plans.getPlan(carId));
                                }
                                i++;
                            }
                            // run = false;
                            logger.debug("END .. received plans.");
                        }
                    });
            comm.registerReceiveCallback(new UpdateFactory(),
                    new ProtobufMessageHandler<RadarData>() {

                        @Override
                        public void notify(RadarData updates) {
                            logger.debug("Received update...");
                            if (updates != null) {
                                HashMap<Integer, TrafficCar> ghosts = simulator.getPhysicalTraffic().getGhosts();
                                for (RoadObject vehicle : updates.getCars()) {
                                    if (ghosts.containsKey(vehicle.getId())) {
                                        // TODO: Create method update ghost
                                        TrafficCar ghost = ghosts.get(vehicle.getId());
                                        Vector3f newPos = Util.highway2OpenDS(vehicle.getPosition());
                                        newPos.setY(ghost.getPosition().y);
                                        ghost.setPosition(newPos);
                                        Quaternion rot = new Quaternion().fromAngleNormalAxis(FastMath.PI, Vector3f.UNIT_Y);

                                        ghost.setRotation(rot);
                                    } else {
                                        System.out.println("Ghost not found: "+vehicle.getId());

                                        // Ghost initialization
                                        // Create new ghost
                                        TrafficCar ghost = new TrafficCar(vehicle.getId(), simulator,
                                                Util.highway2OpenDS(vehicle.getPosition()),
                                                PhysicalTraffic.getVehicleDataMap().get(vehicle.getId()));
                                        ghosts.put(vehicle.getId(), ghost);

                                    }

                                }
                                // run = false;
                                logger.debug("END .. received updates.");
                            }
                        }
                    });
            comm.registerOutFactory(new UpdateFactory());

            //sendInit();
            connected = true;

            // Receive the init plans
            comm.run();
            // Receive the init update
            comm.run();
            // Finally start the receiving thread
            comm.startReceiveThread();
        } catch (ConnectException e) {
            connected = false;
            logger.warn("Protobuf client could not connect to :" + uri);
        } catch (IOException e) {
            e.printStackTrace();
            connected = false;
        }
    }

   
    public void update(float tpf, float timeInSeconds) {
        // not try to send anything if not connected
        if (!connected) {

            return;
        }

        try {

            if (timeInSeconds - lastUpdateTime > UPDATE_INTERVAL) {
                RadarData carsIn = generateUpdate(timeInSeconds);

                // System.out.println("sending \n" + carsIn);
                sendUpdate(carsIn);
                lastUpdateTime = timeInSeconds;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private RadarData generateUpdate(float timeInSec) {
        logger.debug("Generating update...");
        RadarData carsIn = new RadarData();
        SteeringCar drivenCar = simulator.getCar();

        RoadObject steeringCar = new RoadObject(drivenCar.getId(), timeInSec, simulator.getLaneAgent().getCarLane(
                drivenCar.getId()), Util.openDS2Highway(drivenCar.getPosition()), Util.openDS2Highway(
                drivenCar.getCurrentSpeedMs(), drivenCar.getRotation()));
        carsIn.add(steeringCar);
        PhysicalTraffic traffic = simulator.getPhysicalTraffic();
        int c = 1;
        RoadObject carIn;
        for (TrafficCar car : traffic.getVehicleMap().values()) {
            carIn = new RoadObject(car.getId(), timeInSec, simulator.getLaneAgent().getCarLane(
                    car.getId()), Util.openDS2Highway(car.getPosition()), Util.openDS2Highway(
                    car.getCurrentSpeedMs(), car.getRotation()));
            carsIn.add(carIn);
        }

        // CarIn carIn = new CarIn(int id, int t,Point2d position, Point2d positionHigh,double
        // speed,int lane);
        logger.debug("END... Generating update...");
        return carsIn;
    }

    public void registerCallback(int id, PlanCallback planCallback) {
        registeredCBs.put(id, planCallback);
        System.out.println("registered callback for " + id);

    }

    public void close() {
        comm.close();
        
    }

}
