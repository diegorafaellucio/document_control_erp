package net.wasys.getdoc.mb.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

import org.apache.commons.lang3.StringUtils;

import net.wasys.getdoc.mb.Constant;

/**
 * 
 * NumberUtils
 * 1 de ago de 2016 09:17:29
 * @autor Everton Luiz Pascke
 */
public class NumberUtils {
	
	public static String format(Number value) {
		return format(value, Constant.DECIMAL);
	}
	
	public static String format(Number value, String pattern) {
		if (value != null) {
			try {
				NumberFormat formatter = new DecimalFormat(pattern, new DecimalFormatSymbols(Constant.LOCALE));
				return formatter.format(value);
			} catch (Exception e) {
				
			}
		}
		return null;
	}
	
	public static Double parseDouble(String value) {
		return parseDouble(value, (Double) null);
	}
	
	public static Double parseDouble(String value, String pattern) {
		return parseDouble(value, pattern, null);
	}
	
	public static Double parseDouble(String value, Double defaultValue) {
		return parseDouble(value, Constant.DECIMAL, defaultValue);
	}
	
	public static Double parseDouble(String value, String pattern, Double defaultValue) {
		if (StringUtils.isNotBlank(value)) {
			try {
				if (!value.contains(",")) {
					return new Double(value);
				}
				else {
					NumberFormat formatter = new DecimalFormat(pattern, new DecimalFormatSymbols(Constant.LOCALE));
					Number number = formatter.parse(value);
					if (number != null) {
						return number.doubleValue();
					}
				}
			} catch (Exception e) {
				
			}
		}
		return defaultValue;
	}
}