package net.wasys.getdoc.job;

import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.service.DocumentoService;
import net.wasys.getdoc.domain.service.LogAcessoService;
import net.wasys.getdoc.domain.service.ParametroService;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapperJob;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static net.wasys.util.DummyUtils.*;

@Service
public class AtualizaDadoUsaTermoJob extends TransactionWrapperJob {
	@Autowired private DocumentoService documentoService;
	@Autowired private LogAcessoService logAcessoService;
	@Autowired private ParametroService parametroService;

	@Override
	@Scheduled(cron = "0 0/30 * * * ?")//a cada 30 minuto
	public void run() {
		super.run();
	}

	@Override
	protected void execute() {

		long inicio = System.currentTimeMillis();
		Date inicioDt = new Date();
		systraceThread("iniciando job " + DummyUtils.formatDateTime2(inicioDt));

		LogAcesso log = null;
		try {
			log = logAcessoService.criaLogJob(inicio, getLogAcessoJob());

			boolean continuar;
			do {

				Date dataInicioAtualizacao = new Date();

				Date dataUltimaAtualizacao = parametroService.getValorDate(ParametroService.P.ULTIMA_DATA_ATUALIZACAO_TERMO);
				if (dataUltimaAtualizacao == null) {
					dataUltimaAtualizacao = DummyUtils.parseDate("30/09/2019");
				}

				Date dataFim = getDataFimExecucao(dataUltimaAtualizacao);

				systraceThread("Atualizando de " + formatDateTime(dataUltimaAtualizacao) + " até " + formatDateTime(dataFim));

				documentoService.atualizarUsaTermo(dataUltimaAtualizacao, dataFim, null);
				parametroService.setValorDate(ParametroService.P.ULTIMA_DATA_ATUALIZACAO_TERMO, dataFim);

				continuar = deveContinuarAtualizacao(dataUltimaAtualizacao, dataInicioAtualizacao);

				long duracao = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - dataInicioAtualizacao.getTime());
				systraceThread("Atualização finalizada. Continua: " + continuar + ". Data de Corte: " + DummyUtils.formatDateTime2(dataFim) + ". TempoAtualizacaoSegundos: " + duracao);

			} while (continuar);
		}
		catch (Exception e) {
			handleException(log, e);
		}
		finally {
			doFinally(inicio, log);
		}
	}

	private Date getDataFimExecucao(Date dataInicio) {

		Date agora = new Date();
		long intervaloEmHoras = MILLISECONDS.toHours(agora.getTime() - dataInicio.getTime());

		Date dataFim;
		if (intervaloEmHoras > 6) {
			dataFim = DateUtils.addHours(dataInicio, 6);
		}
		else {
			dataFim = agora;
		}

		return dataFim;
	}

	private boolean deveContinuarAtualizacao(Date ultimaAtualizacao, Date inicioAtualizacao) {
		return DateUtils.truncatedCompareTo(ultimaAtualizacao, inicioAtualizacao, Calendar.MINUTE) < 0;
	}
}
