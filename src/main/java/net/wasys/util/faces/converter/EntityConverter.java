package net.wasys.util.faces.converter;

import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import net.wasys.util.ddd.Entity;

@FacesConverter(value="entityConverter")
public class EntityConverter implements Converter {

	public Object getAsObject(FacesContext ctx, UIComponent component, String value) {

		if (value != null) {
			Map<String, Object> attributes = component.getAttributes();
			Object o = attributes.get(value);
			return o;
		}

		return null;
	}

	public String getAsString(FacesContext ctx, UIComponent component, Object value) {

		Map<String, Object> attributes = component.getAttributes();

		if (value instanceof Entity) {
			Entity entity = (Entity) value;
			String codigo = entity != null ? String.valueOf(entity.getId()) : String.valueOf(entity);
			attributes.put(codigo, value);
			return codigo;
		}
		else {
			String codigo = String.valueOf(value);
			attributes.put(codigo, value);
			return codigo;
		}
	}
}