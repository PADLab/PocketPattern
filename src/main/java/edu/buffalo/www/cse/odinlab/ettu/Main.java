package edu.buffalo.www.cse.odinlab.ettu;

import edu.buffalo.www.odinlab.statlib.StatLib;

public class Main {

	public static void main(String[] args) {
		
		double[] p = new double[5];
		double[] q = new double[5];
		
		p[0] = 3.0 / 10.0;
		p[1] = 4.0 / 10.0;
		p[2] = 2.0 / 10.0;
		p[3] = 1.0 / 10.0;
		p[4] = 0.0 / 10.0;
		
		q[0] = 3.0 / 10.0;
		q[1] = 3.0 / 10.0;
		q[2] = 3.0 / 10.0;
		q[3] = 0.0 / 10.0;
		q[4] = 1.0 / 10.0;
		
		double result = StatLib.klDivergenceWithLaplaceCorrection(p, q);
		
		System.out.println("D_KL(p||q)= " + result);
		
		result = StatLib.klDivergenceWithLaplaceCorrection(q, p);
		
		System.out.println("D_KL(q||p)= " + result);
		
		result = StatLib.jsDivergence(p, q);
		
		System.out.println("D_JS(p||q)= " + result);
		
		result = StatLib.jsDivergence(q, p);
		
		System.out.println("D_JS(q||p)= " + result);

	}

}
