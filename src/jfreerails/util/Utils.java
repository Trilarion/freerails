/*
 * Created on Jun 26, 2004
 */
package jfreerails.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.StringTokenizer;

import jfreerails.world.top.KEY;

/**
 * A bunch of static methods.
 * 
 * @author Luke
 * 
 */
public class Utils {
	public static Serializable cloneBySerialisation(Object m)
			throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream objectOut = new ObjectOutputStream(out);
		objectOut.writeObject(m);
		objectOut.flush();

		byte[] bytes = out.toByteArray();

		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		ObjectInputStream objectIn = new ObjectInputStream(in);
		Serializable o;

		try {
			o = (Serializable) objectIn.readObject();
		} catch (ClassNotFoundException e) {
			// Should never happen.
			throw new IllegalStateException();
		}

		return o;
	}

	public static String capitalizeEveryWord(String str) {
		StringBuffer result = new StringBuffer();
		StringTokenizer tok = new StringTokenizer(str);

		while (tok.hasMoreTokens()) {
			String token = tok.nextToken().toLowerCase();
			result.append(Character.toUpperCase(token.charAt(0))
					+ token.substring(1) + " ");
		}
		return result.toString().trim();
	}

	public static String findConstantFieldName(Object o) {
		Field[] fields = KEY.class.getFields();
		for (int i = 0; i < fields.length; i++) {
			int modifiers = fields[i].getModifiers();

			try {
				if (Modifier.isStatic(modifiers)
						&& Modifier.isPublic(modifiers)) {
					Object o2 = fields[i].get(null);
					if (o2.equals(o)) {
						return fields[i].getName();
					}
				}
			} catch (IllegalAccessException e) {
				throw new IllegalStateException();
			}
		}

		return null;
	}

	/** Returns the largest solution of the quadratic equation ax<sup><font size="-1">2</font></sup> + bx + c = 0.
	 * @throws IllegalArgumentException if <code>a == 0</code>
	 * @throws IllegalArgumentException if <code>(b * b - 4 * a * c) < 0</code> 
	 */
	public static double solveQuadratic(double a, double b, double c)
			throws IllegalArgumentException {		
		if (a == 0) {
			throw new IllegalArgumentException("a == 0");
		}
		double disc = b * b - 4 * a * c;
		if (disc < 0)
			throw new IllegalArgumentException("(b * b - 4 * a * c) < 0");
		return (-b + Math.sqrt(disc)) / (2 * a);

	}	
}