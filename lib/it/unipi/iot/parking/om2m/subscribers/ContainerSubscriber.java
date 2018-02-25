package it.unipi.iot.parking.om2m.subscribers;

import org.json.JSONObject;

import it.unipi.iot.parking.om2m.OM2M;
import it.unipi.iot.parking.om2m.SubscriberResource;
import it.unipi.iot.parking.om2m.SubscriptionServer;
import it.unipi.iot.parking.om2m.data.ContentInstance;
import it.unipi.iot.parking.om2m.data.Resource;

/**
 * This class tracks changes inside a Node, either a InfrastructureNode or a
 * MiddleNode and copies Application entities in it in the local remote CSE
 * reference.
 * 
 * @author Gabriele Ara
 *
 */
public class ContainerSubscriber extends CopySubscriberResource {

	public ContainerSubscriber(SubscriberResource parentResource, String remoteResourceID,
			String localResourceID) {
		super(parentResource, remoteResourceID, localResourceID, OM2M.RESOURCE_TYPE_CONTENT_INSTANCE);
	}

	public ContainerSubscriber(SubscriptionServer server, String remoteResourceID,
			String localResourceID) {
		super(server, remoteResourceID, localResourceID, OM2M.RESOURCE_TYPE_CONTENT_INSTANCE);
	}

	@Override
	Resource fromJSONObject(JSONObject res) {
		return new ContentInstance(res);
	}

	@Override
	protected void postProcess(Resource original, Resource copy) {
		// TODO: update new data!
	}
	
	
	
}
