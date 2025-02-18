package net.wasys.getdoc.domain.enumeration;

import net.wasys.getdoc.domain.entity.TipoProcesso;

import java.util.ArrayList;
import java.util.List;

public enum TipoProcessoMestradoDoutorado {

	PS_MESTRADO (9L, TipoProcesso.PS_MESTRADO, "MESTRADO"),
	PS_DOUTORADO (9L, TipoProcesso.PS_DOUTORADO, "DOUTORADO"),
	PS_POS_DOUTORADO (9L, TipoProcesso.PS_POS_DOUTORADO, "PÓS-DOUTORADO"),
	DI_MESTRADO (10L, TipoProcesso.DI_MESTRADO, "MESTRADO"),
	DI_DOUTORADO (10L, TipoProcesso.DI_DOUTORADO, "DOUTORADO");

	private Long classificacao;
	private Long tipoProcessoId;
	private String tipoCurso;

	TipoProcessoMestradoDoutorado(Long classificacao, Long tipoProcessoId, String tipoCurso){

		this.classificacao = classificacao;
		this.tipoProcessoId = tipoProcessoId;
		this.tipoCurso = tipoCurso;
	}

	public Long getClassificacao() {
		return classificacao;
	}

	public Long getTipoProcessoId() {
		return tipoProcessoId;
	}

	public String getTipoCurso() {
		return tipoCurso;
	}

	public static Long getByClassificacaoAndTipoCurso(Long classificacaoProcessoId, String tipoCurso){
		TipoProcessoMestradoDoutorado[] tipoProcesso = TipoProcessoMestradoDoutorado.values();
		for(TipoProcessoMestradoDoutorado key : tipoProcesso){
			Long classificacao = key.getClassificacao();
			String tipo = key.getTipoCurso();
			if(classificacao.equals(classificacaoProcessoId) && tipo.equals(tipoCurso))
				return key.getTipoProcessoId();
		}
		return null;
	}

	public static List<String> getAllTipoCurso(){
		TipoProcessoMestradoDoutorado[] tipoProcesso = TipoProcessoMestradoDoutorado.values();
		List<String> lista = new ArrayList<>();
		for(TipoProcessoMestradoDoutorado key : tipoProcesso){
			String tipo = key.getTipoCurso();
			lista.add(tipo);
		}
		return lista;
	}
}
