/*package net.wasys.getdoc.domain.service.webservice.fazenda.nfe;

import br.com.samuelweb.certificado.Certificado;
import br.com.samuelweb.certificado.CertificadoService;
import br.com.samuelweb.nfe.NfeWeb;
import br.com.samuelweb.nfe.dom.ConfiguracoesWebNfe;
import br.com.samuelweb.nfe.util.ConstantesUtil;
import br.com.samuelweb.nfe.util.Estados;
import br.com.samuelweb.nfe.util.XmlUtil;
import br.inf.portalfiscal.nfe.schema.retdistdfeint.RetDistDFeInt;
import com.fasterxml.jackson.databind.JsonNode;
import net.wasys.getdoc.domain.entity.ConsultaExterna;
import net.wasys.getdoc.domain.enumeration.StatusConsultaExterna;
import net.wasys.getdoc.domain.service.ParametroService;
import net.wasys.getdoc.domain.service.ParametroService.P;
import net.wasys.getdoc.domain.service.ResourceService;
import net.wasys.getdoc.domain.service.webservice.SOAPWebServiceClient;
import net.wasys.getdoc.domain.vo.ConfiguracoesWsNfeInteresseVO;
import net.wasys.getdoc.domain.vo.NfeInteresseRequestVO;
import net.wasys.getdoc.domain.vo.WebServiceClientVO;
import net.wasys.getdoc.soapws.SOAPBuilder;
import net.wasys.util.DummyUtils;
import net.wasys.util.other.Criptografia;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NfeInteresseService extends SOAPWebServiceClient {

	private static final String CNPJ_INTERESSADO = "cnpjInteressado";

	@Autowired private ParametroService parametroService;
	@Autowired private ResourceService resourceService;

	@Override
	protected ConsultaExterna chamarWebService(MultiValueMap<String, Object> parametros) {

		ConfiguracoesWsNfeInteresseVO cwid = parametroService.getConfiguracaoAsObject(ParametroService.P.CONFIGURACOES_WS_NFE_INTERESSE, ConfiguracoesWsNfeInteresseVO.class);
		String resultadoJson = null;
		String stackTrace = null;
		String mensagem = null;

		try {
			String certPath = resourceService.getValue(ResourceService.CERT_PATH);
			String certCod = resourceService.getValue(ResourceService.CERT_COD);
			File certCodFile = new File(certCod);
			certCod = FileUtils.readFileToString(certCodFile, "UTF-8");
			certCod = Criptografia.decrypt(Criptografia.CERT_COD, certCod);
			String cnpjInteressado = cwid.getCnpjInteressado();

			String chaveNfe = (String) parametros.getFirst(NfeInteresseRequestVO.CHAVE_NFE);
			chaveNfe = chaveNfe.replace(" ", "");
			String ufAutorStr = (String) parametros.getFirst(NfeInteresseRequestVO.UF_AUTOR);
			Estados ufAutor = Estados.valueOf(ufAutorStr);

			Certificado certificado = CertificadoService.certificadoPfx(certPath, certCod);
			ConfiguracoesWebNfe config = ConfiguracoesWebNfe.iniciaConfiguracoes(ufAutor, String.valueOf(ConstantesUtil.AMBIENTE.PRODUCAO), certificado, null);
			RetDistDFeInt retorno = NfeWeb.distribuicaoDfe(config, ConstantesUtil.TIPOS.CNPJ, cnpjInteressado, ConstantesUtil.TIPOS.CHAVE, chaveNfe);

			String xMotivo = retorno.getXMotivo();
			if(!"Documento localizado".equals(xMotivo)) {
				resultadoJson = DummyUtils.objectToJson(retorno);
			}
			else {
				RetDistDFeInt.LoteDistDFeInt loteDistDFeInt = retorno.getLoteDistDFeInt();
				List<RetDistDFeInt.LoteDistDFeInt.DocZip> docZip = loteDistDFeInt.getDocZip();

				if(docZip.isEmpty()) {
					resultadoJson = "";
				}
				else {
					RetDistDFeInt.LoteDistDFeInt.DocZip zip = docZip.get(0);
					byte[] value = zip.getValue();
					String resultadoXml = XmlUtil.gZipToXml(value);
					resultadoJson = StringUtils.isNotBlank(resultadoXml) ? converterRespostaParaJson(resultadoXml) : "";
				}
			}
		}
		catch (Exception e) {
			mensagem = DummyUtils.getExceptionMessage(e);
			mensagem = "Erro ao chamar Rest WebService: " + mensagem;
			DummyUtils.systraceThread(mensagem);
			e.printStackTrace();
			stackTrace = DummyUtils.getStackTrace(e);
		}

		ConsultaExterna consultaExterna = new ConsultaExterna();
		consultaExterna.setResultado(resultadoJson);
		consultaExterna.setStackTrace(stackTrace);
		consultaExterna.setMensagem(mensagem);
		return consultaExterna;
	}

	@Override
	protected MultiValueMap<String, Object> getParametrosApi(WebServiceClientVO vo) {

		ConfiguracoesWsNfeInteresseVO cwid = parametroService.getConfiguracaoAsObject(P.CONFIGURACOES_WS_NFE_INTERESSE, ConfiguracoesWsNfeInteresseVO.class);

		MultiValueMap<String, Object> queryParams = new LinkedMultiValueMap<>(2);
		queryParams.add(CNPJ_INTERESSADO, cwid.getCnpjInteressado());
		return queryParams;
	}

	@Override
	protected MultiValueMap<String, Object> getParametros(WebServiceClientVO vo) {

		NfeInteresseRequestVO vo2 = (NfeInteresseRequestVO) vo;
		String chaveNfe = vo2.getChaveNfe();
		String codUfAutor = vo2.getUfAutor();

		MultiValueMap<String, Object> parametros = new LinkedMultiValueMap<>(3);
		parametros.add(NfeInteresseRequestVO.CHAVE_NFE, chaveNfe);
		parametros.add(NfeInteresseRequestVO.UF_AUTOR, codUfAutor);
		return parametros;
	}

	@Override
	protected String extrairResultado(JsonNode resultadoJson) {
		JsonNode respostaSucesso = resultadoJson.findPath("infNFe");
		if(!respostaSucesso.isMissingNode()) {
			String respostaSucessoStr = respostaSucesso.toString();
			return respostaSucessoStr;
		}
		return resultadoJson.toString();
	}

	@Override
	protected String getEndpoint() {
		ConfiguracoesWsNfeInteresseVO cwid = parametroService.getConfiguracaoAsObject(P.CONFIGURACOES_WS_NFE_INTERESSE, ConfiguracoesWsNfeInteresseVO.class);
		return cwid.getEndpoint();
	}

	@Override
	protected SOAPBuilder criarSOAPBuilder(MultiValueMap<String, Object> parametros) {
		return null;
	}

	@Override
	protected StatusConsultaExterna validarSucessoOuErro(ConsultaExterna consultaExterna, JsonNode resultadoJson) {
		JsonNode infNFe = resultadoJson.findPath("infNFe");
		JsonNode xMotivo = resultadoJson.findPath("xmotivo");
		return !infNFe.isMissingNode() || !xMotivo.isMissingNode() ? StatusConsultaExterna.SUCESSO : StatusConsultaExterna.ERRO;
	}
}
*/