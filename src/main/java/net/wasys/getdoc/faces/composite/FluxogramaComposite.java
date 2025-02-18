package net.wasys.getdoc.faces.composite;

import javax.el.ValueExpression;
import javax.faces.component.FacesComponent;
import javax.faces.component.UINamingContainer;

import net.wasys.getdoc.domain.entity.Ajuda;

@FacesComponent("fluxogramaComposite")
public class FluxogramaComposite extends UINamingContainer {

	private Ajuda raiz;

	@Override
	public void setValueExpression(String name, ValueExpression expression) {
		if ("raiz".equals(name)) {
			setRaiz((Ajuda) expression.getValue(getFacesContext().getELContext()));
		} else {
			super.setValueExpression(name, expression);
		}
	}

	public Ajuda getRaiz() {
		return raiz;
	}

	public void setRaiz(Ajuda raiz) {
		this.raiz = raiz;
	}
}
