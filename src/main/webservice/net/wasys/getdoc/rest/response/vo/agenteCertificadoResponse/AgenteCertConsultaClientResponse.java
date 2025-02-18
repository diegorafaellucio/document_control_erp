package net.wasys.getdoc.rest.response.vo.agenteCertificadoResponse;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class AgenteCertConsultaClientResponse{

	@SerializedName("Body")
	private Body body;

	public void setBody(Body body){
		this.body = body;
	}

	public Body getBody(){
		return body;
	}
}