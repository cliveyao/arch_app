package lab.s2jh.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Metadata is used for annotation classes or attributes , 
 * metadata can be used to build or run -time code to generate dynamic content
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PACKAGE })
public @interface MetaData {

    /**
     * Brief explanatory notes : Usually correspond individual Label property displays
     */
    String value();

    /**
     * Tips : generally correspond to the form item 's instructions , in HTML format support
     */
    String tooltips() default "";

    /**
     * Comment Description : Used to describe the internal code instructions , generally not used for front-end UI display
     */
    String comments() default "";

    /**
     *Identity property appears in the version comparison list
     * @see PersistableController#getRevisionFields()
     */
    boolean comparable() default true;

    /**
     * Identifies whether the property can be edited in code generation entry
     */
    boolean editable() default true;

    /**
     * For the self-energizing type entity set initial AUTO_INCREMENT value
     * Generally used for business objects such as Order ID directly as the order number, 
     * serial number can hope to align directly to the self-energizing initialization value longer 
     * -digit number , such as 1 billion
     */
    long autoIncrementInitValue() default 0;
}
