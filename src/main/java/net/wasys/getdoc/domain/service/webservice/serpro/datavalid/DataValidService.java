package net.wasys.getdoc.domain.service.webservice.serpro.datavalid;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.wasys.getdoc.domain.entity.ConsultaExterna;
import net.wasys.getdoc.domain.enumeration.StatusConsultaExterna;
import net.wasys.getdoc.domain.service.ParametroService;
import net.wasys.getdoc.domain.service.webservice.RestWebServiceClient;
import net.wasys.getdoc.domain.vo.ConfiguracoesWsDataValidVO;
import net.wasys.getdoc.domain.vo.DataValidBiometriaRequestVO;
import net.wasys.getdoc.domain.vo.DataValidRequestVO;
import net.wasys.getdoc.domain.vo.WebServiceClientVO;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.rest.RestClient;
import org.apache.http.entity.ByteArrayEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class DataValidService extends RestWebServiceClient {

	@Autowired private ParametroService parametroService;

	@Override
	protected ConsultaExterna chamarWebService(MultiValueMap<String, Object> parametros) {

		ConfiguracoesWsDataValidVO cwid = parametroService.getConfiguracaoAsObject(ParametroService.P.CONFIGURACOES_WS_DATAVALID, ConfiguracoesWsDataValidVO.class);
		String resultadoJson = null;
		String stackTrace = null;
		String mensagem = null;

		try {
			String accessToken = login(cwid);

			resultadoJson = chamarWebService(cwid, accessToken, parametros);
		}
		catch (RestClient.RestException e) {
			String release = e.getRelease();
			if(DummyUtils.isJson(release)) {
				resultadoJson = "{\"errors\": " + release + "}";
			}
			else {
				mensagem = DummyUtils.getExceptionMessage(e);
				mensagem = "Erro ao chamar WebService: " + mensagem;
			}
		}
		catch (Exception e) {
			mensagem = DummyUtils.getExceptionMessage(e);
			mensagem = "Erro ao chamar WebService: " + mensagem;
			systraceThread(mensagem, LogLevel.ERROR);
			e.printStackTrace();
			stackTrace = DummyUtils.getStackTrace(e);
		}

		ConsultaExterna consultaExterna = new ConsultaExterna();
		consultaExterna.setResultado(resultadoJson);
		consultaExterna.setStackTrace(stackTrace);
		consultaExterna.setMensagem(mensagem);
		return consultaExterna;
	}

	protected String login(ConfiguracoesWsDataValidVO cwid) throws Exception {

		String endpointLogin = cwid.getEndpointLogin();

		String consumerKey = cwid.getConsumerKey();
		String consumerSecret = cwid.getConsumerSecret();
		String login = consumerKey + ":" + consumerSecret;
		Base64.Encoder encoder = Base64.getEncoder();
		String loginBase64 = encoder.encodeToString(login.getBytes());

		RestClient rc1 = new RestClient(endpointLogin);

		Map<String, String> headers1 = new LinkedHashMap<>();
		headers1.put("Authorization", "Basic " + loginBase64);
		rc1.setHeaders(headers1);

		ByteArrayEntity entity1 = new ByteArrayEntity("grant_type=client_credentials".getBytes());
		Map map = rc1.execute(entity1, Map.class);

		String accessToken = (String) map.get("access_token");
		return accessToken;
	}

	protected String chamarWebService(ConfiguracoesWsDataValidVO cwid, String accessToken, MultiValueMap<String, Object> parametros) throws Exception {

		String cpf = (String) parametros.getFirst(DataValidBiometriaRequestVO.CPF);
		Date dataValidadeCnh = (Date) parametros.getFirst(DataValidBiometriaRequestVO.DATA_VALIDADE_CNH);
		String dataValidadeCnhStr = DummyUtils.format(dataValidadeCnh, "yyyy-MM-dd");
		String nome = (String) parametros.getFirst(DataValidBiometriaRequestVO.NOME);
		nome = DummyUtils.substituirCaracteresEspeciais(nome);
		Date dataNascimento = (Date) parametros.getFirst(DataValidBiometriaRequestVO.DATA_NASCIMENTO);
		String dataNascimentoStr = DummyUtils.format(dataNascimento, "yyyy-MM-dd");
		String nomeMaeFinanciado = (String) parametros.getFirst(DataValidBiometriaRequestVO.NOME_MAE);
		nomeMaeFinanciado = DummyUtils.substituirCaracteresEspeciais(nomeMaeFinanciado);

		String endpoint = cwid.getEndpoint();

		RestClient rc2 = new RestClient(endpoint);

		Map<String, String> headers2 = new LinkedHashMap<>();
		headers2.put("Authorization", "Bearer " + accessToken);
		headers2.put("Content-Type", "application/json");
		rc2.setHeaders(headers2);

		Map<String, String> key = new LinkedHashMap<>();
		key.put("cpf", cpf);

		Map<String, Object> answer = new LinkedHashMap<>();
		Map<String, String> cnhMap = new LinkedHashMap<>();
		cnhMap.put("data_validade", dataValidadeCnhStr);
		answer.put("cnh", cnhMap);
		Map<String, String> filiacaoMap = new LinkedHashMap<>();
		filiacaoMap.put("nome_mae", nomeMaeFinanciado);
		answer.put("filiacao", filiacaoMap);
		answer.put("nome", nome);
		answer.put("data_nascimento", dataNascimentoStr);

		FaceRequest faceRequest = new FaceRequest();
		faceRequest.setKey(key);
		faceRequest.setAnswer(answer);
		String faceRequestJson = DummyUtils.objectToJson(faceRequest);

		ByteArrayEntity entity2 = new ByteArrayEntity(faceRequestJson.getBytes());
		return rc2.execute(entity2, String.class);
	}

	@Override
	protected MultiValueMap<String, Object> getParametrosApi(WebServiceClientVO vo) {
		return null;
	}

	@Override
	protected MultiValueMap<String, Object> getParametros(WebServiceClientVO vo) {

		DataValidRequestVO vo2 = (DataValidRequestVO) vo;
		String cpf = vo2.getCpf();
		Date dataValidadeCnh = vo2.getDataValidadeCnh();
		String nome = vo2.getNome();
		Date dataNascimento = vo2.getDataNascimento();
		String nomeMae = vo2.getNomeMae();

		MultiValueMap<String, Object> parametros = new LinkedMultiValueMap<>(3);
		parametros.add(DataValidBiometriaRequestVO.CPF, cpf);
		parametros.add(DataValidBiometriaRequestVO.DATA_VALIDADE_CNH, dataValidadeCnh);
		parametros.add(DataValidBiometriaRequestVO.NOME, nome);
		parametros.add(DataValidBiometriaRequestVO.DATA_NASCIMENTO, dataNascimento);
		parametros.add(DataValidBiometriaRequestVO.NOME_MAE, nomeMae);
		return parametros;
	}

	@Override
	protected boolean isPost() {
		return true;
	}

	@Override
	protected String montarUrl(MultiValueMap<String, Object> parametrosAPI) {
		return null;
	}

	@Override
	protected StatusConsultaExterna validarSucessoOuErro(ConsultaExterna consultaExterna, JsonNode resultadoJson) {
		JsonNode nomeSimilaridade = resultadoJson.findPath("nome_similaridade");
		if(nomeSimilaridade.isMissingNode()) {
			return StatusConsultaExterna.ERRO;
		}
		return StatusConsultaExterna.SUCESSO;
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
