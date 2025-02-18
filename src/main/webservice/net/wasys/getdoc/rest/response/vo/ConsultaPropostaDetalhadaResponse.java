package net.wasys.getdoc.rest.response.vo;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class ConsultaPropostaDetalhadaResponse{

	@SerializedName("consultaPropostaDetalhadaResponse")
	private ConsultaPropostaDetalhadaResponse consultaPropostaDetalhadaResponse;

	@SerializedName("descricaoErroSistema")
	private String descricaoErroSistema;

	@SerializedName("codigoErroSistema")
	private String codigoErroSistema;

	@SerializedName("codigoErro")
	private String codigoErro;

	@SerializedName("consultaPropostaDetalhada")
	private ConsultaPropostaDetalhada consultaPropostaDetalhada;

	@SerializedName("descricaoErro")
	private String descricaoErro;

	@SerializedName("codigoRetorno")
	private String codigoRetorno;

	@SerializedName("indicadorRechamada")
	private String indicadorRechamada;

	public void setConsultaPropostaDetalhadaResponse(ConsultaPropostaDetalhadaResponse consultaPropostaDetalhadaResponse){
		this.consultaPropostaDetalhadaResponse = consultaPropostaDetalhadaResponse;
	}

	public ConsultaPropostaDetalhadaResponse getConsultaPropostaDetalhadaResponse(){
		return consultaPropostaDetalhadaResponse;
	}

	public void setDescricaoErroSistema(String descricaoErroSistema){
		this.descricaoErroSistema = descricaoErroSistema;
	}

	public String getDescricaoErroSistema(){
		return descricaoErroSistema;
	}

	public void setCodigoErroSistema(String codigoErroSistema){
		this.codigoErroSistema = codigoErroSistema;
	}

	public String getCodigoErroSistema(){
		return codigoErroSistema;
	}

	public void setCodigoErro(String codigoErro){
		this.codigoErro = codigoErro;
	}

	public String getCodigoErro(){
		return codigoErro;
	}

	public void setConsultaPropostaDetalhada(ConsultaPropostaDetalhada consultaPropostaDetalhada){
		this.consultaPropostaDetalhada = consultaPropostaDetalhada;
	}

	public ConsultaPropostaDetalhada getConsultaPropostaDetalhada(){
		return consultaPropostaDetalhada;
	}

	public void setDescricaoErro(String descricaoErro){
		this.descricaoErro = descricaoErro;
	}

	public String getDescricaoErro(){
		return descricaoErro;
	}

	public void setCodigoRetorno(String codigoRetorno){
		this.codigoRetorno = codigoRetorno;
	}

	public String getCodigoRetorno(){
		return codigoRetorno;
	}

	public void setIndicadorRechamada(String indicadorRechamada){
		this.indicadorRechamada = indicadorRechamada;
	}

	public String getIndicadorRechamada(){
		return indicadorRechamada;
	}
}