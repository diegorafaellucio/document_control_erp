package net.wasys.getdoc.rest.response.vo.imprimeDocumentosGeraisResponse;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class ImprimeDocumentosGeraisResponse{

	@SerializedName("imprimeDocumentosGeraisResponse")
	private ImprimeDocumentosGeraisResponse imprimeDocumentosGeraisResponse;

	@SerializedName("resultCode")
	private String resultCode;

	@SerializedName("message")
	private String message;

	@SerializedName("content")
	private String content;

	public void setImprimeDocumentosGeraisResponse(ImprimeDocumentosGeraisResponse imprimeDocumentosGeraisResponse){
		this.imprimeDocumentosGeraisResponse = imprimeDocumentosGeraisResponse;
	}

	public ImprimeDocumentosGeraisResponse getImprimeDocumentosGeraisResponse(){
		return imprimeDocumentosGeraisResponse;
	}

	public void setResultCode(String resultCode){
		this.resultCode = resultCode;
	}

	public String getResultCode(){
		return resultCode;
	}

	public void setMessage(String message){
		this.message = message;
	}

	public String getMessage(){
		return message;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}