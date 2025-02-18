package net.wasys.getdoc.bean;

import net.wasys.getdoc.bean.datamodel.LogImportacaoDataModel;
import net.wasys.getdoc.domain.enumeration.TipoImportacao;
import net.wasys.getdoc.domain.service.LogImportacaoService;
import net.wasys.getdoc.domain.vo.filtro.LogImportacaoFiltro;
import net.wasys.util.faces.AbstractBean;
import org.springframework.beans.factory.annotation.Autowired;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

@ManagedBean
@ViewScoped
public class LogImportacaoListBean extends AbstractBean {

	@Autowired private LogImportacaoService logImportacaoService;

	private LogImportacaoDataModel dataModel;
	private LogImportacaoFiltro filtro = new LogImportacaoFiltro();
	private List<String> tiposImportacao = new ArrayList<>(asList(TipoImportacao.BASE_INTERNA.name()));

	protected void initBean() {

		dataModel = new LogImportacaoDataModel();
		dataModel.setFiltro(filtro);
		dataModel.setService(logImportacaoService);
	}

	public LogImportacaoDataModel getDataModel() {
		return dataModel;
	}

	public LogImportacaoFiltro getFiltro() {
		return filtro;
	}

	public void setFiltro(LogImportacaoFiltro filtro) {
		this.filtro = filtro;
	}

	public List<String> getTiposImportacao() {
		return tiposImportacao;
	}
}
