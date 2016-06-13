package lab.s2jh.core.entity.def;

import lab.s2jh.core.annotation.MetaData;

public enum DynamicParameterTypeEnum {

    @MetaData(value = "date", comments = "Date date without a time-division format yyyy-MM-dd")
    DATE,

    @MetaData(value = "Date Time", comments = "Date with the division in the format : yyyy-MM-dd HH: mm: ss")
    TIMESTAMP,

    @MetaData("Float")
    FLOAT,

    @MetaData("Integer")
    INTEGER,

    @MetaData("Are Boolean")
    BOOLEAN,

    @MetaData(value = "Enumeration data definition", comments = "According to enumerate the object name () and getLabel () returns the corresponding key1-value1 structure data , listDataSource example corresponding wording : lab.s2jh.demo.po.entity.PurchaseOrder $ PurchaseOrderTypEnum")
    ENUM,

    @MetaData(value = "Data Dictionary drop-down list", comments = "Provides data dictionary table CATEGORY key1-value1 corresponding data structure , listDataSource example corresponding wording : PRIVILEGE_CATEGORY")
    DATA_DICT_LIST,

    @MetaData(value = "SQL Query drop-down list", comments = "Directly in the form of SQL statement returns the key-value data structure , corresponding listDataSource wording example : select role_code, role_name from t_sys_role")
    SQL_LIST,

    @MetaData(value = "OGNL key-value pair syntax", comments = "In OGNL syntactic constructs key-value data structure , corresponding listDataSource examples of the notation : # { 'A': 'ClassA', 'B': 'ClassB'}")
    OGNL_LIST,

    @MetaData("Multiple lines of text")
    MULTI_TEXT,

    @MetaData("HTML Text")
    HTML_TEXT,

    @MetaData("Single-line text")
    STRING;

}
