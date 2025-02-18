package net.wasys.getdoc.job;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.getdoc.domain.service.*;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapperJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class VerificaAlteracaoProcessoFinanciamentoRascunhoJob extends TransactionWrapperJob {

	@Autowired private LogAcessoService logAcessoService;
	@Autowired private ProcessoService processoService;
	@Autowired private TipoProcessoService tipoProcessoService;
	@Autowired private SituacaoService situacaoService;
	@Autowired private ProcessoLogService processoLogService;

	@Override
	@Scheduled(cron="0 0/30 * * * ?")//a cada 30 minutos
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

			List<Long> ids = Arrays.asList(TipoProcesso.SIS_PROUNI);
			List<TipoProcesso> tipoProcessos = tipoProcessoService.findByIds(ids);
			Situacao situacao = new Situacao();
			List<Long> situacoesParaBuscarNovo = new ArrayList<>();
			List<Long> situacoesParaBuscarProcessados = new ArrayList<>();

			for (TipoProcesso tipoProcesso : tipoProcessos) {
				Long tipoProcessoId = tipoProcesso.getId();

				List<Processo> processosComAlteracao = new ArrayList<>();

				if(tipoProcessoId.equals(TipoProcesso.SIS_PROUNI)) {
					situacao = situacaoService.get(Situacao.SISPROUNI_PROCESSAMENTO_SISTEMICO_ID);
					situacoesParaBuscarNovo = Arrays.asList(Situacao.SISPROUNI_NOVO_ID);
					situacoesParaBuscarProcessados = Arrays.asList(Situacao.SISPROUNI_DIGITALIZADO_SEM_VINCULO_ID, Situacao.SISPROUNI_DIGITALIZADO_INCOMPLETO_ID);
				} else {
					//TODO quando pedirem Sisfies é só mudar aqui
					//situacao = situacaoService.get(Situacao.SISPROUNI_PROCESSAMENTO_SISTEMICO_ID);
					//situacoesParaBuscarNovo = Arrays.asList(new Situacao(Situacao.SISPROUNI_NOVO_ID));
					//situacoesParaBuscarProcessados = Arrays.asList(new Situacao(Situacao.SISPROUNI_DOCUMENTACAO_PENDENTE_ID));
				}

				List<Processo> processosNovoComAlteracao = processoService.findProcessosFinanciamentoNovoComAlteracao(tipoProcessoId, situacoesParaBuscarNovo);
				//List<Processo> processadosComAlteracao = processoService.findProcessosFinanciamentoProcessadosComAlteracao(tipoProcessoId, situacoesParaBuscarProcessados);
				processosComAlteracao.addAll(processosNovoComAlteracao);
				//processosComAlteracao.addAll(processadosComAlteracao);

				for (Processo processo : processosComAlteracao) {
					ProcessoLog processoLog = processoLogService.criaLog(processo, null, AcaoProcesso.VERIFICACAO_ATUALIZACAO_JOB);
					processoService.concluirSituacao(processo, null, situacao, processoLog, null);
				}
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
