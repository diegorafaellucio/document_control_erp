package net.wasys.getdoc.domain.service.webservice.crivo;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.wasys.getdoc.soapws.SOAPBuilder;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.JsonNode;

import net.wasys.getdoc.domain.entity.ConsultaExterna;
import net.wasys.getdoc.domain.enumeration.StatusConsultaExterna;
import net.wasys.getdoc.domain.service.ParametroService;
import net.wasys.getdoc.domain.service.ParametroService.P;
import net.wasys.getdoc.domain.service.webservice.SOAPWebServiceClient;
import net.wasys.getdoc.domain.vo.ConfiguracoesWsCrivoVO;
import net.wasys.getdoc.domain.vo.CrivoConsultaVO;
import net.wasys.getdoc.domain.vo.WebServiceClientVO;

@Service
public class CrivoService extends SOAPWebServiceClient {

	private static final String CAMPO_USER = "sUser";
	private static final String CAMPO_PASSWORD = "sPassword";
	private static final String CAMPO_POLITICA = "sPolitica";
	private static final String TIPO_PARAMETROS = "sParametros";

	@Autowired private ParametroService parametroService;

	@Override
	protected MultiValueMap<String, Object> getParametrosApi(WebServiceClientVO vo) {

		CrivoConsultaVO crivoConsultaVo = (CrivoConsultaVO) vo;
		MultiValueMap<String, Object> queryParams = new LinkedMultiValueMap<>(2);

		ConfiguracoesWsCrivoVO cwda = parametroService.getConfiguracaoAsObject(P.CONFIGURACOES_CRIVO, ConfiguracoesWsCrivoVO.class);

		queryParams.add(CAMPO_USER, cwda.getLogin());
		queryParams.add(CAMPO_PASSWORD, cwda.getSenha());
		queryParams.add(CAMPO_POLITICA, crivoConsultaVo.getPolitica());

		return queryParams;
	}

	@Override
	protected MultiValueMap<String, Object> getParametros(WebServiceClientVO vo) {

		CrivoConsultaVO crivoConsultaVo = (CrivoConsultaVO) vo;

		MultiValueMap<String, Object> queryParams = new LinkedMultiValueMap<>(3);
		Map<String, String> parametros = crivoConsultaVo.getParametros();
		Set<Entry<String, String>> entrySet = parametros.entrySet();
		String param = "P\n";
		for (Entry<String, String> entry : entrySet) {
			String key = entry.getKey();
			String value = entry.getValue();
			if(key.equalsIgnoreCase("DDD") && StringUtils.isNotBlank(value) && value.matches(".*\\(.*\\).*")) {
				value = value.replaceAll("^.*\\(", "");
				value = value.replaceAll("\\).*$", "");
			}
			if(key.equalsIgnoreCase("Telefone") && StringUtils.isNotBlank(value) && value.matches(".*\\(.*\\).*")) {
				value = value.replaceAll("^.*\\)", "");
				value = value.trim();
			}
			param += key + ";" + value + "\n";
		}

		queryParams.add(TIPO_PARAMETROS, param);

		return queryParams;
	}


	@Override
	protected String extrairResultado(JsonNode resultadoJson) {

		JsonNode respostaSucesso = resultadoJson.findPath("ResultadoXml");
		if(!respostaSucesso.isMissingNode()) {
			return respostaSucesso.toString();
		}

		return "";
	}

	@Override
	protected SOAPBuilder criarSOAPBuilder(MultiValueMap<String, Object> parametros) {
		String namespace = "wcc";
		String namespaceUri = "http://tempuri.org/WCCrivo";
		String baseElement = "SetPolicyEvalValuesObjectXml";
		return new SOAPBuilder(parametros, namespace, namespaceUri, baseElement);
	}

	@Override
	protected String getEndpoint() {
		ConfiguracoesWsCrivoVO cwid = parametroService.getConfiguracaoAsObject(P.CONFIGURACOES_CRIVO, ConfiguracoesWsCrivoVO.class);
		return cwid.getEndPointCrivo();
	}

	@Override
	protected StatusConsultaExterna validarSucessoOuErro(ConsultaExterna consultaExterna, JsonNode resultadoJson) {

		JsonNode respostaNode = resultadoJson.findPath("ResultadoXml");
		if(respostaNode.isMissingNode()) {

			JsonNode erro = resultadoJson.findPath("faultstring");
			String erroStr = "";
			if(erro != null) {
				erroStr = erro.toString();
				erroStr = erroStr.replaceAll("^\"", "");
				erroStr = erroStr.replaceAll("\"$", "");
				erroStr = erroStr.replace("\\n", "\n ");
				consultaExterna.setMensagem(erroStr);
			}

			return StatusConsultaExterna.ERRO;
		}

		return StatusConsultaExterna.SUCESSO;
	}
}
