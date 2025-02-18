package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;

import com.google.common.base.Stopwatch;
import net.wasys.getdoc.domain.entity.ConfiguracaoGeracaoRelatorio;
import net.wasys.getdoc.domain.entity.ExecucaoGeracaoRelatorio;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.TipoExtensaoRelatorio;
import net.wasys.getdoc.domain.vo.filtro.RelatorioPendenciaDocumentoFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.other.RepeatTry;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static net.wasys.util.DummyUtils.*;
import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
public class GerarRelatorioDocumentosPendentesService {

	@Autowired private ApplicationContext applicationContext;
	@Autowired private EmailSmtpService emailSmtpService;
	@Autowired private ExecucaoGeracaoRelatorioService execucaoGeracaoRelatorioService;
	@Autowired private ResourceService resourceService;

	public void gerar(ConfiguracaoGeracaoRelatorio config, Usuario usuario, boolean execucaoManual) {

		systraceThread("Processamento iniciado com a configuracao=" + config);

		ExecucaoGeracaoRelatorio execucao = criarExecucaoGeracaoRelatorio(config);

		List<ExecucaoGeracaoRelatorio> execucoes = new ArrayList<>();
		try {

			RepeatTry<RelatorioPendenciaDocumentoExporter> rt = new RepeatTry<>(3, TimeUnit.MINUTES.toMillis(5));
			rt.setToTry(() -> {

				RelatorioPendenciaDocumentoExporter result = executarExporter(config, usuario);
				Map<String, File> mapFile = result.getMapFile();
				boolean hasNoFiles = mapFile == null || mapFile.isEmpty();
				if (result.getError() != null || result.getException() != null || hasNoFiles) {

					Exception error = result.getError();
					if (error != null) {
						systraceThread("Ocorreu algum erro inesperado ao gerar o relatório de pendência do aluno. Config=" + config);
						throw error;
					}

					Exception exception = result.getException();
					if (exception != null) {
						systraceThread("Ocorreu algum erro inesperado ao gerar o relatório de pendência do aluno. Config=" + config);
						throw exception;
					}

					if (hasNoFiles) {
						throw new RuntimeException("Ocorreu algum erro inesperado ao gerar o relatório de pendência do aluno. O arquivo não foi gerado. Config=" + config);
					}
				}

				return result;
			});

			RelatorioPendenciaDocumentoExporter exporter = rt.execute();

			Exception error = exporter.getError();
			Map<String, File> mapFile = exporter.getMapFile();
			boolean hasNoFiles = mapFile == null || mapFile.isEmpty();

			if (error != null) {

				enviarEmailErro(config);
				execucao.setObservacao("Erro inesperado.");

				enviarEmailAlertaErroInesperado(config, exporter);
			}
			else if (hasNoFiles) {

				enviarEmailErro(config);
				execucao.setObservacao("Erro inesperado. Arquivo não foi gerado.");

				enviarEmailAlertaArquivoNaoGerado(config);
			}
			else {
				execucoes = tratarSucesso(config, execucao, exporter);
			}
		}
		catch (Exception e) {

			execucao.setSucesso(false);

			e.printStackTrace();
			execucao.setObservacao("Erro inesperado.");
			execucoes.add(execucao);
		}
		finally {

			if (execucao.getSucesso() == null) {
				execucao.setSucesso(false);
			}

			if (execucaoManual) {
				atualizarObservacaoExecucaoManual(usuario, execucao);
			}

			for (ExecucaoGeracaoRelatorio execucaoAux : execucoes) {
				execucaoAux.setFim(new Date());
				execucaoGeracaoRelatorioService.saveOrUpdate(execucaoAux);
			}
		}

		systraceThread("Processamento finalizado.");
	}

	private List<ExecucaoGeracaoRelatorio> tratarSucesso(ConfiguracaoGeracaoRelatorio config, ExecucaoGeracaoRelatorio execucao, RelatorioPendenciaDocumentoExporter exporter) throws IOException {

		List<ExecucaoGeracaoRelatorio> execucoes = new ArrayList<>();

		Map<String, File> mapFile = exporter.getMapFile();
		String firstKey = mapFile.keySet().stream().findFirst().get();
		Set<String> keySet = mapFile.keySet();
		for (String fileName : keySet) {
			String extensao = getExtensao(fileName);

			String configNome = config.getNome();
			String nomeArquivo = configNome + "." + extensao;

			File fileExporter = mapFile.get(fileName);
			String diretorioFinal = getDiretorioFinal(config, nomeArquivo);
			File arquivoFinal = new File(diretorioFinal);
			FileUtils.copyFile(fileExporter, arquivoFinal);

			systraceThread("Arquivo gerado=" + diretorioFinal);

			ExecucaoGeracaoRelatorio clone = fileName.equals(firstKey) ? execucao : execucao.clone();
			clone.setSucesso(true);
			clone.setCaminhoArquivo(diretorioFinal);
			clone.setExtensao(TipoExtensaoRelatorio.valueOf(extensao.toUpperCase()));

			execucoes.add(clone);
		}

		try {
			enviarEmailSucesso(config, mapFile);
		}
		catch (Exception e) {

			e.printStackTrace();

			execucao.setObservacao("Erro inesperado. Não foi possível enviar os e-mails.");
			execucao.setSucesso(false);
		}

		return execucoes;
	}

	private void atualizarObservacaoExecucaoManual(Usuario usuario, ExecucaoGeracaoRelatorio execucao) {

		String msgExecucaoManual = "Execução manual feita pelo usuário \"" + usuario.getLogin() + "\"";

		String observacao = execucao.getObservacao();
		if (isBlank(observacao)) {
			execucao.setObservacao(msgExecucaoManual);
		}
		else {
			String observacaoAtual = execucao.getObservacao();
			observacaoAtual += "\n" + msgExecucaoManual;
			execucao.setObservacao(observacaoAtual);
		}
	}

	private void enviarEmailAlertaArquivoNaoGerado(ConfiguracaoGeracaoRelatorio config) {

		try {
			emailSmtpService.enviarEmailException("Erro ao gerar relatório. Arquivo não foi gerado", "", "Config=" + config);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void enviarEmailAlertaErroInesperado(ConfiguracaoGeracaoRelatorio config, RelatorioPendenciaDocumentoExporter exporter) {

		try {
			Exception e = exporter.getException();
			if (e != null) {

				e.printStackTrace();
				String message = getExceptionMessage(e);
				emailSmtpService.enviarEmailException(message, e);
			}
			else {
				emailSmtpService.enviarEmailException("Erro ao gerar relatório.", "", "Config=" + config);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private ExecucaoGeracaoRelatorio criarExecucaoGeracaoRelatorio(ConfiguracaoGeracaoRelatorio config) {

		TipoExtensaoRelatorio extensao = config.getExtensao();

		ExecucaoGeracaoRelatorio execucao = new ExecucaoGeracaoRelatorio();
		execucao.setConfiguracaoGeracaoRelatorio(config);
		execucao.setExtensao(extensao);
		execucao.setInicio(new Date());

		execucaoGeracaoRelatorioService.saveOrUpdate(execucao);

		return execucao;
	}

	private RelatorioPendenciaDocumentoExporter executarExporter(ConfiguracaoGeracaoRelatorio config, Usuario usuario) {

		boolean dehMenosUm = config.isDehMenosUm();
		String opcoes = config.getOpcoes();
		RelatorioPendenciaDocumentoFiltro filtro = jsonToObject(opcoes, RelatorioPendenciaDocumentoFiltro.class);
		filtro.setDehMenosUm(dehMenosUm);
		TipoExtensaoRelatorio extensao = config.getExtensao();

		RelatorioPendenciaDocumentoExporter exporter = applicationContext.getBean(RelatorioPendenciaDocumentoExporter.class);
		exporter.setExtensao(extensao);
		exporter.setUsuario(usuario);
		exporter.setFiltro(filtro);
		exporter.start();
		Stopwatch stopwatch = Stopwatch.createStarted();

		systraceThread("Exporter iniciado.");

		do {
			sleep(10000);
			systraceThread("Aguardando finalizacao do exporter. Tempo=" + stopwatch.elapsed(TimeUnit.SECONDS) + "s.");
		} while (!exporter.isFinalizado());

		return exporter;
	}

	private void enviarEmailSucesso(ConfiguracaoGeracaoRelatorio config, Map<String, File> files) {

		String[] emails = config.getEmailsEnvioSucessoList();

		if (isNotEmpty(emails)) {
			enviarEmailSucesso(config.getNome(), emails, files);
		}
		else {
			systraceThread("Nenhum destinatário configurado.");
		}
	}

	private String getDiretorioFinal(ConfiguracaoGeracaoRelatorio config, String nomeArquivo) {

		String diretorio = resourceService.getValue(ResourceService.RELATORIO_PENDENCIA_DOCUMENTO_PATH);

		String date = formatDate(new Date());
		date = date.replace("/", "-");

		String horario = config.getHorario();
		horario = horario.replace(":", "");

		return diretorio + date + "-" + horario + "-" + nomeArquivo;
	}

	private void enviarEmailErro(ConfiguracaoGeracaoRelatorio config) {

		String[] emails = config.getEmailsEnvioErroList();

		if (isNotEmpty(emails)) {
			enviarEmailErro(config.getNome(), emails);
		}
	}

	private void enviarEmailSucesso(String nomeRelatorio, String[] destinatarios, Map<String, File> files) {

		Map<String, File> anexos = new HashMap<>();

		int sizeFiles = 0;
		Set<String> keySet = files.keySet();
		for (String fileName : keySet) {
			File file = files.get(fileName);
			sizeFiles += file.length();
		}

		boolean isArquivoMuitoGrande = sizeFiles > 10485760; //10MB
		if (!isArquivoMuitoGrande) {
			anexos.putAll(files);
		}

		String urlRelatorio = "relatorios/pasta-vermelha/";
		emailSmtpService.enviarNotificaoRelatorioGeradoSucesso(nomeRelatorio, !isArquivoMuitoGrande, urlRelatorio, destinatarios, anexos);
	}

	private void enviarEmailErro(String nomeRelatorio, String[] destinatarios) {

		systraceThread("Enviando e-mail de erro para os destinatarios=" + Arrays.toString(destinatarios));

		String urlRelatorio = "relatorios/pendencia/documento-alunos/";
		emailSmtpService.enviarNotificaoRelatorioGeradoErro(nomeRelatorio, urlRelatorio, destinatarios);
	}
}
