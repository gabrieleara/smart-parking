package it.unipi.iot.parking.om2m;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.coap.Request;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.unipi.iot.parking.om2m.data.ApplicationEntity;
import it.unipi.iot.parking.om2m.data.Container;
import it.unipi.iot.parking.om2m.data.ContentInstance;
import it.unipi.iot.parking.om2m.data.OM2MResource;
import it.unipi.iot.parking.om2m.data.Subscription;

/**
 * 
 * @author Gabriele Ara
 * 
 *         TODO: the whole documentation is kinda wrong
 */
public class OM2M {
    /*
     * -----------------------------------------------------------------------------
     * CONST:
     * -----------------------------------------------------------------------------
     */
    
    /**
     * Standard logger object.
     */
    private static final Logger LOGGER = Logger.getLogger(OM2M.class.getName());
    
    static { 
        LOGGER.setLevel(Level.OFF);
    }

    
    /*
     * -----------------------------------------------------------------------------
     * STATIC:
     * -----------------------------------------------------------------------------
     */
    
    /*
     * -----------------------------------------------------------------------------
     * ATTRIBUTES:
     * -----------------------------------------------------------------------------
     */
    
    /**
     * The data that will be used to connect to the OM2M Node.
     * 
     * @see OM2MSession
     */
    private final OM2MSession sessionData;
    
    /*
     * -----------------------------------------------------------------------------
     * PUBLIC:
     * -----------------------------------------------------------------------------
     */
    
    /**
     * Creates a new OM2M object, ready to be used to perform queries.
     * 
     * @param sessionData
     *            the data that will be used to connect to the OM2M Node
     * @see OM2MSession
     */
    public OM2M(final OM2MSession sessionData) {
        this.sessionData = sessionData;
    }
    
    /**
     * Creates a new {@link ApplicationEntity} in the specified parent, using the
     * given parameters. Required parameters are (in this order):
     * <ul>
     * <li>{@code appID} - the application id associated to the new
     * ApplicationEntity
     * <li>{@code name} - the resource name associated to the new ApplicationEntity
     * </ul>
     * 
     * Optionally, additional string parameters can be given. Each of them will be
     * treated as a label associated with the new ApplicationEntity.
     * 
     * @param parentID
     *            the parent resource of the new ApplicationEntity
     * @param parameters
     *            the list of parameters needed to create the new ApplicationEntity
     * @return the newly created ApplicationEntity
     * @throws TimeoutException
     * @throws OM2MException
     */
    public ApplicationEntity createApplicationEntity(final String parentID, final String appID,
            final String name, final String[] labels) throws TimeoutException, OM2MException {
        
        JSONObject requestObject = constructAEObject(appID, name, labels);
        
        return (ApplicationEntity) create(parentID, OM2MConstants.RESOURCE_TYPE_APPLICATION_ENTITY,
                requestObject);
    }
    
    /**
     * Creates a new {@link Container} in the specified parent, using the given
     * parameters. Required parameters are (in this order):
     * <ul>
     * <li>{@code name} - the resource name associated to the new Container
     * </ul>
     * 
     * Optionally, additional string parameters can be given. Each of them will be
     * treated as a label associated with the new Container.
     * 
     * @param parentID
     *            the parent resource of the new Container
     * @param parameters
     *            the list of parameters needed to create the new Container
     * @return the newly created Container
     * @throws TimeoutException
     * @throws OM2MException
     */
    public Container createContainer(final String parentID, final String name,
            final String[] labels) throws TimeoutException, OM2MException {
        
        JSONObject requestObject = constructCNTObject(name, labels);
        
        return (Container) create(parentID, OM2MConstants.RESOURCE_TYPE_CONTAINER, requestObject);
    }
    
    /**
     * Creates a new {@link ContentInstance} in the specified parent, using the
     * given parameters. Required parameters are (in this order):
     * <ul>
     * <li>{@code value} - the value associated with the new ContentInstance,
     * represented as the String serialization of a {@link JSONObject}
     * </ul>
     * 
     * Optionally, additional string parameters can be given. Each of them will be
     * treated as a label associated with the new ContentInstance.
     * 
     * @param parentID
     *            the parent resource of the new ContentInstance
     * @param parameters
     *            the list of parameters needed to create the new ContentInstance
     * @return the newly created ContentInstance
     * @throws TimeoutException
     * @throws OM2MException
     */
    public ContentInstance createContentInstance(final String parentID, final String value,
            final String[] labels) throws TimeoutException, OM2MException {
        
        JSONObject requestObject = constructCIObject(value, labels);
        
        return (ContentInstance) create(parentID, OM2MConstants.RESOURCE_TYPE_CONTENT_INSTANCE,
                requestObject);
    }
    
    public ContentInstance createContentInstance(final String parentID, final String name,
            final String value, final String[] labels) throws TimeoutException, OM2MException {
        
        JSONObject requestObject = constructCIObject(value, name, labels);
        
        return (ContentInstance) create(parentID, OM2MConstants.RESOURCE_TYPE_CONTENT_INSTANCE,
                requestObject);
    }
    
    /**
     * Creates a new {@link Subscription} in the specified parent, using the given
     * parameters. Required parameters are (in this order):
     * <ul>
     * <li>{@code subscriberURI} - the URI at which the new subscriber will listen
     * for updates
     * </ul>
     * 
     * Optionally, additional string parameters can be given. Each of them will be
     * treated as a label associated with the new Subscription.
     * 
     * <p>
     * <b>NOTICE:</b> the subscriber shall already be listening for updates on a
     * different thread, since the OM2M Node will contact it immediately before
     * creating the new Subscription object.
     * 
     * @param parentID
     *            the parent resource of the new Subscription
     * @param parameters
     *            the list of parameters needed to create the new Subscription
     * @return the newly created Subscription
     * @throws TimeoutException
     * @throws OM2MException
     */
    public Subscription createSubscription(final String parentID, final String subscriberURI,
            final String[] labels) throws TimeoutException, OM2MException {
        
        JSONObject requestObject = constructSUBObject(subscriberURI, labels);
        
        return (Subscription) create(parentID, OM2MConstants.RESOURCE_TYPE_SUBSCRIPTION,
                requestObject);
    }
    
    public OM2MResource create(final String parentID, final int resourceType,
            final String... parameters) throws TimeoutException, OM2MException {
        final JSONObject requestObject;
        
        requestObject = constructRequestObject(resourceType, parameters);
        
        return create(parentID, resourceType, requestObject);
    }
    
    /**
     * Creates a new {@link OM2MResource} as a direct child of the given parent
     * identifier. The actual content of the resource and the number of parameters
     * varies depending on the value of the {@code resourceType} parameter. See
     * related methods to check the actual behavior.
     * 
     * @param parentID
     *            the resource where the new copy shall be appended to
     * @param original
     *            the resource that shall be copied
     * @return the newly created copy
     * @throws TimeoutException
     * @throws OM2MException
     * 
     * @see #createApplicationEntity(String, String...)
     * @see #createContainer(String, String...)
     * @see #createContentInstance(String, String...)
     * @see #createSubscription(String, String...)
     * 
     */
    private OM2MResource create(final String parentID, final int resourceType,
            final JSONObject requestObject) throws TimeoutException, OM2MException {
        final JSONObject requestBody, responseObject;
        final CoapResponse response;
        
        requestBody = new JSONObject();
        requestBody.put(OM2MConstants.getFullResourceString(resourceType), requestObject);
        
        LOGGER.info("Creating a new " + OM2MConstants.getResourceString(resourceType)
                + " with following data: `" + requestBody + "`");
        
        response = performPost(parentID, requestBody, resourceType);
        
        responseObject = (JSONObject) getResponsePayload(response, resourceType,
                OM2MConstants.RESPONSE_CREATED);
        
        return OM2MResource.fromJSONObject(responseObject);
    }
    
    /**
     * Creates a copy of a given {@link OM2MResource} as a direct child of the given
     * parent identifier. The copy will have the same labels and relevant attributes
     * as the original resource, but children won't be copied as well.
     * 
     * @param parentID
     *            the resource where the new copy shall be appended to
     * @param original
     *            the resource that shall be copied
     * @return the newly created copy
     * @throws TimeoutException
     * @throws OM2MException
     * 
     */
    /*public OM2MResource createCopy(final String parentID, final OM2MResource original)
            throws TimeoutException, OM2MException {
        return createCopy(parentID, original, new String[] {});
    }*/
    /*
    public OM2MResource createCopy(final String parentID, final OM2MResource original,
            final String[] labels) throws TimeoutException, OM2MException {
        final String[] copyParameters = original.getCopyParameters();
        final String[] parameters = new String[copyParameters.length + labels.length];
        
        System.arraycopy(copyParameters, 0, parameters, 0, copyParameters.length);
        System.arraycopy(labels, 0, parameters, copyParameters.length, labels.length);
        
        return create(parentID, original.getResourceType(), parameters);
    }
    */
    /**
     * Performs a remote request for the resource identified by the given ID.
     * 
     * @param resourceID
     * @return the requested resource, if it exists, null otherwise
     * @throws TimeoutException
     * @throws OM2MException
     */
    public OM2MResource get(final String resourceID) throws TimeoutException, OM2MException {
        final CoapResponse response = performGet(concatURIs(resourceID));
        return OM2MResource.fromJSONObject(
                (JSONObject) getResponsePayload(response, -1, OM2MConstants.RESPONSE_OK));
    }
    
    public void delete(final String resourceID) throws TimeoutException, OM2MException {
        final CoapResponse response;
        
        response = performDelete(concatURIs(resourceID));
        
        getResponsePayload(response, -1, OM2MConstants.RESPONSE_ACCEPTED);
    }
    
    public String[] discovery(String resourceID) throws TimeoutException, OM2MException {
        return discovery(resourceID, new String[] {});
    }
    
    /**
     * Performs a remote request for the resource identified by the given ID.
     * 
     * @param resourceID
     * @return the requested resource, if it exists, null otherwise
     * @throws TimeoutException
     * @throws OM2MException
     */
    public String[] discovery(final String resourceID, final String[] uriQueries)
            throws TimeoutException, OM2MException {
        final String[] queries;
        final CoapResponse response;
        final JSONArray juril;
        final String[] uril;
        
        queries = new String[uriQueries.length + 1];
        
        queries[0] = "fu=1";
        System.arraycopy(uriQueries, 0, queries, 1, uriQueries.length);
        
        response = performGet(resourceID, queries);
        
        juril = (JSONArray) getResponsePayload(response, OM2MConstants.RESOURCE_TYPE_URIL,
                OM2MConstants.RESPONSE_OK);
        
        uril = new String[juril.length()];
        
        int i = 0;
        for (Object uri : juril) {
            if (uri instanceof String) {
                uril[i++] = (String) uri;
            } else
                throw new RuntimeException(
                        "Expected a JSONArray made of Strings, but found something else! "
                                + "One of the objects was actually an instance of " + uri.getClass()
                                                                                         .getName()
                                + ".");
        }
        
        return uril;
    }
    
    /*
     * -----------------------------------------------------------------------------
     * PROTECTED:
     * -----------------------------------------------------------------------------
     */
    
    /**
     * Dispatches the request to the appropriate method, depending on the
     * resourceType given as parameter.
     * 
     * @param resourceType
     *            the type of the resource to be created
     * @param parameters
     *            the parameters that shall be used to create it, varies depending
     *            on the resourceType, see each corresponding related method
     * @return the object that shall be put in message payload to create a new
     *         resource
     * 
     * @see #constructAEObject(String...)
     * @see #constructCNTObject(String...)
     * @see #constructCIObject(String...)
     * @see #constructSUBObject(String...)
     * 
     */
    protected JSONObject constructRequestObject(final int resourceType,
            final String... parameters) {
        final JSONObject requestObject;
        
        switch (resourceType) {
        case OM2MConstants.RESOURCE_TYPE_APPLICATION_ENTITY:
            requestObject = constructAEObject(parameters);
            break;
        case OM2MConstants.RESOURCE_TYPE_CONTAINER:
            requestObject = constructCNTObject(parameters);
            break;
        case OM2MConstants.RESOURCE_TYPE_CONTENT_INSTANCE:
            requestObject = constructCIObject(parameters);
            break;
        case OM2MConstants.RESOURCE_TYPE_SUBSCRIPTION:
            requestObject = constructSUBObject(parameters);
            break;
        default:
            throw new UnsupportedOperationException(
                    "The given resource type is not supported yet!");
        }
        
        return requestObject;
    }
    
    protected JSONObject constructAEObject(final String... parameters) {
        if (parameters.length < 2) {
            throw new IllegalArgumentException(
                    "At least two parameters are required, the App ID and the Resource Name.");
        }
        
        final String[] labels = Arrays.copyOfRange(parameters, 2, parameters.length);
        
        return constructAEObject(parameters[0], parameters[1], labels);
    }
    
    protected JSONObject constructAEObject(final String appID, final String name,
            final String[] labels) {
        final JSONObject requestObject;
        
        requestObject = new JSONObject();
        requestObject.put(OM2MConstants.ATTR_APPLICATION_ID, appID)
                     .put(OM2MConstants.ATTR_RESOURCE_NAME, name)
                     .put(OM2MConstants.ATTR_REQUEST_REACHABILITY,
                             OM2MConstants.ATTR_REQUEST_REACHABILITY_VALUE)
                     .put(OM2MConstants.ATTR_LABELS, labels);
        
        return requestObject;
    }
    
    protected JSONObject constructCNTObject(final String... parameters) {
        if (parameters.length < 1) {
            throw new IllegalArgumentException(
                    "At least one parameter is required, the Resource Name.");
        }
        
        final String[] labels = Arrays.copyOfRange(parameters, 1, parameters.length);
        
        return constructCNTObject(parameters[0], labels);
    }
    
    protected JSONObject constructCNTObject(final String name, final String[] labels) {
        final JSONObject requestObject;
        
        requestObject = new JSONObject();
        requestObject.put(OM2MConstants.ATTR_RESOURCE_NAME, name)
                     .put(OM2MConstants.ATTR_LABELS, labels);
        
        return requestObject;
    }
    
    protected JSONObject constructCIObject(final String... parameters) {
        if (parameters.length < 1) {
            throw new IllegalArgumentException("At least one parameter is required, the value.");
        }
        
        final String[] labels = Arrays.copyOfRange(parameters, 1, parameters.length);
        
        return constructCIObject(parameters[0], labels);
    }
    
    protected JSONObject constructCIObject(final String value, final String[] labels) {
        final JSONObject requestObject;
        
        requestObject = new JSONObject();
        requestObject.put(OM2MConstants.ATTR_CONTENT, value)
                     .put(OM2MConstants.ATTR_CONTENT_INFO, OM2MConstants.ATTR_CONTENT_INFO_VALUE)
                     .put(OM2MConstants.ATTR_LABELS, labels);
        
        return requestObject;
    }
    
    protected JSONObject constructCIObject(final String value, final String name,
            final String[] labels) {
        final JSONObject requestObject;
        
        requestObject = new JSONObject();
        requestObject.put(OM2MConstants.ATTR_CONTENT, value)
                     .put(OM2MConstants.ATTR_RESOURCE_NAME, name)
                     .put(OM2MConstants.ATTR_CONTENT_INFO, OM2MConstants.ATTR_CONTENT_INFO_VALUE)
                     .put(OM2MConstants.ATTR_LABELS, labels);
        
        return requestObject;
    }
    
    protected JSONObject constructSUBObject(final String... parameters) {
        if (parameters.length < 1) {
            throw new IllegalArgumentException(
                    "At least one parameter is required, the subscriber URI.");
        }
        
        final String[] labels = Arrays.copyOfRange(parameters, 1, parameters.length);
        
        return constructSUBObject(parameters[0], labels);
    }
    
    protected JSONObject constructSUBObject(final String subscriberURI, final String[] labels) {
        final JSONObject requestObject;
        
        requestObject = new JSONObject();
        requestObject.put(OM2MConstants.ATTR_RESOURCE_NAME,
                OM2MConstants.ATTR_RESOURCE_NAME_SUBSCRIBER)
                     .put(OM2MConstants.ATTR_NOTIFICATION_URI, subscriberURI)
                     .put(OM2MConstants.ATTR_NOTIFICATION_CONTENT_TYPE,
                             OM2MConstants.ATTR_NCT_VALUE)
                     .put(OM2MConstants.ATTR_LABELS, labels);
        
        return requestObject;
    }
    
    /*
     * -----------------------------------------------------------------------------
     * PRIVATE:
     * -----------------------------------------------------------------------------
     */
    
    /**
     * Performs a POST operation, used to create a new resource under the parentID
     * resource. The new resource content depends on the body argument, while its
     * type is given as resourceType argument.
     * 
     * @param parentID
     *            the parent of the new resource, it may be a resource ID or a path
     *            formed by resource names IF the resource to be created will reside
     *            on the OM2M node that will receive this request
     * @param body
     *            the content of the new resource to be created
     * @param resourceType
     *            the type of the new resource to be created
     * @return the response of the OM2M that receives this request
     * 
     * @throws RuntimeException
     *             if the given resourceID cannot be used to create a valid URI
     */
    private CoapResponse performPost(String parentID, JSONObject body, int resourceType) {
        Request request = Request.newPost();
        
        request.setURI(getURIFromID(parentID));
        
        LOGGER.info("Sending POST request to URI `" + request.getURI() + "`");
        
        OptionSet options = request.getOptions();
        
        options.addOption(new Option(OM2MConstants.OPTION_RESOURCE_TYPE, resourceType))
               .addOption(
                       new Option(OM2MConstants.OPTION_CREDENTIALS, sessionData.getCredentials()))
               .setContentFormat(MediaTypeRegistry.APPLICATION_JSON)
               .setAccept(MediaTypeRegistry.APPLICATION_JSON);
        
        request.setPayload(body.toString());
        
        LOGGER.info("Sending following payload: `" + body + "`");
        
        return performRequest(request);
    }
    
    /**
     * The same as calling {@link #performGet(String, String[])} with an empty array
     * as second parameter.
     * 
     * @see #performGet(String, String[])
     */
    private CoapResponse performGet(String resourceID) {
        return performGet(resourceID, new String[0]);
    }
    
    /**
     * Performs a GET request, to retrieve a resource from the given OM2M node. The
     * request can be a simple one if no uriQueries are given as argument or a
     * discovery.
     * 
     * @param resourceID
     *            the parent element on which the get shall be applied
     * @param uriQueries
     *            queries used to perform a discovery request
     * @return the CoAP response if any, otherwise null if the request timed out
     */
    private CoapResponse performGet(String resourceID, String[] uriQueries) {
        Request request = Request.newGet();
        
        request.setURI(getURIFromID(resourceID));
        
        LOGGER.info("Sending GET request to URI `" + request.getURI() + "`");
        
        OptionSet options = request.getOptions();
        
        options.addOption(
                new Option(OM2MConstants.OPTION_CREDENTIALS, sessionData.getCredentials()))
               .setContentFormat(MediaTypeRegistry.APPLICATION_JSON)
               .setAccept(MediaTypeRegistry.APPLICATION_JSON);
        
        for (String s : uriQueries)
            options.addUriQuery(s);
        
        return performRequest(request);
    }
    
    private CoapResponse performDelete(String resourceID) {
        Request request = Request.newDelete();
        
        request.setURI(getURIFromID(resourceID));
        
        LOGGER.info("Sending DELETE request to URI `" + request.getURI() + "`");
        
        OptionSet options = request.getOptions();
        
        options.addOption(
                new Option(OM2MConstants.OPTION_CREDENTIALS, sessionData.getCredentials()))
               .setContentFormat(MediaTypeRegistry.APPLICATION_JSON)
               .setAccept(MediaTypeRegistry.APPLICATION_JSON);
        
        return performRequest(request);
    }
    
    /**
     * Performs a CoAP Request.
     * 
     * @param request
     *            the request
     * @return the response, if any, null if the request timed out
     */
    private CoapResponse performRequest(Request request) {
        CoapClient client = new CoapClient();
        
        client.setTimeout(OM2MSession.TIMEOUT);
        
        return client.advanced(request);
    }
    
    /**
     * Returns the absolute URI of the resource identified by the resourceID.
     * 
     * @param resourceID
     *            can be either a resource ID or a path formed by resource names IF
     *            the resource to be created will reside on the OM2M node that will
     *            receive this request.
     * @return the absolute URI of the resource
     * 
     * @throws RuntimeException
     *             if the given resourceID cannot be used to create a valid URI
     */
    private URI getURIFromID(String resourceID) {
        // Concatenates the baseURI used for each request with resourceID
        String containerLocation = concatURIs(sessionData.getBaseURI(), resourceID);
        URI uri = null;
        
        // Creates the URI object
        try {
            uri = new URI(containerLocation);
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
        
        return uri;
    }
    
    /**
     * Concatenates multiple URIs given as arguments, the URIs may start or with a
     * '/' character, but not finish with one. The final URI will not have a '/' as
     * final character.
     * 
     * @param strings
     * @return
     */
    private String concatURIs(String... strings) {
        StringBuffer buffer = new StringBuffer();
        int oldSize;
        
        for (String s : strings) {
            if (s == null || s.length() < 1)
                continue;
            
            oldSize = buffer.length();
            
            buffer.append(s);
            
            while (oldSize < buffer.length() && buffer.charAt(oldSize) == '/')
                buffer.deleteCharAt(oldSize);
            
            buffer.append('/');
        }
        
        while (buffer.charAt(buffer.length() - 1) == '/')
            buffer.deleteCharAt(buffer.length() - 1);
        
        return buffer.toString();
    }
    
    /**
     * Checks whether the request that has been performed as terminated
     * successfully.
     * 
     * @param response
     * @param resourceType
     * @param successCode
     * @return
     * @throws TimeoutException
     * @throws OM2MException
     */
    private Object getResponsePayload(final CoapResponse response, int resourceType,
            final int successCode) throws TimeoutException, OM2MException {
        if (response == null) {
            LOGGER.severe("The response timed out! The node may be unreachable. ");
            LOGGER.severe("Throwing a TimeoutException...");
            throw new TimeoutException("The response timed out! The node may be unreachable.");
        }
        
        String responseBody = response.getResponseText();
        
        LOGGER.info("Response body is `" + responseBody + "`");
        
        List<Option> list = response.getOptions()
                                    .asSortedList();
        
        Predicate<Option> predicate = opt -> opt.getNumber() == OM2MConstants.OPTION_RESPONSE_CODE;
        Option responseOption = list.stream()
                                    .filter(predicate)
                                    .findFirst()
                                    .orElse(null);
        
        if (responseOption == null) {
            LOGGER.severe("The given response didn't have any RESPONSE option.");
            LOGGER.severe("Throwing a OM2MException...");
            
            String message = "Response body was: " + responseBody;
            
            throw new OM2MException(message, ErrorCode.fromWebCode(0));
        }
        
        if (responseOption.getIntegerValue() != successCode) {
            String message = "Option RESPONSE status code was " + responseOption.getIntegerValue()
                    + ", but the expected one was " + successCode + "!";
            LOGGER.severe(message);
            LOGGER.severe("Throwing a OM2MException...");
            
            message = "Response body was: " + responseBody;
            
            throw new OM2MException(message,
                    ErrorCode.fromWebCode(responseOption.getIntegerValue()));
        }
        
        LOGGER.info("Request has been accepted!");
        
        if (successCode == OM2MConstants.RESPONSE_ACCEPTED
                || successCode == OM2MConstants.RESPONSE_NO_CONTENT)
            return null;
        
        final JSONObject object = new JSONObject(responseBody);
        
        if (resourceType >= 0) {
            // Parse directly
            try {
                return object.get(OM2MConstants.getFullResourceString(resourceType));
            } catch (JSONException e) {
                throw new OM2MException("Bad response! Response body was: " + responseBody, e,
                        ErrorCode.OTHER);
            }
        }
        
        try {
            return OM2MResource.fromEnclosedJSONObject(object);
        } catch (OM2MException e) {
            throw new OM2MException("Bad response! Response body was: " + responseBody, e,
                    ErrorCode.OTHER);
        }
    }
}
