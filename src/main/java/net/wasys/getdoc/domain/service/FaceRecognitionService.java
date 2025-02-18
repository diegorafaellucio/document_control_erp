package net.wasys.getdoc.domain.service;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.wasys.getdoc.GetdocConstants;
import net.wasys.getdoc.domain.entity.Documento;
import net.wasys.getdoc.domain.entity.FaceRecognition;
import net.wasys.getdoc.domain.entity.Imagem;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.enumeration.CampoMap;
import net.wasys.getdoc.domain.enumeration.FaceRecognitionApi;
import net.wasys.getdoc.domain.enumeration.StatusFacial;
import net.wasys.getdoc.domain.repository.FaceRecognitionRepository;
import net.wasys.getdoc.domain.vo.ComparacaoFacesVO;
import net.wasys.util.DummyUtils;

@Service
public class FaceRecognitionService {

	@Autowired private FaceRecognitionRepository faceRecognitionRepository;
	@Autowired private FaceRecognitionGrupoDigitalService faceRecognitionGrupoDigitalService;
	@Autowired private FaceRecognitionBetafaceService faceRecognitionBetafaceService;
	@Autowired private ImagemService imagemService;

	public ComparacaoFacesVO comparar(Imagem imagemBase, List<Imagem> imagemList) {

		ComparacaoFacesVO cf = new ComparacaoFacesVO();
		cf.setImagemBase(imagemBase);

		if(imagemBase == null) {
			return cf;
		}

		Documento documentoBase = imagemBase.getDocumento();
		documentoBase.setStatusFacial(StatusFacial.BASE);

		Long imagemId = imagemBase.getId();
		FaceRecognition frBase = faceRecognitionRepository.getByImagem(imagemId, GetdocConstants.FACE_RECOGNITION_API_DEFAULT);

		List<Imagem> toReconhecer = new ArrayList<>();
		if(frBase == null) {
			toReconhecer.add(imagemBase);
		}

		List<FaceRecognition> compareList = new ArrayList<>();
		for (Imagem imagem : imagemList) {

			Long imagemId2 = imagem.getId();
			Imagem imagemBase2 = imagem.getBaseFacial();
			BigDecimal similaridadeFacial = imagem.getSimilaridadeFacial();

			cf.addImagem(imagem);

			if(imagemBase.equals(imagemBase2) && similaridadeFacial != null) {
				continue;
			}

			String caminho = imagem.getCaminho();
			if(!new File(caminho).exists()) {
				caminho = imagemService.gerarCaminho(imagem);
			}

			if(new File(caminho).exists()) {

				FaceRecognition fr = faceRecognitionRepository.getByImagem(imagemId2, GetdocConstants.FACE_RECOGNITION_API_DEFAULT);

				if(fr == null) {
					toReconhecer.add(imagem);
				} else {
					compareList.add(fr);
				}
			}
		}

		if(!toReconhecer.isEmpty()) {

			List<FaceRecognition> reconhecimento = null;
			if(GetdocConstants.FACE_RECOGNITION_API_DEFAULT.equals(FaceRecognitionApi.POLICIAL_TECH)) {
				reconhecimento = faceRecognitionGrupoDigitalService.reconhecer(toReconhecer);
			} else {
				reconhecimento = faceRecognitionBetafaceService.reconhecer(toReconhecer);
			}

			for (FaceRecognition fr : reconhecimento) {

				Imagem imagem = fr.getImagem();
				Long imagemId2 = imagem.getId();
				FaceRecognitionApi api = fr.getApi();
				faceRecognitionRepository.delete(imagemId2, api);

				faceRecognitionRepository.saveOrUpdate(fr);

				toReconhecer.remove(imagem);

				if(imagemBase.equals(imagem)) {

					frBase = fr;

					Documento documento = imagem.getDocumento();
					Processo processo = documento.getProcesso();
					String nome = DummyUtils.getCampoProcessoValor(processo, CampoMap.CampoEnum.NOME_COMPLETO);
					fr.setNome(nome);
				}
				else {
					compareList.add(fr);
				}
			}

			for (Imagem imagem : toReconhecer) {
				imagem.setSimilaridadeFacial(null);
				imagem.setBaseFacial(imagemBase);
				Documento documento2 = imagem.getDocumento();
				documento2.setStatusFacial(null);
			}
		}

		if(frBase == null) {
			for (Imagem imagem : imagemList) {
				imagem.setSimilaridadeFacial(null);
				imagem.setBaseFacial(imagemBase);
				Documento documento2 = imagem.getDocumento();
				documento2.setStatusFacial(null);
			}
		}
		else if(!compareList.isEmpty()) {

			Map<Imagem, BigDecimal> mapResult = null;
			if(GetdocConstants.FACE_RECOGNITION_API_DEFAULT.equals(FaceRecognitionApi.POLICIAL_TECH)) {
				mapResult = faceRecognitionGrupoDigitalService.comparar(frBase, compareList);
			} else {
				mapResult = faceRecognitionBetafaceService.comparar(frBase, compareList);
			}

			for (Imagem imagem : mapResult.keySet()) {

				if(!imagemBase.equals(imagem)) {

					BigDecimal similaridade = mapResult.get(imagem);

					imagem.setSimilaridadeFacial(similaridade);
					imagem.setBaseFacial(imagemBase);

					Documento documento2 = imagem.getDocumento();
                    BigDecimal similaridadeFacial = new BigDecimal(100);
                    int similiarityInt = similaridade.multiply(similaridadeFacial).intValue();
                    documento2.setSimilaridadeFacial(new BigDecimal(similiarityInt));
					if(similiarityInt >= 70) {
						documento2.setStatusFacial(StatusFacial.OK);
					} else {
						documento2.setStatusFacial(StatusFacial.NOK);
					}
				}
			}
		}

		return cf;
	}
}
