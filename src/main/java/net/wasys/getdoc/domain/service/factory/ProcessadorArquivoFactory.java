package net.wasys.getdoc.domain.service.factory;

import static net.wasys.getdoc.GetdocConstants.EXTENSAO_DEFINICAO_CSV;
import static net.wasys.getdoc.GetdocConstants.EXTENSAO_DEFINICAO_TXT;
import static net.wasys.getdoc.GetdocConstants.EXTENSAO_DEFINICAO_XLS;
import static net.wasys.getdoc.GetdocConstants.EXTENSAO_DEFINICAO_XLSX;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import net.wasys.getdoc.domain.service.ProcessadorArquivo;
import net.wasys.getdoc.domain.service.ProcessadorArquivoCSV;
import net.wasys.getdoc.domain.service.ProcessadorArquivoExcel;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.MessageKeyException;

public class ProcessadorArquivoFactory {

	private ProcessadorArquivoFactory() {}

	public static String getExtensoesSuportadas() {
		return EXTENSAO_DEFINICAO_CSV + ", " + EXTENSAO_DEFINICAO_TXT + ", " + EXTENSAO_DEFINICAO_XLSX + ", " + EXTENSAO_DEFINICAO_XLS;
	}

	public static ProcessadorArquivo getProcessador(File file, List<String> colunasUnicidade) throws MessageKeyException {
		String extensao = DummyUtils.getExtensao(file.getName());
		ProcessadorArquivo processador = null;

		try {
			if(EXTENSAO_DEFINICAO_CSV.equals(extensao)
					|| EXTENSAO_DEFINICAO_TXT.equals(extensao)) {
				processador = new ProcessadorArquivoCSV(file, colunasUnicidade);
			}
			else if(EXTENSAO_DEFINICAO_XLSX.equals(extensao)
					|| EXTENSAO_DEFINICAO_XLS.equals(extensao)) {
				processador = new ProcessadorArquivoExcel(file, colunasUnicidade);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new MessageKeyException("erroInesperado.error", e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			throw new MessageKeyException("erroInesperado.error", e.getMessage());
		}

		if(processador != null) {
			return processador;
		}
		else {
			throw new MessageKeyException("erroInesperadoProcessarArquivo.error", ProcessadorArquivoFactory.getExtensoesSuportadas());
		}
	}
}
