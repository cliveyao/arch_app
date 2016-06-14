package lab.s2jh.module.schedule.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Based on the timing task list Spring Schedule XML configuration suitable for defining each server 
 * node to perform and does not require logging , no management interface features such as 
 * support for intervention tasks
 *
 * Configuration example :
    <Task: scheduled-tasks scheduler = "springScheduler">
        <! - The task is triggered at regular intervals , in milliseconds - >
        <Task: scheduled ref = "systemSechduleService" method = "statOnlineUserCount" fixed-rate = "300000" />
    </ Task: scheduled-tasks>
 *
 */
@Component
public class SystemSechduleService {

    private final static Logger logger = LoggerFactory.getLogger(SystemSechduleService.class);

    /**
     * Statistics current number of online users , if the value exceeds the alert notification 
     * is sent to the administrator e-mail or SMS
     * @return
     */
    public Integer statOnlineUserCount() {
        logger.debug("Just mocking: statOnlineUserCount...");
        return 0;
    }
}
