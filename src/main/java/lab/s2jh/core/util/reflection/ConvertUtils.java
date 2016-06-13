/**
 * Copyright (c) 2012
 */
package lab.s2jh.core.util.reflection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.lang3.StringUtils;

public class ConvertUtils {

	static {
		registerDateConverter();
	}

	/**
	 *Attribute extraction collection objects ( via getter function ) , combined into a List.
	 *
	 * @param Collection source collection .
	 * @param PropertyName attribute names to be extracted .
	 */
	@SuppressWarnings("unchecked")
	public static List convertElementPropertyToList(final Collection collection, final String propertyName) {
		List list = new ArrayList();

		try {
			for (Object obj : collection) {
				list.add(PropertyUtils.getProperty(obj, propertyName));
			}
		} catch (Exception e) {
			throw ReflectionUtils.convertReflectionExceptionToUnchecked(e);
		}

		return list;
	}

	/**
	 * Extract objects in a collection of properties ( through getter function ) , a combination of strings separated by a delimiter .
	 *
	 * @param Collection source collection .
	 * @param PropertyName attribute names to be extracted .
	 * @param Separator separator.
	 */
	@SuppressWarnings("unchecked")
	public static String convertElementPropertyToString(final Collection collection, final String propertyName,
			final String separator) {
		List list = convertElementPropertyToList(collection, propertyName);
		return StringUtils.join(list, separator);
	}

	/**
	 * Convert a string to the appropriate type.
	 *
	 * @param Value to be converted string.
	 * @param ToType conversion target type.
	 */
	public static Object convertStringToObject(String value, Class<?> toType) {
	    if(StringUtils.isBlank(value)){
	        return null;
	    }
		try {
			return org.apache.commons.beanutils.ConvertUtils.convert(value, toType);
		} catch (Exception e) {
			throw ReflectionUtils.convertReflectionExceptionToUnchecked(e);
		}
	}

	/**
	 * Custom Date Converter format : yyyy-MM-dd or yyyy-MM-dd HH: mm: ss
	 */
	private static void registerDateConverter() {
		DateConverter dc = new DateConverter();
		dc.setUseLocaleFormat(true);
		dc.setPatterns(new String[] { "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss" });
		org.apache.commons.beanutils.ConvertUtils.register(dc, Date.class);
	}
}
