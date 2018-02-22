package it.unipi.iot.parking.lib.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * Reads a file containing a JSON configuration objects and stores it inside its
 * static variables. The file shall be located inside program execution
 * directory and it shall contain an object similar to the following one:
 *
 * { host_address: "127.0.0.1", port_number: "5684", cse_id:
 * "parking-pisa-pisano", credentials: "admin:admin", api: "perking-api",
 * app_name:"parks", cse_name: "Via Andrea Pisano, Pisa", in_id: "parking",
 * containers_number: 20, park_data: {lat: "", lon:"", name: "", address: "",
 * price: 1.2, openT: "", closeT: ""} spot_data: [{ lat: "", lon: "", free:
 * true, user = null}, { lat: "", lon: "", free: true, user = null}]}
 * 
 * @author Gabriele Ara
 *
 */
public class MNConfig {
	
	public static final String		HOST_ADDRESS;
	public static final String		PORT_NUMBER;
	public static final String		CREDENTIALS;
	public static final String		CSE_ID;
	public static final String		CSE_NAME;
	public static final String		IN_ID;
	
	public static final String		API;
	public static final String		APP_NAME;
	
	public static final int			CONTAINERS_NUMBER;
	
	public static final String		PARK_DATA;
	public static final String[]	SPOT_DATA;
	
	static {
		Path currentRelativePath = Paths.get("");
		String s = currentRelativePath.toAbsolutePath().toString();
		System.out.println("Current relative path is: " + s);
		
		
		Charset encoding = StandardCharsets.UTF_8;
		String fname = "./config.json";
		
		String content;
		JSONObject obj;
		try {
			byte[] encoded = Files.readAllBytes(Paths.get(fname));
			content = new String(encoded, encoding);
			obj = new JSONObject(content);
			
			HOST_ADDRESS = obj.getString("host_address");
			
			System.out.println("Host address:" + HOST_ADDRESS);
			
			PORT_NUMBER = obj.getString("port_number");
			CREDENTIALS = obj.getString("credentials");
			API = obj.getString("api");
			APP_NAME = obj.getString("app_name");
			CSE_ID = obj.getString("cse_id");
			CSE_NAME = obj.getString("cse_name");
			IN_ID = obj.getString("in_id");
			CONTAINERS_NUMBER = obj.getInt("containers_number");
			PARK_DATA = obj	.getJSONObject("park_data")
							.toString();
			
			JSONArray spots = obj.getJSONArray("spot_data");
			
			SPOT_DATA = new String[CONTAINERS_NUMBER];
			
			for (int i = 0; i < CONTAINERS_NUMBER; ++i) {
				SPOT_DATA[i] = spots.get(i)
									.toString();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		if (HOST_ADDRESS == null)
			throw new RuntimeException("Configuration constant HOST_ADDRESS was not initialized!");
		if (PORT_NUMBER == null)
			throw new RuntimeException("Configuration constant PORT_NUMBER was not initialized!");
		if (CREDENTIALS == null)
			throw new RuntimeException("Configuration constant CREDENTIALS was not initialized!");
		if (API == null)
			throw new RuntimeException("Configuration constant API was not initialized!");
		if (APP_NAME == null)
			throw new RuntimeException("Configuration constant APP_NAME was not initialized!");
		if (CSE_ID == null)
			throw new RuntimeException("Configuration constant CSE_ID was not initialized!");
		if (CSE_NAME == null)
			throw new RuntimeException("Configuration constant CSE_NAME was not initialized!");
		if (IN_ID == null)
			throw new RuntimeException("Configuration constant IN_ID was not initialized!");
		if (CONTAINERS_NUMBER <= 0)
			throw new RuntimeException(
					"Configuration constant CONTAINERS_NUMBER was not initialized!");
		if (PARK_DATA == null)
			throw new RuntimeException("Configuration constant PARK_DATA was not initialized!");
		
		for(int i = 0; i < CONTAINERS_NUMBER; ++i) {
			if (SPOT_DATA[i] == null || SPOT_DATA[i] == "null")
				throw new RuntimeException("Configuration constant SPOT_DATA[" + i +"] was not initialized!");
		}
		
	}
	
}
