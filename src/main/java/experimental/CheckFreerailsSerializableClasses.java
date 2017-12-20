/*
 * FreeRails
 * Copyright (C) 2000-2018 The FreeRails Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 *
 */
package experimental;

import freerails.util.Immutable;
import freerails.util.InstanceControlled;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;

/**
 * Checks that all class that implement FreerailsSerializable are immutable and
 * override equals and hashcode.
 */
public class CheckFreerailsSerializableClasses {

    static final HashSet<Class> immutableTypes = new HashSet<>();

    static final HashSet<Class> mutableTypes = new HashSet<>();

    static final Logger logger = Logger.getLogger(CheckFreerailsSerializableClasses.class.getName());

    /**
     * @param args
     */
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
                if (logger.isDebugEnabled()) {
                    logger.debug("Skipping static field " + field.getName());
                }
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
            if (!Serializable.class.isAssignableFrom(type)
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
        Class[] classes = locater.getSubclassesOf(Serializable.class);
        int classesWithProblems = 0;
        for (Class clazz : classes) {
            if (clazz.isInterface()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Skipping interface " + clazz.getName());
                }
                continue;
            }

            int mods = clazz.getModifiers();
            if ((mods & Modifier.ABSTRACT) != 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Skipping abstract class " + clazz.getName());
                }
                continue;
            }
            if (clazz.isAnnotationPresent(InstanceControlled.class)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Skipping InstanceControlled class "
                            + clazz.getName());
                }
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
        } catch (SecurityException | NoSuchMethodException e) {
            return false;
        }

    }
}
