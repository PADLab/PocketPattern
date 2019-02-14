package edu.buffalo.www.cse.odinlab.PocketBench.QuerySimilarity;

public enum ParsabilityStatus {
	PRAGMA,
	TRANSACTION_COMMAND,
	MAINTENANCE_QUERY,
	SCHEMA_ALTERATION_QUERY,
	PARSABLE_CRUD_QUERY,
	UNPARSABLE_QUERY,
	PARSER_CRASHED,
	EMPTY_STRING
}
