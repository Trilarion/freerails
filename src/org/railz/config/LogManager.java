package org.railz.config;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.railz.launcher.Launcher;

public class LogManager {
    public static Handler fileHandler2 = null;
    private static String LOG_SIZE = "";
    private static String LOG_ROTATION_COUNT = "";
    private static String CONFIG_FILE = "/org/railz/util/logging.properties";
    private static final String FILE_NAME_LOG = "logger.log";
    
    public static final Level LOG_LEVEL_DEFAULT = Level.INFO;
    public static final Level LOG_LEVEL_DEFAULT_FILE = Level.WARNING;
    static {
	
	try {
	    FileHandler fileHandler = new FileHandler(FILE_NAME_LOG);
	    fileHandler2 = fileHandler;
	    fileHandler2.setLevel(LOG_LEVEL_DEFAULT_FILE);
	    
	} catch (SecurityException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	
    }
    
    private static void setupFileHandler() {
	FileHandler fileHandler3 = null;
	try {
	    fileHandler3 = new FileHandler(FILE_NAME_LOG);
	} catch (SecurityException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	
	fileHandler2 = fileHandler3;
	fileHandler2.setLevel(LOG_LEVEL_DEFAULT_FILE);
    }
    
    private static Logger defaultLogger = null;
    
    public static Logger getDefault() {
	
	// Logger currentLogger =
	// Logger.getLogger(LoggingConstants.DEFAULT_LOGGER);
	
	if (defaultLogger != null) {
	    return defaultLogger;
	}
	defaultLogger = Logger.getLogger(LoggingConstants.DEFAULT_LOGGER);
	
	Handler[] arrr = defaultLogger.getHandlers();
	if (arrr != null && arrr.length > 0) {
	    
	} else {
	    defaultLogger.addHandler(fileHandler2);
	    // defaultLogger.addHandler(fileHandler2);
	    ConsoleHandler consoleHandler = new ConsoleHandler();
	    consoleHandler.setLevel(LOG_LEVEL_DEFAULT);
	    defaultLogger.addHandler(consoleHandler);
	    
	}
	
	return defaultLogger;
	
    }
    

    public static Logger getLogger(String CLASS_NAME) {
	Logger newLogger = Logger.getLogger(CLASS_NAME);
	newLogger.setLevel(LOG_LEVEL_DEFAULT);
	return newLogger;
	// return getDefault();
    }
    
    public static void setupDefaultLogger() {
	
	if (defaultLogger == null) {
	    defaultLogger = Logger.getLogger(LoggingConstants.DEFAULT_LOGGER);
	}
	
	Handler[] handlers = defaultLogger.getHandlers();
	if (handlers == null || handlers.length < 2) {
	    
	    defaultLogger.addHandler(fileHandler2);
	    // ConsoleHandler consoleHandler = new ConsoleHandler();
	    // consoleHandler.setLevel(LOG_LEVEL_DEFAULT);
	    // defaultLogger.addHandler(consoleHandler);
	    
	}
	
	// Logger logger = Logger.getLogger(LoggingConstants.DEFAULT_LOGGER);
	defaultLogger.log(Level.INFO, "Logging enabled");
    }
    
    public static void enableLogging() {
	Logger logger = Logger.getLogger(LoggingConstants.DEFAULT_LOGGER);
	logger.log(LOG_LEVEL_DEFAULT, "Logging enabled");
    }
    
    public static void initiateLogging() {
	java.util.logging.LogManager lm = java.util.logging.LogManager.getLogManager();
	try {
	    lm.readConfiguration(Launcher.class.getResourceAsStream(CONFIG_FILE));
	} catch (IOException e) {
	    System.err.println("Couldn't open logging properties" + " due to IOException"
		    + e.getMessage());
	} catch (SecurityException e) {
	    System.err.println("Couldn't open logging configuration " + "due to SecurityException:"
		    + e.getMessage());
	}
    }
}
