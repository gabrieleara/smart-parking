package it.unipi.iot.parking.web;

import java.util.concurrent.TimeoutException;

import it.unipi.iot.parking.ParksDataHandler;
import it.unipi.iot.parking.om2m.ErrorCode;
import it.unipi.iot.parking.om2m.OM2MException;

public class InitializeUsersThread extends Thread {
    public InitializeUsersThread() {
        super();
    }
    
    @Override
    public void run() {
        try {
            ParksDataHandler.initUsers();
        } catch (OM2MException e) {
            if (e.getCode() != ErrorCode.NAME_ALREADY_PRESENT)
                throw e;
        } catch (TimeoutException e) {
            throw new RuntimeException("IN node is not responding to requests!");
        }
    }
}
