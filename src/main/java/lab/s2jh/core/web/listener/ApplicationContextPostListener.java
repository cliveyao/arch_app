package lab.s2jh.core.web.listener;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import lab.s2jh.core.cons.GlobalConstant;
import lab.s2jh.core.context.SpringContextHolder;
import lab.s2jh.support.service.DynamicConfigService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.google.common.collect.Maps;

/**
 * Spring loaded container "after" ServletContextListene
 */
public class ApplicationContextPostListener implements ServletContextListener {

    private final Logger logger = LoggerFactory.getLogger(ApplicationContextPostListener.class);

    public final static String Application_Configuation_Value_Key = "cfg";

    @Override
    public void contextInitialized(ServletContextEvent event) {
        logger.debug("Invoke ApplicationContextPostListener contextInitialized");
        try {
            ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(event.getServletContext());

            SpringContextHolder.setApplicationContext(applicationContext);

            ServletContext sc = event.getServletContext();
            String appName = sc.getServletContextName();
            logger.info("[{}] init context ...", appName);

            // Builds
            sc.setAttribute("build_version", new Boolean(DynamicConfigService.getBuildVersion()));

            DynamicConfigService dynamicConfigService = SpringContextHolder.getBean(DynamicConfigService.class);
            Map<String, Object> globalCfg = Maps.newHashMap();
            sc.setAttribute(Application_Configuation_Value_Key, globalCfg);
            // System Title
            globalCfg.put("cfg_system_title", dynamicConfigService.getString("cfg_system_title"));
           // Development mode boolean parameter
            globalCfg.put("dev_mode", new Boolean(dynamicConfigService.getString("dev_mode")));

            Map<String, Object> globalConstant = Maps.newHashMap();
            sc.setAttribute("cons", globalConstant);
            globalConstant.put("booleanLabelMap", GlobalConstant.booleanLabelMap);
        } catch (Exception e) {
            logger.error("error detail:", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        logger.debug("Invoke ApplicationContextPostListener contextDestroyed");
    }
}
