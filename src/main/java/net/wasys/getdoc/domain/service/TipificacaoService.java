package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.Documento;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.vo.FileVO;
import net.wasys.getdoc.domain.vo.filtro.DocumentoFiltro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TipificacaoService {

	@Autowired private TipificacaoDarknetService tipificacaoDarknetService;
	@Autowired private TipificacaoVisionApiService tipificacaoVisionApiService;
	@Autowired private DocumentoService documentoService;
	@Autowired private ProcessoService processoService;

	public Map<Long, List<FileVO>> tipificar(Long processoId, Documento documentoOrigem, List<FileVO> list, List<FileVO> naoTipificou) {

		Processo processo = processoService.get(processoId);
		DocumentoFiltro filtro = new DocumentoFiltro();
		filtro.setProcesso(processo);
		List<Documento> documentos = documentoService.findByFiltro(filtro, null, null);

		Map<Long, List<FileVO>> map = new HashMap<>();
		Map<Long, List<FileVO>> tipificacaoDarknet = tipificacaoDarknetService.tipificar(documentoOrigem, list, documentos, naoTipificou);
		map.putAll(tipificacaoDarknet);

		if(!naoTipificou.isEmpty()){

			Map<Long, List<FileVO>> tipificacaoVision = tipificacaoVisionApiService.tipificar(documentos, naoTipificou);
			for (Long documentoId2 : tipificacaoVision.keySet()) {
				List<FileVO> listVision = tipificacaoVision.get(documentoId2);
				List<FileVO> list2 = map.get(documentoId2);
				list2 = list2 != null ? list2 : new ArrayList<>();
				map.put(documentoId2, list2);
				list2.addAll(listVision);
			}
		}

		return map;
	}

	public boolean isVisionHabilitada() {
		return tipificacaoVisionApiService.isHabilitada();
	}

	public boolean isHabilitada() {
		boolean darknetHabilitada = tipificacaoDarknetService.isHabilitada();
		boolean visionHabilitada = tipificacaoVisionApiService.isHabilitada();
		return darknetHabilitada || visionHabilitada;
	}

	public Map<Long, List<FileVO>> tipificarFluxoAprovacao(Documento documento, List<FileVO> imageList) {

		Map<Long, List<FileVO>> tipificacaoDarknet = tipificacaoDarknetService.tipificarFluxoAprovacao(imageList, documento);

		return tipificacaoDarknet;
	}
}
