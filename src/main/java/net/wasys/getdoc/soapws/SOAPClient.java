package net.wasys.getdoc.soapws;

import com.sun.xml.messaging.saaj.client.p2p.HttpSOAPConnectionFactory;
import net.wasys.util.DummyUtils;
import net.wasys.util.other.RepeatTry;
import org.apache.commons.lang3.StringUtils;

import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;

import lombok.extern.slf4j.Slf4j;

import static net.wasys.util.DummyUtils.systraceThread;

public class SOAPClient {

	private long repeatInterval = 5000;
	private int repeatTimes = 3;
	private Integer proxyPort;
	private String proxyHost;

	public String chamarWebService(SOAPMessage soapMessage, String endpoint) throws Exception {

		RepeatTry<String> rt = new RepeatTry<>(repeatTimes, repeatInterval);
		rt.setToTry(() -> {
			return callSoapWebService(soapMessage, endpoint);
		});

		return rt.execute();
	}

	public String callSoapWebService(SOAPMessage soapMessage, String endpoint) throws Exception {

		SOAPMessage response = fazerChamada(soapMessage, endpoint);

		String responseString = "";
		if(response != null) {
			responseString = tranformarResponse(response);
		}
		return responseString;
	}

	private SOAPMessage fazerChamada(SOAPMessage soapMessage, String endpoint) throws Exception {

		SOAPConnection conexao = null;
		SOAPMessage response = null;
		Socket socket = null;
		try {
			conexao = criarConexao();

			Proxy proxy = null;
			socket = new Socket();

			if(StringUtils.isNotBlank(proxyHost)) {
				SocketAddress sockaddr = new InetSocketAddress(proxyHost, proxyPort);
				socket.connect(sockaddr, 10000);
				proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(socket.getInetAddress(), proxyPort));
				systraceThread("configurando proxy host: " + proxyHost + " port: " + proxyPort + " endpoint: " + endpoint);
			}

			final Proxy proxy2 = proxy;
			URL endpoint2 = new URL(null, endpoint, new URLStreamHandler() {
				protected URLConnection openConnection (URL url) throws IOException {
					URL clone = new URL(url.toString());
					URLConnection connection = null;
					if(proxy2 != null) {
						systraceThread("conectando com proxy " + endpoint);
						connection = clone.openConnection(proxy2);
					} else {
						connection = clone.openConnection();
					}
					connection.setConnectTimeout(20 * 1000);
					connection.setReadTimeout(20 * 1000);
					return connection;
				}
			});

			response = conexao.call(soapMessage, endpoint2);
		}
		finally {
			fecharConexao(conexao);
			if (socket != null) {
				socket.close();
			}
		}

		return response;
	}

	private String tranformarResponse(SOAPMessage response) {

		String responseString = null;

		try {
			Source content = response.getSOAPPart().getContent();
			responseString = new SOAPTransformer().transformar(content);
		} catch (SOAPException | TransformerException | UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		return responseString;
	}

	private SOAPConnection criarConexao() throws SOAPException {

		SOAPConnectionFactory soapConnectionFactory = HttpSOAPConnectionFactory.newInstance();

		return soapConnectionFactory.createConnection();
	}

	private void fecharConexao(SOAPConnection conexao) {

		if(conexao != null) {
			try {
				conexao.close();
			} catch (SOAPException e) {
				e.printStackTrace();
			}
		}
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public void setProxyPort(Integer proxyPort) {
		this.proxyPort = proxyPort;
	}
}