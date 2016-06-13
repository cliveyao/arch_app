package lab.s2jh.core.web.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Spring loaded container "before" the ServletContextListener
 */
public class ApplicationContextPreListener implements ServletContextListener {

    private final Logger logger = LoggerFactory.getLogger(ApplicationContextPreListener.class);

    @Override
    public void contextInitialized(ServletContextEvent event) {
    	// Forced precedence IPV4 protocol
        logger.info("Set system property: {} = {}", "java.net.preferIPv4Stack", "true");
        System.setProperty("java.net.preferIPv4Stack", "true");

        logger.debug("Invoke ApplicationContextPreListener contextInitialized");
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        logger.debug("Invoke ApplicationContextPreListener contextDestroyed");
    }

}
