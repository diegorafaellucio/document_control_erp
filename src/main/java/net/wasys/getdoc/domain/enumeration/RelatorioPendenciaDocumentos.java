package net.wasys.getdoc.domain.enumeration;

public class RelatorioPendenciaDocumentos {

    public enum ColunasEnum {
        ID("ID"),
        SITUACAO("Situação"),
        SITUACAO_ANTERIOR("Situação Anterior"),
        REGIONAL("Regional"),
        COD_INSTITUICAO("Cod. Instituição"),
        INSTITUICAO("Instituição"),
        COD_CAMPUS("Cod. Campus"),
        CAMPUS("Campus"),
        POLO_PARCEIRO("Polo Parceiro"),
        COD_CURSO("Cod. Curso"),
        CURSO("Curso"),
		FORMA_INGRESSO("Forma Ingresso"),
		MODALIDADE_ENSINO("Modalidade"),
		NUM_INSCRICAO("Numero Inscrição"),
		NUM_CANDIDATO("Numero Candidato"),
		MATRICULA("Matrícula"),
		ORIGEM("Origem"),
		TIPO_CURSO("Tipo Curso"),
		PERIODO_INGRESSO("Periodo Ingresso"),
		TURNO("Turno"),
		ALUNO("Aluno"),
		CPF("CPF"),
        DOCUMENTO("Documento"),
        STATUS_DOCUMENTO("Status"),
        OBSERVACAO("Observacao"),
        TOTAL_IMAGENS("Total Imagens"),
        EMAIL("E-mail"),
        TELEFONE("Telefone"),
		DATA_ENVIO_ANALISE("Data Envio Análise"),
		DATA_FINALIZACAO_ANALISE("Data Finalização Análise"),
		TOTAL_VEZES_EM_ANALISE("Total Vezes em Análise"),
        MOTIVO_DE_REJEITE("Irregularidade"),
        OBRIGATORIO("Doc.Obrigatório?"),
        VERSAO_ATUAL("Versão Documento"),
        PASTA_VERMELHA("Pasta Vermelha"),
        PASTA_AMARELA("Pasta Amarela"),
        TIPO_PROCESSO("Tipo de Processo"),
        MODELO_DOCUMENTO("Modelo Documento"),
        DATA_CRIACAO("Data Criação"),
        DOCUMENTO_ID("Documento Id"),
        NOME_IMPORTACAO("Nome Importação"),
        LOCAL_OFERTA("Local Oferta"),
        CHAMADA("Chamada"),
        NUMERO_MENBRO("Número Membro"),
        ANALISTA("Analista"),
        ANALISTA_LOGIN("Analista Login"),
        NUMERO_DE_PAGINAS("Número de Página"),
        PROFESSOR_DE_REDE_PUBLICA("Professor de Rede Pública"),
        ENSINO_MEDIO_EM("Ensino Médio Em"),
        CANDIDATO_EH_DEFICIENTE("Candidato e Deficiente"),
        TIPO_DE_BOLSA_IMPORTACAO("Tipo de Bolsa Importação"),
        TURNO_IMPORTACAO("Turno Importação"),
        ENDERECO_IMPORTACAO("Endereço Importação"),
        CIDADE_IMPORTACAO("Cidade Importação"),
        CEP_IMPORTACAO("Cep Importação"),
        EMAIL_IMPORTACAO("E-mail Importação"),
        DDD_TELEFONE_IMPORTACAO("DDD telefone Importação"),
        NOTA_MEDIA_IMPORTACAO("Nota Média Importação"),
        TIPO_PROUNI_IMPORTACAO("Tipo Prouni Importação"),
        CURSO_IMPORTACAO("Curso Importação"),
        CPF_IMPORTACAO("Cpf Importação"),
        DATA_VINCULO("Data Vinculo"),
        USA_TERMO("Usa Termo"),
        PERIODO_INGRESSO_IMPORTACAO("Período Importação"),
        ORIGEM_DOCUMENTO("Origem Documento")
	    ;

        private String nome;
        ColunasEnum(String nome) {
            this.nome = nome;
        }
        public String getNome() {
            return nome;
        }
    }
}