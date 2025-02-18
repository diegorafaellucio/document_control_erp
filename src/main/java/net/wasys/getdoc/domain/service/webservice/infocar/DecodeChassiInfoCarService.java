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
import net.wasys.getdoc.domain.vo.DecodeInfoCarRequestVO;
import net.wasys.getdoc.domain.vo.WebServiceClientVO;

@Service
public class DecodeChassiInfoCarService extends SOAPWebServiceClient {

	private static final String CAMPO_CHAVE = "chave";
	private static final String CAMPO_OPCAO = "OPCAO";
	private static final String CAMPO_TIPO = "tipo";
	private static final String CAMPO_CHASSI = "chassi";
	private static final String CAMPO_PLACA = "placa";

	@Autowired private ParametroService parametroService;

	@Override
	protected String getEndpoint() {
		ConfiguracoesWsInfocarVO cwid = parametroService.getConfiguracaoAsObject(P.CONFIGURACOES_WS_INFOCAR, ConfiguracoesWsInfocarVO.class);
		return cwid.getEndPointDecode();
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

		DecodeInfoCarRequestVO leilaoInfoCarConsultaVO = (DecodeInfoCarRequestVO) vo;

		MultiValueMap<String, Object> parametrosMetodoConsulta = new LinkedMultiValueMap<>();

		parametrosMetodoConsulta.add(CAMPO_PLACA, "");
		parametrosMetodoConsulta.add(CAMPO_CHASSI, leilaoInfoCarConsultaVO.getChassi());
		parametrosMetodoConsulta.add(CAMPO_OPCAO, "150");
		parametrosMetodoConsulta.add(CAMPO_TIPO, 1);

		return parametrosMetodoConsulta;
	}

	@Override
	protected StatusConsultaExterna validarSucessoOuErro(ConsultaExterna consultaExterna, JsonNode resultadoJson) {

		JsonNode respostaNode = resultadoJson.findPath("consultaResponse");

		if(respostaNode.isMissingNode()) {
			return StatusConsultaExterna.ERRO;
		}
		else {
			//0 - Não Possui Registro
			//1 - Tem Registro
			//2 - Falha de Autenticação
			//3 - Dado Incorreto
			//4 - Erro na Pesquisa e/ou Sistema Indisponível
			JsonNode mensagemNode = respostaNode.findPath("MENSAGEM");
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

	@Override
	protected String extrairResultado(JsonNode resultadoJson) {

		JsonNode respostaSucesso = resultadoJson.findPath("RESPOSTA");
		if(!respostaSucesso.isMissingNode()) {
			return respostaSucesso.toString();
		}

		JsonNode respostaErro = resultadoJson.findPath("SOLICITACAO");
		if(!respostaErro.isMissingNode()) {
			return respostaErro.toString();
		}

		return "";
	}
}