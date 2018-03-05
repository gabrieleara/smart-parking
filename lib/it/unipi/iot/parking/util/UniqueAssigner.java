package it.unipi.iot.parking.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UniqueAssigner {
    // TODO: keep better track of what is assigned and what is not!
    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d+");
    
    private int counter = 0;
    
    private static final String num3char(int num) {
        String base = "";
        
        if (num < 100)
            base += "0";
        if (num < 10)
            base += "0";
        
        return base + num;
    }
    
    public void reserveName(String name) {
        Matcher matcher = DIGIT_PATTERN.matcher(name);
        
        if (!matcher.find())
            throw new IllegalArgumentException("The name must contain an integer value!");
        
        int id = Integer.parseInt(matcher.group());
        
        synchronized (this) {
            if (counter > id) {
                // throw new IllegalStateException("Cannot reserve specified id, already "
                // + "(potentially) assigned.");
                return;
            }
            
            counter = id + 1;
        }
    }
    
    public String assignName() {
        int num;
        
        synchronized (this) {
            num = counter++;
        }
        
        return num3char(num);
    }
    
}
