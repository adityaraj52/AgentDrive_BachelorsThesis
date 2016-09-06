package cz.agents.highway.vis;import java.awt.Color;import java.awt.Graphics2D;import java.text.DecimalFormat;import java.util.Map.Entry;import javax.vecmath.Point3f;import cz.agents.alite.vis.Vis;import cz.agents.alite.vis.layer.AbstractLayer;import cz.agents.alite.vis.layer.VisLayer;import cz.agents.highway.storage.HighwayStorage;import cz.agents.highway.storage.RoadDescription;import cz.agents.highway.storage.RoadObject;import cz.agents.highway.storage.plan.Action;public class ProtobufVisLayer extends AbstractLayer {        private static final double CAR_LENGTH = 1;	private static final double CAR_WIDTH = 1;	private static final double LANE_WIDTH = 3.5;	    private final HighwayStorage storage;    ProtobufVisLayer(HighwayStorage storage) {    	this.storage = storage;    }    @Override    public void paint(Graphics2D canvas) {//        paintRoad(canvas);        paintCars(canvas);    }    private void paintCars(Graphics2D canvas) {    	for (Entry<Integer, RoadObject> entry: storage.getPosCurr().entrySet()) {    		int id = entry.getKey();    		RoadObject object = entry.getValue();			Color color = AgentColors.getColorForAgent(id);									canvas.setColor(color);			Point3f pos = object.getPosition();			canvas.fillRect(Vis.transX(pos.x - CAR_LENGTH/2), Vis.transY(pos.y - CAR_WIDTH/2), Vis.transW(CAR_LENGTH), Vis.transH(CAR_WIDTH));						Action action = storage.getActions().get(id);									if(action != null){				canvas.setColor(Color.WHITE);				DecimalFormat df = new DecimalFormat("0.0");				String s = "[ID=" + action.getCarId() + " POS=" + pos + " SPEED=" +                            df.format(object.getVelocity().length()) + " LANE = " +                            object.getLaneIndex() + "->" + action;				canvas.drawString(s, (int) (Vis.transX(pos.x + CAR_LENGTH)), (int) (Vis.transY(pos.y)));			}		}	}		private void paintRoad(Graphics2D canvas) {        canvas.setColor(Color.BLACK);                for(RoadDescription.Line line: storage.getRoadDescription().getLines()) {        	        	int lanes = (int)line.a.z;        	        	for(int l = 0; l < lanes + 1; l++){        		double shift = 2 * LANE_WIDTH - l * LANE_WIDTH + 3;        		canvas.drawLine(Vis.transX(line.a.x + shift), Vis.transY(-line.a.y), Vis.transX(line.b.x+ shift), Vis.transY(-line.b.y));        	}        }    }    public static VisLayer create(HighwayStorage storage) {               ProtobufVisLayer envLayer = new ProtobufVisLayer(storage);               return envLayer;    }}