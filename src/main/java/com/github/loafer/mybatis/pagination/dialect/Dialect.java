package com.github.loafer.mybatis.pagination.dialect;

/**
 * Date Created  2014-2-18.
 *
 * @author loafer[zjh527@163.com]
 * @version 2.0
 */
public abstract class Dialect {
    public static enum Type{
        MYSQL,
        ORACLE
    }

    /**
     *Whether the database itself supports paging query
     *
     * @return {@code true} Support paging query
     */
    public abstract boolean supportsLimit();

    /**
     *  Packed into the sql database support specific query
     *
     * @param sql SQL statements
     * @param offset Starting position
     * @param limit The number of records to display per page
     * @return Exclusive paging query sql database
     */
    public abstract String getLimitString(String sql, int offset, int limit);

    /**
     * The total number of SQL sql packaged Statistics
     * @param sql SQL statements
     * @return Statistics Total SQL
     */
    public String getCountString(String sql){
        return "select count(1) from (" + sql + ") tmp_count";
    } 
    /**
     *  SQL statements into a single statement , and each word is a blank interval
     * @param sql
     * @return sql converted
     */
    protected String getLineSql(String sql){
        return sql.replaceAll("[\r\n]", " ").replaceAll("\\s{2,}", " ");
    }
}
