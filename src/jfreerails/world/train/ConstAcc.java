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

	public static final ConstAcc STOPPED = new ConstAcc(0, 0, 0);

	public static SpeedAgainstTime uas(double u, double a, double s) {
		double t = calcT(u, a, s);
		return new ConstAcc(a, t, u);
	}

	private static double calcT(double u, double a, double s) {
		// Note, Utils.solveQuadratic throws an exception if a == 0
		return a == 0 ? s / u : Utils.solveQuadratic(a * 0.5d, u, -s);
	}

	public static SpeedAgainstTime uat(double u, double a, double t) {
		return new ConstAcc(a, t, u);
	}

	private final double u, a, dt;

	private ConstAcc(double a, double t, double u) {
		this.a = a;
		this.dt = t;
		this.u = u;
	}

	public double calcS(double t) {
		return u * t + a * t * t / 2;
	}

	public double calcT(double s) {
		return calcT(u, a, s);
	}

	public double calcV(double t) {
		return u + a * t;
	}

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ConstAcc))
			return false;

		final ConstAcc constAcc = (ConstAcc) o;

		if (a != constAcc.a)
			return false;
		if (dt != constAcc.dt)
			return false;
		if (u != constAcc.u)
			return false;

		return true;
	}

	public double calcA(double t) {
		return a;
	}

	public double getT() {
		return dt;
	}

	public double getS() {
		return u * dt + a * dt * dt / 2;
	}

	public double getU() {
		return u;
	}

	public double getV() {
		return u + a * dt;
	}

	public int hashCode() {
		int result;
		long temp;
		temp = u != +0.0d ? Double.doubleToLongBits(u) : 0l;
		result = (int) (temp ^ (temp >>> 32));
		temp = a != +0.0d ? Double.doubleToLongBits(a) : 0l;
		result = 29 * result + (int) (temp ^ (temp >>> 32));
		temp = dt != +0.0d ? Double.doubleToLongBits(dt) : 0l;
		result = 29 * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public String toString() {
		String s = "ConstAcc [a=" + a + ", u=" + u + ", dt=" + dt + "]";
		return s;
	}

}
