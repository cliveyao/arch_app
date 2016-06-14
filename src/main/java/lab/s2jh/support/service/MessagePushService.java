package lab.s2jh.support.service;

import lab.s2jh.module.sys.entity.NotifyMessage;
import lab.s2jh.module.sys.entity.UserMessage;

/**
 * Push Service message APP Interface
 */
public interface MessagePushService {

    /**
     * Announcement Message Push Interface
     * @return Push Results : null = no need to push , true = push successfully ; false = push fails
     */
    Boolean sendPush(NotifyMessage notifyMessage);

    /**
     * Personal Message Push Interface
     * @return Push Results : null = no need to push , true = push successfully ; false = push fails
     */
    Boolean sendPush(UserMessage userMessage);
}
