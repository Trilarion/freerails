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