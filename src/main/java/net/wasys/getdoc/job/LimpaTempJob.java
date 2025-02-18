package net.wasys.getdoc.job;

import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.service.LogAcessoService;
import net.wasys.getdoc.domain.service.ResourceService;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.TransactionWrapperJob;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class LimpaTempJob extends TransactionWrapperJob {

	private static final int LIMITE_DIAS_CACHE = 4;
	private static final int LIMITE_DIAS_REAPROVEITAMENTO = 2;

	private enum FileToDelete {

		POP3("pop3_.*", ".*"),
		OCR_RMI("ocrRmi.*", "jar"),
		PDF_TO_IMG("pdf_to_img.*", "jpg"),
		UPLOAD("upload_.*", "(jpg)|(tmp)"),
		ANEXO_PROC("anexo-proc-.*", "tmp"),
		DOWN_ADITIVO("down-aditivo.*", "pdf"),
		DOWN_PROTOCOLO("down-protocolo.*", "pdf"),
		LOGO_EMAIL("logo-[a-z].*-200px.*", "png"),
		UPLOAD_PRINT("uploadImagePrint_.*", "jpg"),
		UPLOADED_ARQUIVO("uploadedArquivo_.*", "pdf"),
		EXPORTACAO_DADOS_OCR("exportacao-dados-ocr_.*", "txt"),
		RELATORIO_GERAL("relatorio-geral.*", "(xlsm)|(xlsx)|(xls)"),
		PASTA_VERMELHA(".*pasta[_-]vermelha.*", "(csv|zip|xlsx)"),
		;

		private String matchNome;
		private String matchExtensao;

		private FileToDelete(String matchNome, String matchExtensao) {
			this.matchNome = matchNome;
			this.matchExtensao = matchExtensao;
		}
	}

	@Autowired protected ResourceService resourceService;
	@Autowired private LogAcessoService logAcessoService;

	@Override
	@Scheduled(cron="0 0 03 * * ?")//03:00h
	//@Scheduled(cron="0 0/1 * * * ?")//a cada 1 minuto
	public void run() {
		super.run();
	}

	@Override
	public void execute() {

		long inicio = System.currentTimeMillis();
		systraceThread("iniciando job " + DummyUtils.formatDateTime2(new Date()));

		String mode = DummyUtils.getMode();
		if(StringUtils.isNotBlank(mode) && !"dev".equals(mode)) {
			systraceThread("FIM (job não habilitado para execução em modo " + mode + ")");
			return;
		}

		AtomicInteger tempsDeletados = new AtomicInteger();
		AtomicInteger reaproveitamentosDeletados = new AtomicInteger();
		AtomicInteger cachesDeletados = new AtomicInteger();

		Timer timer = new Timer();

		LogAcesso log = null;
		try {
			log = logAcessoService.criaLogJob(inicio, getLogAcessoJob());

			LogAcesso log2 = log;
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					salvarLogs(log2, tempsDeletados, reaproveitamentosDeletados, cachesDeletados);
				}
			}, 5000, 5000);

			String tmpDirStr = System.getProperty("java.io.tmpdir");
			File tmpDir = new File(tmpDirStr);
			File[] listFiles = tmpDir.listFiles();
			for (File file : listFiles) {
				String name = file.getName();
				name = name != null ? name : "";
				String extensao = DummyUtils.getExtensao(name);
				extensao = extensao != null ? extensao : "";
				for (FileToDelete ftd : FileToDelete.values()) {
					if(name.matches(ftd.matchNome) && extensao.matches(ftd.matchExtensao)) {
						DummyUtils.deleteFile(file);
						tempsDeletados.incrementAndGet();
					}
				}
			}

			String prefix = DummyUtils.SYSPREFIX + "/reaproveitamento/";
			prefix = prefix.replace("[", "").replace("]", "").replace(" ", "");
			String storageTmpPath = resourceService.getValue(ResourceService.STORAGE_TMP_PATH) + prefix;
			File storageTmpDir = new File(storageTmpPath);
			if(storageTmpDir.exists()) {
				File[] files = storageTmpDir.listFiles();
				deleteFiles(storageTmpDir.getAbsolutePath(), files, reaproveitamentosDeletados, LIMITE_DIAS_REAPROVEITAMENTO);
			}

			String cachePath = resourceService.getValue(ResourceService.CACHE_PATH);
			File cacheDir = new File(cachePath);
			if(cacheDir.exists()) {
				File[] files = cacheDir.listFiles();
				deleteFiles(cacheDir.getAbsolutePath(), files, cachesDeletados, LIMITE_DIAS_CACHE);
			}
		}
		catch (Exception e) {
			handleException(log, e);
		}
		finally {

			timer.cancel();
			salvarLogs(log, tempsDeletados, reaproveitamentosDeletados, cachesDeletados);

			doFinally(inicio, log);
		}
	}

	private void salvarLogs(LogAcesso log, AtomicInteger tempsDeletados, AtomicInteger reaproveitamentosDeletados, AtomicInteger cachesDeletados) {

		DummyUtils.addParameter(log, "tempsDeletados", tempsDeletados.get());
		DummyUtils.addParameter(log, "reaproveitamentosDeletados", reaproveitamentosDeletados.get());
		DummyUtils.addParameter(log, "cachesDeletados", cachesDeletados.get());

		logAcessoService.saveOrUpdateNewSession(log);
	}

	private void deleteFiles(String parent, File[] files, AtomicInteger count, int limiteDias) {

		systraceThread("parent: " + parent + ", files: " + files.length);

		Date hoje = new Date();
		if(files != null) {
			for (File file : files) {
				if(file.exists() && file.isDirectory()) {
					File[] files2 = file.listFiles();
					deleteFiles(file.getAbsolutePath(), files2, count, limiteDias);

					String[] list = file.list();
					if(list == null || list.length ==0) {
						file.delete();
					}
				}
				else {
					long lastModified = file.lastModified();
					Date lastModifiedDate = new Date(lastModified);

					int diasEntre = DummyUtils.getDiasEntre(lastModifiedDate, hoje);
					if(diasEntre > limiteDias){
						file.delete();
						count.incrementAndGet();
					}
				}
			}
		}
	}
}
