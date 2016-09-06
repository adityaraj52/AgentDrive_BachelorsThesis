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

package eu.opends.jasperReport;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme3.math.FastMath;

import eu.opends.main.Simulator;


/**
 * Class for handling database connection and logging to database
 * 
 * @author Rafael Math
 */
public class ContreReport extends JasperReport
{
    // TODO: load from settings.xml
	private float maxDeviation = 1; // --> blinking threshold

    
    /**
     * Constructor, that creates database connection and prepared statement for fast query execution
     */
    public ContreReport(String reportTemplate, String url, String user, String pass, String table,
    		boolean useAdditionalTable) 
    {
    	super(reportTemplate, url, user, pass, table, useAdditionalTable);

    	
        try {

            // Creating prepared statement for faster query execution all "?" then have to be assigned some value using statement.set[Float,Int,Long,String,etc]
        	statement = connection.prepareStatement(getInsertStatement(table));

            PreparedStatement clearStatement = connection.prepareStatement("TRUNCATE TABLE " + table);
            clearStatement.executeUpdate();
            
            if(useAdditionalTable)
            {
            	String additionalTable = table + "_" + Simulator.getOutputFolder().replace("analyzerData/", "");
            	
            	// create new table
            	String createStatement = getCreateStatement(additionalTable);
            	PreparedStatement newTableStatement = connection.prepareStatement(createStatement);
            	newTableStatement.executeUpdate();
            	
            	additionalStatement = connection.prepareStatement(getInsertStatement(additionalTable));
            }
            
            parameters.put("maxDeviation", new Float(FastMath.abs(maxDeviation)));
            	
        } catch(Exception e) {

        	e.getStackTrace();
        }
    }


	private String getInsertStatement(String table) 
	{
		return "INSERT INTO `" + table + "` (subject_name, is_main_driver, condition_name, condition_number, " +
		"lateral_target_pos, lateral_steering_pos, steering_deviation, light_state, co_driver_reaction, co_driver_reaction_time, brake_reaction, " +
		"brake_reaction_time_driver, brake_reaction_time_co_driver, acceleration_reaction, acceleration_reaction_time_driver, " +
		"acceleration_reaction_time_co_driver, absolute_time, gesture_reaction, gesture_reaction_time, lat_relevant_building, lon_relevant_building, " +
		"x_screen_coordinate, y_screen_coordinate, non_relevant_buildings, experimentTime, markerID) VALUES (?, ?, ?, ?,   ?, ?, ?, ?, ?, ?, ?,   ?, ?, ?, ?,  " +
		" ?, ?, ?, ?, ?,   ?, ?, ?, ?,?, ?);";
	}


	private String getCreateStatement(String table) 
	{
		return "CREATE TABLE IF NOT EXISTS `" + table + "` (" +
				"`subject_name` varchar(100) default NULL," +
				"`is_main_driver` tinyint(1) default NULL," +
				"`condition_name` varchar(20) default NULL," +
				"`condition_number` bigint(20) default NULL," +
				"`lateral_target_pos` float default NULL," +
				"`lateral_steering_pos` float default NULL," +
				"`steering_deviation` float default NULL," +
				"`light_state` varchar(20) default NULL," +
				"`co_driver_reaction` int(11) default NULL," +
				"`co_driver_reaction_time` bigint(20) default NULL," +
				"`brake_reaction` int(11) default NULL," +
				"`brake_reaction_time_driver` bigint(20) default NULL," +
				"`brake_reaction_time_co_driver` bigint(20) default NULL," +
				"`acceleration_reaction` int(11) default NULL," +
				"`acceleration_reaction_time_driver` bigint(20) default NULL," +
				"`acceleration_reaction_time_co_driver` bigint(20) default NULL," +
				"`absolute_time` bigint(20) default NULL," +
				"`gesture_reaction` int(11) default NULL," +
				"`gesture_reaction_time` bigint(20) default NULL," +
				"`lat_relevant_building` float default NULL," +
				"`lon_relevant_building` float default NULL," +
				"`x_screen_coordinate` float default NULL," +
				"`y_screen_coordinate` float default NULL," +
				"`non_relevant_buildings` int(11) default NULL," +
				"`experimentTime` bigint(20) default NULL," +
				"`markerID` varchar(100) default NULL) ENGINE=MyISAM DEFAULT CHARSET=ascii;";
	}


    // Method, which writes record to database. It assigns to each "?" in prepared statement definite value and then executes 
    // update inserting record in database
    public void addDataSet(String subjectName, boolean isMainDriver, String conditionName, long conditionNumber,
    							float lateralTargetPos, float lateralSteeringPos, float steeringDeviation, 
    							String lightStateAndPosition, int coDriverReaction, long coDriverReactionTime, int brakeReaction,
                                long brakeReactionTimeDriver, long brakeReactionTimeCoDriver, int accelReaction, 
                                long accelReactionTimeDriver, long accelReactionTimeCoDriver, long absoluteTime, 
                                int gestureReaction, long gestureReactionTime, float latRelBuilding, float lonRelBuilding, 
                                float xCoordinate, float yCoordinate, int nonRelBuildings, long experimentTime, String markerID)
    {
        try {
            statement.setString(1, subjectName);
            statement.setBoolean(2, isMainDriver);
            statement.setString(3, conditionName);
            statement.setLong(4, conditionNumber);
            
            statement.setFloat(5, lateralTargetPos);
            statement.setFloat(6, lateralSteeringPos);
            statement.setFloat(7, steeringDeviation);
            statement.setString(8, lightStateAndPosition);
            statement.setInt(9, coDriverReaction);
            statement.setLong(10, coDriverReactionTime);
            
            statement.setInt(11, brakeReaction);
            statement.setLong(12, brakeReactionTimeDriver);
            statement.setLong(13, brakeReactionTimeCoDriver);
            
            statement.setInt(14, accelReaction);
            statement.setLong(15, accelReactionTimeDriver);
            statement.setLong(16, accelReactionTimeCoDriver);
            
            statement.setLong(17, absoluteTime);
            statement.setInt(18, gestureReaction);
            statement.setLong(19, gestureReactionTime);
            
            statement.setFloat(20, latRelBuilding);            
            statement.setFloat(21, lonRelBuilding);
            statement.setFloat(22, xCoordinate);
            statement.setFloat(23, yCoordinate);
            
            statement.setInt(24, nonRelBuildings);
            statement.setLong(25, experimentTime);
            
            statement.setString(26, markerID);
            
            statement.executeUpdate();
            
            if(useAdditionalTable)
            {
                additionalStatement.setString(1, subjectName);
                additionalStatement.setBoolean(2, isMainDriver);
                additionalStatement.setString(3, conditionName);
                additionalStatement.setLong(4, conditionNumber);
                
                additionalStatement.setFloat(5, lateralTargetPos);
                additionalStatement.setFloat(6, lateralSteeringPos);
                additionalStatement.setFloat(7, steeringDeviation);
                additionalStatement.setString(8, lightStateAndPosition);
                additionalStatement.setInt(9, coDriverReaction);
                additionalStatement.setLong(10, coDriverReactionTime);
                
                additionalStatement.setInt(11, brakeReaction);
                additionalStatement.setLong(12, brakeReactionTimeDriver);
                additionalStatement.setLong(13, brakeReactionTimeCoDriver);
                
                additionalStatement.setInt(14, accelReaction);
                additionalStatement.setLong(15, accelReactionTimeDriver);
                additionalStatement.setLong(16, accelReactionTimeCoDriver);
                
                additionalStatement.setLong(17, absoluteTime);
                additionalStatement.setInt(18, gestureReaction);
                additionalStatement.setLong(19, gestureReactionTime);
                
                additionalStatement.setFloat(20, latRelBuilding);            
                additionalStatement.setFloat(21, lonRelBuilding);
                additionalStatement.setFloat(22, xCoordinate);
                additionalStatement.setFloat(23, yCoordinate);
                
                additionalStatement.setInt(24, nonRelBuildings);
                additionalStatement.setLong(25, experimentTime);
                
                additionalStatement.setString(26, markerID);
                
                additionalStatement.executeUpdate();
            }

        } catch (SQLException ex) {
        	
            Logger.getLogger(JasperReport.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
