package net.wasys.getdoc.domain.service.webservice.infocar;

import net.wasys.getdoc.soapws.SOAPBuilder;
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
import net.wasys.getdoc.domain.vo.ConfiguracoesWsInfocarVO;
import net.wasys.getdoc.domain.vo.LeilaoInfoCarRequestVO;
import net.wasys.getdoc.domain.vo.WebServiceClientVO;

@Service
public class LeilaoInfoCarService extends SOAPWebServiceClient {

	private static final String CAMPO_CHAVE = "chave";
	private static final String CAMPO_TIPO = "tipo";
	private static final String CAMPO_DADO = "dado";
	private static final String TIPO_PLACA = "PLACA";

	@Autowired private ParametroService parametroService;

	@Override
	protected String getEndpoint() {
		ConfiguracoesWsInfocarVO cwid = parametroService.getConfiguracaoAsObject(P.CONFIGURACOES_WS_INFOCAR, ConfiguracoesWsInfocarVO.class);
		return cwid.getEndPointLeilao();
	}

	@Override
	protected String extrairResultado(JsonNode resultadoJson) {

		JsonNode respostaSucesso = resultadoJson.findPath("INFO-XML");
		if(!respostaSucesso.isMissingNode()) {
			return respostaSucesso.toString();
		}

		return "";
	}

	@Override
	protected SOAPBuilder criarSOAPBuilder(MultiValueMap<String, Object> parametros) {
		String namespace = "tem";
		String namespaceUri = "http://tempuri.org/";
		String baseElement = "consulta";
		return new SOAPBuilder(parametros, namespace, namespaceUri, baseElement);
	}

	@Override
	protected MultiValueMap<String, Object> getParametrosApi(WebServiceClientVO vo) {

		MultiValueMap<String, Object> parametros = new LinkedMultiValueMap<>();
		ConfiguracoesWsInfocarVO cwid = parametroService.getConfiguracaoAsObject(P.CONFIGURACOES_WS_INFOCAR, ConfiguracoesWsInfocarVO.class);

		parametros.add(CAMPO_CHAVE, cwid.getKey());

		return parametros;
	}

	@Override
	protected MultiValueMap<String, Object> getParametros(WebServiceClientVO vo) {

		LeilaoInfoCarRequestVO leilaoInfoCarConsultaVO = (LeilaoInfoCarRequestVO) vo;
		String placa = leilaoInfoCarConsultaVO.getPlaca();
		placa = placa != null ? placa : "";
		placa = placa.replace("-", "");

		MultiValueMap<String, Object> parametros = new LinkedMultiValueMap<>(2);
		parametros.add(CAMPO_TIPO, TIPO_PLACA);
		parametros.add(CAMPO_DADO, placa);

		return parametros;
	}

	@Override
	protected StatusConsultaExterna validarSucessoOuErro(ConsultaExterna consultaExterna, JsonNode resultadoJson) {

		JsonNode respostaNode = resultadoJson.findPath("SOLICITACAO");
		if(respostaNode.isMissingNode()) {
			return StatusConsultaExterna.ERRO;
		}

		JsonNode mensagemNode = respostaNode.findPath("MENSAGEM");
		if(mensagemNode.isMissingNode()) {
			return StatusConsultaExterna.ERRO;
		}
		else {
			//0 - Não Possui Registro
			//1 - Tem Registro
			//2 - Falha de Autenticação
			//3 - Dado Incorreto
			//4 - Erro na Pesquisa e/ou Sistema Indisponível
			String mensagem = mensagemNode.asText();
			if ("2".equals(mensagem)) {
				consultaExterna.setMensagem("Falha de autenticação. Mensagem 2.");
				return StatusConsultaExterna.ERRO;
			}
			else if ("3".equals(mensagem)) {
				consultaExterna.setMensagem("Dado incorreto. Mensagem 3.");
				return StatusConsultaExterna.ERRO;
			}
			else if ("4".equals(mensagem)) {
				consultaExterna.setMensagem("Erro na Pesquisa e/ou Sistema Indisponível. Mensagem 4.");
				return StatusConsultaExterna.ERRO;
			}
		}

		return StatusConsultaExterna.SUCESSO;
	}
}
