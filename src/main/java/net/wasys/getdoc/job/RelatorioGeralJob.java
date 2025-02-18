package net.wasys.getdoc.job;

import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.service.LogAcessoService;
import net.wasys.getdoc.domain.service.ParametroService;
import net.wasys.getdoc.domain.service.ParametroService.P;
import net.wasys.getdoc.domain.service.ProcessoLogService;
import net.wasys.getdoc.domain.service.RelatorioGeralService;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapperJob;
import net.wasys.util.servlet.LogAcessoFilter;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class RelatorioGeralJob extends TransactionWrapperJob {

	@Autowired private RelatorioGeralService relatorioGeralService;
	@Autowired private ParametroService parametroService;
	@Autowired private ProcessoLogService processoLogService;
	@Autowired private LogAcessoService logAcessoService;

	@Override
	@Scheduled(cron="0 20/30 * * * ?")//a cada meia hora
	//@Scheduled(cron="0 0/1 * * * ?")//a cada 1 minuto
	public void run() {
		super.run();
	}

	@Override
	public void execute() {

		long inicio = System.currentTimeMillis();
		DummyUtils.systraceThread("iniciando job " + DummyUtils.formatDateTime(new Date()));

		LogAcesso log = null;
		try {
			log = logAcessoService.criaLogJob(inicio, getLogAcessoJob());

			atualizarDataUltimaAtualizacaoProcessos();

			atualizarRelatorioGeral();
		}
		catch (Exception e) {
			handleException(log, e);
		}
		finally {
			doFinally(inicio, log);
		}
	}

	private void atualizarDataUltimaAtualizacaoProcessos() throws Exception {

		boolean continuar;

		Date ultimaAtualizacao = parametroService.getValorDate(P.ULTIMA_DATA_ATUALIZACAO_DATAS_PROCESSO);
		LogAcesso la = LogAcessoFilter.getLogAcesso();
		DummyUtils.addParameter(la, "ultimaDataAtualizacaoDatasProcessoInicio", DummyUtils.formatDateTime2(ultimaAtualizacao));

		do {
			Date inicioAtualizacao = new Date();

			ultimaAtualizacao = relatorioGeralService.atualizarDataUltimaAtualizacaoProcessos(ultimaAtualizacao);

			continuar = deveContinuarAtualizacaoDatas(inicioAtualizacao, ultimaAtualizacao);

			long duracao = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - inicioAtualizacao.getTime());
			DummyUtils.systraceThread("[data] atualização finalizada. Continua: " + continuar + ". Data de Corte: " + DummyUtils.formatDateTime2(ultimaAtualizacao) + ". TempoAtualizacaoSegundos: " + duracao);
		}
		while (continuar);
	}

	private void atualizarRelatorioGeral() throws Exception {

		boolean continuar;

		Date data0 = parametroService.getValorDate(P.ULTIMA_DATA_RELATORIO_GERAL);
		LogAcesso logAcesso = LogAcessoFilter.getLogAcesso();
		DummyUtils.addParameter(logAcesso, "dataBD", data0);
		do {
			long inicio2 = System.currentTimeMillis();

			data0 = relatorioGeralService.atualizar(data0);

			int tempoAtualizacaoMinutos = (int) ((System.currentTimeMillis() - inicio2) / 1000 / 60);

			continuar = processoLogService.existePosteriorA(data0);

			if(continuar) {
				atualizarDataUltimaAtualizacaoProcessos();
			}

			DummyUtils.systraceThread("[data] atualização finalizada. Continua: " + continuar + ". Data de Corte: " + DummyUtils.formatDateTime2(data0) + ". TempoAtualizacaoMinutos: " + tempoAtualizacaoMinutos);
		}
		while (continuar);
	}

	private boolean deveContinuarAtualizacaoDatas(Date inicioAtualizacao, Date ultimaAtualizacao) {
		return DateUtils.truncatedCompareTo(ultimaAtualizacao, inicioAtualizacao, Calendar.MINUTE) < 0;
	}
}
