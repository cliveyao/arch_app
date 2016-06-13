package lab.s2jh.module.auth.entity;

import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.entity.BaseEntity;
import lab.s2jh.core.util.DateUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
@Accessors(chain = true)
@Access(AccessType.FIELD)
@Entity
@Table(name = "auth_UserExt")
@MetaData(value = "Extended user information object")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class UserExt extends BaseEntity<Long> {

    private static final long serialVersionUID = 8977448800400578128L;

    @MetaData(value = "Shared primary key", comments = "Corresponding to the main image on the ID")
    @Id
    @Column(length = 128)
    @JsonProperty
    private Long id;

    @MetaData(value = "Registration time")
    @DateTimeFormat(pattern = DateUtils.DEFAULT_TIME_FORMAT)
    @JsonFormat(pattern = DateUtils.DEFAULT_TIME_FORMAT)
    private Date signupTime;

    @MetaData(value = "last login time")
    @DateTimeFormat(pattern = DateUtils.DEFAULT_TIME_FORMAT)
    @JsonFormat(pattern = DateUtils.DEFAULT_TIME_FORMAT)
    private Date lastLogonTime;

    @MetaData(value = "Last Login IP")
    private String lastLogonIP;

    @MetaData(value = "Last Login hostname")
    private String lastLogonHost;

    @MetaData(value = "Total number of logins")
    private Integer logonTimes = 0;

    @MetaData(value = "Recently authentication failure time")
    private Date lastLogonFailureTime;

    @MetaData(value = "random number", comments = "Random UUID string used to retrieve the password set")
    private String randomCode;
}
