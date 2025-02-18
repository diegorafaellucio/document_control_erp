package net.wasys.getdoc.domain.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import net.wasys.util.http.ProxyManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Service;

import net.wasys.util.DummyUtils;

@Service
public class ResourceService {

	public static final String URL_SISTEMA_EXTRANET = "url_sistema_extranet";
	public static final String IMAGENS_PATH = "imagens_path";
	public static final String DOCUMENTOS_PATH = "documentos_path";
	public static final String CACHE_PATH = "cache_path";
	public static final String AMOSTRAGEM_IMAGENS_PATH = "amostragem_imagens_path";
	public static final String ADMINAJUDA_PATH = "admin_ajuda_path";
	public static final String HISTORICO_BASES_PATH = "historico_bases_path";
	public static final String MODELOS_PATH = "modelos_path";
	public static final String ANEXO_PROCESSO_PATH = "anexo_processo_path";
	public static final String CORREIO_ENDPOINT = "correio_endpoint";
	public static final String ANEXO_EMAIL_PATH = "anexo_email_path";
	public static final String TMP_MOBILE_PATH = "tmp_mobile_path";
	public static final String BACALHAU_PATH = "bacalhau_path";
	public static final String PUSH_IOS_CER_PWD = "push_ios_cer_pwd";
	public static final String PUSH_IOS_CER_PATH = "push_ios_cer_path";
	public static final String PUSH_REQUISICAO_EDICAO_HREF = "push_requisicao_edicao_href";
	public static final String IMAGENS_TMP_PATH = "imagens_tmp_path";
	public static final String MODELOS_TMP_PATH = "modelos_tmp_path";
	public static final String IMAGEM_LAYOUT_PATH = "imagem_layout_path";
	public static final String VISION_PROCESSOR_KEY = "vision_processor_key";
	public static final String FACE_RECOGNITION_ENDPOINT = "face_recognition_endpoint";
	public static final String BETAFACEAPI_KEY = "betafaceapi_key";
	public static final String BETAFACEAPI_API_SECRET = "betafaceapi_api_secret";
	public static final String DOWNLOAD_PATH = "download_path";
	public static final String OCR_WS_ID_SISTEMA = "ocr_ws_id_sistema";
	public static final String OCR_WS_URL = "ocr_ws_url";
	public static final String OCR_WS_BASE64 = "ocr_ws_base64";
	public static final String LOG_IMPORTACAO_PATH = "log_importacao_path";
	public static final String PROXY_HOST = "proxy_host";
	public static final String PROXY_PORT = "proxy_port";
	public static final String PROXY_USER = "proxy_user";
	public static final String PROXY_PASSWORD = "proxy_password";
	public static final String MONITORADOR_DISCOS = "monitorador_discos";
	public static final String GERAL_ENDPOINT = "geral_endpoint";
	public static final String GERAL_USERNAME = "geral_username";
	public static final String GERAL_PASSWORD = "geral_password";
	public static final String GERAL_SISTEMA = "geral_sistema";
	public static final String SENHA_CRIPTOGRAFADA = "senha_criptografada";
	public static final String NOME_EMPRESA_ASSUNTO_EMAIL = "nome_empresa_assunto_email";
	public static final String CERT_PATH = "cert_path";
	public static final String CERT_COD = "cert_cod";
	public static final String IMPORTACAO_DADOS_PATH = "importacao_dados_path";
	public static final String RELATORIO_PENDENCIA_DOCUMENTO_PATH = "relatorio_pendencia_documento_path";
	public static final String EWS_USERNAME = "ews.username";
	public static final String EWS_PASSWORD = "ews.password";
	public static final String EWS_URL = "ews.url";
	public static final String PORTAL_CANDIDATO = "portal_candidato";
	public static final String PORTAL_CANDIDATO_POS = "portal_candidato_pos";
	public static final String STORAGE_TMP_PATH = "storage_tmp_path";
	public static final String RELATORIO_GERAL_PATH = "relatorio_geral_path";
	public static final String RELATORIO_GERAL_HISTORICO_PATH = "relatorio_geral_historico_path";
    public static final String BACALHAU_REVERSO_PATH = "bacalhau_reverso_path";

	@Resource(name="resource") private MessageSource resource;

	public <T> T getValue(String key, Class<T> resultType) {
		String valor = getValue(key);
		T result = DummyUtils.convertTypes(valor, resultType);
		return result;
	}

	public String getValue(String key) {
		return getValue(key, (Object[]) null);
	}

	/**
	 * Procura o parametro considerando também o mode (-Dgetdoc.mode=dev, p.e.)
	 * Procura se o mode for dev.poc5, para procurar por imagens_path vai seguir essa ordem:
	 * 	1) dev.poc5.imagens_path (se encontrou para aqui)
	 * 	2) poc5.imagens_path (se encontrou para aqui)
	 * 	3) imagens_path
	 */
	public String getValue(String key, Object... args) {

		String valor = System.getProperty(key);
		if(valor != null) {
			return valor;
		}

		String mode = DummyUtils.getMode();
		mode = StringUtils.isBlank(mode) ? "" : mode + ".";

		String key2 = mode + key;

		for (int i = 0; i <= 4 /*max 4 niveis, por seguraça*/; i++) {
			valor = getValueFromResource(key2, args);

			if(valor != null) {
				return valor;
			}

			int indexOf = key2.indexOf(".");
			if(indexOf < 0) {
				return null;
			}

			key2 = key2.substring(indexOf + 1);
		}

		return null;
	}

	private String getValueFromResource(String key, Object[] args) {

		try {
			return resource.getMessage(key, args, null);
		}
		catch (NoSuchMessageException e) {
			//apenas não encontrou a mensagem
			return null;
		}
	}

	public List<String> getValueList(String key) {

		String value = getValue(key);
		if(StringUtils.isBlank(value)) {
			return new ArrayList<>();
		}

		String[] split = value.split(",");
		List<String> list = new ArrayList<>();
		for (String str : split) {
			str = StringUtils.trim(str);
			if(StringUtils.isNotBlank(str)) {
				list.add(str);
			}
		}
		return list;
	}

	public ProxyManager getProxyManager() {

		String proxyHost = getValue(ResourceService.PROXY_HOST);
		Integer proxyPort = getValue(ResourceService.PROXY_PORT, Integer.class);
		String proxyUser = getValue(ResourceService.PROXY_USER);
		String proxyPassword = getValue(ResourceService.PROXY_PASSWORD);
		ProxyManager proxyManager = null;
		if(org.apache.commons.lang.StringUtils.isNotBlank(proxyHost)) {
			proxyManager = new ProxyManager(proxyHost, proxyPort, proxyUser, proxyPassword);
		}

		return proxyManager;
	}
}
