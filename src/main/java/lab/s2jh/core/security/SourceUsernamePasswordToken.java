package lab.s2jh.core.security;

import lab.s2jh.core.annotation.MetaData;

import org.apache.shiro.authc.UsernamePasswordToken;

public class SourceUsernamePasswordToken extends UsernamePasswordToken {

    public SourceUsernamePasswordToken(String username, String password, boolean rememberMe, String host) {
        super(username, password, rememberMe, host);
    }

    private static final long serialVersionUID = 4494958942452530263L;

    @MetaData(value = "Log Source", comments = "Logo is a front-end or back-end management, user login sources , the default role can be granted according to different sources")
    private AuthSourceEnum source;

    @MetaData(value = "Uniquely identifies the source", comments = "Other identification that uniquely identifies the source device or application")
    private String uuid;

    public static enum AuthSourceEnum {

        @MetaData(value = "APP mobile applications")
        P,

        @MetaData(value = "HTML5 Mobile Site")
        M,

        @MetaData(value = "WWW Master", comments = "source source is empty , said this type")
        W,

        @MetaData(value = "Admin management side")
        A;
    }

    public AuthSourceEnum getSource() {
        return source;
    }

    public void setSource(AuthSourceEnum source) {
        this.source = source;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
