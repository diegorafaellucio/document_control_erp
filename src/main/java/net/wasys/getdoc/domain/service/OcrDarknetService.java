package net.wasys.getdoc.domain.service;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import net.wasys.getdoc.GetdocConstants;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.CamposMetadadosTipificacao;
import net.wasys.getdoc.domain.enumeration.StatusConsultaExterna;
import net.wasys.getdoc.domain.enumeration.TipoConsultaExterna;
import net.wasys.getdoc.domain.vo.ConfiguracoesWsDarknetVO;
import net.wasys.getdoc.domain.vo.FileVO;
import net.wasys.getdoc.domain.vo.ResultadoOcrDarknetVO;
import net.wasys.getdoc.domain.vo.TipificacaoDarknetVO;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.http.ProxyManager;
import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class OcrDarknetService {

	public static final String HEADERS_NAME_IMAGE = "image";
	public static final String HEADERS_NAME_HOMOGRAPHY_FILE = "modelo_homografia";
	public static final String HEADERS_NAME_DN_CROP = "dn_crop";

	@Autowired private ResourceService resourceService;
	@Autowired private TipoDocumentoService tipoDocumentoService;
	@Autowired private ModeloDocumentoService modelosDocumentosService;
	@Autowired private GrupoModeloDocumentoService grupoModeloDocumentoService;
	@Autowired private ParametroService parametroService;
	@Autowired private ConsultaExternaService consultaExternaService;
	@Autowired private ImagemMetaService imagemMetaService;
	@Autowired private CampoOcrService campoOcrService;

	public String getURLFullText() {
		ConfiguracoesWsDarknetVO configuracoesVO = parametroService.getConfiguracaoAsObject(ParametroService.P.CONFIGURACOES_WS_DARKNET, ConfiguracoesWsDarknetVO.class);
		return configuracoesVO.getEndpointFullText();
	}

	public void extrairOCR(Documento documento, List<FileVO> files) {

		Map<Long, List<FileVO>> map = new LinkedHashMap<>();
		for (FileVO fileVO : files) {

			File file = fileVO.getFile();
			String fileName = file.getName();
			String extensao = DummyUtils.getExtensao(fileName);

			ModeloDocumento modeloDocumento = fileVO.getModeloTipificacao();
			if(modeloDocumento == null){
				continue;
			}

			ModeloOcr modeloOcr = modeloDocumento.getModeloOcr();

			if(modeloOcr == null){
				continue;
			}

			List<CampoOcr> camposOcr = fileVO.getCamposOcr();

			List<ResultadoOcrDarknetVO> results = null;
			if(GetdocConstants.OCR_ACCEPTED_EXTENSIONS.contains(extensao)) {
				try {
					results = getOcrWS(documento, file, modeloOcr, fileVO);
					ImagemMeta imagemMeta = imagemMetaService.getByImagem(fileVO.getImagem().getId());
					imagemMeta.setFullText(fileVO.getText());
					imagemMetaService.saveOrUpdate(imagemMeta);
				}
				catch(Exception e) {
					e.printStackTrace();
					throw e;
				}
			}

			if(results != null && !results.isEmpty()) {
				processarRetorno(camposOcr, results);
			}
		}
	}

	private void processarRetorno(List<CampoOcr> camposOcr, List<ResultadoOcrDarknetVO> results) {

		for (ResultadoOcrDarknetVO result : results) {
			String labelOcr = result.getLabelOcr();

			for (CampoOcr campoOcr : camposOcr) {
				CampoModeloOcr campoModeloOcr = campoOcr.getCampoModeloOcr();
				String labelOcrCampo = campoModeloOcr.getLabelOcr();

				if(labelOcr.equals(labelOcrCampo)){
					String valor = result.getValueOcr();
					if (labelOcr.equalsIgnoreCase("CPF")){
						valor = tratamentoCPF(valor);
					}
					campoOcr.setRecognizedValue(valor);
					campoOcr.setValor(valor);
					campoOcr.setPositionLeft(result.getPositionLeft());
					campoOcr.setPositionTop(result.getPositionTop());
					campoOcr.setPositionRight(result.getPositionRight());
					campoOcr.setPositionBottom(result.getPositionBottom());

					campoOcrService.saveOrUpdate(campoOcr);
				}


			}
		}
	}


	public List<ResultadoOcrDarknetVO> getOcrWS(Documento documento, File file, ModeloOcr modeloOcr, FileVO vo) {

		String endpoint = modeloOcr.getEndpointModeloOcr();

		if(StringUtils.isBlank(endpoint)) {
			if (modeloOcr.isExtrairFullText()){
				String fullText = extrairFulltext(file, vo);
				vo.setText(fullText);

				return null;
			}

			throw new RuntimeException("orc daknet não habilitado") ;
		}

		ConsultaExterna ce = new ConsultaExterna();
		ce.setData(new Date());

		Stopwatch stopwatch = Stopwatch.createStarted();
		String absolutePath = file.getAbsolutePath();
		systraceThread("URL OCR darknet: " + endpoint + ". File: " + absolutePath);
		try {
			MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
			bodyMap.add(HEADERS_NAME_IMAGE, new FileSystemResource(file));

			Map<String, String> metadados = vo.getMetadados();
			if (metadados != null && metadados.containsKey(CamposMetadadosTipificacao.DN_CROP.getCampo())){
				bodyMap.add(HEADERS_NAME_DN_CROP, metadados.get(CamposMetadadosTipificacao.DN_CROP.getCampo()));
			}

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
			HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(bodyMap, headers);

			RestTemplate restTemplate = getRestTemplate(endpoint);

			ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.POST, requestEntity, String.class);
			String responseBody = response.getBody();

			systraceThread("Sucesso ao acessar OCR DarknetAPI. responseBody: " + responseBody);

			String result = DummyUtils.stringToJson(responseBody);
			ce.setResultado(result);
			JSONObject jsonObj = new JSONObject(result);

			String status = jsonObj.get("status").toString();
			Boolean extraiuOcr = Boolean.valueOf(status);

			JSONObject data = jsonObj.getJSONObject("data");
			List<ResultadoOcrDarknetVO> resultadoList = new ArrayList<>();

			if (extraiuOcr) {
				Map<String, Object> map = data.toMap();
				for (Map.Entry<String, Object> entry : map.entrySet()) {
					ResultadoOcrDarknetVO resultadoOcrDarknetVO = new ResultadoOcrDarknetVO();

					String labelOcr = entry.getKey();
					//System.out.println(labelOcr);
					String valorOcr = (String) ((ArrayList<Object>)entry.getValue()).get(ResultadoOcrDarknetVO.INDEX_OCR_VALUE);
					Integer positionLeft = Integer.parseInt((String) ((ArrayList<Object>)(((ArrayList<Object>)entry.getValue()).get(ResultadoOcrDarknetVO.INDEX_OCR_COORDS))).get(ResultadoOcrDarknetVO.INDEX_OCR_COORDS_POSITION_LEFT));
					Integer positionTop = Integer.parseInt((String) ((ArrayList<Object>)(((ArrayList<Object>)entry.getValue()).get(ResultadoOcrDarknetVO.INDEX_OCR_COORDS))).get(ResultadoOcrDarknetVO.INDEX_OCR_COORDS_POSITION_TOP));
					Integer positionRight = Integer.parseInt((String) ((ArrayList<Object>)(((ArrayList<Object>)entry.getValue()).get(ResultadoOcrDarknetVO.INDEX_OCR_COORDS))).get(ResultadoOcrDarknetVO.INDEX_OCR_COORDS_POSITION_RIGHT));
					Integer positionBottom = Integer.parseInt((String) ((ArrayList<Object>)(((ArrayList<Object>)entry.getValue()).get(ResultadoOcrDarknetVO.INDEX_OCR_COORDS))).get(ResultadoOcrDarknetVO.INDEX_OCR_COORDS_POSITION_BOTTOM));

					resultadoOcrDarknetVO.setLabelOcr(labelOcr);
					resultadoOcrDarknetVO.setValueOcr(valorOcr);
					resultadoOcrDarknetVO.setPositionLeft(positionLeft);
					resultadoOcrDarknetVO.setPositionTop(positionTop);
					resultadoOcrDarknetVO.setPositionRight(positionRight);
					resultadoOcrDarknetVO.setPositionBottom(positionBottom);

					resultadoList.add(resultadoOcrDarknetVO);

				}
			}

			if (modeloOcr.isExtrairFullText()){
				String fullText = extrairFulltext(file, vo);
				vo.setText(fullText);
			}
			vo.setExtraiuOCR(extraiuOcr);
			return resultadoList;
		}
		catch (Exception e) {
			e.printStackTrace();
			String stackTrace = DummyUtils.getStackTrace(e);
			ce.setStackTrace(stackTrace);
			String exceptionMessage = DummyUtils.getExceptionMessage(e);
			ce.setMensagem(exceptionMessage);
			throw e;
		}
		finally {

			stopwatch.stop();
			long tempo = stopwatch.elapsed(TimeUnit.MILLISECONDS);
			String stackTrace = ce.getStackTrace();
			ce.setStatus(StringUtils.isNotBlank(stackTrace) ? StatusConsultaExterna.ERRO : StatusConsultaExterna.SUCESSO);
			ce.setTipo(TipoConsultaExterna.OCR);
			ce.setTempoExecucao(tempo);

			StringBuilder parametros = new StringBuilder();
			parametros.append("{");
			parametros.append("\"file\": \"" + absolutePath + "\"");
			if(documento != null) {
				Long documentoId = documento.getId();
				parametros.append(", \"documentoId\": \"" + documentoId + "\"");
			}
			parametros.append("}");
			ce.setParametros(parametros.toString());

			TipificacaoDarknetVO vo2 = new TipificacaoDarknetVO();
			if(documento != null) {
				Processo processo = documento.getProcesso();
				vo2.setProcesso(processo);
			}
			consultaExternaService.salvarComLog(ce, vo2);
		}
	}

	private String extrairFulltext(File file, FileVO vo) {
		String fulltext = "";

		String endpointFullText = getURLFullText();
		String absolutePath = file.getAbsolutePath();

		systraceThread("URL OCR FullText: " + endpointFullText + ". File: " + absolutePath);
		try {
			MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
			bodyMap.add(HEADERS_NAME_IMAGE, new FileSystemResource(file));

			Map<String, String> metadados = vo.getMetadados();
			if (metadados != null && metadados.containsKey(CamposMetadadosTipificacao.DN_CROP.getCampo())){
				bodyMap.add(HEADERS_NAME_DN_CROP, metadados.get(CamposMetadadosTipificacao.DN_CROP.getCampo()));
			}

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
			HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(bodyMap, headers);

			RestTemplate restTemplate = getRestTemplate(endpointFullText);

			ResponseEntity<String> response = restTemplate.exchange(endpointFullText, HttpMethod.POST, requestEntity, String.class);
			String responseBody = response.getBody();

			systraceThread("Sucesso ao acessar OCR DarknetAPI FullText. responseBody: " + responseBody);

			String result = DummyUtils.stringToJson(responseBody);
			JSONObject jsonObj = new JSONObject(result);

			String status = jsonObj.get("status").toString();
			Boolean extraiuFulltext = Boolean.valueOf(status);

			if (extraiuFulltext) {
				JSONObject data = jsonObj.getJSONObject("data");
				Map<String, Object> map = data.toMap();
				for (Map.Entry<String, Object> entry : map.entrySet()) {
					fulltext = (String) entry.getValue();
				}

			}

		} catch (Exception e) {
			systraceThread("Falha ao extrair FullText", LogLevel.ERROR);
		}


		return fulltext;
	}

	private RestTemplate getRestTemplate(String endpoint) {

		ProxyManager proxyManager = resourceService.getProxyManager();
		RestTemplate restTemplate;

		if(proxyManager != null) {
			SimpleClientHttpRequestFactory clientHttpReq = new SimpleClientHttpRequestFactory();
			//String host = proxyManager.getHost();
			//int port = proxyManager.getPort();
			//systraceThread("configurando proxy, host:" + host + " port: " + port);
			//Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, Integer.valueOf(port)));
			//clientHttpReq.setProxy(proxy);
			restTemplate = new RestTemplate(clientHttpReq);
		}
		else {
			restTemplate = new RestTemplate();
		}

		if(endpoint.startsWith("https")) {
			HttpComponentsClientHttpRequestFactory  requestFactory = getHttpComponentsClientHttpRequestFactory();
			restTemplate.setRequestFactory(requestFactory);
		}

		return restTemplate;
	}

	private HttpComponentsClientHttpRequestFactory getHttpComponentsClientHttpRequestFactory() {

		HttpComponentsClientHttpRequestFactory requestFactory = null;
		try {
			TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
			SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
			SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
			CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();
			requestFactory = new HttpComponentsClientHttpRequestFactory();
			requestFactory.setHttpClient(httpClient);
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Erro ao criar requestFactory para ssl...");
		}

		return requestFactory;
	}

	private String tratamentoCPF(String campoAnalise) {
		campoAnalise = DummyUtils.getCpfCnpjDesformatado(campoAnalise);
		return StringUtils.substring(campoAnalise, 0, 11);
	}
}
