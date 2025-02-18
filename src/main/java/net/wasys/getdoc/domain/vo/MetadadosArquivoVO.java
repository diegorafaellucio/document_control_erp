package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.enumeration.Origem;

public class MetadadosArquivoVO {

	private String ipOrigem;
	private Origem origem;

	public String getIpOrigem() { return ipOrigem; }

	public void setIpOrigem(String ipOrigem) {
		this.ipOrigem = ipOrigem;
	}

	public Origem getOrigem() {
		return origem;
	}

	public void setOrigem(Origem origem) {
		this.origem = origem;
	}
}
