package net.wasys.getdoc.bean;

import lombok.extern.slf4j.Slf4j;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.PermissaoTP;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.domain.service.ModeloDocumentoService;
import net.wasys.getdoc.domain.service.RelatorioTipificacaoExporter;
import net.wasys.getdoc.domain.service.RelatorioTipificacaoService;
import net.wasys.getdoc.domain.service.TipoProcessoService;
import net.wasys.getdoc.domain.vo.filtro.ImagemMetaFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.faces.AbstractBean;
import org.omnifaces.util.Ajax;
import org.omnifaces.util.Faces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import static net.wasys.util.DummyUtils.systraceThread;

@ManagedBean
@SessionScoped
public class RelatorioTipificacaoBean extends AbstractBean {

	@Autowired private ModeloDocumentoService modeloDocumentoService;
	@Autowired private TipoProcessoService tipoProcessoService;
	@Autowired private ApplicationContext applicationContext;

	private RelatorioTipificacaoExporter exporter;
	private ImagemMetaFiltro filtro = new ImagemMetaFiltro();
	private Documento documentoSelecionado;
	private List<TipoProcesso> tipoProcessoList;
	private List<ModeloDocumento> modeloDocumentoList;

	protected void initBean() {

		Date date = new Date();
		filtro.setDataInicio(date);
		filtro.setDataFim(date);

		Usuario usuario = getUsuarioLogado();
		RoleGD roleGD = usuario.getRoleGD();
		List<PermissaoTP> permissoes = PermissaoTP.getPermissoes(roleGD);
		tipoProcessoList = tipoProcessoService.findAtivosAndInitialize(permissoes);

		modeloDocumentoList = modeloDocumentoService.findAtivos();
	}

	public void baixar() {

		try {
			Exception exception = exporter.getException();
			if (exception != null) {
				throw exception;
			}

			File file = exporter.getFile();
			String fileName = exporter.getFileName();

			FileInputStream fis = new FileInputStream(file);
			Faces.sendFile(fis, fileName, false);
		}
		catch (Exception e) {
			addMessageError(e);
		}

		exporter = null;
	}

	public void verificar() {

		Usuario usuario = getUsuarioLogado();
		String login = usuario.getLogin();

		if(exporter == null) {
			systraceThread("login: " + login + " null " + DummyUtils.getLogMemoria());
			return;
		}
		boolean finalizado = exporter.isFinalizado();
		Ajax.data("terminou", finalizado);
	}

	public void exportar() {
		if (exporter == null) {
			exporter = applicationContext.getBean(RelatorioTipificacaoExporter.class);
			exporter.setFiltro(filtro);
			exporter.setUsuario(getUsuarioLogado());
			exporter.start();
		}
	}

	public Documento getDocumentoSelecionado() {
		return documentoSelecionado;
	}

	public void setDocumentoSelecionado(Documento documentoSelecionado) {
		this.documentoSelecionado = documentoSelecionado;
	}

	public ImagemMetaFiltro getFiltro() { return filtro; }

	public void setFiltro(ImagemMetaFiltro filtro) { this.filtro = filtro; }


	public String getUrlVisualizacaoDocumentoSelecionado() {

		if(documentoSelecionado == null) {
			return "xxx";
		}

		Processo processo = documentoSelecionado.getProcesso();
		Long processoId = processo.getId();
		Long documentoId = documentoSelecionado.getId();
		return "/processos/processo-visualizar.xhtml?id=" + processoId + "&documentoId=" + documentoId;
	}

	public List<TipoProcesso> getTipoProcessoList() {
		return tipoProcessoList;
	}

	public List<ModeloDocumento> getModeloDocumentoList() {
		return modeloDocumentoList;
	}
}
