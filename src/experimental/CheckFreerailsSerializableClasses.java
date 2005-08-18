/*
 * Created on 03-Jul-2005
 *
 */
package experimental;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.logging.Logger;

import jfreerails.util.ClassLocater;
import jfreerails.util.Immutable;
import jfreerails.util.InstanceControlled;
import jfreerails.world.common.FreerailsSerializable;

/** Checks that all class that implement FreerailsSerializable are immutable and override equals and hashcode.
 *
 *
 */
public class CheckFreerailsSerializableClasses {

	static final HashSet<Class> immutableTypes = new HashSet<Class>();

	static final HashSet<Class> mutableTypes = new HashSet<Class>();

	static Logger logger = Logger.getLogger(CheckFreerailsSerializableClasses.class
			.getName());

	public static void main(String[] args) {
		immutableTypes.clear();
		mutableTypes.clear();

		immutableTypes.add(String.class);

		// Class clazz = StationModel.class;
		// System.err.println(overridesHashCodeAndEquals(clazz));
		// System.out.println(clazz.isAnnotationPresent(InstanceControlled.class));
		// Annotation[] ans = clazz.getAnnotations();
		// for (Annotation an : ans) {
		// System.err.println(an);
		// }
		// System.err.println(checkFields(clazz));

		testAllClasses();

		for (Class c : mutableTypes) {
			System.err.println(c.getName());
		}

	}

	static boolean checkFields(Class<?> clazz) {
		Field[] fields = clazz.getDeclaredFields();
		boolean okSoFar = true;
		boolean assertImmutable = clazz.isAnnotationPresent(Immutable.class);

		for (Field field : fields) {
			int modifiers = field.getModifiers();
			if (Modifier.isStatic(modifiers)) {
				logger.fine("Skipping static field " + field.getName());
				continue;
			}

			Class<?> type = field.getType();
			if (type.isPrimitive()) {
				continue;
			}
			// if(!Modifier.isPrivate(modifiers)){
			// System.err.println(clazz.getName()+field.getName()+" should be
			// private!");
			// okSoFar = false;
			// }
			if (!FreerailsSerializable.class.isAssignableFrom(type)
					&& !assertImmutable) {
				if (!immutableTypes.contains(type) && !type.isEnum()
						&& !type.isAnnotationPresent(Immutable.class)) {
					System.err.println(clazz.getName() + "." + field.getName()
							+ " {" + type.getName()
							+ "} might not be immutable!");
					okSoFar = false;
					if (!type.isArray())
						mutableTypes.add(type);
				}
			}
		}
		return okSoFar;
	}

	@SuppressWarnings("unchecked")
	static void testAllClasses() {
		ClassLocater locater = new ClassLocater();
		Class[] classes = locater.getSubclassesOf(FreerailsSerializable.class);
		int classesWithProblems = 0;
		for (Class clazz : classes) {
			if (clazz.isInterface()) {
				logger.fine("Skipping inferface " + clazz.getName());
				continue;
			}

			int mods = clazz.getModifiers();
			if ((mods & Modifier.ABSTRACT) != 0) {
				logger.fine("Skipping abstract class " + clazz.getName());
				continue;
			}
			if (clazz.isAnnotationPresent(InstanceControlled.class)) {
				logger.fine("Skipping InstanceControlled class "
						+ clazz.getName());
				continue;
			}

			boolean b = overridesHashCodeAndEquals(clazz);
			b = b && checkFields(clazz);
			if (!b) {
				classesWithProblems++;
			}
		}
		System.err.println(classes.length + " classes checked, "
				+ classesWithProblems + " have problems");

	}

	static boolean overridesHashCodeAndEquals(Class clazz) {

		try {
			boolean okSoFar = true;
			Method equals = clazz.getMethod("equals", Object.class);

			if (equals.getDeclaringClass().equals(Object.class)) {
				System.err.println(clazz.getName() + " does not override "
						+ equals.getName());
				okSoFar = false;
			}
			Method hashCode = clazz.getMethod("hashCode");

			if (hashCode.getDeclaringClass().equals(Object.class)) {
				System.err.println(clazz.getName() + " does not override "
						+ hashCode.getName());
				okSoFar = false;
			}
			return okSoFar;
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

}
