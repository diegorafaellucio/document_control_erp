package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.Documento;
import net.wasys.getdoc.domain.vo.FileVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OcrService {

	@Autowired private OcrDarknetService orcDarknetService;
	@Autowired private DocumentoService documentoService;
	@Autowired private ProcessoService processoService;

	public void extrairOcr(Documento documento, List<FileVO> list) throws Exception {
		orcDarknetService.extrairOCR(documento, list);
	}

}
