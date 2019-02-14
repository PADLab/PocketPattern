package edu.buffalo.www.odinlab.statlib;

/**
 * This class contains various combinatorial functions, 
 * such as computing (n choose k) or k!.
 * 
 * @author chaiml
 */
public class CombFuncs 
{

	// Cache of (ln of) factorials (see lnFactorial())
	private static double[] lnFactorialCache = null;
	private static int FACTORIAL_CACHE_SIZE = 30000;

	// Initializations
	static {
		
		// Initialize the lnFactorial() cache.
		lnFactorialCache = new double[FACTORIAL_CACHE_SIZE];
		for (int n=0; n < lnFactorialCache.length; n++) {
			lnFactorialCache[n] = HelperFuncs.gammln (n+1.0);
		}
		lnFactorialCache[0] = 0.0;
		lnFactorialCache[1] = 0.0;
		
	}
	
	
	/**
	 * Returns ln(n!).
	 * Implementation from Numerical Recipes (Chapter 6).
	 */
	public static double lnFactorial (int n)
	{
		if (n < 0) { throw new IllegalArgumentException(); }

		// Check whether value is in cache
		if (n < lnFactorialCache.length) {
			return (lnFactorialCache[n]);
		}
		
		return (HelperFuncs.gammln (n+1.0));
	}

	/**
	 * Returns ln(n choose k).
	 */
	public static double lnNchooseK (int n, int k) {
		return (lnBinomial(n,k));
	}
	public static double lnBinomial (int n, int k)
	{
		if ((n < 0) || (k < 0) || (k > n)) { throw new IllegalArgumentException(); }
		
		if ((k == n) || (k == 0)) {
			return (0.0);
		}
		return (lnFactorial(n) - lnFactorial(k) - lnFactorial(n-k));
	}

	/**
	 * Compute n choose k.
	 * See also lnNchooseK().
	 */
	public static int NchooseK (int n, int k)
	{
		return (int) ((Math.exp(lnNchooseK(n,k))) + 0.5);
	}

}
