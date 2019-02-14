package edu.buffalo.www.cse.odinlab.PocketBench.Sessions.Cascade;

import java.util.ArrayList;
import java.util.TreeMap;

public class TermList {
	
	private ArrayList<String> termList = null;
	private String query = null;
	private TreeMap<String, Integer> ngrams = null;
	
//	public TermList (String query, ArrayList<TermType> termList) {
	public TermList (String query, ArrayList<String> termList) {
		this.termList = termList;
		this.query = query;
	} 
	
	public TermList(String query) {
		this.query = query;
//		this.termList = new ArrayList<TermType>();
		this.termList = new ArrayList<String>();
	}
	
	public void addTerm(String term) {
		try {
			this.termList.add(term);
		} catch (Exception ex) {
			if (this.termList == null) {
//				this.termList = new ArrayList<TermType>();
				this.termList = new ArrayList<String>();
			}
			this.termList.add(term);
		}
	}
	
//	public void addTerm(TermType term) {
//		try {
//			this.termList.add(term);
//		} catch (Exception ex) {
//			if (this.termList == null) {
//				this.termList = new ArrayList<TermType>();
//			}
//			this.termList.add(term);
//		}
//	}
	
//	public void addTerm(ArrayList<TermType> term) {
//		try {
//			this.termList.addAll(term);
//		} catch (Exception ex) {
//			if (this.termList == null) {
//				this.termList = new ArrayList<TermType>();
//			}
//			this.termList.addAll(term);
//		}
//	}
	
	public String query() {
		return query;
	}
	
//	public ArrayList<TermType> get() {
	public ArrayList<String> get() {
		return termList;
	}
	
//	public TermType getLast() {
	public String getLast() {
		if (termList == null || termList.size() == 0) {
			return null;
		} else {
			return termList.get(termList.size() - 1);
		}
	}
	
	public String toString() {
		return "Query: " + query + "\n" + "TermList: "+termList.toString() + "\n";
	}
	
	public void addNGram(String ngram) {
		if (this.ngrams == null) {
			this.ngrams = new TreeMap<String, Integer>();
		}
		
		Integer temp = ngrams.get(ngram);
		if (temp == null)
			ngrams.put(ngram, 1);
		else {
			ngrams.put(ngram, temp + 1);
		}
	}
	
	public TreeMap<String, Integer> getNGrams() {
		return ngrams;
	}
}

