package lab.s2jh.aud.entity;

import java.util.Date;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.entity.PersistableEntity;
import lab.s2jh.core.web.json.DateTimeJsonSerializer;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Getter
@Setter
@Accessors(chain = true)
@Access(AccessType.FIELD)
@Entity
@Table(name = "sche_JobRunHist")
@MetaData(value = "Task Scheduler run history")
@Cache(usage = CacheConcurrencyStrategy.NONE)
public class JobRunHist extends PersistableEntity<Long> {

    private static final long serialVersionUID = -5759986321900611939L;

    @Id
    @GeneratedValue(generator = "idGenerator")
    @GenericGenerator(name = "idGenerator", strategy = "native")
    private Long id;

    @MetaData(value = "Job Title")
    @Column(length = 64, nullable = true)
    private String jobName;

    @MetaData(value = "Job grouping")
    @Column(length = 64, nullable = true)
    private String jobGroup;

    @MetaData(value = "Job category")
    @Column(length = 512, nullable = true)
    private String jobClass;

    @MetaData(value = "Trigger name")
    @Column(length = 64, nullable = true)
    private String triggerName;

    @MetaData(value = "Trigger Packet ")
    @Column(length = 64, nullable = true)
    private String triggerGroup;

    @MetaData(value = "Abnormality flag")
    private Boolean exceptionFlag = Boolean.FALSE;

    @MetaData(value = "Results")
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private String result;

    @MetaData(value = "Exception log")
    @Lob
    @Basic(fetch = FetchType.LAZY)
    private String exceptionStack;

    //The following parameters are specifically refer to the official documentation Interface :
    //org.quartz.plugins.history.LoggingJobHistoryPlugin.LoggingJobHistoryPlugin#jobWasExecuted(JobExecutionContext context, JobExecutionException jobException)
    @MetaData(value = "The trigger time")
    @JsonSerialize(using = DateTimeJsonSerializer.class)
    private Date fireTime;

    @MetaData(value = "Last trigger time")
    @JsonSerialize(using = DateTimeJsonSerializer.class)
    private Date previousFireTime;

    @MetaData(value = "Next trigger time")
    @JsonSerialize(using = DateTimeJsonSerializer.class)
    private Date nextFireTime;

    @MetaData(value = "Trigger times")
    private Integer refireCount;

    @MetaData(value = "Trigger node identifier")
    private String nodeId;

    @Override
    @Transient
    public String getDisplay() {
        return jobClass + ":" + fireTime;
    }
}
