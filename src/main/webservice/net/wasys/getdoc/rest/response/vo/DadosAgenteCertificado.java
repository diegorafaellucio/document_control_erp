package net.wasys.getdoc.rest.response.vo;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class DadosAgenteCertificado{

	@SerializedName("dtValidadeCert")
	private String dtValidadeCert;

	@SerializedName("nome")
	private String nome;

	@SerializedName("numCpfAgente")
	private String numCpfAgente;

	@SerializedName("codigoAgenteCertificado")
	private String codigoAgenteCertificado;

	@SerializedName("codigoCertificadoAgente")
	private String codigoCertificadoAgente;

	public void setDtValidadeCert(String dtValidadeCert){
		this.dtValidadeCert = dtValidadeCert;
	}

	public String getDtValidadeCert(){
		return dtValidadeCert;
	}

	public void setNome(String nome){
		this.nome = nome;
	}

	public String getNome(){
		return nome;
	}

	public void setNumCpfAgente(String numCpfAgente){
		this.numCpfAgente = numCpfAgente;
	}

	public String getNumCpfAgente(){
		return numCpfAgente;
	}

	public void setCodigoAgenteCertificado(String codigoAgenteCertificado){
		this.codigoAgenteCertificado = codigoAgenteCertificado;
	}

	public String getCodigoAgenteCertificado(){
		return codigoAgenteCertificado;
	}

	public void setCodigoCertificadoAgente(String codigoCertificadoAgente){
		this.codigoCertificadoAgente = codigoCertificadoAgente;
	}

	public String getCodigoCertificadoAgente(){
		return codigoCertificadoAgente;
	}
}