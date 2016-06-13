package lab.s2jh.core.dao.mybatis;

import java.util.List;
import java.util.Map;

import lab.s2jh.core.pagination.GroupPropertyFilter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface MyBatisDao {

    <E> List<E> findList(String namespace, String statementId, Map<String, Object> parameters);

    <E> List<E> findLimitList(String namespace, String statementId, Map<String, Object> parameters, Integer top);

    <E> List<E> findSortList(String namespace, String statementId, GroupPropertyFilter groupPropertyFilter, Sort sort);

    <E> List<E> findSortList(String namespace, String statementId, Map<String, Object> parameters, Sort sort);

    <E> Page<E> findPage(String namespace, String statementId, Map<String, Object> parameters, Pageable pageable);

    <E> Page<E> findPage(String namespace, String statementId, GroupPropertyFilter groupPropertyFilter, Pageable pageable);

    <V> Map<String, V> findMap(String namespace, String statementId, Map<String, Object> parameters, String mapKey);

    <V> Map<String, V> findMap(String namespace, String statementId, Map<String, Object> parameter, String mapKey, Integer top);

    /**
     *Perform update operations, including insert, update, delete
     * Full path @param namespace entity classes for general use , such as User.class.getName ()
     * @param StatementId insert, update, delete and other statements identified
     * @param Parameters required for the operation parameter object may be an object or entity , or other types Map
     * @return Note : Returns the number of records affected by the operation , not the primary key
     */

    // Since this framework priority to Hibernate for data management , and therefore only complex with MyBatis query , the data update operations do not interfere with Hibernate cache
    //int execute(String namespace, String statementId, Object parameters);
}
