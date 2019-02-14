package edu.buffalo.www.odinlab.statlib;

/**
 * This class contains static methods for computing the tail probabilities 
 * of various distribution functions: 
 *  normal, chi-square, poisson, binomial, hyper-geometric
 * These functions compute Prob{X>=x}, for a given value of x (and
 * given the distribution's parameters). The computed probability is
 * inaccurate if the result is very close to 1.0, due to the limited
 * accuracy of floating-point numbers. Thus, if p=Prob{X>=x} is very
 * close to 1.0 and you'd like to compute the complement tail probability,
 * i.e., Prob{X<x}, then you should use the 'C' function (do NOT
 * perform 1.0-p, since the result is highly inaccurate when p is very
 * close to 1.0). For example, to compute the tail probability of a
 * normal distribution, use probNormal() ; the complement probability
 * can be computed using probNormalC().
 *
 * @author chaiml
 */
public class TailProbs {

	// accuracy of hyper-geometric probability computation (see probHyperGeometric())
	private static final double TAILPROBS_HG_ACCURACY = 1000.0;


	/**
	 * Compute the tail probability Prob{X>=z} of a normal (Gaussian)
	 * distribution with expected=0, variance=1.
	 */
	public static double probNormal (double z)
	{
		return (0.5 * HelperFuncs.erffc(z / Math.sqrt(2.0)));
	}

	/**
	 * Compute the tail probability Prob{X<z} of a normal (Gaussian)
	 * distribution with expected=0, variance=1.
	 */
	public static double probNormalC (double z)
	{
		return (probNormal(-z));
	}
	
	/**
	 * 
	 * Computes z where Prob(x<z) = p and x is standard normal random variable.
	 * Taken from http://home.online.no/~pjacklam/notes/invnorm/
	 */
	public static double invNormal (double p)
	{
		if (p == 1.0)
			return 4.9;
		// Coefficients in rational approximations.
		double[] a = {
				-3.969683028665376e+01,
				2.209460984245205e+02,
				-2.759285104469687e+02,
				1.383577518672690e+02,
				-3.066479806614716e+01,
				2.506628277459239e+00};

		double[] b = {
				-5.447609879822406e+01,
				1.615858368580409e+02,
				-1.556989798598866e+02,
				6.680131188771972e+01,
				-1.328068155288572e+01};

		double[] c = {
				-7.784894002430293e-03,
				-3.223964580411365e-01,
				-2.400758277161838e+00,
				-2.549732539343734e+00,
				4.374664141464968e+00,
				2.938163982698783e+00};

		double[] d = {
				7.784695709041462e-03,
				3.224671290700398e-01,
				2.445134137142996e+00,
				3.754408661907416e+00};

		// Define break-points.

		double p_low  = 0.02425;
		double p_high = 1.0 - p_low;

		// Rational approximation for lower region.
		double x = 0.0;
		if ((0.0 <= p) && (p < p_low))
		{
		     double q = Math.sqrt(-2.0*Math.log(p));
		     x = (((((c[0]*q+c[1])*q+c[2])*q+c[3])*q+c[4])*q+c[5]) /
		            ((((d[0]*q+d[1])*q+d[2])*q+d[3])*q+1.0);
		}

		// Rational approximation for central region.

		if ((p_low <= p) && (p <= p_high))
		{
			double q = p - 0.5;
		    double r = q*q;
		    x = (((((a[0]*r+a[1])*r+a[2])*r+a[3])*r+a[4])*r+a[5])*q /
		           (((((b[0]*r+b[1])*r+b[2])*r+b[3])*r+b[4])*r+1.0);
		}

		// Rational approximation for upper region.

		if ((p_high < p) && (p <= 1))
		{
			double q = Math.sqrt(-2.0*Math.log(1.0-p));
		    x = -(((((c[0]*q+c[1])*q+c[2])*q+c[3])*q+c[4])*q+c[5]) /
		             ((((d[0]*q+d[1])*q+d[2])*q+d[3])*q+1.0);
		}
		
		// refinement
		// The relative error of the approximation has
		// absolute value less than 1.15?10-9.  One iteration of
		// Halley's rational method (third order) gives full machine precision.

		   
		double e = 0.5 * HelperFuncs.erffc(-x/Math.sqrt(2.0)) - p;
		double u = e * Math.sqrt(2.0*Math.PI) * Math.exp((x*x)/2.0);
		x = x - u/(1.0 + x*u/2.0);
		return x;

	}
	/**
	 * Compute the tail probability Prob{X>=x} of a chi-square
	 * distribution with df degrees of freedom.
	 * Implementation from Numerical Recipes (Chapter 6).
	 */
	public static double probChiSquare (double x, int df)
	{
		return (HelperFuncs.gammq (df/2.0, x/2.0));
	}
	
	/**
	 * Compute the tail probability Prob{X<x} of a chi-square
	 * distribution with df degrees of freedom.
	 * Implementation from Numerical Recipes (Chapter 6).
	 */
	public static double probChiSquareC (double x, int df)
	{
		return (HelperFuncs.gammp (df/2.0, x/2.0));
	}
	
	/**
	 * Compute the tail probability Prob{X>=k} of a poisson
	 * distribution with expected mean m.
	 * Implementation from Numerical Recipes (Chapter 6).
	 */
	public static double probPoisson (double k, double m)
	{
		if (k == 0.0) { return (1.0); }
		return (HelperFuncs.gammp (k, m));
	}

	/**
	 * Compute the tail probability Prob{X<k} of a poisson
	 * distribution with expected mean m.
	 * Implementation from Numerical Recipes (Chapter 6).
	 */
	public static double probPoissonC (double k, double m)
	{
		if (k == 0.0) { return (0.0); }
		return (HelperFuncs.gammq (k, m));
	}
	
	/**
	 * Compute the tail probability Prob{X>=k} of a binomial
	 * distribution with parameters p, n.
	 * Implementation from Numerical Recipes (Chapter 6).
	 */
	public static double probBinomial (double p, int n, int k)
	{
		return (HelperFuncs.betai ((double) k, n-k+1.0, p));
	}
	
	/**
	 * Compute the tail probability Prob{X<k} of a binomial
	 * distribution with parameters p, n.
	 * Implementation from Numerical Recipes (Chapter 6).
	 */
	public static double probBinomialC (double p, int n, int k)
	{
		return (HelperFuncs.betai (n-k+1.0, (double) k, 1.0-p));
	}
	
	/**
	 * Compute the tail probability Prob{X>=k} of a hyper-geometric
	 * distribution with parameters n, n1, m (i.e., the probability
	 * to receive k or more 1's when sampling (without repetitions)
	 * m items from a set of n items, out of which n1 are 1's).
	 * Implementation partially based on Amos Tanay's code.
	 *
	 * @param maxProb - If the probability is above the given maxProb parameter, we stop the computation, 
	 *     and return an arbitrary value between maxProb and 1. Thus, is the supplied maxProb is 
	 *     small, there's a good chance the method will run faster.
	 * @see probHyperGeometricC()
	 */
	public static double probHyperGeometric (int n, int n1, int m, int k)
	{
		return probHyperGeometric (n, n1, m, k, 1.0);
	}
	public static double probHyperGeometric (int n, int n1, int m, int k, double maxProb)
	{
		if ((n1 > n) || (m > n)) {
			throw new IllegalArgumentException("'n1' and 'm' may not be larger than 'n'");
		}
		if ((k > m) || (k > n1)) {
			return (0.0);
		}
		if (k <= 0) {
			return (1.0);
		}
		if (k == m) {
			// For efficiency, we handle this special case here
			double p1 = MathUtils.expApprox (CombFuncs.lnBinomial(n-m,n1-m) - CombFuncs.lnBinomial(n,n1));
			if (p1 > 1.0) { p1 = 1.0; }
			return (p1);
		}
		if (k == n1) {
			// For efficiency, we handle this special case here
			double p1 = MathUtils.expApprox (CombFuncs.lnBinomial(n-n1,m-n1) - CombFuncs.lnBinomial(n,m));
			if (p1 > 1.0) { p1 = 1.0; }
			return (p1);
		}
		
		double prob = 0.0;

		if ((m*n1)/n > k) {
			// k is relatively small, so it's more efficient to compute using the complement
			prob = 1.0 - probHyperGeometricC (n, n1, m, k);
			if (prob > 1.0) { prob = 1.0; }
			return (prob);
		}

		double bin_n_n1 = CombFuncs.lnBinomial(n,n1);
		int minI = k;
		if (minI < n1+m-n) { minI = n1+m-n; }
		int maxI = (m < n1) ? m : n1;

		boolean isLast = false;
		for (int i=minI; (! isLast) && (i <= maxI) && (prob < maxProb); i++) {
			double curr = MathUtils.expApprox (CombFuncs.lnBinomial(m,i) + CombFuncs.lnBinomial(n-m,n1-i) - bin_n_n1);
			double maxAdd = (maxI - i + 1.0) * curr;
			if (maxAdd < prob / TAILPROBS_HG_ACCURACY) {
				// All subsequent iterations will add at most prob/TAILPROBS_HGACCURACY, 
				// so we'll stop now (we give an upper bound on prob by adding maxAdd)
				prob += maxAdd;
				isLast = true;
			}
			else {
				prob += curr;
			}
		}

		if (prob > 1.0) { prob = 1.0; }

		return (prob);
	}

	/**
	 * Compute the tail probability Prob{X<k} of a hyper-geometric
	 * distribution with parameters n, n1, m (i.e., the probability
	 * to receive less than k 1's when sampling (without repetitions)
	 * m items from a set of n items, out of which n1 are 1's).
	 * Implementation partially based on Amos Tanay's code.
	 *
	 * @param maxProb - If the probability is above the given maxProb parameter, we stop the computation, 
	 *     and return an arbitrary value between maxProb and 1. Thus, is the supplied maxProb is 
	 *     small, there's a good chance the method will run faster.
	 * @see probHyperGeometric()
	 */
	public static double probHyperGeometricC (int n, int n1, int m, int k)
	{
		return probHyperGeometricC (n, n1, m, k, 1.0);
	}
	public static double probHyperGeometricC (int n, int n1, int m, int k, double maxProb)
	{
		if ((n1 > n) || (m > n)) {
			throw new IllegalArgumentException("'n1' and 'm' may not be larger than 'n'");
		}
		if ((k > m) || (k > n1)) {
			return (1.0);
		}
		if (k <= 0) {
			return (0.0);
		}
		if (k == 1) {
			// For efficiency, we handle this special case here
			double p1 = MathUtils.expApprox (CombFuncs.lnBinomial(n-m,n1) - CombFuncs.lnBinomial(n,n1));
			if (p1 > 1.0) { p1 = 1.0; }
			return (p1);
		}

		double prob = 0.0;

		if (m*n1/n <= k) {
			// k is relatively large, so it's more efficient to compute using the non-complement
			prob = 1.0 - probHyperGeometric (n, n1, m, k);
			if (prob > 1.0) { prob = 1.0; }
			return (prob);
		}

		double bin_n_n1 = CombFuncs.lnBinomial(n,n1);
		int minI = n1+m-n;
		if (minI < 0) { minI = 0; }
		int maxI = k - 1;
		if (maxI > m)  { maxI = m; }
		if (maxI > n1) { maxI = n1; }
		
		boolean isLast = false;
		for (int i=maxI; (! isLast) && (i >= minI) && (prob < maxProb); i--) {
			double curr = MathUtils.expApprox (CombFuncs.lnBinomial(m,i) + CombFuncs.lnBinomial(n-m,n1-i) - bin_n_n1);
			double maxAdd = (i - minI + 1.0) * curr;
			if (maxAdd < prob / TAILPROBS_HG_ACCURACY) {
				// All subsequent iterations will add at most prob/TAILPROBS_HGACCURACY, 
				// so we'll stop now (we give an upper bound on prob by adding maxAdd)
				prob += maxAdd;
				isLast = true;
			}
			else {
				prob += curr;
			} 
		}

		if (prob > 1.0) { prob = 1.0; }
		return (prob);
	}


}

