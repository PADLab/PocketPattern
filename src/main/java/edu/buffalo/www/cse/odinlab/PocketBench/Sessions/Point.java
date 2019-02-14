package edu.buffalo.www.cse.odinlab.PocketBench.Sessions;

public class Point{
	public Integer idleTime;
	public Long numberofSessions;
	
	@Override
	public String toString() {
		return "Point [" + (idleTime != null ? "idleTime=" + idleTime + ", " : "")
				+ (numberofSessions != null ? "numberofSessions=" + numberofSessions : "") + "]";
	}

	/**
	 * @param idleTime
	 * @param numberofSessions
	 */
	public Point(Integer idleTime, Long numberofSessions) {
		super();
		this.idleTime = idleTime;
		this.numberofSessions = numberofSessions;
	}
	
	public Integer getIdleTime() {
		return idleTime;
	}
	public void setIdleTime(Integer idleTime) {
		this.idleTime = idleTime;
	}
	public Long getNumberofSessions() {
		return numberofSessions;
	}
	public void setNumberofSessions(Long numberofSessions) {
		this.numberofSessions = numberofSessions;
	}
}
