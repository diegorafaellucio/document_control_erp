package net.wasys.getdoc.job;

import net.wasys.getdoc.domain.entity.Campo;
import net.wasys.getdoc.domain.entity.CampoGrupo;
import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.enumeration.CampoMap;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.service.ParametroService.P;
import net.wasys.getdoc.domain.service.webservice.sia.SiaService;
import net.wasys.getdoc.domain.vo.ConsultaInscricoesVO;
import net.wasys.getdoc.domain.vo.filtro.AlunoFiltro;
import net.wasys.getdoc.domain.vo.filtro.ProcessoFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.ddd.TransactionWrapperJob;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class AtualizarGeralJob extends TransactionWrapperJob {

	@Autowired private SiaService siaService;
	@Autowired private ConsultaCandidatoService consultaCandidatoService;
	@Autowired private ParametroService parametroService;
	@Autowired private ProcessoService processoService;
	@Autowired private LogAcessoService logAcessoService;
	@Autowired private EmailSmtpService emailSmtpService;
	@Autowired private ResourceService resourceService;
	private ExecutorService executor = Executors.newFixedThreadPool(10);

	@Override
	@Scheduled(cron="0 20/30 * * * ?")//a cada meia hora
	//@Scheduled(cron="0 0/1 * * * ?")//a cada 1 minuto
	public void run() {
		super.run();
	}

	@Override
	public void execute() {

		long inicio = System.currentTimeMillis();
		systraceThread("[INICIO] AtualizarGeralJob " + DummyUtils.formatDateTime(new Date()));
		String pathLogAtualizarGeralJob = resourceService.getValue(ResourceService.LOG_IMPORTACAO_PATH);
		File file = new File(pathLogAtualizarGeralJob + "logAtualizarGeralJob.txt");
		LogAcesso log = null;

		try {
			log = logAcessoService.criaLogJob(inicio, getLogAcessoJob());

			Date dataInicio = parametroService.getValorDate(P.ULTIMA_DATA_ATUALIZACAO_GERAL);

			Date dataFim = dataInicio;
			dataFim = DateUtils.addHours(dataFim, +3);
			if(dataFim.after(new Date())){
				dataFim = new Date();
			}

			ProcessoFiltro filtro = new ProcessoFiltro();
			filtro.setDataInicio(dataInicio);
			filtro.setDataFim(dataFim);
			filtro.setConsiderarData(ProcessoFiltro.ConsiderarData.CRIACAO);
			filtro.setDataFimDoDia(false);
			List<Processo> processos = processoService.findByFiltro(filtro, null, null);
			systraceThread("[INFO] Data Inicio: " + DummyUtils.formatDateTime(dataInicio) + " Data Fim: " + DummyUtils.formatDateTime(dataFim) + " Total de Processos: " + processos.size());

			long inicio2 = System.currentTimeMillis();

			do {
				List<Processo> processos2 = DummyUtils.removeItens(processos, 100);
				if (processos2.isEmpty()) break;

				List<Future> futures = new ArrayList<>();
				for (Processo processo : processos2) {
					TransactionWrapper tw = new TransactionWrapper(applicationContext);
					tw.setRunnable(() -> {
						Set<CampoGrupo> gruposCampos = processo.getGruposCampos();
						String numCandidato = null;
						String numInscrito = null;

						for(CampoGrupo campoGrupo : gruposCampos){
							String grupoNome = campoGrupo.getNome();
							if(CampoMap.GrupoEnum.DADOS_DO_INSCRITO.getNome().equals(grupoNome)) {
								Set<Campo> campos = campoGrupo.getCampos();
								for(Campo campo : campos){
									String nome = campo.getNome();
									if(CampoMap.CampoEnum.NUM_CANDIDATO.getNome().equals(nome)) {
										numCandidato = campo.getValor();
									}
									if(CampoMap.CampoEnum.NUM_INSCRICAO.getNome().equals(nome)) {
										numInscrito = campo.getValor();
									}
								}
							}
						}

						if(StringUtils.isBlank(numCandidato) && StringUtils.isBlank(numInscrito)){
							return;
						}

						AlunoFiltro alunoFiltro = new AlunoFiltro();
						alunoFiltro.setNumInscricao(numInscrito);
						alunoFiltro.setNumCandidato(numCandidato);
						ConsultaInscricoesVO consultarInscricoesSIA = siaService.consultaInscricao(alunoFiltro);

						if(consultarInscricoesSIA != null) {
							consultaCandidatoService.atualizaProcesso(processo, null, consultarInscricoesSIA);
						} else {
							String texto = "[VAZIO] Tipo Processo: " + processo.getTipoProcesso().getNome() + " ProcessoID: " + processo.getId();
							DummyUtils.escrever(file, texto);
						}
					});
					String currentMethodName = DummyUtils.getCurrentMethodName();
					tw.setExceptionHandler((e, stackTrace) -> {
						emailSmtpService.enviarEmailException(currentMethodName, e, stackTrace);
					});
					Future<?> future = executor.submit(tw);
					futures.add(future);
					DummyUtils.sleep(154);//sem isso o hibernate se perde nas sessions
				}

				long timeout = System.currentTimeMillis() + 10 * 60 * 1000;
				DummyUtils.checkTimeout(futures, timeout);
			}
			while (!processos.isEmpty());

			int tempoAtualizacaoMinutos = (int) ((System.currentTimeMillis() - inicio2) / 1000 / 60);

			parametroService.setValorDate(P.ULTIMA_DATA_ATUALIZACAO_GERAL, dataFim);

			systraceThread("[FIM] AtualizarGeralJob. TempoAtualizacaoMinutos: " + tempoAtualizacaoMinutos);
		}
		catch (Exception e) {
			handleException(log, e);
		}
		finally {
			doFinally(inicio, log);
		}
	}
}