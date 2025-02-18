package net.wasys.util.faces.component;

import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import net.bootsfaces.component.inputTextarea.InputTextarea;

public class CampoInputTextArea extends InputTextarea {

	private String name;

	@Override
	public Object saveState(FacesContext context) {
		Object[] values = new Object[3];
		values[0] = super.saveState(context);
		values[1] = getFacets();
		values[2] = getName();
		return values;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void restoreState(FacesContext context, Object state) {
		Object[] values = (Object[]) state;
		super.restoreState(context, values[0]);
		Map<String, UIComponent> facets = (Map<String, UIComponent>) values[1];
		String name = (String) values[2];
		setName(name);
		getFacets().putAll(facets);
	}

	@Override
	public void decode(FacesContext context) {
		String name = getName();
		String submittedValue = (String) context.getExternalContext().getRequestParameterMap().get(name);
		setSubmittedValue(submittedValue);
		setValid(true);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}