package net.wasys.getdoc.domain.service;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.wasys.util.LogLevel;
import net.wasys.util.ddd.SpringJob;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import net.wasys.getdoc.domain.entity.FaceRecognition;
import net.wasys.getdoc.domain.entity.Imagem;
import net.wasys.getdoc.domain.enumeration.FaceRecognitionApi;
import net.wasys.util.DummyUtils;
import net.wasys.util.betaface.BetafaceRequest;
import net.wasys.util.betaface.BetafaceResponse;
import net.wasys.util.betaface.Face;
import net.wasys.util.betaface.FacesMatch;
import net.wasys.util.betaface.GetFaceImageRequest;
import net.wasys.util.betaface.GetFaceImageResponse;
import net.wasys.util.betaface.GetImageInfoRequest;
import net.wasys.util.betaface.GetImageInfoResponse;
import net.wasys.util.betaface.GetRecognizeResultRequest;
import net.wasys.util.betaface.GetRecognizeResultResponse;
import net.wasys.util.betaface.Match;
import net.wasys.util.betaface.RecognizeFacesRequest;
import net.wasys.util.betaface.RecognizeFacesResponse;
import net.wasys.util.betaface.UploadImageRequest;
import net.wasys.util.betaface.UploadImageResponse;
import net.wasys.util.other.Base64;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class FaceRecognitionBetafaceService {

	@Autowired private ResourceService resourceService;
	@Autowired private ImagemService imagemService;

	@Transactional(rollbackFor=Exception.class)
	public List<FaceRecognition> reconhecer(List<Imagem> imagens) {

		List<FaceRecognition> frList = upload(imagens);

		List<FaceRecognition> frListResult = new ArrayList<>();
		for (FaceRecognition fr : frList) {

			GetImageInfoResponse imageInfo = getImageInfo(fr);
			Face[] faces = imageInfo.getFaces();

			if(faces != null && faces.length > 0) {

				String faceUid = faces[0].getUid();
				fr.setFaceUid(faceUid);

				GetFaceImageResponse result = getFaceImage(faceUid);

				byte[] faceImage = result.getFaceImage();
				Imagem imagem = fr.getImagem();
				String camimhoFacial = imagemService.gerarCaminhoFace(imagem);
				try {
					FileUtils.writeByteArrayToFile(new File(camimhoFacial), faceImage);
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
				imagem.setCaminhoFacial(camimhoFacial);

				frListResult.add(fr);
			}
		}

		return frListResult;
	}

	private List<FaceRecognition> upload(List<Imagem> imagens) {

		final List<FaceRecognition> frList = new ArrayList<>();
		Map<Thread, SpringJob> threads = new HashMap<>();

		for (final Imagem imagem : imagens) {

			final String caminho = imagem.getCaminho();

			SpringJob job = new SpringJob() {
				@Override
				public void run() {
					executeWithoutSession();
				}
				@Override
				public void execute() throws Exception {
					FaceRecognition fr = uploadImage(caminho);
					fr.setImagem(imagem);
					frList.add(fr);
				}
			};
			Thread thread = new Thread(job);
			thread.start();
			threads.put(thread, job);
		}

		for (Thread thread : threads.keySet()) {

			try {
				thread.join();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}

			SpringJob job = threads.get(thread);
			Exception exception = job.getException();
			if(exception != null) {
				throw new RuntimeException(exception);
			}
		}
		return frList;
	}

	public Map<Imagem, BigDecimal> comparar(FaceRecognition frBase, List<FaceRecognition> compareList) {

		Map<String, Imagem> map1 = new HashMap<>();
		for (FaceRecognition fr : compareList) {
			String faceUid = fr.getFaceUid();
			Imagem imagem = fr.getImagem();
			map1.put(faceUid, imagem);
		}

		RecognizeFacesResponse result = recognizeFaces(frBase, compareList);

		String recognizeUid = result.getRecognizeUid();

		GetRecognizeResultResponse result2 = getRecognizeResult(recognizeUid);

		FacesMatch[] facesMatches = result2.getFacesMatches();
		FacesMatch faceMatch = facesMatches[0];
		Match[] matches = faceMatch.getMatches();

		Map<Imagem, BigDecimal> map2 = new HashMap<>();
		for (Match match : matches) {

			BigDecimal confidence = match.getConfidence();
			String faceUid = match.getFaceUid();
			Imagem imagem = map1.get(faceUid);

			map2.put(imagem, confidence);
		}

		return map2;
	}

	private GetRecognizeResultResponse getRecognizeResult(String recognizeUid) {

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
		String apiKey = resourceService.getValue(ResourceService.BETAFACEAPI_KEY);
		String apiSecret = resourceService.getValue(ResourceService.BETAFACEAPI_API_SECRET);

		GetRecognizeResultRequest request = new GetRecognizeResultRequest();
		request.setApiKey(apiKey);
		request.setApiSecret(apiSecret);
		request.setRecognizeUid(recognizeUid);

		GetRecognizeResultResponse result = post("http://www.betafaceapi.com/service_json.svc/GetRecognizeResult", mapper, request, GetRecognizeResultResponse.class);

		return result;
	}

	private GetFaceImageResponse getFaceImage(String faceUid) {

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
		String apiKey = resourceService.getValue(ResourceService.BETAFACEAPI_KEY);
		String apiSecret = resourceService.getValue(ResourceService.BETAFACEAPI_API_SECRET);

		GetFaceImageRequest request = new GetFaceImageRequest();
		request.setApiKey(apiKey);
		request.setApiSecret(apiSecret);
		request.setFaceUid(faceUid);

		try {
			GetFaceImageResponse result = post("http://betafaceapi.com/service_json.svc/GetFaceImage", mapper, request, GetFaceImageResponse.class);

			return result;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public GetImageInfoResponse getImageInfo(FaceRecognition fr) {

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
		String apiKey = resourceService.getValue(ResourceService.BETAFACEAPI_KEY);
		String apiSecret = resourceService.getValue(ResourceService.BETAFACEAPI_API_SECRET);
		String imgUid = fr.getImgUid();

		GetImageInfoRequest request = new GetImageInfoRequest();
		request.setApiKey(apiKey);
		request.setApiSecret(apiSecret);
		request.setImgUid(imgUid);

		GetImageInfoResponse result = post("http://www.betafaceapi.com/service_json.svc/GetImageInfo", mapper, request, GetImageInfoResponse.class);

		return result;
	}

	private FaceRecognition uploadImage(String caminho) {

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
		String apiKey = resourceService.getValue(ResourceService.BETAFACEAPI_KEY);
		String apiSecret = resourceService.getValue(ResourceService.BETAFACEAPI_API_SECRET);

		UploadImageRequest request = new UploadImageRequest();
		try {
			File imagemFile = new File(caminho);
			String imagemFileName = imagemFile.getName();
			byte[] imagemBA = FileUtils.readFileToByteArray(imagemFile);
			byte[] imagembase64 = Base64.encodeBase64(imagemBA);
			String imagembase64Str = new String(imagembase64);

			request.setApiKey(apiKey);
			request.setApiSecret(apiSecret);
			request.setImageBase64(imagembase64Str);
			request.setOriginalFilename(imagemFileName);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		UploadImageResponse response = post("http://www.betafaceapi.com/service_json.svc/UploadImage", mapper, request, UploadImageResponse.class);

		FaceRecognition fr = convert(response);
		return fr;
	}

	private RecognizeFacesResponse recognizeFaces(FaceRecognition frBase, List<FaceRecognition> compareList) {

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
		String apiKey = resourceService.getValue(ResourceService.BETAFACEAPI_KEY);
		String apiSecret = resourceService.getValue(ResourceService.BETAFACEAPI_API_SECRET);
		String faceUid = frBase.getFaceUid();
		StringBuilder targets = new StringBuilder();
		for (FaceRecognition fr : compareList) {
			String faceUid2 = fr.getFaceUid();
			targets.append(faceUid2).append(", ");
		}
		String targetsStr = targets.toString().replaceAll(", $", "");

		RecognizeFacesRequest request = new RecognizeFacesRequest();
		request.setApiKey(apiKey);
		request.setApiSecret(apiSecret);
		request.setFacesUids(faceUid);
		request.setTargets(targetsStr);

		RecognizeFacesResponse result = post("http://www.betafaceapi.com/service_json.svc/RecognizeFaces", mapper, request, RecognizeFacesResponse.class);

		return result;
	}

	private FaceRecognition convert(UploadImageResponse result) {

		String imgUid = result.getImgUid();

		FaceRecognition fr = new FaceRecognition();
		fr.setApi(FaceRecognitionApi.BETAFACE);
		fr.setImgUid(imgUid);

		return fr;
	}

	private <T extends BetafaceResponse> T post(String url, ObjectMapper mapper, BetafaceRequest request, Class<T> clazz) {

		T result = null;

		try {
			URL url2 = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) url2.openConnection();
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");

			OutputStream outputStream = connection.getOutputStream();
			mapper.writeValue(outputStream, request);

			int responseCode = connection.getResponseCode();
			if (responseCode != 200) {
				systraceThread("Falha ao acessar betaface. RespondeCode " + responseCode + ". URL: " + url, LogLevel.ERROR);
				throw new IllegalStateException("Falha ao acessar o betaface. RespondeCode " + responseCode + ". URL: " + url);
			}

			InputStream inputStream = connection.getInputStream();
			String json = IOUtils.toString(inputStream, Charset.forName("UTF-8"));

			//DummyUtils.systraceThread(url);
			//DummyUtils.systraceThread(json);

			result = mapper.readValue(json, clazz);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		int intResponse = result.getIntResponse();

		if(intResponse == 1) {
			DummyUtils.sleep(1000);
			return post(url, mapper, request, clazz);
		}

		String stringResponse = result.getStringResponse();
		if(intResponse != 0) {
			throw new RuntimeException(intResponse + " - " + stringResponse + " - " + url);
		}

		return result;
	}
}
