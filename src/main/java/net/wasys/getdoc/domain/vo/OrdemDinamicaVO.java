package net.wasys.getdoc.domain.vo;

import org.primefaces.model.SortOrder;

public class OrdemDinamicaVO {

	private String key;
	private SortOrder order;

	public OrdemDinamicaVO(){
	}

	public String getKey() { return key; }

	public void setKey(String key) { this.key = key; }

	public SortOrder getOrder() { return order; }

	public void setOrder(SortOrder order) { this.order = order; }
}
