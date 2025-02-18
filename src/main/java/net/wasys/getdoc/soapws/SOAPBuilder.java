package net.wasys.getdoc.soapws;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.soap.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class SOAPBuilder {

	private static final String PREFERRED_PREFIX = "soapenv";
	private static final String SOAP_ENV_NAMESPACE = "http://schemas.xmlsoap.org/soap/envelope/";

	private MultiValueMap<String, Object> parametros;
	protected String namespace;
	protected String namespaceUri;
	protected String baseElement;

	public SOAPBuilder(MultiValueMap<String, Object> parametros, String namespace, String namespaceUri, String baseElement) {
		this.parametros = parametros;
		this.namespace = namespace;
		this.namespaceUri = namespaceUri;
		this.baseElement = baseElement;
	}

	public SOAPMessage criarSOAPMessage() throws SOAPException {

		SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();

		criarEnvelope(soapMessage.getSOAPPart().getEnvelope());
		criarHeaders(soapMessage.getMimeHeaders());

		trocarPrefixos(soapMessage);

		soapMessage.saveChanges();

		return soapMessage;
	}

	private void criarEnvelope(SOAPEnvelope envelope) throws SOAPException {

		envelope.addNamespaceDeclaration(namespace, namespaceUri);

		criarSoapHeader(envelope);

		SOAPBody soapBody = envelope.getBody();
		SOAPElement soapBodyElem = soapBody.addChildElement(baseElement, namespace);

		adicionarParametros(parametros, soapBodyElem);
	}

	protected SOAPHeader criarSoapHeader(SOAPEnvelope envelope) throws SOAPException {
		SOAPHeader soapHeader = envelope.getHeader();
		soapHeader = soapHeader != null ? soapHeader : envelope.addHeader();
		return soapHeader;
	}

	private void adicionarParametros(MultiValueMap<String, Object> parametros, SOAPElement soapBodyElem) throws SOAPException {

		Set<Entry<String,List<Object>>> entrySet = parametros.entrySet();
		for (Entry<String, List<Object>> entry : entrySet) {
			adicionarFilhos(entry.getKey(), entry.getValue(), soapBodyElem);
		}
	}

	@SuppressWarnings("unchecked")
	protected void adicionarFilhos(String key, List<Object> values, SOAPElement soapBodyElem) throws SOAPException {

		for (Object value : values) {

			if(value instanceof MultiValueMap) {
				SOAPElement childElement = namespace != null ? soapBodyElem.addChildElement(key, namespace) : soapBodyElem.addChildElement(key);
				adicionarObjetosFilhos((MultiValueMap<String, Object>) value, childElement);
			}
			else {
				SOAPElement childElement = namespace != null ? soapBodyElem.addChildElement(key, namespace) : soapBodyElem.addChildElement(key);
				childElement.addTextNode(String.valueOf(value));
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void adicionarObjetosFilhos(MultiValueMap<String, Object> value, SOAPElement soapBodyElem) throws SOAPException {

		Set<Entry<String,List<Object>>> entrySet = value.entrySet();
		for (Entry<String, List<Object>> entry : entrySet) {

			List<Object> values = entry.getValue();
			SOAPElement childElement;
			for (Object obj : values) {
				childElement = namespace != null ? soapBodyElem.addChildElement(entry.getKey(), namespace) : soapBodyElem.addChildElement(entry.getKey());

				if(obj instanceof LinkedMultiValueMap) {
					adicionarObjetosFilhos((MultiValueMap<String, Object>) obj, childElement);
				}
				else {
					childElement.addTextNode(String.valueOf(obj));
				}
			}
		}
	}

	private void criarHeaders(MimeHeaders headers) {
		headers.addHeader("Content-Type", "text/xml; charset=utf-8");
		headers.addHeader("User-Agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
	}

	private void trocarPrefixos(SOAPMessage soapMessage) throws SOAPException {

		trocarPrefixoEnvelope(soapMessage.getSOAPPart().getEnvelope());

		soapMessage.getSOAPHeader().setPrefix(PREFERRED_PREFIX);

		trocarPrefixoBody(soapMessage);
	}

	private void trocarPrefixoEnvelope(SOAPEnvelope envelope) throws SOAPException {

		envelope.removeNamespaceDeclaration(envelope.getPrefix());
		try {
			envelope.addNamespaceDeclaration(PREFERRED_PREFIX, SOAP_ENV_NAMESPACE);
		} catch (SOAPException e) {
			// TODO tratar erro
			e.printStackTrace();
		}
		envelope.setPrefix(PREFERRED_PREFIX);
	}

	private void trocarPrefixoBody(SOAPMessage soapMessage) throws SOAPException {
		SOAPBody body = soapMessage.getSOAPBody();
		body.setPrefix(PREFERRED_PREFIX);

		SOAPFault fault = body.getFault();
		if (fault != null) {
			fault.setPrefix(PREFERRED_PREFIX);
		}
	}
}