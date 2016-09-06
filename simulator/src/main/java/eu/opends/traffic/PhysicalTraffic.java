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

package eu.opends.traffic;

import java.util.ArrayList;
import java.util.HashMap;

import com.jme3.math.Vector3f;

import eu.opends.car.SteeringCar;
import eu.opends.main.Simulator;

/**
 * 
 * @author Rafael Math
 */
public class PhysicalTraffic extends Thread
{
	private static HashMap<Integer, TrafficCarData> vehicleDataMap = new HashMap<Integer, TrafficCarData>();
    private HashMap<Integer, TrafficCar> vehicleMap = new HashMap<Integer, TrafficCar>();
    private HashMap<Integer,TrafficCar> ghosts = new HashMap<Integer,TrafficCar>();
	private boolean isRunning = true;
	private int updateIntervalMsec = 20;
	private long lastUpdate = 0;
	
	private SteeringCar egoCar;

	public PhysicalTraffic(Simulator sim)
	{  
//	    egoCar = sim.getCar();
//
//		for(TrafficCarData vehicleData : vehicleDataList)
//		{
//			// build and add traffic car
////            if(vehicleData.getName().length() <= 5){
////		    	vehicleList.add(new TrafficCar(sim, vehicleData));
////            }else{
////                ghosts.put(vehicleData.getName(),new TrafficCar(sim, vehicleData));
////            }
//            vehicleList.add(new TrafficCar(sim, vehicleData));
//		}
	}

    public void setEgoCar(SteeringCar egoCar) {
        this.egoCar = egoCar;
    }

    public static HashMap<Integer, TrafficCarData> getVehicleDataMap() {
        return vehicleDataMap;
    }

    public HashMap<Integer, TrafficCar> getVehicleMap() {
        return vehicleMap;
    }

    public HashMap<Integer, TrafficCar> getGhosts() {
        return ghosts;
    }

    public TrafficCar getTrafficCar(int trafficCarId)
	{
        return vehicleMap.get(trafficCarId);
	}
	
	
	public void run()
	{
		if(vehicleMap.size() >= 1)
		{
			/*
			for(TrafficCar vehicle : vehicleList)
				vehicle.showInfo();
			*/
			
			while (isRunning) 
			{
				long elapsedTime = System.currentTimeMillis() - lastUpdate;
				
				if (elapsedTime > updateIntervalMsec) 
				{
					lastUpdate = System.currentTimeMillis();
					
					// update every vehicle
					for(TrafficCar vehicle : vehicleMap.values())
						vehicle.update(vehicleMap.values());
				}
				else
				{
					// sleep until update interval has elapsed
					try {
						Thread.sleep(updateIntervalMsec - elapsedTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			//System.out.println("PhysicalTraffic closed");
		}
	}
	
	
	// TODO use thread instead
	public void update()
	{
        int egoTeleport = 1000;//where ego vehicle is teleported
        int visibility = 200;//the distance the other vehicles continue before also being teleported (to be seen by driver)
        int teleportDistance = (egoTeleport-500); //not to start at 0 where texture ends

	    Vector3f egoPos = egoCar.getPosition();
	    Boolean teleport = false;
	    if(egoPos.z > egoTeleport){
            //teleport ego
	       teleport = true; 
	       egoPos.setZ(egoPos.z - teleportDistance);
	       egoCar.setPosition(egoPos);
	    }
		for(TrafficCar vehicle : vehicleMap.values()){
            Vector3f pos = vehicle.getPosition();
            if(pos.z > egoTeleport + visibility){
                //teleport all too far
                pos.setZ(egoPos.z - visibility);
                vehicle.setPosition(pos);
            }else if(pos.z > egoTeleport && teleport){
                //teleport cars in ego's visibility range
		       pos.setZ(pos.z - teleportDistance);
		       vehicle.setPosition(pos);
		   }
			vehicle.update(vehicleMap.values());
		}
	}


	public synchronized void close() 
	{
		isRunning = false;
		
		// close all traffic cars
		for(TrafficCar vehicle : vehicleMap.values())
			vehicle.close();
	}

}
