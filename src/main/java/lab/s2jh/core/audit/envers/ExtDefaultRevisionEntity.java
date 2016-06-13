package lab.s2jh.core.audit.envers;

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
import lab.s2jh.core.web.json.DateTimeJsonSerializer;
import lab.s2jh.module.auth.entity.User.AuthTypeEnum;
import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Extend the default Hibernate Envers audit table object definition
 * 
 * @see http://docs.jboss.org/hibernate/orm/4.2/devguide/en-US/html/ch15.html
 */
@Getter
@Setter
@Access(AccessType.FIELD)
@Entity
@Table(name = "aud_RevisionEntity")
@RevisionEntity(ExtRevisionListener.class)
@JsonIgnoreProperties(value = { "hibernateLazyInitializer", "javassistLazyInitializer", "revisionEntity", "handler" }, ignoreUnknown = true)
public class ExtDefaultRevisionEntity extends PersistableEntity<Long> {

    private static final long serialVersionUID = -2946153158442502361L;

    /** Record version */
    @Id
    @GeneratedValue
    @RevisionNumber
    private Long rev;

    /** Record Time */
    @RevisionTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @JsonSerialize(using = DateTimeJsonSerializer.class)
    private Date revstmp;

    private String entityClassName;

    /** requestMapping Controller annotation defined */
    private String requestMappingUri;

    /** Request execution of Web Controller class name */
    private String controllerClassName;

    /** Request execution of Web Controller class MetaData */
    private String controllerClassLabel;

    /** The Web Controller request execution method name */
    private String controllerMethodName;

    /** MetaData  annotation request to execute a method of Web Controller */
    private String controllerMethodLabel;

    /** Web Controller method requests execution RequestMethod: POST */
    private String controllerMethodType;

    /** Globally unique user ID, ​​ensure that the user explicitly associated with a unique operation */
    @Column(length = 128)
    private String authGuid;

    @MetaData(value = "Account type corresponds to a unique identifier")
    @Column(length = 128)
    private String authUid;

    @MetaData(value = "Account Types")
    @Column(length = 32)
    @Enumerated(EnumType.STRING)
    private AuthTypeEnum authType;

    @Override
    @Transient
    public Long getId() {
        return rev;
    }

    @Override
    @Transient
    public boolean isNew() {
        return rev == null;
    }

    @Override
    @Transient
    public String getDisplay() {
        return String.valueOf(rev);
    }

    @Transient
    public String getControllerClassDisplay() {
        if (StringUtils.isBlank(controllerClassName)) {
            return null;
        }
        return controllerClassName + (StringUtils.isBlank(controllerClassLabel) ? "" : "(" + controllerClassLabel + ")");
    }

    @Transient
    public String getControllerMethodDisplay() {
        if (StringUtils.isBlank(controllerMethodName)) {
            return null;
        }
        return controllerMethodName + (StringUtils.isBlank(controllerMethodLabel) ? "" : "(" + controllerMethodLabel + ")");
    }
}
