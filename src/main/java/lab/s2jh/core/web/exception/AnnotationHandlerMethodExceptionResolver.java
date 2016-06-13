package lab.s2jh.core.web.exception;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lab.s2jh.core.exception.BaseRuntimeException;
import lab.s2jh.core.exception.DuplicateTokenException;
import lab.s2jh.core.exception.ValidationException;
import lab.s2jh.core.web.util.ServletUtils;
import lab.s2jh.core.web.view.OperationResult;

import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import com.google.common.collect.Maps;

/**
 * Global exception parsing processor
 * Injection contentNegotiationManager, depending on the type of request is determined in response to construct a data format , such as JSON or JSP page
 */
public class AnnotationHandlerMethodExceptionResolver implements HandlerExceptionResolver, Ordered {

    private static final Logger logger = LoggerFactory.getLogger("lab.s2jh.errors");

    private ContentNegotiationManager contentNegotiationManager;

    public int getOrder() {
    	// Takes precedence
        return Integer.MIN_VALUE;
    }

    /**
     * <P> injection contentNegotiationManager, depending on the type of request is determined in response to construct a data format , such as JSON or JSP page <p>
     * <P> Depending on the type of exception , do some error message and friendly escaped , to distinguish between different control whether an exception is required logger logging <p>
     * <P> when the associated request data logger recording mode based MDC recorded for troubleshooting <p>
     */
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object aHandler, Exception e) {

        String errorMessage = null;
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;

        if (e instanceof HttpRequestMethodNotSupportedException) {
        	// HTTP request wrong way
            errorMessage = e.getMessage();
            httpStatus = HttpStatus.BAD_REQUEST;

            // This time not to the Controller method can not be judged based on ResponseBody annotation type of response , the judge based contentNegotiationManager
            try {
            	// First deal with a specific type of the corresponding
                ServletWebRequest webRequest = new ServletWebRequest(request);
                List<MediaType> mediaTypes = contentNegotiationManager.resolveMediaTypes(webRequest);
                for (MediaType mediaType : mediaTypes) {
                    // JSON response to the type of request
                    if (mediaType.equals(MediaType.APPLICATION_JSON)) {
                        ModelAndView mv = new ModelAndView();
                        MappingJackson2JsonView view = new MappingJackson2JsonView();
                        Map<String, Object> attributes = Maps.newHashMap();
                        attributes.put("type", OperationResult.OPERATION_RESULT_TYPE.failure);
                        attributes.put("code", OperationResult.FAILURE);
                        attributes.put("message", errorMessage);
                        attributes.put("exception", e.getMessage());
                        view.setAttributesMap(attributes);
                        mv.setView(view);
                        return mv;
                    }
                }
            } catch (HttpMediaTypeNotAcceptableException e1) {
                logger.error(e1.getMessage(), e1);
            }

        } else if (e instanceof UnauthenticatedException) {
        	// Not logged access to the login screen
            errorMessage = "Access requires login";
            httpStatus = HttpStatus.UNAUTHORIZED;
        } else if (e instanceof UnauthorizedException) {
            //访问受限或无权限访问，转向403提示页面
            errorMessage = "Unauthorized access";
            httpStatus = HttpStatus.FORBIDDEN;
        } else {

        	// Build and record -friendly and detailed error information and message
            // Generates an exception serial number , to display the front-end user is appended to the error message , the user is given feedback to the operation and maintenance issues with this serial number or developers to quickly locate the corresponding specific exception details
            String exceptionCode = BaseRuntimeException.buildExceptionCode();
             // Check mark some type of exception without calling logger object is written to the log
            boolean skipLog = false;
            String errorTitle = exceptionCode + ": ";
            errorMessage = errorTitle + "System operation errors, please contact the administrator !";

         // First determine clearly a subclass is terminated after another judge take precedence match
            boolean continueProcess = true;
            if (continueProcess) {
                DuplicateTokenException ex = parseSpecException(e, DuplicateTokenException.class);
                if (ex != null) {
                    continueProcess = false;
                    errorMessage = "请勿重复提交表单";
                    skipLog = true;
                }
            }

            if (continueProcess) {
            	// Check fails abnormal operations , direct feedback to check message
                ValidationException ex = parseSpecException(e, ValidationException.class);
                if (ex != null) {
                    continueProcess = false;
                    httpStatus = HttpStatus.BAD_REQUEST;
                    errorMessage = e.getMessage();
                    skipLog = true;
                }
            }

            if (continueProcess) {
            	// Framework defines the base class for abnormal operation
                BaseRuntimeException ex = parseSpecException(e, BaseRuntimeException.class);
                if (ex != null) {
                    continueProcess = false;
                    errorMessage = errorTitle + e.getMessage();
                }
            }

            if (continueProcess) {
            	// Some exception friendly database escaping , so that the front-end user can understand
                SQLException ex = parseSpecException(e, SQLException.class);
                if (ex != null) {
                    continueProcess = false;
                    String sqlMessage = ex.getMessage();
                    if (sqlMessage != null && (sqlMessage.indexOf("FK") > -1 || sqlMessage.startsWith("ORA-02292"))) {
                        errorMessage = "This data has been used in association :" + sqlMessage;
                        skipLog = true;
                    } else if (sqlMessage != null
                            && (sqlMessage.indexOf("Duplicate") > -1 || sqlMessage.indexOf("UNIQUE") > -1 || sqlMessage.startsWith("ORA-02292"))) {
                        errorMessage = "The only constraint violation :" + sqlMessage;
                        skipLog = true;
                    }
                }
            }

            if (!skipLog) {
            	// Logger to record the MDC model assembled string information
                MDC.setContextMap(ServletUtils.buildRequestInfoDataMap(request, true));
                logger.error(errorMessage, e);
                MDC.clear();
            } else {
                logger.debug(errorMessage, e);
            }
        }

     // Set error http status code is based on components such as jqGrid this code to identify the success of request processing
        response.setStatus(httpStatus.value());
     // The rest according to the standard error-page treatment
        request.setAttribute("javax.servlet.error.message", errorMessage);

        boolean json = false;
        if (aHandler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) aHandler;
            Method method = handlerMethod.getMethod();
            ResponseBody responseBody = method.getAnnotation(ResponseBody.class);
            if (responseBody != null) {
                json = true;
            }
        }

        if (json) {
            ModelAndView mv = new ModelAndView();
            MappingJackson2JsonView view = new MappingJackson2JsonView();
            Map<String, Object> attributes = Maps.newHashMap();
            attributes.put("type", OperationResult.OPERATION_RESULT_TYPE.failure);
            attributes.put("code", OperationResult.FAILURE);
            attributes.put("message", errorMessage);
            attributes.put("exception", e.getMessage());
            view.setAttributesMap(attributes);
            mv.setView(view);
            return mv;
        } else {
            if (httpStatus.equals(HttpStatus.UNAUTHORIZED)) {
            	// Record before the current request information directly to the login after the registration is completed URL
                WebUtils.saveRequest(request);
                String view = null;
                String path = request.getServletPath();
                if (path.startsWith("/admin")) {
                    view = "/admin/login";
                } else if (path.startsWith("/m")) {
                    view = "/m/login";
                } else {
                    view = "/w/login";
                }
                return new ModelAndView("redirect:" + view);
            }
        }

        return new ModelAndView("error/" + httpStatus.value());
    }

    /**
     * Take the current exception and recursively its root case examples , it is determined whether a sample or sub- class example of a particular type of exception
     * If it is then returned directly cast after abnormal sample , otherwise return null
     */
    @SuppressWarnings("unchecked")
    private <X> X parseSpecException(Exception e, Class<X> clazz) {
        if (clazz.isAssignableFrom(e.getClass())) {
            return (X) e;
        }
        Throwable cause = e.getCause();
        while (cause != null) {
            if (clazz.isAssignableFrom(cause.getClass())) {
                return (X) cause;
            }
            cause = cause.getCause();
        }
        return null;
    }

    public void setContentNegotiationManager(ContentNegotiationManager contentNegotiationManager) {
        this.contentNegotiationManager = contentNegotiationManager;
    }
}