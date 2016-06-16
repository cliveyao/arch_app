package lab.s2jh.core.web;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import lab.s2jh.core.entity.PersistableEntity;
import lab.s2jh.core.exception.WebException;
import lab.s2jh.core.pagination.GroupPropertyFilter;
import lab.s2jh.core.pagination.PropertyFilter;
import lab.s2jh.core.service.BaseService;
import lab.s2jh.core.util.DateUtils;
import lab.s2jh.core.web.EntityProcessCallbackHandler.EntityProcessCallbackException;
import lab.s2jh.core.web.view.OperationResult;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;
import org.springframework.util.Assert;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public abstract class BaseController<T extends PersistableEntity<ID>, ID extends Serializable> {

    private final static Logger logger = LoggerFactory.getLogger(BaseController.class);

    /** Subclass DOE Service interface objects corresponding generic */
    abstract protected BaseService<T, ID> getEntityService();

    /** Class definition entities corresponding generic */
    protected Class<T> entityClass;

    /** Corresponding to the primary key generic definition of Class */
    protected Class<ID> entityIdClass;

    /**
     * Initialization constructor , generic computing related objects
     */
    @SuppressWarnings("unchecked")
    public BaseController() {
        super();
         // Get the Entity Class by reflection .
        try {
            Object genericClz = getClass().getGenericSuperclass();
            if (genericClz instanceof ParameterizedType) {
                entityClass = (Class<T>) ((ParameterizedType) genericClz).getActualTypeArguments()[0];
                entityIdClass = (Class<ID>) ((ParameterizedType) genericClz).getActualTypeArguments()[1];
            }
        } catch (Exception e) {
            throw new WebException(e.getMessage(), e);
        }
    }

    /**
     * String id parameter id = 123 format into a generic ID corresponding primary key instance variables
     * In addition , the page will take a Struts tag obtain the current operation target ID value is displayed
     * @return ID generic object instantiation
     */
    public ID getId(HttpServletRequest request) {
        return getId(request, "id");
    }

    /**
     * The specified parameters into generic ID corresponding primary key instance variables
     * In addition , the page will take a Struts tag obtain the current operation target ID value is displayed
     * @return ID generic object instantiation
     */
    @SuppressWarnings("unchecked")
    public ID getId(HttpServletRequest request, String paramName) {
        String entityId = request.getParameter(paramName);
        //jqGrid inline edit新增数据传入id=负数标识 
        if (StringUtils.isBlank(entityId) || entityId.startsWith("-")) {
            return null;
        }
        if (String.class.isAssignableFrom(entityIdClass)) {
            return (ID) entityId;
        } else if (Long.class.isAssignableFrom(entityIdClass)) {
            return (ID) (Long.valueOf(entityId));
        } else {
            throw new IllegalStateException("Undefine entity ID class: " + entityIdClass);
        }
    }

    protected Page<T> findByPage(Class<T> clazz, HttpServletRequest request, EntityProcessCallbackHandler<T> handler) {
        //RoutingDataSourceAdvice.setSlaveDatasource();
        Pageable pageable = PropertyFilter.buildPageableFromHttpRequest(request);
        GroupPropertyFilter groupFilter = GroupPropertyFilter.buildFromHttpRequest(clazz, request);
        appendFilterProperty(groupFilter);
        Page<T> pageData = getEntityService().findByPage(groupFilter, pageable);
        if (handler != null) {
            List<T> content = pageData.getContent();
            for (T entity : content) {
                try {
                    handler.processEntity(entity);
                } catch (EntityProcessCallbackException e) {
                    throw new WebException("entity process callback error", e);
                }
            }
        }
        return pageData;
    }

    protected Page<T> findByPage(Class<T> clazz, HttpServletRequest request) {
        return findByPage(clazz, request, null);
    }

    protected OperationResult editSave(T entity) {
        getEntityService().save(entity);
        Map<String, Object> result = Maps.newHashMap();
        result.put("id", entity.getId());
        return OperationResult.buildSuccessResult("数据保存处理完成", result);
    }

    protected OperationResult delete(ID... ids) {
        Assert.notNull(ids);
        return delete(ids, null);
    }

    protected OperationResult delete(ID[] ids, EntityProcessCallbackHandler<T> handler) {
    	// Delete the failed id and the corresponding return messages Map structure 
    	//can be used to display error messages and the front end of batch computing 
    	//Spreadsheet Component Update Delete Line Items
        Map<ID, String> errorMessageMap = Maps.newLinkedHashMap();

        Set<T> enableDeleteEntities = Sets.newHashSet();
        Collection<T> entities = getEntityService().findAll(ids);
        for (T entity : entities) {
            String msg = null;
         // Callback interface calls , such as internal incoming class manner whether the object can be deleted detection logic
            if (handler != null) {
                try {
                    handler.processEntity(entity);
                } catch (EntityProcessCallbackException e) {
                    msg = e.getMessage();
                }
            }
            if (StringUtils.isBlank(msg)) {
                enableDeleteEntities.add(entity);
            } else {
                errorMessageMap.put(entity.getId(), msg);
            }
        }
        // For the bulk delete , loop over each object is called Service Interface delete , then each separate transaction object deletion
        // Delete some objects so you can easily fail without affecting other objects deleted
        // If you need to make sure the business logic batch delete objects in the same transaction then please call the Service subclasses override batch delete interfaces
        for (T entity : enableDeleteEntities) {
            try {
                getEntityService().delete(entity);
            } catch (Exception e) {
                logger.warn("entity delete failure", e);
                errorMessageMap.put(entity.getId(), e.getMessage());
            }
        }
        int rejectSize = errorMessageMap.size();
        if (rejectSize == 0) {
            return OperationResult.buildSuccessResult("Successfully deleted selected selected records :" + entities.size() + " Article");
        } else {
            if (rejectSize == entities.size()) {
                return OperationResult.buildFailureResult("Delete all selected records operation fails", errorMessageMap);
            } else {
                return OperationResult.buildWarningResult("Delete operation has been successfully processed :" + (entities.size() - rejectSize) + "Article" + " failure:" + rejectSize + 
                		"Article",
                        errorMessageMap);
            }
        }
    }

    protected T initPrepareModel(HttpServletRequest request, Model model, ID id) {
        T entity = null;
        if (id != null && StringUtils.isNotBlank(id.toString())) {
        	// If the data is a POST request , obtain Detach state object , the other way in order to retain the Session Gets Lazy property
            if (request.getMethod().equalsIgnoreCase("POST")) {
                entity = buildDetachedBindingEntity(id);
            }
         // If the child class does not detach objects are given , will remain non- detach mode query returns the object
            if (entity == null) {
                entity = getEntityService().findOne(id);
            }
            model.addAttribute("id", id);
        }
        if (entity == null) {
            try {
                entity = entityClass.newInstance();
            } catch (Exception e) {
                throw new WebException(e.getMessage(), e);
            }
        }
        model.addAttribute("clazz", entityClass.getName());
        model.addAttribute("entity", entity);
        return entity;
    }

    /**
     * If the child class needs many associated objects batch processing, in the sub-class
     *  return custom detach objects
     * @param id
     * @return
     */
    protected T buildDetachedBindingEntity(ID id) {
        return null;
    }

    /**
     * In order to prevent a malicious user to modify the incoming data inaccessible property 
     * whitelist mechanism , only the properties defined in this process will be automatically
     *  bound
     * Remember all the form elements to the properties added to this method setAllowedFields 
     * list , there would be page data is not saved correctly to the database
     */
    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat(DateUtils.DEFAULT_DATE_FORMAT), true));
        
        // Converts empty strings into null when a form is submitted
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));  
        
        //binder.setAllowedFields("nick", "gender", "name", "idCardNo", "studentExt.dormitory");
    }

    /**
     * Subclass extra additional filter restrictions entrance method , generally based on the currently logged-in user to force an additional filter
     * Note : all control parameters based on the currently logged on user, must not tamper with the data access request data transfer request parameter illegal adoption page , there is a risk of the user
     * Therefore, we must at Controller level by overwriting callback function or their business methods to force additional filters
     * @param Filters are assembled based on the query conditions Request collection object
     */
    protected void appendFilterProperty(GroupPropertyFilter groupPropertyFilter) {

    }

    /**
     * For some complex processing logic required to submit data on the server after checking prompted a warning message requires the user to confirm the secondary
     * Determine whether the current form has been confirmed that the user confirm OK
     */
    protected boolean postNotConfirmedByUser(HttpServletRequest request) {
        return !BooleanUtils.toBoolean(request.getParameter("_serverValidationConfirmed_"));
    }

}
