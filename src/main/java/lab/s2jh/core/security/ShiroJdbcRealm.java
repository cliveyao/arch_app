package lab.s2jh.core.security;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import lab.s2jh.core.security.SourceUsernamePasswordToken.AuthSourceEnum;
import lab.s2jh.core.util.DateUtils;
import lab.s2jh.module.auth.entity.Privilege;
import lab.s2jh.module.auth.entity.Role;
import lab.s2jh.module.auth.entity.User;
import lab.s2jh.module.auth.service.UserService;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

public class ShiroJdbcRealm extends AuthorizingRealm {

    private PasswordService passwordService;

    private UserService userService;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof SourceUsernamePasswordToken;
    }

    /**
     * Certification callback function is called when you log on .
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) throws AuthenticationException {
        SourceUsernamePasswordToken token = (SourceUsernamePasswordToken) authcToken;

        if (AuthSourceEnum.P.equals(token.getSource())) {
            if (StringUtils.isBlank(token.getUuid())) {
                throw new AuthenticationException("uuid identification parameter can not be empty");
            }
        }

        String username = token.getUsername();
        User authAccount = userService.findByAuthTypeAndAuthUid(User.AuthTypeEnum.SYS, username);
        if (authAccount == null) {
            throw new UnknownAccountException("Login name or password is incorrect");
        }


     // Determine the user administrative rights
        if (AuthSourceEnum.A.equals(token.getSource())) {
            if (!Boolean.TRUE.equals(authAccount.getMgmtGranted())) {
                throw new AccountException("Currently logged in account management access unauthorized");
            }
        }


     // To inject salt value to the user for a password for subsequent encryption algorithm
        token.setPassword(passwordService.injectPasswordSalt(String.valueOf(token.getPassword()), authAccount.getAuthGuid()).toCharArray());

        AuthUserDetails authUserDetails = buildAuthUserDetails(authAccount);
        authUserDetails.setSource(token.getSource());
        authUserDetails.setAccessToken(authAccount.getAccessToken());

        return new SimpleAuthenticationInfo(authUserDetails, authAccount.getPassword(), "Shiro JDBC Realm");
    }

    public static AuthUserDetails buildAuthUserDetails(User authAccount) {

    	// Status check
        if (Boolean.FALSE.equals(authAccount.getAccountNonLocked())) {
            throw new LockedAccountException("Account locked disabled");
        }

        Date accountExpireTime = authAccount.getAccountExpireTime();
        if (accountExpireTime != null && accountExpireTime.before(DateUtils.currentDate())) {
            throw new DisabledAccountException("Account has been disabled due");
        }


     // Construct user authentication information object privilege framework
        AuthUserDetails authUserDetails = new AuthUserDetails();
        authUserDetails.setAuthGuid(authAccount.getAuthGuid());
        authUserDetails.setAuthType(authAccount.getAuthType());
        authUserDetails.setAuthUid(authAccount.getAuthUid());
        authUserDetails.setNickName(authAccount.getNickName());

        return authUserDetails;
    }

    /**
     *Authorization query callback function call but when the cache authentication information 
     *without the user's authorization .
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        AuthUserDetails authUserDetails = (AuthUserDetails) principals.getPrimaryPrincipal();

     // APP fixed user role
        if (AuthSourceEnum.P.equals(authUserDetails.getSource())) {
            return null;
        }
        User user = userService.findByAuthTypeAndAuthUid(authUserDetails.getAuthType(), authUserDetails.getAuthUid());

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

        if (Boolean.TRUE.equals(user.getMgmtGranted())) {
        	// End management Log Source
            info.addRole(AuthUserDetails.ROLE_MGMT_USER);
        } else {
        	// Common source tip Login
            info.addRole(AuthUserDetails.ROLE_SITE_USER);
        }


        // Query the list of user roles
        List<Role> userRoles = userService.findRoles(user);
        for (Role role : userRoles) {
            info.addRole(role.getCode());
        }


        // Super administrator special treatment
        for (String role : info.getRoles()) {
            if (AuthUserDetails.ROLE_SUPER_USER.equals(role)) {

            	// Append super privileges configuration
                info.addStringPermission("*");
                break;
            }
        }

     // Gets a collection based on the current set of valid user permissions for all roles
        List<Privilege> privileges = userService.findPrivileges(info.getRoles());
        for (Privilege privilege : privileges) {
            info.addStringPermission(privilege.getCode());
        }

        return info;
    }

    public AuthorizationInfo getAuthorizationInfo(PrincipalCollection principals) {
        return super.getAuthorizationInfo(principals);
    }

    /**
     *Setting Password check Hash algorithm iterations.
     */
    @PostConstruct
    public void initCredentialsMatcher() {
        HashedCredentialsMatcher matcher = new HashedCredentialsMatcher(PasswordService.HASH_ALGORITHM);
        setCredentialsMatcher(matcher);
    }

    public void setPasswordService(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
