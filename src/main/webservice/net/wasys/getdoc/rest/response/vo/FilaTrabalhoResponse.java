package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.rest.request.vo.SuperVo;

import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "FilaTrabalhoResponse")
public class FilaTrabalhoResponse extends SuperVo {

	@ApiModelProperty(notes = "Indica se o usuário possui permissão para criar uma nova requisição.")
	private boolean permiteCriar;

	@ApiModelProperty(notes = "Indica se o usuário possui permissão para trocar analista.")
	private boolean permiteTrocarAnalista;

	@ApiModelProperty(notes = "Indica se o usuário pode filtrar por analista.")
	private boolean permiteFiltrarAnalista;

	@ApiModelProperty(notes = "Indica se o usuário pode verificar qual é o próximo processo.")
	private boolean permiteVerificarProxima;

	@ApiModelProperty(notes = "Indica se o usuário poderá visualizar os filtros /realizar buscas.")
	private boolean permiteFiltrar;

	@ApiModelProperty(notes = "Contador de processos em determinado status.")
	private List<ContadorStatusProcesso> contadorStatusProcesso;

	@ApiModelProperty(notes = "Lista de processos da fila de trabalho.")
	private List<FilaTrabalhoVoResponse> filaTrabalhoResponse;

	@ApiModelProperty(notes = "Indica se o usuário por concluir processos em massa.")
	private Boolean permiteConclusaoEmMassa;

	@ApiModelProperty(notes = "Quantidade de processos retornados no filtro.")
	private Long quantidadeDeProcessos;

	public boolean isPermiteFiltrar() {
		return permiteFiltrar;
	}

	public void setPermiteFiltrar(boolean permiteFiltrar) {
		this.permiteFiltrar = permiteFiltrar;
	}

	public boolean isPermiteTrocarAnalista() {
		return permiteTrocarAnalista;
	}

	public void setPermiteTrocarAnalista(boolean permiteTrocarAnalista) {
		this.permiteTrocarAnalista = permiteTrocarAnalista;
	}

	public boolean isPermiteFiltrarAnalista() {
		return permiteFiltrarAnalista;
	}

	public void setPermiteFiltrarAnalista(boolean permiteFiltrarAnalista) {
		this.permiteFiltrarAnalista = permiteFiltrarAnalista;
	}

	public boolean isPermiteVerificarProxima() {
		return permiteVerificarProxima;
	}

	public void setPermiteVerificarProxima(boolean permiteVerificarProxima) {
		this.permiteVerificarProxima = permiteVerificarProxima;
	}

	public List<ContadorStatusProcesso> getContadorStatusProcesso() {
		return contadorStatusProcesso;
	}

	public void setContadorStatusProcesso(List<ContadorStatusProcesso> contadorStatusProcesso) {
		this.contadorStatusProcesso = contadorStatusProcesso;
	}

	public List<FilaTrabalhoVoResponse> getFilaTrabalhoResponse() {
		return filaTrabalhoResponse;
	}

	public void setFilaTrabalhoResponse(List<FilaTrabalhoVoResponse> filaTrabalhoResponse) {
		this.filaTrabalhoResponse = filaTrabalhoResponse;
	}

	public Boolean getPermiteConclusaoEmMassa() {
		return permiteConclusaoEmMassa;
	}

	public void setPermiteConclusaoEmMassa(Boolean permiteConclusaoEmMassa) {
		this.permiteConclusaoEmMassa = permiteConclusaoEmMassa;
	}

	public boolean isPermiteCriar() {
		return permiteCriar;
	}

	public void setPermiteCriar(boolean permiteCriar) {
		this.permiteCriar = permiteCriar;
	}

	public Long getQuantidadeDeProcessos() {
		return quantidadeDeProcessos;
	}

	public void setQuantidadeDeProcessos(Long quantidadeDeProcessos) {
		this.quantidadeDeProcessos = quantidadeDeProcessos;
	}

	public void addContadorStatusProcesso(ContadorStatusProcesso contadorStatusProcesso) {
		if (this.contadorStatusProcesso == null) {
			this.contadorStatusProcesso = new ArrayList<>();
		}
		this.contadorStatusProcesso.add(contadorStatusProcesso);
	}


}