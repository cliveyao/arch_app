package lab.s2jh.module.auth.entity;

import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.Size;

import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.entity.BaseNativeEntity;
import lab.s2jh.core.web.json.DateTimeJsonSerializer;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.Email;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Getter
@Setter
@Accessors(chain = true)
@Access(AccessType.FIELD)
@Entity
@Table(name = "auth_SignupUser")
@MetaData(value = "Self- registered account data")
@Cache(usage = CacheConcurrencyStrategy.NONE)
public class SignupUser extends BaseNativeEntity {

    private static final long serialVersionUID = -1802915812231452200L;

    @MetaData(value = "Account globally unique identifier", comments = "At the same time as the SYS user types a password to log SALT")
    @Column(length = 64, nullable = false, unique = true)
    private String authGuid;

    @MetaData(value = "Login account")
    @Size(min = 3, max = 30)
    @Column(length = 128, unique = true, nullable = false)
    private String authUid;

    @MetaData(value = "login password")
    @Column(updatable = false, length = 128, nullable = false)
    private String password;

    @MetaData(value = "actual name")
    @Column(length = 64)
    private String trueName;

    @MetaData(value = "nickname")
    @Column(length = 64)
    private String nickName;

    @MetaData(value = "e-mail")
    @Email
    @Column(length = 128)
    private String email;

    @MetaData(value = "mobile phone", tooltips = "Please fill in , the system can be used to send notification messages , retrieve password function")
    private String mobile;

    @MetaData(value = "Registration time")
    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    @JsonSerialize(using = DateTimeJsonSerializer.class)
    private Date signupTime;

    @MetaData(value = "instruction manual")
    @Column(length = 3000)
    private String remarkInfo;

    @MetaData(value = "Audit processing time")
    @Temporal(TemporalType.TIMESTAMP)
    @JsonSerialize(using = DateTimeJsonSerializer.class)
    private Date auditTime;

    @JsonIgnore
    @Transient
    private User user;
}
