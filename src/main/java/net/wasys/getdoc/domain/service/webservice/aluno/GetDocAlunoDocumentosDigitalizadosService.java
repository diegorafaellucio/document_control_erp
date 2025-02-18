package net.wasys.getdoc.domain.service.webservice.aluno;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import net.wasys.getdoc.domain.entity.ConsultaExterna;
import net.wasys.getdoc.domain.enumeration.StatusConsultaExterna;
import net.wasys.getdoc.domain.service.ParametroService;
import net.wasys.getdoc.domain.service.ParametroService.P;
import net.wasys.getdoc.domain.service.webservice.RestWebServiceClient;
import net.wasys.getdoc.domain.vo.AlunoConsultaVO;
import net.wasys.getdoc.domain.vo.ConfiguracoesWsGetDocAlunoVO;
import net.wasys.getdoc.domain.vo.WebServiceClientVO;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.crawler.Crawler;
import net.wasys.util.other.Criptografia;
import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class GetDocAlunoDocumentosDigitalizadosService extends RestWebServiceClient {

	private static final String CAMPO_QUANTIDADE = "quantidade";

	@Autowired private ParametroService parametroService;

	@Override
	protected ConsultaExterna chamarWebService(MultiValueMap<String, Object> parametros) {

		ConfiguracoesWsGetDocAlunoVO configuracoesVO = parametroService.getConfiguracaoAsObject(P.CONFIGURACOES_WS_ALUNO, ConfiguracoesWsGetDocAlunoVO.class);
		String endpointImagensDigitalizadas = configuracoesVO.getEndpointImagensDigitalizadas();
		String resultadoJson = null;
		String stackTrace = null;
		String mensagem = null;

		long fim;
		long inicio = System.currentTimeMillis();

		try {
			if(StringUtils.isBlank(endpointImagensDigitalizadas)) {
				//throw new RuntimeException ("Endpoint não configurado.");
				return null;
			}

			Crawler crawler = new Crawler();
			crawler.setTimeout(30);
			crawler.setMaxTentativas(3);

			autenticarWebService(crawler);

			Integer quantidade = (Integer) parametros.getFirst(CAMPO_QUANTIDADE);

			String url = endpointImagensDigitalizadas + "/" + quantidade;

			Crawler.RespostaHTTP respostaHTTP = crawler.get(url);
			Document body = respostaHTTP.getBody();
			resultadoJson = body.getAllElements().text();
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

	protected void autenticarWebService(Crawler crawler) {

		ConfiguracoesWsGetDocAlunoVO configuracoesVO = parametroService.getConfiguracaoAsObject(P.CONFIGURACOES_WS_ALUNO, ConfiguracoesWsGetDocAlunoVO.class);
		String endpointLogin = configuracoesVO.getEndpointLogin();
		String login = configuracoesVO.getLogin();
		String senha = Criptografia.decrypt(Criptografia.SENHA, configuracoesVO.getSenha());

		String resultadoJson = null;
		String mensagem = null;

		try {
			Map<String, Object> postBody = new LinkedHashMap<>();
			postBody.put("login", login);
			postBody.put("senha", senha);

			Gson gson = new Gson();
			String json = gson.toJson(postBody);

			systraceThread(" URL: " + endpointLogin + " parans: " + postBody);
			resultadoJson = crawler.postJSON(endpointLogin, json);
			systraceThread("release: " + resultadoJson);
		}
		catch (Exception e) {
			mensagem = DummyUtils.getExceptionMessage(e);
			mensagem = String.format("Erro ao chamar Rest WebService: %s", mensagem);
			systraceThread(mensagem, LogLevel.ERROR);
			e.printStackTrace();
		}
	}

	@Override
	protected MultiValueMap<String, Object> getParametrosApi(WebServiceClientVO vo) {
		return new LinkedMultiValueMap<>(1);
	}

	@Override
	protected MultiValueMap<String, Object> getParametros(WebServiceClientVO vo) {

		AlunoConsultaVO alunoConsultaVO = (AlunoConsultaVO) vo;
		MultiValueMap<String, Object> queryParams = new LinkedMultiValueMap<>(1);
		int quantidade = alunoConsultaVO.getQuantidade();
		queryParams.add(CAMPO_QUANTIDADE, quantidade);

		return queryParams;
	}

	@Override
	protected StatusConsultaExterna validarSucessoOuErro(ConsultaExterna consultaExterna, JsonNode resultadoJson) {
		return resultadoJson != null ? StatusConsultaExterna.SUCESSO : StatusConsultaExterna.ERRO;
	}

	@Override
	protected boolean isPost() {
		return false;
	}

	@Override
	protected String montarUrl(MultiValueMap<String, Object> parametrosAPI) {
		ConfiguracoesWsGetDocAlunoVO configuracoesVO = parametroService.getConfiguracaoAsObject(P.CONFIGURACOES_WS_ALUNO, ConfiguracoesWsGetDocAlunoVO.class);
		String endpoint = configuracoesVO.getEndpointImgDoc();
		return endpoint;
	}
}