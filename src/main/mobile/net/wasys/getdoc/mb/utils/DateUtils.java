package net.wasys.getdoc.mb.utils;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import net.wasys.getdoc.mb.Constant;

/**
 * 
 * DateUtils.java
 * @pascke
 */
@Deprecated
public class DateUtils {

	public enum DateType {
		
		ISO_8601 ("yyyy-MM-dd'T'HH:mm:ssz", "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}[A-Z]{3}"),
		DATE_TIME ("yyyy-MM-dd HH:mm:ss", "\\d{4}-\\d{2}-\\d{2}"),
		DATE ("yyyy-MM-dd", "\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}"),
		
		DATE_TIME_BR ("dd/MM/yyyy HH:mm:ss", "\\d{2}/\\d{2}/\\d{4}\\s\\d{2}:\\d{2}:\\d{2}"),
		DATE_BR ("dd/MM/yyyy", "\\d{2}/\\d{2}/\\d{4}");
		
		private String pattern;
		private String expression;

		private DateType(String pattern, String expression) {
			this.pattern = pattern;
			this.expression = expression;
		}
		
		public String getPattern() {
			return pattern;
		}
		
		public String getExpression() {
			return expression;
		}
		
		public static String[] getPatterns() {
			DateType[] types = values();
			String[] patterns = new String[types.length];
			for (int i = 0; i < types.length; i++) {
				patterns[i] = types[i].pattern;
			}
			return patterns;
		}
		
		public static String[] getExpressions() {
			DateType[] types = values();
			String[] expressions = new String[types.length];
			for (int i = 0; i < types.length; i++) {
				expressions[i] = types[i].expression;
			}
			return expressions;
		}
	}
	
	public static final String[] patterns = DateType.getPatterns();
	public static final String[] expressions = DateType.getExpressions();
	
	public static final DateType defaultType = DateType.ISO_8601;
	public static final String defaultPattern = defaultType.pattern;
	
	private static DateFormat getDateFormat(String pattern) {
		DateFormat formatter = new SimpleDateFormat(pattern, Constant.LOCALE);
		return formatter;
	}
	
	public static boolean isDate(String value) {
		if (StringUtils.isNotBlank(value)) {
			for (int i = 0; i < expressions.length; i++) {
				if (value.matches(expressions[i])) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static String format(Date value) {
		return format(value, defaultType.getPattern());
	}
	
	public static String format(Date value, String pattern) {
		if (value != null) {
			try {
				DateFormat formatter = getDateFormat(pattern);
				return formatter.format(value);
			} catch (Exception e) {
				
			}
		}
		return null;
	}
	
	public static Date parse(String value) {
		return parse(value, patterns);
	}
	
	public static Date parse(String value, String... patterns) {
		if (StringUtils.isNotBlank(value)) {
			DateFormat formatter;
			for (String pattern : patterns) {
				formatter = getDateFormat(pattern);
				try {
					return formatter.parse(value);
				} catch (ParseException e) {
					// try next ...
				}
			}
		}
		return null;
	}
	
	public static boolean isBetween(Date minDay, Date maxDay, Date day) {
		return day.compareTo(minDay) >= 0 && day.compareTo(maxDay) <= 0;
	}
	
	public static Date getActualMinDay() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
		truncate(cal);
		return cal.getTime();
	}
	
	public static Date getActualMaxDay() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		truncate(cal);
		return cal.getTime();
	}
	
	public static Date getActualMinDay(Date day) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(day);
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
		truncate(cal);
		return cal.getTime();
	}
	
	public static Date getActualMaxDay(Date day) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(day);
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		truncate(cal);
		return cal.getTime();
	}

	public static int getYear(Date day) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(day);
		int ano = cal.get(Calendar.YEAR);
		return ano;
	}

	public static int getDay(Date day) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(day);
		int ano = cal.get(Calendar.DAY_OF_MONTH);
		return ano;
	}

	public static int getMonth(Date day) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(day);
		int ano = cal.get(Calendar.MONTH) + 1;
		return ano;
	}

	public static void truncate(Calendar calendar) {
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
	}
	
	public static Date truncate(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		truncate(calendar);
		return calendar.getTime();
	}
	
	public static Date getLastTimeOfDay() {
		return getLastTimeOfDay(new Date());
	}
	
	public static Date getFirstTimeOfDay() {
		return getFirstTimeOfDay(new Date());
	}
	
	public static Date getFirstTimeOfDay(Date date) {
		return truncate(date);
	}
	
	public static Date getLastTimeOfDay(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 999);
		return calendar.getTime();
	}
	
	public static Integer getDaysBetween(Date d1, Date d2){
		Date d1Trunc = truncate(d1);
		Date d2Trunc = truncate(d2);
		return new BigDecimal(d1Trunc.getTime() - d2Trunc.getTime()).divide(new BigDecimal(1000 * 60 * 60 * 24), BigDecimal.ROUND_HALF_UP).intValue(); 
	}
	
	public static Date addDays(Date date, int days) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, days);
		return calendar.getTime();
	}

	public static Date addYear(Date date, int anos) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.YEAR, anos);
		return calendar.getTime();
	}

	public static Date addHourOfDay(Date date, int hour) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.HOUR_OF_DAY, hour);
		return calendar.getTime();
	}

	public static Date addMonths(Date date, int i) {
		return org.apache.commons.lang.time.DateUtils.addMonths(date, i);
	}
}