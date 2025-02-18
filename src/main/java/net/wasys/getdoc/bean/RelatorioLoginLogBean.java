package net.wasys.getdoc.bean;

import lombok.extern.slf4j.Slf4j;

import net.wasys.getdoc.bean.datamodel.RelatorioLoginLogDataModel;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.domain.service.LoginLogService;
import net.wasys.getdoc.domain.service.RelatorioLoginLogExporter;
import net.wasys.getdoc.domain.service.UsuarioService;
import net.wasys.getdoc.domain.vo.filtro.LoginLogFiltro;
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
public class RelatorioLoginLogBean extends AbstractBean {

	@Autowired private ApplicationContext applicationContext;
	@Autowired private UsuarioService usuarioService;
	@Autowired private LoginLogService loginLogService;

	private RelatorioLoginLogDataModel dataModel;
	private RelatorioLoginLogExporter exporter;

	private List<Usuario> analistas;
	private LoginLogFiltro filtro = new LoginLogFiltro();

	protected void initBean() {

		if (dataModel == null) {
			Date date = new Date();
			filtro.setDataInicio(date);
			filtro.setDataFim(date);
			filtro.setRoleGD(RoleGD.GD_ANALISTA);

			dataModel = new RelatorioLoginLogDataModel();
			dataModel.setFiltro(filtro);
			dataModel.setService(loginLogService);
		}

		analistas = usuarioService.findAnalistasAtivos();
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
			exporter = applicationContext.getBean(RelatorioLoginLogExporter.class);
			exporter.setFiltro(filtro);
			exporter.setUsuario(getUsuarioLogado());
			exporter.start();
		}
	}

	public RelatorioLoginLogExporter getExporter() {
		return exporter;
	}

	public RelatorioLoginLogDataModel getDataModel() {
		return dataModel;
	}

	public LoginLogFiltro getFiltro() {
		return filtro;
	}

	public List<Usuario> getAnalistas() {
		return analistas;
	}
}
