package lab.s2jh.aud.aop;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Map;

import lab.s2jh.core.annotation.MetaData;
import lab.s2jh.core.audit.envers.ExtRevisionListener;
import lab.s2jh.support.service.DynamicConfigService;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.util.ClassUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.google.common.collect.Maps;

/**
 * Controller -based Spring AOP to intercept method calls processed
 * Extract relevant information is set to call
 * @see ExtRevisionListener Pass to Hibernate Envers component record
 */
public class ControllerInvokeAuditor {

    private final static Logger logger = LoggerFactory.getLogger(ControllerInvokeAuditor.class);

    private static Map<String, Map<String, String>> cachedMethodDatas = Maps.newHashMap();

    public Object process(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Signature signature = joinPoint.getSignature();
            MethodSignature methodSignature = (MethodSignature) signature;
            Method method = methodSignature.getMethod();
            String key = method.toString();

            if (logger.isDebugEnabled()) {
                String monthName = method.getName();
                String className = joinPoint.getThis().getClass().getName();
                if (className.indexOf("$$") > -1) { // If it is dynamically generated class CGLIB 
                    className = StringUtils.substringBefore(className, "$$");
                }
                logger.debug("AOP Aspect: {}, Point: {}.{}", ControllerInvokeAuditor.class, className, monthName);
            }

            Map<String, String> cachedMethodData = cachedMethodDatas.get(key);
            //If you already have a method to cache data , you deal directly with
            if (cachedMethodData != null && !DynamicConfigService.isDevMode()) {
                logger.debug("Controller method audit, cached data: {}", cachedMethodData);
                ExtRevisionListener.setKeyValue(cachedMethodData);
            } else {
                //If there is no cache data , the judgment processing
                RequestMapping methodRequestMapping = method.getAnnotation(RequestMapping.class);
                RequestMethod[] requestMethods = methodRequestMapping.method();
                for (RequestMethod requestMethod : requestMethods) {

                	// Only handles POST method
                    if (RequestMethod.POST.equals(requestMethod)) {


                    	// Constructor cached data objects , and calculate the associated attributes into the object cache
                        cachedMethodData = Maps.newHashMap();

                        Class<?> thisClazz = joinPoint.getThis().getClass();
                        String className = thisClazz.getName();
                        if (className.indexOf("$$") > -1) { // If it is dynamically generated class CGLIB  
                            className = StringUtils.substringBefore(className, "$$");
                        }
                        Class<?> controllerClazz = ClassUtils.forName(className);
                        String requestMappingUri = "";
                        RequestMapping classRequestMapping = controllerClazz.getAnnotation(RequestMapping.class);
                        if (classRequestMapping != null) {
                            requestMappingUri += StringUtils.join(classRequestMapping.value());
                        }
                        requestMappingUri += StringUtils.join(methodRequestMapping.value());
                        cachedMethodData.put(ExtRevisionListener.requestMappingUri, requestMappingUri);

                        cachedMethodData.put(ExtRevisionListener.controllerClassName, className);
                        MetaData clazzMetaData = controllerClazz.getAnnotation(MetaData.class);
                        if (clazzMetaData != null) {
                            cachedMethodData.put(ExtRevisionListener.controllerClassLabel, clazzMetaData.value());
                        }

                        Object genericClz = controllerClazz.getGenericSuperclass();
                        if (genericClz instanceof ParameterizedType) {
                            Class<?> entityClass = (Class<?>) ((ParameterizedType) genericClz).getActualTypeArguments()[0];
                            cachedMethodData.put(ExtRevisionListener.entityClassName, entityClass.getName());
                        }

                        cachedMethodData.put(ExtRevisionListener.controllerMethodName, method.getName());
                        cachedMethodData.put(ExtRevisionListener.controllerMethodType, requestMethod.name());

                        MetaData methodMetaData = method.getAnnotation(MetaData.class);
                        if (methodMetaData != null) {
                            cachedMethodData.put(ExtRevisionListener.controllerMethodLabel, methodMetaData.value());
                        }

                        cachedMethodDatas.put(key, cachedMethodData);

                        logger.debug("Controller method audit, init data: {}", cachedMethodData);
                        ExtRevisionListener.setKeyValue(cachedMethodData);
                        break;
                    }
                }
            }
        } catch (Exception e) {
        	// Handle exceptions capture audit information , to avoid affecting normal business processes
            logger.error(e.getMessage(), e);
        }
        return joinPoint.proceed();
    }
}
