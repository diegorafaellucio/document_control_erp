package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.wasys.util.DummyUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.grupodigital.facerec.DefaultSoapProxy;
import com.grupodigital.facerec.FacialRecogn;
import com.grupodigital.facerec.MatchResult;

import net.wasys.getdoc.domain.entity.FaceRecognition;
import net.wasys.getdoc.domain.entity.Imagem;
import net.wasys.getdoc.domain.enumeration.FaceRecognitionApi;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class FaceRecognitionGrupoDigitalService {

	@Autowired private ResourceService resourceService;
	@Autowired private ImagemService imagemService;

	public List<FaceRecognition> reconhecer(List<Imagem> imagens) {

		systraceThread("FaceRecognitionGrupoDigitalService.reconhecer()");

		try {
			String endpoint = resourceService.getValue(ResourceService.FACE_RECOGNITION_ENDPOINT);
			DefaultSoapProxy proxy = new DefaultSoapProxy(endpoint);

			List<FaceRecognition> result = new ArrayList<>();
			for (Imagem imagem2 : imagens) {

				String caminho = imagem2.getCaminho();
				FacialRecogn faceRecong = proxy.getFaceImage(caminho);

				FaceRecognition fr = convert(faceRecong);
				fr.setImagem(imagem2);

				byte[] faceImage = fr.getFaceImage();
				String caminhoFacial = imagemService.gerarCaminhoFace(imagem2);
				if(faceImage != null) {

					try {
						FileUtils.writeByteArrayToFile(new File(caminhoFacial), faceImage);
					} catch (Exception e) {
						e.printStackTrace();
						throw new RuntimeException(e);
					}
					imagem2.setCaminhoFacial(caminhoFacial);

					result.add(fr);
				}
			}

			return result;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public Map<Imagem, BigDecimal> comparar(FaceRecognition frBase, List<FaceRecognition> compareList) {

		FacialRecogn imagePrincipal = convert(frBase);
		FacialRecogn[] compareList2 = new FacialRecogn[compareList.size()];
		Map<String, Imagem> map = new HashMap<>();

		for (int i = 0; i < compareList2.length; i++) {

			FaceRecognition fr = compareList.get(i);
			FacialRecogn fr2 = convert(fr);
			compareList2[i] = fr2;

			String imageFileName = fr2.getImageFileName();
			Imagem imagem = fr.getImagem();
			map.put(imageFileName, imagem);
		}

		Map<Imagem, BigDecimal> mapResult = new HashMap<>();

		try {
			String endpoint = resourceService.getValue(ResourceService.FACE_RECOGNITION_ENDPOINT);
			DefaultSoapProxy proxy = new DefaultSoapProxy(endpoint);

			MatchResult[] match = proxy.getMatch(imagePrincipal, compareList2);

			for (MatchResult matchResult : match) {

				String imageSimiliarity = matchResult.getImageSimiliarity();
				float similiarity = matchResult.getSimiliarity();
				BigDecimal similiarityBD = new BigDecimal(String.valueOf(similiarity));

				Imagem imagem = map.get(imageSimiliarity);
				mapResult.put(imagem, similiarityBD);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		return mapResult;
	}

	private FacialRecogn convert(FaceRecognition faceRecong) {

		int eye1x = faceRecong.getEye1X();
		int eye1y = faceRecong.getEye1Y();
		int eye2x = faceRecong.getEye2X();
		int eye2y = faceRecong.getEye2Y();
		int faceListId = faceRecong.getFaceListId();
		double facePositionAngle = faceRecong.getFacePositionAngle();
		int facePositionW = faceRecong.getFacePositionW();
		int facePositionXc = faceRecong.getFacePositionXc();
		int facePositionYc = faceRecong.getFacePositionYc();
		String imageFileName = faceRecong.getImagePath();
		byte[] image = faceRecong.getImage();
		byte[] faceImage = faceRecong.getFaceImage();
		byte[] imageRecogn = faceRecong.getImageRecogn();
		byte[] template = faceRecong.getTemplate();

		FacialRecogn fr = new FacialRecogn();
		fr.setEye1X(eye1x);
		fr.setEye1Y(eye1y);
		fr.setEye2X(eye2x);
		fr.setEye2Y(eye2y);
		fr.setFaceListId(faceListId);
		fr.setFacePositionAngle(facePositionAngle);
		fr.setFacePositionW(facePositionW);
		fr.setFacePositionXc(facePositionXc);
		fr.setFacePositionYc(facePositionYc);
		fr.setImageFileName(imageFileName);
		fr.setImage(image);
		fr.setFaceImage(faceImage);
		fr.setImageRecogn(imageRecogn);
		fr.setTemplate(template);

		return fr;
	}

	private FaceRecognition convert(FacialRecogn faceRecong) {

		int eye1x = faceRecong.getEye1X();
		int eye1y = faceRecong.getEye1Y();
		int eye2x = faceRecong.getEye2X();
		int eye2y = faceRecong.getEye2Y();
		int faceListId = faceRecong.getFaceListId();
		double facePositionAngle = faceRecong.getFacePositionAngle();
		int facePositionW = faceRecong.getFacePositionW();
		int facePositionXc = faceRecong.getFacePositionXc();
		int facePositionYc = faceRecong.getFacePositionYc();
		String imageFileName = faceRecong.getImageFileName();
		byte[] image = faceRecong.getImage();
		byte[] faceImage = faceRecong.getFaceImage();
		byte[] imageRecogn = faceRecong.getImageRecogn();
		byte[] template = faceRecong.getTemplate();

		FaceRecognition fr = new FaceRecognition();
		fr.setApi(FaceRecognitionApi.POLICIAL_TECH);
		fr.setEye1X(eye1x);
		fr.setEye1Y(eye1y);
		fr.setEye2X(eye2x);
		fr.setEye2Y(eye2y);
		fr.setFaceListId(faceListId);
		fr.setFacePositionAngle(facePositionAngle);
		fr.setFacePositionW(facePositionW);
		fr.setFacePositionXc(facePositionXc);
		fr.setFacePositionYc(facePositionYc);
		fr.setImagePath(imageFileName);
		fr.setImage(image);
		fr.setFaceImage(faceImage);
		fr.setImageRecogn(imageRecogn);
		fr.setTemplate(template);

		return fr;
	}
}
