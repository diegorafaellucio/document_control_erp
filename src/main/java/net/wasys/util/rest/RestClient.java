package net.wasys.util.rest;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.http.HttpManager;
import net.wasys.util.http.HttpManager.Response;
import net.wasys.util.http.ProxyManager;
import net.wasys.util.other.RepeatTry;
import net.wasys.util.rest.dto.ErrorDTO;
import net.wasys.util.rest.jackson.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.entity.AbstractHttpEntity;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import static net.wasys.util.DummyUtils.systraceThread;

public class RestClient {

	private String url;
	private Long timeout = 100000l; // valor antigo 30000l
	private long repeatInterval = 5000;
	private int repeatTimes = 3;
	private Map<String, String> headers;
	private PropertyNamingStrategy propertyNamingStrategy;
	private Response response;
	private ProxyManager proxyManager;

	public RestClient() {}

	public RestClient(String url) {
		this.url = url;
	}

	public <T> T execute(Class<T> resultType) throws Exception {

		RepeatTry<T> rt = new RepeatTry<>(repeatTimes, repeatInterval);
		rt.setToTry(() -> {
			return execute2(null, resultType);
		});

		return rt.execute();
	}

	public <T> T execute(Object dto, Class<T> resultType) throws Exception {

		RepeatTry<T> rt = new RepeatTry<>(repeatTimes, repeatInterval);
		rt.setToTry(() -> {
			return execute2(dto, resultType);
		});

		return rt.execute();
	}

	@SuppressWarnings("unchecked")
	private <T> T execute2(Object dto, Class<T> resultType) {

		DummyUtils.systraceThread("url: " + url + " proxy: " + proxyManager);

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		if(propertyNamingStrategy != null) {
			mapper.setPropertyNamingStrategy(propertyNamingStrategy);
		}

		HttpManager httpManager = new HttpManager(proxyManager);
		httpManager.setIsSsl(url.startsWith("https"));
		httpManager.setThrowByStatus(false);
		httpManager.setHeaders(headers);
		httpManager.setTimeout(timeout);
		httpManager.start();

		String json = null;
		try {
			if(dto != null) {
				if (dto instanceof AbstractHttpEntity) {
					this.response = httpManager.post(url, (AbstractHttpEntity) dto);
				}
				else {
					if(dto instanceof String) {
						json = (String) dto;
					} else {
						json = mapper.writeValueAsString(dto);
					}
					this.response = httpManager.postJson(url, json);
				}
			}
			else {
				this.response = httpManager.get(url, null);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			String exceptionMessage = DummyUtils.getExceptionMessage(e);
			String json2 = StringUtils.substring(json, 0, 400);
			throw new RuntimeException("Erro ao chamar restws (" + url + "): " + exceptionMessage + ". " + json2, e);
		}

		HttpResponse httpResponse = response.getHttpResponse();
		StatusLine statusLine = httpResponse.getStatusLine();
		int statusCode = statusLine.getStatusCode();
		String release = response.release();
		int indexOf = url.indexOf("?");
		String url2 = indexOf > 0 ? url.substring(0, indexOf) : url;

		if(statusCode == 400) {
			String mensagem = null;
			try {
				ErrorDTO error = mapper.readValue(release, ErrorDTO.class);
				Set<String> messages = error.getMessages();
				if(messages != null && !messages.isEmpty()) {
					mensagem = "Erro 400 ao chamar restws (" + url2 + "): message: " + messages;
				} else {
					mensagem = "Erro 400 ao chamar restws (" + url2 + "): release2: " + release;
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				mensagem = "Erro 400 ao chamar restws (" + url2 + "): release: " + release;
			}

			DummyUtils.systraceThread(mensagem);
			mensagem = StringUtils.substring(mensagem, 0, 400);
			throw new RestException(mensagem, release, statusCode);
		}
		else if(statusCode != 200) {
			String mensagem = "Erro " + statusCode + " ao chamar restws (" + url2 + "): " + release;
			systraceThread(mensagem, LogLevel.ERROR);
			mensagem = StringUtils.substring(mensagem, 0, 400);
			throw new RestException(mensagem, release, statusCode);
		}

		if(resultType == null) {
			return null;
		}

		Constructor<?>[] constructors = resultType.getConstructors();
		for (Constructor<?> constructor : constructors) {
			Class<?>[] parameterTypes = constructor.getParameterTypes();
			Class<?>[] classesArray = new Class[] {String.class};
			if(Arrays.equals(classesArray, parameterTypes)) {
				try {
					return (T) constructor.newInstance(release);
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		}

		try {
			return mapper.readValue(release, resultType);
		}
		catch (Exception e) {
			String exceptionMessage = DummyUtils.getExceptionMessage(e);
			String message = "Erro ao chamar restws (" + url + ") falha no parse do json: " + exceptionMessage;
			systraceThread(message, LogLevel.ERROR);
			systraceThread(release, LogLevel.ERROR);
			e.printStackTrace();
			throw new RestException(message, release, null);
		}
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setTimeout(Long timeout) {
		this.timeout = timeout;
	}

	public void setRepeatTimes(int repeatTimes) {
		this.repeatTimes = repeatTimes;
	}

	public void setRepeatInterval(long repeatInterval) {
		this.repeatInterval = repeatInterval;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public void setPropertyNamingStrategy(PropertyNamingStrategy propertyNamingStrategy) {
		this.propertyNamingStrategy = propertyNamingStrategy;
	}

	public Response getResponse() {
		return response;
	}

	public static class RestException extends RuntimeException {

		private String release;
		private Integer statusCode;

		public RestException(String mensagem, String release, Integer statusCode) {
			super(mensagem);
			this.release = release;
			this.statusCode = statusCode;
		}

		public String getRelease() {
			return release;
		}

		public Integer getStatusCode() {
			return statusCode;
		}
	}

	public void setProxyManager(ProxyManager proxyManager) {
		this.proxyManager = proxyManager;
	}
}
