package lab.s2jh.core.service;

import java.util.Collection;

import lab.s2jh.core.exception.ValidationException;
import lab.s2jh.support.service.DynamicConfigService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

public class Validation {

    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new ValidationException(message);
        }
    }

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new ValidationException(message);
        }
    }

    public static void notBlank(String str, String message) {
        if (StringUtils.isBlank(str)) {
            throw new ValidationException(message);
        }
    }

    public static void notEmpty(Collection<?> collection, String message) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new ValidationException(message);
        }
    }

    public static void notDemoMode() {
        if (DynamicConfigService.isDemoMode()) {
            throw new ValidationException("Sorry, this feature is disabled in the demo mode , refer to the documentation in local deployment run experience .");
        }
    }
}
