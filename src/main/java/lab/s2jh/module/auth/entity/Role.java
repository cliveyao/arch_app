package lab.s2jh.module.auth.entity;

import java.util.List;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.entity.BaseNativeEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;

@Getter
@Setter
@Accessors(chain = true)
@Access(AccessType.FIELD)
@Entity
@Table(name = "auth_Role")
@MetaData(value = "Role")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Audited
public class Role extends BaseNativeEntity {

    private static final long serialVersionUID = 7955799161213060384L;

    @MetaData(value = "Code", tooltips = "Heading must begin with ROLE_")
    @Size(min = 6)
    @Pattern(regexp = "^ROLE_.*", message = "You must begin with [ROLE_]")
    @Column(nullable = false, length = 64, unique = true)
    private String code;

    @MetaData(value = "name")
    @Column(nullable = false, length = 256)
    private String name;

    @MetaData(value = "description")
    @Column(nullable = true, length = 2000)
    private String description;

    @MetaData(value = "Disable Logo", tooltips = "Disable role is not involved in access control logic")
    private Boolean disabled = Boolean.FALSE;

    @MetaData(value = "Association role permissions")
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @JsonIgnore
    private List<RoleR2Privilege> roleR2Privileges = Lists.newArrayList();

    @MetaData(value = "Associate user roles")
    @OneToMany(mappedBy = "role", cascade = CascadeType.ALL, orphanRemoval = true)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @JsonIgnore
    private List<UserR2Role> roleR2Users = Lists.newArrayList();

    @Override
    @Transient
    public String getDisplay() {
        return code + " " + name;
    }
}
