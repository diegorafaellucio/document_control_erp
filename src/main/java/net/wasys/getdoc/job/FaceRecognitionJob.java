package net.wasys.getdoc.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.service.LogAcessoService;
import net.wasys.util.ddd.TransactionWrapperJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.wasys.getdoc.domain.entity.Documento;
import net.wasys.getdoc.domain.service.DocumentoService;
import net.wasys.util.DummyUtils;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class FaceRecognitionJob extends TransactionWrapperJob {

	@Autowired private DocumentoService documentoService;
	@Autowired private LogAcessoService logAcessoService;

	@Override
	//@Scheduled(cron="1/5 * * * * ?")//a cada 5 segundos
	public void run() {
		super.run();
	}

	@Override
	public void execute() {

		long inicio = System.currentTimeMillis();

		LogAcesso log = null;
		try {
			log = logAcessoService.criaLogJob(inicio, getLogAcessoJob());
			List<Long> ids = null;

			ids = documentoService.findIdsToReconhecimentoFacial();

			if(ids.isEmpty()) {
				return;
			}

			systraceThread("processando... " + ids.size() + ", " + ids);

			do {
				List<Long> ids2 = new ArrayList<>();
				for (int i = 0; i < 20 && !ids.isEmpty(); i++) {
					Long id = ids.remove(0);
					ids2.add(id);
				}

				if(ids2.isEmpty()) {
					break;
				}

				List<Documento> documentos = documentoService.findByIds(ids2);

				documentoService.fillReconhecimentoFacial(documentos);
			}
			while(!ids.isEmpty());

			long fim = System.currentTimeMillis();
			long tempo = fim - inicio;
			systraceThread("job finalizado " + DummyUtils.formatDateTime2(new Date()) + " tempo: " + tempo);
		}
		catch (Exception e) {
			handleException(log, e);
		}
		finally {
			doFinally(inicio, log);
		}
	}
}
