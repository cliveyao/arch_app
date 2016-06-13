package lab.s2jh.core.security;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import lab.s2jh.module.auth.service.UserService;

import org.apache.shiro.web.filter.authc.LogoutFilter;

public class AppLogoutFilter extends LogoutFilter {

    private UserService userService;

    protected void issueRedirect(ServletRequest request, ServletResponse response, String redirectUrl) throws Exception {
        //TODO Log records found correlation process accessToken
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
