package net.wasys.getdoc.domain.vo;

import java.io.Serializable;

import net.wasys.getdoc.domain.entity.BaseInterna;
import net.wasys.getdoc.domain.entity.FonteExterna;
import net.wasys.getdoc.domain.entity.TipoProcesso;

public class FonteVO implements Serializable {

	private String nome;
	private BaseInterna baseInterna;
	private TipoProcesso tipoProcesso;
	private FonteExterna fonteExterna;

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public BaseInterna getBaseInterna() {
		return baseInterna;
	}

	public void setBaseInterna(BaseInterna baseInterna) {
		this.baseInterna = baseInterna;
	}

	public TipoProcesso getTipoProcesso() {
		return tipoProcesso;
	}

	public void setTipoProcesso(TipoProcesso tipoProcesso) {
		this.tipoProcesso = tipoProcesso;
	}

	public FonteExterna getFonteExterna() {
		return fonteExterna;
	}

	public void setFonteExterna(FonteExterna fonteExterna) {
		this.fonteExterna = fonteExterna;
	}
}
