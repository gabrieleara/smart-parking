package it.unipi.iot.parking.om2m;

/**
 * A OM2MSession is a way to "pack" together a bunch of data that will be used
 * by a {@link OM2M} object to connect and communicate with a remote OM2M Node
 * over the Internet.
 * 
 * Notice that this is just a way to tie data together, it doesn't open any
 * Internet connection yet. This is done each time you perform a request via a
 * {@link OM2M} object.
 * 
 * @author Gabriele Ara
 *
 */
public class OM2MSession {
    /**
     * The protocol used to communicate with OM2M nodes.
     */
    private final String PROTOCOL = "coap";
    
    /**
     * The timeout used to determine whether a CoAP server is unreachable, in
     * milliseconds.
     */
    public static final long TIMEOUT = 10000L;
    
    /**
     * The host name or IP address where the OM2M node resides.
     */
    private final String hostName;
    
    /**
     * The port number that shall be used to contact the OM2M node.
     */
    private final String portNumber;
    
    /**
     * The credentials accepted by the OM2M node.
     */
    private final String credentials;
    
    /**
     * Creates a new {@linkplain OM2MSession} object. Notice that this has nothing
     * to do with Internet session, as stated in the class {@link OM2MSession}
     * description.
     * 
     * @param hostName
     *            the host name or IP address where the OM2M node resides
     * @param portNumber
     *            the port number that shall be used to contact the OM2M node
     * @param credentials
     *            the credentials accepted by the OM2M node, in the form of
     *            "user:pass"
     */
    public OM2MSession(String hostName, String portNumber, String credentials) {
        super();
        this.hostName = hostName;
        this.portNumber = portNumber;
        this.credentials = credentials;
    }
    
    /**
     * 
     * @return the host name or IP address where the OM2M node resides
     */
    public String getHostName() {
        return hostName;
    }
    
    /**
     * 
     * @return the port number that shall be used to contact the OM2M node
     */
    public String getPortNumber() {
        return portNumber;
    }
    
    /**
     * Constructs the URI that will be used by a {@link OM2M} object to construct
     * identifiers of elements and perform queries to the actual OM2M Node.
     * 
     * @return the constructed URI
     */
    String getBaseURI() {
        return PROTOCOL + "://" + hostName + ":" + portNumber + "/~";
    }
    
    /**
     * 
     * @return the credentials accepted by the OM2M node, in the form of "user:pass"
     */
    public String getCredentials() {
        return credentials;
    }
}
