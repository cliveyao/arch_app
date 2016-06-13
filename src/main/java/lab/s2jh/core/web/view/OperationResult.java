/**
 * Copyright (c) 2012
 */
package lab.s2jh.core.web.view;

import javax.persistence.Access;
import javax.persistence.AccessType;

import lab.s2jh.core.annotation.MetaData;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * For Object to JSON serialized object structure definition
 */
@Getter
@Setter
@Accessors(chain = true)
@Access(AccessType.FIELD)
@JsonInclude(Include.NON_NULL)
public class OperationResult {

	// Global success identification code
    public final static String SUCCESS = "100000";

 // Global unknown error identification code
    public final static String FAILURE = "999999";

    /**Identifies the type of operation result */
    public enum OPERATION_RESULT_TYPE {
        @MetaData(value = "success", comments = "Handling success . Green tip is generally short-lived bubble tips")
        success,

        @MetaData(value = "warning", comments = "Occasionally it is used to identify business process is substantially complete , but there are some which need to pay attention on the message or data in the message. Usually the tip is yellow bubble tips")
        warning,

        @MetaData(value = "failure", comments = "Operation failed. Usually the tip is red for a long time or require users to actively turn off the bubble tips")
        failure,

        @MetaData(value = "confirm", comments = "The abort Submit feedback for user confirmation . Automatically initiates a request again after a tip will pop up for the general user 'OK' dialog boxes are presented , and then the user take the initiative to identify and skip validation checks subsequent business processes")
        confirm
    }

    /**Returns success or failure identification operation */
    private String type;

    /** Success: 100000 other identification error */
    private String code;

    /** Internationalization process returns JSON message body , for providing general failure error message */
    private String message;

    /**Supplementary service data */
    private Object data;

    /**Logo redirect path */
    private String redirect;

    public static OperationResult buildSuccessResult(String message, Object data) {
        return new OperationResult(OPERATION_RESULT_TYPE.success, message, data).setCode(SUCCESS);
    }

    public static OperationResult buildSuccessResult() {
        return new OperationResult(OPERATION_RESULT_TYPE.success, null).setCode(SUCCESS);
    }

    public static OperationResult buildSuccessResult(String message) {
        return new OperationResult(OPERATION_RESULT_TYPE.success, message).setCode(SUCCESS);
    }

    public static OperationResult buildSuccessResult(Object data) {
        return new OperationResult(OPERATION_RESULT_TYPE.success, "success", data).setCode(SUCCESS);
    }

    public static OperationResult buildWarningResult(String message, Object data) {
        return new OperationResult(OPERATION_RESULT_TYPE.warning, message, data).setCode(SUCCESS);
    }

    public static OperationResult buildFailureResult(String message) {
        return new OperationResult(OPERATION_RESULT_TYPE.failure, message).setCode(FAILURE);
    }

    public static OperationResult buildFailureResult(String message, Object data) {
        return new OperationResult(OPERATION_RESULT_TYPE.failure, message, data).setCode(FAILURE);
    }

    public static OperationResult buildConfirmResult(String message, Object data) {
        return new OperationResult(OPERATION_RESULT_TYPE.confirm, message, data).setCode(SUCCESS);
    }

    public static OperationResult buildConfirmResult(String message) {
        return new OperationResult(OPERATION_RESULT_TYPE.confirm, message, null).setCode(SUCCESS);
    }

    public OperationResult(OPERATION_RESULT_TYPE type, String message) {
        this.type = type.name();
        this.message = message;
    }

    public OperationResult(OPERATION_RESULT_TYPE type, String message, Object data) {
        this.type = type.name();
        this.message = message;
        this.data = data;
    }
}
