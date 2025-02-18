package net.wasys.util.other;

import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.comparators.ComparableComparator;
import org.apache.commons.lang.StringUtils;

/**
 * @author Felipe Maschio
 * @created 19/05/2011
 */
@SuppressWarnings("unchecked")
public class SuperBeanComparator<T> implements Comparator<T> {

	private final Map<String, Boolean> comparators;
	private final Comparator<Object> comparator = ComparableComparator.getInstance();
	private boolean nullFirst = true;

	/**
	 * @param properties separadas por virgula
	 */
	public SuperBeanComparator(String properties) {

		String[] split = properties.split(",");
		comparators = new LinkedHashMap<String, Boolean>(split.length);
		for (String property : split) {

			boolean desc = property.contains(" desc");
			property = property.replace(" desc", "");
			property = property.replace(" asc", "");
			property = StringUtils.trim(property);

			comparators.put(property, desc);
		}
	}

	public int compare(T arg0, T arg1) {

		for (String property : comparators.keySet()) {

			Boolean desc = comparators.get(property);

			int compare;
			if(desc) {
				compare = compare2(arg1, arg0, property);
			} else {
				compare = compare2(arg0, arg1, property);
			}

			if(compare != 0) {
				return compare;
			}
		}

		return 0;
	}

	private int compare2(T arg0, T arg1, String property) {

		try {
			Object value1 = getProperty(arg0, property);
			Object value2 = getProperty(arg1, property);

			if(value1 == null && value2 == null) {
				return 0;
			}
			if(value1 == null) {
				return nullFirst ? -1 : 1;
			}
			if(value2 == null) {
				return nullFirst ? 1 : -1;
			}

			return comparator.compare(value1, value2);
		}
		catch ( IllegalAccessException iae ) {
			iae.printStackTrace();
			throw new RuntimeException("IllegalAccessException: " + iae.toString());
		}
		catch ( InvocationTargetException ite ) {
			ite.printStackTrace();
			throw new RuntimeException("InvocationTargetException: " + ite.toString());
		}
		catch ( NoSuchMethodException nsme ) {
			nsme.printStackTrace();
			throw new RuntimeException("NoSuchMethodException: " + nsme.toString());
		}
	}

	private Object getProperty(T arg0, String property) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

		String[] split = property.split("\\.");
		for (String string : split) {

			if(arg0 == null) {
				return null;
			}
			arg0 = (T) PropertyUtils.getProperty(arg0, string);
		}

		return arg0;
	}

	public void setNullFirst(boolean nullFirst) {
		this.nullFirst = nullFirst;
	}
}
