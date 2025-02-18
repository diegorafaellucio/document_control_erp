package net.wasys.getdoc.faces.composite;

import javax.el.ValueExpression;
import javax.faces.component.FacesComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.component.html.HtmlPanelGroup;

import net.wasys.getdoc.domain.entity.Ajuda;

@FacesComponent("passoComposite")
public class PassoComposite extends UINamingContainer {

	private Ajuda atual;
	private HtmlPanelGroup modal;

	@Override
	public void setValueExpression(String name, ValueExpression expression) {
		if ("atual".equals(name)) {
			setAtual((Ajuda) expression.getValue(getFacesContext().getELContext()));
		} else if ("modal".equals(name)) {
			setModal((HtmlPanelGroup) expression.getValue(getFacesContext().getELContext()));
		} else {
			super.setValueExpression(name, expression);
		}
	}

	public Ajuda getAtual() {
		return atual;
	}

	public HtmlPanelGroup getModal() {
		return modal;
	}

	public void setAtual(Ajuda atual) {
		this.atual = atual;
	}

	public void setModal(HtmlPanelGroup modal) {
		this.modal = modal;
	}
}
