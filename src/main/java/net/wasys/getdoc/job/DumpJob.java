package net.wasys.getdoc.job;

import net.wasys.getdoc.domain.service.EmailSmtpService;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapperJob;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Date;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class DumpJob extends TransactionWrapperJob {

	@Autowired private EmailSmtpService emailSmtpService;

	@Override
	//@Scheduled(cron="0 0 04 * * ?")//quatro da manhã
	//@Scheduled(cron="0 0/2 * * * ?")//a cada 2 minutos
	public void run() {
		super.run();
	}

	@Override
	public void execute() {

		long inicio = System.currentTimeMillis();

		String mode = DummyUtils.getMode();
		if(StringUtils.isNotBlank(mode) && !"dev".equals(mode)) {
			systraceThread("FIM (job não habilitado para execução em modo " + mode + ")");
			return;
		}

		try {

			String pathScript = "D:\\dump\\getdoc_ag\\script-dump.bat";

			Runtime runtime = Runtime.getRuntime();
			Process process = runtime.exec(pathScript);
			InputStream is = process.getInputStream();

			StringBuilder saida = new StringBuilder();
			for (int c; (c = is.read()) != -1;) {
				saida.append((char) c);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			emailSmtpService.enviarEmailException("DumpJob", e);
		}
		finally {
			long fim = System.currentTimeMillis();
			long tempo = fim - inicio;

			systraceThread("job finalizado " + new Date() + " tempo: " + tempo);
		}
	}
}
