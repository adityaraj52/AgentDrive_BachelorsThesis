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

package eu.opends.trigger;

import eu.opends.basics.SimulationBasics;
import eu.opends.main.Simulator;
import eu.opends.traffic.PhysicalTraffic;
import eu.opends.traffic.TrafficCar;


/**
 * This class represents a MoveTraffic trigger action. Whenever a collision
 * with a related trigger was detected, the given traffic object will be moved
 * to the given way point
 * 
 * @author Rafael Math
 */
public class MoveTrafficTriggerAction extends TriggerAction 
{
	private SimulationBasics sim;
	private int trafficCarId;
	private String wayPointID;
	
	
	/**
	 * Creates a new MoveTraffic trigger action instance, providing traffic 
	 * object's name and way point's ID.
	 * 
	 * @param maxRepeat
	 * 			Number of maximum recurrences
	 * 
	 * @param trafficCarId
	 * 			Name of the traffic object to move.
	 * 
	 * @param wayPointID
	 * 			ID of the way point to move the traffic object to.
	 */
	public MoveTrafficTriggerAction(SimulationBasics sim, float delay, int maxRepeat, int trafficCarId, String wayPointID)
	{
		super(delay, maxRepeat);
		this.sim = sim;
		this.trafficCarId = trafficCarId;
		this.wayPointID = wayPointID;
	}

	
	/**
	 * Moves the given traffic participant to the given way point
	 */
	@Override
	protected void execute() 
	{
		if(!isExceeded())
		{		
			if(sim instanceof Simulator)
			{
				PhysicalTraffic physicalTraffic = ((Simulator)sim).getPhysicalTraffic();
				TrafficCar vehicle = physicalTraffic.getTrafficCar(trafficCarId);
				
				if(vehicle != null)
					vehicle.setToWayPoint(wayPointID);
				
				updateCounter();
			}
		}
	}
}
