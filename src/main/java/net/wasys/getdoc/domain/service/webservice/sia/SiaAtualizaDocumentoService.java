package net.wasys.getdoc.domain.service.webservice.sia;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import net.wasys.getdoc.domain.entity.ConsultaExterna;
import net.wasys.getdoc.domain.enumeration.StatusConsultaExterna;
import net.wasys.getdoc.domain.service.ParametroService;
import net.wasys.getdoc.domain.service.webservice.RestWebServiceClient;
import net.wasys.getdoc.domain.vo.ConfiguracoesWsSiaVO;
import net.wasys.getdoc.domain.vo.SiaAtualizaDocumentoVO;
import net.wasys.getdoc.domain.vo.WebServiceClientVO;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.http.ProxyManager;
import net.wasys.util.rest.RestClient;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class SiaAtualizaDocumentoService extends RestWebServiceClient {

	private static final String NUMERO_INSCRICAO = "numero_inscricao";
	private static final String NUMERO_CANDIDATO = "numero_candidato";
	private static final String COD_ORIGEM = "cod_origem";

	@Autowired private ParametroService parametroService;

	@Override
	protected ConsultaExterna chamarWebService(MultiValueMap<String, Object> parametros) {

		ConfiguracoesWsSiaVO configuracoesVO = parametroService.getConfiguracaoAsObject(ParametroService.P.CONFIGURACOES_WS_SIA, ConfiguracoesWsSiaVO.class);
		String endpoint = configuracoesVO.getAtualizaDocumentoEndpoint();
		Long timeout = configuracoesVO.getAtualizaDocumentoTimeout();
		int repeatTimes = configuracoesVO.getAtualizaDocumentoRepeattimes();

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

			Map<String, String> postBody = getPostBody(parametros);

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

	private Map<String, String> getPostBody(MultiValueMap<String, Object> parametros) {
		String numInscricao = (String) parametros.getFirst(NUMERO_INSCRICAO);
		String numCandidato = (String) parametros.getFirst(NUMERO_CANDIDATO);
		String codOrigem = (String) parametros.getFirst(COD_ORIGEM);
		Map<String, String> postBody = new LinkedHashMap<>();
		postBody.put("NumSeqInscricao", numInscricao);
		postBody.put("NumSeqCandidato", numCandidato);
		postBody.put("CodMatricula", null);
		postBody.put("CodTipoDocumento", codOrigem);
		return postBody;
	}

	@Override
	protected MultiValueMap<String, Object> getParametrosApi(WebServiceClientVO vo) {
		MultiValueMap<String, Object> queryParams = new LinkedMultiValueMap<>(2);
		return queryParams;
	}

	@Override
	protected MultiValueMap<String, Object> getParametros(WebServiceClientVO vo) {

		SiaAtualizaDocumentoVO vo2 = (SiaAtualizaDocumentoVO) vo;
		MultiValueMap<String, Object> queryParams = new LinkedMultiValueMap<>(3);

		String numeroInscricao = vo2.getNumeroInscricao();
		String numeroCandidato = vo2.getNumeroCandidato();
		String codOrigem = vo2.getCodOrigem();

		queryParams.add(NUMERO_INSCRICAO, numeroInscricao);
		queryParams.add(NUMERO_CANDIDATO, numeroCandidato);
		queryParams.add(COD_ORIGEM, codOrigem);

		return queryParams;
	}

	@Override
	protected StatusConsultaExterna validarSucessoOuErro(ConsultaExterna consultaExterna, JsonNode resultadoJson) {
		JsonNode sucesso = resultadoJson.findPath("Success");
		StatusConsultaExterna valido = (!sucesso.isMissingNode() && sucesso.asText().equals("true")) ? StatusConsultaExterna.SUCESSO : StatusConsultaExterna.ERRO;
		if(StatusConsultaExterna.SUCESSO.equals(valido)) {
			JsonNode value = resultadoJson.findPath("Value");
			if(!value.isMissingNode()) {
				String valueStr = value.asText();
				valueStr = StringUtils.trim(valueStr);
				if(StringUtils.isNotBlank(valueStr) && !valueStr.startsWith("0")) {

					String parametros = consultaExterna.getParametros();
					try {
						Map<String, List<Object>> parametros2 = DummyUtils.jsonToObject(parametros, new TypeReference<Map<String, List<Object>>>() {});
						MultiValueMap<String, Object> parametros3 = new LinkedMultiValueMap<>(parametros2);
						Map<String, String> postBody = getPostBody(parametros3);
						parametros = DummyUtils.objectToJson(postBody);
					}
					catch (Exception e) {
						e.printStackTrace();
					}

					consultaExterna.setMensagem("Valor de retorno inválido: " + valueStr + ". Parâmetros: "  + parametros + ". Retorno: " + resultadoJson + ".");
					return StatusConsultaExterna.ERRO;
				}
			}
		}

		return valido;
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