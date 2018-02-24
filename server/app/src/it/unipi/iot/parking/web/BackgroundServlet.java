package it.unipi.iot.parking.web;

import it.unipi.iot.parking.util.MNConfig; 
import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class BackgroundServlet
 */
@WebServlet(name = "servlet", urlPatterns={"/back/servlet"}, loadOnStartup = 1)
public class BackgroundServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
    public BackgroundServlet() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Servlet#init()“
	 */
	public void init() throws ServletException {
		// TODO Auto-generated method stub
		//System.out.println(MNConfig.APP_NAME);
		
		BackgroundThread t = new BackgroundThread();
		t.start();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
