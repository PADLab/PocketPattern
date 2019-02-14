package edu.buffalo.www.cse.odinlab.PocketBench.ControlledData;

import java.util.List;

public class LabeledActivity {
	
	private List<Label> labels = null;
	private List<ActivityData> activityData= null;
	
	public List<Label> getLabels() {
		return labels;
	}
	
	public void setLabels(List<Label> labels) {
		this.labels = labels;
	}
	
	public List<ActivityData> getActivityData() {
		return activityData;
	}
	
	public void setActivityData(List<ActivityData> activityData) {
		this.activityData = activityData;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((activityData == null) ? 0 : activityData.hashCode());
		result = prime * result + ((labels == null) ? 0 : labels.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LabeledActivity other = (LabeledActivity) obj;
		if (activityData == null) {
			if (other.activityData != null)
				return false;
		} else if (!activityData.equals(other.activityData))
			return false;
		if (labels == null) {
			if (other.labels != null)
				return false;
		} else if (!labels.equals(other.labels))
			return false;
		return true;
	}

}
