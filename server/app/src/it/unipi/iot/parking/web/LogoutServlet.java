package it.unipi.iot.parking.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class LoginServlet
 */
@WebServlet(name = "register-logout", urlPatterns = { "/servlets/logout" })
public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public LogoutServlet() {
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
        
        if (session.isNew())
            response.getWriter()
                    .println("{ \"err\":\"Logout failed! "
                            + "You need to be logged in to log out. Please log in to log out.\" }");
        
        session.invalidate();
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
