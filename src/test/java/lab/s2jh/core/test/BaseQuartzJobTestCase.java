package lab.s2jh.core.test;

import lab.s2jh.module.schedule.BaseQuartzJobBean;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

/**
 *Spring support database access , transaction control and dependency injection JUnit4 integration testing base class.
 * Compared to Spring primordia shorter class name and save the dataSource variable.
 *
 * Subclasses need to define the location applicationContext file , such as:
 * @ContextConfiguration(locations = { "/applicationContext.xml" })
 * 
 */
@ActiveProfiles("test")
@ContextConfiguration(locations = { "classpath:/context/context-profiles.xml", "classpath*:/context/spring*.xml" })
public abstract class BaseQuartzJobTestCase extends AbstractTransactionalJUnit4SpringContextTests {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void execute() {
        try {
            BaseQuartzJobBean jobBean = (BaseQuartzJobBean) getJobClass().newInstance();
            jobBean.executeInternal(null);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    abstract public Class<?> getJobClass();
}
