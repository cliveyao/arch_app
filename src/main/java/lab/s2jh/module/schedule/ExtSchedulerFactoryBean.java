package lab.s2jh.module.schedule;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import lab.s2jh.module.schedule.entity.JobBeanCfg;
import lab.s2jh.module.schedule.service.JobBeanCfgService;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Extension of the standard SchedulerFactoryBean, initialization is accomplished based on the 
 * database configuration task manager
 */
public class ExtSchedulerFactoryBean extends SchedulerFactoryBean {

    private static Logger logger = LoggerFactory.getLogger(ExtSchedulerFactoryBean.class);

    private ConfigurableApplicationContext applicationContext;

    private JobBeanCfgService jobBeanCfgService;

    private boolean runWithinCluster = false;

    public static Map<String, Boolean> TRIGGER_HIST_MAPPING = Maps.newHashMap();

    public void setDataSource(DataSource dataSource) {
        super.setDataSource(dataSource);
        this.runWithinCluster = true;
    }

    public boolean isRunWithinCluster() {
        return runWithinCluster;
    }

    public void setJobBeanCfgService(JobBeanCfgService jobBeanCfgService) {
        this.jobBeanCfgService = jobBeanCfgService;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
        super.setApplicationContext(applicationContext);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void registerJobsAndTriggers() throws SchedulerException {
        logger.debug("Invoking registerJobsAndTriggers...");
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getBeanFactory();
        List<JobBeanCfg> jobBeanCfgs = jobBeanCfgService.findAll();
        List<Trigger> allTriggers = Lists.newArrayList();

        List<Trigger> triggers = null;
        try {

        	// Get the reflection-based triggers that have been defined in XML collections
            triggers = (List<Trigger>) FieldUtils.readField(this, "triggers", true);
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        }

        if (triggers == null) {
            triggers = Lists.newArrayList();
        } else {
            allTriggers.addAll(triggers);
        }

        for (JobBeanCfg jobBeanCfg : jobBeanCfgs) {

        	// Only deal with the current Scheduler cluster running pattern matching data
            if (jobBeanCfg.getRunWithinCluster() == null || !jobBeanCfg.getRunWithinCluster().equals(runWithinCluster)) {
                continue;
            }

         // Full class name to the task as Job and Trigger related names
            Class<?> jobClass = null;
            try {
                jobClass = Class.forName(jobBeanCfg.getJobClass());
            } catch (ClassNotFoundException e) {

            	// Fault tolerance avoid configuration errors can not start the application
                logger.error(e.getMessage(), e);
            }
            if (jobClass == null) {
                continue;
            }
            String jobName = jobClass.getName();

            boolean jobExists = false;
            for (Trigger trigger : triggers) {
                if (trigger.getJobKey().getName().equals(jobName)) {
                    jobExists = true;
                    break;
                }
            }
            if (jobExists) {
                logger.warn("WARN: Skipped dynamic  job [{}] due to exists static configuration.", jobName);
                continue;
            }

            logger.debug("Build and schedule dynamical job： {}, CRON: {}", jobName, jobBeanCfg.getCronExpression());


         // Spring loaded dynamically Job Bean
            BeanDefinitionBuilder bdbJobDetailBean = BeanDefinitionBuilder.rootBeanDefinition(JobDetailFactoryBean.class);
            bdbJobDetailBean.addPropertyValue("jobClass", jobBeanCfg.getJobClass());
            bdbJobDetailBean.addPropertyValue("durability", true);
            beanFactory.registerBeanDefinition(jobName, bdbJobDetailBean.getBeanDefinition());


         // Spring loaded dynamically Trigger Bean
            String triggerName = jobName + ".Trigger";
            JobDetail jobDetailBean = (JobDetail) beanFactory.getBean(jobName);
            BeanDefinitionBuilder bdbCronTriggerBean = BeanDefinitionBuilder.rootBeanDefinition(CronTriggerFactoryBean.class);
            bdbCronTriggerBean.addPropertyValue("jobDetail", jobDetailBean);
            bdbCronTriggerBean.addPropertyValue("cronExpression", jobBeanCfg.getCronExpression());
            beanFactory.registerBeanDefinition(triggerName, bdbCronTriggerBean.getBeanDefinition());

            allTriggers.add((Trigger) beanFactory.getBean(triggerName));
        }

        this.setTriggers(allTriggers.toArray(new Trigger[] {}));
        super.registerJobsAndTriggers();

        for (Trigger trigger : allTriggers) {
            TRIGGER_HIST_MAPPING.put(trigger.getJobKey().getName(), true);
            for (JobBeanCfg jobBeanCfg : jobBeanCfgs) {
                if (jobBeanCfg.getJobClass().equals(trigger.getJobKey().getName())) {

                	// Set the scheduled task AutoStartup initially set to the suspended state
                    if (!jobBeanCfg.getAutoStartup()) {
                        logger.debug("Setup trigger {} state to PAUSE", trigger.getKey().getName());
                        this.getScheduler().pauseTrigger(trigger.getKey());
                    }

                 // Set whether logging is turned on
                    TRIGGER_HIST_MAPPING.put(trigger.getJobKey().getName(), jobBeanCfg.getLogRunHist());
                    break;
                }
            }
        }
    }

    public static boolean isTriggerLogRunHist(Trigger trigger) {
        Boolean hist = TRIGGER_HIST_MAPPING.get(trigger.getJobKey().getName());
        return hist == null ? true : hist;
    }
}
