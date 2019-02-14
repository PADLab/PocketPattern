package edu.buffalo.www.odinlab.statlib;

import edu.illinois.dais.ttr.Mathematics;

public class MatLib {

	/**
     * The natural logarithm of 2.
     */
    public static final double LN_2 = java.lang.Math.log(2.0);

    
    /**
     * Returns the log base 2 of the specivied value.
     *
     * @param x Value whose log is taken.
     * @return Log of specified value.
     */
    public static double log2(double x) {
        return naturalLogToBase2Log(java.lang.Math.log(x));
    }
    
    /**
     * Converts a natural logarithm to a base 2 logarithm.
     * That is, if the input is <code><i>x</i> = ln <i>z</i></code>, then
     * the return value is <code>log<sub>2</sub> <i>z</i></code>.
     * Recall that <code>log<sub>2</sub> <i>z</i> = ln <i>z</i> / ln 2.
     *
     * @param x Natural log of value.
     * @return Log base 2 of value.
     */
    public static double naturalLogToBase2Log(double x) {
        return x / LN_2;
    }
}
