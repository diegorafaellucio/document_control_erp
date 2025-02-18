package net.wasys.getdoc.domain.service;

import java.io.PrintWriter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.wasys.getdoc.domain.entity.ModeloDocumento;
import net.wasys.util.DummyUtils;

@Service
public class ExportacaoPalavrasEEService {

	@Autowired private ModeloDocumentoService modeloDocumentoService;

	public void exportar(PrintWriter writer) {

		renderHeader(writer);

		List<ModeloDocumento> documentos = modeloDocumentoService.findAtivos();

		renderBody(writer, documentos);
	}

	private void renderHeader(PrintWriter writer) {

		writer.append("DOCUMENTO").append(";");
		writer.append("PALAVRAS ESPERADAS").append(";");
		writer.append("PALAVRAS EXCLUDENTES").append(";");
		writer.append("\n");
	}

	private void renderBody(PrintWriter writer, List<ModeloDocumento> documentos) {

		for (ModeloDocumento documento : documentos) {

			String descricao = documento.getDescricao();
			writer.append(descricao).append(";");

			String esperadas = DummyUtils.substituirCaracteresEspeciais(documento.getPalavrasEsperadas());
			String palavrasEsperadas = converteString(esperadas);
			writer.append(palavrasEsperadas).append(";");

			String excludentes = DummyUtils.substituirCaracteresEspeciais(documento.getPalavrasExcludentes());
			String palavrasExcludentes = converteString(excludentes);
			writer.append(palavrasExcludentes).append(";");

			writer.append("\n");
		}
	}

	private String converteString(String palavraStr) {
		String palavras = DummyUtils.substituirCaracteresEspeciais(palavraStr);
		String string = ""; 
		if(palavras != null) {
			String[] split = palavras.split(";");

			for(int i = 0; i <= split.length - 1; i ++) {
				String p = split[i];
				if(i < split.length && i != 0) {
					string = p + ", " + string;
				}else {
					string = p + string;
				}
			}
		}

		return string;
	}
}
