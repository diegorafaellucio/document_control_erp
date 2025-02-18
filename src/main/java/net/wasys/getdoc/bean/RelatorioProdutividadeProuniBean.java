package net.wasys.getdoc.bean;

import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.vo.RelatorioProdutividadeProuniVO;
import net.wasys.getdoc.domain.vo.filtro.RelatorioProdutividadeProuniFiltro;
import net.wasys.util.faces.AbstractBean;
import org.apache.commons.lang.StringUtils;
import org.omnifaces.util.Ajax;
import org.omnifaces.util.Faces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

@ManagedBean
@SessionScoped
public class RelatorioProdutividadeProuniBean extends AbstractBean {

	@Autowired private ApplicationContext applicationContext;
	@Autowired private RelatorioProdutividadeProuniService relatorioProdutividadeProuniService;

	private RelatorioProdutividadeProuniExporter exporter;
	private RelatorioProdutividadeProuniFiltro filtro = new RelatorioProdutividadeProuniFiltro();

	private List<RelatorioProdutividadeProuniVO> voList = new ArrayList<>();

	protected void initBean() {
		Usuario usuario = getUsuarioLogado();
	}

	public void buscar() {
		voList = relatorioProdutividadeProuniService.getRelatorioProdutividadeProuni(filtro);
	}

	public void baixar() {

		Exception error = exporter.getError();
		if(error != null) {
			addMessageError(error);
		}
		else {
			File file = exporter.getFile();
			try {
				FileInputStream fis = new FileInputStream(file);
				String fileName = "relatorio-produtividade-prouni.xlsx";
				String fileName2 = exporter.getFileName();
				if(StringUtils.isNotBlank(fileName2)) {
					fileName = fileName2;
				}
				Faces.sendFile(fis, fileName, true);
			}
			catch (Exception e1) {
				addMessageError(e1);
			}
		}

		exporter = null;
	}

	public void verificar() {

		if(exporter == null) {
			return;
		}

		if(exporter.isFinalizado()) {
			Ajax.data("terminou", true);
		}
		else {
			Ajax.data("terminou", false);
		}
	}

	public void exportar() {

		if(exporter == null) {
			exporter = applicationContext.getBean(RelatorioProdutividadeProuniExporter.class);
			exporter.setFiltro(filtro);
			exporter.setUsuario(getUsuarioLogado());
			exporter.start();
		}
	}

	public RelatorioProdutividadeProuniFiltro getFiltro() {
		return filtro;
	}

	public void setFiltro(RelatorioProdutividadeProuniFiltro filtro) {
		this.filtro = filtro;
	}

	public RelatorioProdutividadeProuniExporter getExporter() {
		return exporter;
	}

	public List<RelatorioProdutividadeProuniVO> getVoList() {
		return voList;
	}
}
