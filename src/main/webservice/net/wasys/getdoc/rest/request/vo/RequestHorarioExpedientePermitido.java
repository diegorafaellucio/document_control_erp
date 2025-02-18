package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.rest.annotations.NotNull;

/**
 * 
 * @author jonas.baggio@wasys.com.br
 * @create 25 de jul de 2018 17:17:31
 */
@ApiModel(value = "RequestHorarioExpedientePermitido")
public class RequestHorarioExpedientePermitido extends SuperVo {


	@ApiModelProperty(notes = "chegada Manha.")
	private String chegadaManha;

	@ApiModelProperty(notes = "saida Manha.")
	private String saidaManha;

	@ApiModelProperty(notes = "chegada Tarde.")
	private String chegadaTarde;

	@ApiModelProperty(notes = "saida Tarde.")
	private String saidaTarde;

	@ApiModelProperty(notes = "horario Abertura.")
	private String horarioAbertura;

	@ApiModelProperty(notes = "horario Fechamento.")
	private String horarioFechamento;

	@ApiModelProperty(notes = "permitir Acesso Final Semana Feriado.")
	private boolean permitirAcessoFinalSemanaFeriado;



	public RequestHorarioExpedientePermitido(){}

	public String getChegadaManha() {
		return chegadaManha;
	}

	public void setChegadaManha(String chegadaManha) {
		this.chegadaManha = chegadaManha;
	}

	public String getSaidaManha() {
		return saidaManha;
	}

	public void setSaidaManha(String saidaManha) {
		this.saidaManha = saidaManha;
	}

	public String getChegadaTarde() {
		return chegadaTarde;
	}

	public void setChegadaTarde(String chegadaTarde) {
		this.chegadaTarde = chegadaTarde;
	}

	public String getSaidaTarde() {
		return saidaTarde;
	}

	public void setSaidaTarde(String saidaTarde) {
		this.saidaTarde = saidaTarde;
	}

	public String getHorarioAbertura() {
		return horarioAbertura;
	}

	public void setHorarioAbertura(String horarioAbertura) {
		this.horarioAbertura = horarioAbertura;
	}

	public String getHorarioFechamento() {
		return horarioFechamento;
	}

	public void setHorarioFechamento(String horarioFechamento) {
		this.horarioFechamento = horarioFechamento;
	}

	public boolean isPermitirAcessoFinalSemanaFeriado() {
		return permitirAcessoFinalSemanaFeriado;
	}

	public void setPermitirAcessoFinalSemanaFeriado(boolean permitirAcessoFinalSemanaFeriado) {
		this.permitirAcessoFinalSemanaFeriado = permitirAcessoFinalSemanaFeriado;
	}
}