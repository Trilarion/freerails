/*
 * Created on 09-Jul-2005
 *
 */
package jfreerails.world.train;

import jfreerails.util.Utils;
import jfreerails.world.common.FreerailsSerializable;

strictfp public class ConstAcc implements FreerailsSerializable,
		SpeedAgainstTime {

	private static final long serialVersionUID = -2180666310811530761L;

	public static final ConstAcc STOPPED = new ConstAcc(0, 0, 0, 0);

	public static ConstAcc uas(double u, double a, double s) {
		double t = calcT(u, a, s);
		return new ConstAcc(a, t, u, s);
	}

	private static double calcT(double u, double a, double s) {
		// Note, Utils.solveQuadratic throws an exception if a == 0
		return a == 0 ? s / u : Utils.solveQuadratic(a * 0.5d, u, -s);
	}

	public static ConstAcc uat(double u, double a, double t) {
		double s = u * t + a * t * t / 2;
		return new ConstAcc(a, t, u, s);
	}

	private final double u, a, finalS, finalT;

	private ConstAcc(double a, double t, double u, double s) {
		this.a = a;
		this.finalT = t;
		this.u = u;
		this.finalS = s;
	}

	public double calcS(double t) {
		if(t == finalT) return finalS;
		validateT(t);
		double ds = u * t + a * t * t / 2;
		ds = Math.min(ds, finalS);
		return ds;
	}

	public double calcT(double s) {
		if(s == finalS ) return finalT;
		if(s < 0 || s > this.finalS ) 
			throw new IllegalArgumentException(s+" < 0 || "+s+" > "+finalS );
		double returnValue = calcT(u, a, s);
		returnValue = Math.min(returnValue, finalT);
		return returnValue;
	}

	public double calcV(double t) {
		validateT(t);
		return u + a * t;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ConstAcc))
			return false;

		final ConstAcc constAcc = (ConstAcc) o;

		if (a != constAcc.a)
			return false;
		if (finalT != constAcc.finalT)
			return false;
		if (u != constAcc.u)
			return false;

		return true;
	}

	public double calcA(double t) {
		validateT(t);
		return a;
	}

	public double getT() {
		return finalT;
	}

	public double getS() {
		return finalS;
	}

    @Override
	public int hashCode() {
        int result;
        long temp;
        temp = u != +0.0d ? Double.doubleToLongBits(u) : 0l;
        result = (int) (temp ^ (temp >>> 32));
        temp = a != +0.0d ? Double.doubleToLongBits(a) : 0l;
        result = 29 * result + (int) (temp ^ (temp >>> 32));
        temp = finalT != +0.0d ? Double.doubleToLongBits(finalT) : 0l;
        result = 29 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
    
    private void validateT(double t){
    	if(t < 0 || t > finalT)
    		throw new IllegalArgumentException("("+t+" < 0 || "+t+" > "+finalT+")");
    	
    }

	@Override
	public String toString() {
		String str = "ConstAcc [a=" + a + ", u=" + u + ", dt=" + finalT + "]";
		return str;
	}

}
