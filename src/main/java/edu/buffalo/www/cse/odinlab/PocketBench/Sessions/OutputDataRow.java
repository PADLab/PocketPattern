package edu.buffalo.www.cse.odinlab.PocketBench.Sessions;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OutputDataRow {
	String user;
	Long windowId;
	Double averageSimilarity;
	Double averageResponse; // Average Response Time
	Integer numberOfQueries;

	/**
	 * @param user
	 * @param windowId
	 * @param averageSimilarity
	 * @param averageResponse
	 * @param numberOfQueries
	 */
	public OutputDataRow(String user, Long windowId, Double averageSimilarity, Double averageResponse,
			int numberOfQueries) {
		this.user = user;
		this.windowId = windowId;
		this.averageSimilarity = averageSimilarity;
		this.averageResponse = averageResponse;
		this.numberOfQueries = numberOfQueries;
	}

	public String toCsvRow() {
		return Stream
				.of(user, windowId.toString(), averageSimilarity.toString(), averageResponse.toString(),
						numberOfQueries.toString())
				.map(value -> value.replaceAll("\"", "\"\""))
				.map(value -> Stream.of("\"", ",").anyMatch(value::contains) ? "\"" + value + "\"" : value)
				.collect(Collectors.joining(","));
	}
}
