package edu.buffalo.www.cse.odinlab.PocketBench.SessionSimilarity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.buffalo.www.cse.odinlab.PocketBench.ControlledData.ActivityData;
import edu.buffalo.www.cse.odinlab.PocketBench.ControlledData.Label;
import edu.buffalo.www.cse.odinlab.PocketBench.Sessions.DataRow;
import edu.buffalo.www.cse.odinlab.PocketBench.Sessions.Session;

public class LabeledSession {
	
	private long sessionID = -1;
	private List<Label> labels = null;
	private List<ActivityData> rows = null;
	private String startTime = null;
	private String endTime = null;
	private String user = null;

	public List<ActivityData> getRows() {
		return rows;
	}

	public void setRows(List<ActivityData> rows) {
		this.rows = rows;
	}
	
	public void addRow(ActivityData newRow) {
		if (this.rows == null) {
			this.rows = new ArrayList<ActivityData>();
		}
		this.rows.add(newRow);
	}
	
	public void addRows(List<ActivityData> newRows) {
		if (this.rows == null) {
			this.rows = new ArrayList<ActivityData>();
		}
		this.rows.addAll(newRows);
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public LabeledSession(String user, long sessionID) {
		super();
		// TODO Auto-generated constructor stub
		this.user = user;
		this.sessionID = sessionID;
	}

	public LabeledSession(String user, String startTime, String endTime) {
		//TODO 
	}

	public long getSessionID() {
		return sessionID;
	}

	public void setSessionID(long sessionID) {
		this.sessionID = sessionID;
	}

	public List<Label> getLabels() {
		return labels;
	}

	public void setLabels(List<Label> labels) {
		this.labels = labels;
		Collections.sort(this.labels);
	}
	
	public void addLabel(Label label) {
		if (this.labels == null) {
			this.labels = new ArrayList<Label>();
		}
		this.labels.add(label);
		Collections.sort(this.labels);
	}
	
	public void addLabels(List<Label> labels) {
		if (this.labels == null) {
			this.labels = new ArrayList<Label>();
		}
		this.labels.addAll(labels);
		Collections.sort(this.labels);
	}
	
	

}
