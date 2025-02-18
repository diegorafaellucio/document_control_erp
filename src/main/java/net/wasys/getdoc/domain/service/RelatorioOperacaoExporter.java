package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.vo.filtro.RelatorioOperacaoFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.AbstractProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
@Scope("prototype")
public class RelatorioOperacaoExporter extends AbstractProcessor {

	@Autowired private RelatorioOperacaoService relatorioOperacaoService;
	@Autowired private LogAcessoService logAcessoService;

	private String fileName;
	private File file;

	private RelatorioOperacaoFiltro filtro;
	private Usuario usuario;

	@Override
	protected void execute2() {

		long inicio = System.currentTimeMillis();
		systraceThread("Iniciando exportacao " + DummyUtils.formatDateTime(new Date()));

		LogAcesso logAcesso = null;

		try {

			logAcesso = logAcessoService.criaLogProcessor(inicio, getLogAcessoProcessor(), filtro, usuario);

			file = relatorioOperacaoService.renderRelatorioOperacao(filtro);

			fileName = "relatorio-operacao.xlsx";
		}
		catch (Exception e) {
			handleException(logAcesso, e);
		}
		finally {
			doFinally(inicio, logAcesso);
		}
	}

	public String getFileName() {
		return fileName;
	}

	public File getFile() {
		return file;
	}

	public void setFiltro(RelatorioOperacaoFiltro filtro) {
		this.filtro = filtro;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}
}
