package it.unipi.iot.parking.web;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.unipi.iot.parking.ParksDataHandler;
import it.unipi.iot.parking.data.UserData;
import it.unipi.iot.parking.om2m.OM2MException;

/**
 * Servlet implementation class LoginServlet
 */
@WebServlet(name = "login-servlet", urlPatterns = { "/servlets/login" }, loadOnStartup = 2)
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LoginServlet() {
        super();
    }
    
    /**
     * @see Servlet#init()“
     */
    public void init() throws ServletException {
        new InitializeUsersThread().start();
    }
    
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // New session, however, the true argument retrieves a previously created
        // session if available
        HttpSession session = request.getSession(true);
        
        // Check if this is new comer on your web page.
        if (session.isNew()) {
            // Perform login and store it
            String username = request.getParameter("username");
            if (username == null) {
                session.invalidate();
                response.getWriter()
                        .println("{\"err\":\"Please, fill username field.\"}");
                return;
            }
            
            String password = request.getParameter("password");
            if (password == null) {
                session.invalidate();
                response.getWriter()
                        .println("{\"err\":\"Please, fill password field.\"}");
                return;
            }
            
            UserData data;
            
            try {
                data = ParksDataHandler.login(username, password);
            } catch (OM2MException | TimeoutException e) {
                // Error performing login
                session.invalidate();
                response.getWriter()
                        .println("{ \"err\":\"Contact system administrator!\"}");
                return;
            }
            
            // Login failed (correctly)
            if (data == null) {
                session.invalidate();
                response.getWriter()
                        .println(
                                "{ \"err\":\"Incorrect pair username:password, could not perform login!\"}");
                return;
            }
            
            session.setAttribute("userdata", data);
        }
        
        // Return user data
        UserData data = (UserData) session.getAttribute("userdata");
        
        response.getWriter()
                .println(data.toString());
    }
    
    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO Auto-generated method stub
        doGet(request, response);
    }
    
}
