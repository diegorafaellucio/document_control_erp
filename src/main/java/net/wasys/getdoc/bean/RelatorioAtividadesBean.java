package net.wasys.getdoc.bean;

import lombok.extern.slf4j.Slf4j;

import net.wasys.getdoc.bean.datamodel.ProcessoLogDataModel;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.domain.enumeration.StatusUsuario;
import net.wasys.getdoc.domain.service.ProcessoLogService;
import net.wasys.getdoc.domain.service.RelatorioAtividadesExporter;
import net.wasys.getdoc.domain.service.RelatorioAtividadesSinteticoService;
import net.wasys.getdoc.domain.service.UsuarioService;
import net.wasys.getdoc.domain.vo.RelatorioAtividadesSinteticoVO;
import net.wasys.getdoc.domain.vo.filtro.RelatorioAtividadeFiltro;
import net.wasys.getdoc.domain.vo.filtro.UsuarioFiltro;
import net.wasys.util.faces.AbstractBean;
import org.omnifaces.util.Ajax;
import org.omnifaces.util.Faces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

@ManagedBean
@ViewScoped
public class RelatorioAtividadesBean extends AbstractBean {

	@Autowired private ProcessoLogService processoLogService;
	@Autowired private ApplicationContext applicationContext;
	@Autowired private UsuarioService usuarioService;
	@Autowired private RelatorioAtividadesSinteticoService relatorioAtividadesSinteticoService;

	private ProcessoLogDataModel dataModel;
	private RelatorioAtividadesSinteticoVO sinteticoVO;
	private RelatorioAtividadesExporter exporter;

	private List<Usuario> analistas;

	private RelatorioAtividadeFiltro filtro = new RelatorioAtividadeFiltro();

	protected void initBean() {

		if (dataModel == null) {

			Date date = new Date();
			filtro.setDataInicio(date);
			filtro.setDataFim(date);
			filtro.setTipo(RelatorioAtividadeFiltro.Tipo.SINTETICO);

			dataModel = new ProcessoLogDataModel();
			dataModel.setFiltro(filtro);
			dataModel.setService(processoLogService);

			UsuarioFiltro f = new UsuarioFiltro();
			f.setRole(RoleGD.GD_ANALISTA);
			f.setStatus(StatusUsuario.ATIVO);
			analistas = usuarioService.findByFiltroToSelect(f);
		}
	}

	public void buscar() {

		RelatorioAtividadeFiltro.Tipo tipo = filtro.getTipo();
		if(RelatorioAtividadeFiltro.Tipo.SINTETICO.equals(tipo)) {
			sinteticoVO = relatorioAtividadesSinteticoService.getRelatorioSintetico(filtro);
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
			exporter = applicationContext.getBean(RelatorioAtividadesExporter.class);
			exporter.setFiltro(filtro);
			exporter.setUsuario(getUsuarioLogado());
			exporter.start();
		}
	}

	public List<AcaoProcesso> getAcoesProcesso() {
		AcaoProcesso[] values = AcaoProcesso.values();
		List<AcaoProcesso> list = Arrays.asList(values);
		Collections.sort(list, Comparator.comparing(Enum::name));
		return list;
	}

	public RelatorioAtividadesExporter getExporter() {
		return exporter;
	}

	public ProcessoLogDataModel getDataModel() {
		return dataModel;
	}

	public RelatorioAtividadeFiltro getFiltro() {
		return filtro;
	}

	public List<Usuario> getAnalistas() {
		return analistas;
	}

	public RelatorioAtividadesSinteticoVO getSinteticoVO() {
		return sinteticoVO;
	}
}
