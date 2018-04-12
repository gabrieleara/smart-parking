package it.unipi.iot.parking.data;

import java.util.Date;

import org.json.JSONObject;

import it.unipi.iot.parking.util.DateConverter;

public class PaymentData {
    // private final static String STRING_ID = "id";
    private final static String STRING_COST       = "cost";
    private final static String STRING_START_TIME = "startT";
    private final static String STRING_END_TIME   = "endT";
    
    // private final String id;
    private final double cost;
    private final Date   startTime;
    private final Date   endTime;
    
    public PaymentData(JSONObject data) {
        this.cost = data.getDouble(STRING_COST);
        this.startTime = DateConverter.fromString(data.getString(STRING_START_TIME));
        this.endTime = DateConverter.fromString(data.getString(STRING_END_TIME));
    }
    
    public PaymentData(double cost, Date startTime, Date endTime) {
        this.cost = cost;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    
    public double getCost() {
        return cost;
    }
    
    public Date getStartTime() {
        return startTime;
    }
    
    public Date getEndTime() {
        return endTime;
    }
    
    public JSONObject toJSONObject() {
        final JSONObject obj;
        
        obj = new JSONObject()// .put(STRING_ID, this.id)
                              .put(STRING_COST, this.cost)
                              .put(STRING_START_TIME, DateConverter.fromDate(this.startTime))
                              .put(STRING_END_TIME, DateConverter.fromDate(this.endTime));
        return obj;
    }
    
    public String toString() {
        return toJSONObject().toString();
    }
    
}
