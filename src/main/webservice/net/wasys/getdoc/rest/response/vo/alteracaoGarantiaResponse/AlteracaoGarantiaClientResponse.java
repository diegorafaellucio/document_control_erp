package net.wasys.getdoc.rest.response.vo.alteracaoGarantiaResponse;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class AlteracaoGarantiaClientResponse{

	@SerializedName("codigoErro")
	private String codigoErro;

	@SerializedName("descricaoErro")
	private String descricaoErro;

	@SerializedName("codigoRetorno")
	private String codigoRetorno;

	public void setCodigoErro(String codigoErro){
		this.codigoErro = codigoErro;
	}

	public String getCodigoErro(){
		return codigoErro;
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
}