package net.wasys.getdoc.domain.vo.filtro;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import net.wasys.getdoc.domain.enumeration.AcaoProcesso;

public class AnexoFiltro {

	private Long processoId;
	private Date dataInicio;
	private Date dataFim;
	private String busca;
	private List<AcaoProcesso> acoesSelecionadas;
	private List<AcaoProcesso> acoesPesquisa = Arrays.asList(new AcaoProcesso[]{AcaoProcesso.ANEXO_PROCESSO,AcaoProcesso.ENVIO_EMAIL,AcaoProcesso.ENVIO_PENDENCIA,AcaoProcesso.SOLICITACAO_CRIACAO,AcaoProcesso.REGISTRO_EVIDENCIA});

	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	public Date getDataFim() {
		return dataFim;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}

	public Long getProcessoId() {
		return processoId;
	}

	public void setProcessoId(Long processoId) {
		this.processoId = processoId;
	}

	public List<AcaoProcesso> getAcoesSelecionadas() {
		return acoesSelecionadas;
	}

	public void setAcoesSelecionadas(List<AcaoProcesso> acoesSelecionadas) {
		this.acoesSelecionadas = acoesSelecionadas;
	}

	public List<AcaoProcesso> getAcoesPesquisa() {
		return acoesPesquisa;
	}

	public void setAcoesPesquisa(List<AcaoProcesso> acoesPesquisa) {
		this.acoesPesquisa = acoesPesquisa;
	}

	public String getBusca() {
		return busca;
	}

	public void setBusca(String busca) {
		this.busca = busca;
	}

}
