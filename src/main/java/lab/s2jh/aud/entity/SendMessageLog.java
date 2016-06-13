package lab.s2jh.aud.entity;

import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.entity.PersistableEntity;
import lab.s2jh.core.util.ExtStringUtils;
import lab.s2jh.core.util.WebFormatter;
import lab.s2jh.core.web.json.JsonViews;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;

@Getter
@Setter
@Accessors(chain = true)
@Access(AccessType.FIELD)
@Entity
@Table(name = "sys_SendMessageLog")
@Cache(usage = CacheConcurrencyStrategy.NONE)
@MetaData(value = "Send message record", comments = "Including email, SMS, push other news flow record")
public class SendMessageLog extends PersistableEntity<Long> {

    private static final long serialVersionUID = -541805294603254373L;

    @Id
    @GeneratedValue(generator = "idGenerator")
    @GenericGenerator(name = "idGenerator", strategy = "native")
    private Long id;

    @MetaData(value = "Message recipients")
    @Column(length = 300, nullable = false)
    private String targets;

    @MetaData(value = "title")
    @Column(length = 256, nullable = true)
    private String title;

    @MetaData(value = "Message content", comments = "It can be plain or formatted HTMl TEXT")
    @Lob
    @Column(nullable = false)
    @JsonIgnore
    private String message;

    @MetaData(value = "Message response", comments = "Such as JSON, HTML response text")
    @Lob
    @Column(nullable = true)
    private String response;

    @MetaData(value = "Message Type")
    @Column(length = 16, nullable = false)
    @Enumerated(EnumType.STRING)
    private SendMessageTypeEnum messageType;

    @MetaData(value = "Transmission time")
    @Column(nullable = false, updatable = false)
    private Date sendTime;

    public static enum SendMessageTypeEnum {
        @MetaData(value = "API")
        API,

        @MetaData(value = "e-mail")
        EMAIL,

        @MetaData(value = "SMS")
        SMS,

        @MetaData(value = "APP push notifications")
        APP_PUSH;
    }

    @Override
    @Transient
    public String getDisplay() {
        return title;
    }

    @Transient
    @JsonView(JsonViews.Admin.class)
    public String getMessageAbstract() {
        if (!StringUtils.isEmpty(message)) {
            String text = WebFormatter.html2text(message);
            return ExtStringUtils.cutRedundanceStr(text, 200);
        } else {
            return "";
        }
    }
}
