package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;

import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.vo.filtro.RelatorioGeralFiltro;
import net.wasys.getdoc.domain.vo.filtro.RelatorioGeralFiltro.Tipo;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.AbstractProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
@Scope("prototype")
public class RelatorioGeralExporter extends AbstractProcessor {

	@Autowired private RelatorioGeralService relatorioGeralService;
	@Autowired private LogAcessoService logAcessoService;

	private String fileName;
	private File file;

	private RelatorioGeralFiltro filtro;
	private Usuario usuario;

	@Override
	protected void execute2() {

		long inicio = System.currentTimeMillis();
		systraceThread("Iniciando exportacao " + DummyUtils.formatDateTime(new Date()));

		LogAcesso logAcesso = null;

		try {

			logAcesso = logAcessoService.criaLogProcessor(inicio, getLogAcessoProcessor(), filtro, usuario);

			Tipo tipo = filtro.getTipo();
			systraceThread("tipo: " + tipo);

			if (Tipo.PROCESSOS.equals(tipo)) {
				fileName = "relatorio-geral.xlsx";
			}
			else if (Tipo.SOLICITACOES.equals(tipo)) {
				fileName = "relatorio-geral-solicitacoes.xlsx";
			}
			else if (Tipo.ISENCAO_DISCIPLINAS.equals(tipo)) {
				fileName = "relatorio-isencao-disciplinas.xlsx";
			}
			else if (Tipo.ETAPAS.equals(tipo)) {
				fileName = "relatorio-geral-etapas.csv";
				filtro.setTipoArquivo(RelatorioGeralFiltro.TipoArquivo.CSV);
			}

			file = relatorioGeralService.render(filtro);

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

	public void setFiltro(RelatorioGeralFiltro filtro) {
		this.filtro = filtro;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	@Override public String toString() {
		return "RelatorioGeralExporter{" +
				", fileName='" + fileName + '\'' +
				", file=" + file +
				", filtro=" + filtro +
				", usuario=" + usuario +
				'}';
	}
}
