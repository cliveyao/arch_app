package lab.s2jh.core.entity.def;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.entity.BaseNativeEntity;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

@MappedSuperclass
public abstract class DynamicParameterDef extends BaseNativeEntity {

    private static final long serialVersionUID = 3228409693335636739L;

    @MetaData("Code")
    private String code;

    @MetaData("name")
    private String title;

    @MetaData("description")
    private String description;

    @MetaData("Required identification")
    private Boolean required = Boolean.FALSE;

    @MetaData(value = "Disable Logo", tooltips = "Global disable items are not displayed")
    private Boolean disabled = Boolean.FALSE;

    @MetaData(value = "Hidden ID", tooltips = "Hide items without user input, generally you need to configure a reasonable defaultValue")
    private Boolean hidden = Boolean.FALSE;

    @MetaData("Parameter Type")
    private DynamicParameterTypeEnum type = DynamicParameterTypeEnum.STRING;

    @MetaData(value = "Front-end UI Validation Rules", tooltips = "如：{required:true,min:0,max:1000}")
    private String validateRules;

    @MetaData(value = "Default parameter values")
    private String defaultValue;

    @MetaData(value = "Whether to allow multiple selections ", tooltips = "Drop-down box for the data parameter")
    private Boolean multiSelectFlag = Boolean.FALSE;

    @MetaData(value = "Collection of data sources ", tooltips = "Specifies the data source for List types of data that defines how data is provided to the user selected ")
    private String listDataSource;

    @MetaData(value = "queue number", tooltips = "Relative ranking number, the larger the number, the closer the display")
    private Integer orderRank = 100;

    @Column(nullable = false, length = 64)
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Column(length = 32, nullable = false)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Column(length = 512, nullable = true)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public Integer getOrderRank() {
        return orderRank;
    }

    public void setOrderRank(Integer orderRank) {
        this.orderRank = orderRank;
    }

    @Enumerated(EnumType.STRING)
    @Column(length = 32, nullable = true)
    public DynamicParameterTypeEnum getType() {
        return type;
    }

    public void setType(DynamicParameterTypeEnum type) {
        this.type = type;
    }

    @Column(length = 256, nullable = true)
    public String getValidateRules() {
        return validateRules;
    }

    public void setValidateRules(String validateRules) {
        this.validateRules = validateRules;
    }

    @Column(length = 4000, nullable = true)
    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Boolean getMultiSelectFlag() {
        return multiSelectFlag;
    }

    public void setMultiSelectFlag(Boolean multiSelectFlag) {
        this.multiSelectFlag = multiSelectFlag;
    }

    @Column(length = 512, nullable = true)
    public String getListDataSource() {
        return listDataSource;
    }

    public void setListDataSource(String listDataSource) {
        this.listDataSource = listDataSource;
    }

    @Transient
    public String getFullValidateRules() {
        StringBuilder sb = new StringBuilder();
        switch (this.getType()) {
        case DATE:
            sb.append("date:true,");
            break;
        case TIMESTAMP:
            sb.append("timestamp:true,");
            break;
        case FLOAT:
            sb.append("number:true,");
            break;
        case INTEGER:
            sb.append("number:true,digits:true,");
            break;
        default:
        }
        if (BooleanUtils.toBoolean(this.required)) {
            sb.append("required:true,");
        }
        if (StringUtils.isNotBlank(this.getValidateRules())) {
            sb.append(this.getValidateRules() + ",");
        }
        if (sb.length() == 0) {
            return "";
        } else {
            return "{" + sb.substring(0, sb.length() - 1) + "}";
        }
    }

}
