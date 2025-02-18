package net.wasys.getdoc.domain.service.webservice.credilink;

import com.fasterxml.jackson.databind.JsonNode;
import net.wasys.getdoc.domain.entity.ConsultaExterna;
import net.wasys.getdoc.domain.enumeration.StatusConsultaExterna;
import net.wasys.getdoc.domain.service.ParametroService;
import net.wasys.getdoc.domain.service.ParametroService.P;
import net.wasys.getdoc.domain.service.webservice.SOAPWebServiceClient;
import net.wasys.getdoc.domain.vo.ConfiguracoesWsCredilinkVO;
import net.wasys.getdoc.domain.vo.CredilinkRequestVO;
import net.wasys.getdoc.domain.vo.WebServiceClientVO;
import net.wasys.getdoc.soapws.SOAPBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.xml.soap.*;
import java.util.List;

@Service
public class CredilinkService extends SOAPWebServiceClient {

	private static final String CAMPO_USUARIO = "usuario";
	private static final String CAMPO_SENHA = "senha";
	private static final String CAMPO_SIGLA = "sigla";

	@Autowired private ParametroService parametroService;

	@Override
	protected MultiValueMap<String, Object> getParametrosApi(WebServiceClientVO vo) {

		ConfiguracoesWsCredilinkVO cwid = parametroService.getConfiguracaoAsObject(P.CONFIGURACOES_WS_CREDILINK, ConfiguracoesWsCredilinkVO.class);
		MultiValueMap<String, Object> queryParams = new LinkedMultiValueMap<>(2);

		queryParams.add(CAMPO_USUARIO, cwid.getUsuario());
		queryParams.add(CAMPO_SENHA, cwid.getSenha());
		queryParams.add(CAMPO_SIGLA, cwid.getSigla());

		return queryParams;
	}

	@Override
	protected MultiValueMap<String, Object> getParametros(WebServiceClientVO vo) {

		CredilinkRequestVO vo2 = (CredilinkRequestVO) vo;
		String cpfCnpj = vo2.getCpfCnpj();
		String nome = vo2.getNome();
		String telefone = vo2.getTelefone();

		MultiValueMap<String, Object> parametros = new LinkedMultiValueMap<>(3);

		parametros.add(CredilinkRequestVO.CAMPO_CPF_CNPJ, cpfCnpj);
		parametros.add(CredilinkRequestVO.CAMPO_NOME, nome);
		parametros.add(CredilinkRequestVO.CAMPO_TELEFONE, telefone);

		return parametros;
	}

	@Override
	protected String extrairResultado(JsonNode resultadoJson) {

		JsonNode respostaSucesso = resultadoJson.findPath("return");

		if(!respostaSucesso.isMissingNode()) {
			String respostaSucessoStr = respostaSucesso.toString();
			respostaSucessoStr = respostaSucessoStr.replaceAll("^\"", "");
			String respostaParaJson = converterRespostaParaJson(respostaSucessoStr);
			return respostaParaJson;
		}

		return "";
	}

	@Override
	protected SOAPBuilder criarSOAPBuilder(MultiValueMap<String, Object> parametros) {
		String namespace = "br";
		String namespaceUri = "http://br.com.Integracao.Webservice/";
		String baseElement = "completo";

		return new SOAPBuilder(parametros, namespace, namespaceUri, baseElement) {

			@Override
			protected void adicionarFilhos(String key, List<Object> values, SOAPElement soapBodyElem) throws SOAPException {
				//tira o namespace
				String namespace2 = this.namespace;
				this.namespace = null;

				super.adicionarFilhos(key, values, soapBodyElem);

				//coloca o namespace de volta
				this.namespace = namespace2;
			}
		};
	}

	@Override
	protected String getEndpoint() {
		ConfiguracoesWsCredilinkVO cwid = parametroService.getConfiguracaoAsObject(P.CONFIGURACOES_WS_CREDILINK, ConfiguracoesWsCredilinkVO.class);
		return cwid.getEndpoint();
	}

	@Override
	protected StatusConsultaExterna validarSucessoOuErro(ConsultaExterna consultaExterna, JsonNode resultadoJson) {
		JsonNode respostaNode = resultadoJson.findPath("return");
		return !respostaNode.isMissingNode() ? StatusConsultaExterna.SUCESSO : StatusConsultaExterna.ERRO;
	}
}
