package net.wasys.getdoc.domain.vo.filtro;

import net.wasys.getdoc.domain.entity.BaseInterna;

import java.util.LinkedHashMap;
import java.util.Map;

public class BaseRegistroFiltro {

	private Long id;
	private BaseInterna baseInterna;
	private String chaveUnicidade;
	private Integer qntColunas;
	private Map<String, String[]> camposFiltro = new LinkedHashMap<>();
	private Boolean ativo;
	private Long usuarioId;
	private boolean usarLikeCampos;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public BaseInterna getBaseInterna() {
		return baseInterna;
	}

	public void setBaseInterna(BaseInterna baseInterna) {
		this.baseInterna = baseInterna;
	}

	public String getChaveUnicidade() {
		return chaveUnicidade;
	}

	public void setChaveUnicidade(String chaveUnicidade) {
		this.chaveUnicidade = chaveUnicidade;
	}

	public Integer getQntColunas() {
		return qntColunas;
	}

	public void setQntColunas(Integer qntColunas) {
		this.qntColunas = qntColunas;
	}

	public void addCampoFiltro(String campo, String... valor) {
		camposFiltro.put(campo, valor);
	}

	public Map<String, String[]> getCamposFiltro() {
		return camposFiltro;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	public Long getUsuarioId() { return usuarioId; }

	public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

	public void setUsarLikeCampos(boolean usarLikeCampos) {
		this.usarLikeCampos = usarLikeCampos;
	}

	public boolean isUsarLikeCampos() {
		return usarLikeCampos;
	}
}
