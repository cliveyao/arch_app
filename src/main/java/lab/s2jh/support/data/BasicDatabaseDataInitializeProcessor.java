package lab.s2jh.support.data;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import lab.s2jh.core.annotation.MenuData;
import lab.s2jh.core.cons.GlobalConstant;
import lab.s2jh.core.context.ExtPropertyPlaceholderConfigurer;
import lab.s2jh.core.data.DatabaseDataInitializeProcessor;
import lab.s2jh.core.security.AuthUserDetails;
import lab.s2jh.core.util.DateUtils;
import lab.s2jh.core.util.Exceptions;
import lab.s2jh.core.util.UidUtils;
import lab.s2jh.module.auth.entity.Department;
import lab.s2jh.module.auth.entity.Privilege;
import lab.s2jh.module.auth.entity.Role;
import lab.s2jh.module.auth.entity.User;
import lab.s2jh.module.auth.entity.User.AuthTypeEnum;
import lab.s2jh.module.auth.entity.UserR2Role;
import lab.s2jh.module.auth.service.DepartmentService;
import lab.s2jh.module.auth.service.PrivilegeService;
import lab.s2jh.module.auth.service.RoleService;
import lab.s2jh.module.auth.service.UserService;
import lab.s2jh.module.sys.entity.ConfigProperty;
import lab.s2jh.module.sys.entity.DataDict;
import lab.s2jh.module.sys.entity.Menu;
import lab.s2jh.module.sys.entity.NotifyMessage;
import lab.s2jh.module.sys.entity.UserMessage;
import lab.s2jh.module.sys.service.ConfigPropertyService;
import lab.s2jh.module.sys.service.DataDictService;
import lab.s2jh.module.sys.service.MenuService;
import lab.s2jh.module.sys.service.NotifyMessageService;
import lab.s2jh.module.sys.service.UserMessageService;
import lab.s2jh.support.service.DynamicConfigService;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Database initialization data base processor
 */
@Component
public class BasicDatabaseDataInitializeProcessor extends DatabaseDataInitializeProcessor {

    private static final Logger logger = LoggerFactory.getLogger(BasicDatabaseDataInitializeProcessor.class);

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserService userService;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private PrivilegeService privilegeService;

    @Autowired
    private MenuService menuService;

    @Autowired
    private ConfigPropertyService configPropertyService;

    @Autowired
    private DataDictService dataDictService;

    @Autowired
    private UserMessageService userMessageService;

    @Autowired
    private NotifyMessageService notifyMessageService;

    @Autowired
    private ExtPropertyPlaceholderConfigurer extPropertyPlaceholderConfigurer;

    @Override
    public void initializeInternal() {

        logger.info("Running " + this.getClass().getName());
        Date now = DateUtils.currentDate();

     // Roles, users, and data initialization , the default password is : Account +123
        if (isEmptyTable(User.class)) {

        	// Backend preset super administrator , you need to configure the relevant authority by default automatically assigned all permissions
            Role superRole = new Role();
            superRole.setCode(AuthUserDetails.ROLE_SUPER_USER);
            superRole.setName("The rear end of the Super Administrator role");
            superRole.setDescription("System Preferences , Do not modify . Preset super administrator backend , no configuration permissions by default automatically assigned all permissions.");
            roleService.save(superRole);


         // Preset super administrator account
            User entity = new User();
            entity.setAuthUid("admin");
            entity.setAuthType(AuthTypeEnum.SYS);
            entity.setMgmtGranted(true);
            entity.setNickName("Preset super administrator backend");

         // Associate Super Administrator role
            UserR2Role r2 = new UserR2Role();
            r2.setUser(entity);
            r2.setRole(superRole);
            entity.setUserR2Roles(Lists.newArrayList(r2));
            userService.save(entity, entity.getAuthUid() + "123");


           // Backend default login user roles specific permission through the management interface configuration All logged-on user 
           // backend default associated with this role , no additional write user roles and associated data
            Role mgmtRole = new Role();
            mgmtRole.setCode(AuthUserDetails.ROLE_MGMT_USER);
            mgmtRole.setName("Backend login user default role");
            mgmtRole.setDescription("System Preferences , Do not modify . Note: All backend default login user associated with this role , and no additional write user data associated with the role .");
            roleService.save(mgmtRole);


           // Default background common administrator account
            entity = new User();
            entity.setAuthGuid(UidUtils.UID());
            entity.setAuthUid("mgmt");
            entity.setAuthType(AuthTypeEnum.SYS);
            entity.setMgmtGranted(true);
            entity.setNickName("Background Default Normal Administrator");

         // Default password expires , the user is forced to modify the initial password password
            entity.setCredentialsExpireTime(now);
            userService.save(entity, entity.getAuthUid() + "123");


            // Default login user front-end role ,, specific permission through the management interface configuration
           // All front-end default login user associated with this role , no additional write user roles and associated data
            Role siteUserRole = new Role();
            siteUserRole.setCode(AuthUserDetails.ROLE_SITE_USER);
            siteUserRole.setName("The front end of the logged in user default role");
            siteUserRole.setDescription("System Preferences , Do not modify . NOTE : All front-end default login user associated with this role , and no additional write user data associated with the role .");
            roleService.save(siteUserRole);

            if (DynamicConfigService.isDemoMode()) {
                Department department = new Department();
                department.setCode("SC00");
                department.setName("Market");
                departmentService.save(department);

                Department department1 = new Department();
                department1.setCode("SC01");
                department1.setName("A market");
                department1.setParent(department);
                departmentService.save(department1);

                Department department2 = new Department();
                department2.setCode("SC02");
                department2.setName("Market two");
                department2.setParent(department);
                departmentService.save(department2);
            }
        }


     // Initialize data permissions
        rebuildPrivilageDataFromControllerAnnotation();
        commitAndResumeTransaction();


     // Initialize data menu
        rebuildMenuDataFromControllerAnnotation();
        commitAndResumeTransaction();


     // File system name attribute configuration
        String systemTitle = "未定义";
        if (extPropertyPlaceholderConfigurer != null) {
            systemTitle = extPropertyPlaceholderConfigurer.getProperty("cfg_system_title");
        }


     // Initialize the system configuration parameters
        if (configPropertyService.findByPropKey(GlobalConstant.cfg_system_title) == null) {
            ConfigProperty entity = new ConfigProperty();
            entity.setPropKey(GlobalConstant.cfg_system_title);
            entity.setPropName("system name");
            entity.setSimpleValue(systemTitle);
            configPropertyService.save(entity);
        }
        if (configPropertyService.findByPropKey(GlobalConstant.cfg_mgmt_signup_disabled) == null) {
            ConfigProperty entity = new ConfigProperty();
            entity.setPropKey(GlobalConstant.cfg_mgmt_signup_disabled);
            entity.setPropName("Disable self-registration feature");
            entity.setSimpleValue("false");
            entity.setPropDescn("Set to true to disable the login screen shield self-registration feature");
            configPropertyService.save(entity);
        }
        if (configPropertyService.findByPropKey(GlobalConstant.cfg_public_send_sms_disabled) == null) {
            ConfigProperty entity = new ConfigProperty();
            entity.setPropKey(GlobalConstant.cfg_public_send_sms_disabled);
            entity.setPropName("Whether globally disable sending SMS phone number Open");
            entity.setSimpleValue("false");
            entity.setPropDescn("If it is true only to a verified phone number to send text messages through the platform , the platform has never been proven in other phone number no longer send text messages");
            configPropertyService.save(entity);
        }


     // Initialize the data dictionary items
        if (dataDictService.findByProperty("primaryKey", GlobalConstant.DataDict_Message_Type) == null) {
            DataDict entity = new DataDict();
            entity.setPrimaryKey(GlobalConstant.DataDict_Message_Type);
            entity.setPrimaryValue("Message Type");
            dataDictService.save(entity);

            DataDict item = new DataDict();
            item.setPrimaryKey("notify");
            item.setPrimaryValue("Notice");
            item.setSecondaryValue("#32CFC4");
            item.setParent(entity);
            dataDictService.save(item);

            item = new DataDict();
            item.setPrimaryKey("bulletin");
            item.setPrimaryValue("Good news");
            item.setSecondaryValue("#FF645D");
            item.setParent(entity);
            dataDictService.save(item);

            item = new DataDict();
            item.setPrimaryKey("remind");
            item.setPrimaryValue("remind");
            item.setSecondaryValue("#FF8524");
            item.setParent(entity);
            dataDictService.save(item);
        }


     // Initialize Demo notification message
        if (isEmptyTable(NotifyMessage.class)) {
            NotifyMessage entity = new NotifyMessage();
            entity.setType("notify");
            entity.setTitle("Welcome to visit" + systemTitle);
            entity.setPublishTime(now);
            entity.setMessage("<p> system initialization time :" + DateUtils.formatTime(now) + "</p>");
            notifyMessageService.save(entity);
        }


     // Initialize Demo notification message
        if (isEmptyTable(UserMessage.class)) {
            User admin = userService.findByAuthUid("admin");

            UserMessage entity = new UserMessage();
            entity.setType("notify");
            entity.setPublishTime(DateUtils.currentDate());
            entity.setTitle("1 demonstrates a personal message");
            entity.setTargetUser(admin);
            entity.setMessage("<p>Demo directed to send a personal message contents 1</p>");
            userMessageService.save(entity);

            entity = new UserMessage();
            entity.setType("bulletin");
            entity.setPublishTime(DateUtils.currentDate());
            entity.setTitle("2 demonstrates a personal message");
            entity.setTargetUser(admin);
            entity.setMessage("<p>2 Demo content directed to send a personal message</p>");
            userMessageService.save(entity);
        }
    }

    /**
     * Menu basic data reconstruction based Controller of @MenuData comment
     */
    private void rebuildMenuDataFromControllerAnnotation() {
        try {
            logger.debug("Start to rebuildMenuDataFromControllerAnnotation...");
            Date now = DateUtils.currentDate();

            Set<BeanDefinition> beanDefinitions = Sets.newHashSet();
            ClassPathScanningCandidateComponentProvider scan = new ClassPathScanningCandidateComponentProvider(false);
            scan.addIncludeFilter(new AnnotationTypeFilter(Controller.class));
            String[] packages = StringUtils.split(ExtPropertyPlaceholderConfigurer.getBasePackages(),
                    ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
            for (String pkg : packages) {
                beanDefinitions.addAll(scan.findCandidateComponents(pkg));
            }

            ClassPool pool = ClassPool.getDefault();
            //The default ClassPool returned by a static method ClassPool.getDefault() searches the same path that the underlying JVM (Java virtual machine) has. 
            //If a program is running on a web application server such as JBoss and Tomcat, 
            //the ClassPool object may not be able to find user classes since such a web application server uses multiple class loaders as well as the system class loader. 
            //In that case, an additional class path must be registered to the ClassPool. Suppose that pool refers to a ClassPool object:  
            pool.insertClassPath(new ClassClassPath(this.getClass()));

            for (BeanDefinition beanDefinition : beanDefinitions) {
                String className = beanDefinition.getBeanClassName();
                CtClass cc = pool.get(className);
                CtMethod[] methods = cc.getMethods();
                for (CtMethod method : methods) {
                    MenuData menuData = (MenuData) method.getAnnotation(MenuData.class);
                    if (menuData != null) {
                        String[] paths = menuData.value();
                        Assert.isTrue(paths.length == 1, "Unimplments for multi menu path");
                        String fullpath = paths[0];
                        String[] names = fullpath.split(":");
                        for (int i = 0; i < names.length; i++) {
                            String path = StringUtils.join(names, ":", 0, i + 1);
                            Menu menu = menuService.findByProperty("path", path);
                            if (menu == null) {
                                menu = new Menu();
                                menu.setPath(path);
                                menu.setName(names[i]);
                                if (i > 0) {
                                    String parentPath = StringUtils.join(names, ":", 0, i);
                                    Menu parent = menuService.findByProperty("path", parentPath);
                                    menu.setParent(parent);
                                }
                            }
                            menu.setInheritLevel(i);

                         // Calculate the URL corresponding to the menu path
                            if (i + 1 == names.length) {
                                String url = "";
                                RequestMapping clazzRequestMapping = (RequestMapping) cc.getAnnotation(RequestMapping.class);
                                if (clazzRequestMapping != null) {
                                    url = url + clazzRequestMapping.value()[0];
                                }
                                RequestMapping methodRequestMapping = (RequestMapping) method.getAnnotation(RequestMapping.class);
                                if (methodRequestMapping != null) {
                                    url = url + methodRequestMapping.value()[0];
                                }
                                menu.setUrl(url);
                                menu.setControllerClass(cc.getName());
                                menu.setControllerMethod(method.getName());
                            }

                            menu.setRebuildTime(now);
                            menuService.save(menu);
                        }
                    }
                }
            }


         // Clean up expired useless menu data , descending deleted or there will be a foreign key constraint problem
            List<Menu> menus = menuService.findAllCached();
            for (int i = menus.size(); i > 0; i--) {
                Menu menu = menus.get(i - 1);
                if (menu.getRebuildTime().before(now)) {
                    menuService.delete(menu);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * @RequiresPermissions Sweep all methods annotated code Spring MVC Controller and rebuild basic data permissions
     */
    private void rebuildPrivilageDataFromControllerAnnotation() {
        try {
            logger.debug("Start to rebuildPrivilageDataFromControllerAnnotation...");
            Date now = DateUtils.currentDate();
            Set<BeanDefinition> beanDefinitions = Sets.newHashSet();
            ClassPathScanningCandidateComponentProvider scan = new ClassPathScanningCandidateComponentProvider(false);
            scan.addIncludeFilter(new AnnotationTypeFilter(Controller.class));
            String[] packages = StringUtils.split(ExtPropertyPlaceholderConfigurer.getBasePackages(),
                    ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS);
            for (String pkg : packages) {
                beanDefinitions.addAll(scan.findCandidateComponents(pkg));
            }

            List<Privilege> privileges = privilegeService.findAllCached();
            ClassPool pool = ClassPool.getDefault();
            //The default ClassPool returned by a static method ClassPool.getDefault() searches the same path that the underlying JVM (Java virtual machine) has. 
            //If a program is running on a web application server such as JBoss and Tomcat, 
            //the ClassPool object may not be able to find user classes since such a web application server uses multiple class loaders as well as the system class loader. 
            //In that case, an additional class path must be registered to the ClassPool. Suppose that pool refers to a ClassPool object:  
            pool.insertClassPath(new ClassClassPath(this.getClass()));


         // Merge all classes in all RequiresPermissions definition information
            Set<String> mergedPermissions = Sets.newHashSet();
            for (BeanDefinition beanDefinition : beanDefinitions) {
                String className = beanDefinition.getBeanClassName();
                CtClass cc = pool.get(className);
                CtMethod[] methods = cc.getMethods();
                for (CtMethod method : methods) {
                    RequiresPermissions rp = (RequiresPermissions) method.getAnnotation(RequiresPermissions.class);
                    if (rp != null) {
                        //int startLine = method.getMethodInfo().getLineNumber(0);
                        String[] perms = rp.value();
                        for (String perm : perms) {
                            mergedPermissions.add(perm);
                        }
                    }
                }
            }

            for (String perm : mergedPermissions) {
                Privilege entity = null;
                for (Privilege privilege : privileges) {
                    if (privilege.getCode().equals(perm)) {
                        entity = privilege;
                        break;
                    }
                }
                if (entity == null) {
                    entity = new Privilege();
                    entity.setCode(perm);
                }
                entity.setRebuildTime(now);
                privilegeService.save(entity);
            }


         // Clean up expired data useless Permissions
            for (Privilege privilege : privileges) {
                if (privilege.getRebuildTime().before(now)) {
                    privilegeService.delete(privilege);
                }
            }
        } catch (Exception e) {
            Exceptions.unchecked(e);
        }
    }
}
