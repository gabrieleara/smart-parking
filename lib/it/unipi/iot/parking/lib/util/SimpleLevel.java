package it.unipi.iot.parking.lib.util;

public enum SimpleLevel {
	NONE, ERROR, WARNING, VERBOSE;
	
	boolean shouldPrint() {
		return this.ordinal() <= SimpleLogger.logLevel.ordinal();
	}
}
