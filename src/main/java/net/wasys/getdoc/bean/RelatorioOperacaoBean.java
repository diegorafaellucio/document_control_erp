package net.wasys.getdoc.bean;

import lombok.extern.slf4j.Slf4j;

import net.wasys.getdoc.domain.entity.Subperfil;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.service.RelatorioOperacaoExporter;
import net.wasys.getdoc.domain.service.RelatorioOperacaoService;
import net.wasys.getdoc.domain.service.SubperfilService;
import net.wasys.getdoc.domain.service.TipoProcessoService;
import net.wasys.getdoc.domain.vo.RelatorioOperacaoVO;
import net.wasys.getdoc.domain.vo.filtro.RelatorioOperacaoFiltro;
import net.wasys.getdoc.mb.utils.DateUtils;
import net.wasys.util.faces.AbstractBean;
import org.omnifaces.util.Ajax;
import org.omnifaces.util.Faces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ManagedBean
@ViewScoped
public class RelatorioOperacaoBean extends AbstractBean {

	@Autowired private ApplicationContext applicationContext;
	@Autowired private RelatorioOperacaoService relatorioOperacaoService;
	@Autowired private TipoProcessoService tipoProcessoService;
	@Autowired private SubperfilService subperfilService;

	private RelatorioOperacaoFiltro filtro = new RelatorioOperacaoFiltro();
	private List<TipoProcesso> tiposProcesso;
	private List<Subperfil> subPerfils;
	private RelatorioOperacaoExporter relatorioOperacaoExporter;
	private List<RelatorioOperacaoVO> relatorioOperacaoVOS = new ArrayList<>();

	protected void initBean() {
		Date dataInicio = new Date();
		Date dataFim = new Date();
		dataInicio = DateUtils.getFirstTimeOfDay(dataInicio);
		dataFim = DateUtils.getLastTimeOfDay(dataFim);
		filtro.setDataInicio(dataInicio);
		filtro.setDataFim(dataFim);
		tiposProcesso = tipoProcessoService.findAtivos(null);
		subPerfils = subperfilService.findAll();
	}

	public void buscar() {
		relatorioOperacaoVOS = relatorioOperacaoService.findRelatorioOperacao(filtro);
	}

	public void baixarRelatorioOperacao() {

		try {
			Exception exception = relatorioOperacaoExporter.getError();
			if(exception != null) {
				throw exception;
			}

			File file = relatorioOperacaoExporter.getFile();
			String fileName = relatorioOperacaoExporter.getFileName();

			FileInputStream fis = new FileInputStream(file);
			Faces.sendFile(fis, fileName, false);
		}
		catch (Exception e) {
			e.printStackTrace();
			addMessageError(e);
		}

		relatorioOperacaoExporter = null;
	}

	public void verificarRelatorioOperacao() {
		if(relatorioOperacaoExporter == null) {
			return;
		}
		boolean finalizado = relatorioOperacaoExporter.isFinalizado();
		Ajax.data("terminou", finalizado);
	}

	public void exportarRelatorioOperacao() {
		if(relatorioOperacaoExporter == null) {
			relatorioOperacaoExporter = applicationContext.getBean(RelatorioOperacaoExporter.class);
			relatorioOperacaoExporter.setFiltro(filtro);
			relatorioOperacaoExporter.start();
		}
	}

	public RelatorioOperacaoFiltro getFiltro() {
		return filtro;
	}

	public void setFiltro(RelatorioOperacaoFiltro filtro) {
		this.filtro = filtro;
	}

	public List<TipoProcesso> getTiposProcesso() {
		return tiposProcesso;
	}

	public List<Subperfil> getSubPerfils() {
		return subPerfils;
	}

	public List<RelatorioOperacaoVO> getRelatorioOperacaoVOS() {
		return relatorioOperacaoVOS;
	}

	public void setRelatorioOperacaoVOS(List<RelatorioOperacaoVO> relatorioOperacaoVOS) {
		this.relatorioOperacaoVOS = relatorioOperacaoVOS;
	}

}
