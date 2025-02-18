package net.wasys.getdoc.rest.response.vo;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class Body{

	@SerializedName("consultaPropostaDetalhadaResponse")
	private ConsultaPropostaDetalhadaResponse consultaPropostaDetalhadaResponse;

	public void setConsultaPropostaDetalhadaResponse(ConsultaPropostaDetalhadaResponse consultaPropostaDetalhadaResponse){
		this.consultaPropostaDetalhadaResponse = consultaPropostaDetalhadaResponse;
	}

	public ConsultaPropostaDetalhadaResponse getConsultaPropostaDetalhadaResponse(){
		return consultaPropostaDetalhadaResponse;
	}
}