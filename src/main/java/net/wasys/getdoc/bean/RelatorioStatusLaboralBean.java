package net.wasys.getdoc.bean;

import net.wasys.getdoc.bean.datamodel.RelatorioStatusLaboralDataModel;
import net.wasys.getdoc.domain.entity.StatusLaboral;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.LogAtendimentoService;
import net.wasys.getdoc.domain.service.RelatorioStatusLaboralExporter;
import net.wasys.getdoc.domain.service.StatusLaboralService;
import net.wasys.getdoc.domain.service.UsuarioService;
import net.wasys.getdoc.domain.vo.RelatorioStatusLaboralSinteticoVO;
import net.wasys.getdoc.domain.vo.filtro.LogAtendimentoFiltro;
import net.wasys.util.faces.AbstractBean;
import org.omnifaces.util.Ajax;
import org.omnifaces.util.Faces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.List;

@ManagedBean
@ViewScoped
public class RelatorioStatusLaboralBean extends AbstractBean {

	@Autowired private ApplicationContext applicationContext;
	@Autowired private UsuarioService usuarioService;
	@Autowired private LogAtendimentoService logAtendimentoService;
	@Autowired private StatusLaboralService statusLaboralService;

	private RelatorioStatusLaboralDataModel dataModel;
	private RelatorioStatusLaboralExporter exporter;

	private List<Usuario> analistas;
	private List<StatusLaboral> statusLaboral;

	private LogAtendimentoFiltro filtro = new LogAtendimentoFiltro();
	private RelatorioStatusLaboralSinteticoVO sinteticoVO;
	private Boolean reloadAutomatico = false;

	protected void initBean() {

		if (dataModel == null) {

			Date date = new Date();
			filtro.setInicio(date);
			filtro.setFim(date);
			filtro.setTipo(LogAtendimentoFiltro.Tipo.SINTETICO);

			dataModel = new RelatorioStatusLaboralDataModel();
			dataModel.setFiltro(filtro);
			dataModel.setService(logAtendimentoService);

			analistas = usuarioService.findAnalistasAtivos();
			statusLaboral = statusLaboralService.findAtivas();
		}
	}

	public void buscar() {
		LogAtendimentoFiltro.Tipo tipo = filtro.getTipo();
		if(LogAtendimentoFiltro.Tipo.SINTETICO.equals(tipo)) {
			sinteticoVO = logAtendimentoService.getRelatorioSintetico(filtro);
		}
	}

	public void baixar() {

		try {
			Exception exception = exporter.getError();
			if(exception != null) {
				throw exception;
			}

			File file = exporter.getFile();
			String fileName = exporter.getFileName();

			FileInputStream fis = new FileInputStream(file);
			Faces.sendFile(fis, fileName, false);
		}
		catch (Exception e) {
			e.printStackTrace();
			addMessageError(e);
		}

		exporter = null;
	}

	public void verificar() {
		if(exporter == null) {
			return;
		}
		boolean finalizado = exporter.isFinalizado();
		Ajax.data("terminou", finalizado);
	}

	public void exportar() {
		if(exporter == null) {
			exporter = applicationContext.getBean(RelatorioStatusLaboralExporter.class);
			exporter.setFiltro(filtro);
			exporter.setUsuario(getUsuarioLogado());
			exporter.start();
		}
	}

	public void reloadAutomatico(){
		if (reloadAutomatico){
			Ajax.oncomplete("stopReloadAutomatico()");
			setReloadAutomatico(false);
		}
		else {
			Ajax.oncomplete("startReloadAutomatico()");
			setReloadAutomatico(true);
		}
	}

	public Boolean getReloadAutomatico() {
		return reloadAutomatico;
	}

	public void setReloadAutomatico(Boolean reloadAutomatico) {
		this.reloadAutomatico = reloadAutomatico;
	}

	public RelatorioStatusLaboralExporter getExporter() {
		return exporter;
	}

	public RelatorioStatusLaboralDataModel getDataModel() {
		return dataModel;
	}

	public LogAtendimentoFiltro getFiltro() {
		return filtro;
	}

	public List<Usuario> getAnalistas() {
		return analistas;
	}

	public List<StatusLaboral> getStatusLaboral() {
		return statusLaboral;
	}

	public RelatorioStatusLaboralSinteticoVO getSinteticoVO() {
		return sinteticoVO;
	}
}
