package net.wasys.getdoc.domain.service.webservice;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.JsonNode;
import net.wasys.getdoc.domain.entity.ConsultaExterna;
import net.wasys.getdoc.domain.enumeration.StatusConsultaExterna;
import net.wasys.getdoc.domain.service.ResourceService;
import net.wasys.getdoc.domain.service.webservice.credilink.CredilinkService;
import net.wasys.getdoc.domain.vo.WebServiceClientVO;
import net.wasys.getdoc.soapws.SOAPBuilder;
import net.wasys.getdoc.soapws.SOAPClient;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.rest.jackson.ObjectMapper;
import net.wasys.util.rest.jackson.XMLMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.MultiValueMap;

import javax.xml.soap.SOAPMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static net.wasys.util.DummyUtils.systraceThread;

public abstract class SOAPWebServiceClient extends WebServiceClient {

	protected abstract String getEndpoint();
	protected abstract SOAPBuilder criarSOAPBuilder(MultiValueMap<String, Object> parametros);
	protected abstract String extrairResultado(JsonNode resultadoJson);

	private ObjectMapper om = new ObjectMapper();
	private XMLMapper xmlMapper = new XMLMapper();

	@Override
	protected ConsultaExterna chamarWebService(MultiValueMap<String, Object> parametros) {

		SOAPBuilder builder = criarSOAPBuilder(parametros);

		ConsultaExterna consultaExterna = new ConsultaExterna();
		try {
			SOAPMessage soapMessage = builder.criarSOAPMessage();

			//sysout do XML da chamada (ou seja, do soapMessage)
			/*ByteArrayOutputStream out = new ByteArrayOutputStream();
			soapMessage.writeTo(out);
			String strMsg = new String(out.toByteArray());
			DummyUtils.systraceThread(strMsg);*/

			String endpoint = getEndpoint();
			consultaExterna = chamarSOAPWebService(soapMessage, endpoint);
		}
		catch (Exception e) {

			e.printStackTrace();
			String stackTrace = DummyUtils.getStackTrace(e);
			consultaExterna.setStackTrace(stackTrace);

			String exceptionMessage = DummyUtils.getExceptionMessage(e);
			consultaExterna.setMensagem(exceptionMessage);
		}

		return consultaExterna;
	}

	@Override
	protected void atualizarConsultaExterna(ConsultaExterna consultaExterna, WebServiceClientVO vo, MultiValueMap<String, Object> parametros) {

		super.atualizarConsultaExterna(consultaExterna, vo, parametros);

		extrairResultadoDaMensagemSOAP(consultaExterna);
	}

	private ConsultaExterna chamarSOAPWebService(SOAPMessage soapMessage, String endpoint) {

		String resultadoXml = null;
		String resultadoJson = null;
		String stackTrace = null;
		String mensagem = null;

		String proxyHost = resourceService.getValue(ResourceService.PROXY_HOST);
		Integer proxyPort =  resourceService.getValue(ResourceService.PROXY_PORT, Integer.class);

		long inicio = 0;
		long fim;

		try {
			inicio = System.currentTimeMillis();
			SOAPClient soapClient = new SOAPClient();
			soapClient.setProxyHost(proxyHost);
			soapClient.setProxyPort(proxyPort);

			//mock
			if(getClass().equals(CredilinkService.class)) {
				resultadoXml = "<S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
						"   <S:Body>\n" +
						"      <ns2:completoResponse xmlns:ns2=\"http://br.com.Integracao.Webservice/\">\n" +
						"         <return><![CDATA[<?xml version='1.0' encoding='iso-8859-1'?><credilink_webservice><consulta_ccf619><restricoes_bancarias><quantidade>0</quantidade><restricao><mensagem>NAO EXISTEM RESTRICOES BANCARIAS PARA ESSE CPF/CNPJ</mensagem></restricao></restricoes_bancarias><info_restricao>* NADA CONSTA *</info_restricao><nome_completo>VITOR ANDRE SAVI</nome_completo><statusReceita>REGULAR</statusReceita><cpf>11663586900</cpf><filiacao></filiacao><identidade></identidade><data_nascimento>30/03/00</data_nascimento><datafundacao></datafundacao><signo>ARIES</signo><codigo_consulta>9655053807</codigo_consulta><consultas_realizadas>5</consultas_realizadas><restricoes_lojistas><quantidade>0</quantidade><restricao><mensagem>NAO EXISTEM RESTRICOES LOJISTAS PARA ESSE CPF/CNPJ</mensagem></restricao></restricoes_lojistas><cheques_pre_datados><quantidade>0</quantidade><cheque><emissao></emissao><banco></banco><numerocheque></numerocheque><vencimento></vencimento><valor></valor><cliente></cliente></cheque></cheques_pre_datados><alertas><quantidade>0</quantidade><alerta><mensagem>NAO EXISTEM ALERTAS PARA ESTE CPF/CNPJ</mensagem></alerta></alertas></consulta_ccf619><consulta_telefone_proprietario></consulta_telefone_proprietario><consulta_emails_proprietario><emails>NULL</emails></consulta_emails_proprietario><parentes></parentes><vizinhos></vizinhos><dados_sociedades></dados_sociedades><veiculos></veiculos><obito></obito><moradores></moradores><comercial></comercial><iptu></iptu><trabalho></trabalho><escolaridade></escolaridade><outros_contatos></outros_contatos><rendapresumida>788</rendapresumida><consulta_telefone_referencia></consulta_telefone_referencia><consulta_bancos><quantidade>0</quantidade></consulta_bancos><consultas_realizadas><consulta><codigo_credilink>WEBSERVICE INTEGRACAO - No.: 9655053807 S/R</codigo_credilink><datahora>08/12/18 18:53:34</datahora></consulta><consulta><codigo_credilink>WEBSERVICE INTEGRACAO - No.: 6188196304 S/R</codigo_credilink><datahora>07/12/18 23:40:58</datahora></consulta><consulta><codigo_credilink>WEBSERVICE INTEGRACAO - No.: 611795441 S/R</codigo_credilink><datahora>07/12/18 22:43:02</datahora></consulta><consulta><codigo_credilink>WEBSERVICE INTEGRACAO - No.: 2688312040 S/R</codigo_credilink><datahora>07/12/18 22:42:53</datahora></consulta><consulta><codigo_credilink>WEBSERVICE INTEGRACAO - No.: 6723215343 S/R</codigo_credilink><datahora>07/12/18 22:31:47</datahora></consulta></consultas_realizadas><passagem><consulta><codigo_credilink>WEBSERVICE INTEGRACAO - No.: 9655053807 S/R</codigo_credilink><datahora>08/12/18 18:53:34</datahora></consulta><consulta><codigo_credilink>WEBSERVICE INTEGRACAO - No.: 6188196304 S/R</codigo_credilink><datahora>07/12/18 23:40:58</datahora></consulta><consulta><codigo_credilink>WEBSERVICE INTEGRACAO - No.: 611795441 S/R</codigo_credilink><datahora>07/12/18 22:43:02</datahora></consulta><consulta><codigo_credilink>WEBSERVICE INTEGRACAO - No.: 2688312040 S/R</codigo_credilink><datahora>07/12/18 22:42:53</datahora></consulta><consulta><codigo_credilink>WEBSERVICE INTEGRACAO - No.: 6723215343 S/R</codigo_credilink><datahora>07/12/18 22:31:47</datahora></consulta></passagem></credilink_webservice>]]></return>\n" +
						"      </ns2:completoResponse>\n" +
						"   </S:Body>\n" +
						"</S:Envelope>";
			}
			else {
				resultadoXml = soapClient.chamarWebService(soapMessage, endpoint);
			}

			fim = System.currentTimeMillis();
			//DummyUtils.systraceThread(resultadoXml);
		}
		catch (Exception e) {
			fim = System.currentTimeMillis();

			mensagem = DummyUtils.getExceptionMessage(e);
			mensagem = String.format("Erro ao chamar SOAP WebService. Endpoint: %s. Parametros: %s. Erro: %s", endpoint, soapMessage, mensagem);
			systraceThread(mensagem, LogLevel.ERROR);
			e.printStackTrace();

			stackTrace = DummyUtils.getStackTrace(e);
		}

		resultadoJson = StringUtils.isNotBlank(resultadoXml) ? converterRespostaParaJson(resultadoXml) : "{}";

		ConsultaExterna consultaExterna = new ConsultaExterna();
		consultaExterna.setResultado(resultadoJson);
		consultaExterna.setStackTrace(stackTrace);
		consultaExterna.setMensagem(mensagem);
		consultaExterna.setTempoExecucao(fim - inicio);

		return consultaExterna;
	}

	private void extrairResultadoDaMensagemSOAP(ConsultaExterna consultaExterna) {

		String resultado = consultaExterna.getResultado();
		if(StringUtils.isBlank(resultado)) {
			return;
		}

		try {
			JsonNode resultadoJson = om.readTree(resultado);

			String resultadoExtraido = "";

			StatusConsultaExterna status = consultaExterna.getStatus();
			if(StatusConsultaExterna.SUCESSO.equals(status)) {
				resultadoExtraido = extrairResultado(resultadoJson);
			}
			else {
				if(resultadoJson != null && !resultadoJson.isMissingNode()) {
					resultadoExtraido = resultadoJson.toString();
				}
			}

			consultaExterna.setResultado(resultadoExtraido);
		}
		catch (IOException e) {

			String stackTrace = DummyUtils.getStackTrace(e);
			consultaExterna.setStackTrace(stackTrace);
			consultaExterna.setStatus(StatusConsultaExterna.ERRO);

			String mensagem = DummyUtils.getExceptionMessage(e);
			consultaExterna.setMensagem(mensagem);
		}
	}

	protected String converterRespostaParaJson(String resultadoXml) {

		//sysout(resultadoXml);
		Object resultadoCompletoObject = xmlMapper.readValue(resultadoXml, Object.class);
		String resultadoCompletoJson = om.writeValueAsString(resultadoCompletoObject);
		return resultadoCompletoJson;
	}
}
