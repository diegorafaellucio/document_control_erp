package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.enumeration.TipoConsultaExterna;

public class SantanderPropostaDetalhadaRequestVO extends WebServiceClientVO {

	public static final String NUMERO_INTERMEDIARIO = "NUMERO_INTERMEDIARIO";
	public static final String CODIGO_PROPOSTA = "CODIGO_PROPOSTA";

	private String numeroIntermediario;
	private String codigoProposta;

	@Override
	public TipoConsultaExterna getTipoConsultaExterna() {
		return TipoConsultaExterna.SANTANDER_PROPOSTA_DETALHADA;
	}

	public String getNumeroIntermediario() {
		return numeroIntermediario;
	}

	public void setNumeroIntermediario(String numeroIntermediario) {
		this.numeroIntermediario = numeroIntermediario;
	}

	public String getCodigoProposta() {
		return codigoProposta;
	}

	public void setCodigoProposta(String codigoProposta) {
		this.codigoProposta = codigoProposta;
	}
}
