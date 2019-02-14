package edu.buffalo.www.odinlab.statlib;

/**
 * Various statistical functions.
 */
public class Stat 
{
	
	public static final double LOG2P = 0.9189385326; // (= 0.5*ln(2*Pi))

	public static final double SQRT2 = Math.sqrt(2.0);
	public static final double LN2   = Math.log(2.0);
	
	
	/**
	 * Bonferroni correction for multiple testing.
	 * @param prob the smallest p-value obtained from nTests independent tests
	 * @param nTests number of tests
	 * @return Bonferroni-corrected p-value
	 */

	public static double bonferroni(double prob, int nTests)
	{
		double p = prob * nTests;

		// If prob*nTests is very small, then it's a good approximation
		if (p < 0.0001) {
			return (p);
		}
		
		// If nTests is large, then we can use (1-prob)^nTests = exp(-x),
		// where x=prob*nTests
		if (nTests > 10) {
			return (1.0 - Math.exp(-p));
		}

		// If prob is (almost) 1 then return 1 (we can't differentiate between 1.0-prob and 0.0 in a double)
		double prob1 = 1.0 - prob;
		if (prob1 <= 0.0) {
			return (1.0);
		}
		
		// Exact computation
		// We use approximated methods for speed
		// (the approximation ratio is small, since nTests is small)
		double logProb1 = MathUtils.log2approx(prob1) * LN2;
		return (1.0 - MathUtils.powApprox (logProb1, nTests));
		
	}
	
	/**
	 * Hamming-like distance between two sets of characters.
	 * Counts the number of characters found only in one of the sets. 
	 * @param l1 the 1st set of characters
	 * @param l2 the 2nd set of characters
	 * @return the number of characters that are exclusive to l1 or l2,
	 * divided by the sum of the lengths of l1 and l2.
	 */
	public static double Hamming(String l1, String l2)
	{
		// now l1 holds all bases that are related to 
		// s1[i] and l2 the same for s2[i]
		// i.e if s1[i]='B' and s2[i]='V' then l1="CGT", l2="ACG"
		// the way we calc the distance is looping over l1 and check 

		// for its characters in l2, and the same for l2 (with l1).

		// every missing char is counted and the final distance is:

		// nMissing/(l1.len+l2.len)

		//System.out.println("l1="+l1+" ; l2="+l2);

		
		int nMissing = 0;

		for ( int j = 0; j < l1.length() ; j++ ){
			char c = l1.charAt(j);
			boolean found = false;

			for ( int k = 0 ; k < l2.length() ; k++ ){
				if (c==l2.charAt(k)){

					found = true;
					break;
					}

				}

			if (!found){
				nMissing++;

				}
			}

			

		for ( int j = 0; j < l2.length() ; j++ ){
			char c = l2.charAt(j);
			boolean found = false;

			for ( int k = 0 ; k < l1.length() ; k++ ){
				if (c==l1.charAt(k)){

					found = true;
					break;
					}

				}
			if (!found){

				nMissing++;

				}
			}
		//System.out.println("nMissing="+nMissing);

		return (nMissing+0.0)/(l1.length()+l2.length());

	}
		
					

    /**
     * Compute the Pearson correlation coeffcient - r(x,y)
     * @param x first variable distribution
     * @param y second variable distribution
     * @return r(x,y)
     */
	public static double pearsonCorrCoeff(double[] x, double[] y)
	{
		if (x == null || y == null)
			throw new NullPointerException();

		if (x.length != y.length)
			throw new IllegalArgumentException("x and y differ in their length");
		
		int nVals = x.length;
		double nValsD = (double) nVals;
		double avgX = 0.0;
		double avgY = 0.0;
		
		for (int i = 0; i < nVals; i++)
		{
			avgX += x[i];
			avgY += y[i];
		}
		avgX /= nValsD;
		avgY /= nValsD;
		
		return (pearsonCorrCoeff(x, y, avgX, avgY));
	}
	

    /**
     * Compute the Pearson correlation coeffcient - r(x,y),
     * given E(x) and E(y).
     * @param x first variable distribution
     * @param y second variable distribution
     * @param ex the average of x
     * @param ey the average of y
     * @return r(x,y)
     */

	public static double pearsonCorrCoeff(double[] x, double[] y, double ex, double ey)
	{
		if (x == null || y == null)
			throw new NullPointerException();

		if (x.length != y.length)
			throw new IllegalArgumentException("x and y differ in their length");
		
		int nVals = x.length;
		double numerator = 0.0;
		double denominatorX = 0.0;
		double denominatorY = 0.0;
		
		double tmpX;
		double tmpY;
		
		for (int i = 0; i < nVals; i++)
		{
			tmpX = x[i] - ex;
			tmpY = y[i] - ey;
			numerator += tmpX*tmpY;
			denominatorX += tmpX*tmpX;
			denominatorY += tmpY*tmpY;
		}
		
		if (denominatorX == 0.0 || denominatorY == 0.0)
		{
			return 0.0;
		}
		
		return (numerator / Math.sqrt(denominatorX*denominatorY));
	}
	
	/**
	 * Compute the Euclidean distance between two vectors.
	 * @param x first vector.
	 * @param y second vector. Must have the same length as x
	 * @return the euclidan distance between vec x and y
	 */
	public static double euclidianDist(double[] x, double[] y)
	{
		if (x == null || y == null)
			throw new NullPointerException();

		if (x.length != y.length)
			throw new IllegalArgumentException("x and y differ in their length");
		
		double res = 0.0;
		for (int i = 0; i < x.length; i++)
		{
			double tmp = x[i] - y[i];
			res += (tmp*tmp);
		}
		return Math.sqrt(res);
	}
	


	/**
	 * Combine p-values using the weighted Z-transform.
	 * @param p a set of p-values
	 * @param weights the weights of the variables
	 * @param nVars number of variables
	 * @return the combined p-value. It is computed by using the z-transform method.
	 */
	public static double combinePvals(double[] p, double[] weights, int nVars)
	{
		if (nVars == 1) {
			return p[0];
		}
			
		double zw = 0.0;
		double d = 0.0;
		for (int i = 0; i < nVars; i++)
		{
			if(p[i] > 1.0E-310)
			{
				zw += weights[i]*TailProbs.invNormal(p[i]);
			}
			else
			{
				zw += weights[i]*TailProbs.invNormal(1.0E-310);
			}
			
			d += weights[i]*weights[i];
		}
		zw /= Math.sqrt(d);
		return (TailProbs.probNormalC(zw));
	}
	
	/**
	 * Combine p-values using the Z-transform.
	 * @param p a set of p-values
	 * @param nVars number of variables
	 * @return the combined p-value. It is computed by using the z-transform method.
	 */
	public static double combinePvals(double[] p, int nVars)
	{
		if (nVars == 1) {
			return p[0];
		}
		double zw = 0.0;
		
		for (int i = 0; i < nVars; i++)
		{
			if(p[i] > 1.0E-310)
			{
				zw += TailProbs.invNormal(p[i]);
			}
			else
			{
				zw += TailProbs.invNormal(1.0E-310);
			}
		}
		zw /= Math.sqrt(nVars);
		return (TailProbs.probNormalC(zw));
	}
	
	/**
	 * Combine p-values using Fisher's combined probability test.
	 * @param p is the result of multiplying nVars p-values
	 * @return the combined p-value. It is computed by using the Fisher method.
	 */	
	public static double combinePvals(double p, int nVars)
	{
		// using the formula
		//p * sum(1/i! * (ln(1/p))^i)[i=0,1,...,nVars-1]
		double sum = 0;
		for ( int i = 0 ;i < nVars ; i++ ){
			sum += 1.0/Math.exp(CombFuncs.lnFactorial(i)) * (Math.pow(Math.log(1.0/p),i));
			}
		return p*sum;
	}
	
	/**
	 * Combine weighted p-values using Fisher's combined probability test.
	 * @param p is the result of multiplying nVars p-value^weight
	 * @param nVars number of p-values that were multiplied
	 * @param sortedWeights the weights that were used as the power
	 * 		  of the p-values sorted in descending order.
	 * 		  The weights must be > 0 and not equal.
	 * @return the combined p-value.
	 */

	public static double combinePvals(double p, int nVars, double[] sortedWeights)
	{
		//TODO handle numerical problems
		// using the formula
		// sum([p^(1/Ci)*Ci^(nVars - 1)]/[product(Ci - Cj)])
		//    i=0,2,...,nVars - 1		     j != i	
		//    Ci = sortedWeights[i]
		double res = 0.0;
		double lnd = 0.0;
		double lnn =0.0;
		int pow = nVars - 1;
		double negPos = 1.0;
		for (int i = 0; i < nVars; i++)
		{
			lnd = 0.0;
			lnn = 0.0;
			for (int j = 0; j < nVars; j++)
			{
				if (j != i)
				{

					if (sortedWeights[i] != sortedWeights[j])
					{
						lnd  += Math.log(Math.abs(sortedWeights[i] - sortedWeights[j])) ;
					}
					else
					{
						System.out.println("combinePvals: equal weights");
					}
				}
			}
			
			lnn = pow*Math.log(sortedWeights[i]) + (1.0/sortedWeights[i])*Math.log(p);
			res += negPos*Math.pow(Math.E, lnn - lnd);
			negPos *= -1.0;
		}

		return res;
	}
	
	/**

	 * Copmute the binomial score for n0 0's and n1 1's,
	 * assuming the null hypothesis of equal probability (0.5) for 0 and 1.
	 * @return p-value

	 */
	public static double calcBinomialScore(int n0, int n1)
	{
		if (n0 == n1) {
			return (1.0);
		}
		
		int n = n0 + n1;

		int nm = Math.max(n0,n1);

		// Compute binomial prob, and correct for 2 multiple tests
		double pval = bonferroni(TailProbs.probBinomial(0.5, n, nm), 2);
		
		return (pval);

	}


	/**

	 * Compute the KL divergence between two vectors.

	 * @param x first vector.

	 * @param y second vector. Must have the same length as x

	 * @return the average KL Divergence between vec x and y

	 */

	public static double klDivergence(double[] x, double[] y) {

		if (x == null || y == null)

			throw new NullPointerException();



		if (x.length != y.length)

			throw new IllegalArgumentException("x and y differ in their length");

		

		double res = 0.0;

		final double LAPLACE_CORRECTION = 0.00001;

		double sumX = 0.0;

		double sumY = 0.0;

		for (int i = 0; i < x.length; i++)

		{

			sumX += x[i];

			sumY += y[i];

		}

		sumX += LAPLACE_CORRECTION*x.length;

		sumY += LAPLACE_CORRECTION*y.length;

		for (int i = 0; i < x.length; i++)

		{

			double x_i = (x[i] + LAPLACE_CORRECTION)/sumX;

			double y_i = (y[i] + LAPLACE_CORRECTION)/sumY;

			double logXY = Math.log(x_i/y_i);///LN2;

			res += x_i*logXY + y_i*(-logXY);

		}

		return (res/2.0);

	}

		

	/**
	 * Multi-HG score.

	 * Compute the probability to receive a sum of 'i_sum' or more when

	 * drawing 'i_m' balls uniformly from a pool of 'i_n0' balls with

	 * value 0, 'i_n1' balls with value 1, ...

	 * Returns -1.0 in case the input parameters are illegal.

	 */

	public static double calcMultiHyperGeometricScore

		(int i_n0, int i_n1, int i_n2, int i_n3,

		 int i_m, int i_sum) 
	{

		int n = i_n0 + i_n1 + i_n2 + i_n3;



		if ((i_n0 < 0) || (i_n1 < 0) || (i_n2 < 0) || (i_n3 < 0) || (i_m < 0) || (i_m > n)) {

			return (-1.0);

		}

		if ((i_m == 0) && (i_sum > 0)) {

			return (0.0);

		}
		

		double prob = 0.0;

		int max, max1, max2, max3, min3;



		if (i_m > i_n3) 
		{

			max = i_n3*3;

			if ((i_m-i_n3) > i_n2) 
			{

				max += i_n2*2;

				if ((i_m-i_n3-i_n2) > i_n1)
				{

					max += i_n1;

				}

				else
				{

					max += (i_m-i_n3-i_n2);

				}

			}

			else 
			{

				max += 2*(i_m-i_n3);

			}

		}

		else
		{

			max = 3*i_m;

		}



		double logAll = CombFuncs.lnNchooseK (n,i_m);



		max1 = MIN3 (max, i_n1, i_m);

		for (int i1=0; i1 <= max1; i1++) {

			max2 = MIN3 ((max-i1) / 2, i_n2, i_m-i1);

			for (int i2=0; i2 <= max2; i2++) {

				min3 = Math.max (0, i_m-i1-i2-i_n0);

				max3 = MIN3((max-i1-2*i2)/3, i_n3, i_m-i1-i2);

				for (int i3=min3; i3 <= max3; i3++) {

					if ((i1+2*i2+3*i3) >= i_sum) {

						prob += Math.exp (CombFuncs.lnNchooseK (i_n1,i1) + CombFuncs.lnNchooseK (i_n2,i2) 

										  + CombFuncs.lnNchooseK (i_n3,i3) + CombFuncs.lnNchooseK 

											(i_n0,(i_m-i1-i2-i3)) - logAll);

					}

				}

			}

		}



		if (prob > 1.01) {

			System.err.println("WARNING: calcHyperGeometricScore prob="+prob+"> 1.0");
		}

		if (prob < -0.01) {

			System.err.println("WARNING: calcHyperGeometricScore prob="+prob+"< 0.0");
		}



		if (prob > 1.0) {

			prob = 1.0;
		}

		else if (prob < 0.0) {

			prob = 0.0;
		}



		return (prob);

	}


	
	

	public static double MIN3(double a, double b, double c)
	{

		return (a < b ? Math.min(a,c) : Math.min(b,c));

	}

	public static int MIN3(int a, int b, int c)
	{

		return (a < b ? Math.min(a,c) : Math.min(b,c));

	}

}