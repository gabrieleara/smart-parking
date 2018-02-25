package it.unipi.iot.parking.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import it.unipi.iot.parking.om2m.OM2M;
import it.unipi.iot.parking.om2m.SubscriptionServer;
import it.unipi.iot.parking.om2m.subscribers.NodeSubscriber;
import it.unipi.iot.parking.om2m.data.ContentInstance;

/**
 * Servlet implementation class BackgroundServlet
 */
@WebServlet(name = "parks-servlet", urlPatterns = { "/servlets/parks" }, loadOnStartup = 1)
public class ParkingServlet extends HttpServlet {
	// Used for the Serialization, let us just leave it here
	private static final long serialVersionUID = 1L;
	
	/**
	 * @see Servlet#init()“
	 */
	public void init() throws ServletException {
		// TODO: read from configuration file, one single configuration file for each
		// node kind
		OM2M om2m = OM2M.init("127.0.0.1", "5683", "admin:admin");
		
		SubscriptionServer cs = new SubscriptionServer(8023);
		cs.start();
		
		NodeSubscriber ns = new NodeSubscriber(cs);
		om2m.createSubscription("parking", ns.getFullURI());
	}
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter writer = response.getWriter();
		
		JSONObject responseObj = new JSONObject();
		
		OM2M conn = OM2M.getInstance();
		
		String[] uril = conn.getAllChildrenIDOfType("parking", OM2M.RESOURCE_TYPE_CONTENT_INSTANCE, new String[] { "lbl=type/manifest" });
		
		List<ContentInstance> parks = new ArrayList<>();
		
		for (String uri : uril) {
			parks.add(new ContentInstance(conn.getResourceFromID(uri, OM2M.RESOURCE_TYPE_CONTENT_INSTANCE)));
		}
		
		//ContentInstance[] parksInstances = parks.toArray(new ContentInstance[parks.size()]);
		JSONArray arr = new JSONArray();
		
		for(ContentInstance in : parks) {
			arr.put(in.getContentValue());
		}
		
		responseObj.put("parks", arr);
		
		writer.append(responseObj.toString(2));
		
		//response.getWriter().append("Served at: ").append(request.getContextPath());
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		// doGet(request, response);
	}
	
}
