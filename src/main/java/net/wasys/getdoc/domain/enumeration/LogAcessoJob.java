package net.wasys.getdoc.domain.enumeration;

public enum LogAcessoJob {

	ATRASO_SOLICITACAO_JOB("AtrasoSolicitacaoJob"),
	RELATORIO_GERAL_JOB("RelatorioGeralJob"),//0 0 0/1 * * ?
	IMPORTACAO_POP3_JOB("ImportacaoPop3Job"),//0 0/5 * * * ?
	EXECUCAO_REGRAS_GRUPO1_JOB("ExecucaoRegrasGrupo1Job"),//5/30 * * * * ?
	EXECUCAO_REGRAS_GRUPO2_JOB("ExecucaoRegrasGrupo2Job"),//10/30 * * * * ?
	EXECUCAO_REGRAS_GRUPO3_JOB("ExecucaoRegrasGrupo3Job"),//10/30 * * * * ?
	TIPIFICACAO_JOB("TipificacaoJob"),//15 0/1 * * * ?
	TIPIFICACAO2_JOB("Tipificacao2Job"),//15 0/1 * * * ?
//	TIPIFICACAO3_JOB("Tipificacao3Job"),//15 0/1 * * * ?
	VISION_API_JOB("VisionApiJob"),//25 0/1 * * * ?
	SINCRONIZA_BASE_JOB("SincronizaBaseJob"),//0 4/5 * * * ?
	LIMPA_TEMP_JOB("LimpaTempJob"),//0 0 03 * * ?
	CONTINGENCIA_NOTIFICACAO_OCR_JOB("ContingenciaNotificacaoOcrJob"),//5 0/1 * * * ?
	ATRASO_REQUISICAO_ANALISTA_JOB("AtrasoRequisicaoAnalistaJob"),
	ATRASO_REQUISICAO_GESTOR_JOB("AtrasoRequisicaoGestorJob"),
	BACALHAU_DIARIO_JOB("BacalhauDiarioJob"),
	BACALHAU_GERAL_AGENDADO_JOB("BacalhauGeralAgendadoJob"),
	CONSULTA_BR_SCAN("ConsultaBrScan"),
	CONTINGENCIA_AGENDAMENTO_OCR_JOB("ContingenciaAgendamentoOcrJob"),
	LIMPA_LOG_ACESSO_JOB("LimpaLogAcessoJob"),
	LIMPA_CONSULTA_EXTERNA_JOB("LimpaConsultaExternaJob"),
	STATUS_PRAZO_JOB("StatusPrazoJob"),
	LIMPA_PROCESSO_REGRA_JOB("LimpaProcessoRegraJob"),
	FACE_RECOGNITION_JOB("FaceRecognitionJob"),
	NOTIFICACAO_DOCUMENTO_SIA_JOB("NotificacaoDocumentoSiaJob"),
	IMPORTACAO_EXCHANGE_JOB("ImportacaoExchangeJob"),
	ENVIO_EMAIL_NOTIFICACAO_ALUNO_JOB("EnvioEmailNotificacaoAlunoJob"),
	NOTIFICA_CANDIDATO_FIES_AND_PROUNI_JOB("NotificaCandidatoFiesAndProuniJob"),
	NOTIFICA_ANALISE_ISENCAO_JOB("NotificacaoAnaliseIsencaoJob"),
	ENVIO_PARA_ANALISE_GRADUACAO_JOB("EnvioNovoParaAnaliseJob"),
	ENVIO_PARA_ANALISE_JOB("EnvioPendenteParaAnaliseJob"),
	ATUALIZAR_GERAL_JOB("AtualizarGeralJob"),
	RELATORIO_GERAL_EXPORTACAO_JOB("RelatorioGeralExportacaoJob"),
	GERA_HASH_UNICO("GeraHashUnicoJob"),
	NOTIFICA_PENDENCIA_PROUNI_JOB("NotificaPendenciaProuniJob"),
	ENVIO_PROCESSO_PROUNI_JOB("EnvioProcessoProuniJob"),
	ENVIO_PARA_ANALISE_SISFIES_SISPROUNI_CONCLUIDO_JOB("EnvioSisFiesProuniConcluidoParaAnaliseJob"),
	ATUALIZA_DADO_USA_TERMO_JOB("AtualizaDadoUsaTermoJob"),
	REAPROVEITAMENTO_DIGITALIZADOS_JOB("ReaproveitamentoDigitalizadosJob"),
	GERAR_RELATORIOS_JOB("GerarRelatoriosJob"),
	LIMPA_RELATORIOS_GERADOS_JOB("LimpaRelatoriosGeradosJob"),
	FLUXO_APROVACAO_TIPIFICACAO_JOB("FluxoAprovacaoTipificacaoJob"),//15 0/1 * * * ?
	FLUXO_APROVACAO_OCR_JOB("FluxoAprovacaoOCRJob"),
	FLUXO_APROVACAO_ANALISE_JOB("FluxoAprovacaoAnaliseJob"),
	VERIFICA_VINCULO_JOB("VerificaVinculoJob"),
	BACALHAU_REVERSO_JOB("BacalhauReversoJob"),
	FUNCAO_JOB("FuncaoJob"),
	ACESSO_USUARIO_JOB("AcessoUsuarioJob"),
	VERIFICA_ALTERACAO_PROCESSO_FINANCIAMENTO_RASCUNHO_JOB("VerificaAlteracaoProcessoFinanciamentoRascunhoJob"),
	ENVIO_PENDENTE_PARA_APROVADO_JOB("EnvioPendenteParaAprovadoJob"),
	CRIAR_PROCESSO_ISENCAO_JOB("CriarProcessoIsencaoJob"),
	NOTIFICACAO_DOCUMENTO_PENDENTE_ATILA_JOB("NotificacaoDocumentoPendenteAtilaJob"),
	;

	private String key;

	LogAcessoJob(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}
}
