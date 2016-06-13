package lab.s2jh.core.exception;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.core.NestedRuntimeException;

public abstract class BaseRuntimeException extends NestedRuntimeException {

    private static final long serialVersionUID = -23347847086757165L;

    private String errorCode;

    public BaseRuntimeException(String message) {
        super(message);
    }

    public BaseRuntimeException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BaseRuntimeException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public String getErrorCode() {
        return errorCode;
    }

 // Generates an exception serial number , to display the front-end user is appended to the error message , 
    //the user is given feedback to the operation and maintenance issues with this serial number or developers 
    //to quickly locate the corresponding specific exception details
    public static String buildExceptionCode() {
        return "ERR" + DateFormatUtils.format(new java.util.Date(), "yyMMddHHmmss") + RandomStringUtils.randomNumeric(3);
    }
}
