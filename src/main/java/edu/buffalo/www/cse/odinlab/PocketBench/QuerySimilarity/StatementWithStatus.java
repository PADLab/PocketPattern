package edu.buffalo.www.cse.odinlab.PocketBench.QuerySimilarity;

import net.sf.jsqlparser.statement.Statement;

public class StatementWithStatus {
	
	private ParsabilityStatus status = ParsabilityStatus.EMPTY_STRING;
	private Statement statement = null;

	public StatementWithStatus(ParsabilityStatus status, Statement statement) {
		this.statement = statement;
		this.status = status;
	}

	public ParsabilityStatus getStatus() {
		return status;
	}

	public void setStatus(ParsabilityStatus status) {
		this.status = status;
	}

	public Statement getStatement() {
		return statement;
	}

	public void setStatement(Statement statement) {
		this.statement = statement;
	}
}
