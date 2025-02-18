package net.wasys.getdoc.domain.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.wasys.getdoc.GetdocConstants;
import net.wasys.getdoc.domain.vo.FileVO;
import net.wasys.util.DummyUtils;
import net.wasys.util.google.cloud.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class VisionApiService {

	@Autowired private ResourceService resourceService;

	public static void main(String[] args) {

		long inicio = System.currentTimeMillis();
		systraceThread("...");

		List<FileVO> arquivos = new ArrayList<>();
		arquivos.add(new FileVO(new File("D:\\Google Drive\\Wasys\\Documentos Teste\\cnh-cel.jpg")));
		arquivos.add(new FileVO(new File("D:\\Google Drive\\Wasys\\Documentos Teste\\cnh-fabiano.jpg")));

		VisionApiService visionApiService = new VisionApiService();
		visionApiService.detectingText(arquivos, "AIzaSyBeLMJ0fDneulrkZqvnKVYtlbSPKgDHfF8");
		for (FileVO fileVO : arquivos) {
			systraceThread(fileVO.getFile().getName() + "--------------------");
			//systraceThread(fileVO.getText());
			systraceThread("");
		}

		long fim = System.currentTimeMillis();
		systraceThread("fim: " + (fim - inicio) + "ms.");
	}

	public void detectingText(List<FileVO> arquivos) {

		String key = resourceService.getValue(ResourceService.VISION_PROCESSOR_KEY);
		detectingText(arquivos, key);
	}

	private void detectingText(List<FileVO> arquivos, String key) {

		if(StringUtils.isBlank(key)) {
			throw new IllegalArgumentException("A Chave de API do google vision é necessária. (parametro key)");
		}

		List<FileVO> arquivos2 = new ArrayList<>();
		for (FileVO fileVO : arquivos) {
			if(fileVO != null) {
				File file = fileVO.getFile();
				String fileName = file.getName();
				String extensao = DummyUtils.getExtensao(fileName);
				if(GetdocConstants.IMAGEM_EXTENSOES.contains(extensao)) {
					arquivos2.add(fileVO);
				}
			}
		}

		if(arquivos2.isEmpty()) {
			systraceThread("nenhum arquivo de imagem informados!!! " + arquivos);
			return;
		}

		systraceThread("arquivos: " + arquivos2);

		do {
			List<FileVO> arquivos3 = new ArrayList<>();
			for (int i = 0; i < 5 && !arquivos2.isEmpty(); i++) {
				FileVO id = arquivos2.remove(0);
				arquivos3.add(id);
			}

			try {
				ObjectMapper mapper = new ObjectMapper();
				mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

				HttpURLConnection conn = post(arquivos3, key, mapper);

				read(arquivos3, mapper, conn);
			}
			catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		while(!arquivos2.isEmpty());
	}

	private List<AnnotateImageResponse> read(List<FileVO> arquivos, ObjectMapper mapper, HttpURLConnection conn) throws IOException {

		systraceThread("... " + arquivos);

		int responseCode = conn.getResponseCode();
		if (responseCode == 200) {

			InputStream inputStream = conn.getInputStream();
			BatchAnnotateImageResponse batchResponse = mapper.readValue(inputStream, BatchAnnotateImageResponse.class);

			List<AnnotateImageResponse> responses = readOk(arquivos, batchResponse);

			return responses;
		}
		else {

			InputStream inputStream = conn.getErrorStream();

			String msg = "Falha ao detectarTexto das imagens. RespondeCode " + responseCode + ".";
			systraceThread(msg + " " + conn.getURL());
			String string = IOUtils.toString(inputStream, "UTF-8");
			systraceThread(string);
			throw new IllegalStateException(msg);
		}
	}

	private List<AnnotateImageResponse> readOk(List<FileVO> arquivos, BatchAnnotateImageResponse batchResponse) {

		List<AnnotateImageResponse> responses = batchResponse.getResponses();

		for (int i = 0; i < responses.size(); i++) {

			AnnotateImageResponse response = responses.get(i);

			//List<EntityAnnotation> textAnnotations = response.getTextAnnotations();
			//for (EntityAnnotation entityAnnotation : textAnnotations) {
			//	String description = entityAnnotation.getDescription();
			//	sysout(description);
			//	BoundingPoly boundingPoly = entityAnnotation.getBoundingPoly();
			//	sysout(boundingPoly.getVertices());
			//}

			FileVO fileVO = arquivos.get(i);
			TextAnnotation fullText = response.getFullTextAnnotation();
			String text = fullText != null ? fullText.getText() : null;
			fileVO.setText(text);
		}

		return responses;
	}

	private HttpURLConnection post(List<FileVO> arquivos, String key, ObjectMapper mapper) throws IOException {

		String endpoint = String.format("https://vision.googleapis.com/v1/images:annotate?alt=%1$s&key=%2$s", "json", key);

		String proxyHost = resourceService.getValue(ResourceService.PROXY_HOST);
		Integer proxyPort = resourceService.getValue(ResourceService.PROXY_PORT, Integer.class);
		Socket socket = new Socket();
		try {
			URL endpoint2 = new URL(null, endpoint);

			HttpURLConnection connection = null;
			if(StringUtils.isNotBlank(proxyHost)) {
				SocketAddress sockaddr = new InetSocketAddress(proxyHost, proxyPort);
				socket.connect(sockaddr, 10000);
				Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(socket.getInetAddress(), proxyPort));
				systraceThread("configurando proxy. host: " + proxyHost + " port: " + proxyPort + " endpoint: " + endpoint);
				connection = (HttpURLConnection) endpoint2.openConnection(proxy);
			}
			else {
				connection = (HttpURLConnection) endpoint2.openConnection();
			}

			connection.setConnectTimeout(20 * 1000);
			connection.setReadTimeout(20 * 1000);
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");

			BatchAnnotateImageRequest batchRequest = new BatchAnnotateImageRequest();

			for (FileVO fileVO : arquivos) {

				File file = fileVO.getFile();

				byte[] bytes = FileUtils.readFileToByteArray(file);
				String base64 = Base64.getEncoder().encodeToString(bytes);

				AnnotateImageRequest request = new AnnotateImageRequest();
				request.setImage(new Image(base64));
				request.add(new Feature(Type.TEXT_DETECTION));

				batchRequest.add(request);
			}

			OutputStream outputStream = connection.getOutputStream();
			mapper.writeValue(outputStream, batchRequest);

			systraceThread("post executado com sucesso.");

			return connection;
		}
		finally {
			/*TODO rever se isso é necessário
			try {
				socket.close();
			}
			catch (Exception e) {e.printStackTrace();}*/
		}
	}
}
