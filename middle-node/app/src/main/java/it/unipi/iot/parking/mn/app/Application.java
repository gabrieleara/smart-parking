package it.unipi.iot.parking.mn.app;

import it.unipi.iot.parking.om2m.OM2M;

public class Application {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		OM2M om2m = OM2M.init("localhost", "5684", "admin:admin");
		
		System.out.println(om2m.createContentInstance("/parking-pisa-pisano/cnt-186642875", "{ \"roba\": 10 }").toJSONObject().toString());
		
		//om2m.createContent("parking-pisa-pisano/cnt-347118771", "{roba}");
	}
}
