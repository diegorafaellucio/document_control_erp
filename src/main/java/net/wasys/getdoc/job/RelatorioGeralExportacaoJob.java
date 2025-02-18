package net.wasys.getdoc.job;

import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.service.LogAcessoService;
import net.wasys.getdoc.domain.service.RelatorioGeralService;
import net.wasys.getdoc.domain.service.ResourceService;
import net.wasys.getdoc.domain.vo.filtro.RelatorioGeralFiltro;
import net.wasys.getdoc.mb.utils.DateUtils;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.ddd.TransactionWrapperJob;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

import static net.wasys.util.DummyUtils.*;

@Service
public class RelatorioGeralExportacaoJob extends TransactionWrapperJob {

	private static final String dataCorteFinal = "201907";

	@Autowired private RelatorioGeralService relatorioGeralService;
	@Autowired private ResourceService resourceService;
	@Autowired private LogAcessoService logAcessoService;

	@Override
	@Scheduled(cron="0 0 1-6 * * *")//a cada uma hora entre 1:00 e 6:00
	//@Scheduled(cron="0 0/1 * * * ?")//a cada 1 minuto
	public void run() {
		super.run();
	}

	@Override
	public void execute() {

		long inicio = System.currentTimeMillis();
		systraceThread("iniciando job " + DummyUtils.formatDateTime2(new Date()));

		LogAcesso log = null;
		try {
			log = logAcessoService.criaLogJob(inicio, getLogAcessoJob());
			boolean continuarProcessamento = true;
			Calendar c = Calendar.getInstance();
			Date dataAtual = c.getTime();
			String dataAtualStr = DummyUtils.formatDate2(dataAtual);
			systraceThread("Job iniciou com data atual: " + dataAtualStr);

			do {
				c.set(Calendar.DAY_OF_MONTH, c.getActualMinimum(Calendar.DAY_OF_MONTH));
				Date dataInicio = c.getTime();
				dataInicio = DateUtils.getFirstTimeOfDay(dataInicio);
				c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
				Date dataFim = c.getTime();
				dataFim = DateUtils.getLastTimeOfDay(dataFim);
				String mesAno = DummyUtils.formatDate2(dataInicio);
				systraceThread("Job iniciou com mês atual: " + mesAno);

				if (dataCorteFinal.equals(mesAno)) {
					continuarProcessamento = false;
				}

				RelatorioGeralFiltro filtro = new RelatorioGeralFiltro();
				filtro.setDataInicio(dataInicio);
				filtro.setDataFim(dataFim);
				filtro.setTipoArquivo(RelatorioGeralFiltro.TipoArquivo.CSV);
				filtro.setConsiderarData(RelatorioGeralFiltro.ConsiderarData.CRIACAO);

				List<RelatorioGeralFiltro.Tipo> tipos = Arrays.asList(RelatorioGeralFiltro.Tipo.PROCESSOS, RelatorioGeralFiltro.Tipo.SITUACOES, RelatorioGeralFiltro.Tipo.ETAPAS);

				TransactionWrapper tw = new TransactionWrapper(applicationContext);
				tw.setRunnable(() -> {
					for (RelatorioGeralFiltro.Tipo tipo : tipos) {
						if(tipo.equals(RelatorioGeralFiltro.Tipo.ETAPAS)) {
							filtro.setConsiderarData(null);
						}
						filtro.setTipo(tipo);
						String tipoStr = tipo.toString().toLowerCase();
						String caminho;
						if (mesAno.equals(dataAtualStr)) {
							systraceThread(" Mesma data de importação. Tipo: " + tipoStr + " mesAno importacao:" + mesAno + " MesAnoAtual: " + dataAtualStr);
							filtro.setDataFimEtapaVazio(false);
							caminho = resourceService.getValue(ResourceService.RELATORIO_GERAL_PATH);
							File destino = new File(caminho);
							File[] files = destino.listFiles();
							if (files != null) {
								List<File> filesRemover = new ArrayList<>();
								for (File file : files) {
									String name = file.getName();
									if (!name.contains(dataAtualStr)) {
										filesRemover.add(file);
									}
								}

								if (!filesRemover.isEmpty()) {
									for (File file : filesRemover) {
										systraceThread("Removendo arquivo: " + file.getName());
										file.deleteOnExit();
									}
								}
							}
						} else {
							filtro.setDataFimEtapaVazio(true);
							caminho = resourceService.getValue(ResourceService.RELATORIO_GERAL_HISTORICO_PATH);
							systraceThread(" Datas de importacao diferentes. Tipo: " + tipoStr + " mesAno importacao:" + mesAno + " MesAnoAtual: " + dataAtualStr);
							File destino = new File(caminho);
							File[] files = destino.listFiles();
							if (files != null) {
								List<String> nomeArquivos = new ArrayList<>();
								for(File file : files) {
									String name = file.getName();
									nomeArquivos.add(name);
								}
								String name = "RelatorioGeral-" + mesAno + "-" + tipoStr + ".csv";
								if (nomeArquivos.contains(name)) {
									systraceThread("Nome de arquivo já existe em historico: " + name);
									continue;
								}
							}
						}
						systraceThread(" Criando RelatorioGeral-" + mesAno + "-" + tipoStr + ".csv");
						File file = new File(caminho + "RelatorioGeral-" + mesAno + "-" + tipoStr + ".csv");
						File render = relatorioGeralService.render(filtro);
						FileUtils.copyFile(render, file);
					}
				});
				tw.runNewThread();
				tw.join();
				DummyUtils.sleep(150);//sem isso o hibernate se perde nas sessions
				c.add(Calendar.MONTH, -1);
			} while (continuarProcessamento);
		}
		catch (Exception e) {
			handleException(log, e);
		}
		finally {
			doFinally(inicio, log);
		}
	}
}