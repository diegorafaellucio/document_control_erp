package net.wasys.getdoc.rest.response.vo.alteracaoGarantiaResponse;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class Body{

	@SerializedName("alteracaoGarantiaResponse")
	private AlteracaoGarantiaResponse alteracaoGarantiaResponse;

	public void setAlteracaoGarantiaResponse(AlteracaoGarantiaResponse alteracaoGarantiaResponse){
		this.alteracaoGarantiaResponse = alteracaoGarantiaResponse;
	}

	public AlteracaoGarantiaResponse getAlteracaoGarantiaResponse(){
		return alteracaoGarantiaResponse;
	}
}