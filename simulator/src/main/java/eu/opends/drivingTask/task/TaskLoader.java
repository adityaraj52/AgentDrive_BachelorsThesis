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

package eu.opends.drivingTask.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jme3.math.Vector3f;

import eu.opends.drivingTask.DrivingTask;
import eu.opends.drivingTask.DrivingTaskDataQuery;
import eu.opends.drivingTask.DrivingTaskDataQuery.Layer;
import eu.opends.drivingTask.scene.SceneLoader;
import eu.opends.taskDescription.TVPTaskSettings;


/**
 * 
 * @author Rafael Math
 */
public class TaskLoader 
{
	private DrivingTaskDataQuery dtData;
	private SceneLoader sceneLoader;
	private TVPTaskSettings tvpTaskSettings;
	

	public TaskLoader(DrivingTaskDataQuery dtData, DrivingTask drivingTask) 
	{
		this.dtData = dtData;
		this.sceneLoader = drivingTask.getSceneLoader();
		extractContreTaskSettings();
		extractTVPTaskSettings();
	}
	
	
	private Vector3f getPointRef(String pointRef)
	{
		Map<String, Vector3f> pointMap = sceneLoader.getPointMap();
		
		if((pointRef != null) && (pointMap.containsKey(pointRef)))
			return pointMap.get(pointRef);
		else 
			return null;
	}
	
	
	private void extractContreTaskSettings()
	{		
		try {

			String steeringTaskPath = "/task:scenario/task:driver/task:steeringTask";
			
			String startPointLoggingId = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:startPoint/@ref", String.class);
			Vector3f startPointLogging = getPointRef(startPointLoggingId);
			
			String endPointLoggingId = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:endPoint/@ref", String.class);
			Vector3f endPointLogging = getPointRef(endPointLoggingId);
			
			String steeringTaskType = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:steeringTaskType", String.class);
			
			Float distanceToObjects = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:distanceToObjects", Float.class);
			
			Float objectOffset = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:objectOffset", Float.class);
			
			Float heightOffset = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:heightOffset", Float.class);
			
			String targetObjectId = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:targetObject/@id", String.class);
			
			Float targetObjectSpeed = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:targetObject/@speed", Float.class);
			
			Float targetObjectMaxLeft = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:targetObject/@maxLeft", Float.class);
			
			Float targetObjectMaxRight = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:targetObject/@maxRight", Float.class);
			
			String steeringObjectId = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:steeringObject/@id", String.class);
			
			Float steeringObjectSpeed = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:steeringObject/@speed", Float.class);
			
			Float steeringObjectMaxLeft = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:steeringObject/@maxLeft", Float.class);
			
			Float steeringObjectMaxRight = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:steeringObject/@maxRight", Float.class);
			
			String trafficLightObjectId = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:trafficLightObject/@id", String.class);
			
			Integer pauseAfterTargetSet = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:pauseAfterTargetSet", Integer.class);
			
			Integer blinkingInterval = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:blinkingInterval", Integer.class);
			
			
			String databaseUrl = "";
			String databaseUser = "";
			String databasePassword = "";
			String databaseTable = "";
			Boolean writeToDB = false;
			
			// check whether DB node exists
			Node databaseNode = (Node) dtData.xPathQuery(Layer.TASK, 
					steeringTaskPath + "/task:database", XPathConstants.NODE);

			if(databaseNode != null)
			{
				databaseUrl = dtData.getValue(Layer.TASK, 
						steeringTaskPath + "/task:database/@url", String.class);
				
				databaseUser = dtData.getValue(Layer.TASK, 
						steeringTaskPath + "/task:database/@user", String.class);
				
				databasePassword = dtData.getValue(Layer.TASK, 
						steeringTaskPath + "/task:database/@password", String.class);
				
				databaseTable = dtData.getValue(Layer.TASK, 
						steeringTaskPath + "/task:database/@table", String.class);
				
				writeToDB = true;
			}
			
			String conditionName = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:conditionName", String.class);
			
			Long conditionNumber = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:conditionNumber", Long.class);
			
			String ptStartPointId = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:primaryTask/task:startPoint/@ref", String.class);
			Vector3f ptStartPoint = getPointRef(ptStartPointId);
			
			String ptEndPointId = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:primaryTask/task:endPoint/@ref", String.class);
			Vector3f ptEndPoint = getPointRef(ptEndPointId);

			Boolean isPeripheralMode = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:primaryTask/task:isPeripheralMode", Boolean.class);
			
			Integer ptIconWidth = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:primaryTask/task:iconWidth", Integer.class);
			
			Integer ptIconHeight = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:primaryTask/task:iconHeight", Integer.class);
			
			Integer ptIconDistFromLeftFrameBorder = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:primaryTask/task:iconDistFromLeftFrameBorder", Integer.class);
			
			Integer ptIconDistFromRightFrameBorder = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:primaryTask/task:iconDistFromRightFrameBorder", Integer.class);
			
			Integer ptLightMinPause = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:primaryTask/task:lightMinPause", Integer.class);
			
			Integer ptLightMaxPause = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:primaryTask/task:lightMaxPause", Integer.class);

			Integer ptLightDuration = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:primaryTask/task:lightDuration", Integer.class);
			
			Float ptBlinkingThreshold = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:primaryTask/task:blinkingThreshold", Float.class);
			
			Integer ptMinimumBlinkingDuration = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:primaryTask/task:minBlinkingDuration", Integer.class);
			
			String stStartPointId = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:secondaryTask/task:startPoint/@ref", String.class);
			Vector3f stStartPoint = getPointRef(stStartPointId);
			
			String stEndPointId = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:secondaryTask/task:endPoint/@ref", String.class);
			Vector3f stEndPoint = getPointRef(stEndPointId);
			
			Integer stWaitForNextLandmark = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:secondaryTask/task:waitForNextLandmark", Integer.class);
			
			Integer stMinTimeOfAppearance = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:secondaryTask/task:minTimeOfAppearance", Integer.class);
			
			Float stMaxVisibilityDistance = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:secondaryTask/task:maxVisibilityDistance", Float.class);
			
			Float stMaxSelectionDistance = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:secondaryTask/task:maxSelectionDistance", Float.class);
			
			Float stMaxAngle = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:secondaryTask/task:maxAngle", Float.class);
			
			
			List<String> stLandmarkObjectsList = new ArrayList<String>();
			NodeList landmarkObjectNodes = (NodeList) dtData.xPathQuery(Layer.TASK, 
					steeringTaskPath + "/task:secondaryTask/task:landmarkObjects/task:landmarkObject", XPathConstants.NODESET);

			for (int k = 1; k <= landmarkObjectNodes.getLength(); k++) 
			{
				String landmarkObjectId = dtData.getValue(Layer.TASK, 
						steeringTaskPath + "/task:secondaryTask/task:landmarkObjects/task:landmarkObject["+k+"]/@id", String.class);
				
				if(landmarkObjectId != null)
					stLandmarkObjectsList.add(landmarkObjectId);
				else 
					throw new Exception("Error in landmark objects list");
			}
			
			
			List<String> stLandmarkTexturesList = new ArrayList<String>();
			NodeList landmarkTextureNodes = (NodeList) dtData.xPathQuery(Layer.TASK, 
					steeringTaskPath + "/task:secondaryTask/task:landmarkTextures/task:landmarkTexture", XPathConstants.NODESET);

			for (int k = 1; k <= landmarkTextureNodes.getLength(); k++) 
			{
				String landmarkTexturesUrl = dtData.getValue(Layer.TASK, 
						steeringTaskPath + "/task:secondaryTask/task:landmarkTextures/task:landmarkTexture["+k+"]/@url", String.class);
				
				if(landmarkTexturesUrl != null)
					stLandmarkTexturesList.add(landmarkTexturesUrl);
				else 
					throw new Exception("Error in landmark textures list");
			}
			
			
			List<String> stDistractorTexturesList = new ArrayList<String>();
			NodeList distractorTextureNodes = (NodeList) dtData.xPathQuery(Layer.TASK, 
					steeringTaskPath + "/task:secondaryTask/task:distractorTextures/task:distractorTexture", XPathConstants.NODESET);

			for (int k = 1; k <= distractorTextureNodes.getLength(); k++) 
			{
				String distractorTexturesUrl = dtData.getValue(Layer.TASK, 
						steeringTaskPath + "/task:secondaryTask/task:distractorTextures/task:distractorTexture["+k+"]/@url", String.class);
				
				if(distractorTexturesUrl != null)
					stDistractorTexturesList.add(distractorTexturesUrl);
				else 
					throw new Exception("Error in distractor textures list");
			}
			

			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	

	
	private void extractTVPTaskSettings()
	{		
		try {
			String steeringTaskPath = "/task:task/task:threeVehiclePlatoon";
			
			String leadingCarName = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:leadingCar/@id", String.class);
			
			Float minDistanceToLeadingCar = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:leadingCar/@minDistance", Float.class);
			
			Float maxDistanceToLeadingCar = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:leadingCar/@maxDistance", Float.class);
			
			String followerCarName = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:followerCar/@id", String.class);
			
			Float minDistanceToFollowerCar = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:followerCar/@minDistance", Float.class);
			
			Float maxDistanceToFollowerCar = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:followerCar/@maxDistance", Float.class);
			
			Float laneOffsetX = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:laneOffset/@x", Float.class);
			
			Integer brakeLightMinDuration = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:brakeLight/@minDuration", Integer.class);
			
			Integer turnSignalDuration = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:turnSignal/@duration", Integer.class);
			
			Integer maxReactionTime = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:maxReactionTime", Integer.class);
			
			Float longitudinalToleranceLowerBound = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:deviationTolerance/task:longitudinal/@lowerBound", Float.class);
			
			Float longitudinalToleranceUpperBound = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:deviationTolerance/task:longitudinal/@upperBound", Float.class);
			
			Float lateralToleranceLowerBound = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:deviationTolerance/task:lateral/@lowerBound", Float.class);
			
			Float lateralToleranceUpperBound = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:deviationTolerance/task:lateral/@upperBound", Float.class);

			Float startPositionZ = dtData.getValue(Layer.TASK,
					steeringTaskPath + "/task:logging/task:startPosition/@z", Float.class);
			
			Float endPositionZ = dtData.getValue(Layer.TASK,
					steeringTaskPath + "/task:logging/task:endPosition/@z", Float.class);
			
			Boolean shutDownAtEnd = dtData.getValue(Layer.TASK,
					steeringTaskPath + "/task:logging/task:endPosition/@shutDownWhenReached", Boolean.class);
			
			Integer loggingRate = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:logging/task:loggingRate", Integer.class);
	
			
			String databaseUrl = "";
			String databaseUser = "";
			String databasePassword = "";
			String databaseTable = "";
			Boolean writeToDB = false;
			
			// check whether DB node exists
			Node databaseNode = (Node) dtData.xPathQuery(Layer.TASK, 
					steeringTaskPath + "/task:logging/task:database", XPathConstants.NODE);

			if(databaseNode != null)
			{
				databaseUrl = dtData.getValue(Layer.TASK, 
						steeringTaskPath + "/task:logging/task:database/@url", String.class);
				
				databaseUser = dtData.getValue(Layer.TASK, 
						steeringTaskPath + "/task:logging/task:database/@user", String.class);
				
				databasePassword = dtData.getValue(Layer.TASK, 
						steeringTaskPath + "/task:logging/task:database/@password", String.class);
				
				databaseTable = dtData.getValue(Layer.TASK, 
						steeringTaskPath + "/task:logging/task:database/@table", String.class);
				
				writeToDB = true;
			}
			
			String conditionName = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:logging/task:condition/@name", String.class);
			
			Integer conditionNumber = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:logging/task:condition/@number", Integer.class);

			String reportTemplate = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:logging/task:reportTemplate", String.class);
			
			Boolean additionalTable = dtData.getValue(Layer.TASK, 
					steeringTaskPath + "/task:logging/task:additionalTable", Boolean.class);
			
			tvpTaskSettings = new TVPTaskSettings(leadingCarName, minDistanceToLeadingCar, 
					maxDistanceToLeadingCar, followerCarName, minDistanceToFollowerCar, 
					maxDistanceToFollowerCar, laneOffsetX, brakeLightMinDuration, turnSignalDuration,
					maxReactionTime, longitudinalToleranceLowerBound, longitudinalToleranceUpperBound, 
					lateralToleranceLowerBound, lateralToleranceUpperBound, startPositionZ, endPositionZ, 
					shutDownAtEnd, loggingRate, writeToDB, databaseUrl, databaseUser, databasePassword, 
					databaseTable, conditionName, conditionNumber, reportTemplate, additionalTable);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	public TVPTaskSettings getTVPTaskSettings() 
	{
		return tvpTaskSettings;
	}

	
}
