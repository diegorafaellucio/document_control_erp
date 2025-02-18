package net.wasys.getdoc.ocrws;

import net.wasys.getdoc.domain.service.ResourceService;
import net.wasys.util.DummyUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import net.wasys.getdoc.domain.entity.ModeloOcr;
import net.wasys.getdoc.domain.service.LogOcrService;
import net.wasys.getdoc.domain.service.ModeloOcrService;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.ocrws.dto.callback.ModeloOcrDTO;
import net.wasys.util.ocrws.dto.callback.NotificacaoDTO;
import net.wasys.util.rest.AbstractController;

import java.beans.Encoder;
import java.io.File;
import java.util.Base64;

import static net.wasys.util.DummyUtils.systraceThread;

@RestController
@RequestMapping(path="/ocrcallback", produces=MediaType.APPLICATION_JSON_VALUE)
public class OcrCallbackController extends AbstractController {

	@Autowired private ModeloOcrService modeloOcrService;
	@Autowired private LogOcrService logOcrService;
	@Autowired private ResourceService resourceService;

	@RequestMapping(value="/getmodelo/{modeloId}")
	public ResponseEntity<?> getModelo(@PathVariable Long modeloId) {

		systraceThread("modeloId: " + modeloId);

		try {
			ModeloOcr modeloOcr = modeloOcrService.get(modeloId);
			if(modeloOcr == null) {
				throw new MessageKeyException("modeloIdInexistente.error", String.valueOf(modeloId));
			}
			Boolean base64 = resourceService.getValue(ResourceService.OCR_WS_BASE64, Boolean.class);

			Long modeloId2 = modeloOcr.getId();
			String descricao = modeloOcr.getDescricao();
			String pathModelo = modeloOcr.getPathModelo();
			Integer altura = modeloOcr.getAltura();
			Integer largura = modeloOcr.getLargura();
			String hashChecksum = modeloOcr.getHashChecksum();
			String fileEncoded = null;
			if(base64) {
				File fileModelo = new File(pathModelo);
				byte[] bytes = FileUtils.readFileToByteArray(fileModelo);
				Base64.Encoder encoder = Base64.getEncoder();
				byte[] encodedBytes = encoder.encode(bytes);
				fileEncoded = new String(encodedBytes);
			}

			ModeloOcrDTO dto = new ModeloOcrDTO();
			dto.setIdOrigem(modeloId2);
			dto.setDescricao(descricao);
			dto.setPathOrigem(pathModelo);
			dto.setAltura(altura);
			dto.setLargura(largura);
			dto.setHashChecksum(hashChecksum);
			dto.setArquivo(fileEncoded);

			return new ResponseEntity<ModeloOcrDTO>(dto, HttpStatus.OK);
		}
		catch (Exception e) {
			return handleException(e);
		}
	}

	@RequestMapping(value="/notificacao/", method=RequestMethod.POST, produces={"application/json; charset=UTF-8"})
	public ResponseEntity<?> notificacao(@RequestBody NotificacaoDTO dto) {

		Long idRegistro = dto.getIdRegistro();
		systraceThread("idRegistro: " + idRegistro);

		try {
			logOcrService.processarNotificacao(dto);

			return new ResponseEntity<>(HttpStatus.OK);
		}
		catch (Exception e) {
			return handleException(e);
		}
	}
}
