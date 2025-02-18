package net.wasys.getdoc.domain.enumeration;

public enum LogAcessoProcessor {

	RELATORIO_GERAL("RelatorioGeralExporter"),
	COLUNA_VALOR_IMPORTER("ColunaValorImporter"),
	DASHBOARD_MENSAL_EXPORTER("DashboardMensalExporter"),
	RELATORIO_ATIVIDADES_EXPORTER("RelatorioAtividadesExporter"),
	RELATORIO_CANDIDATO_EXPORTER("RelatorioCandidatoExporter"),
	RELATORIO_GERAL_EXPORTER("RelatorioGeralExporter"),
	RELATORIO_LOGIN_LOG_EXPORTER("RelatorioLoginLogExporter"),
	RELATORIO_PENDENCIA_DOCUMENTO_EXPORTER("RelatorioPendenciaDocumentoExporter"),
	RELATORIO_PRODUTIVIDADE_PROUNI_EXPORTER("RelatorioProdutividadeProuniExporter"),
	RELATORIO_STATUS_LABORAL_EXPORTER("RelatorioStatusLaboralExporter"),
	RELATORIO_TIPIFICACAO_EXPORTER("RelatorioTipificacaoExporter"),
	RELATORIO_OPERACAO_EXPORTER("RelatorioOperacaoExporter"),
	RELATORIO_LOG_ANALISE_IA("LogAnaliseIAExporter")
	;

	private String key;

	LogAcessoProcessor(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}
}
