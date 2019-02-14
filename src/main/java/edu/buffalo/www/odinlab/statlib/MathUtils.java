package edu.buffalo.www.odinlab.statlib;

/**
 * Misc mathematical functions.
 * 
 * @author chaiml
 */
public class MathUtils 
{

	/**
	 * Number of most significant bits to use from the mantissa for the approximation in log2approx().
	 */
	public final static int   LOG2APPROX_PRECISION_BITS = 14;

	/**
	 * Number of bits to use in each mantissa block for the approximation in expApprox().
	 */
	public final static int   EXPAPPROX_PRECISION_BITS = 5;

	// Masks for interpreting the binary representation of "double"
	private final static int  DOUBLE_MANTISSA_BITS = 52;
	private final static int  DOUBLE_EXPONENT_BITS = 11;
	private final static long DOUBLE_EXPONENT_MASK = (((long) 1) << DOUBLE_EXPONENT_BITS) - 1;
	private final static int  DOUBLE_EXPONENT_BIAS = 1023;

	// Constants for log2approx()
	private final static int  DOUBLE_MANTISSA_LOG2APPROX_SHIFT = DOUBLE_MANTISSA_BITS - LOG2APPROX_PRECISION_BITS;
	private final static long DOUBLE_MANTISSA_LOG2APPROX_MASK = (((long) 1) << LOG2APPROX_PRECISION_BITS) - 1;

	// Caches for log2approx() 
	private static double ms_log2MantissaCache[];  // holds the log of the significant bits we extract from the mantissa 
	private static double ms_log2ExpCache[];       // holds the exponent value minus the bias (1023) as a double (converting long to double is slow)

	// Constants for expApprox()
	private final static int  DOUBLE_MANTISSA_EXPAPPROX_SHIFT1  = DOUBLE_MANTISSA_BITS -    EXPAPPROX_PRECISION_BITS;
	private final static int  DOUBLE_MANTISSA_EXPAPPROX_SHIFT2  = DOUBLE_MANTISSA_BITS - (2*EXPAPPROX_PRECISION_BITS);
	private final static int  DOUBLE_MANTISSA_EXPAPPROX_SHIFT3  = DOUBLE_MANTISSA_BITS - (3*EXPAPPROX_PRECISION_BITS);
	private final static int  DOUBLE_MANTISSA_EXPAPPROX_SHIFT4  = DOUBLE_MANTISSA_BITS - (4*EXPAPPROX_PRECISION_BITS);
	private final static long DOUBLE_MANTISSA_EXPAPPROX_MASK    = (((long) 1) << EXPAPPROX_PRECISION_BITS) - 1;
	private final static long DOUBLE_MANTISSA_EXPAPPROX_NOTMASK = ~ DOUBLE_MANTISSA_EXPAPPROX_MASK;

	// Caches for expApprox() 
	private static double ms_expExpCache1[];   // holds exp(x), where x includes only the EXPAPPROX_PRECISION_BITS most significant bits in the mantissa 
	private static double ms_expExpCache2[];   // holds exp(x), where x includes only the 2nd block of EXPAPPROX_PRECISION_BITS most significant bits in the mantissa 
	private static double ms_expExpCache3[];   // holds exp(x), where x includes only the 3rd block of EXPAPPROX_PRECISION_BITS most significant bits in the mantissa 
	private static double ms_expExpCache4[];   // holds exp(x), where x includes only the 4th block of EXPAPPROX_PRECISION_BITS most significant bits in the mantissa 

	
	public final static double LN2   = Math.log(2.0);
	
	
	// Initializations
	static {

		// Initialize caches for log2approx()
		int lMantissaCache = 1 << LOG2APPROX_PRECISION_BITS;
		ms_log2MantissaCache = new double[lMantissaCache]; 
		double d1 = (double) lMantissaCache;
		double d2 = 1.0 + 1.0/(2.0*d1);
		for (int i=0; i < lMantissaCache; i++) {
			ms_log2MantissaCache[i] = Math.log(d2 + (i/d1)) / LN2;
		}
		
		int lExpCache = 1 << DOUBLE_EXPONENT_BITS;
		ms_log2ExpCache = new double[lExpCache]; 
		for (int i=0; i < lExpCache; i++) {
			ms_log2ExpCache[i] = (double) (i - DOUBLE_EXPONENT_BIAS);
		}

		// Initialize caches for expApprox()
		lExpCache = 1 << (1 + DOUBLE_EXPONENT_BITS + EXPAPPROX_PRECISION_BITS);
		ms_expExpCache1 = new double[lExpCache]; 
		ms_expExpCache2 = new double[lExpCache]; 
		ms_expExpCache3 = new double[lExpCache]; 
		ms_expExpCache4 = new double[lExpCache]; 
		for (int i=0; i < lExpCache; i++) {
			long bits = ((long) i) << DOUBLE_MANTISSA_EXPAPPROX_SHIFT1;
			ms_expExpCache1[i] = Math.exp(Double.longBitsToDouble(bits));
			long signExponent = (((long) i) >>> EXPAPPROX_PRECISION_BITS) << DOUBLE_MANTISSA_BITS;
			long mantissa = ((long) i) & DOUBLE_MANTISSA_EXPAPPROX_MASK;
			long mantissa2 = mantissa << DOUBLE_MANTISSA_EXPAPPROX_SHIFT2;
			long mantissa3 = mantissa << DOUBLE_MANTISSA_EXPAPPROX_SHIFT3;
			long mantissa4 = mantissa << DOUBLE_MANTISSA_EXPAPPROX_SHIFT4;
			double exp1 = Math.exp(Double.longBitsToDouble(signExponent));
			ms_expExpCache2[i] = Math.exp(Double.longBitsToDouble(signExponent ^ mantissa2)) / exp1;
			ms_expExpCache3[i] = Math.exp(Double.longBitsToDouble(signExponent ^ mantissa3)) / exp1;
			ms_expExpCache4[i] = Math.exp(Double.longBitsToDouble(signExponent ^ mantissa4)) / exp1;
		}

	}
	
	/**
	 * An efficient version of the logarithm function, approximates the value of log_2(x).
	 * This function is about 8-10 times faster than Math.log().
	 * The returned value approximates the real value within +-0.0001: 
	 *   | Math.log(x)/Math.log(2.0) - MathUtils.log2approx(x) | &lt; 0.0001 
	 *   (actually, the difference is bounded by log2(1+1/(1.5*2^LOG2APPROX_PRECISION_BITS)).
	 * Method: A (positive) double is represented as 1.M*2^E, where M is the mantissa and E is the exponent.
	 *         Thus, log2(1.M*2^E) = log2(1.M) + E
	 *         So, by looking at the binary representation of the double, we can easily extract E;
	 *         and, using a pre-computed cache, we get log2(1.M'), where M' is the most significant
	 *         bits of M.  
	 * @param x 
	 * @return an approximation of log_2(x); if x is non-positive or Nan, an arbitrary value is returned.
	 */
	public static double log2approx (double x) 
	{
		long bits = Double.doubleToRawLongBits(x);
		
		int mantissaMsbs = (int) ((bits >> DOUBLE_MANTISSA_LOG2APPROX_SHIFT) & DOUBLE_MANTISSA_LOG2APPROX_MASK);
		int exponent     = (int) ((bits >> DOUBLE_MANTISSA_BITS ) & DOUBLE_EXPONENT_MASK);

		return (ms_log2ExpCache[exponent] + ms_log2MantissaCache[mantissaMsbs]);
	}
	
	/**
	 * An efficient version of the exponential function, approximates the value of exp(x).
	 * This function is about 3-4 times faster than Math.exp().
	 * The returned value approximates the real value with ratio 1.001: 
	 *   1/1.001 &lt; Math.exp(x) / MathUtils.expApprox(x) &lt; 1.001 
	 * For values of x that aren't too large or too small, the approximation is better,
	 * e.g., if x is between -10 and 10, the ratio is 1.00001.
	 * Method: A double x is represented as S*1.M*2^E, where S is the sign bit, M is the mantissa and E is the exponent.
	 *         We break the mantissa into several blocks: M1 contains the 5 most significant bits of M,
	 *         M2 contains the next 5 significant bits, and so on. Thus: x=S*(1+M1+M2+...)*2^E.
	 *         So: exp(x) = exp(S*(1+M1)*2^E) * exp(S*M2*2^E) * ...
	 *         Each of the exp()'s in the above product is pre-computed and saved in an array of size 2^L,
	 *         where L=1+11+5 (1 for the sign bit, 11 for the exponent, and 5 bits for the mantissa block);
	 *         Using four blocks from the mantissa, i.e., a total of 20 bits from the mantissa, the maximum
	 *         ratio between the correct exp(x) and the approximated result we get is (for positive numbers): 
	 *         exp(2^-20 * 2^E) = exp(2^E-20) ; now, the largest number a double can hold is ~2^1024=~e^709,
	 *         so we can assume that x &lt; 709, which means that E &lt; 10. Therefore, the approximation
	 *         ratio is at most exp(2^-10)=~1.001. For small x, the ratio is better. 
	 * @param x 
	 * @return an approximation of exp(x)
	 */
	public static double expApprox (double x) 
	{
		long bits = Double.doubleToRawLongBits(x);
		long mantissa2 = (bits >> DOUBLE_MANTISSA_EXPAPPROX_SHIFT2) & DOUBLE_MANTISSA_EXPAPPROX_MASK;
		long mantissa3 = (bits >> DOUBLE_MANTISSA_EXPAPPROX_SHIFT3) & DOUBLE_MANTISSA_EXPAPPROX_MASK;
		long mantissa4 = (bits >> DOUBLE_MANTISSA_EXPAPPROX_SHIFT4) & DOUBLE_MANTISSA_EXPAPPROX_MASK;
		long bits1 = bits >>> DOUBLE_MANTISSA_EXPAPPROX_SHIFT1;
		long signExponent = bits1 & DOUBLE_MANTISSA_EXPAPPROX_NOTMASK;
		long bits2 = signExponent ^ mantissa2;
		long bits3 = signExponent ^ mantissa3;
		long bits4 = signExponent ^ mantissa4;
		return (ms_expExpCache1[(int) bits1] * 
				ms_expExpCache2[(int) bits2] * 
				ms_expExpCache3[(int) bits3] *
				ms_expExpCache4[(int) bits4]);
	}
	
	/**
	 * An efficient version of the power function, approximates the value of pow(a,b).
	 * This function receives the paramters log(a),b and approximates a^b.
	 * It uses the expApprox() method, so the approximation ratio is the same as there.
	 * There are 3 ways to use this function:
	 * (1) If a is fixed, then you could pre-compute loga=Math.log(a), and call powApprox(loga,b)
	 * (2) If a is variable, and you'd like a good approximation, then use: powApprox(Math.log(a),b)
	 * (3) If a is variable, and you don't require a good approximation, then use: powApprox(MathUtils.log2approx(a)*MathUtils.LN2,b)
	 * Method (1) is ~10 times faster than Math.pow(); (2) is ~2.5 times faster; (3) is ~5 times faster.
	 * Note that in method (3) the approximation ratio is e1*exp(+/-b*LN2*e2), where e1 and e2 are the approximations
	 * guaranteed by expApprox() and log2approx(), respectively.       
	 */ 
	public static double powApprox (double loga, double b) 
	{
		return (expApprox(b * loga));
	}
	
}
