package net.wasys.getdoc.domain.enumeration;

public class CamposGravameCetip {

	public enum CampoCetipConsultaEnum {
		OPCAO("in_1316_2"), 
		DATA_CONTRATO("//tbody/tr[15]/td[3]"), 
		CHASSI("in_1396_21"), 
		XPATH_RESULTADO("//tbody/tr[24]"),
		NUMERO_CONTRATO("//tbody/tr[15]/td[17]"), 
		NOME_AGENTE("//tbody/tr[14]/td[3]"), 
		CNPJ_AGENTE("//tbody/tr[14]/td[9]"), 
		NUMERO_GRAVAME("//tbody/tr[16]/td[15]"), 
		DATA_INCLUSAO("//tbody/tr[17]/td[3]"), 
		DATA_BAIXA("//tbody/tr[17]/td[17]"), 
		NUMERO_CONTRATO_SCR("//tbody/tr[18]/td[3]"), 
		COMENTARIO("//tbody/tr[19]/td[3]"), 
		RESULTADO_CONSULTA(""), 
		CHASSI_RESULTADO("//tbody/tr[10]/td[5]"), 
		TIPO_CHASSI_RESULTADO_CONSULTA("//tbody/tr[10]/td[11]"), 
		UF_RESULTADO_CONSULTA("//tbody/tr[11]/td[5]"), 
		PLACA_RESULTADO_CONSULTA("//tbody/tr[11]/td[9]"), 
		UF_LICENCIAMENTO_RESULTADO_CONSULTA("//tbody/tr[11]/td[15]"), 
		RENAVAM_RESULTADO_CONSULTA("//tbody/tr[12]/td[5]"), 
		ANO_FAB_RESULTADO_CONSULTA("//tbody/tr[12]/td[11]"), 
		ANO_MOD_RESULTADO_CONSULTA("//tbody/tr[12]/td[17]"), 
		NOME_AGENTE_RESULTADO_CONSULTA("//tbody/tr[14]/td[5]"), 
		CNPJ_AGENTE_RESULTADO_CONSULTA("//tbody/tr[14]/td[11]"), 
		DIA_CONTRATO_RESULTADO_CONSULTA("//tbody/tr[15]/td[5]"), 
		MES_CONTRATO_RESULTADO_CONSULTA("//tbody/tr[15]/td[9]"), 
		ANO_CONTRATO_RESULTADO_CONSULTA("//tbody/tr[15]/td[13]"), 
		NUMERO_CONTRATO_RESULTADO_CONSULTA("//tbody/tr[15]/td[19]"), 
		QTD_MES_RESULTADO_CONSULTA("//tbody/tr[16]/td[5]"), 
		TIPO_RESTRICAO_RESULTADO_CONSULTA("//tbody/tr[16]/td[11]"), 
		NUMERO_GRAVAME_RESULTADO_CONSULTA("//tbody/tr[16]/td[17]"), 
		DIA_INCLUSAO_RESULTADO_CONSULTA("//tbody/tr[17]/td[5]"), 
		MES_INCLUSAO_RESULTADO_CONSULTA("//tbody/tr[17]/td[9]"), 
		ANO_INCLUSAO_RESULTADO_CONSULTA("//tbody/tr[17]/td[13]"), 
		DIA_BAIXA_RESULTADO_CONSULTA("//tbody/tr[17]/td[19]"), 
		MES_BAIXA_RESULTADO_CONSULTA("//tbody/tr[17]/td[23]"), 
		ANO_BAIXA_RESULTADO_CONSULTA("//tbody/tr[17]/td[27]"), 
		NUM_CONTRATO_SCR_RESULTADO_CONSULTA("//tbody/tr[18]/td[5]"), 
		COMENTARIO_RESULTADO_CONSULTA("//tbody/tr[19]/td[5]"), 
		RESULTADO_CONSULTA_1("//tbody/tr[21]/td[3]"), 
		RESULTADO_CONSULTA_2("//tbody/tr[22]/td[3]"), 
		NAO_EXISTE_OCORRENCIA("//tbody/tr[23]/td[3]"), 
		OPCAO_STATUS_1("//tbody/tr[12]/td[3]"), 
		OPCAO_STATUS_2("//tbody/tr[13]/td[3]"), 
		OPCAO_STATUS_3("//tbody/tr[14]/td[3]"), 
		OPCAO_STATUS_4("//tbody/tr[15]/td[3]"), 
		OPCAO_STATUS_5("//tbody/tr[16]/td[3]"), 
		OPCAO_STATUS_6("//tbody/tr[17]/td[3]"), 
		ESCOLHER_TIPO_CONSULTA_1("in_866_1"),
		ESCOLHER_TIPO_CONSULTA_2("in_946_1"), 
		ESCOLHER_TIPO_CONSULTA_3("in_1026_1"), 
		ESCOLHER_TIPO_CONSULTA_4("in_1106_1"), 
		ESCOLHER_TIPO_CONSULTA_5("in_1186_1"), 
		ESCOLHER_TIPO_CONSULTA_6("in_1266_1"), 
		STATUS_GRAVAME("//tbody/tr[5]/td[7]"), 
		NOME_FINANCIADO("//tbody/tr[8]/td[5]"), 
		CPF_CNPJ("//tbody/tr[8]/td[9]"), 
		;

		private String valor;
		CampoCetipConsultaEnum(String valor) {
			this.valor = valor;
		}
		public String getValor() {
			return valor;
		}
	}

	public enum CampoCetipInclusaoEnum {

		CODIGO("codigo"), 
		SUB_CODIGO("subcodigo"),
		LOGIN_1("login1"),
		LOGIN_2("login2"), 
		SENHA("entity.usuarioSenha.senha0"), 
		BTN_LOGIN("btnLogin"), 
		CHASSI("in_1465_21"),
		RENAVAM("in_1504_11"), 
		CLIENTE("in_496_40"), 
		CPF_CNPJ("in_613_14"), 
		TIPO_CHASSI("in_773_1"), 
		PLACA("in_821_7"),
		UF_PLACA("in_816_2"), 
		ANO_FABRICACAO("in_933_4"), 
		ANO_MODELO("in_953_4"), 
		DIA_DO_MES("in_1218_2"), 
		MES_DO_ANO("in_1223_2"), 
		ANO("in_1228_4"), 
		QTD_PRESTACAO("in_1298_3"), 
		TIPO_RESTRICAO("in_1333_2"), 
		INDICATIVA_DE_MULTA("in_594_3"), 
		INDICATIVA_DE_MULTA_MORADIA("in_634_3"), 
		TAXA_JUROS_MENSAL("in_668_7"), 
		VALOR_IOF("in_945_12"), 
		VALOR_DA_PARCELA("in_825_12"), 
		VALOR_PRINCIPAL_OPERACAO("in_785_12"), 
		TAXA_JUROS_ANUAL("in_748_7"), 
		VALOR_TAXA_CONTRATO("in_705_12"), 
		DATA_PGTO_OP_DIA("in_998_2"), 
		DATA_PGTO_OP_MES("in_1003_2"), 
		DATA_PGTO_OP_ANO("in_1008_4"), 
		UF_OPERACAO("in_1078_2"), 
		CIDADE_OPERACAO("in_1083_25"), 
		DATA_PRIMEIRO_VCTO_DIA("in_1158_2"), 
		DATA_PRIMEIRO_VCTO_MES("in_1163_2"), 
		DATA_PRIMEIRO_VCTO_ANO("in_1168_4"), 
		DATA_ULTIMO_VCTO_DIA("in_1238_2"), 
		DATA_ULTIMO_VCTO_MES("in_1243_2"), 
		DATA_ULTIMO_VCTO_ANO("in_1248_4"), 
		INDICE_UTILIZADO("in_1301_10"), 
		ENDERECO_CREDOR("in_578_32"), 
		NUMERO_CREDOR("in_619_5"), 
		COMPLEMENTO_CREDOR("in_658_22"), 
		BAIRRO_CREDOR("in_689_23"), 
		UF_CREDOR("in_717_2"), 
		COD_MUNICIPIO_CREDOR("in_738_4"), 
		CEP_CREDOR("in_792_8"), 
		DDD_TELEFONE_CREDOR("in_818_4"), 
		TELEFONE_CREDOR("in_825_10"), 
		ENDERECO_CLIENTE("in_978_32"), 
		NUMERO_CLIENTE("in_1019_5"), 
		COMPLEMENTO_CLIENTE("in_1058_22"), 
		BAIRRO_CLIENTE("in_1089_23"), 
		UF_CLIENTE("in_1117_2"), 
		COD_MUNICIPIO_CLIENTE("in_1138_4"), 
		CEP_CLIENTE("in_1192_8"), 
		DDD_TELEFONE_CLIENTE("in_1218_4"), 
		TELEFONE_CLIENTE("in_1225_10"), 
		MULTA("in_1300_7"), 
		JUROS_MORA("in_1340_10"), 
		INDICIA_COMISSAO("in_1380_3"), 
		COMISSAO("in_1420_10"), 
		PENALIDADE("in_1460_3"), 
		OBS_PENALIDADE("in_1466_50"), 
		CNPJ_DO_VENDEDOR("in_1558_14"), 
		PRINCIPAL_RECEBEDOR("in_1638_1"), 
		NUMERO_DO_DOCUMENTO("in_1664_14"), 
		NUMERO_OPERACAO("in_1253_20"), 
		UF_LICENCIAMENTO("in_853_2"), 
		INCLUSAO_GRAVAME("in_1385_2"), 
		TIPO_DOCUMENTO("in_576_1"), 
		OPCAO("in_1403_2"), 
		XPATH_RESULTADO("//tbody/tr[24]"), 
		XPATH_RESULTADO_CONSULTA("//tbody/tr[22]"), 
		CONTINUAR("HATSLINK"),
		;

		private String valor;
		CampoCetipInclusaoEnum(String valor) {
			this.valor = valor;
		}
		public String getValor() {
			return valor;
		}
	}

	public enum ValidacaoEnum {
		USUARIO_DESATIVADO("USUARIO DESATIVADO."), 
		MESSAGES("messages"), 
		TAG_VALIDACAO("label"), 
		VALIDADOR("validador"), 
		BTN_CHECAR("button-DuplaChecagem"), 
		COMPLETAR_CPF("Complete seu CPF:"), 
		INFORMAR_EMAIL("Informe seu e-mail:"), 
		FINAL_DO_CPF("cpf-final"), 
		USUARIO_OCUPADO("//tbody/tr[1]"),
		OK("*============================================================================*"), 
		CONFIRMAR_ENTER("*=================PARACONFIRMAROSDADOS-TECLEENTER==================*"), 
		OK_FUNCAO("*======================TECLAPF/PASEMFUNCAO======================*"), 
		BOTAO_LINK_ENTER("ENTER=CONTINUAR"),
		NAO_EXISTE_OCORRENCIA("*================NAOEXISTEOCORRENCIASPARAESTEVEICULO===============*"), 
		COM_GRAVAME("COMGRAVAME"),
		URL_HOMOLOGACAO("cetiph"),
		;

		private String valor;
		ValidacaoEnum(String valor) {
			this.valor = valor;
		}
		public String getValor() {
			return valor;
		}
	}
}
