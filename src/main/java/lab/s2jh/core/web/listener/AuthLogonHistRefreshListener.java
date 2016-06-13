package lab.s2jh.core.web.listener;

import java.util.Date;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import lab.s2jh.aud.entity.UserLogonLog;
import lab.s2jh.aud.service.UserLogonLogService;
import lab.s2jh.core.context.SpringContextHolder;
import lab.s2jh.core.pagination.GroupPropertyFilter;
import lab.s2jh.core.pagination.PropertyFilter;
import lab.s2jh.core.pagination.PropertyFilter.MatchType;
import lab.s2jh.core.util.DateUtils;

import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Update login records related to the listener through the login time
 */
public class AuthLogonHistRefreshListener implements HttpSessionListener, ServletContextListener {

    private final static Logger logger = LoggerFactory.getLogger(AuthLogonHistRefreshListener.class);

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        //Do nothing
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession session = se.getSession();
     // Based Shiro determine whether the session has been logged in before, because there were no public access so the constant direct reference to the code string take HttpServletSession
        if (session.getAttribute("org.apache.shiro.web.session.HttpServletSession.HOST_SESSION_KEY") != null) {
            String sessionId = session.getId();
            UserLogonLogService userLogonLogService = SpringContextHolder.getBean(UserLogonLogService.class);
            UserLogonLog userLogonLog = userLogonLogService.findBySessionId(sessionId);
            if (userLogonLog != null) {
                logger.debug("Setup logout time for session ID: {}", sessionId);
                userLogonLog.setLogoutTime(DateUtils.currentDate());
                userLogonLog.setLogonTimeLength(userLogonLog.getLogoutTime().getTime() - userLogonLog.getLogonTime().getTime());
                userLogonLogService.save(userLogonLog);
            }
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        //Do nothing
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        //在容器销毁时把未正常结束遗留的登录记录信息强制设置登出时间
        logger.info("ServletContext destroy force setup session user logout time...");

        UserLogonLogService userLogonLogService = SpringContextHolder.getBean(UserLogonLogService.class);
        GroupPropertyFilter groupPropertyFilter = GroupPropertyFilter.buildDefaultAndGroupFilter();
        groupPropertyFilter.append(new PropertyFilter(MatchType.NU, "logoutTime", Boolean.TRUE));
        List<UserLogonLog> userLogonLogs = userLogonLogService.findByFilters(groupPropertyFilter);
        if (!CollectionUtils.isEmpty(userLogonLogs)) {
            Date yesterday = new DateTime().minusDays(1).toDate();
            for (UserLogonLog userLogonLog : userLogonLogs) {
                //超过一天都没有登出的，直接强制设置登出时间
                if (userLogonLog.getLogonTime().before(yesterday)) {
                    Date logoutTime = new DateTime(userLogonLog.getLogonTime()).plusHours(1).toDate();
                    userLogonLog.setLogoutTime(logoutTime);

                    logger.debug(" - Setup logout time for session ID: {}", userLogonLog.getHttpSessionId());
                    userLogonLog.setLogonTimeLength(userLogonLog.getLogoutTime().getTime() - userLogonLog.getLogonTime().getTime());
                    userLogonLogService.save(userLogonLog);
                }
            }
        }
    }
}
