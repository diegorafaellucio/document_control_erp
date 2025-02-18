package net.wasys.getdoc.domain.enumeration;

public enum TipoConsultaExterna {
	BRSCAN("BRSCAN"),
	CREDILINK("Credilink"),
	CRIVO("Crivo"),
	DATA_VALID("DataValid "),
	DATA_VALID_BIOMETRIA("DataValidBiometria"),
	DECODE("Decode"),
	DETRAN_ARN("DetranArn"),
	LEILAO("Leilao"),
	//NFE_INTERESSE("NFeInteresse"),
	RENAVAM_INDICADORES_CHASSI("RenavamIndicadoresChassi"),
	SANTANDER_PROPOSTA_DETALHADA("SantanderPropostaDetalhada"),
	SIA_CONSULTA_LINHA_TEMPO("SiaConsultaLinhaTempo"),
	SIA_CONSULTA_INSCRICOES("SiaConsultaInscricoes"),
	SIA_CONSULTA_COMPROVANTE_INSCRICAO("SiaConsultaComprovanteInscricao"),
	SIA_ATUALIZA_DOCUMENTO("SiaAtualizaDocumento"),
	ATILA_ATUALIZA_DOCUMENTO("AtilaAtualizaDocumento"),
	SIA_ANALISE_ISENCAO_DISCIPLINAS("SiaAnaliseIsencaoDisciplinas"),
	TIPIFICACAO_DARKNET("TipificacaoDarknet"),
	OCR_SPACE("OcrSpace"),
	LOGIN_AZURE("LoginAzure"),
	OCR("OcrDarknet"),
	DOCUMENTOS_DIGITALIZADOS_GETDOC_ALUNO("ConsultaDocumentosDigitalizadosGetdocAluno"),
	CONSULTA_MATRICULA_GETDOC_ALUNO("ConsultaMatriculaGetdocAluno"),
	REAPROVEITA_DOCUMENTO_GETDOC_ALUNO("ReaproveitaDocumentoGetdocAluno"),
	CONSULTA_DOCUMENTO_GRADUACAO_GETDOC_ALUNO("ConsultaDocumentoGraduacaoGetdocAluno"),
	;

	private String varConsulta;

	private TipoConsultaExterna(String varConsulta) {
		this.varConsulta = varConsulta;
	}

	public String getVarConsulta() {
		return varConsulta;
	}

	public static TipoConsultaExterna getByVarConsulta(String var) {

		TipoConsultaExterna[] values = TipoConsultaExterna.values();
		for (TipoConsultaExterna tipo : values) {
			String varConsulta = tipo.getVarConsulta();
			if(varConsulta.equals(var)) {
				return tipo;
			}
		}

		return null;
	}
}
