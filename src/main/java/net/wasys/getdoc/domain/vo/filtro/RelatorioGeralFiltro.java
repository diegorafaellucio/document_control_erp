package net.wasys.getdoc.domain.vo.filtro;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.wasys.getdoc.domain.entity.Etapa;
import net.wasys.getdoc.domain.entity.Situacao;
import net.wasys.getdoc.domain.entity.TipoCampo;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.vo.CampoDinamicoVO;
import org.primefaces.model.SortOrder;

public class RelatorioGeralFiltro extends CamposFiltro {

	public enum ConsiderarData {
		CRIACAO,
		ENVIO_ANALISE,
		ATUALIZACAO
	}

	public enum Tipo {
		PROCESSOS("Processos"),
		SOLICITACOES("Solicitacoes"),
		SITUACOES("Situacoes"),
		ETAPAS("Etapas"),
		ISENCAO_DISCIPLINAS("Isenção de Disciplinas");

		private String planilha;

		Tipo(String planilha) {
			this.planilha = planilha;
		}

		public String getPlanilha() {
			return planilha;
		}
	}

	public enum TipoArquivo {
		XLSX, CSV
	}

	private Date dataInicio;
	private Date dataFim;
	private String campoOrdem;
	private SortOrder ordem;
	private Tipo tipo;
	private List<TipoProcesso> tiposProcessoList;
	private List<StatusProcesso> statusList;
	private ConsiderarData considerarData;
	private List<Situacao> situacoes;
	private List<Long> situacoesIds;
	private List<Long> tiposProcessoIds;
	private Etapa etapa;
	private Long relatorioGeralId;
	private TipoArquivo tipoArquivo;
	private Boolean dataFimEtapaVazio;
	private List<CampoDinamicoVO> camposFiltro;
	private List<TipoProcesso> tiposProcesso;
	private boolean campoFiltro;

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

	public void setOrdenar(String campoOrdem, SortOrder ordem) {
		this.campoOrdem = campoOrdem;
		this.ordem = ordem;
	}

	public String getCampoOrdem() {
		return campoOrdem;
	}

	public SortOrder getOrdem() {
		return ordem;
	}

	public Tipo getTipo() {
		return tipo;
	}

	public void setTipo(Tipo tipo) {
		this.tipo = tipo;
	}

	public List<TipoProcesso> getTiposProcessoList() {
		return tiposProcessoList;
	}

	public void setTiposProcessoList(List<TipoProcesso> tiposProcessoList) {
		this.tiposProcessoList = tiposProcessoList;
	}

	public List<StatusProcesso> getStatusList() {
		return statusList;
	}

	public void setStatusList(List<StatusProcesso> statusList) {
		this.statusList = statusList;
	}

	public ConsiderarData getConsiderarData() {
		return considerarData;
	}

	public void setConsiderarData(ConsiderarData considerarData) {
		this.considerarData = considerarData;
	}

	public List<Situacao> getSituacoes() {
		return situacoes;
	}

	public void setSituacoes(List<Situacao> situacoes) {
		this.situacoes = situacoes;
	}

	public List<Long> getSituacoesIds() {
		return situacoesIds;
	}

	public void setSituacoesIds(List<Long> situacoesIds) {
		this.situacoesIds = situacoesIds;
	}

	public List<Long> getTiposProcessoIds() {
		return tiposProcessoIds;
	}

	public void setTiposProcessoIds(List<Long> tiposProcessoIds) {
		this.tiposProcessoIds = tiposProcessoIds;
	}

	public Etapa getEtapa() {
		return etapa;
	}

	public void setEtapa(Etapa etapa) {
		this.etapa = etapa;
	}

	public Long getRelatorioGeralId() {
		return relatorioGeralId;
	}

	public void setRelatorioGeralId(Long relatorioGeralId) {
		this.relatorioGeralId = relatorioGeralId;
	}

	public TipoArquivo getTipoArquivo() {
		return tipoArquivo;
	}

	public void setTipoArquivo(TipoArquivo tipoArquivo) {
		this.tipoArquivo = tipoArquivo;
	}

	public Boolean getDataFimEtapaVazio() {
		return dataFimEtapaVazio;
	}

	public void setDataFimEtapaVazio(Boolean dataFimEtapaVazio) {
		this.dataFimEtapaVazio = dataFimEtapaVazio;
	}

	public boolean isCampoFiltro() {
		return campoFiltro;
	}

	public void setCampoFiltro(Boolean campoFiltro) {
		this.campoFiltro = campoFiltro;
	}

	@Override public String toString() {
		return "RelatorioGeralFiltro{" +
				"dataInicio=" + dataInicio +
				", dataFim=" + dataFim +
				", campoOrdem='" + campoOrdem + '\'' +
				", ordem=" + ordem +
				", tipo=" + tipo +
				", tiposProcessoList=" + tiposProcessoList +
				", statusList=" + statusList +
				", considerarData=" + considerarData +
				", situacoes=" + situacoes +
				", situacoesIds=" + situacoesIds +
				", tiposProcessoIds=" + tiposProcessoIds +
				", etapa=" + etapa +
				", relatorioGeralId=" + relatorioGeralId +
				", tipoArquivo=" + tipoArquivo +
				", dataFimEtapaVazio=" + dataFimEtapaVazio +
				'}';
	}
}
