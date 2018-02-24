package it.unipi.iot.parking.om2m.data;

import java.util.Comparator;
import java.util.Date;

import it.unipi.iot.parking.util.DateConverter;

public class ResourceComparator implements Comparator<Resource> {
	
	@Override
	public int compare(Resource arg0, Resource arg1) {
		Date d0 = DateConverter.fromString(arg0.getCreationTime());
		Date d1 = DateConverter.fromString(arg1.getCreationTime());
		
		if (d0.before(d1))
			return -1;
		if (d0.after(d1))
			return 1;
		return 0;
	}
	
}
