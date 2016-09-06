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
public class TVPTReport extends JasperReport
{   
    /**
     * Constructor, that creates database connection and prepared statement for fast query execution
     */
    public TVPTReport(String reportTemplate, String url, String user, String pass, String table,
    		boolean useAdditionalTable, float longitudinalToleranceLowerBound, 
    		float longitudinalToleranceUpperBound, float lateralToleranceLowerBound, 
    		float lateralToleranceUpperBound) 
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

			parameters.put("maxDeviation", new Float(FastMath.abs(lateralToleranceLowerBound)));
			parameters.put("longitudinalToleranceLowerBound", new Float(longitudinalToleranceLowerBound));
			parameters.put("longitudinalToleranceUpperBound", new Float(longitudinalToleranceUpperBound));
			parameters.put("lateralToleranceLowerBound", new Float(lateralToleranceLowerBound));
			parameters.put("lateralToleranceUpperBound", new Float(lateralToleranceUpperBound));
            	
        } catch(Exception e) {

        	e.getStackTrace();
        }
    }

    
	private String getInsertStatement(String table) 
	{
		return "INSERT INTO `" + table + "` (driverName, conditionName, conditionNumber, steeringCarSpeedKmh, " +
			"steeringCarX, steeringCarZ, leadingCarSpeedKmh, leadingCarZ, followerCarSpeedKmh, " +
			"followerCarZ, leadingCarBrakeLightOn, followerCarTurnSignalOn, distanceToLeadingCar, " +
			"distanceFromLaneCenter, distanceToFollowerCar, longitudinalDeviation, lateralDeviation, " +
			"brakeLightReaction, brakeLightReactionTime, turnSignalReaction, turnSignalReactionTime, " +
			"speedReduction1Reaction, speedReduction1ReactionTime, speedReduction2Reaction, " +
			"speedReduction2ReactionTime, speedReduction3Reaction, speedReduction3ReactionTime, " +
			"speedReduction4Reaction, speedReduction4ReactionTime, acceleratorIntensity, brakeIntensity, " +
			"absoluteTime, experimentTime) VALUES " +
			"(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	}


	private String getCreateStatement(String table) 
	{
		return "CREATE TABLE IF NOT EXISTS `" + table + "` (" +
			"`driverName` varchar(100) default NULL," +
			"`conditionName` varchar(100) default NULL," +
			"`conditionNumber` bigint(20) default NULL," +
			"`steeringCarSpeedKmh` float default NULL," +
			"`steeringCarX` float default NULL," +
			"`steeringCarZ` float default NULL," +
			"`leadingCarSpeedKmh` float default NULL," +
			"`leadingCarZ` float default NULL," +
			"`followerCarSpeedKmh` float default NULL," +
			"`followerCarZ` float default NULL," +
			"`leadingCarBrakeLightOn` bool default NULL," +
			"`followerCarTurnSignalOn` bool default NULL," +
			"`distanceToLeadingCar` float default NULL," +
			"`distanceFromLaneCenter` float default NULL," +
			"`distanceToFollowerCar` float default NULL," +
			"`longitudinalDeviation` float default NULL," +
			"`lateralDeviation` float default NULL," +
			"`brakeLightReaction` int(11) default NULL," +
			"`brakeLightReactionTime` bigint(20) default NULL," +
			"`turnSignalReaction` int(11) default NULL," +
			"`turnSignalReactionTime` bigint(20) default NULL," +
			"`speedReduction1Reaction` int(11) default NULL," +
			"`speedReduction1ReactionTime` bigint(20) default NULL," +
			"`speedReduction2Reaction` int(11) default NULL," +
			"`speedReduction2ReactionTime` bigint(20) default NULL," +
			"`speedReduction3Reaction` int(11) default NULL," +
			"`speedReduction3ReactionTime` bigint(20) default NULL," +
			"`speedReduction4Reaction` int(11) default NULL," +
			"`speedReduction4ReactionTime` bigint(20) default NULL," +
			"`acceleratorIntensity` float default NULL," +
			"`brakeIntensity` float default NULL," +
			"`absoluteTime` bigint(20) default NULL," +
			"`experimentTime` bigint(20) default NULL) ENGINE=MyISAM DEFAULT CHARSET=ascii;";
	}
	

	public void addDataSet(String driverName, String conditionName, int conditionNumber, float steeringCarSpeedKmh, 
			float steeringCarX,	float steeringCarZ, float leadingCarSpeedKmh, float leadingCarZ, 
			float followerCarSpeedKmh, float followerCarZ, boolean leadingCarBrakeLightOn, boolean followerCarTurnSignalOn, 
			float distanceToLeadingCar,	float distanceFromLaneCenter, float distanceToFollowerCar,
			float longitudinalDeviation, float lateralDeviation, int brakeLightReaction, long brakeLightReactionTime,
			int turnSignalReaction, long turnSignalReactionTime, int speedReduction1Reaction, long speedReduction1ReactionTime,
			int speedReduction2Reaction, long speedReduction2ReactionTime, int speedReduction3Reaction, 
			long speedReduction3ReactionTime, int speedReduction4Reaction, long speedReduction4ReactionTime,
			float acceleratorIntensity, float brakeIntensity, long absoluteTime, long experimentTime)
	{
        try {

            statement.setString(1, driverName);
            statement.setString(2, conditionName);
            statement.setInt(3, conditionNumber);
            
            statement.setFloat(4, steeringCarSpeedKmh);            
            statement.setFloat(5, steeringCarX);
            statement.setFloat(6, steeringCarZ);
            statement.setFloat(7, leadingCarSpeedKmh);
            statement.setFloat(8, leadingCarZ);
            statement.setFloat(9, followerCarSpeedKmh);
            statement.setFloat(10, followerCarZ);
            
            statement.setBoolean(11, leadingCarBrakeLightOn);
            statement.setBoolean(12, followerCarTurnSignalOn);
            
            statement.setFloat(13, distanceToLeadingCar);
            statement.setFloat(14, distanceFromLaneCenter);
            statement.setFloat(15, distanceToFollowerCar);
            statement.setFloat(16, longitudinalDeviation);
            statement.setFloat(17, lateralDeviation);
            
            statement.setInt(18, brakeLightReaction);
            statement.setLong(19, brakeLightReactionTime);
            statement.setInt(20, turnSignalReaction);            
            statement.setLong(21, turnSignalReactionTime);
            statement.setInt(22, speedReduction1Reaction);
            statement.setLong(23, speedReduction1ReactionTime);
            statement.setInt(24, speedReduction2Reaction);
            statement.setLong(25, speedReduction2ReactionTime);
            statement.setInt(26, speedReduction3Reaction);
            statement.setLong(27, speedReduction3ReactionTime);
            statement.setInt(28, speedReduction4Reaction);
            statement.setLong(29, speedReduction4ReactionTime);
            
            statement.setFloat(30, acceleratorIntensity);
            statement.setFloat(31, brakeIntensity);
            
            statement.setLong(32, absoluteTime);
            statement.setLong(33, experimentTime);
            
            statement.executeUpdate();
            
            if(useAdditionalTable)
            {
            	additionalStatement.setString(1, driverName);
            	additionalStatement.setString(2, conditionName);
            	additionalStatement.setInt(3, conditionNumber);
                
            	additionalStatement.setFloat(4, steeringCarSpeedKmh);            
            	additionalStatement.setFloat(5, steeringCarX);
            	additionalStatement.setFloat(6, steeringCarZ);
            	additionalStatement.setFloat(7, leadingCarSpeedKmh);
            	additionalStatement.setFloat(8, leadingCarZ);
            	additionalStatement.setFloat(9, followerCarSpeedKmh);
            	additionalStatement.setFloat(10, followerCarZ);
                
            	additionalStatement.setBoolean(11, leadingCarBrakeLightOn);
            	additionalStatement.setBoolean(12, followerCarTurnSignalOn);
                
            	additionalStatement.setFloat(13, distanceToLeadingCar);
            	additionalStatement.setFloat(14, distanceFromLaneCenter);
            	additionalStatement.setFloat(15, distanceToFollowerCar);
            	additionalStatement.setFloat(16, longitudinalDeviation);
            	additionalStatement.setFloat(17, lateralDeviation);
                
            	additionalStatement.setInt(18, brakeLightReaction);
            	additionalStatement.setLong(19, brakeLightReactionTime);
            	additionalStatement.setInt(20, turnSignalReaction);            
            	additionalStatement.setLong(21, turnSignalReactionTime);
            	additionalStatement.setInt(22, speedReduction1Reaction);
            	additionalStatement.setLong(23, speedReduction1ReactionTime);
            	additionalStatement.setInt(24, speedReduction2Reaction);
            	additionalStatement.setLong(25, speedReduction2ReactionTime);
            	additionalStatement.setInt(26, speedReduction3Reaction);
            	additionalStatement.setLong(27, speedReduction3ReactionTime);
            	additionalStatement.setInt(28, speedReduction4Reaction);
            	additionalStatement.setLong(29, speedReduction4ReactionTime);
            	
            	additionalStatement.setFloat(30, acceleratorIntensity);
            	additionalStatement.setFloat(31, brakeIntensity);
                
            	additionalStatement.setLong(32, absoluteTime);
            	additionalStatement.setLong(33, experimentTime);
            	
            	additionalStatement.executeUpdate();
            }

        } catch (SQLException ex) {
        	
            Logger.getLogger(TVPTReport.class.getName()).log(Level.SEVERE, null, ex);
        }
	}

}
