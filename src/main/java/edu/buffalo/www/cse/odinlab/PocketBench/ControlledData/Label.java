package edu.buffalo.www.cse.odinlab.PocketBench.ControlledData;

public class Label implements Comparable<Label>{
	
	private int labelID = -1;
	private String label = "";
	
	public Label(int labelID, String label) {
		super();
		this.labelID = labelID;
		this.label = label;
	}

	public int getLabelID() {
		return labelID;
	}

	public void setLabelID(int labelID) {
		this.labelID = labelID;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		result = prime * result + labelID;
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
		Label other = (Label) obj;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (labelID != other.labelID)
			return false;
		return true;
	}

	@Override
	public int compareTo(Label o) {
		return this.getLabel().compareTo(o.getLabel());
	}
	
	

}
