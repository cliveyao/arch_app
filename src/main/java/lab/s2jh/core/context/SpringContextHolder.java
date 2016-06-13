package lab.s2jh.core.context;

import org.springframework.context.ApplicationContext;
import org.springframework.util.Assert;

/**
 * Spring container context
 */
public class SpringContextHolder {

    private static ApplicationContext applicationContext;

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void setApplicationContext(ApplicationContext applicationContext) {
        SpringContextHolder.applicationContext = applicationContext;
    }
    
    /**
     * Bean obtained from the static variable applicationContext , the automatic transition to the assignment object type.
     */
    public static <T> T getBean(Class<T> requiredType) {
        Assert.notNull(applicationContext);
        return applicationContext.getBean(requiredType);
    }
}
