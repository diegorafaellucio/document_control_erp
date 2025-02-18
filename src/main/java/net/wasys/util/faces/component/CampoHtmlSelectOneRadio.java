package net.wasys.util.faces.component;

import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.primefaces.component.selectoneradio.SelectOneRadio;

public class CampoHtmlSelectOneRadio extends SelectOneRadio {

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
		this.name = (String) values[2];
		getFacets().putAll(facets);
	}

	@Override
	public void decode(FacesContext context) {
		//super.decode(context);
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