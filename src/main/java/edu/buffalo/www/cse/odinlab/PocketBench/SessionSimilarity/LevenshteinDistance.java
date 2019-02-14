package edu.buffalo.www.cse.odinlab.PocketBench.SessionSimilarity;

import java.util.List;

/**
 * This class is the implementation of Levenshtein Distance to find list similarity
 * @author Gökhan Kul
 */
public class LevenshteinDistance {

    /**
     * This method returns the similarity between two texts
     *
     * @param s1 first text
     * @param s2 second text
     * @return the similarity ratio between two texts (from 0 -lowest- to 1 -highest-)
     */
    public double Similarity(List<Integer> s1, List<Integer> s2) {
        if (s1.size() < s2.size()) { // s1 should always be bigger
            List<Integer> swap = s1;
            s1 = s2;
            s2 = swap;
        }
        int bigLen = s1.size();
        if (bigLen == 0) {
            return 1.0; /* both strings are zero length */ }
        return (bigLen - ComputeEditDistance(s1, s2)) / (double) bigLen;
    }

    /**
     * This method computes the distance between two texts
     *
     * @param s1 first text
     * @param s2 second text
     * @return the distance between two texts
     */
    private int ComputeEditDistance(List<Integer> s1, List<Integer> s2) {

        int[] costs = new int[s2.size() + 1];
        for (int i = 0; i <= s1.size(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.size(); j++) {
                if (i == 0) {
                    costs[j] = j;
                } else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (!s1.subList(i - 1, i).equals(s2.subList(j - 1, j))) {
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        }
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0) {
                costs[s2.size()] = lastValue;
            }
        }
        return costs[s2.size()];
    }

}