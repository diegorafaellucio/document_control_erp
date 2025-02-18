package net.wasys.getdoc.rest.response.vo.alteracaoGarantiaResponse;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class AlteracaoGarantiaResponse{

	@SerializedName("alteracaoGarantiaClientResponse")
	private AlteracaoGarantiaClientResponse alteracaoGarantiaClientResponse;

	public void setAlteracaoGarantiaClientResponse(AlteracaoGarantiaClientResponse alteracaoGarantiaClientResponse){
		this.alteracaoGarantiaClientResponse = alteracaoGarantiaClientResponse;
	}

	public AlteracaoGarantiaClientResponse getAlteracaoGarantiaClientResponse(){
		return alteracaoGarantiaClientResponse;
	}
}