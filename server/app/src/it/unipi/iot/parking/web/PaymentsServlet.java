package it.unipi.iot.parking.web;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;

import it.unipi.iot.parking.ParksDataHandler;
import it.unipi.iot.parking.data.PaymentData;
import it.unipi.iot.parking.data.UserData;
import it.unipi.iot.parking.om2m.OM2MException;
import it.unipi.iot.parking.util.DateConverter;

/**
 * Servlet implementation class PaymentsServlet
 */
@WebServlet(name = "payments-servlet", urlPatterns = { "/servlets/payments" })
public class PaymentsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PaymentsServlet() {
        super();
    }
    
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Return user data
        HttpSession session = request.getSession(true);
        
        if (session.isNew()) {
            session.invalidate();
            // TODO: error
            return;
        }
        
        UserData user = (UserData) session.getAttribute("userdata");
        
        final String dateString;
        final Date date;
        
        dateString = request.getParameter("date");
        if (dateString == null) {
            response.getWriter()
                    .println("{\"err\":\"Please, provide a date.\"}");
            return;
        }
        
        try {
            date = DateConverter.fromString(dateString);
        } catch (RuntimeException e) {
            response.getWriter()
                    .println("{\"err\":\"Please, provide a valid date.\"}");
            return;
        }
        
        PaymentData[] payments;

        try {
            payments = ParksDataHandler.getPayments(user.getUsername(), date);
        } catch (OM2MException | TimeoutException e) {
            payments = new PaymentData[0];
        }
        
        final List<PaymentData> paymentsList;
        final List<JSONObject> paymentsJSONList;
        final JSONArray paymentsArray;
        
        paymentsList = Arrays.asList(payments);
        
        paymentsJSONList = paymentsList.stream()
                                .map(p -> p.toJSONObject())
                                .collect(Collectors.toList());
        
        paymentsArray = new JSONArray(paymentsJSONList);
        
        JSONObject obj = new JSONObject().put("payments", paymentsArray);
        
        response.getWriter().println(obj.toString());
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