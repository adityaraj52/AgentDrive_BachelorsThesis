/*
*  This file is part of OpenDS (Open Source Driving Simulator).
*  Copyright (C) 2013 Rafael Math
*
*  OpenDS is free software: you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation, either version 3 of the License, or
*  (at your option) any later version.
*
*  OpenDS is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU General Public License for more details.
*
*  You should have received a copy of the GNU General Public License
*  along with OpenDS. If not, see <http://www.gnu.org/licenses/>.
*/

package eu.opends.taskDescription;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.lang.Math;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.Spatial.CullHint;

import eu.opends.car.SteeringCar;
import eu.opends.main.Simulator;

/**
 * 
 * @author Daniel Braun, Rafael Math
 */
public class LaneKeepingTask 
{
	private Simulator sim;
	private boolean enabled = true;	
	private Random randomizer = new Random();
	private List<Material> materialsList = new ArrayList<Material>();
	
	//road parameters
	private int buildDepth = 300;	
	private int straightRoadElements = 17; //num of straight road elements
	private int roadElementLength = 20;
	private float offsetChange = 1.69f;	//offset between straight an chane road element
	
	//road vars
	private int roadEnd = straightRoadElements * roadElementLength;
	private float roadCenter = 0.0f;
	private LinkedList<Spatial> road;
	
	//display parameters
	private int displayDuration = 1000; //ms
	private int textDisplayDuration = 2000; //ms
	
	//display vars
	private long lastLaneChange = 0;
	private long displayStart = 0;	
	private boolean displayOn = false;
	private boolean waitingForReaction = false;
	private String[] texts = {"eins rechts","eins links","zwei rechts", "zwei links"};
	
	private BitmapText cross;
	private BitmapText line;
	private BitmapText changeText;
	
	//board parameters
	private int maxBoards = 0; //in 300m, should not be > 5
	private int minBoards = 0; //in 300m
	
	//board vars
	private LinkedList<Spatial> boards;
	private LinkedList<Spatial> leftCylinders;
	private LinkedList<Spatial> rightCylinders;
	private int boardsVisible;
	private float lastBoardPosition = 0;
	RigidBodyControl boardControl;
	private float cylinderLeftOffset = 17.0f;
	private float cylinderRightOffset = 14.0f;
	private float boardOffset = 15.5f;
	
	//reaction parameters
	private float significantSteering = 0.1f;
	
	public LaneKeepingTask(Simulator sim) 
	{
		this.sim = sim;
		
		//init timer
		lastLaneChange = System.currentTimeMillis();
		
		//road elements init
		road = new LinkedList<Spatial>();
		
		road.add(sim.getRootNode().getChild("RL"));
		road.add(sim.getRootNode().getChild("RR"));
		
		for (int i = 1; i <= straightRoadElements; i++) {
			road.add(sim.getRootNode().getChild("RS"+String.valueOf(i)));
		}
		
		//test
		for (int i = 0; i < road.size(); i++) {
			if(road.get(i) == null){
				enabled = false;
				break;
			}
		}
		
		//boards init
		boards = new LinkedList<Spatial>();
		leftCylinders = new LinkedList<Spatial>();;
		rightCylinders = new LinkedList<Spatial>();
		
		for (int i = 1; i <= 5; i++) {
			boards.add(i-1, sim.getRootNode().getChild("board"+String.valueOf(i)));
			leftCylinders.add(i-1, sim.getRootNode().getChild("cylinderLeft"+String.valueOf(i)));
			rightCylinders.add(i-1, sim.getRootNode().getChild("cylinderRight"+String.valueOf(i)));
		}
		
		for (int i = 0; i < boards.size(); i++) {
			if(boards.get(i) == null){ //|| leftCylinders.get(i) == null || rightCylinders.get(i) == null){
				//System.out.println("ERROR");
				enabled = false;
				break;
			}
			else{
				boards.get(i).setCullHint(CullHint.Always);
				leftCylinders.get(i).setCullHint(CullHint.Always);
				rightCylinders.get(i).setCullHint(CullHint.Always);
			}
				
		}	
		
		boardsVisible = 0;
		
		if(enabled){
			//display init
			Node guiNode = sim.getGuiNode();
			BitmapFont guiFont = sim.getAssetManager().loadFont("Interface/Fonts/Default.fnt");
			cross = new BitmapText(guiFont, false);
			cross.setSize(guiFont.getCharSet().getRenderedSize()+20);
			cross.setLocalTranslation(sim.getSettings().getWidth()/2, sim.getSettings().getHeight()/2, 0);
			cross.setText("+");
			cross.setColor(ColorRGBA.Red);
			cross.setCullHint(CullHint.Always);
			guiNode.attachChild(cross);
			
			line = new BitmapText(guiFont, false);
			line.setSize(guiFont.getCharSet().getRenderedSize());
			line.setLocalTranslation(sim.getSettings().getWidth()/2 - 20, sim.getSettings().getHeight()/2 - 10, 0);
			line.setText("_________");
			line.setColor(ColorRGBA.Red);
			line.setCullHint(CullHint.Always);
			guiNode.attachChild(line);
			
			changeText = new BitmapText(guiFont, false);
			changeText.setSize(guiFont.getCharSet().getRenderedSize() + 8);		
			changeText.setText("");
			changeText.setColor(ColorRGBA.Green);
			changeText.setCullHint(CullHint.Always);
			guiNode.attachChild(changeText);
			
			// create textures list
			List<String> texturesList = new LinkedList<String>(); //TODO insert textrues
			texturesList.add("Textures/Billboards/alanturing.png");	
			texturesList.add("Textures/Billboards/armstrong.png");	
			texturesList.add("Textures/Billboards/data.png");	
			texturesList.add("Textures/Billboards/dfki.png");	
			texturesList.add("Textures/Billboards/jenniferconnelly.png");	
			texturesList.add("Textures/Billboards/juliana.png");	
			texturesList.add("Textures/Billboards/julianassange.png");	
			texturesList.add("Textures/Billboards/opends.png");	
			texturesList.add("Textures/Billboards/periodictable.png");			
			texturesList.add("Textures/Billboards/rocketscience.png");	
			texturesList.add("Textures/Billboards/seven.png");	
			texturesList.add("Textures/Billboards/sheldoncooper.png");	
			texturesList.add("Textures/Billboards/spock.png");	
			texturesList.add("Textures/Billboards/startrekcaptains.png");	
			texturesList.add("Textures/Billboards/swi.png");	
			texturesList.add("Textures/Billboards/tesla.png");	
			texturesList.add("Textures/Billboards/theremin.png");	
			texturesList.add("Textures/Billboards/uds.png");			
			texturesList.add("Textures/Billboards/whitec-ascorbin.png");	
			texturesList.add("Textures/Billboards/zwergflusspferde-paul-und-debby.png");	
			
			
			// load all available textures from HDD and set up materials for landmarks
			for(String texturePath : texturesList)
			{
				Material material = createMaterial(texturePath);
				if(material != null)
					materialsList.add(material);
			}
		}
		
		lastTaskChange = System.currentTimeMillis();
	}
	
	private boolean shouldShowLaneChange(){		
		int next = (randomizer.nextInt(5)+10)*1000;
		
		if(System.currentTimeMillis() - lastLaneChange >= next)
			return true;
		
		return false;		
	}
	
	private String getChangeText(){				
		return texts[(int) (Math.random() * 4)];
	}
	
	private void setTextPosition(){
		int r1 = 20;
		int r2 = -15;
		
		
		if (Math.random() >= 0.5)
			r1 = -108;
		if (Math.random() >= 0.5)
			r2 = 2;
		
		
		changeText.setLocalTranslation(sim.getSettings().getWidth()/2 + r1, sim.getSettings().getHeight()/2 + r2, 0);
	}
	
	private void showLaneChange(){		
		if(changeText.getCullHint() == CullHint.Dynamic){ //done
			if((System.currentTimeMillis() - displayStart) > textDisplayDuration){
				//SteeringCar car = sim.getCar();
				//Vector3f carPos = car.getPosition();
				lastLaneChange = System.currentTimeMillis();				
				changeText.setCullHint(CullHint.Always);
				cross.setCullHint(CullHint.Always);
				displayOn = false;
			}
		}
		else if(line.getCullHint() == CullHint.Dynamic){ //show changeText now
			if((System.currentTimeMillis() - displayStart) > displayDuration){
				line.setCullHint(CullHint.Always);
				cross.setCullHint(CullHint.Dynamic);
				changeText.setText(getChangeText());
				setTextPosition();
				changeText.setCullHint(CullHint.Dynamic);
				waitingForReaction = true;
				displayStart = System.currentTimeMillis();
			}
		}
		else if(cross.getCullHint()  == CullHint.Dynamic){ //show line now
			//System.out.println();
			if((System.currentTimeMillis() - displayStart) > displayDuration){
				cross.setCullHint(CullHint.Always);
				line.setCullHint(CullHint.Dynamic);
				displayStart = System.currentTimeMillis();
			}
		}
		else{
			displayOn = true;
			cross.setCullHint(CullHint.Dynamic);
			displayStart = System.currentTimeMillis();
			if(waitingForReaction){
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(displayStart);
				Simulator.getDrivingTaskLogger().reportReactionTime("Keine Reaktion"+changeText.getText(), cal);
				waitingForReaction = false;
			}
			
		}
	}

	private void applyRandomTexture(Spatial billboardSpatial)
	{
		// select random material from material list
		int selectedMaterialIndex = randomizer.nextInt(materialsList.size());
		Material material = materialsList.get(selectedMaterialIndex);
		billboardSpatial.setMaterial(material);
	}
	
	private Material createMaterial(String texturePath)
	{
		Material material = null;

	    try {
	    	
			material = new Material(sim.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
			material.setTexture("ColorMap", sim.getAssetManager().loadTexture(texturePath));
			material.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
			
	    } catch (Exception ex) {
	        ex.printStackTrace();
	    }
	    
	    return material;
	}


	private int taskChangeIntervalMsec = 120000; // every 2 minutes
	private long lastTaskChange = 0;
	private int mode = 1;
	public void update(float tpf) 
	{
		if(enabled)
		{
			if (System.currentTimeMillis() - lastTaskChange > taskChangeIntervalMsec) 
			{
				lastTaskChange = System.currentTimeMillis();
				if(mode == 1)
				{
					maxBoards = 2;
					minBoards = 1;
					mode = 2;		
					System.out.println("M2");
				}
				else if(mode == 2)
				{
					maxBoards = 5;
					minBoards = 4;
					mode = 3;		
					System.out.println("M3");
				}
				else if(mode == 3)
				{
					// inc speed
					sim.getCar().setMinSpeed(100);
					sim.getCar().setMaxSpeed(120);
					maxBoards = 0;
					minBoards = 0;
					mode = 4;		
					System.out.println("M4");
				}
				else if(mode == 4)
				{
					maxBoards = 2;
					minBoards = 1;
					mode = 5;		
					System.out.println("M5");
				}
				else if(mode == 5)
				{
					maxBoards = 5;
					minBoards = 4;
					mode = 6;		
					System.out.println("M6");
				}
				else if(mode == 6)
				{
					sim.stop();
				}
			}
			
			
			SteeringCar car = sim.getCar();
			Vector3f carPos = car.getPosition();
			
			if((roadEnd - carPos.getZ()) < buildDepth){
				boolean addStraight = true;
				
				Spatial roadElement = road.get(0);
				Spatial roadElement2 = road.get(1);
				RigidBodyControl roadElementControl = (RigidBodyControl) roadElement.getControl(0);
				RigidBodyControl roadElementControl2 = (RigidBodyControl) roadElement2.getControl(0);
				
				//add road right
				if((carPos.getX() - roadCenter) < -2.2){
					if(roadElement.getName().equals("RR") && roadElement2.getName().equals("RL")){	
						Vector3f roadPos = roadElementControl.getPhysicsLocation();
						Vector3f newRoadPos = new Vector3f(roadCenter - offsetChange, roadPos.getY(),  roadEnd);
						//System.out.println(roadCenter);
						roadElementControl.setPhysicsLocation(newRoadPos);							
						
						roadCenter -= 3.69;
						addStraight = false;
						
						road.remove(0);
						road.addLast(roadElement);
					}
					else if(roadElement2.getName().equals("RR") && roadElement.getName().equals("RL")){	
						Vector3f roadPos = roadElementControl.getPhysicsLocation();
						Vector3f newRoadPos = new Vector3f(roadCenter - offsetChange, roadPos.getY(), roadEnd);
						roadElementControl2.setPhysicsLocation(newRoadPos);							
						
						roadCenter -= 3.69;
						addStraight = false;	
						
						road.remove(1);
						road.addLast(roadElement2);
					}
				}
				
				//add road left
				
				else if((carPos.getX() - roadCenter) > 2.2){
					if(roadElement.getName().equals("RL") && roadElement2.getName().equals("RR")){	
						Vector3f roadPos = roadElementControl.getPhysicsLocation();
						Vector3f newRoadPos = new Vector3f(roadCenter + offsetChange, roadPos.getY(),  roadEnd);
						//System.out.println(roadCenter);
						roadElementControl.setPhysicsLocation(newRoadPos);							
						
						roadCenter += 3.69;
						addStraight = false;
						
						road.remove(0);
						road.addLast(roadElement);
					}
					else if(roadElement2.getName().equals("RL") && roadElement.getName().equals("RR")){	
						Vector3f roadPos = roadElementControl.getPhysicsLocation();
						Vector3f newRoadPos = new Vector3f(roadCenter + offsetChange, roadPos.getY(), roadEnd);
						roadElementControl2.setPhysicsLocation(newRoadPos);							
						
						roadCenter += 3.69;
						addStraight = false;	
						
						road.remove(1);
						road.addLast(roadElement2);					
					}					
				}
				
				//add road straight
				if(addStraight){
					
					for (int i = 0; i < road.size(); i++) {						
						if(!road.get(i).getName().equals("RR") && !road.get(i).getName().equals("RL")){
							roadElementControl = (RigidBodyControl) road.get(i).getControl(0);
							roadElement = road.get(i);
							
							road.remove(i);
							
							break;
						}
					}
					
					
					
					Vector3f roadPos = roadElementControl.getPhysicsLocation();
					Vector3f newRoadPos = new Vector3f(roadCenter, roadPos.getY(),  roadEnd);
					roadElementControl.setPhysicsLocation(newRoadPos);		
					
					
					road.addLast(roadElement);
				}
				
				roadEnd += roadElementLength;				
				
			}
			
			//log reaction
			if(waitingForReaction &&  (Math.abs(car.getSteeringWheelState()) > significantSteering) ){			
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(displayStart);
				Simulator.getDrivingTaskLogger().reportReactionTime("Anweisung:"+changeText.getText()+";Geschw:"+String.valueOf(car.getCurrentSpeedKmh())+";Max. Schilder:"+String.valueOf(maxBoards), cal);
				waitingForReaction = false;
			}
			
			//display
			if(displayOn || shouldShowLaneChange())
				showLaneChange();	
			
			//boards
			
			for (int i = 0; i < boards.size(); i++) {
				boardControl = (RigidBodyControl) boards.get(i).getControl(0);
				
				if((boards.get(i).getCullHint() == CullHint.Dynamic) && (boardControl.getPhysicsLocation().getZ() < carPos.getZ())){
					
					boards.get(i).setCullHint(CullHint.Always);
					leftCylinders.get(i).setCullHint(CullHint.Always);
					rightCylinders.get(i).setCullHint(CullHint.Always);
					boardsVisible--;
				}				
			}
			
			if(boardsVisible < maxBoards)
			do{
				for (int i = 0; i < maxBoards; i++) {
					int n = randomizer.nextInt(3);
					
					boardControl = (RigidBodyControl) boards.get(i).getControl(0);
					
					if((boards.get(i).getCullHint() == CullHint.Always) || (boardControl.getPhysicsLocation().getZ() < carPos.getZ())){
						if(n == 0){
							//stay invisible
						}
						else{
							//place new
							if((boards.get(i).getCullHint() == CullHint.Always))
								boardsVisible++;
							
							applyRandomTexture(boards.get(i));
							
							int direction;
							
							if(n == 1){//left
								direction = 1;
							}
							else{//right
								direction = -1;
							}
							
							
							
							
							float shift = randomizer.nextInt((300 / maxBoards)-20)+20;
							float newPosition = Math.max(lastBoardPosition, carPos.getZ()+300) + shift;
							
							lastBoardPosition = newPosition;
							
							//move board
							Vector3f oldPosition = boardControl.getPhysicsLocation();
							oldPosition.setZ(newPosition);
							oldPosition.setX(roadCenter + boardOffset * direction);
							boardControl.setPhysicsLocation(oldPosition);
							boards.get(i).setCullHint(CullHint.Dynamic);
							
							//move cylinders
							RigidBodyControl leftCylinderControl = (RigidBodyControl) leftCylinders.get(i).getControl(0);
							RigidBodyControl rightCylinderControl = (RigidBodyControl) rightCylinders.get(i).getControl(0);
							oldPosition = leftCylinderControl.getPhysicsLocation();
							oldPosition.setZ(newPosition);
							oldPosition.setX(roadCenter + cylinderLeftOffset * direction);
							leftCylinderControl.setPhysicsLocation(oldPosition);
							leftCylinders.get(i).setCullHint(CullHint.Dynamic);
							oldPosition = rightCylinderControl.getPhysicsLocation();
							oldPosition.setZ(newPosition);
							oldPosition.setX(roadCenter + cylinderRightOffset * direction);
							rightCylinderControl.setPhysicsLocation(oldPosition);
							rightCylinders.get(i).setCullHint(CullHint.Dynamic);
						}
					}	
					
															
				}				
			}while(boardsVisible < minBoards);
		}
	}

}
