package lab.s2jh.core.dao.router;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation data source name on the methods , data sources dynamically switch
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface RoutingDataSource {
    /**
     * Data Sources ( collection of ) name
     */
    String value() default "slave";;
}
