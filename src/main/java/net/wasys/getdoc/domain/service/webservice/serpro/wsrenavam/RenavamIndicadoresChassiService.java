//package net.wasys.getdoc.domain.service.webservice.serpro.wsrenavam;
//
//import br.com.samuelweb.certificado.Certificado;
//import br.com.samuelweb.certificado.CertificadoService;
//import com.fasterxml.jackson.databind.JsonNode;
//import net.wasys.getdoc.domain.entity.ConsultaExterna;
//import net.wasys.getdoc.domain.enumeration.StatusConsultaExterna;
//import net.wasys.getdoc.domain.service.ParametroService;
//import net.wasys.getdoc.domain.service.ResourceService;
//import net.wasys.getdoc.domain.service.webservice.RestWebServiceClient;
//import net.wasys.getdoc.domain.vo.ConfiguracoesWsRenavamVO;
//import net.wasys.getdoc.domain.vo.WebServiceClientVO;
//import net.wasys.getdoc.domain.vo.RenavamIndicadoresChassiRequestVO;
//import net.wasys.util.DummyUtils;
//import net.wasys.util.LogLevel;
//import net.wasys.util.other.Criptografia;
//import net.wasys.util.other.RepeatTry;
//import net.wasys.util.rest.RestClient;
//import org.apache.commons.io.FileUtils;
//import org.apache.http.client.HttpClient;
//import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
//import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
//import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
//import org.apache.http.impl.client.HttpClientBuilder;
//import org.apache.http.ssl.SSLContextBuilder;
//import org.apache.http.ssl.SSLContexts;
//import org.bouncycastle.jce.provider.BouncyCastleProvider;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.ResponseEntity;
//import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
//import org.springframework.stereotype.Service;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.client.RestTemplate;
//
//import javax.net.ssl.*;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.security.*;
//import java.security.cert.CertificateException;
//import java.util.Map;
//
//import static net.wasys.util.DummyUtils.systraceThread;
//
//@Service
//public class RenavamIndicadoresChassiService extends RestWebServiceClient {
//
//	@Autowired private ParametroService parametroService;
//
//	public static void main(String[] args) throws Exception {
//
//		//curl -X GET --header "x-cpf-usuario: 57481687900" --insecure --cert-type pem --cert WEBWASYSINFORMATICALTDA-04916157000112.pem https://renavam.denatran.serpro.gov.br/v2/veiculos/indicadores/chassi/9BD19713HJ3355881
//
//		String certificadoPath = "D:\\tmp\\nfe-interessado.pfx";
//		String certificadoSenhaPath = "D:\\tmp\\nfe-interessado-certcod.key";
//		File certificadoSenhaFile = new File(certificadoSenhaPath);
//		String certificadoSenha = FileUtils.readFileToString(certificadoSenhaFile, "UTF-8");
//		certificadoSenha = Criptografia.decrypt(Criptografia.CERT_COD, certificadoSenha);
//
//		String url = "https://renavam.denatran.serpro.gov.br/v2/veiculos/indicadores/chassi/9BD19713HJ3355881";
//
//		HttpComponentsClientHttpRequestFactory requestFactory = buildRequestFactory(certificadoPath, certificadoSenha);
//		requestFactory.setConnectTimeout(60 * 1000);
//		RestTemplate restTemplate = new RestTemplate(requestFactory);
//
//		HttpHeaders headers = new HttpHeaders();
//		headers.add("x-cpf-usuario", "57481687900");
//		headers.add("accept", "*/*");
//
//		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null, headers);
//
//		RepeatTry<ResponseEntity> repeatTry = new RepeatTry(3, 5 * 1000);
//		repeatTry.setToTry(() ->
//				restTemplate.exchange(url, HttpMethod.GET, request, String.class)
//		);
//		ResponseEntity<String> response = repeatTry.execute();
//
//		systraceThread(response.getBody());
//	}
//
//	@Override
//	protected ConsultaExterna chamarWebService(MultiValueMap<String, Object> parametros) {
//
//		ConfiguracoesWsRenavamVO cwid = parametroService.getConfiguracaoAsObject(ParametroService.P.CONFIGURACOES_WS_RENAVAM, ConfiguracoesWsRenavamVO.class);
//		String resultadoJson = null;
//		String stackTrace = null;
//		String mensagem = null;
//
//		try {
//			resultadoJson = chamarWebService(cwid, parametros);
//		}
//		catch (RestClient.RestException e) {
//			String release = e.getRelease();
//			if(DummyUtils.isJson(release)) {
//				resultadoJson = "{\"errors\": " + release + "}";
//			}
//			else {
//				mensagem = DummyUtils.getExceptionMessage(e);
//				mensagem = "Erro ao chamar WebService: " + mensagem;
//			}
//		}
//		catch (Exception e) {
//			mensagem = DummyUtils.getExceptionMessage(e);
//			mensagem = "Erro ao chamar WebService: " + mensagem;
//			systraceThread(mensagem, LogLevel.ERROR);
//			e.printStackTrace();
//			stackTrace = DummyUtils.getStackTrace(e);
//		}
//
//		ConsultaExterna consultaExterna = new ConsultaExterna();
//		consultaExterna.setResultado(resultadoJson);
//		consultaExterna.setStackTrace(stackTrace);
//		consultaExterna.setMensagem(mensagem);
//		return consultaExterna;
//	}
//
//	protected String chamarWebService(ConfiguracoesWsRenavamVO cwid, MultiValueMap<String, Object> parametros) throws Exception {
//
//		String chassi = (String) parametros.getFirst(RenavamIndicadoresChassiRequestVO.CHASSI);
//		String endpoint = cwid.getIndicadoresChassiEndpoint();
//		String cpfUsuario = cwid.getCpfUsuario();
//
//		String certPath = resourceService.getValue(ResourceService.CERT_PATH);
//		String certCod = resourceService.getValue(ResourceService.CERT_COD);
//		File certCodFile = new File(certCod);
//		certCod = FileUtils.readFileToString(certCodFile, "UTF-8");
//		certCod = Criptografia.decrypt(Criptografia.CERT_COD, certCod);
//
//		String url = endpoint + chassi;
//
//		HttpComponentsClientHttpRequestFactory requestFactory = buildRequestFactory(certPath, certCod);
//		requestFactory.setConnectTimeout(60 * 1000);
//		RestTemplate restTemplate = new RestTemplate(requestFactory);
//
//		HttpHeaders headers = new HttpHeaders();
//		headers.add("x-cpf-usuario", cpfUsuario);
//		headers.add("accept", "*/*");
//
//		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(null, headers);
//
//		RepeatTry<ResponseEntity> repeatTry = new RepeatTry(3, 5 * 1000);
//		repeatTry.setToTry(() ->
//				restTemplate.exchange(url, HttpMethod.GET, request, String.class)
//		);
//		ResponseEntity<String> response = repeatTry.execute();
//
//		return response.getBody();
//	}
//
//	private static HttpComponentsClientHttpRequestFactory buildRequestFactory(String certificadoPath, String certificadoSenha) throws Exception {
//
//		SSLContext sslcontext = getSslContext();
//		SSLSocketFactory socketfactory = getSocketFactory(certificadoPath, certificadoSenha, sslcontext);
//		SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(socketfactory, new AllowAllHostnameVerifier());
//
//		HttpClient httpClient = getHttpClient(sslcontext, sslConnectionSocketFactory);
//		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
//		return factory;
//	}
//
//	private static HttpClient getHttpClient(SSLContext sslcontext, SSLConnectionSocketFactory sslConnectionSocketFactory) {
//		HttpClientBuilder builder = HttpClientBuilder.create();
//		builder.setSslcontext(sslcontext);
//		builder.setSSLSocketFactory(sslConnectionSocketFactory);
//		return builder.build();
//	}
//
//	private static SSLContext getSslContext() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
//		Security.addProvider(new BouncyCastleProvider());
//		SSLContextBuilder custom = SSLContexts.custom();
//		custom = custom.loadTrustMaterial(null, new TrustSelfSignedStrategy());
//		return custom.build();
//	}
//
//	public static SSLSocketFactory getSocketFactory(String certificadoPath, String certificadoSenha, SSLContext sslcontext) throws Exception {
//		Certificado certificado = CertificadoService.certificadoPfx(certificadoPath, certificadoSenha);
//		KeyStore keystore = CertificadoService.getKeyStore(certificado);
//
//		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
//		kmf.init(keystore, certificadoSenha.toCharArray());
//		KeyManager[] km = kmf.getKeyManagers();
//		X509TrustManager tm = getIntelledTrustManager();
//
//		sslcontext.init(km, new TrustManager[]{tm}, null);
//		return sslcontext.getSocketFactory();
//	}
//
//	private static X509TrustManager getIntelledTrustManager() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
//		File tempFile = DummyUtils.getFileFromResource("/net/wasys/getdoc/cacerts/wsrenavam");
//		InputStream in = new FileInputStream(tempFile);
//		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
//		ks.load(in, "changeit".toCharArray());
//		in.close();
//
//		TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//		tmf.init(ks);
//		return (X509TrustManager) tmf.getTrustManagers()[0];
//	}
//
//	@Override
//	protected MultiValueMap<String, Object> getParametrosApi(WebServiceClientVO vo) {
//		return null;
//	}
//
//	@Override
//	protected MultiValueMap<String, Object> getParametros(WebServiceClientVO vo) {
//		RenavamIndicadoresChassiRequestVO vo2 = (RenavamIndicadoresChassiRequestVO) vo;
//		String chassi = vo2.getChassi();
//
//		MultiValueMap<String, Object> parametros = new LinkedMultiValueMap<>(3);
//		parametros.add(RenavamIndicadoresChassiRequestVO.CHASSI, chassi);
//		return parametros;
//	}
//
//	@Override
//	protected boolean isPost() {
//		return true;
//	}
//
//	@Override
//	protected String montarUrl(MultiValueMap<String, Object> parametrosAPI) {
//		return null;
//	}
//
//	@Override
//	protected StatusConsultaExterna validarSucessoOuErro(ConsultaExterna consultaExterna, JsonNode resultadoJson) {
//		//se chegou até aqui é pq retornou um json, se retornou um json considera como sucesso
//		return StatusConsultaExterna.SUCESSO;
//	}
//
//	public static class FaceRequest {
//		private Map<String, String> key;
//		private Map<String, Object> answer;
//		public Map<String, String> getKey() {
//			return key;
//		}
//		public void setKey(Map<String, String> key) {
//			this.key = key;
//		}
//		public Map<String, Object> getAnswer() {
//			return answer;
//		}
//		public void setAnswer(Map<String, Object> answer) {
//			this.answer = answer;
//		}
//	}
//}
