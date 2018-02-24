package it.unipi.iot.parking.web;

import it.unipi.iot.parking.om2m.OM2M;
import it.unipi.iot.parking.util.MNConfig;

public class BackgroundThread extends Thread {

	@Override
	public void run() {
		System.out.println("Ciao!");
		
		OM2M om2m_cse = new OM2M(MNConfig.HOST_ADDRESS, "5683", MNConfig.CREDENTIALS);
		
		//om2m_cse.getLastValues("/parking-pisa-pisano/ViaAndreaPisanoPisa/parks/park-000", 0);
	}
	
	

}
