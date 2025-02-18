package net.wasys.util.crawler;


import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.correiows.Exception;
import org.apache.commons.lang.StringUtils;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static net.wasys.util.DummyUtils.systraceThread;

public class Crawler {

    private final String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.116 Safari/537.36";
    private Map<String, String> campos;
    private Map<String, String> headers;
    private CookieStore cookieStore;
    private CloseableHttpClient httpclient;
    private String dominio;
    private int timeout;
    private int maxTentativas = 2;
    private Boolean persistirHeaders;
    private HttpHost proxy = null;

    public Crawler() {
        this.build();
    }

    public void build() {

        try {
            cookieStore = new BasicCookieStore();
            headers = new HashMap<String, String>();
            campos = new HashMap<String, String>();
            timeout = 30;
            persistirHeaders = false;

            iniciarHeaders();

            SSLContextBuilder builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    builder.build());

            RequestConfig requestConfig = RequestConfig
                    .custom()
                    .setCookieSpec(CookieSpecs.STANDARD)
                    .setConnectTimeout(timeout * 1000)
                    .setSocketTimeout(timeout * 1000)
                    .setConnectionRequestTimeout(timeout * 1000)
                    .build();
            httpclient = HttpClients
                    .custom()
                    .setRedirectStrategy(new LaxRedirectStrategy())
                    .setDefaultRequestConfig(requestConfig)
                    .setDefaultCookieStore(cookieStore)
                    .setSSLSocketFactory(sslsf)
                    .setUserAgent(userAgent)
                    .setProxy(proxy)
                    .build();
        }
        catch (java.lang.Exception e) {
            throw new RuntimeException(e);
        }
    }

    private RespostaHTTP request(HttpRequestBase request, int tentativa) throws IOException {
        RespostaHTTP retorno = new RespostaHTTP();
        retorno.setUri(request.getURI());

        if (this.headers.size() > 0) {
            for (Map.Entry<String, String> entry : this.headers.entrySet()) {
                request.addHeader(entry.getKey(), entry.getValue());
            }
        }

        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(request);
            if(response.getStatusLine().getStatusCode() > 299 && tentativa < getMaxTentativas()){
                tentativa++;
                response.close();
                return request(request, tentativa);
            }
            retorno.setStatusCode(response.getStatusLine().getStatusCode());
            HttpEntity entity = response.getEntity();
            String content = EntityUtils.toString(entity);
            retorno.setBody(Jsoup.parse(content));

            if(persistirHeaders) {
                Header[] headers = response.getAllHeaders();
                for (Header header : headers) {
                    retorno.addHeader(header.getName(), header.getValue());
                    if (!this.headers.containsKey(header.getName())) {
                        this.addHeader(header.getName(), header.getValue());
                    }
                }
            }
            return retorno;
        }
        finally {
            request.releaseConnection();
            if(response != null) {
                response.close();
            }
        }
    }

    public RespostaHTTP get(String url) throws IOException {
        HttpGet request;
        request = new HttpGet(url);

        return this.request(request, 0);
    }

    public RespostaHTTP get(String url, String... params) throws IOException {
        HttpGet request;
        URI uri = this.getUri(url, params);
        request = new HttpGet(uri);

        return this.request(request, 0);
    }

    public RespostaHTTP put(String url, String... params) throws IOException {
        ArrayList<NameValuePair> valores = new ArrayList<NameValuePair>();
        URI uri = this.getUri(url);
        HttpPut request = new HttpPut(uri);
        for (String param : params) {
            if (this.campos.containsKey(param)) {
                valores.add(new BasicNameValuePair(param, this.campos.get(param)));
            }
        }

        try {
            request.setEntity(new UrlEncodedFormEntity(valores));
        } catch (UnsupportedEncodingException e) {
            systraceThread(DummyUtils.getExceptionMessage(e), LogLevel.ERROR);
        }

        return this.request(request, 0);
    }

    public String postJSON(String url, String json) throws IOException {

        URI uri = this.getUri(url);
        HttpPost request = new HttpPost(uri);
        StringEntity requestEntity = new StringEntity(
                json,
                ContentType.APPLICATION_JSON);

        request.setEntity(requestEntity);

        RespostaHTTP resposta = this.request(request, 0);
        String text = null;
        if(resposta != null) {
            Document body = resposta.getBody();
            Elements elements = body.select("body");
            text = elements.text();
        }
        return text;
    }

    public RespostaHTTP post(String url, String... params) throws IOException {
        ArrayList<NameValuePair> valores = new ArrayList<NameValuePair>();
        URI uri = this.getUri(url);
        HttpPost request = new HttpPost(uri);
        for (String param : params) {
            if (this.campos.containsKey(param)) {
                valores.add(new BasicNameValuePair(param, this.campos.get(param)));
            }
        }

        try {
            request.setEntity(new UrlEncodedFormEntity(valores));
        } catch (UnsupportedEncodingException e) {
            systraceThread(DummyUtils.getExceptionMessage(e), LogLevel.ERROR);
        }

        return this.request(request, 0);
    }

    public File baixarArquivo(String url, String nomeArquivo, String extensao, String... params) {
        File tempFile = null;
        try {

            HttpGet request;
            URI uri = this.getUri(url, params);
            request = new HttpGet(uri);

            HttpResponse response = httpclient.execute(request);
            HttpEntity entity = response.getEntity();

            int responseCode = response.getStatusLine().getStatusCode();

            if(responseCode != 200){
                return null;
            }

            InputStream is = entity.getContent();

            tempFile = File.createTempFile(nomeArquivo, "."+extensao);
            FileOutputStream fos = new FileOutputStream(tempFile);

            int inByte;
            while ((inByte = is.read()) != -1) {
                fos.write(inByte);
            }

            is.close();
            fos.close();


        } catch (Exception e) {
            systraceThread(DummyUtils.getExceptionMessage(e), LogLevel.ERROR);
        } finally {
            return tempFile;
        }
    }

    public URI getUri(String url, String... params) {
        try {
            String scheme, host, path, aux;
            if (dominio != null && StringUtils.isNotBlank(dominio) && !url.contains(dominio) && !url.contains("http")) {
                url = dominio + url;
            }

            if (url.contains("://")) {
                String[] urlArray = url.split("://");
                scheme = urlArray[0];
                aux = urlArray[1];
            } else {
                scheme = "http";
                aux = url;
            }

            if (aux.contains("/")) {
                host = aux.substring(0, aux.indexOf("/"));
                path = aux.substring(aux.indexOf("/"));
            } else {
                host = aux;
                path = "";
            }

            URIBuilder uri = new URIBuilder()
                    .setScheme(scheme)
                    .setHost(host);

            if (path.length() > 0) {
                uri.setPath(path);
            }

            if (params.length > 0) {
                for (String param : params) {
                    if (this.campos.containsKey(param)) {
                        uri.addParameter(param, this.campos.get(param));
                    }
                }
            }

            return uri.build();
        }
        catch (URISyntaxException e) {
            systraceThread(DummyUtils.getExceptionMessage(e), LogLevel.ERROR);
        }
        return null;
    }

    private void iniciarHeaders(){
        this.addHeader("User-Agent", userAgent);
        this.addHeader("Cache-Control", "no-cache");
        this.addHeader("Accept", "*/*");
        this.addHeader("Accept-Encoding", "gzip, deflate, br");
    }

    public void definirProxy(String ip, int porta, String scheme) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        proxy = new HttpHost(ip, porta, scheme);
        this.build();
    }

    public void addCookie(String nome, String valor) {
        this.addCookie(nome, valor, this.dominio);
    }

    public void addCookie(String nome, String valor, String dominio) {
        BasicClientCookie clientcookie = new BasicClientCookie(nome, valor);
        clientcookie.setExpiryDate(null);
        clientcookie.setPath("/");
        clientcookie.setDomain(dominio);
    }

    public Map<String, String> separarParametrosUrl(String url){
        Map<String, String> retorno = new LinkedHashMap<String, String>();
        String[] aux1 = url.split("\\?");
        String[] aux2 = aux1[1].split("&");
        for(String temp : aux2){
            String[] aux3 = temp.split("=");
            retorno.put(aux3[0], aux3[1]);
        }

        return retorno;
    }

    public void clearCookies() {
        this.cookieStore.clear();
    }

    public void addHeader(String nome, String valor) {
        this.headers.put(nome, valor);
    }

    public void clearHeaders() {
        this.headers.clear();
    }

    public void addCampo(String nome, String valor) {
        this.campos.put(nome, valor);
    }

    public void setCampos(Map<String, String> campos) {
        this.campos = campos;
    }

    public void deleteCampo(String nome) {
        this.campos.remove(nome);
    }

    public void setDominio(String dominio) {
        this.dominio = dominio;
    }

    public String getDominio() {
        return this.dominio;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public Boolean getPersistirHeaders() {
        return persistirHeaders;
    }

    public void setPersistirHeaders(Boolean persistirHeaders) {
        this.persistirHeaders = persistirHeaders;
    }

    public int getMaxTentativas() {
        return maxTentativas;
    }

    public void setMaxTentativas(int maxTentativas) {
        this.maxTentativas = maxTentativas;
    }

    public class RespostaHTTP {
        private URI uri;
        private Document body;
        private Map<String, String> headers = new HashMap<String, String>();
        private Integer statusCode;

        /**
         * @return the body
         */
        public Document getBody() {
            return body;
        }

        /**
         * @param body the body to set
         */
        public void setBody(Document body) {
            this.body = body;
        }

        /**
         * @return the headers
         */
        public Map<String, String> getHeaders() {
            return headers;
        }

        /**
         * @param headers the headers to set
         */
        public void setHeaders(Map<String, String> headers) {
            this.headers = headers;
        }

        public void addHeader(String nome, String valor) {
            this.headers.put(nome, valor);
        }

        /**
         * @return the statusCode
         */
        public Integer getStatusCode() {
            return statusCode;
        }

        /**
         * @param statusCode the statusCode to set
         */
        public void setStatusCode(Integer statusCode) {
            this.statusCode = statusCode;
        }

        public URI getUri() {
            return uri;
        }

        public void setUri(URI uri) {
            this.uri = uri;
        }
    }
}

