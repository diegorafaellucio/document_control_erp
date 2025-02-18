//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package br.com.samuelweb.certificado;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;

class SocketFactoryDinamico implements ProtocolSocketFactory {
    private SSLContext ssl;
    private X509Certificate certificate;
    private PrivateKey privateKey;
    private InputStream fileCacerts;
    private KeyStore ks;
    private String alias;

    public SocketFactoryDinamico(X509Certificate certificate, PrivateKey privateKey, InputStream fileCacerts, String sslProtocol, KeyStore ks, String alias) throws KeyManagementException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
        this.certificate = certificate;
        this.privateKey = privateKey;
        this.fileCacerts = fileCacerts;
        this.ssl = this.createSSLContext(sslProtocol);
        this.alias = alias;
        this.ks = ks;
    }

    public Socket createSocket(String host, int port, InetAddress localAddress, int localPort, HttpConnectionParams params) throws IOException {
        Socket socket = this.ssl.getSocketFactory().createSocket();
        socket.bind(new InetSocketAddress(localAddress, localPort));
        socket.connect(new InetSocketAddress(host, port), 60000);
        return socket;
    }

    public Socket createSocket(String host, int port, InetAddress clientHost, int clientPort) throws IOException {
        return this.ssl.getSocketFactory().createSocket(host, port, clientHost, clientPort);
    }

    public Socket createSocket(String host, int port) throws IOException {
        return this.ssl.getSocketFactory().createSocket(host, port);
    }

    private SSLContext createSSLContext(String sslProtocol) throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, KeyManagementException {
        KeyManager[] keyManagers = this.createKeyManagers();
        TrustManager[] trustManagers = this.createTrustManagers();
        SSLContext sslContext = SSLContext.getInstance(sslProtocol);
        sslContext.init(keyManagers, trustManagers, (SecureRandom)null);
        return sslContext;
    }

    private KeyManager[] createKeyManagers() {
        return new KeyManager[]{new SocketFactoryDinamico.NFKeyManager(this.certificate, this.privateKey)};
    }

    private TrustManager[] createTrustManagers() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(this.fileCacerts, "changeit".toCharArray());
        trustManagerFactory.init(trustStore);
        return trustManagerFactory.getTrustManagers();
    }

    private class NFKeyManager implements X509KeyManager {
        private final X509Certificate certificate;
        private final PrivateKey privateKey;

        NFKeyManager(X509Certificate certificate, PrivateKey privateKey) {
            this.certificate = certificate;
            this.privateKey = privateKey;
        }

        public String chooseClientAlias(String[] arg0, Principal[] arg1, Socket arg2) {
            return this.certificate.getIssuerDN().getName();
        }

        public String chooseServerAlias(String arg0, Principal[] arg1, Socket arg2) {
            return null;
        }

        public X509Certificate[] getCertificateChain(String arg0) {
            try {
                Certificate[] certificates = SocketFactoryDinamico.this.ks.getCertificateChain(SocketFactoryDinamico.this.alias);
                X509Certificate[] x509Certificates = new X509Certificate[certificates.length];
                System.arraycopy(certificates, 0, x509Certificates, 0, certificates.length);
                return x509Certificates;
            } catch (KeyStoreException var4) {
                return new X509Certificate[]{this.certificate};
            }
        }

        public String[] getClientAliases(String arg0, Principal[] arg1) {
            return new String[]{this.certificate.getIssuerDN().getName()};
        }

        public PrivateKey getPrivateKey(String arg0) {
            return this.privateKey;
        }

        public String[] getServerAliases(String arg0, Principal[] arg1) {
            return null;
        }
    }
}
