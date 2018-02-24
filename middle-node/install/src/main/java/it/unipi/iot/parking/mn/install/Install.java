package it.unipi.iot.parking.mn.install;

import java.util.ArrayList;

import it.unipi.iot.parking.om2m.OM2M;
import it.unipi.iot.parking.om2m.data.ApplicationEntity;
import it.unipi.iot.parking.om2m.data.Container;
import it.unipi.iot.parking.util.MNConfig;

public class Install {
	// private static final String CREDENTIALS = "admin:admin";
	// private static final String API_ID = "Server-Park-ID";
	// private static final String APP_NAME = "Server-Park";
	// private static final String[] CONTAINERS = { "cont-1", "cont-2", "cont-3",
	// "cont-4" };
	
	private static final String			containerBaseName	= "park-";
	
	// private static final String HOST_ADDRESS = "localhost";
	// private static final String HOST_ADDRESS = "192.168.193.128"; // "localhost"
	// private static final String CSE_ID = "parking-mn-roba";
	// private static final String PORT_NUMBER = "5684"; // 5684
	// private static final String CSE_NAME = "park-r";
	
	static final ArrayList<Container>	containers			= new ArrayList<>();
	
	private static final String num3Char(int num) {
		if (num > 99)
			return "" + num;
		
		if (num > 9)
			return "0" + num;
		
		return "00" + num;
	}
	
	public static void main(String[] args) {
		OM2M om2m_cse = OM2M.init(MNConfig.HOST_ADDRESS, MNConfig.PORT_NUMBER, MNConfig.CREDENTIALS);
		
		ApplicationEntity ae = om2m_cse.createApplicationEntity(MNConfig.CSE_ID, MNConfig.API,
				MNConfig.APP_NAME);
		
		System.out.println("AE: " + ae.getResourceID());
		
		String containerName;
		
		int i = -1;
		
		containerName = containerBaseName + num3Char(i + 1);
		Container container = om2m_cse.createContainer(ae.getResourceID(), containerName);
		om2m_cse.createContentInstance(container.getResourceID(), MNConfig.PARK_DATA);
		
		System.out.println("CONT: " + container.getResourceID());
		
		for (i = 0; i < MNConfig.CONTAINERS_NUMBER; ++i) {
			containerName = containerBaseName + num3Char(i + 1);
			container = om2m_cse.createContainer(ae.getResourceID(), containerName);
			om2m_cse.createContentInstance(container.getResourceID(), MNConfig.SPOT_DATA[i]);
			
			System.out.println("CONT: " + container.getResourceID());
		}
		
		/*
		 * int i = 0;
		 * 
		 * while (i++ < 5) { try { Thread.sleep(2000); } catch (InterruptedException e)
		 * { // TODO Auto-generated catch block e.printStackTrace(); }
		 * 
		 * JSONObject obj = new JSONObject(); obj.put("value", i * 5);
		 * 
		 * Content content = om2m_cse.createContent(c.getResourceID(), obj.toString());
		 * }
		 * 
		 * i = 0; } else {
		 * 
		 * String response = om2m_cse.getLastValues(CSE_ID, 3); }
		 * 
		 * // System.out.println(response); return;
		 * 
		 * // Content[] values = om2m_cse.getLastValues(c.getResourceID(), 3); /*
		 * while(i++ < values.length) { System.out.println(values[i].getStateTag() +
		 * ": " + values[i].getContentValue().get("value")); }
		 * 
		 * System.out.println("FINISHED!");
		 */
		
	}
	
}
