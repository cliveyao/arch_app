package lab.s2jh.core.util;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.persistence.Column;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.constraints.Size;

import lab.s2jh.core.audit.DefaultAuditable;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.shiro.util.CollectionUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Simulation entity object instance constructor helper
 */
public class MockEntityUtils {

    private final static Logger logger = LoggerFactory.getLogger(MockEntityUtils.class);

    private final static RandomDataGenerator randomDataGenerator = new RandomDataGenerator();

    private final static Random random = new Random();

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static <X> X buildMockObject(Class<X> clazz) {
        X x = null;
        try {
            x = clazz.newInstance();
            for (Method method : clazz.getMethods()) {
                String mn = method.getName();
                if (mn.startsWith("set")) {
                    Class[] parameters = method.getParameterTypes();
                    if (parameters.length == 1) {
                        Method getMethod = MethodUtils.getAccessibleMethod(clazz, "get" + mn.substring(3), null);
                        if (getMethod != null) {
                            if (getMethod.getName().equals("getId")) {
                                continue;
                            }
                            //有默认值，则直接返回
                            if (MethodUtils.invokeMethod(x, getMethod.getName(), null, null) != null) {
                                continue;
                            }
                            Object value = null;
                            Class parameter = parameters[0];
                            if (parameter.isAssignableFrom(String.class)) {
                                Column column = getMethod.getAnnotation(Column.class);
                                int columnLength = 10;
                                if (column != null && column.length() < columnLength) {
                                    columnLength = column.length();
                                }
                                Size size = getMethod.getAnnotation(Size.class);
                                if (size != null && size.max() < columnLength) {
                                    columnLength = size.max();
                                }
                                value = RandomStringUtils.randomAlphabetic(columnLength);
                            } else if (parameter.isAssignableFrom(Date.class)) {
                                value = DateUtils.currentDate();
                            } else if (parameter.isAssignableFrom(BigDecimal.class)) {
                                value = new BigDecimal(10 + new Double(new Random().nextDouble() * 1000).intValue());
                            } else if (parameter.isAssignableFrom(Integer.class)) {
                                value = 1 + new Double(new Random().nextDouble() * 100).intValue();
                            } else if (parameter.isAssignableFrom(Boolean.class)) {
                                value = new Random().nextBoolean();
                            } else if (parameter.isEnum()) {
                                Method m = parameter.getDeclaredMethod("values", null);
                                Object[] result = (Object[]) m.invoke(parameter.getEnumConstants()[0], null);
                                value = result[new Random().nextInt(result.length)];
                            }
                            if (value != null) {
                                MethodUtils.invokeMethod(x, mn, value);
                                logger.trace("{}={}", method.getName(), value);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return x;
    }

    /**
     *Take a random object returns
     */
    public static <X> X randomCandidates(X... candidates) {
        List<X> list = Lists.newArrayList(candidates);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.get(randomDataGenerator.nextInt(0, list.size() - 1));
    }

    /**
     * Take a random object returns
     */
    public static <X> X randomCandidates(Iterable<X> candidates) {
        List<X> list = Lists.newArrayList(candidates);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        if (list.size() == 1) {
            return list.get(0);
        }
        return list.get(randomDataGenerator.nextInt(0, list.size() - 1));
    }

    /**
     * Returns a random integer range segment
     * @param Lower minimum
     * @param Upper maximum
     * @return
     */
    public static int randomInt(int lower, int upper) {
        return randomDataGenerator.nextInt(lower, upper);
    }

    /**
     * Returns a random integer range segment
     * @param Lower minimum
     * @param Upper maximum
     * @return
     */
    public static long randomLong(int lower, int upper) {
        return randomDataGenerator.nextLong(lower, upper);
    }

    /**
     *Returns a random decimal 0-1 range segment
     * @return
     */
    public static double randomDouble() {
        return random.nextDouble();
    }

    /**
     *Returns a random Boolean value range segment
     * @return
     */
    public static boolean randomBoolean() {
        return randomDataGenerator.nextInt(0, 100) > 50 ? true : false;
    }

    /**
     *Returns a random date range segment
     * Number of days from the current date @param daysBeforeNow
     * A few days later from the current date @param daysAfterNow
     * @return
     */
    public static Date randomDate(int daysBeforeNow, int daysAfterNow) {
        DateTime dt = new DateTime();
        dt = dt.plusMinutes(randomInt(-30, 30));
        dt = dt.plusHours(randomInt(-12, 12));
        dt = dt.minusDays(daysBeforeNow);
        dt = dt.plusDays(randomInt(0, daysBeforeNow + daysAfterNow));
        return dt.toDate();
    }

    /**
     * 
Data Persistence
     * @param Entity to be persistent instances
     * @return
     */
    private static void persistNew(EntityManager entityManager, Object entity) {
        entityManager.persist(entity);

     // Special handling SaveUpdateAuditListener of CreatedDate " tampering " as the current temporary system time
        if (entity instanceof DefaultAuditable) {
            ((DefaultAuditable) entity).setCreatedDate(DateUtils.currentDate());
        }
    }

    /**
     * 
Data Persistence
     * @param Entity to be persistent instances
     * @param ExistCheckFields variable parameters , provided for a list of names to check whether data already exists in the field
     * @return
     */
    public static boolean persistSilently(EntityManager entityManager, Object entity, String... existCheckFields) {
        try {
            if (existCheckFields != null && existCheckFields.length > 0) {
                CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
                CriteriaQuery<?> criteriaQuery = criteriaBuilder.createQuery(entity.getClass());
                Root<?> root = criteriaQuery.from(entity.getClass());
                List<Predicate> predicatesList = new ArrayList<Predicate>();
                Map<String, Object> predicates = Maps.newHashMap();
                for (String field : existCheckFields) {
                    Object value = FieldUtils.readField(entity, field, true);
                    predicates.put(field, value);
                    if (value == null) {
                        predicatesList.add(criteriaBuilder.isNull(root.get(field)));
                    } else {
                        predicatesList.add(criteriaBuilder.equal(root.get(field), value));
                    }
                }
                criteriaQuery.where(predicatesList.toArray(new Predicate[predicatesList.size()]));
                List<?> list = entityManager.createQuery(criteriaQuery).getResultList();
                if (list != null && list.size() > 0) {
                    logger.debug("Skipped exist data: {} -> {}", entity.getClass(), predicates);
                    return false;
                }
            }
            entityManager.persist(entity);
            entityManager.flush();
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    public static class TestVO {
        private String str;
        private Date dt;

        public String getStr() {
            return str;
        }

        public void setStr(String str) {
            this.str = str;
        }

        public Date getDt() {
            return dt;
        }

        public void setDt(Date dt) {
            this.dt = dt;
        }
    }

    public static void main(String[] args) {
        TestVO testVO = MockEntityUtils.buildMockObject(TestVO.class);
        System.out.println("Mock Entity: " + ReflectionToStringBuilder.toString(testVO, ToStringStyle.MULTI_LINE_STYLE));
    }
}
