package it.unipi.iot.parking.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateConverter {
	static final Calendar	calendar;
	static final DateFormat	formatter;
	
	static {
		TimeZone tz = TimeZone.getTimeZone("Europe/Rome");
		calendar = Calendar.getInstance(tz);
		formatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
		formatter.setCalendar(calendar);
	}
	
	public synchronized static Date fromString(String string) {
		try {
			calendar.setTime(formatter.parse(string));
		} catch (ParseException ex) {
			throw new RuntimeException(ex);
		}
		
		return calendar.getTime();
	}
	
	public synchronized static String fromDate(Date d) {
		return formatter.format(d);
	}
	
	/*
	 * public static void main(String[] args) { Date d =
	 * fromString("20130717T035800"); System.out.println("From string: " + d);
	 * System.out.println("From date: " + fromDate(d)); }
	 */
	
}
