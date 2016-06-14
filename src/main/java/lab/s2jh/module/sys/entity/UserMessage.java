package lab.s2jh.module.sys.entity;

import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.entity.BaseNativeEntity;
import lab.s2jh.core.util.WebFormatter;
import lab.s2jh.core.web.json.DateTimeJsonSerializer;
import lab.s2jh.core.web.json.EntityIdDisplaySerializer;
import lab.s2jh.core.web.json.JsonViews;
import lab.s2jh.core.web.json.ShortDateTimeJsonSerializer;
import lab.s2jh.module.auth.entity.User;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Getter
@Setter
@Accessors(chain = true)
@Access(AccessType.FIELD)
@Entity
@Table(name = "sys_UserMessage")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@MetaData(value = "User Message", comments = "If you worried about the impact the amount of message query efficiency , we can consider the introduction of a regular process to archive expired messages relocation archive")
public class UserMessage extends BaseNativeEntity {

    private static final long serialVersionUID = 1685596718660284598L;

    @MetaData(value = "Message Type", comments = "From the data dictionary definition of the message type")
    @Column(length = 32, nullable = true)
    private String type;

    @MetaData(value = "title")
    @Column(nullable = false)
    private String title;

    @MetaData(value = "APP prompted content", comments = "If you do not empty the trigger APP pop-up notification , is empty and will not pop up message push applications")
    @Column(length = 200)
    @JsonView(JsonViews.Admin.class)
    private String notification;

    @MetaData(value = "Message content", comments = "TEXT can be plain or formatted HTMl, generally in the mail or WEB page to view the HTML format details")
    @Lob
    @Column(nullable = false)
    @JsonView(JsonViews.AppDetail.class)
    private String message;

    @MetaData(value = "Target users")
    @ManyToOne
    @JoinColumn(name = "targetUser_id", nullable = false)
    @JsonSerialize(using = EntityIdDisplaySerializer.class)
    @JsonView(JsonViews.Admin.class)
    private User targetUser;

    @MetaData(value = "release time", comments = "Global message creation time")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    @Column(nullable = false)
    @JsonSerialize(using = ShortDateTimeJsonSerializer.class)
    private Date publishTime;

    @MetaData(value = "Push mail message")
    @JsonView(JsonViews.Admin.class)
    private Boolean emailPush = Boolean.FALSE;

    @MetaData(value = "Push mail message time", comments = "Null indicates that no push over")
    @JsonView(JsonViews.Admin.class)
    private Date emailPushTime;

    @MetaData(value = "SMS push messages")
    @JsonView(JsonViews.Admin.class)
    private Boolean smsPush = Boolean.FALSE;

    @MetaData(value = "SMS push message time", comments = "Null indicates that no push over")
    @JsonView(JsonViews.Admin.class)
    private Date smsPushTime;

    @MetaData(value = "APP push message")
    @JsonView(JsonViews.Admin.class)
    private Boolean appPush = Boolean.FALSE;

    @MetaData(value = "APP push message time", comments = "Null indicates that no push over")
    @JsonView(JsonViews.Admin.class)
    private Date appPushTime;

    @MetaData(value = "The size associated with attachment", comments = "Display a list of attachments and associated clean-up process is determined")
    @JsonView(JsonViews.Admin.class)
    private Integer attachmentSize;

    @MetaData(value = "First reading time")
    @JsonSerialize(using = DateTimeJsonSerializer.class)
    @JsonView(JsonViews.Admin.class)
    private Date firstReadTime;

    @MetaData(value = "Last Reading Time")
    @JsonSerialize(using = DateTimeJsonSerializer.class)
    @JsonView(JsonViews.Admin.class)
    private Date lastReadTime;

    @MetaData(value = "Total Views")
    @Column(nullable = false)
    private Integer readTotalCount = 0;

    @Override
    @Transient
    public String getDisplay() {
        return title;
    }

    @Transient
    @JsonView(JsonViews.Admin.class)
    public String getMessageAbstract() {
        if (StringUtils.isNotBlank(notification)) {
            return notification;
        } else {

        	// Extract HTML content optimized for text summary
            if (!StringUtils.isEmpty(message)) {
                return StringUtils.substring(WebFormatter.html2text(message), 0, 50).trim() + "...";
            } else {
                return "";
            }
        }
    }
}
