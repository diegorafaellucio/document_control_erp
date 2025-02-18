package net.wasys.getdoc.rest.response.vo.agenteCertificadoResponse;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

import java.util.List;

@Generated("com.robohorse.robopojogenerator")
public class ListaAgenteCertificadoResponse{

	@SerializedName("listaAgenteCertificadoResponse")
	private ListaAgenteCertificadoResponse listaAgenteCertificadoResponse;

	@SerializedName("codigoErro")
	private String codigoErro;

	@SerializedName("agentesCertificados")
	private List<AgentesCertificadosItem> agentesCertificados;

	@SerializedName("descricaoErro")
	private String descricaoErro;

	@SerializedName("codigoRetorno")
	private String codigoRetorno;

	public void setListaAgenteCertificadoResponse(ListaAgenteCertificadoResponse listaAgenteCertificadoResponse){
		this.listaAgenteCertificadoResponse = listaAgenteCertificadoResponse;
	}

	public ListaAgenteCertificadoResponse getListaAgenteCertificadoResponse(){
		return listaAgenteCertificadoResponse;
	}

	public void setCodigoErro(String codigoErro){
		this.codigoErro = codigoErro;
	}

	public String getCodigoErro(){
		return codigoErro;
	}

	public void setAgentesCertificados(List<AgentesCertificadosItem> agentesCertificados){
		this.agentesCertificados = agentesCertificados;
	}

	public List<AgentesCertificadosItem> getAgentesCertificados(){
		return agentesCertificados;
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