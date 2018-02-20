package it.unipi.iot.parking.lib.util;

/**
 * 
 * @author Gabriele Ara
 *
 */
public class SimpleLogger {
	
	private SimpleLogger() {
	}
	
	static SimpleLevel logLevel = SimpleLevel.VERBOSE;
	
	public static void setLogLevel(SimpleLevel level) {
		logLevel = level;
	}
	
	public static SimpleLevel getLogLevel() {
		return logLevel;
	}
	
	public static void log(SimpleLevel level, String className, String str) {
		if (level.shouldPrint()) {
			System.out.println(className + "[" + level + "]:" + str);
		}
	}
	
}
