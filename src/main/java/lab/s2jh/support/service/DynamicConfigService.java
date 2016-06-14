package lab.s2jh.support.service;

import java.util.List;
import java.util.Map;

import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.context.ExtPropertyPlaceholderConfigurer;
import lab.s2jh.core.util.DateUtils;
import lab.s2jh.module.sys.entity.ConfigProperty;
import lab.s2jh.module.sys.service.ConfigPropertyService;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Database configuration parameters based on dynamic load
 * Extended Attributes frame loaded : Spring In addition to loading attribute data from .properties
 * If there is a database of the same name and attribute takes precedence 
 * over the value of the database property values ​​in the configuration file overwrite
 * To avoid unexpected database configuration caused a system crash , have agreed to the parameters 
 * identified cfg starts showing database parameters can be overwritten , 
 * and the rest will not be overwritten file defines attribute values
 */
@Component
public class DynamicConfigService {

    private final Logger logger = LoggerFactory.getLogger(DynamicConfigService.class);

    @Autowired(required = false)
    private ExtPropertyPlaceholderConfigurer extPropertyPlaceholderConfigurer;

    @Autowired
    private ConfigPropertyService configPropertyService;

    @MetaData(value = "Development Model", comments = "Access control more relaxed , more log information . See application.properties configuration parameter definitions")
    private static boolean devMode = false;

    @MetaData(value = "Demo mode", comments = "Special control the presentation environment in order to avoid unnecessary random data modifications cause a system panic")
    private static boolean demoMode = false;

    @MetaData(value = "Builds")
    private static String buildVersion;

    @MetaData(value = "Build time")
    private static String buildTimestamp;

    public static boolean isDemoMode() {
        return demoMode;
    }

    public static boolean isDevMode() {
        return devMode;
    }

    public static String getBuildVersion() {
        return buildVersion;
    }

    @Value("${build_version}")
    public void setBuildVersion(String buildVersion) {
        DynamicConfigService.buildVersion = buildVersion;
        logger.info("System runnging at build_version={}", DynamicConfigService.buildVersion);
    }

    @Value("${build_timestamp:}")
    public void setBuildTimestamp(String buildTimestamp) {
        if (StringUtils.isBlank(buildTimestamp)) {
            buildTimestamp = DateUtils.formatTimeNow();
        }
        DynamicConfigService.buildTimestamp = buildTimestamp;
        logger.info("System runnging at build_timestamp={}", DynamicConfigService.buildTimestamp);
    }

    @Value("${demo_mode:false}")
    public void setDemoMode(String demoMode) {
        DynamicConfigService.demoMode = BooleanUtils.toBoolean(demoMode);
        logger.info("System runnging at demo_mode={}", DynamicConfigService.demoMode);
    }

    @Value("${dev_mode:false}")
    public void setDevMode(String devMode) {
        DynamicConfigService.devMode = BooleanUtils.toBoolean(devMode);
        logger.info("System runnging at dev_mode={}", DynamicConfigService.devMode);
    }

    /**
     * Get the corresponding dynamic parameter values ​​according to key
     */
    public String getString(String key) {
        return getString(key, null);
    }

    /**
     * Being based on key dynamic parameter values ​​correspond , if there is no return defaultValue
     */
    public String getString(String key, String defaultValue) {
        String val = null;

     // Cfg parameter starts , the first value from the database
        if (key.startsWith("cfg")) {
            ConfigProperty cfg = configPropertyService.findByPropKey(key);
            if (cfg != null) {
                val = cfg.getSimpleValue();
            }
        }


     // Get the variable from the environment
        if (val == null) {
            val = System.getProperty(key);
        }


     // Do not get to continue to define the properties file take from Spring
        if (val == null) {
            if (extPropertyPlaceholderConfigurer != null) {
                val = extPropertyPlaceholderConfigurer.getProperty(key);
            } else {
                logger.warn("ExtPropertyPlaceholderConfigurer not currently in expansion mode defined , and therefore can not get Spring to load configuration properties");
            }
        }
        if (val == null) {
            logger.warn("Undefined config property for: {}", key);
            return defaultValue;
        } else {
            return val.trim();
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return BooleanUtils.toBoolean(getString(key, String.valueOf(defaultValue)));
    }

    public Map<String, String> getAllPrperties() {
        Map<String, String> properties = extPropertyPlaceholderConfigurer.getPropertiesMap();
        List<ConfigProperty> configProperties = configPropertyService.findAllCached();
        for (ConfigProperty configProperty : configProperties) {
            properties.put(configProperty.getPropKey(), configProperty.getSimpleValue());
        }
        return properties;
    }
}
