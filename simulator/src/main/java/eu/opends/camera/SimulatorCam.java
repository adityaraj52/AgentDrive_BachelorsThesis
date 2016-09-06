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

package eu.opends.camera;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.control.CameraControl;

import eu.opends.car.Car;
import eu.opends.main.Simulator;

/**
 * 
 * @author Rafael Math
 */
public class SimulatorCam extends CameraFactory 
{	
	private Car car;
	private Node carNode;
	
	// if false: TOP camera will always show north at the top of the screen
	// if true: car is pointing to the top of the screen (moving map)
	private boolean isCarPointingUp = true;
	
	private Vector3f outsideCamPos = new Vector3f(-558f, 22f, -668f);
	
	
	public SimulatorCam(Simulator sim, Car car) 
	{	    
		this.car = car;
		carNode = car.getCarNode();
		
		initCamera(sim, carNode);		
		setCamMode(CameraMode.EGO);
	}

	
	public void setCamMode(CameraMode mode)
	{
		switch (mode)
		{
			case EGO:
				camMode = CameraMode.EGO;
				sim.getRootNode().detachChild(mainCameraNode);
				carNode.attachChild(mainCameraNode);
				chaseCam.setEnabled(false);
				setCarVisible(true);
				((CameraControl) mainCameraNode.getChild("CamNode1").getControl(0)).setEnabled(true);
				mainCameraNode.setLocalTranslation(car.getEgoCamPos());
				mainCameraNode.setLocalRotation(new Quaternion().fromAngles(0, 0, 0));
				break;
	
			case CHASE:
				camMode = CameraMode.CHASE;
				sim.getRootNode().detachChild(mainCameraNode);
				carNode.attachChild(mainCameraNode);
				chaseCam.setEnabled(true);
				chaseCam.setDragToRotate(false);
				setCarVisible(true);
				((CameraControl) mainCameraNode.getChild("CamNode1").getControl(0)).setEnabled(false);
				break;
	
			case TOP:
				camMode = CameraMode.TOP;
				// camera detached from car node in TOP-mode to make the camera movement more stable
				carNode.detachChild(mainCameraNode);
				sim.getRootNode().attachChild(mainCameraNode);
				chaseCam.setEnabled(false);
				setCarVisible(true);
				((CameraControl) mainCameraNode.getChild("CamNode1").getControl(0)).setEnabled(true);
				break;
				
			case OUTSIDE:
				camMode = CameraMode.OUTSIDE;
				// camera detached from car node in OUTSIDE-mode
				carNode.detachChild(mainCameraNode);
				sim.getRootNode().attachChild(mainCameraNode);
				chaseCam.setEnabled(false);
				setCarVisible(true);
				((CameraControl) mainCameraNode.getChild("CamNode1").getControl(0)).setEnabled(true);
				break;
	
			case STATIC_BACK:
				camMode = CameraMode.STATIC_BACK;
				sim.getRootNode().detachChild(mainCameraNode);
				carNode.attachChild(mainCameraNode);
				chaseCam.setEnabled(false);
				setCarVisible(true);
				((CameraControl) mainCameraNode.getChild("CamNode1").getControl(0)).setEnabled(true);
				mainCameraNode.setLocalTranslation(car.getStaticBackCamPos());
				mainCameraNode.setLocalRotation(new Quaternion().fromAngles(0, 0, 0));
				break;
				
			case OFF:
				camMode = CameraMode.OFF;
				chaseCam.setEnabled(false);
				setCarVisible(false);
				break;
		}
	}
	
	
	public void changeCamera() 
	{
		// STATIC_BACK --> EGO (--> CHASE, only if 1 screen) --> TOP --> OUTSIDE --> STATIC_BACK --> ...
		switch (camMode)
		{
			case STATIC_BACK: setCamMode(CameraMode.EGO); break;
			case EGO: 
					if(sim.getNumberOfScreens() == 1)
						setCamMode(CameraMode.CHASE);
					else
						setCamMode(CameraMode.TOP);
					break;
			case CHASE: setCamMode(CameraMode.TOP); break;
			case TOP: setCamMode(CameraMode.OUTSIDE); break;
			case OUTSIDE: setCamMode(CameraMode.STATIC_BACK); break;
			default: break;
		}
	}
	
	
	public void updateCamera()
	{
		if(camMode == CameraMode.EGO)
		{
			if(mirrorMode == MirrorMode.ALL)
			{
				backViewPort.setEnabled(true);
				leftBackViewPort.setEnabled(true);
				rightBackViewPort.setEnabled(true);
				backMirrorFrame.setCullHint(CullHint.Dynamic);
				leftMirrorFrame.setCullHint(CullHint.Dynamic);
				rightMirrorFrame.setCullHint(CullHint.Dynamic);
			}
			else if(mirrorMode == MirrorMode.BACK_ONLY)
			{
				backViewPort.setEnabled(true);
				leftBackViewPort.setEnabled(false);
				rightBackViewPort.setEnabled(false);
				backMirrorFrame.setCullHint(CullHint.Dynamic);
				leftMirrorFrame.setCullHint(CullHint.Always);
				rightMirrorFrame.setCullHint(CullHint.Always);
			}
			else if(mirrorMode == MirrorMode.SIDE_ONLY)
			{
				backViewPort.setEnabled(false);
				leftBackViewPort.setEnabled(true);
				rightBackViewPort.setEnabled(true);
				backMirrorFrame.setCullHint(CullHint.Always);
				leftMirrorFrame.setCullHint(CullHint.Dynamic);
				rightMirrorFrame.setCullHint(CullHint.Dynamic);
			}
			else
			{
				backViewPort.setEnabled(false);
				leftBackViewPort.setEnabled(false);
				rightBackViewPort.setEnabled(false);
				backMirrorFrame.setCullHint(CullHint.Always);
				leftMirrorFrame.setCullHint(CullHint.Always);
				rightMirrorFrame.setCullHint(CullHint.Always);
			}			
		}
		else
		{
			backViewPort.setEnabled(false);
			leftBackViewPort.setEnabled(false);
			rightBackViewPort.setEnabled(false);
			
			backMirrorFrame.setCullHint(CullHint.Always);
			leftMirrorFrame.setCullHint(CullHint.Always);
			rightMirrorFrame.setCullHint(CullHint.Always);
		}
		
		if(camMode == CameraMode.TOP)
		{
			// camera detached from car node --> update position and rotation separately
			Vector3f targetPosition = carNode.localToWorld(new Vector3f(0, 0, 0), null);
			Vector3f camPos = new Vector3f(targetPosition.x, targetPosition.y + 30, targetPosition.z);
			mainCameraNode.setLocalTranslation(camPos);
			
			float upDirection = 0;
			if(isCarPointingUp)
			{
				float[] angles = new float[3];
				carNode.getLocalRotation().toAngles(angles);
				upDirection = angles[1];
			}
			mainCameraNode.setLocalRotation(new Quaternion().fromAngles(-FastMath.HALF_PI, upDirection, 0));
		}
		
		if(camMode == CameraMode.OUTSIDE)
		{
			// camera detached from car node --> update position and rotation separately
			mainCameraNode.setLocalTranslation(outsideCamPos);
				

			Vector3f carPos = carNode.getWorldTranslation();
	        Vector3f direction = carPos.subtractLocal(outsideCamPos).normalizeLocal().negateLocal();
	        Vector3f up = new Vector3f(0, 1, 0);
	        Vector3f left = new Vector3f(up.crossLocal(direction).normalizeLocal());
	        if (left.equals(Vector3f.ZERO)) {
	            if (direction.x != 0) {
	                left.set(direction.y, -direction.x, 0f);
	            } else {
	                left.set(0f, direction.z, -direction.y);
	            }
	        }
	        up.set(direction).crossLocal(left).normalizeLocal();
			mainCameraNode.setLocalRotation(new Quaternion().fromAxes(left, up, direction));
		}
	}
	
	
	public void setCarVisible(boolean setVisible) 
	{
		if(setVisible)
		{
			if (carNode.getCullHint() == CullHint.Always)
				carNode.setCullHint(CullHint.Dynamic);
		}
		else
		{
			if (carNode.getCullHint() != CullHint.Always)
				carNode.setCullHint(CullHint.Always);
		}
	}
}