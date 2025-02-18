package net.wasys.getdoc.job;

import net.wasys.getdoc.domain.service.BloqueioProcessoService;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.SpringJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import org.springframework.stereotype.Service;

@Service
public class LimpaBloqueioProcessoJob extends SpringJob {

	@Autowired private BloqueioProcessoService bloqueioProcessoService;

	@Override
	@Scheduled(cron="0 0 02 * * ?")
	public void executeWithSessionManagement() {
		super.executeWithSessionManagement();
	}

	@Override
	public void execute() {

		int deletados = bloqueioProcessoService.limparVencidos();

		DummyUtils.systraceThread("finalizando job, " + deletados + " bloqueios deletados.");
	}
}
