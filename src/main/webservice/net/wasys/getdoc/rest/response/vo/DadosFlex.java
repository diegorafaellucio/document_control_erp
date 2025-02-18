package net.wasys.getdoc.rest.response.vo;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class DadosFlex{

	@SerializedName("cdPctFlex")
	private String cdPctFlex;

	public void setCdPctFlex(String cdPctFlex){
		this.cdPctFlex = cdPctFlex;
	}

	public String getCdPctFlex(){
		return cdPctFlex;
	}
}