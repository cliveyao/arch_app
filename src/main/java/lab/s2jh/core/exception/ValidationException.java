package lab.s2jh.core.exception;

/**
 * Business logic verification exceptions, such exceptions will not be subjected to 
 * conventional logger.error records generally only display prompts the user front end
 */
public class ValidationException extends BaseRuntimeException {

    private static final long serialVersionUID = -1613416718940821955L;

    public ValidationException(String errorCode, String message) {
        super(errorCode, message);
    }

    public ValidationException(String message) {
        super(message);
    }
}
