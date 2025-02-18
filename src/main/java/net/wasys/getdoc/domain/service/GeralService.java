package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;

import net.wasys.getdoc.domain.enumeration.MotivoBloqueioUsuario;
import net.wasys.getdoc.restws.dto.*;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.http.HttpManager;
import net.wasys.util.other.Criptografia;
import net.wasys.util.other.RepeatTry;
import net.wasys.util.rest.dto.ErrorDTO;
import net.wasys.util.rest.jackson.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Constructor;
import java.util.*;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class GeralService {

	@Autowired protected ResourceService resourceService;

	@SuppressWarnings("unchecked")
	private <T> T chamarWs(String url, Object dto, Class<T> resultType) throws MessageKeyException {

		systraceThread("url: " + url + " " + dto);

		String username = resourceService.getValue(ResourceService.GERAL_USERNAME);
		String password = resourceService.getValue(ResourceService.GERAL_PASSWORD);
		ObjectMapper mapper = new ObjectMapper();
		String xAccessTokenEncrypt = Criptografia.encrypt(Criptografia.GERAL_ESTACIO, DummyUtils.formatDateTime2(new Date()));

		HttpManager httpManager = new HttpManager();
		boolean isSsh = url.startsWith("https://");
		httpManager.setIsSsl(isSsh);
		Map<String, String> headers = new HashMap<>();
		headers.put("username", username);
		headers.put("password", password);
		headers.put("x-access-token-encrypt", xAccessTokenEncrypt);
		httpManager.setHeaders(headers);
		httpManager.setTimeout(30 * 1000l);
		httpManager.start();

		HttpManager.Response response = null;
		if(dto != null) {
			String json = mapper.writeValueAsString(dto);
			response = httpManager.postJson(url, json);
		}
		else {
			response = httpManager.get(url, null);
		}

		HttpResponse httpResponse = response.getHttpResponse();
		StatusLine statusLine = httpResponse.getStatusLine();
		int statusCode = statusLine.getStatusCode();
		String release = response.release();

		if(statusCode == 400 || statusCode == 417) {

			Set<String> messages = null;
			try {
				ErrorDTO error = mapper.readValue(release, ErrorDTO.class);
				messages = error.getMessages();
			}
			catch (Exception e) {
				systraceThread("falha no parse do json 400: " + DummyUtils.getExceptionMessage(e), LogLevel.ERROR);
				systraceThread(release, LogLevel.ERROR);
				throw new RuntimeException("Erro ao chamar geral (" + url + "): 400");
			}

			String str = String.valueOf(messages);
			str = str.replaceAll("^\\[", "");
			str = str.replaceAll("]$", "");
			if(statusCode == 400) {
				throw new RuntimeException(str);
			} else {
				throw new MessageKeyException("messageKeyExceptionRest.error", str);
			}
		}
		else if(statusCode != 200) {
			systraceThread(release, LogLevel.ERROR);
			throw new RuntimeException("Erro ao chamar geral (" + url + "): " + statusLine.toString());
		}

		if(resultType == null || StringUtils.isBlank(release)) {
			return null;
		}

		//caso seja um tipo primitivo...
		Constructor<?>[] constructors = resultType.getConstructors();
		for (Constructor<?> constructor : constructors) {
			Class<?>[] parameterTypes = constructor.getParameterTypes();
			if(new Class[] {String.class}.equals(parameterTypes)) {
				try {
					return (T) constructor.newInstance(release);
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		}

		try {
			T result = mapper.readValue(release, resultType);
			return result;
		}
		catch (Exception e) {
			systraceThread("falha no parse do json: " + DummyUtils.getExceptionMessage(e), LogLevel.ERROR);
			systraceThread(release, LogLevel.ERROR);
			e.printStackTrace();
			throw new RuntimeException("Erro ao chamar geral (" + url + ")");
		}
	}

	public FeriadoDTO[] findFeriados(Date ultimaDataAtualizacao) throws Exception {

		String endpoint = resourceService.getValue(ResourceService.GERAL_ENDPOINT);
		String sistema = resourceService.getValue(ResourceService.GERAL_SISTEMA);

		final String url = endpoint + "sincronismo/find-feriados/" + sistema;

		RepeatTry<FeriadoDTO[]> rt = new RepeatTry<>(3, 5000);
		rt.setToTry(() -> {
			return chamarWs(url, ultimaDataAtualizacao , FeriadoDTO[].class);
		});

		FeriadoDTO[] result = rt.execute();

		return result;
	}

	public AreaDTO[] findAreas(Date ultimaDataAtualizacao) throws Exception {

		String endpoint = resourceService.getValue(ResourceService.GERAL_ENDPOINT);
		String sistema = resourceService.getValue(ResourceService.GERAL_SISTEMA);

		final String url = endpoint + "sincronismo/find-areas/" + sistema;

		RepeatTry<AreaDTO[]> rt = new RepeatTry<>(3, 5000);
		rt.setToTry(() -> {
			return chamarWs(url, ultimaDataAtualizacao , AreaDTO[].class);
		});

		AreaDTO[] result = rt.execute();

		return result;
	}

	public SubareaDTO[] findSubareas(Date ultimaDataAtualizacao) throws Exception {

		String endpoint = resourceService.getValue(ResourceService.GERAL_ENDPOINT);
		String sistema = resourceService.getValue(ResourceService.GERAL_SISTEMA);

		final String url = endpoint + "sincronismo/find-subareas/" + sistema;

		RepeatTry<SubareaDTO[]> rt = new RepeatTry<>(3, 5000);
		rt.setToTry(() -> {
			return chamarWs(url, ultimaDataAtualizacao , SubareaDTO[].class);
		});

		SubareaDTO[] result = rt.execute();

		return result;
	}

	public List<Map<String, String>> findOutrasBases(String endpoint, Date ultimaDataAtualizacao) throws Exception {

		String endpoint0 = resourceService.getValue(ResourceService.GERAL_ENDPOINT);
		String sistema = resourceService.getValue(ResourceService.GERAL_SISTEMA);

		final String url = endpoint0 + endpoint + sistema;

		RepeatTry<List<Map<String, Object>>> rt = new RepeatTry<>(3, 5000);
		rt.setToTry(() -> {
			return chamarWs(url, ultimaDataAtualizacao , List.class);
		});

		List<Map<String, Object>> list = rt.execute();

		List<Map<String, String>> list2 = new ArrayList<>();
		for (Map<String, Object> map : list) {
			Map<String, String> map2 = new LinkedHashMap<>();
			for (String coluna : map.keySet()) {
				Object valor = map.get(coluna);
				String valorStr = "";
				if(valor != null) {
					if(valor instanceof Date) {
						valorStr = DummyUtils.formatDateTime2((Date) valor);
					} else {
						valorStr = String.valueOf(valor);
					}
				}
				map2.put(coluna, valorStr);
			}
			list2.add(map2);
		}
		return list2;
	}

	public UsuarioDTO[] findUsuarios(Date ultimaDataAtualizacao) throws Exception {

		String endpoint = resourceService.getValue(ResourceService.GERAL_ENDPOINT);
		String sistema = resourceService.getValue(ResourceService.GERAL_SISTEMA);

		final String url = endpoint + "sincronismo/find-usuarios/" + sistema;

		RepeatTry<UsuarioDTO[]> rt = new RepeatTry<>(3, 5000);
		rt.setToTry(() -> {
			return chamarWs(url, ultimaDataAtualizacao , UsuarioDTO[].class);
		});

		UsuarioDTO[] result = rt.execute();

		return result;
	}

	public void atualizarDatasUltimosAcessos(DataUltimoAcessoDTO[] datasUltimosAcessos) throws Exception {

		String endpoint = resourceService.getValue(ResourceService.GERAL_ENDPOINT);
		String sistema = resourceService.getValue(ResourceService.GERAL_SISTEMA);

		final String url = endpoint + "sincronismo/atualizar-acessos/" + sistema;

		RepeatTry<Object> rt = new RepeatTry<>(3, 5000);
		rt.setToTry(() -> {
			return chamarWs(url, datasUltimosAcessos , null);
		});

		rt.execute();
	}

	public UsuarioDTO reiniciarSenha(String login, String usuarioLogadoLogin) throws Exception {

		String endpoint = resourceService.getValue(ResourceService.GERAL_ENDPOINT);
		String sistema = resourceService.getValue(ResourceService.GERAL_SISTEMA);

		final String url = endpoint + "usuario/reiniciar-senha/" + sistema;

		Map<String, String> map = new LinkedHashMap<>();
		map.put("login", login);
		map.put("usuarioLogadoLogin", usuarioLogadoLogin);

		RepeatTry<UsuarioDTO> rt = new RepeatTry<>(3, 5000);
		rt.setToTry(() -> {
			return chamarWs(url, map, UsuarioDTO.class);
		});

		return rt.execute();
	}

	public UsuarioDTO desativarUsuario(String login, String usuarioLogadoLogin, MotivoBloqueioUsuario motivo) throws Exception {
		String endpoint = resourceService.getValue(ResourceService.GERAL_ENDPOINT);
		String sistema = resourceService.getValue(ResourceService.GERAL_SISTEMA);

		final String url = endpoint + "usurio/desativar/" + sistema;

		Map<String, String> map = new LinkedHashMap<>();
		map.put("login", login);
		map.put("usuarioLogadoLogin", usuarioLogadoLogin);
		map.put("motivoDesativacao", motivo.name());

		RepeatTry<UsuarioDTO> rt = new RepeatTry<>(3, 5000);
		rt.setToTry(() -> {
			return chamarWs(url, map, UsuarioDTO.class);
		});

		return rt.execute();
	}

	public UsuarioDTO ativarUsuario(String login, String loginAutor) throws Exception {
		String endpoint = resourceService.getValue(ResourceService.GERAL_ENDPOINT);
		String sistema = resourceService.getValue(ResourceService.GERAL_SISTEMA);

		final String url = endpoint + "usuario/ativar/" + sistema;

		Map<String, String> map = new LinkedHashMap<>();
		map.put("login", login);
		map.put("usuarioLogadoLogin", loginAutor);

		RepeatTry<UsuarioDTO> rt = new RepeatTry<>(3, 5000);
		rt.setToTry(() -> {
			return chamarWs(url, map, UsuarioDTO.class);
		});

		return rt.execute();
	}

	public UsuarioDTO bloquearUsuario(String login, MotivoBloqueioUsuario motivoBloqueio) throws Exception {
		String endpoint = resourceService.getValue(ResourceService.GERAL_ENDPOINT);
		String sistema = resourceService.getValue(ResourceService.GERAL_SISTEMA);

		final String url = endpoint + "usuario/bloquear/" + sistema;

		Map<String, String> map = new LinkedHashMap<>();
		map.put("login", login);
		map.put("motivoBloqueio", motivoBloqueio.name());

		RepeatTry<UsuarioDTO> rt = new RepeatTry<>(3, 5000);
		rt.setToTry(() -> {
			return chamarWs(url, map, UsuarioDTO.class);
		});

		return rt.execute();
	}

	public UsuarioDTO enviarRedefinicaoSenha(String login) throws Exception {

		String endpoint = resourceService.getValue(ResourceService.GERAL_ENDPOINT);
		String sistema = resourceService.getValue(ResourceService.GERAL_SISTEMA);

		String urlSistema = resourceService.getValue(ResourceService.URL_SISTEMA_EXTRANET);
		String urlRetorno = urlSistema + "/trocar-senha/";

		final String url = endpoint + "usuario/redefinicao-senha/" + sistema;

		Map<String, String> map = new LinkedHashMap<>();
		map.put("login", login);
		map.put("url", urlRetorno);

		RepeatTry<UsuarioDTO> rt = new RepeatTry<>(3, 5000);
		rt.setToTry(() -> {
			return chamarWs(url, map, UsuarioDTO.class);
		});

		return rt.execute();
	}

	public UsuarioDTO atualizarSenha(String login, String novaSenha) throws Exception {
		String endpoint = resourceService.getValue(ResourceService.GERAL_ENDPOINT);
		String sistema = resourceService.getValue(ResourceService.GERAL_SISTEMA);

		final String url = endpoint + "usuario/atualizar-senha/" + sistema;

		Map<String, String> map = new LinkedHashMap<>();
		map.put("login", login);
		map.put("senha", novaSenha);

		RepeatTry<UsuarioDTO> rt = new RepeatTry<>(3, 5000);
		rt.setToTry(() -> {
			return chamarWs(url, map, UsuarioDTO.class);
		});

		return rt.execute();
	}

	public UsuarioDTO prorrogarAcesso(String login, String usuarioLogadoLogin) throws Exception {
		String endpoint = resourceService.getValue(ResourceService.GERAL_ENDPOINT);
		String sistema = resourceService.getValue(ResourceService.GERAL_SISTEMA);

		final String url = endpoint + "usuario/prorrogar-acesso/" + sistema;

		Map<String, String> map = new LinkedHashMap<>();
		map.put("login", login);
		map.put("usuarioLogadoLogin", usuarioLogadoLogin);

		RepeatTry<UsuarioDTO> rt = new RepeatTry<>(3, 5000);
		rt.setToTry(() -> {
			return chamarWs(url, map, UsuarioDTO.class);
		});

		return rt.execute();
	}

	public boolean isSincronismoHabilitado() {
		String geralEndpoint = resourceService.getValue(ResourceService.GERAL_ENDPOINT);
		return StringUtils.isNotBlank(geralEndpoint);
	}
}
