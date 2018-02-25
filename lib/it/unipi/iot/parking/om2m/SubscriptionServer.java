package it.unipi.iot.parking.om2m;

import java.net.Inet4Address;
import java.net.UnknownHostException;

import org.eclipse.californium.core.CoapServer;

public class SubscriptionServer extends CoapServer implements FullURIResource {
	
	/**
	 * Machine IP address, base for all servers
	 */
	static final String IPAddress;
	
	static {
		try {
			IPAddress = Inet4Address.getLocalHost()
									.getHostAddress();
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Server full URI, without '/' at the end.
	 */
	private final String serverURI;
	
	// TODO: change to package
	public SubscriptionServer(int port) {
		super(port);
		serverURI = "coap://" + IPAddress + ":" + port;
	}
	
	@Override
	public String getFullURI() {
		return serverURI;
	}
}