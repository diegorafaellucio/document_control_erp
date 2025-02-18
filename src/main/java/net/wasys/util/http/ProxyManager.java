package net.wasys.util.http;

import java.net.InetAddress;
import java.net.URI;
import java.net.URL;

import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;

import com.sun.deploy.net.proxy.BrowserProxyInfo;
import com.sun.deploy.net.proxy.ProxyConfigException;
import com.sun.deploy.net.proxy.ProxyInfo;
import com.sun.deploy.net.proxy.ProxyType;

import static net.wasys.util.DummyUtils.systraceThread;

/**
 * @author Felipe Maschio
 * @created 14/01/2010
 */
@SuppressWarnings("deprecation")
public class ProxyManager {

	private boolean proxyOk;
	protected HttpHost proxyHost;

	private Integer port;
	private String host;
	private String userName;
	private String password;
	private String urlScrip;

	private CustomCredentialsProvider credentialsProvider = new CustomCredentialsProvider();

	public ProxyManager(String host, Integer port, String userName, String password) {
		this.host = host;
		this.port = port;
		this.userName = userName;
		this.password = password;
	}

	public ProxyManager(String urlScrip) {

		try {
			if(!urlScrip.startsWith("http") && !urlScrip.startsWith("https")){
				urlScrip = "http://" + urlScrip;
			}    

			URL netUrl = new URL(urlScrip);
			String host = netUrl.getHost();
			if(host.startsWith("www")){
				host = host.substring("www".length()+1);
			}

			InetAddress ipAddress = InetAddress.getByName(host);
			String hostAddress = ipAddress.getHostAddress();
			String hostIpAddress = urlScrip.replaceAll(host, hostAddress);

			systraceThread("Encontrado ip para: " + urlScrip + " e convertido para: " + hostIpAddress);

			urlScrip = hostIpAddress;
		}
		catch ( Exception e ){
			systraceThread("Não foi encontrado o ip para: " + urlScrip, LogLevel.ERROR);
			e.printStackTrace();
		}

		this.urlScrip = urlScrip;
	}

	public void configura(DefaultHttpClient httpClient, HttpRequestBase httpRequest) throws ProxyConfigException {

		if(!proxyOk) {

			if(StringUtils.isNotBlank(urlScrip)) {
				try {

					BrowserProxyInfo b = new BrowserProxyInfo();
					b.setType(ProxyType.AUTO);
					b.setAutoConfigURL(urlScrip);
					AutoProxyHandler handler = new AutoProxyHandler();
					handler.init(b);

					URI uri = httpRequest.getURI();

					ProxyInfo[] ps = handler.getProxyInfo(uri);
					for(ProxyInfo p : ps){

						String host = p.getProxy();
						int port = p.getPort();

						setProxy(httpClient, httpRequest, host, port);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else {

				setProxy(httpClient, httpRequest, host, port);
			}
		}
	}

	private void setProxy(DefaultHttpClient httpClient, HttpRequestBase httpRequest, String host, int port) {

		systraceThread("configurando proxy host: " + host + " port: " + port + " user: " + userName + " password: " + password);

		credentialsProvider = new CustomCredentialsProvider();

		final HttpHost proxy = getProxy(host, port);

		httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);

		//se mandou configurar denovo pode ser que a senha esteja errada
		if(httpClient.getCredentialsProvider().equals(credentialsProvider)) {
			credentialsProvider.clear();
		}

		httpClient.setCredentialsProvider(credentialsProvider);

		String userName = getUserName();
		String password = getPassword();

		if(userName != null && password != null) {
			httpClient.getCredentialsProvider().setCredentials(
					new AuthScope(host, port), 
					new UsernamePasswordCredentials(userName, password));
		}
		else {
			httpClient.getCredentialsProvider().setCredentials(
					new AuthScope(host, port), 
					null);
		}
	}

	public void shutdown() {
		setProxyOk(false);
	}

	public HttpHost getProxy(String host, int port) {

		if(proxyHost == null) {
			if (host != null) {
				proxyHost = new HttpHost(host, port);
			}
		}

		return proxyHost;
	}

	public void setProxyOk(boolean proxyOk) {
		this.proxyOk = proxyOk;
	}

	public class CustomCredentialsProvider implements CredentialsProvider {

		public void setCredentials(AuthScope authscope, Credentials credentials) {}

		public Credentials getCredentials(AuthScope authscope) {
			return new UsernamePasswordCredentials(userName, password);
		}

		public void clear() {}
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	public String getHost() {
		return host;
	}

	public Integer getPort() {
		return port;
	}

	@Override
	public String toString() {
		return "proxyHost: " + proxyHost + ":" + port + " " + userName + "@" + password;
	}
}
