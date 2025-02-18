package net.wasys.getdoc.job;

import net.wasys.getdoc.domain.entity.Documento;
import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.enumeration.StatusDocumento;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.service.ParametroService.P;
import net.wasys.getdoc.domain.vo.filtro.DocumentoLogFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.ddd.TransactionWrapperJob;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class GeraHashUnicoJob extends TransactionWrapperJob {

	@Autowired private ParametroService parametroService;
	@Autowired private DocumentoService documentoService;
	@Autowired private LogAcessoService logAcessoService;
	@Autowired private EmailSmtpService emailSmtpService;
	@Autowired private DocumentoLogService documentoLogService;
	private ExecutorService executor = Executors.newFixedThreadPool(10);

	@Override
	@Scheduled(cron="0 0/5 1-6 * * *")//a cada 5 minutos entre 1:00 e 6:00
	//@Scheduled(cron="0 0/5 * * * ?")//a cada 5 minutos
	public void run() {
		super.run();
	}

	@Override
	public void execute() {

		long inicio = System.currentTimeMillis();
		systraceThread("[INICIO] GeraHashUnicoJob " + DummyUtils.formatDateTime(new Date()));
		LogAcesso log = null;

		try {
			log = logAcessoService.criaLogJob(inicio, getLogAcessoJob());

			Date dataInicio = parametroService.getValorDate(P.ULTIMA_DATA_DIGITALIZACAO_DOCUMENTO);
			Date dataFim = DateUtils.addHours(dataInicio, +1);
			if(dataFim.after(new Date())){
				dataFim = new Date();
			}

			DocumentoLogFiltro filtro = new DocumentoLogFiltro();
			filtro.setDataInicio(dataInicio);
			filtro.setDataFim(dataFim);
			filtro.setStatusDifetenteDeList(Arrays.asList(StatusDocumento.INCLUIDO, StatusDocumento.EXCLUIDO));
			filtro.setDiferenteDeOutros(true);
			filtro.setDiferenteDeTipificando(true);
			List<Documento> documentos = documentoLogService.findDocumentoByFiltro(filtro, null, null);
			systraceThread("[INFO] Data Inicio: " + DummyUtils.formatDateTime(dataInicio) + " Data Fim: " + DummyUtils.formatDateTime(dataFim) + " Total de Documentos: " + documentos.size());

			long inicio2 = System.currentTimeMillis();

			do {
				List<Documento> documentos2 = DummyUtils.removeItens(documentos, 100);
				if (documentos2.isEmpty()) break;

				List<Future> futures = new ArrayList<>();
				for (Documento documento : documentos2) {
					TransactionWrapper tw = new TransactionWrapper(applicationContext);
					tw.setRunnable(() -> {
						Integer versaoAtual = documento.getVersaoAtual();
						Long documentoId = documento.getId();
						if(versaoAtual != null) {
							for (Integer i = 1; versaoAtual >= i; i++) {
								documentoService.getDownload(documentoId, i, true);
							}
						}
					});
					String currentMethodName = DummyUtils.getCurrentMethodName();
					tw.setExceptionHandler((e, stackTrace) -> {
						emailSmtpService.enviarEmailException(currentMethodName, e, stackTrace);
					});
					Future<?> future = executor.submit(tw);
					futures.add(future);
					DummyUtils.sleep(155);//sem isso o hibernate se perde nas sessions
				}

				long timeout = System.currentTimeMillis() + 10 * 60 * 1000;
				DummyUtils.checkTimeout(futures, timeout);
			}
			while (!documentos.isEmpty());

			int tempoAtualizacaoMinutos = (int) ((System.currentTimeMillis() - inicio2) / 1000 / 60);

			parametroService.setValorDate(P.ULTIMA_DATA_DIGITALIZACAO_DOCUMENTO, dataFim);

			systraceThread("[FIM] GeraHashUnicoJob. TempoAtualizacaoMinutos: " + tempoAtualizacaoMinutos);
		}
		catch (Exception e) {
			handleException(log, e);
		}
		finally {
			doFinally(inicio, log);
		}
	}
}