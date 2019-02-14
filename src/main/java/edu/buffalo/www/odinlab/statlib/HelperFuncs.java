package edu.buffalo.www.odinlab.statlib;

/**
 * Helper functions for statistical computations.
 * Used by TailProbs.
 * Many of the implementations here are borrowed from Numerical Recipes (Ch. 6).
 * 
 * @author chaiml
 */
public class HelperFuncs {

	// max. number of iterations
	private static final int HELPERFUNCS_ITMAX = 200;
	
	// relative accuracy
	private static final double HELPERFUNCS_EPS = 3.0e-7;
	
	// number near smallest representable float
	private static final double HELPERFUNCS_FPMIN = 1.0e-30;   
	  
	/**
	 * Returns the incomplete gamma function P(a,x).
	 * Implementation from Numerical Recipes (Chapter 6).
	 */
	public static double gammp (double a, double x)
	{
		if ((x < 0.0) || (a <= 0.0)) {
			throw new IllegalArgumentException();			
		}

		if (x < (a+1.0)) {  // use the series representation
			return (gser (a, x));
		}
		else {              // use the continued fraction representation
			return (1.0 - gcf (a, x));  // and take its complement
		}
	}

	/**
	 * Returns the incomplete gamma function Q(a,x)=1-P(a,x).
	 * Implementation from Numerical Recipes (Chapter 6).
	 */
	public static double gammq (double a, double x)
	{
		if ((x < 0.0) || (a <= 0.0)) {
			throw new IllegalArgumentException();			
		}

		if (x < (a+1.0)) {  // use the series representation
			return (1.0 - gser (a, x)); // and take its complement
		}
		else {              // use the continued fraction representation
			return (gcf (a, x));
		}
	}

	/**
	 * Returns the incomplete gamma function P(a,x) evaluated by its
	 * series representation. 
	 * Implementation from Numerical Recipes (Chapter 6).
	 */
	public static double gser (double a, double x)
	{
		double gln = gammln (a);

		if (x < 0.0) {
			throw new IllegalArgumentException();			
		}
		else if (x == 0.0) {
			return (0.0);
		}
		else {
			double ap = a;
			double del = 1.0 / a;
			double sum = del;
			for (int n=1; n <= HELPERFUNCS_ITMAX; n++) {
				ap += 1.0;
				del = del * x/ap;
				sum += del;
				if (Math.abs(del) < (Math.abs(sum)*HELPERFUNCS_EPS)) {
					return (sum * Math.exp(-x + a*Math.log(x) - gln));
				}
			}
			throw new ArithmeticException("'a' is too large, 'ITMAX' is too small");
		}
	}

	/**
	 * Returns the incomplete gamma function Q(a,x) evaluated by its
	 * continued fraction representation. 
	 * Implementation from Numerical Recipes (Chapter 6).
	 */
	public static double gcf (double a, double x)
	{
		double gln = gammln (a);

		double b = x + 1.0 - a;
		double c = 1.0 / HELPERFUNCS_FPMIN;
		double d = 1.0 / b;
		double h = d;

		boolean isLast = false;
		for (int i=1; (! isLast) && (i <= HELPERFUNCS_ITMAX); i++) {
			double an = -i * (i - a);
			b += 2.0;
			d = an*d + b;
			if (Math.abs(d) < HELPERFUNCS_FPMIN) { d = HELPERFUNCS_FPMIN; }
			c = b + an/c;
			if (Math.abs(c) < HELPERFUNCS_FPMIN) { c = HELPERFUNCS_FPMIN; }
			d = 1.0 / d;
			double del = d*c;
			h = h * del;
			if (Math.abs(del-1.0) < HELPERFUNCS_EPS) {
				isLast = true;
			}
		}

		if (! isLast) {
			throw new ArithmeticException("'a' is too large, 'ITMAX' is too small");
		}
		
		return (Math.exp(-x + a*Math.log(x) - gln) * h);
	}

	/**
	 * Returns the value ln(Gamma(xx)) for xx>0.
	 * Implementation from Numerical Recipes (Chapter 6).
	 */
	public static double gammln (double xx)
	{
		double cof[] = { 76.18009172947146 , -86.50532032941677   , 24.01409824083091,
						 -1.231739572450155, 0.1208650973866179e-2, -0.5395239384953e-5 };
		double x = xx;
		double y = x;
		double tmp = x + 5.5;
		tmp = tmp - (x+0.5)*Math.log(tmp);
		double ser = 1.000000000190015;
		
		for(int j=0; j <= 5; j++) {
			y += 1.0;
			ser += cof[j]/y;
		}
		
		return (-tmp + Math.log(2.5066282746310005 * ser/x));
	}

	/**
	 * Returns the incomplete beta function Ix(a,b).
	 * Implementation from Numerical Recipes (Chapter 6).
	 */	
	public static double betai (double a, double b, double x)
	{
		if ((x < 0.0-HELPERFUNCS_EPS) || (x > 1.0+HELPERFUNCS_EPS)) {
			throw new IllegalArgumentException();			
		}

		double bt;
		if ((x <= 0.0) || (x >= 1.0)) {
			bt = 0.0;
		}
		else {
			// Factors in front of the continued fraction
			bt = Math.exp (gammln(a+b) - gammln(a) - gammln(b) + 
						   a*Math.log(x) + b*Math.log(1.0-x));
		}

		if (x < (a+1.0)/(a+b+2.0)) {
			// Use continued fraction directly
			return (bt * betacf(a,b,x) / a);
		}
		else {
			// Use continued fraction after making the symmetry transformation
			return (1.0 - (bt * betacf(b,a,1.0-x) / b));
		}
	}

	/**
	 * Used by betai(): Evaluates continued fraction for incomplete beta
	 * function by modified Lentz's method.
	 * Implementation from Numerical Recipes (Chapter 6).
	 */
	public static double betacf (double a, double b, double x)
	{
		// These q's will be used in factors that occur in the coefficients
		double qab = a + b;
		double qap = a + 1.0;
		double qam = a - 1.0;

		// First step of Lentz's method
		double c = 1.0;
		double d = 1.0 - (qab * x / qap);
		if (Math.abs(d) < HELPERFUNCS_FPMIN) { d = HELPERFUNCS_FPMIN; }
		d = 1.0 / d;
		double h = d;

		boolean isLast = false;
		for (int m=1; (! isLast) && (m <= HELPERFUNCS_ITMAX); m++) {
			double m2 = 2*m;
			double aa = m*(b-m)*x / ((qam+m2)*(a+m2));
			d = 1.0 + aa*d;   // One step (the even one) of the recurrence
			if (Math.abs(d) < HELPERFUNCS_FPMIN) { d = HELPERFUNCS_FPMIN; }
			c = 1.0 + aa/c;
			if (Math.abs(c) < HELPERFUNCS_FPMIN) { c = HELPERFUNCS_FPMIN; }
			d = 1.0/d;
			h = h*d*c;
			aa = -(a+m)*(qab+m)*x / ((a+m2)*(qap+m2));
			d = 1.0 + aa*d;   // Next step of the recurrence (the odd one)
			if (Math.abs(d) < HELPERFUNCS_FPMIN) { d = HELPERFUNCS_FPMIN; }
			c = 1.0 + aa/c;
			if (Math.abs(c) < HELPERFUNCS_FPMIN) { c = HELPERFUNCS_FPMIN; }
			d = 1.0/d;
			double del = d*c;
			h = h*del;
			if (Math.abs(del-1.0) < HELPERFUNCS_EPS) {
				isLast = true; // Are we done?
			}
		}

		if (! isLast)  {
			throw new ArithmeticException("'a' or 'b' are too large, or 'ITMAX' is too small");
		}

		return (h);
	}

	/**
	 * Returns the error-function erf(x).
	 * Implementation from Numerical Recipes (Chapter 6).
	 */
	public static double erff (double x)
	{
		double y = gammp(0.5,x*x);

		if (x < 0.0) { return (-y); }
		else         { return (y);  }
	}

	/**
	 * Returns the complementary error-function erfc(x)=1-erf(x).
	 * Implementation from Numerical Recipes (Chapter 6).
	 */
	public static double erffc (double x)
	{
		if (x < 0.0) { return (1.0 + gammp(0.5,x*x)); }
		else         { return (gammq(0.5,x*x)); }
	}

}

