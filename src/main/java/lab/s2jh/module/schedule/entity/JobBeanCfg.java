package lab.s2jh.module.schedule.entity;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.entity.BaseNativeEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;

@Getter
@Setter
@Accessors(chain = true)
@Access(AccessType.FIELD)
@Entity
@Table(name = "sche_JobBeanCfg")
@MetaData(value = "Timing Task Configuration")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Audited
public class JobBeanCfg extends BaseNativeEntity {

    private static final long serialVersionUID = -416068377592076851L;

    @MetaData(value = "Task class full name", tooltips = "Achieve QuartzJobBean class full path name of the class ")
    @Column(length = 128, nullable = false, unique = true)
    private String jobClass;

    @MetaData(value = "CRON expression", tooltips = "Format Cron expression: Second Minute Week Day Month Year ( Optional )")
    @Column(length = 64, nullable = false)
    private String cronExpression;

    @MetaData(value = "Automatic initial run")
    @Column(nullable = false)
    private Boolean autoStartup = Boolean.TRUE;

    @MetaData(value = "Enable log", tooltips = "Each run will be written the history table for the operating frequency is high or monitor meaningful business tasks recommendation Close")
    @Column(nullable = false)
    private Boolean logRunHist = Boolean.TRUE;

    @MetaData(value = "Cluster operation mode", tooltips = "If true, then in a clustered environment, the same deployment task will only trigger a node <br/> otherwise, each node independently run")
    private Boolean runWithinCluster = Boolean.TRUE;

    @MetaData(value = "description")
    @Column(length = 1000, nullable = true)
    private String description;

    @MetaData(value = "Results template text")
    @Lob
    private String resultTemplate;
}
