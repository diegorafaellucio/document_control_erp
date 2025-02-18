package net.wasys.getdoc.domain.service;

import com.google.common.base.Stopwatch;
import net.wasys.getdoc.GetdocConstants;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.CamposMetadadosTipificacao;
import net.wasys.getdoc.domain.enumeration.ModoTipificacao;
import net.wasys.getdoc.domain.enumeration.StatusConsultaExterna;
import net.wasys.getdoc.domain.enumeration.TipoConsultaExterna;
import net.wasys.getdoc.domain.vo.ConfiguracoesWsDarknetVO;
import net.wasys.getdoc.domain.vo.FileVO;
import net.wasys.getdoc.domain.vo.ResultadoDarknet;
import net.wasys.getdoc.domain.vo.TipificacaoDarknetVO;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.http.ProxyManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class TipificacaoDarknetService {

	public static final String HEADERS_NAME_IMAGE = "image";

	@Autowired private ResourceService resourceService;
	@Autowired private TipoDocumentoService tipoDocumentoService;
	@Autowired private ParametroService parametroService;
	@Autowired private ConsultaExternaService consultaExternaService;
	@Autowired private CampoModeloOcrService campoModeloOcrService;
	@Autowired private GrupoModeloDocumentoService grupoModeloDocumentoService;

	public boolean isHabilitada() {
		ConfiguracoesWsDarknetVO configuracoesVO = parametroService.getConfiguracaoAsObject(ParametroService.P.CONFIGURACOES_WS_DARKNET, ConfiguracoesWsDarknetVO.class);
		String endpoint = configuracoesVO != null ? configuracoesVO.getEndpoint() : null;
		return StringUtils.isNotBlank(endpoint);
	}

	public Map<Long, List<FileVO>> tipificar(Documento documentoOrigem, List<FileVO> files, List<Documento> documentos, List<FileVO> naoTipificou) {

		Map<Long, List<FileVO>> map = new LinkedHashMap<>();
		if(!isHabilitada()) {
			systraceThread("Tifificando: tipificação daknet não habilitada");
			return map;
		}

		for (FileVO fileVO : files) {

			File file = fileVO.getFile();
			String fileName = file.getName();
			String extensao = DummyUtils.getExtensao(fileName);

			List<ResultadoDarknet> results = null;
			if(GetdocConstants.IMAGEM_EXTENSOES.contains(extensao) || "pdf".equals(extensao)) {
				try {
					results = getTipificacaoWS(documentoOrigem, file);
				}
				catch(Exception e) {
					e.printStackTrace();
					naoTipificou.add(fileVO);
					throw e;
				}
			}

			if(results != null && !results.isEmpty()) {
				Map<Long, List<FileVO>> mapResult = processarRetorno(documentos, fileVO, results, naoTipificou);
				map.putAll(mapResult);
			}
			else {
				naoTipificou.add(fileVO);
			}
		}

		for(Long documentoId : map.keySet()) {
			List<FileVO> fileVOS = map.get(documentoId);
			files.removeAll(fileVOS);
		}

		return map;
	}

	private Map<Long, List<FileVO>> processarRetorno(List<Documento> documentos, FileVO fileVO, List<ResultadoDarknet> results, List<FileVO> naoTipificou) {

		Map<Long, List<FileVO>> map = new LinkedHashMap<>();

		boolean encontrou = false;

		Map<String, String> metadados = fileVO.getMetadados();
		metadados = metadados != null ? metadados : new LinkedHashMap<>();
		fileVO.setMetadados(metadados);

		String allLabels = "";
		for (ResultadoDarknet result : results) {
			String label = result.getLabel();
			allLabels += label + " --> " + result.getConfidence() +"; ";
		}
		metadados.put(CamposMetadadosTipificacao.DN_IMAGEM_TIPIFICADA.getCampo(), "true");
		metadados.put(CamposMetadadosTipificacao.DN_TODAS_LABELS.getCampo(), allLabels);

		for(Documento documento : documentos) {

			Long documentoId = documento.getId();
			TipoDocumento tipoDocumento = documento.getTipoDocumento();
			if(tipoDocumento == null) continue;

			Long tipoDocumentoId = tipoDocumento.getId();
			List<ModeloDocumento> modelosDocumentos = tipoDocumentoService.findModelosDocumentos(tipoDocumentoId);

			Object[] result = processarRetorno(modelosDocumentos, results);

			if(result != null) {
				ModeloDocumento modeloDocumento = (ModeloDocumento) result[0];
				ResultadoDarknet resultadoDarknet = (ResultadoDarknet) result[1];
				Integer topleftX = resultadoDarknet.getTopleftX();
				Integer topleftY = resultadoDarknet.getTopleftY();
				Integer bottomRightX = resultadoDarknet.getBottomRightX();
				Integer bottomRightY = resultadoDarknet.getBottomRightY();

				List<FileVO> list2 = map.get(documentoId);
				list2 = list2 != null ? list2 : new ArrayList<>();
				map.put(documentoId, list2);
				list2.add(fileVO);
				fileVO.setModeloTipificacao(modeloDocumento);

				String label = resultadoDarknet.getLabel();
				metadados.put(CamposMetadadosTipificacao.DN_LABEL.getCampo(), label);

				metadados.put(CamposMetadadosTipificacao.DN_CROP.getCampo(), "[" + topleftX + ", " + topleftY + ", " + bottomRightX + ", " + bottomRightY + "]");

				BigDecimal confidence = resultadoDarknet.getConfidence();
				confidence = confidence.multiply(new BigDecimal(100));
				confidence = confidence.setScale(0, RoundingMode.CEILING);
				metadados.put(CamposMetadadosTipificacao.DN_PERCENTUAL_ACERTO.getCampo(), confidence.toString());

				encontrou = true;
			}
		}

		if(!encontrou && naoTipificou != null) {
			naoTipificou.add(fileVO);
		}

		return map;
	}

	private Object[] processarRetorno(List<ModeloDocumento> modelosDocumentos, List<ResultadoDarknet> results) {

		for (ResultadoDarknet result : results) {
			for (ModeloDocumento modeloDocumento : modelosDocumentos) {

				boolean darknetHabilitada = modeloDocumento.getDarknetApiHabilitada();
				if(!darknetHabilitada) {
					continue;
				}

				String labelsDarknet = modeloDocumento.getLabelDarknet();
				if(labelsDarknet == null || labelsDarknet.isEmpty()) {
					continue;
				}

				String label = result.getLabel();
				if(label.equals(labelsDarknet)) {
					return new Object[]{modeloDocumento, result};
				}
			}
		}

		return null;
	}

	public List<ResultadoDarknet> getTipificacaoWS(Documento documentoOrigem, File file) {

		ConfiguracoesWsDarknetVO configuracoesVO = parametroService.getConfiguracaoAsObject(ParametroService.P.CONFIGURACOES_WS_DARKNET, ConfiguracoesWsDarknetVO.class);
		String endpoint = configuracoesVO != null ? configuracoesVO.getEndpoint() : null;

		if(StringUtils.isBlank(endpoint)) {
			throw new RuntimeException("tipificação daknet não habilitada") ;
		}

		ConsultaExterna ce = new ConsultaExterna();
		ce.setData(new Date());

		Stopwatch stopwatch = Stopwatch.createStarted();
		String absolutePath = file.getAbsolutePath();
		systraceThread("URL darknet: " + endpoint + ". File: " + absolutePath);
		try {
			MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
			bodyMap.add(HEADERS_NAME_IMAGE, new FileSystemResource(file));
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
			HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(bodyMap, headers);

			RestTemplate restTemplate = getRestTemplate(endpoint);

			ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.POST, requestEntity, String.class);
			String responseBody = response.getBody();

			systraceThread("Sucesso ao acessar DarknetAPI. responseBody: " + responseBody);

			String result = DummyUtils.stringToJson(responseBody);
			ce.setResultado(result);
			JSONObject jsonObj = new JSONObject(result);

			String status = jsonObj.get("status").toString();
			Boolean tipificou = Boolean.valueOf(status);

			Object data = jsonObj.get("data");
			List<ResultadoDarknet> resultadoList = new ArrayList<>();

			if (tipificou && data != null && (data instanceof JSONArray) && !((JSONArray) data).isNull(0)) {

				JSONArray data2 = (JSONArray) data;
				for (int i = 0; i < data2.length(); i++) {
					JSONObject jsonObject = data2.getJSONObject(i);
					Map<String, Object> map = jsonObject.toMap();
					Map<String, Object> topleftMap = (Map<String, Object>) map.get("topleft");
					Integer topLeftX = (Integer) topleftMap.get("x");
					Integer topLeftY = (Integer) topleftMap.get("y");

					Double confidence = (Double) map.get("confidence");
					String label = (String) map.get("label");

					Map<String, Object> bottomrightMap = (Map<String, Object>) map.get("bottomright");
					Integer bottomRightX = (Integer) bottomrightMap.get("x");
					Integer bottomRightY = (Integer) bottomrightMap.get("y");

					ResultadoDarknet rd = new ResultadoDarknet();
					rd.setLabel(label);
					rd.setTopleftX(topLeftX);
					rd.setTopleftY(topLeftY);
					rd.setConfidence(new BigDecimal(confidence));
					rd.setBottomRightX(bottomRightX);
					rd.setBottomRightY(bottomRightY);
					rd.setJson(jsonObject.toString(4));
					resultadoList.add(rd);
				}
			}

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
			ce.setTipo(TipoConsultaExterna.TIPIFICACAO_DARKNET);
			ce.setTempoExecucao(tempo);

			StringBuilder parametros = new StringBuilder();
			parametros.append("{");
			parametros.append("\"file\": \"" + absolutePath + "\"");
			if(documentoOrigem != null) {
				Long documentoId = documentoOrigem.getId();
				parametros.append(", \"documentoId\": \"" + documentoId + "\"");
			}
			parametros.append("}");
			ce.setParametros(parametros.toString());

			TipificacaoDarknetVO vo = new TipificacaoDarknetVO();
			if(documentoOrigem != null) {
				Processo processo = documentoOrigem.getProcesso();
				vo.setProcesso(processo);
			}
			consultaExternaService.salvarComLog(ce, vo);
		}
	}

	private RestTemplate getRestTemplate(String endpoint) {

		ProxyManager proxyManager = resourceService.getProxyManager();
		RestTemplate restTemplate;

		if(proxyManager != null) {
			SimpleClientHttpRequestFactory clientHttpReq = new SimpleClientHttpRequestFactory();
			//String host = proxyManager.getHost();
			//int port = proxyManager.getPort();
			//DummyUtils.systraceThread("configurando proxy, host:" + host + " port: " + port);
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

	private Map<Long, List<FileVO>> processarRetornoFluxoAprovacao(Documento documento, FileVO fileVOOrigin, List<ResultadoDarknet> results, List<FileVO> naoTipificou) {
		Map<Long, List<FileVO>> map = new LinkedHashMap<>();

		boolean encontrou = false;

		Long documentoId = documento.getId();
		TipoDocumento tipoDocumento = documento.getTipoDocumento();
		Long tipoDocumentoId = tipoDocumento.getId();
		List<GrupoModeloDocumento> gruposModeloDocumento = tipoDocumentoService.findGruposModeloDocumento(tipoDocumentoId);
		Object[] result = processarRetornoFluxoAprovacao(gruposModeloDocumento, results);

		if (result != null) {

			FileVO fileVO = fileVOOrigin.clone();

			ModeloDocumento modeloDocumento = (ModeloDocumento) result[0];
			ResultadoDarknet resultadoDarknet = (ResultadoDarknet) result[1];
			GrupoModeloDocumento grupoModeloDocumento = (GrupoModeloDocumento) result[2];
			Integer topleftX = resultadoDarknet.getTopleftX();
			Integer topleftY = resultadoDarknet.getTopleftY();
			Integer bottomRightX = resultadoDarknet.getBottomRightX();
			Integer bottomRightY = resultadoDarknet.getBottomRightY();

			List<FileVO> list2 = map.get(documentoId);
			list2 = list2 != null ? list2 : new ArrayList<>();
			map.put(documentoId, list2);
			list2.add(fileVO);
			fileVO.setModeloTipificacao(modeloDocumento);
			fileVO.setGrupoModeloDocumento(grupoModeloDocumento);

			ModeloOcr modeloOcr = modeloDocumento.getModeloOcr();

			List<CampoOcr> camposOcr = new ArrayList<>();

			if (modeloOcr != null) {
				Long modeloOcrId = modeloOcr.getId();
				List<CampoModeloOcr> camposModeloOcr = campoModeloOcrService.findByModeloOcr(modeloOcrId);

				for (CampoModeloOcr campoModeloOcr : camposModeloOcr) {
					CampoOcr campoOcr = new CampoOcr();
					campoOcr.setCampoModeloOcr(campoModeloOcr);
					campoOcr.setNome(campoModeloOcr.getDescricao());
					campoOcr.setImagem(fileVO.getImagem());
					camposOcr.add(campoOcr);
				}
			}

			if (camposOcr.size() > 0) {
				fileVO.setCampos(camposOcr);
			}

			Map<String, String> metadados = fileVO.getMetadados();
			metadados = metadados != null ? metadados : new LinkedHashMap<>();

			String allLabels = "";
			String label = resultadoDarknet.getLabel();
			BigDecimal confidence = resultadoDarknet.getConfidence();
			confidence = confidence.multiply(new BigDecimal(100));
			confidence = confidence.setScale(0, RoundingMode.CEILING);

			setMetadados(metadados, allLabels, label, confidence, topleftX, topleftY, bottomRightX, bottomRightY);

			fileVO.setMetadados(metadados);

			encontrou = true;
		}

		if (!encontrou && naoTipificou != null) {
			naoTipificou.add(fileVOOrigin);
		}

		return map;
	}

	private void setMetadados(Map<String, String> metadados, String allLabels, String label, BigDecimal confidence, Integer topleftX, Integer topleftY, Integer bottomRightX, Integer bottomRightY) {
		metadados.put(CamposMetadadosTipificacao.DN_IMAGEM_TIPIFICADA.getCampo(), "true");
		metadados.put(CamposMetadadosTipificacao.DN_TODAS_LABELS.getCampo(), allLabels);
		metadados.put(CamposMetadadosTipificacao.DN_LABEL.getCampo(), label);
		metadados.put(CamposMetadadosTipificacao.DN_CROP.getCampo(), "[" + topleftX + ", " + topleftY + ", " + bottomRightX + ", " + bottomRightY + "]");
		metadados.put(CamposMetadadosTipificacao.DN_PERCENTUAL_ACERTO.getCampo(), confidence.toString());
	}

	private Object[] processarRetornoFluxoAprovacao(List<GrupoModeloDocumento> gruposModeloDocumento, List<ResultadoDarknet> results) {

		for (ResultadoDarknet result : results) {
			for (GrupoModeloDocumento grupoModeloDocumento : gruposModeloDocumento) {
				Long grupoModeloDocumentoId = grupoModeloDocumento.getId();
				List<ModeloDocumento> modelosDocumentos = grupoModeloDocumentoService.findModelosDocumento(grupoModeloDocumentoId);
				for (ModeloDocumento modeloDocumento : modelosDocumentos) {
					if (!modeloDocumento.getDarknetApiHabilitada()) {
						continue;
					}

					String labelsDarknet = modeloDocumento.getLabelDarknet();
					if (StringUtils.isEmpty(labelsDarknet)) {
						continue;
					}

					String label = result.getLabel();
					if (label.equals(labelsDarknet)) {
						return new Object[]{modeloDocumento, result, grupoModeloDocumento};
					}
				}
			}
		}
		return null;
	}

	public Map<Long, List<FileVO>> tipificarFluxoAprovacao(List<FileVO> imageList, Documento documento) {

		Map<Long, List<FileVO>> map = new LinkedHashMap<>();
		if (!isHabilitada()) {
			systraceThread("Tipificação daknet não habilitada");
			return map;
		}

		for (FileVO fileVO : imageList) {
			TipoDocumento tipoDocumento = documento.getTipoDocumento();
			ModoTipificacao modoTipificacao = tipoDocumento != null ? tipoDocumento.getModoTipificacao() : null;
			if (ModoTipificacao.OCR.equals(modoTipificacao)) {
				continue;
			}

			String extensao = DummyUtils.getExtensao(fileVO.getFile().getName());
			List<ResultadoDarknet> resultadoDarknetList = null;
			if (GetdocConstants.OCR_ACCEPTED_EXTENSIONS.contains(extensao)) {
				try {
					resultadoDarknetList = getTipificacaoWSJobsOCR(fileVO.getFile(), documento);
				} catch (Exception e) {
					systraceThread("Não foi possivel tipificar o documento " + documento, LogLevel.ERROR);
					throw e;
				}
			}

			if (CollectionUtils.isNotEmpty(resultadoDarknetList)) {
				map.putAll(processarRetornoFluxoAprovacao(documento, fileVO, resultadoDarknetList));
			}
		}

		return map;
	}

	private Map<Long,List<FileVO>> processarRetornoFluxoAprovacao(Documento documento, FileVO fileVO, List<ResultadoDarknet> resultadoDarknetList) {
		Map<Long, List<FileVO>> map = new LinkedHashMap<>();
		TipoDocumento tipoDocumento = documento.getTipoDocumento();
		if(tipoDocumento == null) return map;

		Long tipoDocumentoId = tipoDocumento.getId();
		List<GrupoModeloDocumento> gruposModeloDocumento = tipoDocumentoService.findGruposModeloDocumento(tipoDocumentoId);

		Object[] result = processarRetornoFluxoAprovacao(gruposModeloDocumento, resultadoDarknetList);

		if (result != null) {
			ModeloDocumento modeloDocumento = (ModeloDocumento) result[0];
			ResultadoDarknet resultadoDarknet = (ResultadoDarknet) result[1];
			GrupoModeloDocumento grupoModeloDocumento = (GrupoModeloDocumento) result[2];

			List<FileVO> files = new ArrayList<>();
			map.put(documento.getId(), files);
			FileVO cloneFile = fileVO.clone();
			files.add(cloneFile);
			cloneFile.setModeloTipificacao(modeloDocumento);
			cloneFile.setGrupoModeloDocumento(grupoModeloDocumento);

			ModeloOcr modeloOcr = modeloDocumento.getModeloOcr();
			List<CampoOcr> camposOcr = new ArrayList<>();
			if (modeloOcr != null){
				Long modeloOcrId = modeloOcr.getId();
				List<CampoModeloOcr> campoModeloOcrList = campoModeloOcrService.findByModeloOcr(modeloOcrId);
				for (CampoModeloOcr campoModeloOcr : campoModeloOcrList) {
					if (!campoModeloOcr.isAtivo())
						continue;

					CampoOcr campoOcr = new CampoOcr();
					campoOcr.setCampoModeloOcr(campoModeloOcr);
					campoOcr.setNome(campoModeloOcr.getDescricao());
					campoOcr.setImagem(cloneFile.getImagem());
					camposOcr.add(campoOcr);
				}
			}

			if (camposOcr.size() > 0) {
				cloneFile.setCampos(camposOcr);
			}

			Map<String, String> metadados = fileVO.getMetadados();
			metadados = metadados != null ? metadados : new LinkedHashMap<>();

			String allLabels = "";
			String label = resultadoDarknet.getLabel();
			BigDecimal confidence = resultadoDarknet.getConfidence();
			confidence = confidence.multiply(new BigDecimal(100));
			confidence = confidence.setScale(0, RoundingMode.CEILING);

			Integer topleftX = resultadoDarknet.getTopleftX();
			Integer topleftY = resultadoDarknet.getTopleftY();
			Integer bottomRightX = resultadoDarknet.getBottomRightX();
			Integer bottomRightY = resultadoDarknet.getBottomRightY();

			setMetadados(metadados, allLabels, label, confidence, topleftX, topleftY, bottomRightX, bottomRightY);

			cloneFile.getImagemMeta().setTipificado(true);
			cloneFile.getImagem().setModeloDocumento(modeloDocumento);
			cloneFile.setMetadados(metadados);
			documento.setGrupoModeloDocumento(grupoModeloDocumento);

		}

		return map;
	}

	public List<ResultadoDarknet> getTipificacaoWSJobsOCR(File file, Documento documento) {

		ConfiguracoesWsDarknetVO configuracoesVO = parametroService.getConfiguracaoAsObject(ParametroService.P.CONFIGURACOES_WS_DARKNET, ConfiguracoesWsDarknetVO.class);
		String endpoint = configuracoesVO != null ? configuracoesVO.getEndpointTipificacaoJobOCR() : null;

		if(StringUtils.isBlank(endpoint)) {
			throw new RuntimeException("tipificação daknet não habilitada") ;
		}

		ConsultaExterna ce = new ConsultaExterna();
		ce.setData(new Date());

		Stopwatch stopwatch = Stopwatch.createStarted();
		String absolutePath = file.getAbsolutePath();
		systraceThread("URL darknet: " + endpoint + ". File: " + absolutePath);
		try {
			MultiValueMap<String, Object> bodyMap = new LinkedMultiValueMap<>();
			bodyMap.add(HEADERS_NAME_IMAGE, new FileSystemResource(file));
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
			HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(bodyMap, headers);

			RestTemplate restTemplate = getRestTemplate(endpoint);

			ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.POST, requestEntity, String.class);
			String responseBody = response.getBody();

			systraceThread("Sucesso ao acessar DarknetAPI. responseBody: " + responseBody);

			String result = DummyUtils.stringToJson(responseBody);
			ce.setResultado(result);
			JSONObject jsonObj = new JSONObject(result);

			String status = jsonObj.get("status").toString();
			Boolean tipificou = Boolean.valueOf(status);

			JSONArray data = new JSONArray();
			if(jsonObj.get("data") instanceof JSONArray){
				data = jsonObj.getJSONArray("data");
			}
			List<ResultadoDarknet> resultadoList = new ArrayList<>();

			if (tipificou && !data.isNull(0)) {

				for (int i = 0; i < data.length(); i++) {
					JSONObject jsonObject = data.getJSONObject(i);
					Map<String, Object> map = jsonObject.toMap();
					Map<String, Object> topleftMap = (Map<String, Object>) map.get("topleft");
					Integer topLeftX = (Integer) topleftMap.get("x");
					Integer topLeftY = (Integer) topleftMap.get("y");

					Double confidence = (Double) map.get("confidence");
					String label = (String) map.get("label");

					Map<String, Object> bottomrightMap = (Map<String, Object>) map.get("bottomright");
					Integer bottomRightX = (Integer) bottomrightMap.get("x");
					Integer bottomRightY = (Integer) bottomrightMap.get("y");

					ResultadoDarknet rd = new ResultadoDarknet();
					rd.setLabel(label);
					rd.setTopleftX(topLeftX);
					rd.setTopleftY(topLeftY);
					rd.setConfidence(new BigDecimal(confidence));
					rd.setBottomRightX(bottomRightX);
					rd.setBottomRightY(bottomRightY);
					rd.setJson(jsonObject.toString(4));
					resultadoList.add(rd);
				}
			}

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
			ce.setTipo(TipoConsultaExterna.TIPIFICACAO_DARKNET);
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

			TipificacaoDarknetVO vo = new TipificacaoDarknetVO();
			if(documento != null) {
				Processo processo = documento.getProcesso();
				vo.setProcesso(processo);
			}
			consultaExternaService.salvarComLog(ce, vo);
		}
	}
}
