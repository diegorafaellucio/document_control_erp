package net.wasys.getdoc.domain.enumeration;

import net.wasys.getdoc.domain.entity.TextoPadrao;

public enum TipoEmail {
	NOTIFICACAO_CANDIDATO_SISPROUNI(TextoPadrao.NOTIFICACAO_CANDIDATO_SISPROUNI_ID, "envia-notificacao-candidato-sisprouni.htm"),
	NOTIFICACAO_CANDIDATO_SISFIES(TextoPadrao.NOTIFICACAO_CANDIDATO_SISFIES_ID, "envia-notificacao-candidato-sisfies.htm"),
	NOTIFICACAO_PENDENCIA_CANDIDATO_SISPROUNI(TextoPadrao.NOTIFICACAO_PENDENCIA_CANDIDATO_SISPROUNI_ID, "envia-pendencia-candidato-sisprouni.htm"),
	NOTIFICACAO_CANDIDATO_RASCUNHO(TextoPadrao.NOTIFICACAO_CANDIDATO_RASCUNHO_ID, "envio-email-documentacao-pendente.htm"),
	NOTIFICACAO_CANDIDATO_REPROVACAO(TextoPadrao.NOTIFICACAO_CANDIDATO_REPROVACAO_ID, "envio-email-documentacao-pendente.htm"),
	NOTIFICACAO_CANDIDATO_APROVACAO(TextoPadrao.NOTIFICACAO_CANDIDATO_APROVACAO_ID, "envio-email-documentacao-aprovada.htm");

	private Long textoPadraoId;
	private String nomeTemplate;

	TipoEmail(Long textoPadraoId, String nomeTemplate){
		this.textoPadraoId = textoPadraoId;
		this.nomeTemplate = nomeTemplate;
	}

	public String getNomeTemplate() {
		return nomeTemplate;
	}

	public Long getTextoPadraoId() {
		return textoPadraoId;
	}

	public static String getNomeTemplate(Long textoPadraoId){

		if(textoPadraoId == null){
			return null;
		}

		TipoEmail[] tiposEmails = TipoEmail.values();
		for(TipoEmail key : tiposEmails){
			Long keyTextoPadraoId = key.getTextoPadraoId();
			if(textoPadraoId.equals(keyTextoPadraoId)) {
				return key.getNomeTemplate();
			}
		}

		return null;
	}
}
