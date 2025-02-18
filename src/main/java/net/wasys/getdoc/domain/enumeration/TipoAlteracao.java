package net.wasys.getdoc.domain.enumeration;

import net.wasys.util.ddd.Entity;

public enum TipoAlteracao {

	EXCLUSAO,
	CRIACAO,
	ATUALIZACAO;

	public static TipoAlteracao getCriacaoOuAtualizacao(Entity entity) {

		Long id = entity.getId();
		TipoAlteracao tipoAlteracao = id == null ? TipoAlteracao.CRIACAO : TipoAlteracao.ATUALIZACAO;
		return tipoAlteracao;
	}
}