package net.wasys.getdoc.domain.vo.filtro;

import net.wasys.getdoc.domain.enumeration.StatusProcesso;

import java.util.List;

public class SituacaoFiltro implements Cloneable {

	private Long tipoProcessoId;
	private Boolean ativa;
	private List<StatusProcesso> statusProcesso;
	private String nome;
	private String nomeContem;

	public Long getTipoProcessoId() {
		return tipoProcessoId;
	}

	public void setTipoProcessoId(Long tipoProcessoId) {
		this.tipoProcessoId = tipoProcessoId;
	}

	public Boolean getAtiva() {
		return ativa;
	}

	public void setAtiva(Boolean ativa) {
		this.ativa = ativa;
	}

	public List<StatusProcesso> getStatusProcesso() { return statusProcesso; }

	public void setStatusProcesso(List<StatusProcesso> statusProcesso) {
		this.statusProcesso = statusProcesso;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getNomeContem() {
		return nomeContem;
	}

	public void setNomeContem(String nomeContem) {
		this.nomeContem = nomeContem;
	}

	@Override
	public SituacaoFiltro clone() {
		try {
			return (SituacaoFiltro) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}
