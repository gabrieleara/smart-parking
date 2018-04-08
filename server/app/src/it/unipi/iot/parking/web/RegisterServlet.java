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
@WebServlet(name = "register-servlet", urlPatterns = { "/servlets/register" })
public class RegisterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RegisterServlet() {
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
            // Perform registration and store data
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
            
            String email = request.getParameter("email");
            if (email == null) {
                session.invalidate();
                response.getWriter()
                        .println("{\"err\":\"Please, fill email field.\"}");
                return;
            }
            
            String credit = request.getParameter("credit");
            if (credit == null) {
                session.invalidate();
                response.getWriter()
                        .println("{\"err\":\"Please, fill credit card field.\"}");
                return;
            }
            
            UserData data;
            
            try {
                data = ParksDataHandler.register(username, password, email, credit);
            } catch (OM2MException | TimeoutException e) {
                // Error performing register
                session.invalidate();
                response.getWriter()
                        .println("{ \"err\":\"Contact system administrator!\"}");
                return;
            }
            
            // Register failed (correctly)
            if (data == null) {
                session.invalidate();
                response.getWriter()
                        .println("{ \"err\":\"Already used username, please pick another!\"}");
                return;
            }
            
            session.setAttribute("userdata", data);
        } else {
            response.getWriter()
                    .println(
                            "{ \"err\":\"A session is already open, you can't register right now!\"}");
            return;
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
        doGet(request, response);
    }
    
}
