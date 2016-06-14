package lab.s2jh.tool.builder;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * 用于代码生成处理的辅助对象
 */
public class EntityCodeField implements Comparable<EntityCodeField> {
    /** Property title */
    private String title;
    /** Java property name */
    private String fieldName;
    /** Property Description */
    private String description;
    /** Properties list jqGrid defined width */
    private Integer listWidth = 200;
    /** In the relative order of the generated code attribute */
    private Integer order = Integer.MAX_VALUE;
    /** Properties list jqGrid defined alignment: left, right, center */
    private String listAlign = "center";
    /** Attributes defined in the list jqGrid fixed width mode */
    private boolean listFixed = false;
    /** Attribute list jqGrid not defined in the default display mode */
    private boolean listHidden = false;
    /** Attribute to generate a form element in the editing interface */
    private boolean edit = true;
    /** Property generated column is defined in jqGrid list */
    private boolean list = true;
    /** Identifier attribute is an enumerated type, based on Java reflection to obtain the properties  */
    private Boolean enumField = false;
    /** Property type, according to the Java reflection to obtain property */
    private String fieldType;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getListFixed() {
        return listFixed;
    }

    public void setListFixed(boolean listFixed) {
        this.listFixed = listFixed;
    }

    public boolean getListHidden() {
        return listHidden;
    }

    public void setListHidden(boolean listHidden) {
        this.listHidden = listHidden;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public int compareTo(EntityCodeField o) {
        return order.compareTo(o.getOrder());
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public String getListAlign() {
        return listAlign;
    }

    public void setListAlign(String listAlign) {
        this.listAlign = listAlign;
    }

    public Integer getListWidth() {
        return listWidth;
    }

    public void setListWidth(Integer listWidth) {
        this.listWidth = listWidth;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public Boolean getEnumField() {
        return enumField;
    }

    public void setEnumField(Boolean enumField) {
        this.enumField = enumField;
    }

    public String getUncapitalizeFieldType() {
        return StringUtils.uncapitalize(fieldType);
    }

    public boolean isEdit() {
        return edit;
    }

    public void setEdit(boolean edit) {
        this.edit = edit;
    }

    public boolean getList() {
        return list;
    }

    public void setList(boolean list) {
        this.list = list;
    }

}
