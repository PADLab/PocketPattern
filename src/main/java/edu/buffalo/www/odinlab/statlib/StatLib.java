package edu.buffalo.www.odinlab.statlib;

public class StatLib {
	
	public static final double LAPLACE_CORRECTION = 0.00001;
	
    /**
     * Returns the Kullback-Leibler divergence of the second
     * specified multinomial relative to the first.
     *
     * <p>The K-L divergence of a multinomial <code>q</code> relative
     * to a multinomial <code>p</code>, both with <code>n</code>
     * outcomes, is:
     *
     * <blockquote><pre>
     * D<sub><sub>KL</sub></sub>(p||q)
     * = <big><big><big>&Sigma;</big></big></big><sub><sub>i &lt; n</sub></sub>  p(i) log<sub>2</sub> (p(i) / q(i))</pre></blockquote>
     *
     * The value is guaranteed to be non-negative, and will be 0.0
     * only if the two distributions are identicial.  If any outcome
     * has zero probability in <code>q</code> and non-zero probability
     * in <code>p</code>, the result is infinite.
     *
     * <p>KL divergence is not symmetric.  That is, there are
     * <code>p</code> and <code>q</code> such that
     * <code>D<sub><sub>KL</sub></sub>(p||q) !=
     * D<sub><sub>KL</sub></sub>(q||p)</code>.  See {@link
     * #symmetrizedKlDivergence(double[],double[])} and {@link
     * #jsDivergence(double[],double[])} for symmetric variants.
     *
     * <p>KL divergence is equivalent to conditional entropy, although
     * it is written in the opposite order.  If <code>H(p,q)</code> is
     * the joint entropy of the distributions <code>p</code> and
     * <code>q</code>, and <code>H(p)</code> is the entropy of
     * <code>p</code>, then:
     *
     * <blockquote><pre>
     * D<sub><sub>KL</sub></sub>(p||q) = H(p,q) - H(p)</pre></blockquote>
     *
     * @param p First multinomial distribution.
     * @param q Second multinomial distribution.
     * @throws IllegalArgumentException If the distributions are not
     * the same length or have entries less than zero or greater than
     * 1.
     */
    public static double klDivergence(double[] p, double[] q) {
        verifyDivergenceArgs(p,q);
        double divergence = 0.0;
        int len = p.length;
        for (int i = 0; i < len; ++i) {
            if (p[i] > 0.0 && p[i] != q[i])
                divergence += p[i] * MatLib.log2(p[i] / q[i]);
        }
        return divergence;
    }
    
    /**
     * Returns the Kullback-Leibler divergence of the second
     * specified multinomial relative to the first.
     *
     * <p>The K-L divergence of a multinomial <code>q</code> relative
     * to a multinomial <code>p</code>, both with <code>n</code>
     * outcomes, is:
     *
     * <blockquote><pre>
     * D<sub><sub>KL</sub></sub>(p||q)
     * = <big><big><big>&Sigma;</big></big></big><sub><sub>i &lt; n</sub></sub>  p(i) log<sub>2</sub> (p(i) / q(i))</pre></blockquote>
     *
     * The value is guaranteed to be non-negative, and will be 0.0
     * only if the two distributions are identicial.  If any outcome
     * has zero probability in <code>q</code> and non-zero probability
     * in <code>p</code>, the result is infinite.
     *
     * <p>KL divergence is not symmetric.  That is, there are
     * <code>p</code> and <code>q</code> such that
     * <code>D<sub><sub>KL</sub></sub>(p||q) !=
     * D<sub><sub>KL</sub></sub>(q||p)</code>.  See {@link
     * #symmetrizedKlDivergence(double[],double[])} and {@link
     * #jsDivergence(double[],double[])} for symmetric variants.
     *
     * <p>KL divergence is equivalent to conditional entropy, although
     * it is written in the opposite order.  If <code>H(p,q)</code> is
     * the joint entropy of the distributions <code>p</code> and
     * <code>q</code>, and <code>H(p)</code> is the entropy of
     * <code>p</code>, then:
     *
     * <blockquote><pre>
     * D<sub><sub>KL</sub></sub>(p||q) = H(p,q) - H(p)</pre></blockquote>
     *
     * @param p First multinomial distribution.
     * @param q Second multinomial distribution.
     * @throws IllegalArgumentException If the distributions are not
     * the same length or have entries less than zero or greater than
     * 1.
     */
    public static double klDivergenceWithLaplaceCorrection(double[] p, double[] q) {
        verifyDivergenceArgs(p,q);
        double divergence = 0.0;
        int len = p.length;
        for (int i = 0; i < len; ++i) {
        	p[i] += LAPLACE_CORRECTION;
        	q[i] += LAPLACE_CORRECTION;
            if (p[i] > 0.0 && p[i] != q[i])
                divergence += p[i] * MatLib.log2(p[i] / q[i]);
        }
        return divergence;
    }
    
    /**
     * Bhattacharyya distance between two normalized histograms.
     * @param p Normalized histogram.
     * @param q Normalized histogram.
     * @return The Bhattacharyya distance between the two histograms.
     */
    public static double Bhattacharyya(double[] p, double[] q){
    		verifyDivergenceArgs(p,q);
        int bins = p.length; // histogram bins
        double b = 0; // Bhattacharyya's coefficient

        for (int i = 0; i < bins; i++)
            b += Math.sqrt(p[i]) * Math.sqrt(q[i]);

        // Bhattacharyya similarity between the two distributions
        return b;
    }
    
    static void verifyDivergenceArgs(double[] p, double[] q) {
        if (p.length != q.length) {
            String msg = "Input distributions must have same length."
                + " Found p.length=" + p.length
                + " q.length=" + q.length;
            throw new IllegalArgumentException(msg);
        }
        int len = p.length;
        for (int i = 0; i < len; ++i) {
            if (p[i] < 0.0
                || p[i] > 1.0
                || Double.isNaN(p[i])
                || Double.isInfinite(p[i])) {
                String msg = "p[i] must be between 0.0 and 1.0 inclusive."
                    + " found p[" + i + "]=" + p[i];
                throw new IllegalArgumentException(msg);
            }
            if (q[i] < 0.0
                || q[i] > 1.0
                || Double.isNaN(q[i])
                || Double.isInfinite(q[i])) {
                String msg = "q[i] must be between 0.0 and 1.0 inclusive."
                    + " found q[" + i + "] =" + q[i];
                throw new IllegalArgumentException(msg);
            }
        }
    }
    
    /**
     * Gets the Correlation distance between two points.
     * @param p A point in space.
     * @param q A point in space.
     * @return The Correlation distance between x and y.
     */
    public static double correlation(double[] p, double[] q){
        
    		verifyDivergenceArgs(p,q);
    	
        double x = 0;
        double y = 0;
        
        for (int i = 0; i < p.length; i++) {
            x += -p[i];
            y += -q[i];
        }
        
        x /= p.length;
        y /= q.length;
        
        double num = 0;
        double den1 = 0;
        double den2 = 0;
        for (int i = 0; i < p.length; i++)
        {
            num += (p[i] + x) * (q[i] + y);

            den1 += Math.abs(Math.pow(p[i] + x, 2));
            den2 += Math.abs(Math.pow(q[i] + x, 2));
        }

        return 1 - (num / (Math.sqrt(den1) * Math.sqrt(den2)));
        
    }
    
    /**
     * Return the Jenson-Shannon divergence between the specified
     * multinomial distributions.  The JS divergence is defined by
     *
     * <blockquote><pre>
     * D<sub><sub>JS</sub></sub>(p,q)
     * = ( D<sub><sub>KL</sub></sub>(p,m) + D<sub><sub>KL</sub></sub>(q,m) ) / 2</pre></blockquote>
     *

     * where <code>m</code> is defined as the balanced linear
     * interpolation (that is, the average) of <code>p</code> and
     * <code>q</code>:
     *
     * <pre><blockquote>
     * m[i] = (p[i] + q[i]) / 2</pre></blockquote>
     *
     * The JS divergence is non-zero, equal to zero only if <code>p</code>
     * and <code>q</code> are the same distribution, and symmetric.
     *
     * @param p First multinomial.
     * @param q Second multinomial.
     * @return The JS divergence between the multinomials.
     * @throws IllegalArgumentException If the distributions are not
     * the same length or have entries less than zero or greater than
     * 1.
     */
    public static double jsDivergence(double[] p, double[] q) {
        verifyDivergenceArgs(p,q);
        double[] m = new double[p.length];
        for (int i = 0; i < p.length; ++i)
            m[i] = (p[i] + q[i])/2.0;
        return (klDivergenceWithLaplaceCorrection(p,m) + klDivergenceWithLaplaceCorrection(q,m)) / 2.0;
    }

}
