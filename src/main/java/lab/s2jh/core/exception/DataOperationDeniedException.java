package lab.s2jh.core.exception;

/**
 * No right to data manipulation
 */
public class DataOperationDeniedException extends BaseRuntimeException{

    public DataOperationDeniedException() {
        super("Invalid data manipulation");
    }
    
    public DataOperationDeniedException(String msg) {
        super(msg);
    }

    public DataOperationDeniedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
