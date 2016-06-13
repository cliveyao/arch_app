package lab.s2jh.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation method for identifying the current Controller menu data generated metadata
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface MenuData {

    /**
     * Menu Path
     */
    String[] value();

    /**
     * Comment Description : Used to describe the internal code instructions
     */
    String comments() default "";
}
