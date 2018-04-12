package it.unipi.iot.parking.om2m;

/**
 * 
 * @author Gabriele Ara
 *
 */
public class OM2MException extends RuntimeException {
    
    private static final long serialVersionUID = 1102877999050513127L;
    
    private ErrorCode code;
    
    public OM2MException(ErrorCode code) {
        super(code.toString());
        this.code = code;
    }
    
    public OM2MException(String message, ErrorCode code) {
        super(code.toString() + " " + message);
        this.code = code;
    }
    
    public OM2MException(String message, Throwable cause, ErrorCode code) {
        super(code.toString() + " " + message, cause);
        this.code = code;
    }
    
    public OM2MException(Throwable cause, ErrorCode code) {
        super(cause);
        this.code = code;
    }
    
    public ErrorCode getCode() {
        return this.code;
    }
    
}
