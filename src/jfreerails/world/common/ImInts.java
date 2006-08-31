/*
 * Created on 04-Jul-2005
 *
 */
package jfreerails.world.common;

import java.util.Arrays;

import jfreerails.util.Immutable;

/**
 * An immutable list of ints.
 * 
 * @author Luke
 * 
 */
@Immutable
public class ImInts implements FreerailsSerializable {

	private static final long serialVersionUID = -7171552118713000676L;

	private final int ints[];

	public ImInts(int... i) {
		this.ints = i.clone();
	}

	public static ImInts fromBoolean(boolean... i) {
		int[] ii = new int[i.length];
		for (int j = 0; j < i.length; j++) {
			ii[j] = i[j] ? 1 : 0;
		}
		return new ImInts(ii);
	}

	public int size() {
		return ints.length;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof ImInts))
			return false;

		final ImInts other = (ImInts) o;

		if (!Arrays.equals(ints, other.ints))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return ints.length;
	}

	public int get(int i) {
		return ints[i];
	}

	public ImInts removeLast() {
		int[] newInts = new int[ints.length - 1];
		System.arraycopy(ints, 0, newInts, 0, newInts.length);
		return new ImInts(newInts);
	}

	public ImInts append(int... extra) {
		int[] newInts = new int[ints.length + extra.length];
		System.arraycopy(ints, 0, newInts, 0, ints.length);
		System.arraycopy(extra, 0, newInts, ints.length, extra.length);
		return new ImInts(newInts);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(getClass().getName());
		sb.append("[");
		for (int i = 0; i < ints.length; i++) {
			sb.append(ints[i]);
			if (i + 1 < ints.length)
				sb.append(", ");
		}
		sb.append("]");
		return sb.toString();
	}
	
	/** Returns the sum of the ints stored in the list.*/
	public int sum(){
		int sum = 0;
		for (int i = 0; i < ints.length; i++) {
			sum+=ints[i];
		}
		return sum;
	}

}
