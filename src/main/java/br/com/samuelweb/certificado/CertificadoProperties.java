////
//// Source code recreated from a .class file by IntelliJ IDEA
//// (powered by Fernflower decompiler)
////
//
//package br.com.samuelweb.certificado;
//
//import br.com.samuelweb.certificado.exception.CertificadoException;
//import com.sun.net.ssl.internal.ssl.Provider;
//import net.wasys.util.DummyUtils;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.security.Security;
//
//public class CertificadoProperties {
//    public CertificadoProperties() {
//    }
//
//    public static void inicia(Certificado certificado, InputStream iSCacert) throws CertificadoException {
//        System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
//        System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");
//        Security.addProvider(new Provider());
//        System.clearProperty("javax.net.ssl.keyStore");
//        System.clearProperty("javax.net.ssl.keyStorePassword");
//        System.clearProperty("javax.net.ssl.trustStore");
//        System.setProperty("jdk.tls.client.protocols", "TLSv1.2");
//        if (certificado.getTipo().equals("windows")) {
//            System.setProperty("javax.net.ssl.keyStoreProvider", "SunMSCAPI");
//            System.setProperty("javax.net.ssl.keyStoreType", "Windows-MY");
//            System.setProperty("javax.net.ssl.keyStoreAlias", certificado.getNome());
//        } else if (certificado.getTipo().equals("arquivo") || certificado.getTipo().equals("arquivo_bytes")) {
//            System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");
//            System.setProperty("javax.net.ssl.keyStore", certificado.getArquivo());
//        }
//
//        System.setProperty("javax.net.ssl.keyStorePassword", certificado.getSenha());
//        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
//        String cacert = "";
//
//        try {
//            File file = File.createTempFile("tempfile", ".tmp");
//            OutputStream out = new FileOutputStream(file);
//            byte[] bytes = new byte[1024];
//
//            while(true) {
//                int read;
//                if ((read = iSCacert.read(bytes)) == -1) {
//                    out.close();
//                    cacert = file.getAbsolutePath();
//                    DummyUtils.deleteOnExitFile(file);
//                    break;
//                }
//
//                out.write(bytes, 0, read);
//            }
//        } catch (IOException var7) {
//            throw new CertificadoException(var7.getMessage());
//        }
//
//        System.setProperty("javax.net.ssl.trustStore", cacert);
//    }
//}
