package lab.s2jh.module.auth.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.entity.BaseNativeEntity;
import lab.s2jh.core.util.DateUtils;
import lab.s2jh.core.web.json.JsonViews;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.hibernate.validator.constraints.Email;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

@Getter
@Setter
@Accessors(chain = true)
@Access(AccessType.FIELD)
@Entity
@Table(name = "auth_User", uniqueConstraints = @UniqueConstraint(columnNames = { "authUid", "authType" }))
@MetaData(value = "Login authentication account information")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Audited
public class User extends BaseNativeEntity {

    private static final long serialVersionUID = 8728775138491827366L;

    @MetaData(value = "Account globally unique identifier", comments = "At the same time as the SYS user types a password to log SALT")
    @Column(length = 64, nullable = false, unique = true)
    private String authGuid;

    @MetaData(value = "Account type corresponds to a unique identifier")
    @Column(length = 64, nullable = false)
    private String authUid;

    @MetaData(value = "Account Types")
    @Column(length = 8, nullable = false)
    @Enumerated(EnumType.STRING)
    private AuthTypeEnum authType = AuthTypeEnum.SYS;

    @MetaData(value = "Authorization Management", tooltips = "Identifies whether the user can access the management side")
    private Boolean mgmtGranted = Boolean.FALSE;

    @MetaData(value = "actual name")
    @Column(length = 128)
    private String trueName;

    @MetaData(value = "After logging -friendly display nickname")
    @Column(length = 128)
    private String nickName;

    @MetaData(value = "SYS user password self type", comments = "Encryption Algorithm : MD5 ({authGuid} + original password )")
    @JsonIgnore
    private String password;

    @MetaData(value = "e-mail", tooltips = "Please fill can be used to send e-mail notification system , retrieve password function")
    @Email
    private String email;

    @MetaData(value = "mobile phone", tooltips = "Please fill in , the system can be used to send notification messages , retrieve password function")
    private String mobile;

    @MetaData(value = "REST Access Token")
    private String accessToken;

    @MetaData(value = "Access Token expires")
    @DateTimeFormat(pattern = DateUtils.DEFAULT_TIME_FORMAT)
    @JsonFormat(pattern = DateUtils.DEFAULT_TIME_FORMAT)
    private Date accessTokenExpireTime;

    @MetaData(value = "The account is not locked flag", tooltips = "Unable to login account locked")
    private Boolean accountNonLocked = Boolean.TRUE;

    @MetaData(value = "Expiration date", tooltips = "Setting account expiration date to access the system , empty means never fail")
    @DateTimeFormat(pattern = DateUtils.DEFAULT_DATE_FORMAT)
    @JsonFormat(pattern = DateUtils.DEFAULT_DATE_FORMAT)
    private Date accountExpireTime;

    @MetaData(value = "Password expiration", tooltips = "You must modify the password after the expiration of forcing the user to log on successfully", comments = "For example, set the password used to initialize the current time, so that next time the user after successful login force the user must change the password.")
    @DateTimeFormat(pattern = DateUtils.DEFAULT_DATE_FORMAT)
    @JsonFormat(pattern = DateUtils.DEFAULT_DATE_FORMAT)
    private Date credentialsExpireTime;

    @MetaData(value = "Recently the number of authentication failure", comments = "Authentication failed accumulate, cleared successfully . After reaching the set number of failed locks the account to prevent unlimited number of times to try to guess passwords")
    private Integer logonFailureTimes = 0;

    @MetaData(value = "Associated with the role")
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @JsonIgnore
    private List<UserR2Role> userR2Roles;

    @MetaData(value = "Extended information object")
    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @PrimaryKeyJoinColumn(foreignKey = @ForeignKey(name = "none", value = ConstraintMode.NO_CONSTRAINT))
    @JsonView(JsonViews.Admin.class)
    @NotAudited
    private UserExt userExt;

    @MetaData(value = "Linked role of the primary key set", comments = "Auxiliary attributes: page form label for data binding")
    @Transient
    @Getter(AccessLevel.NONE)
    @JsonIgnore
    private Long[] selectedRoleIds;

    public Long[] getSelectedRoleIds() {
        if (userR2Roles != null && selectedRoleIds == null) {
            selectedRoleIds = new Long[userR2Roles.size()];
            for (int i = 0; i < selectedRoleIds.length; i++) {
                selectedRoleIds[i] = userR2Roles.get(i).getRole().getId();
            }
        }
        return selectedRoleIds;
    }

    public static enum AuthTypeEnum {

        @MetaData(value = "Self Account")
        SYS,

        @MetaData(value = "QQ network")
        QQ,

        @MetaData(value = "Sina Weibo")
        WB;

    }

    @Override
    @Transient
    public String getDisplay() {
        return authType + "_" + authUid;
    }

    @MetaData(value = "User ID aliases", comments = "Users log on multiple terminals , it requires an identity with a multi-terminal identity to push messages")
    @Transient
    public String getAlias() {
        return authType + "_" + authUid;
    }

}
