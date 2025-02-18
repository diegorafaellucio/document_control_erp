package net.wasys.getdoc.domain.service.webservice.sia;

import com.fasterxml.jackson.databind.JsonNode;
import net.wasys.getdoc.domain.entity.ConsultaExterna;
import net.wasys.getdoc.domain.enumeration.StatusConsultaExterna;
import net.wasys.getdoc.domain.service.ParametroService;
import net.wasys.getdoc.domain.service.webservice.RestWebServiceClient;
import net.wasys.getdoc.domain.vo.ConfiguracoesWsSiaVO;
import net.wasys.getdoc.domain.vo.SiaConsultaLinhaTempoVO;
import net.wasys.getdoc.domain.vo.WebServiceClientVO;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.http.ProxyManager;
import net.wasys.util.rest.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class SiaConsultaLinhaTempoService extends RestWebServiceClient {

	private static final String NUMERO_INSCRICAO = "numero_inscricao";
	private static final String NUMERO_CANDIDATO = "numero_candidato";

	@Autowired private ParametroService parametroService;

	@Override
	protected ConsultaExterna chamarWebService(MultiValueMap<String, Object> parametros) {

		ConfiguracoesWsSiaVO configuracoesVO = parametroService.getConfiguracaoAsObject(ParametroService.P.CONFIGURACOES_WS_SIA, ConfiguracoesWsSiaVO.class);
		String endpoint = configuracoesVO.getConsultaLinhaTempoEndpoint();
		Long timeout = configuracoesVO.getConsultaLinhaTempoTimeout();
		int repeatTimes = configuracoesVO.getConsultaLinhaTempoRepeattimes();
		String apiKey = configuracoesVO.getApiKey();

		String resultadoJson = null;
		String stackTrace = null;
		String mensagem = null;

		ProxyManager proxyManager = resourceService.getProxyManager();

		long inicio = System.currentTimeMillis();
		long fim;

		try {
			Map<String, String> headers = new HashMap<>();
			headers.put("x-api-key", apiKey);

			RestClient restClient = new RestClient(endpoint);
			restClient.setRepeatTimes(repeatTimes);
			restClient.setTimeout(timeout);
			restClient.setHeaders(headers);
			restClient.setProxyManager(proxyManager);

			Map<String, String> postBody = new LinkedHashMap<>();
			String numInscricao = (String) parametros.getFirst(NUMERO_INSCRICAO);
			String numCadidato = (String) parametros.getFirst(NUMERO_CANDIDATO);
			postBody.put("NumSeqInscricao", numInscricao);
			postBody.put("NumSeqCandidato", numCadidato);

			systraceThread("proxy: " + proxyManager + " URL: " + endpoint + " parans: " + postBody);
			resultadoJson = restClient.execute(postBody, String.class);
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

	@Override
	protected MultiValueMap<String, Object> getParametrosApi(WebServiceClientVO vo) {
		MultiValueMap<String, Object> queryParams = new LinkedMultiValueMap<>(2);
		return queryParams;
	}

	@Override
	protected MultiValueMap<String, Object> getParametros(WebServiceClientVO vo) {

		SiaConsultaLinhaTempoVO vo2 = (SiaConsultaLinhaTempoVO) vo;
		MultiValueMap<String, Object> queryParams = new LinkedMultiValueMap<>(3);

		String numeroInscricao = vo2.getNumeroInscricao();
		String numeroCandidato = vo2.getNumeroCandidato();

		queryParams.add(NUMERO_INSCRICAO, numeroInscricao);
		queryParams.add(NUMERO_CANDIDATO, numeroCandidato);

		return queryParams;
	}

	@Override
	protected StatusConsultaExterna validarSucessoOuErro(ConsultaExterna consultaExterna, JsonNode resultadoJson) {
		JsonNode sucesso = resultadoJson.findPath("Success");
		return (!sucesso.isMissingNode() && sucesso.asText().equals("true") ) ? StatusConsultaExterna.SUCESSO : StatusConsultaExterna.ERRO;
	}

	@Override
	protected String montarUrl(MultiValueMap<String, Object> parametrosAPI) {
		ConfiguracoesWsSiaVO configuracoesVO = parametroService.getConfiguracaoAsObject(ParametroService.P.CONFIGURACOES_WS_SIA, ConfiguracoesWsSiaVO.class);
		String endpoint = configuracoesVO.getConsultaLinhaTempoEndpoint();
		return endpoint;
	}

	@Override
	protected boolean isPost() {
		return true;
	}
}
