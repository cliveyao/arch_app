package lab.s2jh.module.sys.service;

import lab.s2jh.core.dao.jpa.BaseDao;
import lab.s2jh.core.pagination.GroupPropertyFilter;
import lab.s2jh.core.pagination.PropertyFilter;
import lab.s2jh.core.pagination.PropertyFilter.MatchType;
import lab.s2jh.core.service.BaseService;
import lab.s2jh.core.util.DateUtils;
import lab.s2jh.module.auth.entity.User;
import lab.s2jh.module.sys.dao.UserMessageDao;
import lab.s2jh.module.sys.entity.UserMessage;
import lab.s2jh.support.service.MailService;
import lab.s2jh.support.service.MessagePushService;
import lab.s2jh.support.service.SmsService;
import lab.s2jh.support.service.SmsService.SmsMessageTypeEnum;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserMessageService extends BaseService<UserMessage, Long> {

    private static final Logger logger = LoggerFactory.getLogger(UserMessageService.class);

    @Autowired
    private UserMessageDao userMessageDao;

    @Autowired(required = false)
    private MailService mailService;

    @Autowired(required = false)
    private SmsService smsService;

    @Autowired(required = false)
    private MessagePushService messagePushService;

    @Override
    protected BaseDao<UserMessage, Long> getEntityDao() {
        return userMessageDao;
    }

    /**
     * The number of user queries unread messages
     * @param User currently logged on user
     */
    @Transactional(readOnly = true)
    public Long findCountToRead(User user) {
        GroupPropertyFilter filter = GroupPropertyFilter.buildDefaultAndGroupFilter();
        filter.append(new PropertyFilter(MatchType.EQ, "targetUser", user));
        filter.append(new PropertyFilter(MatchType.NU, "firstReadTime", Boolean.TRUE));
        return count(filter);
    }

    public void processUserRead(UserMessage entity, User user) {
        if (entity.getFirstReadTime() == null) {
            entity.setFirstReadTime(DateUtils.currentDate());
            entity.setLastReadTime(entity.getFirstReadTime());
            entity.setReadTotalCount(1);
        } else {
            entity.setLastReadTime(DateUtils.currentDate());
            entity.setReadTotalCount(entity.getReadTotalCount() + 1);
        }
        userMessageDao.save(entity);
    }

    /**
     * Push message processing
     * @param entity
     */
    public void pushMessage(UserMessage entity) {

    	// User oriented message processing
        User targetUser = entity.getTargetUser();

     // Push mail processing
        if (entity.getEmailPush() && entity.getEmailPushTime() == null) {
            String email = targetUser.getEmail();
            if (StringUtils.isNotBlank(email)) {
                mailService.sendHtmlMail(entity.getTitle(), entity.getMessage(), true, email);
                entity.setEmailPushTime(DateUtils.currentDate());
            }
        }


     // SMS push processing
        if (entity.getSmsPush() && entity.getSmsPushTime() == null) {
            if (smsService != null) {
                String mobileNum = targetUser.getMobile();
                if (StringUtils.isNotBlank(mobileNum)) {
                    String errorMessage = smsService.sendSMS(entity.getNotification(), mobileNum, SmsMessageTypeEnum.Default);
                    if (StringUtils.isBlank(errorMessage)) {
                        entity.setSmsPushTime(DateUtils.currentDate());
                    }
                }
            } else {
                logger.warn("SmsService implement NOT found.");
            }

        }


     // APP Push
        if (entity.getAppPush() && entity.getAppPushTime() == null) {
            if (messagePushService != null) {
                Boolean pushResult = messagePushService.sendPush(entity);
                if (pushResult == null || pushResult) {
                    entity.setAppPushTime(DateUtils.currentDate());
                }
            } else {
                logger.warn("MessagePushService implement NOT found.");
            }
        }

        userMessageDao.save(entity);
    }

    @Override
    public UserMessage save(UserMessage entity) {
        super.save(entity);
        pushMessage(entity);
        return entity;
    }
}
