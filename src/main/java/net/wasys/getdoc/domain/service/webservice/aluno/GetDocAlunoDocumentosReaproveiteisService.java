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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class GetDocAlunoDocumentosReaproveiteisService extends RestWebServiceClient {

	private static final String CAMPO_COD = "codOrigens";
	private static final String CAMPO_CPF = "cpf";
	private static final String CAMPO_CANDIDATO = "numCandidato";
	private static final String CAMPO_INSCRICAO = "numInscricao";

	@Autowired private ParametroService parametroService;

	@Override
	protected ConsultaExterna chamarWebService(MultiValueMap<String, Object> parametros) {

		ConfiguracoesWsGetDocAlunoVO configuracoesVO = parametroService.getConfiguracaoAsObject(ParametroService.P.CONFIGURACOES_WS_ALUNO, ConfiguracoesWsGetDocAlunoVO.class);
		String endpointImgDoc = configuracoesVO.getEndpointImgDoc();
		Crawler crawler = new Crawler();

		crawler.setTimeout(30);
		crawler.setMaxTentativas(3);

		String resultadoJson = null;
		String stackTrace = null;
		String mensagem = null;

		long fim;
		long inicio = System.currentTimeMillis();

		try {
			this.autenticarWebService(crawler);
			String cpf = (String) parametros.getFirst(CAMPO_CPF);
			String numCandidato = (String) parametros.getFirst(CAMPO_CANDIDATO);
			String numInscricao = (String) parametros.getFirst(CAMPO_INSCRICAO);
			Map<String, Object> postBody = new LinkedHashMap<>();
			postBody.put("cpf", cpf);
			postBody.put("numCandidato", numCandidato);
			postBody.put("numInscricao", numInscricao);
			List<Long> codOrigens = (List<Long>) parametros.getFirst(CAMPO_COD);
			postBody.put("codOrigens", codOrigens);

			Gson gson = new Gson();
			String json = gson.toJson(postBody);

			resultadoJson = crawler.postJSON(endpointImgDoc, json);
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

		ConfiguracoesWsGetDocAlunoVO configuracoesVO = parametroService.getConfiguracaoAsObject(ParametroService.P.CONFIGURACOES_WS_ALUNO, ConfiguracoesWsGetDocAlunoVO.class);
		String endpointLogin = configuracoesVO.getEndpointLogin();
		String login = configuracoesVO.getLogin();
		String senha =  Criptografia.decrypt(Criptografia.SENHA, configuracoesVO.getSenha());

		String resultadoJson = null;
		String mensagem = null;


		try {
			Map<String, Object> postBody = new LinkedHashMap<>();
			postBody.put("login", login);
			postBody.put("senha", senha);

			Gson gson = new Gson();
			String json = gson.toJson(postBody);

			resultadoJson = crawler.postJSON(endpointLogin, json);
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

		MultiValueMap<String, Object> queryParams = new LinkedMultiValueMap<>(0);
		return queryParams;
	}

	@Override
	protected MultiValueMap<String, Object> getParametros(WebServiceClientVO vo) {

		AlunoConsultaVO alunoConsultaVO = (AlunoConsultaVO) vo;
		MultiValueMap<String, Object> queryParams = new LinkedMultiValueMap<>(2);

		List<Long> codOrigens = alunoConsultaVO.getCodOrigens();
		String cpf = alunoConsultaVO.getCpf();
		String numInscricao = alunoConsultaVO.getNumInscricao();
		String numCandidato = alunoConsultaVO.getNumCandidato();

		queryParams.add(CAMPO_COD, codOrigens);
		queryParams.add(CAMPO_CPF, cpf);
		queryParams.add(CAMPO_CANDIDATO, numCandidato);
		queryParams.add(CAMPO_INSCRICAO, numInscricao);

		return queryParams;
	}

	@Override
	protected StatusConsultaExterna validarSucessoOuErro(ConsultaExterna consultaExterna, JsonNode resultadoJson) {
		JsonNode sucesso = resultadoJson.findPath("sucesso");
		return sucesso.toString().equals("true") ? StatusConsultaExterna.SUCESSO : StatusConsultaExterna.ERRO;
	}

	@Override
	protected boolean isPost() {
		return true;
	}

	@Override
	protected String montarUrl(MultiValueMap<String, Object> parametrosAPI) {
		ConfiguracoesWsGetDocAlunoVO configuracoesVO = parametroService.getConfiguracaoAsObject(P.CONFIGURACOES_WS_ALUNO, ConfiguracoesWsGetDocAlunoVO.class);
		String endpoint = configuracoesVO.getEndpointImgDoc();
		return endpoint;
	}
}