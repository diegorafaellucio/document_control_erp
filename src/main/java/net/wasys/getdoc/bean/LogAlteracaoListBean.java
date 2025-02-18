package net.wasys.getdoc.bean;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.springframework.beans.factory.annotation.Autowired;

import net.wasys.getdoc.bean.datamodel.LogAlteracaoDataModel;
import net.wasys.getdoc.domain.service.LogAlteracaoService;
import net.wasys.getdoc.domain.vo.filtro.LogAlteracaoFiltro;
import net.wasys.util.faces.AbstractBean;

@ManagedBean
@ViewScoped
public class LogAlteracaoListBean extends AbstractBean {

	@Autowired private LogAlteracaoService logAlteracaoService;

	private LogAlteracaoFiltro filtro = new LogAlteracaoFiltro();
	private LogAlteracaoDataModel dataModel;

	protected void initBean() {

		dataModel = new LogAlteracaoDataModel();
		dataModel.setFiltro(filtro);
		dataModel.setService(logAlteracaoService);
	}

	public LogAlteracaoDataModel getDataModel() {
		return dataModel;
	}

	public LogAlteracaoFiltro getFiltro() {
		return filtro;
	}
}
