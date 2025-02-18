package net.wasys.getdoc.domain.vo.filtro;

import net.wasys.getdoc.domain.enumeration.TipoConfiguracaoRelatorio;

public class ConfiguracaoGeracaoRelatorioFiltro {

	private String nome;
	private TipoConfiguracaoRelatorio tipo;
	private Boolean ativo;

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public TipoConfiguracaoRelatorio getTipo() {
		return tipo;
	}

	public void setTipo(TipoConfiguracaoRelatorio tipo) {
		this.tipo = tipo;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
}
