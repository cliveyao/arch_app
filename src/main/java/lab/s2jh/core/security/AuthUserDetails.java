/**
 * Copyright (c) 2012
 */
package lab.s2jh.core.security;

import java.io.Serializable;

import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.security.SourceUsernamePasswordToken.AuthSourceEnum;
import lab.s2jh.module.auth.entity.User.AuthTypeEnum;

/**
 * Certification authority stored in the container frame of the authorized user data objects
 */
public class AuthUserDetails implements Serializable {

    private static final long serialVersionUID = 8346793124666695534L;

    @MetaData(value = "Super Administrator role")
    public final static String ROLE_SUPER_USER = "ROLE_SUPER_USER";

    @MetaData(value = "Front-end portal user roles")
    public final static String ROLE_SITE_USER = "ROLE_SITE_USER";

    @MetaData(value = "APP user roles")
    public final static String ROLE_APP_USER = "ROLE_APP_USER";

    @MetaData(value = "Backend administration user roles")
    public final static String ROLE_MGMT_USER = "ROLE_MGMT_USER";

    @MetaData(value = "All controlled permissions to this role")
    public final static String ROLE_PROTECTED = "ROLE_PROTECTED";

    @MetaData(value = "Account globally unique identifier")
    private String authGuid;

    @MetaData(value = "Account type corresponds to a unique identifier")
    private String authUid;

    @MetaData(value = "Account Types")
    private AuthTypeEnum authType;

    @MetaData(value = "After logging -friendly display nickname")
    private String nickName;

    @MetaData(value = "Record Log Sources")
    private AuthSourceEnum source = AuthSourceEnum.W;

    @MetaData(value = "Access TOKEN")
    private String accessToken;

    public String getAuthGuid() {
        return authGuid;
    }

    public void setAuthGuid(String authGuid) {
        this.authGuid = authGuid;
    }

    public String getAuthUid() {
        return authUid;
    }

    public void setAuthUid(String authUid) {
        this.authUid = authUid;
    }

    public AuthTypeEnum getAuthType() {
        return authType;
    }

    public void setAuthType(AuthTypeEnum authType) {
        this.authType = authType;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public AuthSourceEnum getSource() {
        return source;
    }

    public void setSource(AuthSourceEnum source) {
        this.source = source;
    }

    public String getAuthDisplay() {
        return authType + ":" + authUid;
    }

    public String getUrlPrefixBySource() {
        if (AuthSourceEnum.A.equals(source)) {
            return "/admin";
        } else if (AuthSourceEnum.M.equals(source)) {
            return "/m";
        } else {
            return "/w";
        }
    }

    @Override
    public String toString() {
        return "AuthUserDetails [authGuid=" + authGuid + ", authUid=" + authUid + ", authType=" + authType + ", nickName=" + nickName + ", source="
                + source + "]";
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
