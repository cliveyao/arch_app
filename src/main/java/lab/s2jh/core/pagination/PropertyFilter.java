/**
 * Copyright (c) 2012
 */
package lab.s2jh.core.pagination;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import lab.s2jh.core.util.DateUtils;
import lab.s2jh.core.util.reflection.ConvertUtils;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.util.Assert;

/**
 * Specific ORM implementation-independent attribute filters package type , 
 * the main record page simple search filter criteria. Page form for the incoming string of conditions , 
 * then the conversion process to identify the level of SQL conditions DAO
 * Page form elements Example:
 * <ul>
 * <li>search['CN_a_OR_b']</li>
 * <li>search['EQ_id']</li>
 * <li>search['CN_user.name']</li>
 * </ul>
 * <p>
 *FORM Form parameters passed rules : <br/>
 * 1 The first part : to "search []" as a query parameter identification <br/>
 * 2 , Part II: The type of query , @ see #MatchType <br/>
 * 3 , Part III : id_OR_email, category, state, user.userprofile property name , generally corresponds to Hibernate
 * Entity attribute correspondence , can _OR_ Separate multiple attributes OR query
 * </ P>
 * <P>
 * The string assembly is mainly used for JSP pages form form element name attribute value , if Java code level, additional filters , the direct use of general constructor :
 * PropertyFilter(final MatchType matchType, final String propertyName, final
 * Object matchValue)
 * </p>
 */
public class PropertyFilter {

    private final static Logger logger = LoggerFactory.getLogger(PropertyFilter.class);

    private static final int DEFAULT_PAGE_ROWS = 20;

    /** Between a plurality of property separator OR relationship  */
    public static final String OR_SEPARATOR = "_OR_";

    /** Property matches the type of comparison */
    public enum MatchType {
        /** "name": "bk", "description": "is blank", "operator":"IS NULL OR ==''" */
        BK,

        /** "name": "nb", "description": "is not blank", "operator":"IS NOT NULL AND !=''" */
        NB,

        /** "name": "nu", "description": "is null", "operator":"IS NULL" */
        NU,

        /** "name": "nn", "description": "is not null", "operator":"IS NOT NULL" */
        NN,

        /** "name": "in", "description": "in", "operator":"IN" */
        IN,

        /** "name": "ni", "description": "not in", "operator":"NOT IN" */
        NI,

        /** "name": "ne", "description": "not equal", "operator":"<>" */
        NE,

        /** "name": "eq", "description": "equal", "operator":"=" */
        EQ,

        /** "name": "cn", "description": "contains", "operator":"LIKE %abc%" */
        CN,

        /**
         * "name": "nc", "description": "does not contain",
         * "operator":"NOT LIKE %abc%"
         */
        NC,

        /** "name": "bw", "description": "begins with", "operator":"LIKE abc%" */
        BW,

        /**
         * "name": "bn", "description": "does not begin with",
         * "operator":"NOT LIKE abc%"
         */
        BN,

        /** "name": "ew", "description": "ends with", "operator":"LIKE %abc" */
        EW,

        /**
         * "name": "en", "description": "does not end with",
         * "operator":"NOT LIKE %abc"
         */
        EN,

        /** "name": "bt", "description": "between", "operator":"BETWEEN 1 AND 2" */
        BT,

        /** "name": "lt", "description": "less", "operator":"小于" */
        LT,

        /** "name": "gt", "description": "greater", "operator":"大于" */
        GT,

        /** "name": "le", "description": "less or equal","operator":"<=" */
        LE,

        /** "name": "ge", "description": "greater or equal", "operator":">=" */
        GE,

        /** @see javax.persistence.criteria.Fetch */
        FETCH,

        /** Property Less Equal: <= */
        PLE,

        /** Property Less Than: < */
        PLT,

        ACLPREFIXS;
    }

    /** Match Type*/
    private MatchType matchType = null;

    /** Matching value */
    private Object matchValue = null;

    /**
     *Match the property type
     * Restrictions Note : If more than one property then take the first one, 
     * so make sure _OR_ define multiple attributes of the same type
     */
    private Class propertyClass = null;

    /** Array of property names , usually a single property , if there was more than one _OR_ */
    private String[] propertyNames = null;
    /**
     * Collection type sub-queries, such as queries that contain a list of all orders for goods , 
     * such as order on top of the List object is a collection of products , you may like this :
     *  search [ 'EQ_products.code']
     * Limit Description : Frame collection object supports only the current master object directly 
     * defined collection queries , do not support the re- nested
     */
    private Class subQueryCollectionPropetyType;

    public PropertyFilter() {
    }

    /**
     * @param filterName
     * A property string containing the type of comparison to be compared , and the property value type attribute list .
     * @param values
     *            The value to be compared 
     */
    public PropertyFilter(Class<?> entityClass, String filterName, String... values) {

        String matchTypeCode = StringUtils.substringBefore(filterName, "_");
        if (matchTypeCode.indexOf("@") > -1) {
            String[] matchTypeCodes = matchTypeCode.split("@");
            matchTypeCode = matchTypeCodes[0];

            String propertyType = matchTypeCodes[1];
            if (Date.class.getName().equalsIgnoreCase(propertyType)) {
                propertyClass = Date.class;
            } else if (Number.class.getName().equalsIgnoreCase(propertyType)) {
                propertyClass = Number.class;
            } else {
                propertyClass = String.class;
            }
        }

        try {
            matchType = Enum.valueOf(MatchType.class, matchTypeCode);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("filter name" + filterName + "Not by the rules to write , can not get the type attribute comparison .", e);
        }

        String propertyNameStr = StringUtils.substringAfter(filterName, "_");
        Assert.isTrue(StringUtils.isNotBlank(propertyNameStr), "filter name" + filterName + " Not by the rules to write , can not get the property name.");
        propertyNames = StringUtils.splitByWholeSeparator(propertyNameStr, PropertyFilter.OR_SEPARATOR);

     // Calculate attribute corresponds Class Type
        if (propertyNameStr.indexOf("count(") > -1) {
            propertyClass = Integer.class;
        } else if (propertyNameStr.indexOf("(") > -1) {
            propertyClass = BigDecimal.class;
        } else {
            String firstPropertyName = propertyNames[0];
            if (firstPropertyName.indexOf("@") > -1) {
                String propertyType = firstPropertyName.split("@")[1];
                if (Date.class.getSimpleName().equalsIgnoreCase(propertyType)) {
                    propertyClass = Date.class;
                } else if (Number.class.getSimpleName().equalsIgnoreCase(propertyType)) {
                    propertyClass = Number.class;
                } else if (Boolean.class.getSimpleName().equalsIgnoreCase(propertyType)) {
                    propertyClass = Boolean.class;
                } else if (String.class.getSimpleName().equalsIgnoreCase(propertyType)) {
                    propertyClass = String.class;
                }
            } else {
                Method method = null;
                String[] namesSplits = StringUtils.split(firstPropertyName, ".");
                if (namesSplits.length == 1) {
                    method = MethodUtils.getAccessibleMethod(entityClass, "get" + StringUtils.capitalize(propertyNames[0]));
                } else {
                    Class<?> retClass = entityClass;
                    for (String nameSplit : namesSplits) {
                        method = MethodUtils.getAccessibleMethod(retClass, "get" + StringUtils.capitalize(nameSplit));
                        retClass = method.getReturnType();
                        if (Collection.class.isAssignableFrom(retClass)) {
                            Type genericReturnType = method.getGenericReturnType();
                            if (genericReturnType instanceof ParameterizedType) {
                                retClass = (Class<?>) ((ParameterizedType) genericReturnType).getActualTypeArguments()[0];
                                subQueryCollectionPropetyType = retClass;
                            }
                        }
                    }
                }
                if (method == null) {
                    propertyClass = String.class;
                } else {
                    propertyClass = method.getReturnType();
                }
            }
        }

        if (values.length == 1) {
            if (matchType.equals(MatchType.IN) || matchType.equals(MatchType.NI)) {
                String value = values[0];
                values = value.split(",");
            } else if (propertyClass.equals(Date.class) || propertyClass.equals(DateTime.class)) {
                String value = values[0].trim();
                value = value.replace("～", "~");
                if (value.indexOf(" ") > -1) {
                    values = StringUtils.split(value, "~");
                    if (matchType.equals(MatchType.BT)) {
                        values[0] = values[0].trim();
                        values[1] = values[1].trim();


                     // Date special treatment : Usually the end time for the date range queries , such as queries before 2012-01-01, 
                        //and 2010-01-01 generally need to show the same day and the previous data ,
                     // The database generally there are minutes and seconds , and therefore require special treatment to the 
                        //current date + 1 day , converted to < 2012-01-02 query
                        DateTime dateTo = new DateTime(DateUtils.parseDate(values[1].trim()));
                        if (dateTo.getHourOfDay() == 0 && dateTo.getMinuteOfHour() == 0 && dateTo.getSecondOfMinute() == 0) {
                            values[1] = DateUtils.formatDate(dateTo.plusDays(1).toDate());
                        }
                    } else {
                        values = new String[] { values[0].trim() };
                    }
                }
            }
        }

        if (values.length == 1) {
            this.matchValue = parseMatchValueByClassType(propertyClass, values[0]);
        } else {
            Object[] matchValues = new Object[values.length];
            for (int i = 0; i < values.length; i++) {
                matchValues[i] = parseMatchValueByClassType(propertyClass, values[i]);
            }
            this.matchValue = matchValues;
        }
    }

    private Object parseMatchValueByClassType(Class propertyClass, String value) {
        if ("NULL".equalsIgnoreCase(value)) {
            return value;
        }
        if (Enum.class.isAssignableFrom(propertyClass)) {
            return Enum.valueOf(propertyClass, value);
        } else if (propertyClass.equals(Boolean.class) || matchType.equals(MatchType.NN) || matchType.equals(MatchType.NU)) {
            return new Boolean(BooleanUtils.toBoolean(value));
        } else if (propertyClass.equals(Date.class)) {
            return DateUtils.parseMultiFormatDate((String) value);
        } else if (propertyClass.equals(Number.class)) {
            if (value.indexOf(".") > -1) {
                return new Double(value);
            } else {
                return new Long(value);
            }
        } else {
            return ConvertUtils.convertStringToObject(value, propertyClass);
        }
    }

    /**
     * Java programs directly to build the filter layer objects , 
     * such as filters.add (new PropertyFilter (MatchType.EQ, "code",
     * code));
     * 
     * @param matchType
     * @param propertyName
     * @param matchValue
     */
    public PropertyFilter(final MatchType matchType, final String propertyName, final Object matchValue) {
        this.matchType = matchType;
        this.propertyNames = StringUtils.splitByWholeSeparator(propertyName, PropertyFilter.OR_SEPARATOR);
        this.matchValue = matchValue;
    }

    /**
     * Java programs directly to build the filter layer objects , such as filters.add (new PropertyFilter (MatchType.LIKE, new
     * String[]{"code","name"}, value));
     * 
     * @param matchType
     * @param propertyName
     * @param matchValue
     */
    public PropertyFilter(final MatchType matchType, final String[] propertyNames, final Object matchValue) {
        this.matchType = matchType;
        this.propertyNames = propertyNames;
        this.matchValue = matchValue;
    }

    /**
     * Create a list from the HttpRequest in PropertyFilter
     * PropertyFilter prefix naming rules for the Filter property type attribute type _ _ Compare the attribute name .
     */
    public static List<PropertyFilter> buildFiltersFromHttpRequest(Class<?> entityClass, ServletRequest request) {

        List<PropertyFilter> filterList = new ArrayList<PropertyFilter>();


     // Get the parameter containing the prefix attribute name from the request , the configuration parameters Map prefix removed after the name .
        Map<String, String[]> filterParamMap = getParametersStartingWith(request, "search['", "']");


     // Analysis parameters Map, construction PropertyFilter list
        for (Map.Entry<String, String[]> entry : filterParamMap.entrySet()) {
            String filterName = entry.getKey();
            String[] values = entry.getValue();
            if (values == null || values.length == 0) {
                continue;
            }

            if (values.length == 1) {
                String value = values[0];

             // If the value is empty, then ignore this filter.
                if (StringUtils.isNotBlank(value)) {
                    PropertyFilter filter = new PropertyFilter(entityClass, filterName, value);
                    filterList.add(filter);
                }
            } else {
                String[] valuesArr = values;

             // If the value is empty, then ignore this filter.
                if (valuesArr.length > 0) {
                    Set<String> valueSet = new HashSet<String>();
                    for (String value : valuesArr) {
                        if (StringUtils.isNotBlank(value)) {
                            valueSet.add(value);
                        }
                    }
                    if (valueSet.size() > 0) {
                        String[] realValues = new String[valueSet.size()];
                        int cnt = 0;
                        for (String v : valueSet) {
                            realValues[cnt++] = v;
                        }
                        PropertyFilter filter = new PropertyFilter(entityClass, filterName, realValues);
                        filterList.add(filter);
                    }

                }
            }

        }
        return filterList;
    }

    /**
     * Extraction assembly paging and sorting objects , the list of parameters from the request object:
     * Rows per record number , if the value is empty then take the rows parameter value ; if the value is negative 
     * or null for no paging
     * Page current page number , starting at 1 , the default is 1
     * Start start record sequence number , starting at 1 , the need for precise control of some scenes 
     * from start to start + size ; the optional parameters , the value is not provided equal page * size
     * Sidx sort property name
     * Sord collation , asc = ascending , desc = descending , default asc
     * @param Request HttpServletRequest objects
     * @param DefaultRows default if the request is not provided in rows parameter records
     * @return 
     */
    public static Pageable buildPageableFromHttpRequest(HttpServletRequest request, int defaultRows) {
        return buildPageableFromHttpRequest(request, null, defaultRows);
    }

    /**
     * Extraction assembly paging and sorting objects , the list of parameters from the request object:
     * Rows number of records per page , default 20
     * Page current page number , starting at 1 , the default is 1
     * Start start record sequence number , starting at 1 , the need for precise control of some scenes from start to start + size ; the optional parameters , the value is not provided equal page * size
     * Sidx sort property name
     * Sord collation , asc = ascending , desc = descending , default asc
     * @param Request HttpServletRequest objects
     * @return 
     */
    public static Pageable buildPageableFromHttpRequest(HttpServletRequest request) {
        return buildPageableFromHttpRequest(request, null, DEFAULT_PAGE_ROWS);
    }

    /**
     * Extraction assembly paging and sorting objects , the list of parameters from the request object:
     * Rows per record number , if the value is empty then take the rows parameter value ; if the value 
     * is negative or null for no paging
     * Page current page number , starting at 1 , the default is 1
     * Start start record sequence number , starting at 1 , the need for precise control of some scenes 
     * from start to start + size ; the optional parameters , the value is not provided equal page * size
     * Sidx sort property name
     * Sord collation , asc = ascending , desc = descending , default asc
     * @param Request
     * @param Sort if its argument is null, constructed from the request , or directly take input sort parameters
     * @param DefaultRows default if the request is not provided in rows parameter records
     * @return
     */
    public static Pageable buildPageableFromHttpRequest(HttpServletRequest request, Sort sort, int defaultRows) {
        int rows = defaultRows;
        String paramRows = request.getParameter("rows");
        if (StringUtils.isNotBlank(paramRows)) {
            int pr = Integer.valueOf(paramRows);
            if (pr <= 0) {
                return null;
            } else {
                rows = pr;
            }
        }
        int offset = -1;
        int page = 1;
        String strStart = request.getParameter("start");
        if (StringUtils.isNotBlank(strStart)) {
            offset = Integer.valueOf(strStart) - 1;
            page = (offset + 1) / Integer.valueOf(rows);
            if (page <= 0) {
                page = 1;
            }
        } else {
            String strPage = request.getParameter("page");
            if (StringUtils.isNotBlank(strPage)) {
                page = Integer.valueOf(strPage);
            }
        }

        if (sort == null) {
            sort = buildSortFromHttpRequest(request);
        }
        return new ExtPageRequest(offset, page - 1, Integer.valueOf(rows), sort);
    }

    /**
     * Extraction assembly paging and sorting objects , the list of parameters from the request object:
     * Rows number of records per page , default 20
     * Page current page number , starting at 1 , the default is 1
     * Start start record sequence number , starting at 1 , the need for precise control of some scenes from start to start + size ; the optional parameters , the value is not provided equal page * size
     * Sidx sort property name
     * Sord collation , asc = ascending , desc = descending , default asc
     * @param Request
     * @param Sort if its argument is null, constructed from the request , or directly take input sort parameters
     * @param DefaultRows default if the request is not provided in rows parameter records
     * @return
     */
    public static Pageable buildPageableFromHttpRequest(HttpServletRequest request, Sort sort) {
        return buildPageableFromHttpRequest(request, sort, DEFAULT_PAGE_ROWS);
    }

    /**
     * Extraction assembly sorting objects from the object request , the parameter list :
     * Sidx sort property name
     * Sord collation , asc = ascending , desc = descending , default asc
     * @param request
     * @return
     */
    public static Sort buildSortFromHttpRequest(HttpServletRequest request) {
        String sidx = StringUtils.isBlank(request.getParameter("sidx")) ? "id" : request.getParameter("sidx");
        Direction sord = "desc".equalsIgnoreCase(request.getParameter("sord")) ? Direction.DESC : Direction.ASC;
        Sort sort = null;

        //In accordance with the comma split support multi- attribute sorting
        for (String sidxItem : sidx.split(",")) {
            if (StringUtils.isNotBlank(sidxItem)) {

            	// Then empty Geqie points get sort property and sort directio
                String[] sidxItemWithOrder = sidxItem.trim().split(" ");
                String sortname = sidxItemWithOrder[0];

	             // If the query attribute contains _OR_ then take the first sort property
	             // Writing the OR more attention to the sort attribute query attributes written at the top
                if (sortname.indexOf(OR_SEPARATOR) > -1) {
                    sortname = StringUtils.substringBefore(sortname, OR_SEPARATOR);
                }

             // Remove type-identifying information
                sortname = sortname.split("@")[0];

             // If a single attribute is not the sort to follow directions , then take the Grid component parameter defines the incoming sord
                if (sidxItemWithOrder.length == 1) {
                    if (sort == null) {

                    	// Initialize the sort object
                        sort = new Sort(sord, sortname);
                    } else {
                    	//and a plurality of additional sorting 
                        sort = sort.and(new Sort(sord, sortname));
                    }
                } else {
                	// Sort attributes space behind to follow the direction of the sort defined
                    String sortorder = sidxItemWithOrder[1];
                    if (sort == null) {
                        sort = new Sort("desc".equalsIgnoreCase(sortorder) ? Direction.DESC : Direction.ASC, sortname);
                    } else {
                        sort = sort.and(new Sort("desc".equalsIgnoreCase(sortorder) ? Direction.DESC : Direction.ASC, sortname));
                    }
                }
            }
        }
        return sort;
    }

    /**
     * Get comparative way
     */
    public MatchType getMatchType() {
        return matchType;
    }

    /**
     * Get the comparison value .
     */
    public Object getMatchValue() {
        return matchValue;
    }

    /**
     * Get a property list of names.
     */
    public String[] getConvertedPropertyNames() {
        String[] convertedPropertyNames = new String[propertyNames.length];
        for (int i = 0; i < propertyNames.length; i++) {
            convertedPropertyNames[i] = propertyNames[i].split("@")[0];
        }
        return convertedPropertyNames;
    }

    /**
     * Being the only comparison attribute name.
     */
    public String getConvertedPropertyName() {
        Assert.isTrue(propertyNames.length == 1, "There are not only one property in this filter.");
        String propertyName = propertyNames[0];
     // Remove the type of identifying information behind @
        return propertyName.split("@")[0];
    }

    /**
     *Compares multiple attributes.
     */
    public boolean hasMultiProperties() {
        return (propertyNames.length > 1);
    }

    /**
     * Constructs a default filter set .
     */
    public static List<PropertyFilter> buildDefaultFilterList() {
        return new ArrayList<PropertyFilter>();
    }

    public Class getPropertyClass() {
        return propertyClass;
    }

    public Class getSubQueryCollectionPropetyType() {
        return subQueryCollectionPropetyType;
    }

    private static Map<String, String[]> getParametersStartingWith(ServletRequest request, String prefix, String suffix) {
        Assert.notNull(request, "Request must not be null");
        @SuppressWarnings("rawtypes")
        Enumeration paramNames = request.getParameterNames();
        Map<String, String[]> params = new TreeMap<String, String[]>();
        if (prefix == null) {
            prefix = "";
        }
        if (suffix == null) {
            suffix = "";
        }
        while (paramNames != null && paramNames.hasMoreElements()) {
            String paramName = (String) paramNames.nextElement();
            if (("".equals(prefix) || paramName.startsWith(prefix)) && ("".equals(suffix) || paramName.endsWith(suffix))) {
                String unprefixed = paramName.substring(prefix.length(), paramName.length() - suffix.length());
                String[] values = request.getParameterValues(paramName);
                if (values == null || values.length == 0) {
                    // Do nothing, no values found at all.
                } else if (values.length > 1) {
                    params.put(unprefixed, values);
                } else {
                    params.put(unprefixed, new String[] { values[0] });
                }
            }
        }
        return params;
    }
}
