package net.wasys.getdoc.rest.response.vo.imprimeDocumentosGeraisResponse;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class Body{

	@SerializedName("imprimeDocumentosGeraisResponse")
	private ImprimeDocumentosGeraisResponse imprimeDocumentosGeraisResponse;

	public void setImprimeDocumentosGeraisResponse(ImprimeDocumentosGeraisResponse imprimeDocumentosGeraisResponse){
		this.imprimeDocumentosGeraisResponse = imprimeDocumentosGeraisResponse;
	}

	public ImprimeDocumentosGeraisResponse getImprimeDocumentosGeraisResponse(){
		return imprimeDocumentosGeraisResponse;
	}
}