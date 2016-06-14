package lab.s2jh.module.sys.entity;

import java.util.List;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.entity.BaseNativeEntity;
import lab.s2jh.core.web.json.EntityIdDisplaySerializer;
import lab.s2jh.core.web.json.JsonViews;
import lab.s2jh.module.sys.service.DataDictService;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Getter
@Setter
@Accessors(chain = true)
@Access(AccessType.FIELD)
@Entity
@Table(name = "sys_DataDict", uniqueConstraints = @UniqueConstraint(columnNames = { "parent_id", "primaryKey", "secondaryKey" }))
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@MetaData(value = "Data Dictionary")
@Audited
public class DataDict extends BaseNativeEntity {

    private static final long serialVersionUID = 5732022663570063926L;

    /** 
     * Primary identification dictionary data , the vast majority of cases for a single label will be able to determine the uniqueness of the main dictionary only need to maintain this data field values ​​to
     * Note : primaryKey + secondaryKey + parent uniqueness constraint
     */
    @MetaData(value = "Main Identity")
    @Column(length = 128, nullable = false)
    @JsonView(JsonViews.List.class)
    private String primaryKey;

    /** 
     * secondaryKey dictionary data value , if not the single determining primaryKey value and 
     * uniqueness of the combined values ​​can be enabled secondaryKey sole control
     */
    @MetaData(value = "Secondary logo")
    @Column(length = 128)
    @JsonView(JsonViews.List.class)
    private String secondaryKey;

    /**
     * Dictionary data corresponding to the data value of Value
     * Most cases are generally in the form of key-value data , and only need to maintain primaryKey primaryValue can,
     * Then {@link DataDictService # findChildrenByPrimaryKey (String)} to quickly return to the form of key-value data Map
     */
    @MetaData(value = "Main data")
    @JsonView(JsonViews.List.class)
    private String primaryValue;

    /**
     * Dictionary data corresponding supplemental data Value value , if in addition to primaryValue business 
     * design needs other supplementary data to enable extension Value field access these values
     * For the extended data acquisition typically by {@link lab.s2jh.sys.service.DataDictService 
     * #findByPrimaryKey (String)}
     * For the return of data , based on actual business use can be customized
     */
    @MetaData(value = "Secondary data")
    @JsonView(JsonViews.List.class)
    private String secondaryValue;

    /**
     * Dictionary data corresponding to the file type Value added value to the page file component maintenance mode
     * For the extended data acquisition typically by {@link lab.s2jh.sys.service.DataDictService 
     * #findByPrimaryKey (String)}
     * For the return of data , based on actual business use can be customized
     */
    @MetaData(value = "File Path")
    @JsonView(JsonViews.List.class)
    @Column(length = 512)
    private String filePathValue;
    
    /**
     * Dictionary data corresponding to the picture type Value added value , multi- page graph component maintenance mode
     * For the extended data acquisition typically by {@link lab.s2jh.sys.service.DataDictService # findByPrimaryKey (String)}
     * For the return of data , based on actual business use can be customized
     */
    @MetaData(value = "Image Data Path")
    @JsonView(JsonViews.List.class)
    @Column(length = 1024)
    private String imagePathValue;

    /**
     * Dictionary data corresponding supplemental data type Value large text value , 
     * if in addition to primaryValue business design needs other supplementary data to enable 
     * extension Value field access these values
     * For the extended data acquisition typically by {@link lab.s2jh.sys.service.DataDictService 
     * #findByPrimaryKey (String)}
     * For the return of data , based on actual business use can be customized
     */
    @MetaData(value = "Large text data", tooltips = "CLOB stored in large text for a particular configuration of large text data")
    @Lob
    @JsonView(JsonViews.Detail.class)
    private String richTextValue;

    @MetaData(value = "Disable Logo", tooltips = "Disabled items are not displayed globally")
    @JsonView(JsonViews.Admin.class)
    private Boolean disabled = Boolean.FALSE;

    @MetaData(value = "queue number", tooltips = "Relative ranking number, the larger the number, the closer the display")
    @JsonView(JsonViews.Admin.class)
    private Integer orderRank = 10;

    @MetaData(value = "Parent")
    @ManyToOne(cascade = CascadeType.DETACH)
    @JoinColumn(name = "parent_id", foreignKey = @ForeignKey(name = "none"))
    @JsonSerialize(using = EntityIdDisplaySerializer.class)
    @JsonView(JsonViews.Admin.class)
    private DataDict parent;

    @MetaData(value = "Child nodes")
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @OrderBy("orderRank desc")
    @JsonIgnore
    private List<DataDict> children;

    @Override
    @Transient
    public String getDisplay() {
        return primaryKey + ":" + primaryValue;
    }

    @Transient
    @JsonIgnore
    public String getUniqueKey() {
        StringBuilder sb = new StringBuilder();
        if (parent != null) {
            sb.append(parent.getPrimaryKey() + "_");
        }
        sb.append(primaryKey);
        if (StringUtils.isNotBlank(secondaryKey)) {
            sb.append("_" + secondaryKey);
        }
        return sb.toString();
    }
}
