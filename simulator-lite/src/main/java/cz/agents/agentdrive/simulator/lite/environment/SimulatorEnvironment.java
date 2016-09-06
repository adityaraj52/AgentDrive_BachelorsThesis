package cz.agents.agentdrive.simulator.lite.environment;

import cz.agents.agentdrive.simulator.lite.storage.SimulatorEvent;
import cz.agents.agentdrive.simulator.lite.storage.VehicleStorage;
import cz.agents.agentdrive.simulator.lite.storage.vehicle.Car;
import cz.agents.agentdrive.simulator.lite.storage.vehicle.Ghost;
import cz.agents.agentdrive.simulator.lite.storage.vehicle.Vehicle;
import cz.agents.alite.common.event.Event;
import cz.agents.alite.common.event.EventHandler;
import cz.agents.alite.common.event.EventProcessor;
import cz.agents.alite.configurator.Configurator;
import cz.agents.alite.environment.eventbased.EventBasedEnvironment;
import cz.agents.alite.protobuf.communicator.ClientCommunicator;
import cz.agents.alite.protobuf.communicator.Communicator;
import cz.agents.alite.protobuf.communicator.callback.ConnectCallback;
import cz.agents.alite.protobuf.factory.FactoryInterface;
import cz.agents.alite.protobuf.factory.ProtobufFactory;
import cz.agents.alite.simulation.SimulationEventType;
import cz.agents.alite.transport.SocketTransportLayer;
import cz.agents.highway.protobuf.factory.dlr.DLR_PlansFactory;
import cz.agents.highway.protobuf.factory.dlr.DLR_UpdateFactory;
import cz.agents.highway.protobuf.factory.simplan.PlansFactory;
import cz.agents.highway.protobuf.factory.simplan.UpdateFactory;
import cz.agents.highway.protobuf.generated.dlr.DLR_MessageContainer;
import cz.agents.highway.protobuf.generated.simplan.MessageContainer;
import cz.agents.highway.storage.RadarData;
import cz.agents.highway.storage.RoadObject;
import cz.agents.highway.storage.plan.PlansOut;
import cz.agents.highway.storage.plan.WPAction;
import org.apache.log4j.Logger;

import javax.vecmath.Vector3f;
import java.io.IOException;

/**
 * Class representing the environment of the simulation
 * <p/>
 * Created by wmatex on 3.7.14.
 */
public class SimulatorEnvironment extends EventBasedEnvironment {
    private static final Logger logger = Logger.getLogger(SimulatorEnvironment.class);
    /// All simulated vehicles
    private VehicleStorage vehicleStorage;
    private Communicator communicator;

    /// Time between updates
    static public final long UPDATE_STEP = 20;

    /// Time between communication updates
    static public final long COMM_STEP = 200;

    /**
     * Create the environment, the storage and register event handlers
     */
    public SimulatorEnvironment(final EventProcessor eventProcessor) {
        super(eventProcessor);
        vehicleStorage = new VehicleStorage(this);

        getEventProcessor().addEventHandler(new EventHandler() {
            @Override
            public EventProcessor getEventProcessor() {
                return eventProcessor;
            }

            @Override
            public void handleEvent(Event event) {
                // Start updating vehicles when the simulation starts
                if (event.isType(SimulationEventType.SIMULATION_STARTED)) {
                    logger.info("SIMULATION STARTED received");
                    // Connect to server
                    try {
                        logger.info("Connecting to server ...");
                        initCommunication();
                        logger.info("Connected");
                        getEventProcessor().addEvent(SimulatorEvent.UPDATE, null, null, null);
                        getEventProcessor().addEvent(SimulatorEvent.COMMUNICATION_UPDATE, null, null, null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (event.isType(SimulatorEvent.COMMUNICATION_UPDATE)) {
                    try {
                        communicator.send(vehicleStorage.generateRadarData());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    getEventProcessor().addEvent(SimulatorEvent.COMMUNICATION_UPDATE, null, null, null, COMM_STEP);
                }
            }
        });


    }

    private void initCommunication() throws IOException {
        String serverUri = Configurator.getParamString("simulator.lite.protobuf.serverUri", "socket://localhost:2222");
        boolean sendThread = true;
        boolean receiveThread = true;
        String protocol = Configurator.getParamString("simulator.lite.protobuf.protocol", "DLR");

        communicator = createCommunicator(protocol, serverUri, receiveThread, sendThread);

        final FactoryInterface updateFactory = createUpdateFactory(protocol);
        final FactoryInterface plansFactory = createPlanFactory(protocol);

        // register connect callback to register receive callbacks before the connection begin to receive data !!!
        communicator.registerConnectCallback(new ConnectCallback() {
            @Override
            public void invoke(ProtobufFactory protobufFactory) {
                try {
                    communicator.registerOutFactory(updateFactory);

                    communicator.registerReceiveCallback(plansFactory, new ProtobufFactory.ProtobufMessageHandler<PlansOut>() {
                        @Override
                        public void notify(PlansOut plans) {
                            logger.info("Received plans: " + plans.getCarIds());
                            for (int carID : plans.getCarIds()) {
                                logger.info("Plan for car " + carID + ": " + plans.getPlan(carID));
                                // This is the init plan
                                Vehicle vehicle = vehicleStorage.getVehicle(carID);
                                if (vehicle == null) {
                                    // Get the first action containing car info
                                    WPAction action = (WPAction) plans.getPlan(carID).iterator().next();
                                    Vector3f heading = new Vector3f(0, -1, 0);
                                    heading.normalize();
                                    vehicleStorage.addVehicle(new Car(carID, 0, action.getPosition(), heading, (float) action.getSpeed()));
                                } else {
                                    vehicle.getVelocityController().updatePlan(plans.getPlan(carID));
                                    vehicle.setWayPoints(plans.getPlan(carID));
                                }
                            }
                        }
                    });
                    communicator.registerReceiveCallback(updateFactory, new ProtobufFactory.ProtobufMessageHandler<RadarData>() {
                        @Override
                        public void notify(RadarData update) {
                            logger.info("Received update: " + update);
                            for (RoadObject roadObject : update.getCars()) {
                                Vehicle ghost = vehicleStorage.getGhost(roadObject.getId());
                                if (ghost == null) {
                                    Ghost newGhost = new Ghost(roadObject.getId(), roadObject.getPosition());
                                    vehicleStorage.addGhost(newGhost);
                                } else {
                                    // TODO: do a smooth transition from one position to another
                                    ghost.setPosition(roadObject.getPosition());
                                    ghost.setVelocityVector(roadObject.getVelocity());
                                }
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });

        communicator.connect();

    }


    /**
     * Initialize the environment and add some vehicles
     */
    public void init() {
        getEventProcessor().addEventHandler(vehicleStorage);
    }

    private FactoryInterface createPlanFactory(String protocol) {
        FactoryInterface plansFactory = null;
        if (protocol.equals("DLR")) {

            plansFactory = new DLR_PlansFactory();
        } else if (protocol.equals("simplan")) {

            plansFactory = new PlansFactory();
        }
        return plansFactory;
    }

    private FactoryInterface createUpdateFactory(String protocol) {
        FactoryInterface updateFactory = null;
        if (protocol.equals("DLR")) {
            updateFactory = new DLR_UpdateFactory();
        } else if (protocol.equals("simplan")) {
            updateFactory = new UpdateFactory();
        }
        return updateFactory;
    }

    private Communicator createCommunicator(String protocol, String serverUri, boolean receiveThread, boolean sendThread) {
        // Create different communicator based on the protocol type
        if (protocol.equals("DLR")) {
            communicator = new ClientCommunicator<DLR_MessageContainer.Header, DLR_MessageContainer.Message>(
                    serverUri, DLR_MessageContainer.Header.getDefaultInstance(), DLR_MessageContainer.Message.getDefaultInstance(),
                    new SocketTransportLayer(), sendThread, receiveThread
            );

        } else if (protocol.equals("simplan")) {
            communicator = new ClientCommunicator<MessageContainer.Header, MessageContainer.Message>(
                    serverUri, MessageContainer.Header.getDefaultInstance(), MessageContainer.Message.getDefaultInstance(),
                    new SocketTransportLayer(), sendThread, receiveThread
            );
        }
        return communicator;
    }

    public VehicleStorage getStorage() {
        return vehicleStorage;
    }

    public Communicator getCommunicator() {
        return communicator;
    }
}
