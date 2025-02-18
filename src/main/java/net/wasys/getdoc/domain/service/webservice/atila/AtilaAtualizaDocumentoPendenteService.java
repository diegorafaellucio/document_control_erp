package net.wasys.getdoc.domain.service.webservice.atila;

import com.fasterxml.jackson.databind.JsonNode;
import net.wasys.getdoc.domain.entity.ConsultaExterna;
import net.wasys.getdoc.domain.enumeration.StatusConsultaExterna;
import net.wasys.getdoc.domain.service.ParametroService;
import net.wasys.getdoc.domain.service.webservice.RestWebServiceClient;
import net.wasys.getdoc.domain.vo.*;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.http.ProxyManager;
import net.wasys.util.rest.RestClient;
import net.wasys.util.rest.jackson.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class AtilaAtualizaDocumentoPendenteService extends RestWebServiceClient {

	private static final String NUMERO_CANDIDATO = "NUM_SEQ_CANDIDATO";
	private static final String GRANT_TYPE = "grant_type";
	private static final String CLIENT_ID = "client_id";
	private static final String CLIENT_SECRET = "client_secret";
	private static final String ACCOUNT_ID = "account_id";
	private static final String ID_DOCUMENTO = "ID_DOCUMENTO";
	private static final String NOM_DOCUMENTO = "NOM_DOCUMENTO";
	private static final String MOTIVO_REJEICAO = "MOTIVO_REJEICAO";
	private static final String DATA_REJEICAO = "DATA_REJEICAO";
	private static final String HORA_REJEICAO = "HORA_REJEICAO";
	private static final String JSON_ARRAY = "JSON_ARRAY";
	private static final String ACCESS_TOKEN = "access_token";

	@Autowired private ParametroService parametroService;

	@Override
	protected ConsultaExterna chamarWebService(MultiValueMap<String, Object> parametros) {

		ConfiguracoesWsAtilaVO configuracoesVO = parametroService.getConfiguracaoAsObject(ParametroService.P.CONFIGURACOES_WS_ATILA, ConfiguracoesWsAtilaVO.class);
		String endpoint = configuracoesVO.getEndpointNotificacao();

		String resultadoJson = null;
		String stackTrace = null;
		String mensagem = null;

		ProxyManager proxyManager = resourceService.getProxyManager();

		long inicio = System.currentTimeMillis();
		long fim;

		try {

			Map<String, String> postBody = getPostBody(parametros);
			String accessToken = postBody.get(ACCESS_TOKEN);
			String jsonArray = postBody.get(JSON_ARRAY);

			Map<String, String> headers = new HashMap<>();
			headers.put("authorization", "Bearer " + accessToken);

			RestClient restClient = new RestClient(endpoint);
			restClient.setHeaders(headers);
			restClient.setProxyManager(proxyManager);

			systraceThread("proxy: " + proxyManager + " URL: " + endpoint + " parans: " + jsonArray);
			resultadoJson = restClient.execute(jsonArray, String.class);
			//DummyUtils.systraceThread("release: " + resultadoJson);
		}
		catch (Exception e) {
			mensagem = DummyUtils.getExceptionMessage(e);
			mensagem = String.format("Erro ao chamar Rest WebService: %s", mensagem);
			systraceThread(mensagem, LogLevel.ERROR);
			e.printStackTrace();

			stackTrace = DummyUtils.getStackTrace(e);
		}
		finally {
			fim = System.currentTimeMillis();
		}

		ConsultaExterna consultaExterna = new ConsultaExterna();
		consultaExterna.setResultado(resultadoJson);
		consultaExterna.setStackTrace(stackTrace);
		consultaExterna.setMensagem(mensagem);
		consultaExterna.setTempoExecucao(fim - inicio);
		return consultaExterna;
	}

	private Map<String, String> getPostBody(MultiValueMap<String, Object> parametros) {
		String jsonArray = (String) parametros.getFirst(JSON_ARRAY);
		String accessToken = (String) parametros.getFirst(ACCESS_TOKEN);

		Map<String, String> postBody = new LinkedHashMap<>();
		postBody.put(ACCESS_TOKEN, accessToken);
		postBody.put(JSON_ARRAY, jsonArray);
		return postBody;
	}

	public JSONObject getJson(AtilaAtualizaDocumentoVO vo, boolean isKey) {
		JSONObject json = new JSONObject();
		if(isKey) {
			String numeroCandidato = vo.getNumeroCandidato();
			Long documentoId = vo.getDocumentoId();
			json.put(NUMERO_CANDIDATO, numeroCandidato);
			json.put(ID_DOCUMENTO, documentoId);
		} else {
			String documentoNome = vo.getDocumentoNome();
			String irregularidadeNome = vo.getIrregularidadeNome();
			String dataPendencia = vo.getDataPendencia();
			String horaPendencia = vo.getHoraPendencia();
			json.put(NOM_DOCUMENTO, documentoNome);
			json.put(MOTIVO_REJEICAO, irregularidadeNome);
			json.put(DATA_REJEICAO, dataPendencia);
			json.put(HORA_REJEICAO, horaPendencia);
		}

		return json;
	}

	@Override
	protected MultiValueMap<String, Object> getParametrosApi(WebServiceClientVO vo) {
		MultiValueMap<String, Object> queryParams = new LinkedMultiValueMap<>(2);
		return queryParams;
	}

	@Override
	protected MultiValueMap<String, Object> getParametros(WebServiceClientVO vo) {

		AtilaAtualizaDocumentoVO vo1 = (AtilaAtualizaDocumentoVO) vo;
		MultiValueMap<String, Object> queryParams = new LinkedMultiValueMap<>(3);

		String documentosJson = vo1.getDocumentosJson();
		String accessToken = vo1.getAccessToken();

		queryParams.add(JSON_ARRAY, documentosJson);
		queryParams.add(ACCESS_TOKEN, accessToken);

		return queryParams;
	}

	@Override
	protected StatusConsultaExterna validarSucessoOuErro(ConsultaExterna consultaExterna, JsonNode resultadoJson) {
		JsonNode sucesso = resultadoJson.findPath("keys");
		JsonNode nUM_seq_candidato = sucesso.findPath("nUM_SEQ_CANDIDATO");
		String numCandidato = nUM_seq_candidato.asText();
		StatusConsultaExterna valido = StringUtils.isNotBlank(numCandidato) ? StatusConsultaExterna.SUCESSO : StatusConsultaExterna.ERRO;

		return valido;
	}

	public String autenticarNoAtila() {

		String resultadoJson;
		String mensagem;

		ConfiguracoesWsAtilaVO configuracoesVO = parametroService.getConfiguracaoAsObject(ParametroService.P.CONFIGURACOES_WS_ATILA, ConfiguracoesWsAtilaVO.class);

		ProxyManager proxyManager = resourceService.getProxyManager();
		String grantType = configuracoesVO.getGrantType();
		String clientId = configuracoesVO.getClientId();
		String clientSecret = configuracoesVO.getClientSecret();
		String accountId = configuracoesVO.getAccountId();
		String endpointAutenticacao = configuracoesVO.getEndpointAutenticacao();


		String tokenStr = "";

		try{

			RestClient restClient = new RestClient(endpointAutenticacao);
			restClient.setProxyManager(proxyManager);

			Map<String, String> postBody = new LinkedHashMap<>();
			postBody.put(GRANT_TYPE, grantType);
			postBody.put(CLIENT_ID, clientId);
			postBody.put(CLIENT_SECRET, clientSecret);
			postBody.put(ACCOUNT_ID, accountId);

			resultadoJson = restClient.execute(postBody, String.class);
			systraceThread("release: " + resultadoJson);

			ObjectMapper om = new ObjectMapper();
			JsonNode jsonNode = om.readTree(resultadoJson);
			JsonNode token = jsonNode.findPath("access_token");
			tokenStr = token.asText();
		} catch (Exception e) {
			mensagem = DummyUtils.getExceptionMessage(e);
			mensagem = String.format("Erro ao chamar Rest WebService: %s", mensagem);
			DummyUtils.systraceThread(mensagem, LogLevel.FATAL);
			e.printStackTrace();
		}

		return tokenStr;
	}

	@Override
	protected String montarUrl(MultiValueMap<String, Object> parametrosAPI) {
		ConfiguracoesWsSiaVO configuracoesVO = parametroService.getConfiguracaoAsObject(ParametroService.P.CONFIGURACOES_WS_SIA, ConfiguracoesWsSiaVO.class);
		String endpoint = configuracoesVO.getAtualizaDocumentoEndpoint();
		return endpoint;
	}

	@Override
	protected boolean isPost() {
		return true;
	}
}