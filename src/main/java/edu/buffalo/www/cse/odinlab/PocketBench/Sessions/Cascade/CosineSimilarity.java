package edu.buffalo.www.cse.odinlab.PocketBench.Sessions.Cascade;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class CosineSimilarity {
	//TODO Write tests which check ordering of elements from Map to Double. Order should be retained in Map, Dimensions and Double array
    public Double getCosineSimilarity(Set<String> d1,Set<String> d2)
    {
    	double[] docVector1 = null;
    	double[] docVector2 = null;    	
    	Set<String> dimensions = new HashSet<String>(d1);
    	dimensions.addAll(d2);
    	Map<String, Double> docHash1 = dimensions.stream().collect(Collectors.toMap(o -> o, o -> 0.0));
    	Map<String, Double> docHash2 = dimensions.stream().collect(Collectors.toMap(o -> o, o -> 0.0));
    	d1.forEach(d -> { // This could be a performance bottleneck. Search for a better way to do it.
    		Double count = docHash1.get(d);
    		if(count != null){
    			docHash1.put(d, count + 1);
    		} else{
    			System.err.println("Weird stuff went down here! Map1 intialization didn't work!!");
    			return;
    		}
    	});
    	docVector1 = docHash1.values().stream().mapToDouble(Double::doubleValue).toArray();
//    	System.out.println(docHash1);
//    	System.out.println(docHash1.values());
    	d2.forEach(d -> {
    		Double count = docHash2.get(d);
    		if(count != null){
    			docHash2.put(d, count + 1);
    		} else{
    			System.err.println("Weird stuff went down here! Map2 intialization didn't work!!");
    			return;
    		}
    	});
    	docVector2 = docHash2.values().stream().mapToDouble(Double::doubleValue).toArray();
//    	System.out.println(docHash2);
//    	System.out.println(docHash2.values());
    	
//    	return null;
    	return this._getCosineSimilarityValue(docVector1, docVector2);
    }
	/**
     * Method to calculate cosine similarity between two documents.
     * Borrowed from http://computergodzilla.blogspot.com/2013/07/how-to-calculate-tf-idf-of-document.html
     * @param docVector1 : document vector 1 (a)
     * @param docVector2 : document vector 2 (b)
     * @return 
     */
    public Double _getCosineSimilarityValue(double[] docVector1, double[] docVector2) {
    	if(docVector1 == null || docVector2 == null || docVector1.length < 1 || docVector2.length < 1)
    		return null;
        double dotProduct = 0.0;
        double magnitude1 = 0.0;
        double magnitude2 = 0.0;
        double cosineSimilarity = 0.0;

        for (int i = 0; i < docVector1.length; i++) //docVector1 and docVector2 must be of same length
        {
            dotProduct += docVector1[i] * docVector2[i];  //a.b
            magnitude1 += Math.pow(docVector1[i], 2);  //(a^2)
            magnitude2 += Math.pow(docVector2[i], 2); //(b^2)
        }

        magnitude1 = Math.sqrt(magnitude1);//sqrt(a^2)
        magnitude2 = Math.sqrt(magnitude2);//sqrt(b^2)

        if (magnitude1 != 0.0 | magnitude2 != 0.0) {
            cosineSimilarity = dotProduct / (magnitude1 * magnitude2);
        } else {
            return 0.0;
        }
        return cosineSimilarity;
    }
}
