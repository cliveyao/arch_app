package lab.s2jh.core.data;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *Database initialization data processor triggers
 */
@Component
public class DatabaseDataInitializeTrigger {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseDataInitializeTrigger.class);

    @Autowired
    private DatabaseDataInitializeExecutor databaseDataInitializeExecutor;

    @PostConstruct
    public void initialize() {
        try {
            databaseDataInitializeExecutor.initialize();
        } catch (Exception e) {
            String msg = null;
            Throwable msgException = e;
            do {
                msg = msgException.getMessage();
                msgException = msgException.getCause();
            } while (msgException != null);

            if (msg != null && msg.indexOf("Transaction not active") > -1) {
                logger.warn(msg);
            } else {
                logger.warn(e.getMessage(), e);
            }
        }

    }

}
