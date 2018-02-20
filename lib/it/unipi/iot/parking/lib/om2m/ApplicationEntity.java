package it.unipi.iot.parking.lib.om2m;

import org.json.JSONObject;

/**
 * This class represents a OneM2M Application Entity.
 * 
 * @author Gabriele Ara
 *
 */
public class ApplicationEntity extends Resource {
	protected ApplicationEntity(JSONObject obj) {
		super(obj);
	}
}