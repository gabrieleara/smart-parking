package it.unipi.iot.parking.om2m.subscribers;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;

import org.eclipse.californium.core.server.resources.CoapExchange;
import org.json.JSONObject;

import it.unipi.iot.parking.om2m.OM2M;
import it.unipi.iot.parking.om2m.SubscriberResource;
import it.unipi.iot.parking.om2m.SubscriptionServer;
import it.unipi.iot.parking.om2m.data.Resource;
import it.unipi.iot.parking.om2m.data.ResourceComparator;

public abstract class CopySubscriberResource extends SubscriberResource {
	
	private final CopyCreator			copyCreator			= new CopyCreator();
	private final OM2M					conn				= OM2M.getInstance();
	
	private final String				remoteResourceID;
	private final String				localResourceID;
	private final int					copyResourceType;
	
	private final Object				MONITOR				= new Object();
	private String[]					initialResources	= null;
	
	private final SortedSet<Resource>	set					= new ConcurrentSkipListSet<>(
			new ResourceComparator());
	
	/**
	 * Creates a new CopySubscriberResource that must be appended to the specified
	 * parentResource.
	 * 
	 * @param parentResource
	 *            the parent resource
	 * @param remoteResourceID
	 *            the remote OM2M Resource monitored by this CoAP resource
	 * @param localResourceID
	 *            the local OM2M Resource where new values must be appended
	 * @param copyResourceType
	 *            the type of the new values that shall be received
	 */
	public CopySubscriberResource(SubscriberResource parentResource, String remoteResourceID,
			String localResourceID, int copyResourceType) {
		super(parentResource);
		
		this.remoteResourceID = remoteResourceID;
		this.localResourceID = localResourceID;
		this.copyResourceType = copyResourceType;
		
		subscribeToRemote();
	}
	
	/**
	 * Creates a new CopySubscriberResource that must be appended to the specified
	 * server.
	 * 
	 * @param server
	 *            the server to which this resource must be appended
	 * @param remoteResourceID
	 *            the remote OM2M Resource monitored by this CoAP resource
	 * @param localResourceID
	 *            the local OM2M Resource where new values must be appended
	 * @param copyResourceType
	 *            the type of the new values that shall be received
	 */
	public CopySubscriberResource(SubscriptionServer server, String remoteResourceID,
			String localResourceID, int copyResourceType) {
		super(server);
		
		this.remoteResourceID = remoteResourceID;
		this.localResourceID = localResourceID;
		this.copyResourceType = copyResourceType;
		
		subscribeToRemote();
	}
	
	/**
	 * Subscribes the current resource to the remote OM2M Resource specified in the
	 * constructor.
	 */
	private void subscribeToRemote() {
		copyCreator.start();
		
		SubscriptionThread st = new SubscriptionThread();
		st.start();
	}
	
	/**
	 * Checks if a resource has been inserted by the SubscriptionThread launched at
	 * the beginning of the subscription.
	 * 
	 * @param id
	 * @return true if the resource has been already inserted (or it has been
	 *         scheduled for the insertion).
	 */
	private boolean alreadyInserted(String id) {
		synchronized (MONITOR) {
			while (initialResources == null) {
				try {
					MONITOR.wait();
				} catch (InterruptedException e) {
					// Thread should terminate? Oh well, eventually it will, don't worry, it won't
					// take much time
				}
			}
		}
		
		return Arrays	.stream(initialResources)
						.anyMatch(s -> s.equals(id));
	}
	
	/**
	 * A new attribute has been added to the monitored remote resource. It shall be
	 * added to the local copy too.
	 */
	@Override
	protected void handlePost(CoapExchange exchange) {
		JSONObject body = new JSONObject(exchange.getRequestText());
		
		if (isVerificationRequest(body)) {
			// First time, do nothing basically
			return;
		}
		
		if (isSubscriptionDeletion(body)) {
			// TODO: for now do nothing, maybe later interrupt copyCreator thread
			return;
		}
		
		// Not first time and got a new value
		
		JSONObject resource = getResource(body, copyResourceType);
		
		if (resource == null)
			return;
		
		// NOTICE: this means that order is no more respected
		if (alreadyInserted(resource.getString(OM2M.ATTR_RESOURCE_ID)))
			return;
		
		Resource copy = fromJSONObject(resource);
		
		addCopy(copy);
	}
	
	/**
	 * Subclasses shall implement this method with the appropriate constructor of a
	 * subclass of Resource.
	 * 
	 * @param res
	 * @return
	 */
	abstract Resource fromJSONObject(JSONObject res);
	
	/**
	 * Adds a ResourceCopy to the set of the resources that shall be copied on the
	 * local node.
	 * 
	 * @param copy
	 */
	private synchronized void addCopy(Resource copy) {
		set.add(copy);
		notify();
	}
	
	/**
	 * Extracts a copy from the set.
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	private Resource extractCopy() throws InterruptedException {
		Resource copy;
		
		synchronized (this) {
			while (set.size() < 1) {
				wait();
			}
			
			copy = set.first();
			set.remove(copy);
		}
		
		return copy;
	}
	
	/**
	 * Subclasses shall implement this method to post-process copies after they are
	 * appended to the new parent resource. Or do nothing if they don't care.
	 * 
	 * @param original
	 * @param copy
	 */
	protected abstract void postProcess(Resource original, Resource copy);
	
	private class SubscriptionThread extends Thread {
		
		@Override
		public void run() {
			conn.createSubscription(remoteResourceID, getFullURI());
			
			String[] uril = conn.getAllChildrenIDOfType(remoteResourceID, copyResourceType);
			
			synchronized (MONITOR) {
				initialResources = uril;
				MONITOR.notify();
			}
			
			for (String uri : uril) {
				JSONObject child = conn.getResourceFromID(uri, copyResourceType);
				
				Resource copy = fromJSONObject(child);
				
				addCopy(copy);
			}
		}
		
	}
	
	private class CopyCreator extends Thread {
		@Override
		public void run() {
			try {
				while (true) {
					Resource original = extractCopy();
					
					Resource copy = conn.create(localResourceID, original);
					
					postProcess(original, copy);
				}
			} catch (InterruptedException ex) {
				// Thread terminates, as requested
			}
		}
	}
	
}
