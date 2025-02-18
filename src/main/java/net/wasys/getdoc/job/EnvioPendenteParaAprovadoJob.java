package net.wasys.getdoc.job;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.getdoc.domain.service.*;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapperJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class EnvioPendenteParaAprovadoJob extends TransactionWrapperJob {

	@Autowired private LogAcessoService logAcessoService;
	@Autowired private ProcessoService processoService;
	@Autowired private TipoProcessoService tipoProcessoService;
	@Autowired private SituacaoService situacaoService;
	@Autowired private ProcessoLogService processoLogService;

	@Override
	@Scheduled(cron="0 00 0/1 * * ?")//a cada 1 hora
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

			List<Processo> processosPendentesComDocsAprovados = processoService.findProcessosPendentesComDocsAprovados();

			if(processosPendentesComDocsAprovados.isEmpty()) {
				return;
			}

			for (Processo processo : processosPendentesComDocsAprovados) {
				TipoProcesso tipoProcesso = processo.getTipoProcesso();
				tipoProcesso = tipoProcessoService.get(tipoProcesso.getId());
				Situacao situacaoDestinoConclusao = tipoProcesso.getSituacaoDestinoConclusao();

				processoService.concluirSituacao(processo, null, situacaoDestinoConclusao, null, null);
			}

		}
		catch (Exception e) {
			handleException(log, e);
		}
		finally {
			doFinally(inicio, log);
		}
	}
}
