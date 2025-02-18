package net.wasys.getdoc.domain.enumeration;

import net.wasys.getdoc.domain.entity.Situacao;

import java.util.List;

public class DashboardCampos {

	public static List<Long> inscrito = Situacao.GERAL_RASCUNHO_IDS;
	public static List<Long> digitalizados = Situacao.GERAL_EM_ANALISE_IDS;
	public static List<Long> conferidos = Situacao.GERAL_APROVADO_IDS;

	public enum TipoAgrupamentoEnum {

		ACUMULADO("Acumulado"),
		PERIODO("Por período");

		private String nome;

		TipoAgrupamentoEnum(String nome) {
			this.nome = nome;
		}

		public String getNome() {
			return nome;
		}

	}

	public enum IntervaloEnum {

		DIA("Dia", "1 day"),
		SEMANA("Semana", "7 day"),
		MES("Mês", "1  month"),
		ANO("Ano", "1 year");

		private String nome;
		private String value;

		IntervaloEnum(String nome, String value) {
			this.nome = nome;
			this.value = value;
		}

		public String getNome() {
			return nome;
		}
		public String getValue() {
			return value;
		}

	}

	public enum SituacaoEnum {

		INSCRITO("Inscrito (I)", inscrito),
		DIGITALIZADO("Digitalizado (D)", digitalizados),
		CONFERIDO("Conferido (C)", conferidos);

		private String nome;
		private List<Long> ids;

		SituacaoEnum(String nome, List<Long> ids) {
			this.nome = nome;
			this.ids = ids;
		}

		public String getNome() {
			return nome;
		}
		public List<Long> getIds() {
			return ids;
		}
	}
}