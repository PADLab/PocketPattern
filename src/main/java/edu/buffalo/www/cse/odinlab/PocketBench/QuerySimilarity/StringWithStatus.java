package edu.buffalo.www.cse.odinlab.PocketBench.QuerySimilarity;



public class StringWithStatus {
	private ParsabilityStatus status = ParsabilityStatus.EMPTY_STRING;
	private String statement = null;

	public StringWithStatus(ParsabilityStatus status, String statement) {
		this.statement = statement;
		this.status = status;
	}

	public ParsabilityStatus getStatus() {
		return status;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((statement == null) ? 0 : statement.hashCode());
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
		StringWithStatus other = (StringWithStatus) obj;
		if (statement == null) {
			if (other.statement != null)
				return false;
		} else if (!statement.equals(other.statement))
			return false;
		return true;
	}

	public void setStatus(ParsabilityStatus status) {
		this.status = status;
	}

	public String getStatement() {
		return statement;
	}

	public void setStatement(String statement) {
		this.statement = statement;
	}
}
