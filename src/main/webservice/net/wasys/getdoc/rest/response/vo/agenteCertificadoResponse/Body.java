package net.wasys.getdoc.rest.response.vo.agenteCertificadoResponse;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class Body{

	@SerializedName("listaAgenteCertificadoResponse")
	private ListaAgenteCertificadoResponse listaAgenteCertificadoResponse;

	public void setListaAgenteCertificadoResponse(ListaAgenteCertificadoResponse listaAgenteCertificadoResponse){
		this.listaAgenteCertificadoResponse = listaAgenteCertificadoResponse;
	}

	public ListaAgenteCertificadoResponse getListaAgenteCertificadoResponse(){
		return listaAgenteCertificadoResponse;
	}
}