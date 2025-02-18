package net.wasys.getdoc.mb;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import net.wasys.getdoc.mb.utils.TypeUtils;

public class Copy {
	
	private Map<Class<?>, String[]> processors;
	
	private Copy(Builder builder) {
		processors = builder.processors;
	}
	
	public <T> T extract(Class<T> targetClass, Object origin) {
		try {
			Class<?> originClass = origin.getClass();
			String[] originFieldNames = getOriginFieldNames(originClass);
			if (ArrayUtils.isNotEmpty(originFieldNames)) {
				List<String> targetFieldNames = getTargetFieldNames(targetClass);
				if (CollectionUtils.isNotEmpty(targetFieldNames)) {
					T target = null;
					for (String fieldName : originFieldNames) {
						if (targetFieldNames.contains(fieldName)) {
							Object value = getValue(origin, fieldName);
							if (value != null) {
								if (target == null) {
									target = targetClass.newInstance();
								}
								setValue(target, fieldName, value);
							}
						}
					}
					return target;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public Collection<?> extract(Field field, Collection<?> origin) throws Exception {
		Collection<Object> destination = null;
		if (origin != null) {
			Class<?> typeClass = field.getType();
			Map<Class<?>, Class<?>> suported = new HashMap<>();
			suported.put(Set.class, HashSet.class);
			suported.put(List.class, ArrayList.class);
			for (Entry<Class<?>, Class<?>> entry : suported.entrySet()) {
				if (entry.getKey().isAssignableFrom(typeClass)) {
					destination = (Collection<Object>) entry.getValue().newInstance();
					break;
				}
			}
			if (destination != null) {
				ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
				Class<?> genericClass = (Class<?>) parameterizedType.getActualTypeArguments()[0];
				Iterator<?> iterator = origin.iterator();
				while (iterator.hasNext()) {
					Object next = (Object) iterator.next();
					Object object = extract(genericClass, next);
					destination.add(object);
				}
			}
		}
		return destination;
	}
	
	private boolean isPrimitiveOrWrapper(Class<?> valueClass) {
		return ClassUtils.isPrimitiveOrWrapper(valueClass) || CharSequence.class.isAssignableFrom(valueClass);
	}
	
	private Object getValue(Object origin, String fieldName) throws Exception {
		return FieldUtils.readField(origin, fieldName, true);
	}
	
	private void setValue(Object target, String fieldName, Object value) throws Exception {
		Class<?> valueClass = value.getClass();
		Class<?> targetClass = target.getClass();
		Field field = FieldUtils.getField(targetClass, fieldName, true);
		Class<?> type = field.getType();
		if (!isPrimitiveOrWrapper(valueClass)) {
			if (valueClass.isEnum()) {
				value = MethodUtils.invokeMethod(value, "name");
			} else if (Collection.class.isAssignableFrom(valueClass)) {
				value = extract(field, (Collection<?>) value);
			}
			else {
				value = extract(type, value);
			}
		}
		valueClass = value.getClass();
		if (!type.isAssignableFrom(valueClass)) {
			if (type.isEnum()) {
				if (value instanceof String) {
					value = TypeUtils.parse(type, (String) value);					
				}
			}
		}
		FieldUtils.writeField(target, fieldName, value, true);
	}
	
	private String[] getOriginFieldNames(Class<?> originClass) {
		String[] originFieldNames = (String[]) MapUtils.getObject(processors, originClass);
		if (ArrayUtils.isEmpty(originFieldNames)) {
			Field[] fields = FieldUtils.getAllFields(originClass);
			if (ArrayUtils.isNotEmpty(fields)) {
				originFieldNames = new String[fields.length];
				for (int i = 0; i < fields.length; i++) {
					Field field = fields[i];
					originFieldNames[i] = field.getName();
				}
			}
		}
		return originFieldNames;
	}
	
	private List<String> getTargetFieldNames(Class<?> targetClass) {
		List<String> targetFieldNames = null;
		Field[] fields = FieldUtils.getAllFields(targetClass);
		for (Field field : fields) {
			if (targetFieldNames == null) {
				targetFieldNames = new ArrayList<>(fields.length);
			}
			targetFieldNames.add(field.getName());
		}
		return targetFieldNames;
	}
	
	public static class Builder {
		private Map<Class<?>, String[]> processors;
		public Copy build() {
			return new Copy(this);
		}
		public Builder processor(Class<?> targetClass, String...fieldNames) {
			if (processors == null) {
				processors = new HashMap<>();
			}
			processors.put(targetClass, fieldNames);
			return this;
		}
	}
}