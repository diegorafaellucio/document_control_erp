package net.wasys.getdoc.job;

import net.wasys.getdoc.domain.entity.ConfiguracaoGeracaoRelatorio;
import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.service.*;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.ddd.TransactionWrapperJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

import static net.wasys.util.DummyUtils.*;
import static org.apache.commons.lang.time.DateUtils.*;

@Service
public class GerarRelatoriosJob extends TransactionWrapperJob {

	@Autowired private LogAcessoService logAcessoService;
	@Autowired private ConfiguracaoGeracaoRelatorioService configuracaoGeracaoRelatorioService;
	@Autowired private ParametroService parametroService;
	@Autowired private GerarRelatoriosService gerarRelatoriosService;

	@Override
	@Scheduled(cron = "0 0/1 * * * ?")//a cada 1 minuto
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

			Date data0 = parametroService.getValorDate(ParametroService.P.ULTIMA_DATA_GERACAO_RELATORIOS);
			data0 = data0 == null ? truncate(new Date(), Calendar.DAY_OF_MONTH) : data0;

			String horaInicio = DummyUtils.formatTime(data0);

			Date dataFim = new Date();
			String horaFim = DummyUtils.formatTime(dataFim);

			boolean isViradaDeDia = !isSameDay(data0, dataFim);

			gerarRelatorios(horaInicio, horaFim, isViradaDeDia);

			final Date dataFim2 = dataFim;
			TransactionWrapper tw = new TransactionWrapper(applicationContext);
			tw.setRunnable(() -> parametroService.setValorDate(ParametroService.P.ULTIMA_DATA_GERACAO_RELATORIOS, dataFim2));
			tw.runNewThread();
			tw.throwException();
		}
		catch (Exception e) {
			handleException(log, e);
		}
		finally {
			doFinally(inicio, log);
		}
	}

	private void gerarRelatorios(String horaInicio, String horaFim, boolean isViradaDeDia) {

		systraceThread("Buscando entre inicio=" + horaInicio + " e fim=" + horaFim + ", virada de dia=" + isViradaDeDia);

		List<ConfiguracaoGeracaoRelatorio> configs = configuracaoGeracaoRelatorioService.findAtivosEntreHorarios(horaInicio, horaFim, isViradaDeDia);
		configs.forEach(c -> systraceThread("Configuracao encontrada=" + c.getId() + ", nome=" + c.getNome() + ", tipo=" + c.getTipo() + ", horario=" + c.getHorario()));

		try {
			gerarRelatoriosService.iniciarGeracaoRelatoriosAutomatico(configs, null);
		}
		catch (MessageKeyException mke) {

			String messageError = DummyUtils.getExceptionMessage(mke);
			systraceThread(getClass().getSimpleName() + " erro: " + messageError, LogLevel.ERROR);
			mke.printStackTrace();
		}
	}
}
