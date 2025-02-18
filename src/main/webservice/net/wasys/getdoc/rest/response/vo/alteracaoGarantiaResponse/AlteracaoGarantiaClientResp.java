package net.wasys.getdoc.rest.response.vo.alteracaoGarantiaResponse;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class AlteracaoGarantiaClientResp{

	@SerializedName("Body")
	private Body body;

	public void setBody(Body body){
		this.body = body;
	}

	public Body getBody(){
		return body;
	}
}