package it.unipi.iot.parking.om2m.subscribers;

import org.json.JSONObject;

import it.unipi.iot.parking.om2m.OM2M;
import it.unipi.iot.parking.om2m.SubscriberResource;
import it.unipi.iot.parking.om2m.SubscriptionServer;
import it.unipi.iot.parking.om2m.data.Container;
import it.unipi.iot.parking.om2m.data.Resource;

/**
 * This class tracks changes inside a Node, either a InfrastructureNode or a
 * MiddleNode and copies Application entities in it in the local remote CSE
 * reference.
 * 
 * @author Gabriele Ara
 *
 */
public class AESubscriber extends CopySubscriberResource {

	public AESubscriber(SubscriberResource parentResource, String remoteResourceID,
			String localResourceID) {
		super(parentResource, remoteResourceID, localResourceID, OM2M.RESOURCE_TYPE_CONTAINER);
	}

	public AESubscriber(SubscriptionServer server, String remoteResourceID,
			String localResourceID) {
		super(server, remoteResourceID, localResourceID, OM2M.RESOURCE_TYPE_CONTAINER);
	}

	@Override
	Resource fromJSONObject(JSONObject res) {
		return new Container(res);
	}

	@Override
	protected void postProcess(Resource original, Resource copy) {
		ContainerSubscriber cons = new ContainerSubscriber(this, original.getResourceID(), copy.getResourceID());
		// TODO: update new data!
	}
	
	
	
}
