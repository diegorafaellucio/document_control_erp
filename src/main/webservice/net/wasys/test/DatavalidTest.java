package net.wasys.test;

import lombok.extern.slf4j.Slf4j;
import net.wasys.util.DummyUtils;
import net.wasys.util.rest.RestClient;
import org.apache.commons.collections.MapUtils;
import org.apache.http.entity.ByteArrayEntity;
import org.aspectj.util.FileUtil;

import java.io.File;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

public class DatavalidTest {

	private static final String CONSUMER_KEY = "B_wOq0dg1OZmWAtK58YTJPWt09ga";
	private static final String CONSUMER_SECRET = "O0VbhcXaKY58_7AqSY5GxfDhnwwa";
	private static final String URL_LOGIN = "https://apigateway.serpro.gov.br/token";
	private static final String URL_FACE = "https://apigateway.serpro.gov.br/datavalid/vbeta1/validate/pf-face";
//	private static final String URL_FACE = "https://apigateway.serpro.gov.br/datavalid/vbeta1/validate/pf";

	public static void main(String[] args) throws Exception {

		/*String login = CONSUMER_KEY + ":" + CONSUMER_SECRET;
		Base64.Encoder encoder = Base64.getEncoder();
		String loginBase64 = encoder.encodeToString(login.getBytes());

		DummyUtils.systraceThread(loginBase64);

		RestClient rc1 = new RestClient(URL_LOGIN);
		rc1.setRepeatTimes(1);

		Map<String, String> headers1 = new LinkedHashMap<>();
		headers1.put("Authorization", "Basic " + loginBase64);
		rc1.setHeaders(headers1);

		ByteArrayEntity entity1 = new ByteArrayEntity("grant_type=client_credentials".getBytes());
		Map map = rc1.execute(entity1, Map.class);

		String accessToken = (String) map.get("access_token");
		DummyUtils.systraceThread(accessToken);

		RestClient rc2 = new RestClient(URL_FACE);
		rc2.setRepeatTimes(1);

		Map<String, String> headers2 = new LinkedHashMap<>();
		headers2.put("Authorization", "Bearer " + accessToken);
		headers2.put("Content-Type", "application/json");
		rc2.setHeaders(headers2);

		String foto = encoder.encodeToString(FileUtil.readAsByteArray(new File("D:\\tmp\\felipe-selfie.jpg")));

		Map<String, String> key = new LinkedHashMap<>();
		key.put("cpf", "058.162.329-08");

		Map<String, Object> answer = new LinkedHashMap<>();
		answer.put("biometria_face", foto);
		answer.put("nome", "Fulano da Silva");
		Map<String, String> cnhMap = new LinkedHashMap<>();
		cnhMap.put("data_validade", "2018-12-10");
		answer.put("cnh", cnhMap);
		Map<String, String> filiacaoMap = new LinkedHashMap<>();
		filiacaoMap.put("nome_mae", "Mônica Alves da Cruz");
		answer.put("filiacao", filiacaoMap);
		answer.put("nome", "Felipe Alves da Cruz Maschio");
		answer.put("data_nascimento", "1987-11-14");

		FaceRequest faceRequest = new FaceRequest();
		faceRequest.setKey(key);
		faceRequest.setAnswer(answer);
		String faceRequestJson = DummyUtils.objectToJson(faceRequest);

		ByteArrayEntity entity2 = new ByteArrayEntity(faceRequestJson.getBytes());
		Map map2 = rc2.execute(entity2, Map.class);
		DummyUtils.systraceThread(map2);*/
	}

	public static class FaceRequest {

		private Map<String, String> key;
		private Map<String, Object> answer;

		public Map<String, String> getKey() {
			return key;
		}
		public void setKey(Map<String, String> key) {
			this.key = key;
		}
		public Map<String, Object> getAnswer() {
			return answer;
		}
		public void setAnswer(Map<String, Object> answer) {
			this.answer = answer;
		}
	}
}
