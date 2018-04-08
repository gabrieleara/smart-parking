package it.unipi.iot.parking.data;

import org.json.JSONObject;

public class UserData {
    private final static String STRING_ID       = "id";
    private final static String STRING_USERNAME = "username";
    private final static String STRING_EMAIL    = "email";
    private final static String STRING_CREDIT   = "credit";
    
    private final String id;
    private final String username;
    private final String email;
    //private final String password;
    private final String credit;
    
    public UserData(JSONObject data) {
        this.id = data.getString(STRING_ID);
        this.username = data.getString(STRING_USERNAME);
        this.email = data.getString(STRING_EMAIL);
        this.credit = data.getString(STRING_CREDIT);
    }
    
    public UserData(String id, String username, String email, String credit) {
        super();
        this.id = id;
        this.username = username;
        this.email = email;
        this.credit = credit;
    }
    
    public String getId() {
        return id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getEmail() {
        return email;
    }
    
    /*
    public String getPassword() {
        return password;
    }
    */
    
    public String getCredit() {
        return credit;
    }
    
    public JSONObject toJSONObject() {
        final JSONObject obj;
        
        obj = new JSONObject().put(STRING_ID, this.id)
                              .put(STRING_USERNAME, this.username)
                              .put(STRING_EMAIL, this.email)
                              .put(STRING_CREDIT, this.credit);
        return obj;
    }
    
    public String toString() {
        return toJSONObject().toString();
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UserData other = (UserData) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
    
}
