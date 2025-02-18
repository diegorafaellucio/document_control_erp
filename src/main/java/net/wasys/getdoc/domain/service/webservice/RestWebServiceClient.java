package net.wasys.getdoc.domain.service.webservice;

import net.wasys.getdoc.domain.entity.ConsultaExterna;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.http.ProxyManager;
import net.wasys.util.rest.RestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map.Entry;

public abstract class RestWebServiceClient extends WebServiceClient {

	protected abstract boolean isPost();
	protected abstract String montarUrl(MultiValueMap<String, Object> parametrosAPI);

	@Override
	protected ConsultaExterna chamarWebService(MultiValueMap<String, Object> parametros) {
		String url = montarUrl(parametros);
		ConsultaExterna consultaExterna = chamarRestWebService(url, parametros);
		return consultaExterna;
	}

	private ConsultaExterna chamarRestWebService(String url, MultiValueMap<String, Object> parametros) {

		String resultadoJson = null;
		String stackTrace = null;
		String mensagem = null;

		long inicio = System.currentTimeMillis();
		long fim;

		try {
			RestClient restClient = new RestClient(url);
			ProxyManager proxyManager = resourceService.getProxyManager();
			restClient.setProxyManager(proxyManager);

			if(isPost()) {
				String json = DummyUtils.objectToJson(parametros);
				json = json.replace("[", "");
				json = json.replace("]", "");
				resultadoJson = restClient.execute(json, String.class);
			}
			else {
				resultadoJson = restClient.execute(String.class);
			}
		}
		catch (Exception e) {
			mensagem = DummyUtils.getExceptionMessage(e);
			mensagem = String.format("Erro ao chamar Rest WebService. Erro: %s", mensagem);
			DummyUtils.systraceThread(mensagem, LogLevel.ERROR);
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

	protected MultiValueMap<String, String> toStringMultiValueMap(MultiValueMap<String, Object> objectMultiValueMap) {

		MultiValueMap<String, String> stringMap = new LinkedMultiValueMap<>(objectMultiValueMap.size());
		for (Entry<String, List<Object>> entry : objectMultiValueMap.entrySet()) {

			for (Object obj : entry.getValue()) {
				stringMap.add(entry.getKey(), String.valueOf(obj));
			}
		}

		return stringMap;
	}
}
