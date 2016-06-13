package lab.s2jh.module.auth.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.entity.BaseNativeEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;

@Getter
@Setter
@Accessors(chain = true)
@Access(AccessType.FIELD)
@Entity
@Table(name = "auth_Privilege")
@MetaData(value = "Competence")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Privilege extends BaseNativeEntity {

    private static final long serialVersionUID = 5139319086984812835L;

    @MetaData(value = "Code")
    @Column(nullable = false, length = 255, unique = true)
    private String code;

    @MetaData(value = "Disable Logo", tooltips = "Disable access control logic does not participate")
    private Boolean disabled = Boolean.FALSE;

    @MetaData(value = "Rebuild Time")
    private Date rebuildTime;

    @MetaData(value = "Association role permissions")
    @OneToMany(mappedBy = "privilege", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<RoleR2Privilege> roleR2Privileges = Lists.newArrayList();

}