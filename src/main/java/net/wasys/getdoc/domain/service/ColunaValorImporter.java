package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import net.wasys.getdoc.domain.service.factory.ProcessadorArquivoFactory;
import net.wasys.getdoc.domain.vo.LinhaVO;
import net.wasys.util.ddd.AbstractProcessor;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.ddd.MessageKeyListException;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
@Scope("prototype")
public class ColunaValorImporter extends AbstractProcessor {

	@Autowired private LogAcessoService logAcessoService;

	private File file;
	private ResultadoProcessamentoVO resultadoProcessamento;
	private List<String> colunasUnicidade;
	private Usuario usuario;

	@Override
	protected void execute2() {

		long inicio = System.currentTimeMillis();
		systraceThread("Iniciando importação " + DummyUtils.formatDateTime(new Date()));

		LogAcesso logAcesso = null;

		resultadoProcessamento = new ResultadoProcessamentoVO();
		try {

			logAcesso = logAcessoService.criaLogProcessor(inicio, getLogAcessoProcessor(), null, usuario);

			ProcessadorArquivo processador = ProcessadorArquivoFactory.getProcessador(file, colunasUnicidade);
			List<LinhaVO> linhas = processador.processar();

			systraceThread("Total de linhas carregadas: " + linhas.size());

			resultadoProcessamento.setLinhas(linhas);
		}
		catch (MessageKeyException | MessageKeyListException e) {
			resultadoProcessamento.setException(e);

			systraceThread("Processamento interrompido. Erro de validação: " + e.getMessage(), LogLevel.ERROR);
		}
		finally {
			doFinally(inicio, logAcesso);
		}
	}

	public void setColunasUnicidade(List<String> colunasUnicidade) {
		this.colunasUnicidade = colunasUnicidade;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public ResultadoProcessamentoVO getResultadoProcessamento() {
		return resultadoProcessamento;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public class ResultadoProcessamentoVO {

		private Exception exception;
		private List<LinhaVO> linhas = new ArrayList<>();

		public Exception getException() {
			return exception;
		}

		public void setException(Exception exception) {
			this.exception = exception;
		}

		public List<LinhaVO> getLinhas() {
			return linhas;
		}

		public void setLinhas(List<LinhaVO> linhas) {
			this.linhas = linhas;
		}
	}
}
