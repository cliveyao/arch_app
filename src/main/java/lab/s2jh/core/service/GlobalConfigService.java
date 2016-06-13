package lab.s2jh.core.service;

import lab.s2jh.core.annotation.MetaData;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GlobalConfigService {

    private static final Logger logger = LoggerFactory.getLogger(GlobalConfigService.class);

    @MetaData(value = "Development Model", comments = "Access control more relaxed , more log information . See application.properties configuration parameter definitions")
    private static boolean devMode = false;

    @MetaData(value = "Demo mode", comments = "Special control the presentation environment in order to avoid unnecessary random data modifications cause a system panic")
    private static boolean demoMode = false;

    @MetaData(value = "Builds")
    private static String buildVersion;

    public static boolean isDemoMode() {
        return demoMode;
    }

    public static boolean isDevMode() {
        return devMode;
    }

    @Value("${build_version}")
    public void setBuildVersion(String buildVersion) {
        GlobalConfigService.buildVersion = buildVersion;
        logger.info("System runnging at build_version={}", GlobalConfigService.buildVersion);
    }

    @Value("${demo_mode:false}")
    public void setDemoMode(String demoMode) {
        GlobalConfigService.demoMode = BooleanUtils.toBoolean(demoMode);
        logger.info("System runnging at demo_mode={}", GlobalConfigService.demoMode);
    }

    @Value("${dev_mode:false}")
    public void setDevMode(String devMode) {
        GlobalConfigService.devMode = BooleanUtils.toBoolean(devMode);
        logger.info("System runnging at dev_mode={}", GlobalConfigService.devMode);
    }

    public static String getBuildVersion() {
        return buildVersion;
    }
}
