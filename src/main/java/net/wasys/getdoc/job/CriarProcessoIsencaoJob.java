package net.wasys.getdoc.job;

import com.sun.faces.util.CollectionsUtils;
import lombok.extern.slf4j.Slf4j;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.getdoc.domain.enumeration.Origem;
import net.wasys.getdoc.domain.service.LogAcessoService;
import net.wasys.getdoc.domain.service.ProcessoService;
import net.wasys.getdoc.domain.service.SituacaoService;
import net.wasys.getdoc.domain.service.TipoProcessoService;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.ddd.TransactionWrapperJob;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class CriarProcessoIsencaoJob extends TransactionWrapperJob {

	@Autowired ProcessoService processoService;
	@Autowired TipoProcessoService tipoProcessoService;
	@Autowired SituacaoService situacaoService;
	@Autowired private LogAcessoService logAcessoService;
	@Autowired private SessionFactory sessionFactory;

	@Override
	@Scheduled(cron="0 0 05 * * *")//05:00h
	public void run() {
		super.run();
	}

	@Override
	public void execute() {

		long inicio = System.currentTimeMillis();
		systraceThread("iniciando job " + DummyUtils.formatDateTime(new Date()));

		LogAcesso log = null;
		try {
			log = logAcessoService.criaLogJob(inicio, getLogAcessoJob());


			List<Processo> processoFdiVestibularList = processoService.findProcessoFormaDeIngressoVestibular();

			if(CollectionUtils.isEmpty(processoFdiVestibularList)) {
				return;
			}

			Situacao analiseIsencaoDisciplina = situacaoService.get(Situacao.ISENCAO_DISCIPLINAS_ID);
			TipoProcesso tipoProcesso = tipoProcessoService.get(TipoProcesso.ISENCAO_DISCIPLINAS);

			Session session = sessionFactory.getCurrentSession();
			session.evict(analiseIsencaoDisciplina);
			session.evict(tipoProcesso);

			Map<Long, Exception> erros = new LinkedHashMap<>();

			for(Processo processoIndice: processoFdiVestibularList) {

				session.evict(processoIndice);
				Aluno aluno = processoIndice.getAluno();
				session.evict(aluno);

				try {
					processoService.criaProcessoIsencao(null, processoIndice, analiseIsencaoDisciplina, tipoProcesso, null, null);
				}
				catch (Exception e) {
					erros.put(processoIndice.getId(), e);
				}
			}

			handleExceptions(log, erros);
		}
		catch (Exception e) {
			handleException(log, e);
		}
		finally {
			doFinally(inicio, log);
		}
	}

	private void handleExceptions(LogAcesso log, Map<Long, Exception> erros) {

		if(!erros.isEmpty()) {
			StringBuilder erroStr = new StringBuilder();
			StringBuilder stackTraces = new StringBuilder();
			for (Map.Entry<Long, Exception> entry : erros.entrySet()) {
				Long processoId = entry.getKey();
				Exception exception = entry.getValue();
				String exceptionMessage = DummyUtils.getExceptionMessage(exception);
				erroStr.append("Erro ao criar processo de isenção para o processo ").append(processoId).append(": ").append(exceptionMessage).append("\n");

				String stackTrace = ExceptionUtils.getStackTrace(exception);
				stackTraces.append("processoId: ").append(processoId).append(": ").append(stackTrace).append("\n\n");
			}
			erroStr.append("\n\n").append(stackTraces);

			String exception = log.getException();
			exception = exception != null ? exception + "\n\n" : "";
			exception += erroStr.toString();
			log.setException(exception);
		}
	}
}
