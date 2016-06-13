package lab.s2jh.core.util;

import java.lang.reflect.Field;
import java.util.Map;

import lab.s2jh.core.annotation.MetaData;

import com.google.common.collect.Maps;

public class EnumUtils {

    private static Map<Class<?>, Map<Enum<?>, String>> enumDatasContainer = Maps.newHashMap();

    /**
     * Construction of objects based on Enum class returns the corresponding key-value Map
     * @param enumClass
     * @return
     */
    public static Map<Enum<?>, String> getEnumDataMap(Class<? extends Enum> enumClass) {
        Map<Enum<?>, String> enumDataMap = enumDatasContainer.get(enumClass);
        if (enumDataMap != null) {
            return enumDataMap;
        }
        enumDataMap = Maps.newLinkedHashMap();
        enumDatasContainer.put(enumClass, enumDataMap);
        Field[] fields = enumClass.getFields();
        for (Field field : fields) {
            String name = field.getName();
            String label = name;
            MetaData entityComment = field.getAnnotation(MetaData.class);
            if (entityComment != null) {
                label = entityComment.value();
            }
            enumDataMap.put(Enum.valueOf(enumClass, name), label);
        }
        return enumDataMap;
    }

}
