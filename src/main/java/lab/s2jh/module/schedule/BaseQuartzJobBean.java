package lab.s2jh.module.schedule;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import lab.s2jh.core.exception.ServiceException;
import lab.s2jh.module.schedule.entity.JobBeanCfg;
import lab.s2jh.module.schedule.service.JobBeanCfgService;
import lab.s2jh.support.service.FreemarkerService;

import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

/**
 *Custom Quartz Job base business object class definition
 * Business Job inherit this abstract base class , and thus gain the ability to get Spring ApplicationContext Spring Bean declaration objects
 * While achieving QuartzJobBean agreed interfaces , the preparation of regular processing logic
 */
public abstract class BaseQuartzJobBean extends QuartzJobBean {

    private static Logger logger = LoggerFactory.getLogger(BaseQuartzJobBean.class);

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private JobBeanCfgService jobBeanCfgService;

    @Autowired
    private FreemarkerService freemarkerService;

    /**
     * Based on the results of assembly tasks Freemarker text data
     * @param context
     * @param dataMap
     * @return
     */
    protected String buildJobResultByTemplate(JobExecutionContext context, Map<String, Object> dataMap) {
        JobBeanCfg jobBeanCfg = jobBeanCfgService.findByJobClass(context.getJobDetail().getJobClass().getName());
        if (jobBeanCfg != null) {
            String resultTemplate = jobBeanCfg.getResultTemplate();
            if (StringUtils.isNotBlank(resultTemplate)) {
                String result = freemarkerService.processTemplate(jobBeanCfg.getJobClass(), jobBeanCfg.getVersion(), resultTemplate, dataMap);
                return result;
            }
        }
        return "UNDEFINED";
    }

    @Override
    public void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            logger.debug("Invoking executeInternalBiz for {}", this.getClass());

            // Process @Autowired injection for the given target object, based on the current web application context. 
            SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);


         // JPA Session bound to the current thread
            if (!TransactionSynchronizationManager.hasResource(entityManagerFactory)) {
                EntityManager entityManager = entityManagerFactory.createEntityManager();
                TransactionSynchronizationManager.bindResource(entityManagerFactory, new EntityManagerHolder(entityManager));
            }

            String result = executeInternalBiz(context);

         // Unbundling JPA Session from the current thread
            EntityManagerHolder emHolder = (EntityManagerHolder) TransactionSynchronizationManager.unbindResource(entityManagerFactory);
            EntityManagerFactoryUtils.closeEntityManager(emHolder.getEntityManager());

            if (context != null && StringUtils.isNotBlank(result)) {
                context.setResult(result);
            }
            logger.debug("Job execution result: {}", result);
        } catch (Exception e) {
            logger.error("Quartz job execution error", e);
            throw new ServiceException(e.getMessage(), e);
        }
    }

    /**
     * Timing of realization of the internal logic of the task , not pay attention to the overall process 
     * control transaction boundaries , so if it comes to multiple data update logic need to pay attention to 
     * all the business logic is encapsulated into the relevant Service interface , and then call this method 
     * to ensure that a one-time transaction control
     * @param Context
     * @return Assembled task record result information , you can call buildJobResultByTemplate Based 
     * Freemarker template assembly complex response text message
     */
    protected abstract String executeInternalBiz(JobExecutionContext context);
}
