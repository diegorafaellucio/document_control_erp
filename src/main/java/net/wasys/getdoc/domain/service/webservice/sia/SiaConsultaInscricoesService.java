package net.wasys.getdoc.domain.service.webservice.sia;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import net.wasys.util.LogLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.JsonNode;

import net.wasys.getdoc.domain.entity.ConsultaExterna;
import net.wasys.getdoc.domain.enumeration.StatusConsultaExterna;
import net.wasys.getdoc.domain.service.ParametroService;
import net.wasys.getdoc.domain.service.webservice.RestWebServiceClient;
import net.wasys.getdoc.domain.vo.ConfiguracoesWsSiaVO;
import net.wasys.getdoc.domain.vo.SiaConsultaInscricoesVO;
import net.wasys.getdoc.domain.vo.WebServiceClientVO;
import net.wasys.util.DummyUtils;
import net.wasys.util.http.ProxyManager;
import net.wasys.util.rest.RestClient;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class SiaConsultaInscricoesService extends RestWebServiceClient {

	private static final String NUMERO_INSCRICAO = "numero_inscricao";
	private static final String NUMERO_CANDIDATO = "numero_candidato";
	private static final String CPF_ALUNO = "cpf_aluno";

	@Autowired private ParametroService parametroService;

	@Override
	protected ConsultaExterna chamarWebService(MultiValueMap<String, Object> parametros) {

		ConfiguracoesWsSiaVO configuracoesVO = parametroService.getConfiguracaoAsObject(ParametroService.P.CONFIGURACOES_WS_SIA, ConfiguracoesWsSiaVO.class);
		String endpoint = configuracoesVO.getConsultaCpfEndPoint();
		Long timetout = configuracoesVO.getConsultaCpfTimeout();
		int repeatTimes = configuracoesVO.getConsultaCpfRepeattimes();
		String apiKey = configuracoesVO.getApiKey();

		String resultadoJson = null;
		String stackTrace = null;
		String mensagem = null;

		ProxyManager proxyManager = resourceService.getProxyManager();

		long fim;
		long inicio = System.currentTimeMillis();

		try {
			Map<String, String> headers = new HashMap<>();
			headers.put("x-api-key", apiKey);

			RestClient restClient = new RestClient(endpoint);
			restClient.setHeaders(headers);
			restClient.setRepeatTimes(repeatTimes);
			restClient.setTimeout(timetout);
			restClient.setProxyManager(proxyManager);

			String numInscricao = (String) parametros.getFirst(NUMERO_INSCRICAO);
			Map<String, String> postBody = new LinkedHashMap<>();
			postBody.put("NumSeqInscricao", numInscricao);
			String numCadidato = (String) parametros.getFirst(NUMERO_CANDIDATO);
			postBody.put("NumSeqCandidato", numCadidato);
			String cpfAluno = (String) parametros.getFirst(CPF_ALUNO);
			postBody.put("CpfAluno", cpfAluno);

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

		SiaConsultaInscricoesVO vo2 = (SiaConsultaInscricoesVO) vo;
		MultiValueMap<String, Object> queryParams = new LinkedMultiValueMap<>(3);

		String numeroInscricao = vo2.getNumeroInscricao();
		String numeroCandidato = vo2.getNumeroCandidato();
		String cpfAluno = vo2.getCpfAluno();

		queryParams.add(NUMERO_INSCRICAO, numeroInscricao);
		queryParams.add(NUMERO_CANDIDATO, numeroCandidato);
		queryParams.add(CPF_ALUNO, cpfAluno);

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
		String endpoint = configuracoesVO.getConsultaComprovanteInscricaoEndpoint();
		return endpoint;
	}

	@Override
	protected boolean isPost() {
		return true;
	}
}
