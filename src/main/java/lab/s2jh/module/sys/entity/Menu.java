package lab.s2jh.module.sys.entity;

import java.lang.reflect.Method;
import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.entity.BaseNativeEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.util.ClassUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Getter
@Setter
@Accessors(chain = true)
@Access(AccessType.FIELD)
@Entity
@Table(name = "sys_Menu")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@MetaData(value = "menu")
@Audited
public class Menu extends BaseNativeEntity {

    private static final long serialVersionUID = 2860233299443173932L;

    @MetaData(value = "name")
    @Column(nullable = false, length = 32)
    private String name;

    @MetaData(value = "Menu Path")
    @Column(nullable = false, length = 255, unique = true)
    private String path;

    @MetaData(value = "Parent")
    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "parent_id")
    @JsonIgnore
    private Menu parent;

    @MetaData(value = "description")
    @Column(length = 1000)
    @JsonIgnore
    private String description;

    @MetaData(value = "Disable Logo", tooltips = "Global disable menu is not displayed")
    private Boolean disabled = Boolean.FALSE;

    @MetaData(value = "Menu URL")
    @Column(length = 256)
    private String url;

    @MetaData(value = "Icon style")
    @Column(length = 128)
    private String style;

    @MetaData(value = "queue number", tooltips = "Relative ranking number, the larger the number, the closer the display")
    @Column(nullable = false)
    private Integer orderRank = 100;

    @MetaData(value = "Expand logo", tooltips = "Expand the menu if the default group")
    private Boolean initOpen = Boolean.FALSE;

    @MetaData(value = "Inherit level")
    private Integer inheritLevel;

    @MetaData(value = "Web Controller corresponds to the class name")
    @Column(length = 256)
    private String controllerClass;

    @MetaData(value = "Web Controller calls the corresponding method name")
    @Column(length = 128)
    private String controllerMethod;

    @MetaData(value = "Rebuild Time")
    private Date rebuildTime;

    @Override
    @Transient
    public String getDisplay() {
        return path;
    }

    @MetaData(value = "Cache Web Controller to call methods", comments = "Controller cache entry corresponding method for easy comparison authority to judge")
    @Transient
    private Method mappingMethod;

    @Transient
    @JsonIgnore
    public Method getMappingMethod() {
        if (mappingMethod != null) {
            return mappingMethod;
        }
     // Controller class and method for recording information on construction MethodInvocation for subsequent calls shiro interceptors access than
        if (StringUtils.isNotBlank(getControllerMethod())) {
            final Class<?> clazz = ClassUtils.forName(getControllerClass());
            Method[] methods = clazz.getMethods();
            for (final Method method : methods) {
                if (method.getName().equals(getControllerMethod())) {
                    RequestMapping rm = method.getAnnotation(RequestMapping.class);
                    if (rm.method() == null || rm.method().length == 0 || ArrayUtils.contains(rm.method(), RequestMethod.GET)) {
                        mappingMethod = method;
                        break;
                    }
                }
            }
        }
        return mappingMethod;
    }
}
