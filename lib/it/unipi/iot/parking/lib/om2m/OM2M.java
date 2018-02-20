package it.unipi.iot.parking.lib.om2m;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.List;
import java.util.function.Predicate;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Option;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.coap.Request;
import org.json.JSONObject;

import it.unipi.iot.parking.lib.util.SimpleLevel;
import it.unipi.iot.parking.lib.util.SimpleLogger;

public class OM2M {
	private static final int		OPTION_CREDENTIALS		= 256;
	private static final int		OPTION_RESPONSE_CODE	= 265;
	private static final int		OPTION_RESOURCE_TYPE	= 267;
	
	private static final int		RESPONSE_CREATED		= 2001;
	private static final int		RESPONSE_CONTENT		= 2000;
	
	private static final int		RESOURCE_TYPE_AE		= 2;
	private static final int		RESOURCE_TYPE_CNT		= 3;
	private static final int		RESOURCE_TYPE_CIN		= 4;
	
	private static final String		RESOURCE_TYPE_STR_AE	= "ae";
	private static final String		RESOURCE_TYPE_STR_CNT	= "cnt";
	private static final String		RESOURCE_TYPE_STR_CIN	= "cin";
	
	protected static final String	ATTR_RESOURCE_TYPE		= "ty";
	protected static final String	ATTR_RESOURCE_ID		= "ri";
	protected static final String	ATTR_RESOURCE_NAME		= "rn";
	protected static final String	ATTR_PARENT_ID			= "pi";
	protected static final String	ATTR_CREATION_TIME		= "ct";
	protected static final String	ATTR_LAST_MOD_TIME		= "lt";
	protected static final String	ATTR_OLDEST				= "ol";
	protected static final String	ATTR_LATEST				= "la";
	protected static final String	ATTR_STATE_TAG			= "st";
	protected static final String	ATTR_APPLICATION_ID		= "api";
	protected static final String	ATTR_CONTENT			= "con";
	protected static final String	ATTR_CONTENT_INFO		= "cnf";
	protected static final String	ATTR_CONTENT_SIZE		= "cs";
	protected static final String	ATTR_RR					= "rr";		// I don't know what the
																		// hell this is
	protected static final boolean	ATTR_RR_CONTENT			= true;
	protected static final String	ATTR_CNF_CONTENT		= "message";
	
	private static final String		URI_PROTOCOL			= "coap";
	
	private final String			baseURI;
	private final String			credentials;
	
	private enum Operation {
		GET, POST, PUT, DELETE;
	}
	
	public OM2M(String host, String portNumber, String credentials) {
		this.baseURI = URI_PROTOCOL + "://" + host + ":" + portNumber + "/~";
		this.credentials = credentials;
	}
	
	public ApplicationEntity createApplicationEntity(String parentID, String appID, String name) {
		JSONObject object = new JSONObject();
		
		object	.put(ATTR_APPLICATION_ID, appID)
				.put(ATTR_RESOURCE_NAME, name)
				.put(ATTR_RR, true);
		
		JSONObject body = new JSONObject();
		body.put("m2m:" + RESOURCE_TYPE_STR_AE, object);
		
		CoapResponse response = performOperation(parentID, body, Operation.POST, RESOURCE_TYPE_AE);
		
		object = checkCreationSuccess(response, RESOURCE_TYPE_AE);
		
		return new ApplicationEntity(object);
	}
	
	public Container createContainer(String parentID, String name) {
		JSONObject object = new JSONObject();
		
		object.put(ATTR_RESOURCE_NAME, name);
		
		JSONObject body = new JSONObject();
		body.put("m2m:" + RESOURCE_TYPE_STR_CNT, object);
		
		CoapResponse response = performOperation(parentID, body, Operation.POST, RESOURCE_TYPE_CNT);
		
		object = checkCreationSuccess(response, RESOURCE_TYPE_CNT);
		
		return new Container(object);
	}
	
	public Content createContent(String parentID, String value) {
		JSONObject object = new JSONObject();
		
		object	.put(ATTR_CONTENT, value)
				.put(ATTR_CONTENT_INFO, ATTR_CNF_CONTENT);
		
		JSONObject body = new JSONObject();
		body.put("m2m:" + RESOURCE_TYPE_STR_CIN, object);
		
		CoapResponse response = performOperation(parentID, body, Operation.POST, RESOURCE_TYPE_CIN);
		
		object = checkCreationSuccess(response, RESOURCE_TYPE_CIN);
		
		return new Content(object);
	}
	
	private JSONObject checkCreationSuccess(CoapResponse response, int resourceType) {
		String responseBody = response.getResponseText();
		
		List<Option> list = response.getOptions()
									.asSortedList();
		
		Predicate<Option> predicate = opt -> opt.getNumber() == OPTION_RESPONSE_CODE;
		Option responseOption = list.stream()
									.filter(predicate)
									.findFirst()
									.orElse(null);
		
		if (responseOption == null) {
			String message = "Option RESPONSE couldn't be found! Response body was: "
					+ responseBody;
			assert false : message;
		}
		
		// TODO: 5042 service unavailable
		// TODO: 4105 name already present in parent collection
		// TODO: 4103 no ACP attached
		// TODO: 4004 Cannot find parent resource
		if (responseOption.getIntegerValue() != RESPONSE_CREATED) {
			String message = "Option RESPONSE status code was " + responseOption.getIntegerValue()
					+ ".\nResponse body was: `" + responseBody + "`";
			throw new RuntimeException(message);
		}
		
		SimpleLogger.log(SimpleLevel.VERBOSE, getClass().getName(), "Request has been accepted!");
		
		return (JSONObject) new JSONObject(responseBody).get(
				"m2m:" + getResourceString(resourceType));
		
	}
	
	private CoapResponse performOperation(String parentID, JSONObject body, Operation opcode,
			int resourceType) {
		return performOperation(parentID, body, opcode, resourceType, null);
	}
	
	private CoapResponse performOperation(String parentID, JSONObject body, Operation opcode,
			int resourceType, String uriQuery) {
		
		if (parentID == null)
			parentID = "/";
		
		if (!parentID.startsWith("/"))
			parentID = "/" + parentID;
		
		if (!parentID.endsWith("/"))
			parentID += "/";
		
		String containerLocation = baseURI + parentID;
		URI uri = null;
		
		try {
			uri = new URI(containerLocation);
		} catch (URISyntaxException ex) {
			throw new RuntimeException(ex);
		}
		
		SimpleLogger.log(SimpleLevel.VERBOSE, getClass().getName(),
				"Sending " + opcode + " request to URI `" + uri + "`");
		
		CoapClient client = new CoapClient(uri);
		
		Request request = null;
		
		switch (opcode) {
		case GET:
			request = Request.newGet();
			break;
		case POST:
			request = Request.newPost();
			break;
		case PUT:
			request = Request.newPut();
			break;
		case DELETE:
			request = Request.newDelete();
			break;
		default:
			assert false : "Bad request code!";
		}
		
		OptionSet options = request.getOptions();
		
		options	.addOption(new Option(OPTION_RESOURCE_TYPE, resourceType))
				.addOption(new Option(OPTION_CREDENTIALS, credentials))
				.setContentFormat(MediaTypeRegistry.APPLICATION_JSON)
				.setAccept(MediaTypeRegistry.APPLICATION_JSON);
		
		if (uriQuery != null && uriQuery.length() > 1)
			options.addUriQuery(uriQuery);
		
		request.setPayload(body.toString());
		
		SimpleLogger.log(SimpleLevel.VERBOSE, getClass().getName(),
				"Sending following payload: `" + body + "`");
		
		CoapResponse response = client.advanced(request);
		
		// TODO: check for NULLPOINTER if timed out!
		
		SimpleLogger.log(SimpleLevel.VERBOSE, getClass().getName(),
				"Received following response: " + response.getResponseText());
		
		return response;
	}
	
	private final String getResourceString(int resourceType) {
		switch (resourceType) {
		case RESOURCE_TYPE_AE:
			return RESOURCE_TYPE_STR_AE;
		case RESOURCE_TYPE_CNT:
			return RESOURCE_TYPE_STR_CNT;
		case RESOURCE_TYPE_CIN:
			return RESOURCE_TYPE_STR_CIN;
		default:
			throw new IllegalArgumentException("Unsupported resource type " + resourceType);
		}
	}
	
	public String getLastValues(String resourceID, int limit) {
		
		String uriQuery = "fu=1";
		//String uriQuery2 = "rty=" + RESOURCE_TYPE_CIN;// + "&ty=" + RESOURCE_TYPE_AE;
		
		if (resourceID == null)
			resourceID = "/";
		
		if (!resourceID.startsWith("/"))
			resourceID = "/" + resourceID;
		
		if (!resourceID.endsWith("/"))
			resourceID += "/";
		
		String containerLocation = baseURI + resourceID;// + uriQuery + "&" + uriQuery2;
		
		URI uri = null;
		
		try {
			uri = new URI(containerLocation);
			
			//uri = new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), uriQuery /*+ "&" + uriQuery2*/, uri.getFragment());
		} catch (URISyntaxException ex) {
			throw new RuntimeException(ex);
		}
		
		SimpleLogger.log(SimpleLevel.VERBOSE, getClass().getName(),
				"Sending " + Operation.GET + " request to URI `" + uri + "`");
		
		CoapClient client = new CoapClient(uri);
		
		Request request = Request.newGet();
		
		OptionSet options = request.getOptions();
		
		options // .addOption(new Option(OPTION_RESOURCE_TYPE, resourceType))
				.addOption(new Option(OPTION_CREDENTIALS, credentials))
				.setContentFormat(MediaTypeRegistry.APPLICATION_JSON)
				.setAccept(MediaTypeRegistry.APPLICATION_JSON);
		
		//if (uriQuery != null && uriQuery.length() > 1)
			options.addUriQuery(uriQuery);
			//options.addUriQuery(uriQuery2);
		
		CoapResponse response = client.advanced(request);
		
		SimpleLogger.log(SimpleLevel.VERBOSE, getClass().getName(),
				"Received following response: " + response.getResponseText());
		
		// CoapResponse response = performOperation(resourceID, new JSONObject(),
		// Operation.GET,
		// RESOURCE_TYPE_CNT, "ty=" + RESOURCE_TYPE_CIN + "&limit=" + limit + "&fu=1");
		// // 1 should be discovery
		
		// TODO: 2000 is OK for these requests
		
		return response.getResponseText();
	}
	
}
