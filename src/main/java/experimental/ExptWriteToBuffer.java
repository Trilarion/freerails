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
 * Created on 21-Apr-2003
 *
 */
package experimental;

import org.apache.log4j.Logger;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Experiment to try out reading and writing to a buffer to test serialization
 * code.
 *
 */
public class ExptWriteToBuffer {
    private static final Logger logger = Logger
            .getLogger(ExptWriteToBuffer.class.getName());

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            Point p = new Point(10, 10);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream objectOut = new ObjectOutputStream(out);
            objectOut.writeObject(p);
            objectOut.flush();

            byte[] bytes = out.toByteArray();

            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            ObjectInputStream objectIn = new ObjectInputStream(in);
            Object o = objectIn.readObject();
            Point p2 = (Point) o;

            if (p.equals(p2)) {
                logger.info("The two objects are equal!");
            } else {
                logger.info("The two objects are not equal!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}