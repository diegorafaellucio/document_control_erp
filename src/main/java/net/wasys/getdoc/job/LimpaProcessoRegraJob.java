package net.wasys.getdoc.job;

import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.service.EmailSmtpService;
import net.wasys.getdoc.domain.service.LogAcessoService;
import net.wasys.getdoc.domain.service.ProcessoRegraLogService;
import net.wasys.getdoc.domain.service.ProcessoRegraService;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.ddd.TransactionWrapperJob;
import net.wasys.util.other.Bolso;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class LimpaProcessoRegraJob extends TransactionWrapperJob {

	private static final long TIMEOUT = 1000 * 60 * 60 * 3;//3 horas
	private static final int REGISTROS_A_SEREM_PRESERVADOS = 1;

	@Autowired private ApplicationContext applicationContext;
	@Autowired private EmailSmtpService emailSmtpService;
	@Autowired private LogAcessoService logAcessoService;
	@Autowired private ProcessoRegraService processoRegraService;
	@Autowired private ProcessoRegraLogService processoRegraLogService;

	@Override
	@Scheduled(cron="0 30 03 * * ?")//03:30h
	//@Scheduled(cron="0/30 * * * * ?")//a cada 30 segundos
	public void run() {
		super.run();
	}

	@Override
	public void execute() {

		long inicio = System.currentTimeMillis();
		systraceThread("iniciando job " + DummyUtils.formatDateTime2(new Date()));

		LogAcesso log = null;
		try {
			log = logAcessoService.criaLogJob(inicio, getLogAcessoJob());

			Date dataCorte1 = DateUtils.addDays(new Date(), -7);
			expurgarPRL(log, dataCorte1, inicio);

			Date dataCorte2 = DateUtils.addDays(new Date(), -7);
			expurgarPR(log, dataCorte2, inicio);
		}
		catch (Exception e) {
			handleException(log, e);
		}
		finally {
			doFinally(inicio, log);
		}
	}

	private void expurgarPRL(LogAcesso log, Date dataCorte, long inicio) {

		try {
			boolean terminou;
			do {
				terminou = expurgarPRL(inicio, dataCorte);
			}
			while(!terminou);
		}
		catch (Exception e) {

			e.printStackTrace();

			String messageError = DummyUtils.getExceptionMessage(e);
			systraceThread("erro: " + messageError, LogLevel.ERROR);
			log.setException("Exception em expurgarLog():\n" + messageError + "\n\n");
			String method = DummyUtils.getCurrentMethodName();
			emailSmtpService.enviarEmailException(method, e);
		}
	}

	private boolean expurgarPRL(long inicio, Date dataCorte) throws Exception {

		long tempo = System.currentTimeMillis() - inicio;
		if(tempo > TIMEOUT) {
			systraceThread("finalizando por TIMEOUT");
			return true;
		}

		Bolso<List<Long>> listBolso = new Bolso<>();

		TransactionWrapper tw1 = new TransactionWrapper(applicationContext);
		tw1.setRunnable(() -> {
			List<Long> list = processoRegraLogService.findToExpurgo(dataCorte, 1000);
			listBolso.setObjeto(list);
		});
		int timeout = 1000 * 60 * 10;//10 minutos
		tw1.runNewThread(timeout);
		tw1.throwException();

		List<Long> list = listBolso.getObjeto();
		if(list.isEmpty()) {
			return true;
		}

		TransactionWrapper tw2 = new TransactionWrapper(applicationContext);
		tw2.setRunnable(() ->
			processoRegraLogService.expurgar(list)
		);
		tw2.runNewThread(timeout);
		tw2.throwException();

		return false;
	}

	private void expurgarPR(LogAcesso log, Date dataCorte, long inicio) {

		try {
			boolean terminou;
			do {
				terminou = expurgarPR(inicio, dataCorte, REGISTROS_A_SEREM_PRESERVADOS);
			}
			while(!terminou);
		}
		catch (Exception e) {

			e.printStackTrace();

			String messageError = DummyUtils.getExceptionMessage(e);
			systraceThread("erro: " + messageError, LogLevel.ERROR);
			messageError = "Exception em expurgar():\n" + messageError + "\n\n";
			String exception2 = log.getException();
			log.setException(exception2 + messageError);

			String method = DummyUtils.getCurrentMethodName();
			emailSmtpService.enviarEmailException(method, e);
		}
	}

	private boolean expurgarPR(long inicio, Date dataCorte, int preservar) throws Exception {

		long tempo = System.currentTimeMillis() - inicio;
		if(tempo > TIMEOUT) {
			systraceThread("finalizando por TIMEOUT");
			return true;
		}

		Bolso<List<Object[]>> listBolso = new Bolso<>();

		TransactionWrapper tw1 = new TransactionWrapper(applicationContext);
		tw1.setRunnable(() -> {
			List<Object[]> list = processoRegraService.findToExpurgo(dataCorte, preservar, 50);
			listBolso.setObjeto(list);
		});
		int timeout = 1000 * 60 * 10;//10 minutos
		tw1.runNewThread(timeout);
		tw1.throwException();

		List<Object[]> list = listBolso.getObjeto();
		if(list.isEmpty()) {
			return true;
		}

		for (Object[] obj : list) {
			TransactionWrapper tw2 = new TransactionWrapper(applicationContext);
			tw2.setRunnable(() ->
				processoRegraService.expurgar(obj, preservar)
			);
			tw2.runNewThread(timeout);
			tw2.throwException();
		}

		return false;
	}
}
