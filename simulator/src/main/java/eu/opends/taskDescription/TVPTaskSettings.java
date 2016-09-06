/*
*  This file is part of OpenDS (Open Source Driving Simulator).
*  Copyright (C) 2013 Rafael Math
*
*  OpenDS is free software: you can redistribute it and/or modify
*  it under the terms of the GNU Lesser General Public License as published by
*  the Free Software Foundation, either version 3 of the License, or
*  (at your option) any later version.
*
*  OpenDS is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU Lesser General Public License for more details.
*
*  You should have received a copy of the GNU Lesser General Public License
*  along with OpenDS. If not, see <http://www.gnu.org/licenses/>.
*/

package eu.opends.taskDescription;

public class TVPTaskSettings 
{
	private String leadingCarName;
	private Float minDistanceToLeadingCar;
	private Float maxDistanceToLeadingCar;
	private String followerCarName;
	private Float minDistanceToFollowerCar;
	private Float maxDistanceToFollowerCar;
	private Float laneOffsetX;
	private Integer brakeLightMinDuration;
	private Integer turnSignalDuration;
	private Integer maxReactionTime;
	private Float longitudinalToleranceLowerBound;
	private Float longitudinalToleranceUpperBound;
	private Float lateralToleranceLowerBound;
	private Float lateralToleranceUpperBound;
	private Float startPositionZ;
	private Float endPositionZ;
	private Boolean shutDownAtEnd;
	private Integer loggingRate;
	private Boolean writeToDB;
	private String databaseUrl;
	private String databaseUser;
	private String databasePassword;
	private String databaseTable;
	private String conditionName;
	private Integer conditionNumber;
	private String reportTemplate;
	private Boolean additionalTable;

	
	public TVPTaskSettings(String leadingCarName,
			Float minDistanceToLeadingCar, Float maxDistanceToLeadingCar,
			String followerCarName, Float minDistanceToFollowerCar,
			Float maxDistanceToFollowerCar, Float laneOffsetX,
			Integer brakeLightMinDuration, Integer turnSignalDuration,
			Integer maxReactionTime, Float longitudinalToleranceLowerBound,
			Float longitudinalToleranceUpperBound,
			Float lateralToleranceLowerBound, Float lateralToleranceUpperBound,
			Float startPositionZ, Float endPositionZ, Boolean shutDownAtEnd,
			Integer loggingRate, Boolean writeToDB, String databaseUrl,
			String databaseUser, String databasePassword, String databaseTable,
			String conditionName, Integer conditionNumber, 	String reportTemplate,
			Boolean additionalTable) 
	{
		this.leadingCarName = leadingCarName;
		this.minDistanceToLeadingCar = minDistanceToLeadingCar;
		this.maxDistanceToLeadingCar = maxDistanceToLeadingCar;
		this.followerCarName = followerCarName;
		this.minDistanceToFollowerCar = minDistanceToFollowerCar;
		this.maxDistanceToFollowerCar = maxDistanceToFollowerCar;
		this.laneOffsetX = laneOffsetX;
		this.brakeLightMinDuration = brakeLightMinDuration;
		this.turnSignalDuration = turnSignalDuration;
		this.maxReactionTime = maxReactionTime;
		this.longitudinalToleranceLowerBound = longitudinalToleranceLowerBound;
		this.longitudinalToleranceUpperBound = longitudinalToleranceUpperBound;
		this.lateralToleranceLowerBound = lateralToleranceLowerBound;
		this.lateralToleranceUpperBound = lateralToleranceUpperBound;
		this.startPositionZ = startPositionZ;
		this.endPositionZ = endPositionZ;
		this.shutDownAtEnd = shutDownAtEnd;
		this.loggingRate = loggingRate;
		this.writeToDB = writeToDB;
		this.databaseUrl = databaseUrl;
		this.databaseUser = databaseUser;
		this.databasePassword = databasePassword;
		this.databaseTable = databaseTable;
		this.conditionName = conditionName;
		this.conditionNumber = conditionNumber;
		this.reportTemplate = reportTemplate;
		this.additionalTable = additionalTable;
	}

	public String getLeadingCarName() {
		return leadingCarName;
	}

	public void setLeadingCarName(String leadingCarName) {
		this.leadingCarName = leadingCarName;
	}

	public Float getMinDistanceToLeadingCar() {
		return minDistanceToLeadingCar;
	}

	public void setMinDistanceToLeadingCar(Float minDistanceToLeadingCar) {
		this.minDistanceToLeadingCar = minDistanceToLeadingCar;
	}

	public Float getMaxDistanceToLeadingCar() {
		return maxDistanceToLeadingCar;
	}

	public void setMaxDistanceToLeadingCar(Float maxDistanceToLeadingCar) {
		this.maxDistanceToLeadingCar = maxDistanceToLeadingCar;
	}

	public String getFollowerCarName() {
		return followerCarName;
	}

	public void setFollowerCarName(String followerCarName) {
		this.followerCarName = followerCarName;
	}

	public Float getMinDistanceToFollowerCar() {
		return minDistanceToFollowerCar;
	}

	public void setMinDistanceToFollowerCar(Float minDistanceToFollowerCar) {
		this.minDistanceToFollowerCar = minDistanceToFollowerCar;
	}

	public Float getMaxDistanceToFollowerCar() {
		return maxDistanceToFollowerCar;
	}

	public void setMaxDistanceToFollowerCar(Float maxDistanceToFollowerCar) {
		this.maxDistanceToFollowerCar = maxDistanceToFollowerCar;
	}

	public Float getLaneOffsetX() {
		return laneOffsetX;
	}

	public void setLaneOffsetX(Float laneOffsetX) {
		this.laneOffsetX = laneOffsetX;
	}

	public Integer getBrakeLightMinDuration() {
		return brakeLightMinDuration;
	}

	public void setBrakeLightMinDuration(Integer brakeLightMinDuration) {
		this.brakeLightMinDuration = brakeLightMinDuration;
	}

	public Integer getTurnSignalDuration() {
		return turnSignalDuration;
	}

	public void setTurnSignalDuration(Integer turnSignalDuration) {
		this.turnSignalDuration = turnSignalDuration;
	}

	public Integer getMaxReactionTime() {
		return maxReactionTime;
	}
	
	public void setMaxReactionTime(Integer maxReactionTime) {
		this.maxReactionTime = maxReactionTime;
	}
	
	public Float getLongitudinalToleranceLowerBound() {
		return longitudinalToleranceLowerBound;
	}

	public void setLongitudinalToleranceLowerBound(
			Float longitudinalToleranceLowerBound) {
		this.longitudinalToleranceLowerBound = longitudinalToleranceLowerBound;
	}

	public Float getLongitudinalToleranceUpperBound() {
		return longitudinalToleranceUpperBound;
	}

	public void setLongitudinalToleranceUpperBound(
			Float longitudinalToleranceUpperBound) {
		this.longitudinalToleranceUpperBound = longitudinalToleranceUpperBound;
	}

	public Float getLateralToleranceLowerBound() {
		return lateralToleranceLowerBound;
	}

	public void setLateralToleranceLowerBound(Float lateralToleranceLowerBound) {
		this.lateralToleranceLowerBound = lateralToleranceLowerBound;
	}

	public Float getLateralToleranceUpperBound() {
		return lateralToleranceUpperBound;
	}

	public void setLateralToleranceUpperBound(Float lateralToleranceUpperBound) {
		this.lateralToleranceUpperBound = lateralToleranceUpperBound;
	}

	public Float getStartPositionZ() {
		return startPositionZ;
	}

	public void setStartPositionZ(Float startPositionZ) {
		this.startPositionZ = startPositionZ;
	}

	public Float getEndPositionZ() {
		return endPositionZ;
	}

	public void setEndPositionZ(Float endPositionZ) {
		this.endPositionZ = endPositionZ;
	}

	public Boolean isShutDownAtEnd() {
		return shutDownAtEnd;
	}

	public void setShutDownAtEnd(Boolean shutDownAtEnd) {
		this.shutDownAtEnd = shutDownAtEnd;
	}

	public Integer getLoggingRate() {
		return loggingRate;
	}

	public void setLoggingRate(Integer loggingRate) {
		this.loggingRate = loggingRate;
	}

	public Boolean isWriteToDB() {
		return writeToDB;
	}

	public void setWriteToDB(Boolean writeToDB) {
		this.writeToDB = writeToDB;
	}

	public String getDatabaseUrl() {
		return databaseUrl;
	}

	public void setDatabaseUrl(String databaseUrl) {
		this.databaseUrl = databaseUrl;
	}

	public String getDatabaseUser() {
		return databaseUser;
	}

	public void setDatabaseUser(String databaseUser) {
		this.databaseUser = databaseUser;
	}

	public String getDatabasePassword() {
		return databasePassword;
	}

	public void setDatabasePassword(String databasePassword) {
		this.databasePassword = databasePassword;
	}

	public String getDatabaseTable() {
		return databaseTable;
	}

	public void setDatabaseTable(String databaseTable) {
		this.databaseTable = databaseTable;
	}

	public String getConditionName() {
		return conditionName;
	}

	public void setConditionName(String conditionName) {
		this.conditionName = conditionName;
	}

	public Integer getConditionNumber() {
		return conditionNumber;
	}

	public void setConditionNumber(Integer conditionNumber) {
		this.conditionNumber = conditionNumber;
	}
	
	public String getReportTemplate() {
		return reportTemplate;
	}
	
	public void setReportTemplate(String reportTemplate) {
		this.reportTemplate = reportTemplate;
	}

	public Boolean getUseAdditionalTable() {
		return additionalTable;
	}
	
	public void setUseAdditionalTable(Boolean additionalTable) {
		this.additionalTable = additionalTable;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TVPTaskSettings [brakeLightMinDuration="
				+ brakeLightMinDuration + ", conditionName=" + conditionName
				+ ", conditionNumber=" + conditionNumber
				+ ", databasePassword=" + databasePassword + ", databaseTable="
				+ databaseTable + ", databaseUrl=" + databaseUrl
				+ ", databaseUser=" + databaseUser + ", endPositionZ="
				+ endPositionZ + ", followerCarName=" + followerCarName
				+ ", laneOffsetX=" + laneOffsetX
				+ ", lateralToleranceLowerBound=" + lateralToleranceLowerBound
				+ ", lateralToleranceUpperBound=" + lateralToleranceUpperBound
				+ ", leadingCarName=" + leadingCarName + ", loggingRate="
				+ loggingRate + ", longitudinalToleranceLowerBound="
				+ longitudinalToleranceLowerBound
				+ ", longitudinalToleranceUpperBound="
				+ longitudinalToleranceUpperBound
				+ ", maxDistanceToFollowerCar=" + maxDistanceToFollowerCar
				+ ", maxDistanceToLeadingCar=" + maxDistanceToLeadingCar
				+ ", minDistanceToFollowerCar=" + minDistanceToFollowerCar
				+ ", minDistanceToLeadingCar=" + minDistanceToLeadingCar
				+ ", shutDownAtEnd=" + shutDownAtEnd + ", startPositionZ="
				+ startPositionZ + ", turnSignalDuration=" + turnSignalDuration
				+ ", writeToDB=" + writeToDB + "]";
	}


}