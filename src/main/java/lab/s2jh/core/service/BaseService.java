package lab.s2jh.core.service;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.Case;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;

import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.dao.jpa.BaseDao;
import lab.s2jh.core.exception.ServiceException;
import lab.s2jh.core.pagination.GroupPropertyFilter;
import lab.s2jh.core.pagination.PropertyFilter;
import lab.s2jh.core.pagination.PropertyFilter.MatchType;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.loader.criteria.CriteriaJoinWalker;
import org.hibernate.loader.criteria.CriteriaQueryTranslator;
import org.hibernate.persister.entity.OuterJoinLoadable;
import org.hibernate.transform.Transformers;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Persistable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public abstract class BaseService<T extends Persistable<? extends Serializable>, ID extends Serializable> {

    private final Logger logger = LoggerFactory.getLogger(BaseService.class);

    /** Class definitions corresponding generic */
    private Class<T> entityClass;

    /** Subclass set a specific DAO object instance */
    abstract protected BaseDao<T, ID> getEntityDao();

    @PersistenceContext
    private EntityManager entityManager;

    protected Class<T> getEntityClass() {
        if (entityClass == null) {
            try {
                // Entity achieved by reflection of the Class
                Object genericClz = getClass().getGenericSuperclass();
                if (genericClz instanceof ParameterizedType) {
                    entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
                }
            } catch (Exception e) {
                logger.error("error detail:", e);
            }
        }
        return entityClass;
    }

    protected EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * Forced refresh loaded from the database entity object
     * Mainly used for Spring DATA JPA Modifying mandatory refresh operation after re- loading data from the database
     * @param entity
     */
    protected void foreceRefreshEntity(Object entity) {
        entityManager.refresh(entity);
    }

    /**
     * Before you create additional data storage data callback method is empty by default logic , 
     * subclasses can add logic needed to override
     *
     * @param Entity
     * Data object to be created
     */
    protected void preInsert(T entity) {

    }

    /**
     * Additional data before updating the data stored callback method is empty by default logic ,
     *  subclasses can add logic needed to override
     *
     * @param Entity
     * Data object to be updated
     */
    protected void preUpdate(T entity) {

    }

    /**
     *Data save operation
     * 
     * @param entity
     * @return
     */
    public T save(T entity) {
        if (entity.isNew()) {
            preInsert(entity);
        } else {
            preUpdate(entity);
        }
        getEntityDao().save(entity);
        return entity;
    }

    /**
     * Bulk data save operation is simply to achieve its cycling set for each element calls {@link #save (Persistable)}
     * So no actual Batch batch processing, if you need to support your own database realize the underlying bulk
     *
     * @param Entities
     * Batch operation data to be set
     * @return
     */
    public List<T> save(Iterable<T> entities) {
        List<T> result = new ArrayList<T>();
        if (entities == null) {
            return result;
        }
        for (T entity : entities) {
            result.add(save(entity));
        }
        return result;
    }

    /**
     * Discover a single data object based on the primary key
     * 
     * @param id
     * @return
     */
    @Transactional(readOnly = true)
    public T findOne(ID id) {
        Assert.notNull(id);
        return getEntityDao().findOne(id);
    }

    /**
     * Discover a single data object based on the primary key
     *
     * @param Id primary key
     * @param InitLazyPropertyNames need to pre- initialize lazy collection property name
     * @return
     */
    @Transactional(readOnly = true)
    public T findDetachedOne(ID id, String... initLazyPropertyNames) {
        Assert.notNull(id);
        T entity = getEntityDao().findOne(id);
        if (initLazyPropertyNames != null && initLazyPropertyNames.length > 0) {
            for (String name : initLazyPropertyNames) {
                try {
                    Object propValue = MethodUtils.invokeMethod(entity, "get" + StringUtils.capitalize(name));
                    if (propValue != null && propValue instanceof Collection<?>) {
                        ((Collection<?>) propValue).size();
                    } else if (propValue != null && propValue instanceof Persistable<?>) {
                        ((Persistable<?>) propValue).getId();
                    }
                } catch (Exception e) {
                    throw new ServiceException("error.init.detached.entity", e);
                }
            }
        }
        getEntityManager().detach(entity);
        return entity;
    }

    /**
     * Based on the primary key of the collection query Data Objects
     *
     * @param Ids primary key set
     * @return
     */
    @Transactional(readOnly = true)
    public List<T> findAll(final ID... ids) {
        Assert.isTrue(ids != null && ids.length > 0, "必须提供有效查询主键集合");
        Specification<T> spec = new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
                @SuppressWarnings("rawtypes")
                Path expression = root.get("id");
                return expression.in(ids);
            }
        };
        return this.getEntityDao().findAll(spec);
    }

    /**
     * Data Delete operation
     *
     * @param Entity
     * Data to be operated
     */
    public void delete(T entity) {
        getEntityDao().delete(entity);
    }

    /**
     * Bulk data delete operation to achieve its cycling set for each element simply call {@link #delete (Persistable)}
     * So no actual Batch batch processing, if you need to support your own database realize the underlying bulk
     *
     * @param Entities
     * Batch operation data to be set
     * @return
     */
    public void delete(Iterable<T> entities) {
        for (T entity : entities) {
            delete(entity);
        }
    }

    /**
     *Discover a collection of objects based on the generic object properties and values
     *
     * @param Property attribute name , that object is the number of variable names
     * @param Value parameter value
     */
    public List<T> findListByProperty(final String property, final Object value) {
        Specification<T> spec = new Specification<T>() {
            @Override
            public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
                @SuppressWarnings("rawtypes")
                Path expression = root.get(property);
                return builder.equal(expression, value);
            }
        };

        return this.getEntityDao().findAll(spec);
    }

    /**
     * The only query object based on the generic object properties and values
     *
     * @param Property attribute name , that object is the number of variable names
     * @param Value parameter value
     * @return Not query to return null, if a query to multiple pieces of data an exception is thrown
     */
    public T findByProperty(final String property, final Object value) {
        List<T> entities = findListByProperty(property, value);
        if (CollectionUtils.isEmpty(entities)) {
            return null;
        } else {
            Assert.isTrue(entities.size() == 1);
            return entities.get(0);
        }
    }

    /**
     * The only query object based on the generic object properties and values
     *
     * @param Property attribute name , that object is the number of variable names
     * @param Value parameter value
     * @return Not query to return null, if a query returns data to a plurality of first
     */
    public T findFirstByProperty(final String property, final Object value) {
        List<T> entities = findListByProperty(property, value);
        if (CollectionUtils.isEmpty(entities)) {
            return null;
        } else {
            return entities.get(0);
        }
    }

    /**
     * Common object properties and values ​​query interface , based on the generic parameter determines the type of data returned
     *
     * @param BaseDao
     * Generic DAO interface parameter object
     * @param Property
     * Attribute name , that object is the number of variable names
     * @param Value
     * Parameter Value
     * @return Not query to return null, if a query to multiple pieces of data an exception is thrown
     */
    public <X> X findByProperty(BaseDao<X, ID> baseDao, final String property, final Object value) {
        Specification<X> spec = new Specification<X>() {
            @Override
            public Predicate toPredicate(Root<X> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
                String[] names = StringUtils.split(property, ".");
                @SuppressWarnings("rawtypes")
                Path expression = root.get(names[0]);
                for (int i = 1; i < names.length; i++) {
                    expression = expression.get(names[i]);
                }
                return builder.equal(expression, value);
            }
        };
        List<X> entities = baseDao.findAll(spec);
        if (CollectionUtils.isEmpty(entities)) {
            return null;
        } else {
            Assert.isTrue(entities.size() == 1);
            return entities.get(0);
        }
    }

    /**
     * Single condition object query data collection
     * 
     * @param propertyFilter
     * @return
     */
    @Transactional(readOnly = true)
    public List<T> findByFilter(PropertyFilter propertyFilter) {
        GroupPropertyFilter groupPropertyFilter = GroupPropertyFilter.buildDefaultAndGroupFilter(propertyFilter);
        Specification<T> spec = buildSpecification(groupPropertyFilter);
        return getEntityDao().findAll(spec);
    }

    /**
     * Based on search criteria count data recording
     * 
     * @param groupPropertyFilter
     * @return
     */
    @Transactional(readOnly = true)
    public long count(GroupPropertyFilter groupPropertyFilter) {
        Specification<T> spec = buildSpecification(groupPropertyFilter);
        return getEntityDao().count(spec);
    }

    /**
     * Query data objects based on a set of dynamic combination of conditions
     * 
     * @param groupPropertyFilter
     * @return
     */
    @Transactional(readOnly = true)
    public List<T> findByFilters(GroupPropertyFilter groupPropertyFilter) {
        Specification<T> spec = buildSpecification(groupPropertyFilter);
        return getEntityDao().findAll(spec);
    }

    /**
     * Based on a combination of dynamic objects and sort conditions defined query data collection
     * 
     * @param groupPropertyFilter
     * @param sort
     * @return
     */
    @Transactional(readOnly = true)
    public List<T> findByFilters(GroupPropertyFilter groupPropertyFilter, Sort sort) {
        Specification<T> spec = buildSpecification(groupPropertyFilter);
        return getEntityDao().findAll(spec, sort);
    }

    @Transactional(readOnly = true)
    public <X extends Persistable> List<X> findByFilters(Class<X> clazz, GroupPropertyFilter groupPropertyFilter, Sort sort) {
        Specification<X> spec = buildSpecification(groupPropertyFilter);
        return ((BaseDao) spec).findAll(spec, sort);
    }

    /**
    * Dynamic combination of conditions and sort objects based on the definition , 
    * limit the number of queries to query data collection
    * Mainly used for such queries Autocomplete avoid returning too much data
    * @param groupPropertyFilter
    * @param sort
    * @return
    */
    @Transactional(readOnly = true)
    public List<T> findByFilters(GroupPropertyFilter groupPropertyFilter, Sort sort, int limit) {
        Pageable pageable = new PageRequest(0, limit, sort);
        return findByPage(groupPropertyFilter, pageable).getContent();
    }

    /**
     * ( Including sorting ) object query data set based on a combination of dynamic objects and conditions tab
     * 
     * @param groupPropertyFilter
     * @param pageable
     * @return
     */
    @Transactional(readOnly = true)
    public Page<T> findByPage(GroupPropertyFilter groupPropertyFilter, Pageable pageable) {
        Specification<T> specifications = buildSpecification(groupPropertyFilter);
        return getEntityDao().findAll(specifications, pageable);
    }

    public String toSql(Criteria criteria) {
        CriteriaImpl criteriaImpl = (CriteriaImpl) criteria;
        SessionImplementor session = criteriaImpl.getSession();
        SessionFactoryImplementor factory = session.getFactory();
        CriteriaQueryTranslator translator = new CriteriaQueryTranslator(factory, criteriaImpl, criteriaImpl.getEntityOrClassName(),
                CriteriaQueryTranslator.ROOT_SQL_ALIAS);
        String[] implementors = factory.getImplementors(criteriaImpl.getEntityOrClassName());

        CriteriaJoinWalker walker = new CriteriaJoinWalker((OuterJoinLoadable) factory.getEntityPersister(implementors[0]), translator, factory,
                criteriaImpl, criteriaImpl.getEntityOrClassName(), session.getLoadQueryInfluencers());

        String sql = walker.getSQLString();
        return sql;
    }

    private class GroupAggregateProperty {
        @MetaData(value = "Literal property", comments = "The last key to the front end of JSON outputThe last key to the front end of JSON output")
        private String label;
        @MetaData(value = "JPA expression", comments = "Incoming content JPA CriteriaBuilder assembly")
        private String name;
        @MetaData(value = "JPA expression alias", comments = "Get aggregate value of alias is used")
        private String alias;

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

    }

    /**
     * Packet aggregation statistics , commonly used in a similar time period according to the account of the statistical merchandise sales profits , according to the general ledger accounts and statistics
     *
     * @param Clazz ROOT entity type
     * @param GroupFilter filter parameter object
     * @param Pageable tab ordering parameter object , TODO: currently there is a limit unrealized total number of records processed directly back to a fixed large numbers
     * @param Properties property collection , judgment rule : attribute name contains " ( " is identified as the polymerization property , the rest of packet attributes
     * Attribute syntax rules : sum = +, diff = -, prod = *, quot = /, case (condition, when, else)
     * Example:
     * Sum (amount)
     * Sum (diff (amount, costAmount))
     * Min (case (equal (amount, 0), - 1, quot (diff (amount, costAmount), amount)))
     * Case (equal (sum (amount), 0), - 1, quot (sum (diff (amount, costAmount)), sum (amount)))
     * @return Map Structure tab collection objects
     */
    public Page<Map<String, Object>> findByGroupAggregate(Class clazz, GroupPropertyFilter groupFilter, Pageable pageable, String... properties) {
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Tuple> criteriaQuery = criteriaBuilder.createTupleQuery();
        Root<?> root = criteriaQuery.from(clazz);

     // Pick grouping and aggregation attribute group to the existence of " ( " as a logo
        List<GroupAggregateProperty> groupProperties = Lists.newArrayList();
        List<GroupAggregateProperty> aggregateProperties = Lists.newArrayList();
        for (String prop : properties) {
            GroupAggregateProperty groupAggregateProperty = new GroupAggregateProperty();
         // Polymerization type expression
            if (prop.indexOf("(") > -1) {
            	// Processing as aliases
                prop = prop.replace(" AS ", " as ").replace(" As ", " as ").replace(" aS ", " as ");
                String[] splits = prop.split(" as ");
                String alias = null;
                String name = null;
                if (splits.length > 1) {
                    name = splits[0].trim();
                    alias = splits[1].trim();
                    groupAggregateProperty.setAlias(alias);
                    groupAggregateProperty.setLabel(alias);
                    groupAggregateProperty.setName(name);
                } else {
                    name = splits[0].trim();
                    alias = fixCleanAlias(name);
                    groupAggregateProperty.setAlias(alias);
                    groupAggregateProperty.setLabel(name);
                    groupAggregateProperty.setName(name);
                }
                aggregateProperties.add(groupAggregateProperty);
            } else {

            	// Direct property expression
                groupAggregateProperty.setAlias(fixCleanAlias(prop));
                groupAggregateProperty.setLabel(prop);
                groupAggregateProperty.setName(prop);
                groupProperties.add(groupAggregateProperty);
            }
        }

       // Build JPA Expression
        Expression<?>[] groupExpressions = buildExpressions(root, criteriaBuilder, groupProperties);
        Expression<?>[] aggregateExpressions = buildExpressions(root, criteriaBuilder, aggregateProperties);
        Expression<?>[] selectExpressions = ArrayUtils.addAll(groupExpressions, aggregateExpressions);
        CriteriaQuery<Tuple> select = criteriaQuery.multiselect(selectExpressions);


        // Based front-end dynamic conditions where dynamic objects assembled condition
        Predicate where = buildPredicatesFromFilters(groupFilter, root, criteriaQuery, criteriaBuilder, false);
        if (where != null) {
            select.where(where);
        }
        // Front-end dynamic conditions Dynamic Object -based assembly having condition
        Predicate having = buildPredicatesFromFilters(groupFilter, root, criteriaQuery, criteriaBuilder, true);
        if (having != null) {
            select.having(having);
        }

     // Paging and sorting process
        if (pageable != null && pageable.getSort() != null) {
            Iterator<Order> orders = pageable.getSort().iterator();
            List<javax.persistence.criteria.Order> jpaOrders = Lists.newArrayList();
            while (orders.hasNext()) {
                Order order = orders.next();
                String prop = order.getProperty();
                String alias = fixCleanAlias(prop);
             // Now found JPA does not support incoming alias sort attribute , we can only find a match based on the alias of Expression expression as the sort parameter
                List<Selection<?>> selections = select.getSelection().getCompoundSelectionItems();
                for (Selection<?> selection : selections) {
                    if (selection.getAlias().equals(alias)) {
                        if (order.isAscending()) {
                            jpaOrders.add(criteriaBuilder.asc((Expression<?>) selection));
                        } else {
                            jpaOrders.add(criteriaBuilder.desc((Expression<?>) selection));
                        }
                        break;
                    }
                }
            }
            select.orderBy(jpaOrders);
        }

     // Additional grouping parameter
        select.groupBy(groupExpressions);


     // Create a query object
        TypedQuery<Tuple> query = getEntityManager().createQuery(select);

     // Append dynamic parameters tab
        if (pageable != null) {
            query.setFirstResult(pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }

     // Get the result set , and the front end assembly is easy to JSON serialization Map structure
        List<Tuple> tuples = query.getResultList();
        List<Map<String, Object>> mapDatas = Lists.newArrayList();
        for (Tuple tuple : tuples) {
            Map<String, Object> data = Maps.newHashMap();
            for (GroupAggregateProperty groupAggregateProperty : groupProperties) {
                data.put(groupAggregateProperty.getLabel(), tuple.get(groupAggregateProperty.getAlias()));
            }
            for (GroupAggregateProperty groupAggregateProperty : aggregateProperties) {
                data.put(groupAggregateProperty.getLabel(), tuple.get(groupAggregateProperty.getAlias()));
            }
            mapDatas.add(data);
        }

     // TODO: currently has a limit of the total number of records processed unrealized directly back to a fixed large numbers
        return new PageImpl(mapDatas, pageable, Integer.MAX_VALUE);
    }

    /**
     * Based on the current generic entity object type is called packet statistics on an interface
     * @param groupFilter
     * @param pageable
     * @param properties
     * @return
     */
    public Page<Map<String, Object>> findByGroupAggregate(GroupPropertyFilter groupFilter, Pageable pageable, String... properties) {
        return findByGroupAggregate(getEntityClass(), groupFilter, pageable, properties);
    }

    /**
     * ( Excluding sort, sorting is defined directly in the native sql ) object query -based data collection and Native SQL tab
     *
     * @param Pageable tab ( without sorting , sorting defined directly in the native sql ) objects
     * @param Sql Native SQL ( good dynamic self-assembly and sort of native SQL statements , without order by part )
     * @return Map Structure tab collection objects
     */
    @Transactional(readOnly = true)
    public Page<Map> findByPageNativeSQL(Pageable pageable, String sql) {
        return findByPageNativeSQL(pageable, sql, null);
    }

    /**
     * ( Excluding sort, sorting is defined directly in the native sql ) object query -based data collection and Native SQL tab
     *
     * @param Pageable tab ( without sorting , sorting defined directly in the native sql ) objects
     * @param Sql Native SQL ( good dynamic self-assembly and sort of native SQL statements , without order by part )
     * @param Orderby order by part
     * @return Map Structure tab collection objects
     */
    @Transactional(readOnly = true)
    public Page<Map> findByPageNativeSQL(Pageable pageable, String sql, String orderby) {
        Query query = null;
        if (StringUtils.isNotBlank(orderby)) {
            query = getEntityManager().createNativeQuery(sql + " " + orderby);
        } else {
            query = getEntityManager().createNativeQuery(sql);
        }
        query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        Query queryCount = getEntityManager().createNativeQuery("select count(*) from (" + sql + ") cnt");
        query.setFirstResult(pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        Object count = queryCount.getSingleResult();
        return new PageImpl(query.getResultList(), pageable, Long.valueOf(count.toString()));
    }

    /**
     * Recording data based JPA common query criteria count
     * 
     * @param spec
     * @return
     */
    @Transactional(readOnly = true)
    private long count(Specification<T> spec) {
        return getEntityDao().count(spec);
    }

    private <X> Predicate buildPredicate(String propertyName, PropertyFilter filter, Root<X> root, CriteriaQuery<?> query, CriteriaBuilder builder,
            Boolean having) {
        Object matchValue = filter.getMatchValue();
        String[] names = StringUtils.split(propertyName, ".");

        if (matchValue == null) {
            return null;
        }
        if (having && propertyName.indexOf("(") == -1) {
            return null;
        }
        if (!having && propertyName.indexOf("(") > -1) {
            return null;
        }
        if (matchValue instanceof String) {
            if (StringUtils.isBlank(String.valueOf(matchValue))) {
                return null;
            }
        }

        if (filter.getMatchType().equals(MatchType.FETCH)) {
            if (names.length == 1) {
                JoinType joinType = JoinType.INNER;
                if (matchValue instanceof String) {
                    joinType = Enum.valueOf(JoinType.class, (String) matchValue);
                } else {
                    joinType = (JoinType) filter.getMatchValue();
                }

                // Hack for Bug: https://jira.springsource.org/browse/DATAJPA-105

               // If it is to calculate the total recorded in the count, then add join; otherwise normal 
                //paging query add fetch instructions
                if (!Long.class.isAssignableFrom(query.getResultType())) {
                    root.fetch(names[0], joinType);
                } else {
                    root.join(names[0], joinType);
                }
            } else {
                JoinType[] joinTypes = new JoinType[names.length];
                if (matchValue instanceof String) {
                    String[] joinTypeSplits = StringUtils.split(String.valueOf(matchValue), ".");
                    Assert.isTrue(joinTypeSplits.length == names.length, filter.getMatchType() + " Join the number of attributes and operations must be the same number of operations");
                    for (int i = 0; i < joinTypeSplits.length; i++) {
                        joinTypes[i] = Enum.valueOf(JoinType.class, joinTypeSplits[i].trim());
                    }
                } else {
                    joinTypes = (JoinType[]) filter.getMatchValue();
                    Assert.isTrue(joinTypes.length == names.length);
                }

                // Hack for Bug: https://jira.springsource.org/browse/DATAJPA-105

             // If it is to calculate the total recorded in the count, then add join; 
                //otherwise normal paging query add fetch instructions
                if (!Long.class.isAssignableFrom(query.getResultType())) {
                    Fetch fetch = root.fetch(names[0], joinTypes[0]);
                    for (int i = 1; i < names.length; i++) {
                        fetch.fetch(names[i], joinTypes[i]);
                    }
                } else {
                    Join join = root.join(names[0], joinTypes[0]);
                    for (int i = 1; i < names.length; i++) {
                        join.join(names[i], joinTypes[i]);
                    }
                }
            }

            return null;
        }

        Predicate predicate = null;
        Expression expression = null;


     // Processing set subqueries
        Subquery<Long> subquery = null;
        Root subQueryFrom = null;
        if (filter.getSubQueryCollectionPropetyType() != null) {
            subquery = query.subquery(Long.class);
            subQueryFrom = subquery.from(filter.getSubQueryCollectionPropetyType());
            Path path = subQueryFrom.get(names[1]);
            if (names.length > 2) {
                for (int i = 2; i < names.length; i++) {
                    path = path.get(names[i]);
                }
            }
            expression = (Expression) path;
        } else {
            expression = buildExpression(root, builder, propertyName, null);
        }

        if ("NULL".equalsIgnoreCase(String.valueOf(matchValue))) {
            return expression.isNull();
        } else if ("EMPTY".equalsIgnoreCase(String.valueOf(matchValue))) {
            return builder.or(builder.isNull(expression), builder.equal(expression, ""));
        } else if ("NONULL".equalsIgnoreCase(String.valueOf(matchValue))) {
            return expression.isNotNull();
        } else if ("NOEMPTY".equalsIgnoreCase(String.valueOf(matchValue))) {
            return builder.and(builder.isNotNull(expression), builder.notEqual(expression, ""));
        }

        // logic operator
        switch (filter.getMatchType()) {
        case EQ:
        	// Date special treatment : Usually the end time for the date range queries , such as queries before 2012-01-01, and 2010-01-01 generally need to show the same day and the previous data ,
            // The database generally there are minutes and seconds , and therefore require special treatment to the current date + 1 day , converted to < 2012-01-02 query
            if (matchValue instanceof Date) {
                DateTime dateTime = new DateTime(((Date) matchValue).getTime());
                if (dateTime.getHourOfDay() == 0 && dateTime.getMinuteOfHour() == 0 && dateTime.getSecondOfMinute() == 0) {
                    return builder.and(builder.greaterThanOrEqualTo(expression, dateTime.toDate()),
                            builder.lessThan(expression, dateTime.plusDays(1).toDate()));
                }
            }
            predicate = builder.equal(expression, matchValue);
            break;
        case NE:

        	// Date special treatment : Usually the end time for the date range queries , such as queries before 2012-01-01, and 2010-01-01 generally need to show the same day and the previous data ,
        	// The database generally there are minutes and seconds , and therefore require special treatment to the current date + 1 day , converted to < 2012-01-02 query
            if (matchValue instanceof Date) {
                DateTime dateTime = new DateTime(((Date) matchValue).getTime());
                if (dateTime.getHourOfDay() == 0 && dateTime.getMinuteOfHour() == 0 && dateTime.getSecondOfMinute() == 0) {
                    return builder.or(builder.lessThan(expression, dateTime.toDate()),
                            builder.greaterThanOrEqualTo(expression, dateTime.plusDays(1).toDate()));
                }
            }
            predicate = builder.notEqual(expression, matchValue);
            break;
        case BK:
            predicate = builder.or(builder.isNull(expression), builder.equal(expression, ""));
            break;
        case NB:
            predicate = builder.and(builder.isNotNull(expression), builder.notEqual(expression, ""));
            break;
        case NU:
            if (matchValue instanceof Boolean && (Boolean) matchValue == false) {
                predicate = builder.isNotNull(expression);
            } else {
                predicate = builder.isNull(expression);
            }
            break;
        case NN:
            if (matchValue instanceof Boolean && (Boolean) matchValue == false) {
                predicate = builder.isNull(expression);
            } else {
                predicate = builder.isNotNull(expression);
            }
            break;
        case CN:
            predicate = builder.like(expression, "%" + matchValue + "%");
            break;
        case NC:
            predicate = builder.notLike(expression, "%" + matchValue + "%");
            break;
        case BW:
            predicate = builder.like(expression, matchValue + "%");
            break;
        case BN:
            predicate = builder.notLike(expression, matchValue + "%");
            break;
        case EW:
            predicate = builder.like(expression, "%" + matchValue);
            break;
        case EN:
            predicate = builder.notLike(expression, "%" + matchValue);
            break;
        case BT:
            Assert.isTrue(matchValue.getClass().isArray(), "Match value must be array");
            Object[] matchValues = (Object[]) matchValue;
            Assert.isTrue(matchValues.length == 2, "Match value must have two value");
            if (matchValues[0] instanceof Date) {
                DateTime dateFrom = new DateTime(((Date) matchValues[0]).getTime());
                return builder.and(builder.greaterThanOrEqualTo(expression, (Date) matchValues[0]),
                        builder.lessThan(expression, (Date) matchValues[1]));
            } else {
                return builder.between(expression, (Comparable) matchValues[0], (Comparable) matchValues[1]);
            }
        case GT:
            predicate = builder.greaterThan(expression, (Comparable) matchValue);
            break;
        case GE:
            predicate = builder.greaterThanOrEqualTo(expression, (Comparable) matchValue);
            break;
        case LT:
        	// Date special treatment : Usually the end time for the date range queries , such as queries before 2012-01-01, and 2010-01-01 generally need to show the same day and the previous data ,
            // The database generally there are minutes and seconds , and therefore require special treatment to the current date + 1 day , converted to < 2012-01-02 query
            if (matchValue instanceof Date) {
                DateTime dateTime = new DateTime(((Date) matchValue).getTime());
                if (dateTime.getHourOfDay() == 0 && dateTime.getMinuteOfHour() == 0 && dateTime.getSecondOfMinute() == 0) {
                    return builder.lessThan(expression, dateTime.plusDays(1).toDate());
                }
            }
            predicate = builder.lessThan(expression, (Comparable) matchValue);
            break;
        case LE:

        	// Date special treatment : Usually the end time for the date range queries , such as queries before 2012-01-01, and 2010-01-01 generally need to show the same day and the previous data ,
        	// The database generally there are minutes and seconds , and therefore require special treatment to the current date + 1 day , converted to < 2012-01-02 query
            if (matchValue instanceof Date) {
                DateTime dateTime = new DateTime(((Date) matchValue).getTime());
                if (dateTime.getHourOfDay() == 0 && dateTime.getMinuteOfHour() == 0 && dateTime.getSecondOfMinute() == 0) {
                    return builder.lessThan(expression, dateTime.plusDays(1).toDate());
                }
            }
            predicate = builder.lessThanOrEqualTo(expression, (Comparable) matchValue);
            break;
        case IN:
            if (matchValue.getClass().isArray()) {
                predicate = expression.in((Object[]) matchValue);
            } else if (matchValue instanceof Collection) {
                predicate = expression.in((Collection) matchValue);
            } else {
                predicate = builder.equal(expression, matchValue);
            }
            break;
        case ACLPREFIXS:
            List<Predicate> aclPredicates = Lists.newArrayList();
            Collection<String> aclCodePrefixs = (Collection<String>) matchValue;
            for (String aclCodePrefix : aclCodePrefixs) {
                if (StringUtils.isNotBlank(aclCodePrefix)) {
                    aclPredicates.add(builder.like(expression, aclCodePrefix + "%"));
                }

            }
            if (aclPredicates.size() == 0) {
                return null;
            }
            predicate = builder.or(aclPredicates.toArray(new Predicate[aclPredicates.size()]));
            break;
        case PLT:
            Expression expressionPLT = buildExpression(root, builder, (String) matchValue, null);
            predicate = builder.lessThan(expression, expressionPLT);
            break;
        case PLE:
            Expression expressionPLE = buildExpression(root, builder, (String) matchValue, null);
            predicate = builder.lessThanOrEqualTo(expression, expressionPLE);
            break;
        default:
            break;
        }

     // Processing set subqueries
        if (filter.getSubQueryCollectionPropetyType() != null) {
            String owner = StringUtils.uncapitalize(getEntityClass().getSimpleName());
            subQueryFrom.join(owner);
            subquery.select(subQueryFrom.get(owner)).where(predicate);
            predicate = builder.in(root.get("id")).value(subquery);
        }

        Assert.notNull(predicate, "Undefined match type: " + filter.getMatchType());
        return predicate;
    }

    /**
     * According to the conditions set objects assembled JPA specification criteria query collection object , 
     * the base class default implementation conditional package combinations
     * A subclass can call this method List <Predicate> additional return additional form other PropertyFilter 
     * difficult conditions such as conditions exist processing
     * 
     * @param filters
     * @param root
     * @param query
     * @param builder
     * @return
     */
    private <X> List<Predicate> buildPredicatesFromFilters(final Collection<PropertyFilter> filters, Root<X> root, CriteriaQuery<?> query,
            CriteriaBuilder builder, Boolean having) {
        List<Predicate> predicates = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(filters)) {
            for (PropertyFilter filter : filters) {
                if (!filter.hasMultiProperties()) { 
                	// Only if a property to be compared .
                    Predicate predicate = buildPredicate(filter.getConvertedPropertyName(), filter, root, query, builder, having);
                    if (predicate != null) {
                        predicates.add(predicate);
                    }
                } else {// Contains a plurality of attributes to be compared , performed or processed .
                    List<Predicate> orpredicates = Lists.newArrayList();
                    for (String param : filter.getConvertedPropertyNames()) {
                        Predicate predicate = buildPredicate(param, filter, root, query, builder, having);
                        if (predicate != null) {
                            orpredicates.add(predicate);
                        }
                    }
                    if (orpredicates.size() > 0) {
                        predicates.add(builder.or(orpredicates.toArray(new Predicate[orpredicates.size()])));
                    }
                }
            }
        }
        return predicates;
    }

    private <X extends Persistable> Specification<X> buildSpecification(final GroupPropertyFilter groupPropertyFilter) {
        return new Specification<X>() {
            @Override
            public Predicate toPredicate(Root<X> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
                if (groupPropertyFilter != null) {
                    return buildPredicatesFromFilters(groupPropertyFilter, root, query, builder);
                } else {
                    return null;
                }
            }
        };
    }

    protected Predicate buildPredicatesFromFilters(GroupPropertyFilter groupPropertyFilter, Root root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        return buildPredicatesFromFilters(groupPropertyFilter, root, query, builder, false);
    }

    protected Predicate buildPredicatesFromFilters(GroupPropertyFilter groupPropertyFilter, Root root, CriteriaQuery<?> query,
            CriteriaBuilder builder, Boolean having) {
        if (groupPropertyFilter == null) {
            return null;
        }
        List<Predicate> predicates = buildPredicatesFromFilters(groupPropertyFilter.getFilters(), root, query, builder, having);
        if (CollectionUtils.isNotEmpty(groupPropertyFilter.getGroups())) {
            for (GroupPropertyFilter group : groupPropertyFilter.getGroups()) {
                if (CollectionUtils.isEmpty(group.getFilters()) && CollectionUtils.isEmpty(group.getForceAndFilters())) {
                    continue;
                }
                Predicate subPredicate = buildPredicatesFromFilters(group, root, query, builder, having);
                if (subPredicate != null) {
                    predicates.add(subPredicate);
                }
            }
        }
        Predicate predicate = null;
        if (CollectionUtils.isNotEmpty(predicates)) {
            if (predicates.size() == 1) {
                predicate = predicates.get(0);
            } else {
                if (groupPropertyFilter.getGroupType().equals(GroupPropertyFilter.GROUP_OPERATION_OR)) {
                    predicate = builder.or(predicates.toArray(new Predicate[predicates.size()]));
                } else {
                    predicate = builder.and(predicates.toArray(new Predicate[predicates.size()]));
                }
            }
        }

        List<Predicate> appendAndPredicates = buildPredicatesFromFilters(groupPropertyFilter.getForceAndFilters(), root, query, builder, having);
        if (CollectionUtils.isNotEmpty(appendAndPredicates)) {
            if (predicate != null) {
                appendAndPredicates.add(predicate);
            }
            predicate = builder.and(appendAndPredicates.toArray(new Predicate[appendAndPredicates.size()]));
        }

        return predicate;
    }

    /**
     * Subclass extra additional filter restrictions entrance method , generally based on the currently logged-in user to force an additional filter
     * 
     * @param filters
     */
    protected List<Predicate> appendPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        return null;
    }

    private Expression parseExpr(Root<?> root, CriteriaBuilder criteriaBuilder, String expr, Map<String, Expression<?>> parsedExprMap) {
        if (parsedExprMap == null) {
            parsedExprMap = Maps.newHashMap();
        }
        Expression<?> expression = null;
        if (expr.indexOf("(") > -1) {
            int left = 0;
            char[] chars = expr.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                if (chars[i] == '(') {
                    left = i;
                }
            }
            String leftStr = expr.substring(0, left);
            String op = null;
            char[] leftStrs = leftStr.toCharArray();
            for (int i = leftStrs.length - 1; i > 0; i--) {
                if (leftStrs[i] == '(' || leftStrs[i] == ')' || leftStrs[i] == ',') {
                    op = leftStr.substring(i + 1);
                    break;
                }
            }
            if (op == null) {
                op = leftStr;
            }
            String rightStr = expr.substring(left + 1);
            String arg = StringUtils.substringBefore(rightStr, ")");
            String[] args = arg.split(",");
            //logger.debug("op={},arg={}", op, arg);
            if (op.equalsIgnoreCase("case")) {
                Case selectCase = criteriaBuilder.selectCase();

                Expression caseWhen = parsedExprMap.get(args[0]);

                String whenResultExpr = args[1];
                Object whenResult = parsedExprMap.get(whenResultExpr);
                if (whenResult == null) {
                    Case<Long> whenCase = selectCase.when(caseWhen, new BigDecimal(whenResultExpr));
                    selectCase = whenCase;
                } else {
                    Case<Expression<?>> whenCase = selectCase.when(caseWhen, whenResult);
                    selectCase = whenCase;
                }
                String otherwiseResultExpr = args[2];
                Object otherwiseResult = parsedExprMap.get(otherwiseResultExpr);
                if (otherwiseResult == null) {
                    expression = selectCase.otherwise(new BigDecimal(otherwiseResultExpr));
                } else {
                    expression = selectCase.otherwise((Expression<?>) otherwiseResult);
                }
            } else {
                Object[] subExpressions = new Object[args.length];
                for (int i = 0; i < args.length; i++) {
                    subExpressions[i] = parsedExprMap.get(args[i]);
                    if (subExpressions[i] == null) {
                        String name = args[i];
                        try {
                            Path<?> item = null;
                            if (name.indexOf(".") > -1) {
                                String[] props = StringUtils.split(name, ".");
                                item = root.get(props[0]);
                                for (int j = 1; j < props.length; j++) {
                                    item = item.get(props[j]);
                                }
                            } else {
                                item = root.get(name);
                            }
                            subExpressions[i] = (Expression) item;
                        } catch (Exception e) {
                            subExpressions[i] = new BigDecimal(name);
                        }
                    }
                }
                try {
                    //criteriaBuilder.quot();
                    expression = (Expression) MethodUtils.invokeMethod(criteriaBuilder, op, subExpressions);
                } catch (Exception e) {
                    logger.error("Error for aggregate  setting ", e);
                }
            }

            String exprPart = op + "(" + arg + ")";
            String exprPartConvert = exprPart.replace(op + "(", op + "_").replace(arg + ")", arg + "_").replace(",", "_");
            expr = expr.replace(exprPart, exprPartConvert);
            parsedExprMap.put(exprPartConvert, expression);

            if (expr.indexOf("(") > -1) {
                expression = parseExpr(root, criteriaBuilder, expr, parsedExprMap);
            }
        } else {
            String name = expr;
            Path<?> item = null;
            if (name.indexOf(".") > -1) {
                String[] props = StringUtils.split(name, ".");
                item = root.get(props[0]);
                for (int j = 1; j < props.length; j++) {
                    item = item.get(props[j]);
                }
            } else {
                item = root.get(name);
            }
            expression = item;
        }
        return expression;
    }

    private String fixCleanAlias(String name) {
        return StringUtils.remove(StringUtils.remove(StringUtils.remove(StringUtils.remove(StringUtils.remove(name, "("), ")"), "."), ","), "-");
    }

    private Expression<?> buildExpression(Root<?> root, CriteriaBuilder criteriaBuilder, String name, String alias) {
        Expression<?> expr = parseExpr(root, criteriaBuilder, name, null);
        if (alias != null) {
            expr.alias(alias);
        }
        return expr;
    }

    private Expression<?>[] buildExpressions(Root<?> root, CriteriaBuilder criteriaBuilder, List<GroupAggregateProperty> groupAggregateProperties) {
        Expression<?>[] parsed = new Expression<?>[groupAggregateProperties.size()];
        int i = 0;
        for (GroupAggregateProperty groupAggregateProperty : groupAggregateProperties) {
            parsed[i++] = buildExpression(root, criteriaBuilder, groupAggregateProperty.getName(), groupAggregateProperty.getAlias());
        }
        return parsed;
    }

    private Selection<?>[] mergeSelections(Root<?> root, Selection<?>[] path1, Selection<?>... path2) {
        Selection<?>[] parsed = new Selection<?>[path1.length + path2.length];
        int i = 0;
        for (Selection<?> path : path1) {
            parsed[i++] = path;
        }
        for (Selection<?> path : path2) {
            parsed[i++] = path;
        }
        return parsed;
    }

    /**
     * Association Object relationships helper methods for subclass operations call
     *
     * @param Id
     * Currently associated with the primary object primary key , such as User objects in the primary key
     * @param R2EntityIds
     * Primary key primary key set associated with an object , such as a user association roles Role collection of objects
     * @param R2PropertyName
     * The name of the primary object associated with the collection of object properties , such as userR2Roles property name User defined objects
     * @param R2EntityPropertyName
     * The associated object attributes associated with the object name in the definition of R2 , as defined in the role UserR2Role property name
     * @param Op
     * Associated with the type of operation , such as add, del , etc. , @see # R2OperationEnum
     */
    protected void updateRelatedR2s(ID id, Collection<? extends Serializable> r2EntityIds, String r2PropertyName, String r2EntityPropertyName,
            R2OperationEnum op) {
        try {
            T entity = findOne(id);
            List oldR2s = (List) FieldUtils.readDeclaredField(entity, r2PropertyName, true);

            Field r2field = FieldUtils.getField(getEntityClass(), r2PropertyName, true);
            Class r2Class = (Class) (((ParameterizedType) r2field.getGenericType()).getActualTypeArguments()[0]);
            Field entityField = null;
            Field[] fields = r2Class.getDeclaredFields();
            for (Field field : fields) {
                if (field.getType().equals(getEntityClass())) {
                    entityField = field;
                    break;
                }
            }

            Field r2EntityField = FieldUtils.getField(r2Class, r2EntityPropertyName, true);
            Class r2EntityClass = r2EntityField.getType();

            if (R2OperationEnum.update.equals(op)) {
                if (CollectionUtils.isEmpty(r2EntityIds) && !CollectionUtils.isEmpty(oldR2s)) {
                    oldR2s.clear();
                }
            }

            if (R2OperationEnum.update.equals(op) || R2OperationEnum.add.equals(op)) {
            	// Project need to add dual loop process associated
                for (Serializable r2EntityId : r2EntityIds) {
                    Object r2Entity = getEntityManager().find(r2EntityClass, r2EntityId);
                    boolean tobeAdd = true;
                    for (Object r2 : oldR2s) {
                        if (FieldUtils.readDeclaredField(r2, r2EntityPropertyName, true).equals(r2Entity)) {
                            tobeAdd = false;
                            break;
                        }
                    }
                    if (tobeAdd) {
                        Object newR2 = r2Class.newInstance();
                        FieldUtils.writeDeclaredField(newR2, r2EntityField.getName(), r2Entity, true);
                        FieldUtils.writeDeclaredField(newR2, entityField.getName(), entity, true);
                        oldR2s.add(newR2);
                    }
                }
            }

            if (R2OperationEnum.update.equals(op)) {

            	// Need to remove the double- loop process associated items
                List tobeDleteList = Lists.newArrayList();
                for (Object r2 : oldR2s) {
                    boolean tobeDlete = true;
                    for (Serializable r2EntityId : r2EntityIds) {
                        Object r2Entity = getEntityManager().find(r2EntityClass, r2EntityId);
                        if (FieldUtils.readDeclaredField(r2, r2EntityPropertyName, true).equals(r2Entity)) {
                            tobeDlete = false;
                            break;
                        }
                    }
                    if (tobeDlete) {
                        tobeDleteList.add(r2);
                    }
                }
                oldR2s.removeAll(tobeDleteList);
            }

            if (R2OperationEnum.delete.equals(op)) {

            	// Need to remove the double- loop process associated items
                List tobeDleteList = Lists.newArrayList();
                for (Object r2 : oldR2s) {
                    boolean tobeDlete = false;
                    for (Serializable r2EntityId : r2EntityIds) {
                        Object r2Entity = getEntityManager().find(r2EntityClass, r2EntityId);
                        if (FieldUtils.readDeclaredField(r2, r2EntityPropertyName, true).equals(r2Entity)) {
                            tobeDlete = true;
                            break;
                        }
                    }
                    if (tobeDlete) {
                        tobeDleteList.add(r2);
                    }
                }
                oldR2s.removeAll(tobeDleteList);
            }

        } catch (SecurityException e) {
            throw new ServiceException(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new ServiceException(e.getMessage(), e);
        } catch (InstantiationException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    /**
     * Association Object relationships helper methods for subclass operations call
     *
     * @param Id
     * Currently associated with the primary object primary key , such as User objects in the primary key
     * @param R2EntityIds
     * Associated with the target object primary key set , such as user roles Role collection of objects associated with the primary key
     * @param R2PropertyName
     * The name of the primary object associated with the collection of object properties , such as userR2Roles property name User defined objects
     * @param R2EntityPropertyName
     * The associated object attributes associated with the object name in the definition of R2 , as defined in the role UserR2Role property name
     */
    protected void updateRelatedR2s(T entity, Serializable[] r2EntityIds, String r2PropertyName, String r2EntityPropertyName) {
        try {
            List oldR2s = (List) MethodUtils.invokeExactMethod(entity, "get" + StringUtils.capitalize(r2PropertyName), null);
            if (oldR2s == null) {
                oldR2s = Lists.newArrayList();
                FieldUtils.writeDeclaredField(entity, r2PropertyName, oldR2s, true);
            }
            if ((r2EntityIds == null || r2EntityIds.length == 0)) {
                if (!CollectionUtils.isEmpty(oldR2s)) {
                    oldR2s.clear();
                }
            } else {
                Field r2field = FieldUtils.getField(getEntityClass(), r2PropertyName, true);
                Class r2Class = (Class) (((ParameterizedType) r2field.getGenericType()).getActualTypeArguments()[0]);
                Field entityField = null;
                Field[] fields = r2Class.getDeclaredFields();
                for (Field field : fields) {
                    if (field.getType().equals(getEntityClass())) {
                        entityField = field;
                        break;
                    }
                }

                Field r2EntityField = FieldUtils.getField(r2Class, r2EntityPropertyName, true);
                Class r2EntityClass = r2EntityField.getType();

             // Need to remove the double- loop process associated items
                if (CollectionUtils.isNotEmpty(oldR2s)) {
                    List tobeDleteList = Lists.newArrayList();
                    for (Object r2 : oldR2s) {
                        boolean tobeDlete = true;
                        for (Serializable r2EntityId : r2EntityIds) {
                            Object r2Entity = getEntityManager().find(r2EntityClass, r2EntityId);
                            if (FieldUtils.readDeclaredField(r2, r2EntityPropertyName, true).equals(r2Entity)) {
                                tobeDlete = false;
                                break;
                            }
                        }
                        if (tobeDlete) {
                            tobeDleteList.add(r2);
                        }
                    }
                    oldR2s.removeAll(tobeDleteList);
                }


             // Project need to add dual loop process associated
                for (Serializable r2EntityId : r2EntityIds) {
                    Object r2Entity = getEntityManager().find(r2EntityClass, r2EntityId);
                    boolean tobeAdd = true;
                    if (CollectionUtils.isNotEmpty(oldR2s)) {
                        for (Object r2 : oldR2s) {
                            if (FieldUtils.readDeclaredField(r2, r2EntityPropertyName, true).equals(r2Entity)) {
                                tobeAdd = false;
                                break;
                            }
                        }
                    }
                    if (tobeAdd) {
                        Object newR2 = r2Class.newInstance();
                        FieldUtils.writeDeclaredField(newR2, r2EntityField.getName(), r2Entity, true);
                        FieldUtils.writeDeclaredField(newR2, entityField.getName(), entity, true);
                        oldR2s.add(newR2);
                    }
                }
            }
        } catch (Exception e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Transactional(readOnly = true)
    public Object findEntity(Class entityClass, Serializable id) {
        return getEntityManager().find(entityClass, id);
    }

    public void detach(Object entity) {
        getEntityManager().detach(entity);
    }
}
