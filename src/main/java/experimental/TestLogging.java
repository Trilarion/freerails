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

package experimental;

import org.apache.log4j.Logger;


/**
 *
 * Used to test the logging configuration.
 *
 *
 * Usage:{@code java -Djava.util.logging.config.file=logging.properties experimental.TestLogging}
 *
 *
 * Make sure {@code logging.properties} is in the working directory.
 *
 *
 */
public class TestLogging {

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        Logger logger1 = Logger.getLogger(TestLogging.class.getName());
        logger1.info("Logging properties file: "
                + System.getProperty("java.util.logging.config.file"));

        logger1.error("Hello severe logging");
        logger1.warn("Hello warning logging");
        logger1.info("Hello info logging");
        logger1.debug("Hello fine logging");
        logger1.debug("Hello finer logging");
        logger1.debug("Hello finest logging");
    }
}