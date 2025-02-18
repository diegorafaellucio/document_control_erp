package net.wasys.getdoc.rest.response.vo;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class ConsultaPropostaDetalhada{

	@SerializedName("dadosBasicosProposta")
	private DadosBasicosProposta dadosBasicosProposta;

	@SerializedName("referenciaPessoalBancaria")
	private ReferenciaPessoalBancaria referenciaPessoalBancaria;

	@SerializedName("dadosEndereco")
	private DadosEndereco dadosEndereco;

	@SerializedName("dadosFlex")
	private DadosFlex dadosFlex;

	@SerializedName("dadosAgenteCertificado")
	private DadosAgenteCertificado dadosAgenteCertificado;

	@SerializedName("dadosClienteDadosProfissionais")
	private DadosClienteDadosProfissionais dadosClienteDadosProfissionais;

	@SerializedName("dadosConjuge")
	private DadosConjuge dadosConjuge;

	@SerializedName("dadosGarantia")
	private DadosGarantia dadosGarantia;

	public void setDadosBasicosProposta(DadosBasicosProposta dadosBasicosProposta){
		this.dadosBasicosProposta = dadosBasicosProposta;
	}

	public DadosBasicosProposta getDadosBasicosProposta(){
		return dadosBasicosProposta;
	}

	public void setReferenciaPessoalBancaria(ReferenciaPessoalBancaria referenciaPessoalBancaria){
		this.referenciaPessoalBancaria = referenciaPessoalBancaria;
	}

	public ReferenciaPessoalBancaria getReferenciaPessoalBancaria(){
		return referenciaPessoalBancaria;
	}

	public void setDadosEndereco(DadosEndereco dadosEndereco){
		this.dadosEndereco = dadosEndereco;
	}

	public DadosEndereco getDadosEndereco(){
		return dadosEndereco;
	}

	public void setDadosFlex(DadosFlex dadosFlex){
		this.dadosFlex = dadosFlex;
	}

	public DadosFlex getDadosFlex(){
		return dadosFlex;
	}

	public void setDadosAgenteCertificado(DadosAgenteCertificado dadosAgenteCertificado){
		this.dadosAgenteCertificado = dadosAgenteCertificado;
	}

	public DadosAgenteCertificado getDadosAgenteCertificado(){
		return dadosAgenteCertificado;
	}

	public void setDadosClienteDadosProfissionais(DadosClienteDadosProfissionais dadosClienteDadosProfissionais){
		this.dadosClienteDadosProfissionais = dadosClienteDadosProfissionais;
	}

	public DadosClienteDadosProfissionais getDadosClienteDadosProfissionais(){
		return dadosClienteDadosProfissionais;
	}

	public void setDadosConjuge(DadosConjuge dadosConjuge){
		this.dadosConjuge = dadosConjuge;
	}

	public DadosConjuge getDadosConjuge(){
		return dadosConjuge;
	}

	public void setDadosGarantia(DadosGarantia dadosGarantia){
		this.dadosGarantia = dadosGarantia;
	}

	public DadosGarantia getDadosGarantia(){
		return dadosGarantia;
	}
}