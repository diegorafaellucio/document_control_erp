package net.wasys.getdoc.domain.enumeration;

import net.wasys.getdoc.domain.entity.TipoProcesso;

public enum TipoProcessoPortal {

	VESTIBULAR (1L, TipoProcesso.VESTIBULAR),
	TRANSFERENCIA_EXTERNA (2L, TipoProcesso.TRANSFERENCIA_EXTERNA),
	MSV_EXTERNA (3L, TipoProcesso.MSV_EXTERNA),
	MSV_INTERNA (5L, TipoProcesso.MSV_INTERNA),
	ENEM (7L, TipoProcesso.ENEM),
	POS_GRADUACAO (15L, TipoProcesso.POS_GRADUACAO),
	CONVENIO_UNIVERSIDADE_EXTERIOR (17L, TipoProcesso.CONVENIO_UNIVERSIDADE_EXTERIOR),
	VESTIBULAR_INGRESSO_SIMPLIFICADO (24L, TipoProcesso.VESTIBULAR),
	CERTIFICACAO_INTERNACIONAL(23L, TipoProcesso.CERTIFICACAO_INTERNACIONAL);


	private Long classificacao;
	private Long tipoProcessoId;

	TipoProcessoPortal(Long classificacao, Long tipoProcessoId){

		this.classificacao = classificacao;
		this.tipoProcessoId = tipoProcessoId;
	}

	public Long getClassificacao() {
		return classificacao;
	}

	public Long getTipoProcessoId() {
		return tipoProcessoId;
	}

	public static Long getByClassificacao(Long classificacaoProcessoId){
		TipoProcessoPortal[] tipoProcesso = TipoProcessoPortal.values();
		for(TipoProcessoPortal key : tipoProcesso){
			Long classificacao = key.getClassificacao();
			if(classificacao.equals(classificacaoProcessoId))
				return key.getTipoProcessoId();
		}
		return null;
	}
}
