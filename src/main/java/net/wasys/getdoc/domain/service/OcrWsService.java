package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;

import net.wasys.getdoc.domain.entity.Imagem;
import net.wasys.getdoc.domain.entity.LogOcr;
import net.wasys.getdoc.domain.entity.ModeloOcr;
import net.wasys.util.DummyUtils;
import net.wasys.util.ocrws.dto.AgendamentoDTO;
import net.wasys.util.ocrws.dto.VerificacaoDTO;
import net.wasys.util.ocrws.dto.callback.NotificacaoDTO;
import net.wasys.util.rest.RestClient;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class OcrWsService {

	private static final String URI_AGENDAR = "/agendar";
	private static final String URI_VERIFICAR = "/verificar";

	@Autowired private ResourceService resourceService;
	@Autowired private EmailSmtpService emailSmtpService;
	@Autowired private ImagemService imagemService;

	public Long agendarOcr(LogOcr logOcr, List<Imagem> imagensSecundarias) throws Exception {

		String executarOcr = System.getProperty("getdoc.executarOcr");
		if("false".equals(executarOcr)) {
			return null;
		}

		Long idSistema = resourceService.getValue(ResourceService.OCR_WS_ID_SISTEMA, Long.class);
		String urlServico = resourceService.getValue(ResourceService.OCR_WS_URL);
		Boolean base64 = resourceService.getValue(ResourceService.OCR_WS_BASE64, Boolean.class);

		if(idSistema == null) {
			throw new RuntimeException("Parametro OCR_WS_ID_SISTEMA null");
		}
		if(StringUtils.isBlank(urlServico)) {
			throw new RuntimeException("Parametro OCR_WS_URL null");
		}

		Long logOcrId = logOcr.getId();
		systraceThread("logOcrId: " + logOcrId + " idSistema: " + idSistema);

		final String url = urlServico + URI_AGENDAR;
		ModeloOcr modeloOcr = logOcr.getModeloOcr();
		Long modeloOcrId = modeloOcr.getId();
		String modeloHashChecksum = modeloOcr.getHashChecksum();
		String caminhoImagem = logOcr.getCaminhoImagem();
		String extensaoImagem = logOcr.getExtensaoImagem();
		String[] pathsImagensSecundariasArray = null;
		String imagemEncoded = null;

		if(base64 == null || !base64) {
			List<String> pathsImagensSecundarias = new ArrayList<>();
			for (Imagem imagem : imagensSecundarias) {
				String caminho = imagem.getCaminho();
				if(!new File(caminho).exists()) {
					imagemService.atualizaCaminho(imagem);
					caminho = imagem.getCaminho();
				}
				pathsImagensSecundarias.add(caminho);
			}
			pathsImagensSecundarias.remove(caminhoImagem);
			pathsImagensSecundariasArray = pathsImagensSecundarias.toArray(new String[pathsImagensSecundarias.size()]);
		}
		else {
			File file = new File(caminhoImagem);
			byte[] bytes = FileUtils.readFileToByteArray(file);
			Base64.Encoder encoder = java.util.Base64.getEncoder();
			byte[] encodedBytes = encoder.encode(bytes);
			imagemEncoded = new String(encodedBytes);
		}

		final AgendamentoDTO dto = new AgendamentoDTO();
		dto.setIdSistema(idSistema);
		dto.setIdRegistro(logOcrId);
		dto.setIdModeloOcr(modeloOcrId);
		dto.setHashChecksumModeloOcr(modeloHashChecksum);
		dto.setExtensaoImagem(extensaoImagem);
		dto.setPathImagem(caminhoImagem);
		dto.setPathsImagensSecundarias(pathsImagensSecundariasArray);
		dto.setImagem(imagemEncoded);

		RestClient rc = new RestClient();
		rc.setUrl(url);
		try {
			Long agendamentoId = rc.execute(dto, Long.class);
			return agendamentoId;
		}
		catch (Exception e) {
			e.printStackTrace();
			String stackTrace = ExceptionUtils.getStackTrace(e);
			emailSmtpService.enviarEmailException(DummyUtils.getCurrentMethodName(), stackTrace, url);
			throw e;
		}
	}

	public NotificacaoDTO verificar(Long logOcrId) {

		Long idSistema = resourceService.getValue(ResourceService.OCR_WS_ID_SISTEMA, Long.class);
		String urlServico = resourceService.getValue(ResourceService.OCR_WS_URL);

		if(idSistema == null) {
			throw new RuntimeException("Parametro OCR_WS_ID_SISTEMA null");
		}
		if(StringUtils.isBlank(urlServico)) {
			throw new RuntimeException("Parametro OCR_WS_URL null");
		}

		systraceThread("logOcrId: " + logOcrId + " idSistema: " + idSistema);

		final String url = urlServico + URI_VERIFICAR;

		final VerificacaoDTO dto = new VerificacaoDTO();
		dto.setIdRegistro(logOcrId);
		dto.setIdSistema(idSistema);

		RestClient rc = new RestClient();
		rc.setUrl(url);

		try {
			NotificacaoDTO dto2 = rc.execute(dto, NotificacaoDTO.class);
			return dto2;
		}
		catch (Exception e) {
			e.printStackTrace();
			emailSmtpService.enviarEmailException(DummyUtils.getCurrentMethodName(), e);
			return null;
		}
	}
}
