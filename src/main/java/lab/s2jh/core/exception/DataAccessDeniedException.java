package lab.s2jh.core.exception;

/**
 *Lack of access to data
 */
public class DataAccessDeniedException extends BaseRuntimeException{


    public DataAccessDeniedException() {
        super("Not entitled to access the data");
    }
    
    public DataAccessDeniedException(String msg) {
        super(msg);
    }

    public DataAccessDeniedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
