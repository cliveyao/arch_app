/**
 * Copyright (c) 2012
 */
package lab.s2jh.core.security;

import lab.s2jh.core.context.SpringContextHolder;
import lab.s2jh.module.auth.entity.User;
import lab.s2jh.module.auth.entity.User.AuthTypeEnum;
import lab.s2jh.module.auth.service.UserService;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ThreadLocal ways to deliver information to the Web logs in the business layer access
 */
public class AuthContextHolder {

    private static final Logger logger = LoggerFactory.getLogger(AuthContextHolder.class);

    public static final String DEFAULT_UNKNOWN_PIN = "N/A";

    /**
     * Get SYS account type Account
     */
    public static String getAuthSysUserUid() {
        AuthUserDetails authUserDetails = getAuthUserDetails();
        if (authUserDetails == null || !AuthTypeEnum.SYS.equals(authUserDetails.getAuthType())) {
            return null;
        }
        return authUserDetails.getAuthUid();
    }

    /**
     * Being logged in user -friendly display string
     */
    public static String getAuthUserDisplay() {
        AuthUserDetails authUserDetails = getAuthUserDetails();
        if (authUserDetails != null) {
            return authUserDetails.getAuthDisplay();
        }
        return DEFAULT_UNKNOWN_PIN;
    }

    /**
     * It is based on Spring Security user authentication information
     */
    public static AuthUserDetails getAuthUserDetails() {
        Subject subject = null;
        try {
            subject = SecurityUtils.getSubject();
        } catch (Exception e) {
            logger.trace(e.getMessage());
        }
        if (subject == null) {
            return null;
        }
        Object principal = null;
        try {
            principal = subject.getPrincipal();
        } catch (InvalidSessionException e) {
        	// If there is no valid Shiro Session direct return null, when processed in the background to avoid the relevant audit exception processing logic code
            logger.trace(e.getMessage());
        }
        if (principal == null) {
            return null;
        }
        return (AuthUserDetails) principal;
    }

    /**
     * Get the current logged-on user Entity Entity Object
     */
    public static User findAuthUser() {
        AuthUserDetails authUserDetails = AuthContextHolder.getAuthUserDetails();
        if (authUserDetails == null) {
            return null;
        }
        UserService userService = SpringContextHolder.getBean(UserService.class);
        return userService.findByAuthTypeAndAuthUid(authUserDetails.getAuthType(), authUserDetails.getAuthUid());
    }
}
