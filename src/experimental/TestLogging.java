package experimental;

import java.util.logging.Logger;

/*
 * Created on 30-May-2004
 */

/**
 * <p>
 * Used to test the logging configuration.
 * </p>
 * <p>
 * Usage:<code> java -Djava.util.logging.config.file=logging.properties experimental.TestLogging</code>
 * </p>
 * <p>
 * Make sure <code>logging.properties</code> is in the working directory.
 * </p>
 * 
 * @author Luke
 */
public class TestLogging {
	public static void main(String[] args) {
		Logger logger1 = Logger.getLogger(TestLogging.class.getName());
		logger1.info("Logging properties file: "
				+ System.getProperty("java.util.logging.config.file"));

		logger1.severe("Hello severe logging");
		logger1.warning("Hello warning logging");
		logger1.info("Hello info logging");
		logger1.fine("Hello fine logging");
		logger1.finer("Hello finer logging");
		logger1.finest("Hello finest logging");
	}
}