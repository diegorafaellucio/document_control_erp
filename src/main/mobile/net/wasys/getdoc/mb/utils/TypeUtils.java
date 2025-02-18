package net.wasys.getdoc.mb.utils;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * TypeUtils
 * 1 de ago de 2016 09:19:49
 * @autor Everton Luiz Pascke
 */
public class TypeUtils {
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> T parse(Class<T> clazz, String value) {
		T parsed = null;
		if (StringUtils.isNotBlank(value)) {
			try {
				// String
				if (clazz.equals(String.class)) {
					parsed = (T) value;
				}
				else if (clazz.isEnum()) {
					parsed = (T) Enum.valueOf((Class) clazz, value);
				}
				// Long
				else if (clazz.equals(Long.class)) {
					parsed = (T) Long.valueOf(value);
				}
				// Integer
				else if (clazz.equals(Integer.class)) {
					parsed = (T) Integer.valueOf(value);
				}
				// Boolean
				else if (clazz.equals(Boolean.class)) {
					parsed = (T) Boolean.valueOf(value);
				}
				// Double
				else if (clazz.equals(Double.class)) {
					parsed = (T) NumberUtils.parseDouble(value);
				}
				else if (clazz.equals(BigDecimal.class)) {
					Double val = NumberUtils.parseDouble(value);
					if (val != null) {
						parsed = (T) BigDecimal.valueOf(val);
					}
				}
				// Date
				else if (clazz.equals(Date.class)) {
					parsed = (T) DateUtils.parse(value);
				}
			} catch (Exception e) {
				
			}
		}
		return parsed;
	}
}