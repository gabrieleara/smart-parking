package it.unipi.iot.parking.util;

public class UniqueAssigner {
	private int counter = 0;
	
	private static final String num3char(int num) {
		String base = "";
		
		if(num < 100)
			base += "0";
		if(num < 10)
			base += "0";
		
		return base + num;
	}
	
	public String assignName() {
		int num;
		
		synchronized(this) {
			num = counter++;
		}
		
		return num3char(num);
	}
	
}
