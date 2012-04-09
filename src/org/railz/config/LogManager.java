package org.railz.config;

import java.util.logging.Logger;

public class LogManager {
	public static Logger getDefault () {
		Logger currentLogger = Logger.getLogger(LoggingConstants.DEFAULT_LOGGER);
		return currentLogger;
	}
	public static Logger getLogger(String className) {
		return getDefault();
	}
}
