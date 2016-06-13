package lab.s2jh.aud.entity;

import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.entity.PersistableEntity;
import lab.s2jh.core.util.DateUtils;
import lab.s2jh.core.web.json.DateTimeJsonSerializer;
import lab.s2jh.module.auth.entity.User.AuthTypeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Getter
@Setter
@Accessors(chain = true)
@Access(AccessType.FIELD)
@Entity
@Table(name = "auth_UserLogonLog")
@Cache(usage = CacheConcurrencyStrategy.NONE)
@MetaData(value = "User Login Logout History")
public class UserLogonLog extends PersistableEntity<Long> {

    private static final long serialVersionUID = 4034691676061136485L;

    @Id
    @GeneratedValue(generator = "idGenerator")
    @GenericGenerator(name = "idGenerator", strategy = "native")
    private Long id;

    @MetaData(value = "Account globally unique identifier", comments = "At the same time as the SYS user types a password to log SALT")
    @Column(length = 64, nullable = false)
    private String authGuid;

    @MetaData(value = "Account type corresponds to a unique identifier")
    @Column(length = 64, nullable = false)
    private String authUid;

    @MetaData(value = "Account Types")
    @Column(length = 8, nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthTypeEnum authType = AuthTypeEnum.SYS;

    @MetaData(value = "Facilitate daily summary statistics of redundant attributes")
    @Column(nullable = true)
    private String logonYearMonthDay;

    @MetaData(value = "Log in time")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    @JsonSerialize(using = DateTimeJsonSerializer.class)
    private Date logonTime;

    @MetaData(value = "Logout Time")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonSerialize(using = DateTimeJsonSerializer.class)
    private Date logoutTime;

    @MetaData(value = "Log Duration")
    private Long logonTimeLength;

    @MetaData(value = "Logins")
    private Integer logonTimes;

    @MetaData(value = "userAgent")
    private String userAgent;

    @MetaData(value = "xforwardFor")
    private String xforwardFor;

    @MetaData(value = "localAddr")
    private String localAddr;

    @MetaData(value = "localName")
    private String localName;

    @MetaData(value = "localPort")
    private Integer localPort;

    @MetaData(value = "remoteAddr")
    private String remoteAddr;

    @MetaData(value = "remoteHost")
    private String remoteHost;

    @MetaData(value = "remotePort")
    private Integer remotePort;

    @MetaData(value = "serverIP")
    private String serverIP;

    @MetaData(value = "Session Number")
    @Column(length = 128, nullable = false, unique = true)
    private String httpSessionId;

    @Override
    @Transient
    public String getDisplay() {
        return authType + ":" + authUid;
    }

    @Transient
    public String getLogonTimeLengthFriendly() {
        return DateUtils.getHumanDisplayForTimediff(logonTimeLength);
    }
}