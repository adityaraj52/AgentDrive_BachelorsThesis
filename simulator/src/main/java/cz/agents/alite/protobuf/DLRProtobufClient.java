package cz.agents.alite.protobuf;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import cz.agents.alite.protobuf.communicator.ClientCommunicator;
import cz.agents.alite.protobuf.communicator.Communicator;
import cz.agents.alite.protobuf.factory.ProtobufFactory.ProtobufMessageHandler;
import cz.agents.alite.transport.SocketTransportLayer;
import cz.agents.highway.protobuf.factory.dlr.DLR_PlansFactory;
import cz.agents.highway.protobuf.factory.dlr.DLR_UpdateFactory;
import cz.agents.highway.protobuf.generated.dlr.DLR_MessageContainer.Header;
import cz.agents.highway.protobuf.generated.dlr.DLR_MessageContainer.Message;
import cz.agents.highway.storage.InitIn;
import cz.agents.highway.storage.RadarData;
import cz.agents.highway.storage.RoadObject;
import cz.agents.highway.storage.plan.PlansOut;
import eu.opends.car.SteeringCar;
import eu.opends.main.Simulator;
import eu.opends.traffic.PhysicalTraffic;
import eu.opends.traffic.TrafficCar;
import org.apache.log4j.Logger;

import javax.vecmath.Point3d;
import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DLRProtobufClient {
    /*
     * ProtobufClient is responsible for communication via ProtocolBuffers When initialized init()
     * it connects to a server and sends Init Message. The update(CarsIn cars) sends Update message,
     * while message handler receives Plan messages.
     */

    // TODO closing, include in settings

    /*private final static Logger logger = Logger.getLogger(DLRProtobufClient.class);

    Communicator<Header, Message> comm;

    float UPDATE_INTERVAL = 0.2f;
    float lastUpdateTime = 0;

    private SocketTransportLayer transport;

    private String uri;
    private Simulator simulator;

    private boolean connected;
    private Map<String, PlanCallback> registeredCBs;
    private Map<Integer, String> mapID2Name;
    private String egoName;

    public DLRProtobufClient(Simulator openDSSimulator, String uri) {
        this.uri = uri;
        simulator = openDSSimulator;
        mapID2Name = new HashMap<Integer, String>();
        egoName = simulator.driverName;
        mapID2Name.put(0, egoName);// driven car

        registeredCBs = new HashMap<String, PlanCallback>();

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
            int id = 1;
            for (TrafficCar car : simulator.getPhysicalTraffic().getVehicleList()) {
                mapID2Name.put(id++, car.getName());
                System.out.println("inserrted into map: " + car.getName() + " with id " + (id - 1)
                        + " at position: " + car.getPosition());

            }

            transport = new SocketTransportLayer();

            boolean receiveInThread = true;
            boolean sendInThread = true;

            comm = new ClientCommunicator<Header, Message>(uri, Header.getDefaultInstance(),
                    Message.getDefaultInstance(), transport, receiveInThread, sendInThread);
            comm.connect();
            comm.registerReceiveCallback(new DLR_PlansFactory(),
                    new ProtobufMessageHandler<PlansOut>() {

                        @Override
                        public void notify(PlansOut plans) {
                            // System.out.println("received Plans: \n");
                            logger.debug("Received plans...");
                            for (int carId : plans.getCarIds()) {
//                                 System.out.println("uploading plan for " + carId);
//                                if (carId == 0)
//                                    continue;
                                String name = getNameFromID(carId);
                                if (registeredCBs.containsKey(name)) {
                                    registeredCBs.get(name).uploadPlan(
                                            plans.getPlan(carId));
                                }

                            }
                            // run = false;
                            logger.debug("END .. received plans.");
                        }
                    });
            comm.registerReceiveCallback(new DLR_UpdateFactory(),
                    new ProtobufMessageHandler<RadarData>() {

                        @Override
                        public void notify(RadarData updates) {
                            logger.debug("Received update...");
                            if (updates != null) {
                                HashMap<String, TrafficCar> ghosts = simulator.getPhysicalTraffic().getGhosts();
                                for (RoadObject vehicle : updates.getCars()) {
                                    String carName = "car" + vehicle.getId();

                                    if (ghosts.containsKey(carName)) {
                                        // TODO: Create method update ghost
                                        TrafficCar ghost = ghosts.get(carName);
                                        Vector3f newPos = Util.highway2OpenDS(vehicle.getPosition());
                                        newPos.setY(ghost.getPosition().y);
                                        ghost.setPosition(newPos);
                                        Quaternion rot = new Quaternion().fromAngleNormalAxis(FastMath.PI, Vector3f.UNIT_Y);

                                        ghost.setRotation(rot);
                                        System.out.println("updating ghost " + carName);
                                    } else {
                                        // System.out.println("not updating ghost "+carName);

                                        //ghosts.add(vehicle.getId(),new UpdateCallback());
                                    }

                                }
                                // run = false;
                                logger.debug("END .. received updates.");
                            }
                        }
                    });
            // comm.registerOutFactory(new InitFactory());
            comm.registerOutFactory(new DLR_UpdateFactory());

            //  sendInit();
            connected = true;
            // Thread receiveThread = new Thread(new Runnable() {
            //
            // @Override
            // public void run() {
            // long lastTime = System.currentTimeMillis();
            // while (true) {
            // logger.debug("Running comm... START d="+(System.currentTimeMillis()-lastTime)+" ms");
            // lastTime = System.currentTimeMillis();
            // comm.run();
            // logger.debug("Running comm... END, t= "+(System.currentTimeMillis()-lastTime)+" ms"
            // );
            // try {
            // Thread.sleep((long)(UPDATE_INTERVAL*1000/3));
            // } catch (InterruptedException e) {
            // e.printStackTrace();
            // }
            // }
            // }
            //
            // });
            // receiveThread.start();
        } catch (ConnectException e) {
            connected = false;
            logger.warn("Protobuf client could not connect to:" + uri);
        } catch (IOException e) {
            e.printStackTrace();
            connected = false;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        comm.close();
        super.finalize();
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
        String egoName = drivenCar.getName();

        RoadObject car0 = new RoadObject(getIDFromName(egoName), timeInSec, simulator.getLaneAgent().getCarLaneInt(
                egoName), Util.openDS2Highway(drivenCar.getPosition()), Util.openDS2Highway(
                drivenCar.getCurrentSpeedMs(), drivenCar.getRotation()));
        carsIn.add(car0);
        PhysicalTraffic traffic = simulator.getPhysicalTraffic();
        int c = 1;
        RoadObject carIn;
        for (TrafficCar car : traffic.getVehicleList()) {

            carIn = new RoadObject(getIDFromName(car.getName()), timeInSec, simulator.getLaneAgent().getCarLaneInt(
                    car.getName()), Util.openDS2Highway(car.getPosition()), Util.openDS2Highway(
                    car.getCurrentSpeedMs(), car.getRotation()));
            carsIn.add(carIn);
        }

        // CarIn carIn = new CarIn(int id, int t,Point2d position, Point2d positionHigh,double
        // speed,int lane);
        logger.debug("END... Generating update...");
        return carsIn;
    }

    public void registerCallback(String name, PlanCallback planCallback) {
        registeredCBs.put(name, planCallback);
        System.out.println("registered callback for " + name);

    }

    private String getNameFromID(int vehicleId) {
//        return mapID2Name.get(vehicleId);
        return "car" + vehicleId;
    }

    private int getIDFromName(String name) {
        return Integer.parseInt(name.substring(3));
    }

    public void close() {
        comm.close();

    }*/


}
