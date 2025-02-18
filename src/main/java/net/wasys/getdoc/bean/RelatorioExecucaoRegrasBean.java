package net.wasys.getdoc.bean;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.springframework.beans.factory.annotation.Autowired;

import net.wasys.getdoc.bean.datamodel.RelatorioExecucaoRegrasDataModel;
import net.wasys.getdoc.domain.entity.ProcessoRegra;
import net.wasys.getdoc.domain.entity.ProcessoRegraLog;
import net.wasys.getdoc.domain.service.ProcessoRegraLogService;
import net.wasys.getdoc.domain.service.ProcessoRegraService;
import net.wasys.getdoc.domain.vo.filtro.ProcessoRegraFiltro;
import net.wasys.util.faces.AbstractBean;

@ManagedBean
@ViewScoped
public class RelatorioExecucaoRegrasBean extends AbstractBean {

	@Autowired private ProcessoRegraService processoRegraService;
	@Autowired private ProcessoRegraLogService processoRegraLogService;

	private ProcessoRegraFiltro filtro = new ProcessoRegraFiltro();
	private RelatorioExecucaoRegrasDataModel dataModel;

	private List<ProcessoRegraLog> listLogComplete;
	private List<ProcessoRegraLog> listLog;

	private List<String> regrasNomes;
	private ProcessoRegra regra;
	private Long regraId;
	private ProcessoRegraLog regraLog;

	protected void initBean() {

		dataModel = new RelatorioExecucaoRegrasDataModel();
		dataModel.setFiltro(filtro);
		dataModel.setService(processoRegraService);

		this.regrasNomes = processoRegraService.getDistinctRegrasNomes();

		if (regraId != null) {
			regra = processoRegraService.getById(regraId);
			filtro.setProcessoId(regra.getProcesso().getId());
			filtro.setRegraId(regraId);
			filtro.setRegraNome(regra.getRegra().getNome());
			buscar(true);
		}
	}

	public void buscar(boolean hasId) {
		if (hasId) {
			this.listLogComplete = processoRegraLogService.findByFiltro(filtro);
			this.listLog = processoRegraLogService.findByFiltro(filtro);
		}
		filtro.setAtivo(true);
	}

	public void limpar() {
		filtro = new ProcessoRegraFiltro();
		initBean();
	}

	public List<ProcessoRegraLog> getListLogComplete() {
		return listLogComplete;
	}

	public List<ProcessoRegraLog> getListLog() {
		return listLog;
	}

	public ProcessoRegraFiltro getFiltro() {
		return filtro;
	}

	public RelatorioExecucaoRegrasDataModel getDataModel() {
		return dataModel;
	}

	public List<String> getRegrasNomes() {
		return regrasNomes;
	}

	public ProcessoRegra getRegra() {
		return regra;
	}

	public void setRegra(ProcessoRegra regra) {
		this.regra = regra;
	}

	public Long getRegraId() {
		return regraId;
	}

	public void setRegraId(Long regraId) {
		this.regraId = regraId;
	}

	public ProcessoRegraLog getRegraLog() {
		return regraLog;
	}

	public void setRegraLog(ProcessoRegraLog regraLog) {
		this.regraLog = regraLog;
	}

	public boolean isFirst(Object item) {
		if(listLog == null) {
			return false;
		}
		ProcessoRegraLog logItem = (ProcessoRegraLog) item;
		ProcessoRegraLog previous = null;

		for (ProcessoRegraLog logAtual : listLog) {
			if (previous == null && logAtual.equals(logItem)) {
				return true;
			}
			previous = logAtual;
		}
		return false;
	}
}

