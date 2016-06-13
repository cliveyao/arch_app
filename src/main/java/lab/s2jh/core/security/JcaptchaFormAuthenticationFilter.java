package lab.s2jh.core.security;

import java.util.Date;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lab.s2jh.aud.entity.UserLogonLog;
import lab.s2jh.core.security.SourceUsernamePasswordToken.AuthSourceEnum;
import lab.s2jh.core.util.DateUtils;
import lab.s2jh.core.util.IPAddrFetcher;
import lab.s2jh.core.util.UidUtils;
import lab.s2jh.core.web.captcha.ImageCaptchaServlet;
import lab.s2jh.module.auth.entity.User;
import lab.s2jh.module.auth.entity.UserExt;
import lab.s2jh.module.auth.service.UserService;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

public class JcaptchaFormAuthenticationFilter extends FormAuthenticationFilter {

    private static final Logger logger = LoggerFactory.getLogger(JcaptchaFormAuthenticationFilter.class);

    public static final Integer LOGON_FAILURE_LIMIT = 2;

    /**
    * Authentication failed to reach the number of restrictions , passed flags property , 
    * the login screen displays the code input
    */
    public static final String KEY_AUTH_CAPTCHA_REQUIRED = "auth_captcha_required";

    /**
    * Record the user name information entered by the user for the login screen echo
    */
    public static final String KEY_AUTH_USERNAME_VALUE = "auth_username_value";

    /**
    * The default parameter name codes
    */
    public static final String DEFAULT_VALIDATE_CODE_PARAM = "captcha";

    // Parameter name codes
    private String captchaParam = DEFAULT_VALIDATE_CODE_PARAM;

    private UserService userService;

    /**
     * URL is forced toward the designated successUrl, automatically save login before ignored
     */
    private boolean forceSuccessUrl = false;

    private boolean isMobileAppAccess(ServletRequest request) {

    	// Get the device ID identifies
        String uuid = request.getParameter("uuid");
        return StringUtils.isNotBlank(uuid);
    }

    protected AuthenticationToken createToken(String username, String password, ServletRequest request, ServletResponse response) {
        boolean rememberMe = isRememberMe(request);
        String host = getHost(request);
        SourceUsernamePasswordToken token = new SourceUsernamePasswordToken(username, password, rememberMe, host);
        String source = request.getParameter("source");

     // Get the device ID identifies
        String uuid = request.getParameter("uuid");
        token.setUuid(uuid);
        if (StringUtils.isNotBlank(source)) {
            token.setSource(Enum.valueOf(AuthSourceEnum.class, source));
        } else {
            if (isMobileAppAccess(request)) {
                token.setSource(AuthSourceEnum.P);
            }
        }
        return token;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        if (isLoginRequest(request, response)) {
            if (isLoginSubmission(request, response)) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Login submission detected.  Attempting to execute login.");
                }
                return executeLogin(request, response);
            } else {
                if (logger.isTraceEnabled()) {
                    logger.trace("Login page view.");
                }
                //allow them to see the login page ;)
                return true;
            }
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("Attempting to access a path which requires authentication.  Forwarding to the " + "Authentication url ["
                        + getLoginUrl() + "]");
            }

            if (isMobileAppAccess(request)) {
                HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
                return true;
            } else {
                saveRequestAndRedirectToLogin(request, response);
                return false;
            }
        }
    }

    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        SourceUsernamePasswordToken token = (SourceUsernamePasswordToken) createToken(request, response);
        try {
            String username = getUsername(request);
         // Write login account name for echo
            request.setAttribute(KEY_AUTH_USERNAME_VALUE, username);

            User authAccount = userService.findByAuthTypeAndAuthUid(User.AuthTypeEnum.SYS, username);
            if (authAccount != null) {


            	// Fail LOGON_FAILURE_LIMIT times , mandatory CAPTCHA
                if (authAccount.getLogonFailureTimes() > LOGON_FAILURE_LIMIT) {
                    String captcha = request.getParameter(captchaParam);
                    if (StringUtils.isBlank(captcha) || !ImageCaptchaServlet.validateResponse((HttpServletRequest) request, captcha)) {
                        throw new CaptchaValidationException("Incorrect verification code");
                    }
                }

                Subject subject = getSubject(request, response);
                subject.login(token);
                return onLoginSuccess(token, subject, request, response);
            } else {
                return onLoginFailure(token, new UnknownAccountException("Login name or password is incorrect"), request, response);
            }
        } catch (AuthenticationException e) {
            return onLoginFailure(token, e, request, response);
        }
    }

    /**
    * Override the parent class method , when the number of failed login greater than allowLoginNum
    *  ( Allow logon times ) , it will display a verification code
    */
    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        if (e instanceof CaptchaValidationException) {
            request.setAttribute(KEY_AUTH_CAPTCHA_REQUIRED, Boolean.TRUE);
        } else if (e instanceof IncorrectCredentialsException) {

        	// Message -friendly tips
            e = new IncorrectCredentialsException("Login name or password is incorrect");

         // Failure record
            SourceUsernamePasswordToken sourceUsernamePasswordToken = (SourceUsernamePasswordToken) token;
            User user = userService.findByAuthTypeAndAuthUid(User.AuthTypeEnum.SYS, sourceUsernamePasswordToken.getUsername());
            if (user != null) {
                UserExt userExt = user.getUserExt();
                userExt.setLogonTimes(userExt.getLogonTimes() + 1);
                userExt.setLastLogonFailureTime(DateUtils.currentDate());
                userService.saveExt(userExt);

                user.setLogonFailureTimes(user.getLogonFailureTimes() + 1);
                userService.save(user);


             // Reached the limit number of authentication failures , passed flags property , the login screen displays the code input
                if (user.getLogonFailureTimes() > LOGON_FAILURE_LIMIT) {
                    request.setAttribute(KEY_AUTH_CAPTCHA_REQUIRED, Boolean.TRUE);
                }
            }
        }
        return super.onLoginFailure(token, e, request, response);
    }

    /**
    * Override the parent class method , when the login is successful , reset the failure flag
    */
    @Override
    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request, ServletResponse response) throws Exception {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;

        SourceUsernamePasswordToken sourceUsernamePasswordToken = (SourceUsernamePasswordToken) token;
        User authAccount = userService.findByAuthTypeAndAuthUid(User.AuthTypeEnum.SYS, sourceUsernamePasswordToken.getUsername());
        Date now = DateUtils.currentDate();


     // After updating Access Token, and set to expire six months
        if (StringUtils.isBlank(authAccount.getAccessToken()) || authAccount.getAccessTokenExpireTime().before(now)) {
            authAccount.setAccessToken(UidUtils.UID());
            authAccount.setAccessTokenExpireTime(new DateTime(DateUtils.currentDate()).plusMonths(6).toDate());
            userService.save(authAccount);
        }


        // Write Sign recorded information
        UserLogonLog userLogonLog = new UserLogonLog();
        userLogonLog.setLogonTime(DateUtils.currentDate());
        userLogonLog.setLogonYearMonthDay(DateUtils.formatDate(userLogonLog.getLogoutTime()));
        userLogonLog.setRemoteAddr(httpServletRequest.getRemoteAddr());
        userLogonLog.setRemoteHost(httpServletRequest.getRemoteHost());
        userLogonLog.setRemotePort(httpServletRequest.getRemotePort());
        userLogonLog.setLocalAddr(httpServletRequest.getLocalAddr());
        userLogonLog.setLocalName(httpServletRequest.getLocalName());
        userLogonLog.setLocalPort(httpServletRequest.getLocalPort());
        userLogonLog.setServerIP(IPAddrFetcher.getGuessUniqueIP());
        userLogonLog.setHttpSessionId(httpServletRequest.getSession().getId());
        userLogonLog.setUserAgent(httpServletRequest.getHeader("User-Agent"));
        userLogonLog.setXforwardFor(IPAddrFetcher.getRemoteIpAddress(httpServletRequest));
        userLogonLog.setAuthType(authAccount.getAuthType());
        userLogonLog.setAuthUid(authAccount.getAuthUid());
        userLogonLog.setAuthGuid(authAccount.getAuthGuid());
        userService.userLogonLog(authAccount, userLogonLog);

        AuthUserDetails authUserDetails = AuthContextHolder.getAuthUserDetails();
        authUserDetails.setAccessToken(authAccount.getAccessToken());


       // Move to a different interface depending on the type of login success
        if (isMobileAppAccess(request)) {
            return true;
        } else {

        	// Determine whether the password has expired , and if the steering password modification interface
            Date credentialsExpireTime = authAccount.getCredentialsExpireTime();
            if (credentialsExpireTime != null && credentialsExpireTime.before(DateUtils.currentDate())) {
                httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + authUserDetails.getUrlPrefixBySource()
                        + "/profile/credentials-expire");
                return false;
            }

         // If it is forced toward the designated successUrl then emptied SavedRequest
            if (forceSuccessUrl) {
                WebUtils.getAndClearSavedRequest(httpServletRequest);
            }

            return super.onLoginSuccess(token, subject, request, httpServletResponse);
        }
    }

    protected void setFailureAttribute(ServletRequest request, AuthenticationException ae) {

    	// Write exception object for authentication error display
        request.setAttribute(getFailureKeyAttribute(), ae);
    }

    public void setCaptchaParam(String captchaParam) {
        this.captchaParam = captchaParam;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public static class CaptchaValidationException extends AuthenticationException {

        private static final long serialVersionUID = -7285314964501172092L;

        public CaptchaValidationException(String message) {
            super(message);
        }
    }

    public void setForceSuccessUrl(boolean forceSuccessUrl) {
        this.forceSuccessUrl = forceSuccessUrl;
    }

}
