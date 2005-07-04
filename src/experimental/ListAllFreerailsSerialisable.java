/*
 * Created on 03-Jul-2005
 *
 */
package experimental;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.logging.Logger;

import jfreerails.util.ClassLocater;
import jfreerails.util.InstanceControlled;
import jfreerails.world.common.FreerailsSerializable;

public class ListAllFreerailsSerialisable {

	static Logger logger = Logger.getLogger(ListAllFreerailsSerialisable.class
			.getName());

	public static void main(String[] args) {

		// Class clazz = KEY.class;
		// System.err.println(overridesHashCodeAndEquals(clazz));
		// System.out.println(clazz.isAnnotationPresent(InstanceControlled.class));
		// Annotation[] ans =clazz.getAnnotations();
		// for(Annotation an : ans){
		// System.err.println(an);
		// }
		testAllClasses();

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
