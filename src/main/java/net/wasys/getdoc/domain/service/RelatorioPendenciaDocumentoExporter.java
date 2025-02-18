package net.wasys.getdoc.domain.service;

import com.sun.faces.util.CollectionsUtils;

import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.TipoExtensaoRelatorio;
import net.wasys.getdoc.domain.vo.RelatorioPendenciaDocumentoVO;
import net.wasys.getdoc.domain.vo.filtro.RelatorioPendenciaDocumentoFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.AbstractProcessor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
@Scope("prototype")
public class RelatorioPendenciaDocumentoExporter extends AbstractProcessor {

	@Autowired private RelatorioPendenciaDocumentoService relatorioPendenciaDocumentoService;
	@Autowired private LogAcessoService logAcessoService;

	private Map<String, File> mapFile = new HashMap<>();;
	private TipoExtensaoRelatorio extensao;
	private RelatorioPendenciaDocumentoFiltro filtro;
	private Usuario usuario;

	@Override
	protected void execute2() {

		long inicio = System.currentTimeMillis();
		systraceThread("inicio");

		LogAcesso logAcesso = null;

		try {

			logAcesso = logAcessoService.criaLogProcessor(inicio, getLogAcessoProcessor(), filtro, usuario);

			List<RelatorioPendenciaDocumentoVO> relatorioPendenciaDocumentoVOS = new ArrayList<>();
			if(Arrays.asList(TipoExtensaoRelatorio.TODOS, TipoExtensaoRelatorio.XLSX).contains(extensao)) {
				File file = relatorioPendenciaDocumentoService.render(filtro, relatorioPendenciaDocumentoVOS, RelatorioPendenciaDocumentoFiltro.EXCEL);
				String fileName = "relatorio-pendencias-documentos-alunos.xlsx";
				mapFile.put(fileName, file);
			}

			if(Arrays.asList(TipoExtensaoRelatorio.TODOS, TipoExtensaoRelatorio.CSV).contains(extensao)) {
				File file = relatorioPendenciaDocumentoService.render(filtro, relatorioPendenciaDocumentoVOS, RelatorioPendenciaDocumentoFiltro.CSV);
				String fileName = "relatorio-pendencias-documentos-alunos.csv";
				mapFile.put(fileName, file);
			}

			DummyUtils.systraceThread("fim");
		}
		catch (Exception e) {
			handleException(logAcesso, e);
		}
		finally {
			doFinally(inicio, logAcesso);
		}
	}

	public void setFiltro(RelatorioPendenciaDocumentoFiltro filtro) {
		this.filtro = filtro;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public Map<String, File> getMapFile() {
		return mapFile;
	}

	public void setExtensao(TipoExtensaoRelatorio extensao) {
		this.extensao = extensao;
	}
}
