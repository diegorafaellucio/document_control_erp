package net.wasys.getdoc.domain.vo;

import org.apache.commons.lang.StringUtils;

import java.util.Set;
import java.util.TreeSet;

public class OrigemValorVO {

	private Set<String> subRegraIds = new TreeSet<>();
	private String origem;
	private String valor;

	public String getSubRegraId() {
		
		String subRegraId = "";
		for (String subId : subRegraIds) {
			subRegraId = StringUtils.isBlank(subRegraId) ? "" : subRegraId + ", ";
			subRegraId += "#" + subId;
		}
		
		return subRegraId;
	}

	public String getOrigem() {
		return origem;
	}

	public void setOrigem(String origem) {
		this.origem = origem;
	}

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

	public void addSubRegraId(String subId) {
		subRegraIds.add(subId);
	}

	@Override
	public int hashCode() {
		String origem = getOrigem();
		if(StringUtils.isNotBlank(origem)) {
			return origem.hashCode();
		}
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof  OrigemValorVO) {
			String origem1 = getOrigem();
			String origem2 = ((OrigemValorVO) obj).getOrigem();
			return StringUtils.equals(origem1, origem2);
		}
		return false;
	}
}
