package lab.s2jh.aud.web;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;

import lab.s2jh.aud.service.RevisionEntityService;
import lab.s2jh.core.annotation.MenuData;
import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.audit.envers.EntityRevision;
import lab.s2jh.core.audit.envers.ExtDefaultRevisionEntity;
import lab.s2jh.core.entity.BaseEntity;
import lab.s2jh.core.entity.PersistableEntity;
import lab.s2jh.core.exception.WebException;
import lab.s2jh.core.pagination.ExtPageRequest;
import lab.s2jh.core.service.BaseService;
import lab.s2jh.core.util.EnumUtils;
import lab.s2jh.core.web.BaseController;
import lab.s2jh.core.web.json.JsonViews;
import lab.s2jh.module.auth.entity.User.AuthTypeEnum;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.util.ClassUtils;
import org.hibernate.envers.Audited;
import org.hibernate.proxy.pojo.javassist.JavassistLazyInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonView;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@Controller
@RequestMapping("/admin/aud/revision-entity")
public class RevisionEntityController extends BaseController<ExtDefaultRevisionEntity, Long> {

    private final static Logger logger = LoggerFactory.getLogger(RevisionEntityController.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private RevisionEntityService revisionEntityService;

    @Override
    protected BaseService<ExtDefaultRevisionEntity, Long> getEntityService() {
        return revisionEntityService;
    }

    @MenuData("Configuration Management: The system records : operational record")
    @RequiresPermissions("Configuration Management: The system records : operational record")
    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public String revisionEntityUserIndex(Model model) throws Exception {
        model.addAttribute("authTypeMap", EnumUtils.getEnumDataMap(AuthTypeEnum.class));
        return "admin/aud/revisionEntity-userIndex";
    }

    @RequiresPermissions("Configuration Management: The system records : operational record")
    @RequestMapping(value = "/user/list", method = RequestMethod.GET)
    @ResponseBody
    @JsonView(JsonViews.Admin.class)
    public Page<ExtDefaultRevisionEntity> findByPage(HttpServletRequest request) {
        return super.findByPage(ExtDefaultRevisionEntity.class, request);
    }

    /**
     *The main interface page turning version data
     */
    @MenuData("Configuration Management: The system records : Data Change History")
    @RequiresPermissions("Configuration Management: The system records : Data Change History")
    @RequestMapping(value = "/data", method = RequestMethod.GET)
    public String revisionEntityDataIndex(Model model) {
        model.addAttribute("authTypeMap", EnumUtils.getEnumDataMap(AuthTypeEnum.class));

        Map<String, String> clazzMapping = Maps.newHashMap();
     // Search for all entity objects and automatically incremented initialization settings
        ClassPathScanningCandidateComponentProvider scan = new ClassPathScanningCandidateComponentProvider(false);
        scan.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
        Set<BeanDefinition> beanDefinitions = scan.findCandidateComponents("**.entity.**");
        for (BeanDefinition beanDefinition : beanDefinitions) {
            Class<?> entityClass = ClassUtils.forName(beanDefinition.getBeanClassName());
            Audited audited = entityClass.getAnnotation(Audited.class);
            if (audited != null) {
                MetaData metaData = entityClass.getAnnotation(MetaData.class);
                if (metaData != null) {
                    clazzMapping.put(entityClass.getName(), metaData.value());
                } else {
                    clazzMapping.put(entityClass.getName(), entityClass.getName());
                }
            }
        }
        model.addAttribute("clazzMapping", clazzMapping);

        return "admin/aud/revisionEntity-dataIndex";
    }

    @MetaData(value = "Version list Object Properties")
    @RequestMapping(value = "/properties", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> revisionEntityProperties(HttpServletRequest request) {
        String clazz = request.getParameter("clazz");
        Class<?> entityClass = ClassUtils.forName(clazz);
        Map<String, String> properties = Maps.newLinkedHashMap();
        Map<Field, String> fields = getRevisionFields(entityClass);
        for (Map.Entry<Field, String> me : fields.entrySet()) {
            properties.put(me.getKey().getName(), me.getValue());
        }
        return properties;
    }

    @MetaData(value = "Version Datasheets")
    @RequiresPermissions("Configuration Management: The system records : Data Change History")
    @RequestMapping(value = "/data/list", method = RequestMethod.GET)
    @ResponseBody
    public Page<EntityRevision> revisionList(HttpServletRequest request) {
        String clazz = request.getParameter("clazz");
        Class<?> entityClass = ClassUtils.forName(clazz);

        String property = request.getParameter("property");
        Boolean hasChanged = null;
        String changed = request.getParameter("changed");
        if (StringUtils.isNotBlank(changed)) {
            hasChanged = BooleanUtils.toBooleanObject(changed);
        }

        String id = request.getParameter("id");
        List<EntityRevision> entityRevisions = revisionEntityService.findEntityRevisions(entityClass, NumberUtils.isDigits(id) ? Long.valueOf(id)
                : id, property, hasChanged);
        for (EntityRevision entityRevision : entityRevisions) {
            ExtDefaultRevisionEntity revEntity = entityRevision.getRevisionEntity();
            revEntity.setEntityClassName(clazz);
            revEntity.addExtraAttribute("entityId", id);
        }

        return ExtPageRequest.buildPageResultFromList(entityRevisions);
    }

    /**
     * Drop-down list for the version attribute set
     * @return
     */
    public Map<Field, String> getRevisionFields(final Class<?> entityClass) {
        Map<Field, String> revisionFields = Maps.newLinkedHashMap();
        Class<?> loopClass = entityClass;
        do {
            for (Field field : loopClass.getDeclaredFields()) {
                MetaData metaData = field.getAnnotation(MetaData.class);
                if (metaData != null && metaData.comparable()) {
                    revisionFields.put(field, metaData != null ? metaData.value() : field.getName().toUpperCase());
                }
            }
            loopClass = loopClass.getSuperclass();
        } while (!(loopClass.equals(BaseEntity.class) || loopClass.equals(Object.class)));

        return revisionFields;
    }

    /**
     * Revision Version data comparison shows
     */
    @MetaData(value = "Version comparison of the data")
    @RequiresPermissions("Configuration Management: The system records : Data Change History")
    @RequestMapping(value = "/compare", method = RequestMethod.GET)
    public String revisionCompare(HttpServletRequest request, @RequestParam("clazz") String clazz,
            @RequestParam(value = "entityId", required = false) Long entityId, @RequestParam("revs") Long[] revs) {
        Class<?> entityClass = ClassUtils.forName(request.getParameter("clazz"));

     // Gets the collection of historical data corresponding to an array of version objects
        List<EntityRevision> entityRevisions = revisionEntityService.findEntityRevisions(entityClass, entityId, revs);

        List<Map<String, Object>> revEntityProperties = Lists.newArrayList();
        for (Map.Entry<Field, String> me : getRevisionFields(entityClass).entrySet()) {
            Field field = me.getKey();
            Map<String, Object> revEntityProperty = Maps.newHashMap();
            revEntityProperty.put("name", me.getValue());

            List<String> values = Lists.newArrayList();
            for (EntityRevision entityRevision : entityRevisions) {
                try {
                    Object value = FieldUtils.readField(entityRevision.getEntity(), field.getName(), true);
                    String valueDisplay = convertPropertyDisplay(entityRevision.getEntity(), field, value);
                    values.add(valueDisplay);
                } catch (IllegalAccessException e) {
                    throw new WebException(e.getMessage(), e);
                }
            }
            revEntityProperty.put("values", values);

            revEntityProperties.add(revEntityProperty);
        }

        request.setAttribute("entityRevisions", entityRevisions);
        request.setAttribute("revEntityProperties", revEntityProperties);
        return "admin/aud/revisionEntity-compare";
    }

    /**
     * Object Value object into display strings , subclasses can output custom format string as necessary to override this method
     * @param entity Version of the data entity object
     * @param field Version field properties
     * @param value Version attribute data value
     * @return After the format string processing
     */
    private String convertPropertyDisplay(Object entity, Field field, Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof PersistableEntity) {
            @SuppressWarnings("rawtypes")
            PersistableEntity persistableEntity = (PersistableEntity) value;
            String label = "N/A";
            try {
                label = persistableEntity.getDisplay();
            } catch (EntityNotFoundException e) {
                //Hibernate Envers Always check default version data corresponding to Audit , Audit no record before it is possible to associate an object , which can lead to data not found exception thrown Envers
                //Here do Hack Remedy: If you do not find the associated Audit records , the query associated primary object record
                try {

                	// Objects from Hibernate AOP enhanced anti-check data corresponding to the entity object
                    JavassistLazyInitializer jli = (JavassistLazyInitializer) FieldUtils.readDeclaredField(value, "handler", true);
                    Class entityClass = jli.getPersistentClass();
                    Serializable id = jli.getIdentifier();
                    Object obj = entityManager.find(entityClass, id);
                    PersistableEntity auditTargetEntity = (PersistableEntity) obj;
                    label = auditTargetEntity.getDisplay();
                } catch (IllegalAccessException iae) {
                    logger.warn(e.getMessage());
                }
            }
            return label;
        }
        return String.valueOf(value);
    }

}
