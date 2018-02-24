package it.unipi.iot.parking.om2m.subscribers;

import org.json.JSONObject;

import it.unipi.iot.parking.om2m.OM2M;
import it.unipi.iot.parking.om2m.SubscriberResource;
import it.unipi.iot.parking.om2m.SubscriptionServer;
import it.unipi.iot.parking.om2m.data.ApplicationEntity;
import it.unipi.iot.parking.om2m.data.Resource;

public class RemoteCSESubscriber extends CopySubscriberResource {
	
	public RemoteCSESubscriber(SubscriberResource parentResource, String remoteResourceID,
			String localResourceID) {
		super(parentResource, remoteResourceID, localResourceID,
				OM2M.RESOURCE_TYPE_APPLICATION_ENTITY);
	}
	
	public RemoteCSESubscriber(SubscriptionServer server, String remoteResourceID,
			String localResourceID) {
		super(server, remoteResourceID, localResourceID, OM2M.RESOURCE_TYPE_APPLICATION_ENTITY);
	}
	
	@Override
	Resource fromJSONObject(JSONObject res) {
		return new ApplicationEntity(res);
	}
	
	@Override
	protected void postProcess(Resource original, Resource copy) {
		AESubscriber aes = new AESubscriber(this, original.getResourceID(), copy.getResourceID());
		// TODO: update new data!
	}
	
}
