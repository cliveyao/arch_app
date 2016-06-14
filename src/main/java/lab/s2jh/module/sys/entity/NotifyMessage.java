package lab.s2jh.module.sys.entity;

import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.entity.BaseNativeEntity;
import lab.s2jh.core.util.WebFormatter;
import lab.s2jh.core.web.json.JsonViews;
import lab.s2jh.core.web.json.ShortDateTimeJsonSerializer;
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
@Table(name = "sys_NotifyMessage")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@MetaData(value = "Announcement Message")
public class NotifyMessage extends BaseNativeEntity {

    private static final long serialVersionUID = 2544390748513253055L;

    @MetaData(value = "Message Type", comments = "From the data dictionary definition of the message type")
    @Column(length = 32, nullable = true)
    private String type;

    @MetaData(value = "title")
    @Column(length = 128, nullable = false)
    private String title;

    @MetaData(value = "Logo into force", comments = "Arrange regular task , this value is updated based on publishTime and expireTime")
    @Column(nullable = true)
    private Boolean effective;

    @MetaData(value = "Identification must be logged in to access")
    @Column(nullable = false)
    private Boolean authRequired = Boolean.FALSE;

    @MetaData(value = "release time")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    @Column(nullable = false)
    @JsonSerialize(using = ShortDateTimeJsonSerializer.class)
    private Date publishTime;

    @MetaData(value = "Expire date", comments = "Blank means never expire")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    @JsonSerialize(using = ShortDateTimeJsonSerializer.class)
    private Date expireTime;

    @MetaData(value = "Platform Settings", comments = "No value = all other combinations of the following items separated by commas：web-admin,web-site,ios,android,winphone, @see NotifyMessage#NotifyMessagePlatformEnum")
    @Column(length = 200, nullable = true)
    private String platform;

    @MetaData(value = "Message and set the target attribute OR", comments = "With labels for large-scale equipment properties , user attribute grouping , among the elements OR take the union . = No value all the other data dictionary item comma -separated combinations such as : student, teacher")
    @Column(length = 1000, nullable = true)
    private String audienceTags;

    @MetaData(value = "Message target attribute AND intersection", comments = "With labels for large-scale equipment properties , user attribute grouping for AND intersected between each element. = No value all the other data dictionary item comma -separated combinations such as : student, school_01")
    @Column(length = 1000, nullable = true)
    private String audienceAndTags;

    @MetaData(value = "User ID list", comments = "alias list of User objects , among the elements OR take the union . = No value all the other data dictionary item comma -separated combinations such as : user_01, user_02")
    @Column(length = 1000, nullable = true)
    private String audienceAlias;

    @MetaData(value = "APP prompted content", comments = "If you do not empty the trigger APP pop-up notification , is empty and will not pop up message push applications")
    @Column(length = 200)
    private String notification;

    @MetaData(value = "Recently Pushed", comments = "Null indicates that no push over")
    private Date lastPushTime;

    @MetaData(value = "Message content", comments = "TEXT can be plain or formatted HTMl, generally in the mail or WEB page to view the HTML format details")
    @Lob
    @Column(nullable = false)
    @JsonView(JsonViews.AppDetail.class)
    private String message;

    @MetaData(value = "View total number of users")
    private Integer readUserCount = 0;

    @MetaData(value = "queue number", tooltips = "The higher the number the closer the display")
    private Integer orderRank = 100;

    @MetaData(value = "The number associated with attachment", comments = "Display a list of attachments and associated clean-up process is determined")
    private Integer attachmentSize;

    @Transient
    @MetaData(value = "Read identification")
    private Boolean readed;

    public static enum NotifyMessagePlatformEnum {

        @MetaData(value = "Back-end systems")
        web_admin,

        @MetaData(value = "The front end of the site")
        web_site,

        @MetaData(value = "Mobile Website")
        html5_site,

        @MetaData(value = "Apple iOS")
        ios,

        @MetaData(value = "Android")
        android,

        @MetaData(value = "Win Phone")
        winphone;

    }

    @Override
    @Transient
    public String getDisplay() {
        return title;
    }

    @Transient
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

    @Transient
    public boolean isPublic() {
        if (StringUtils.isBlank(audienceTags) && StringUtils.isBlank(audienceAndTags) && StringUtils.isBlank(audienceAlias)) {
            return true;
        }
        return false;
    }

    @MetaData(value = "Helper method : Forms for data binding")
    @Transient
    public String[] getPlatformSplit() {
        return StringUtils.isNotBlank(platform) ? platform.split(",") : null;
    }

    @MetaData(value = "Helper method : Forms for data binding")
    @Transient
    public void setPlatformSplit(String[] platformSplit) {
        platform = platformSplit != null ? StringUtils.join(platformSplit, ",") : null;
    }
}
