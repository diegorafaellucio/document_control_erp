////
//// Source code recreated from a .class file by IntelliJ IDEA
//// (powered by Fernflower decompiler)
////
//
//package br.com.samuelweb.certificado;
//
//import br.com.samuelweb.certificado.exception.CertificadoException;
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.UnsupportedEncodingException;
//import java.security.KeyManagementException;
//import java.security.KeyStore;
//import java.security.KeyStoreException;
//import java.security.NoSuchAlgorithmException;
//import java.security.NoSuchProviderException;
//import java.security.PrivateKey;
//import java.security.Provider;
//import java.security.Security;
//import java.security.UnrecoverableKeyException;
//import java.security.cert.CertificateException;
//import java.security.cert.X509Certificate;
//import java.time.LocalDate;
//import java.time.ZoneId;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Date;
//import java.util.Enumeration;
//import java.util.Iterator;
//import java.util.List;
//
//import org.apache.commons.httpclient.protocol.Protocol;
//import org.bouncycastle.asn1.*;
//import org.bouncycastle.x509.extension.X509ExtensionUtil;
//import sun.security.pkcs11.SunPKCS11;
//import sun.security.pkcs11.wrapper.CK_C_INITIALIZE_ARGS;
//import sun.security.pkcs11.wrapper.CK_TOKEN_INFO;
//import sun.security.pkcs11.wrapper.PKCS11;
//import sun.security.pkcs11.wrapper.PKCS11Exception;
//
//public class CertificadoService {
//    private static final DERObjectIdentifier CNPJ = new DERObjectIdentifier("2.16.76.1.3.3");
//    private static final DERObjectIdentifier CPF = new DERObjectIdentifier("2.16.76.1.3.1");
//
//    public CertificadoService() {
//    }
//
//    public static void inicializaCertificado(Certificado certificado, InputStream cacert) throws CertificadoException {
//        try {
//            KeyStore keyStore = getKeyStore(certificado);
//            X509Certificate certificate = getCertificate(certificado, keyStore);
//            PrivateKey privateKey = (PrivateKey)keyStore.getKey(certificado.getNome(), certificado.getSenha().toCharArray());
//            if (certificado.isAtivarProperties()) {
//                CertificadoProperties.inicia(certificado, cacert);
//            } else {
//                SocketFactoryDinamico socketFactory = new SocketFactoryDinamico(certificate, privateKey, cacert, certificado.getSslProtocol(), keyStore, certificado.getNome());
//                Protocol protocol = new Protocol("https", socketFactory, 443);
//                Protocol.registerProtocol("https", protocol);
//            }
//
//        } catch (KeyStoreException | NoSuchAlgorithmException | KeyManagementException | CertificateException | IOException | UnrecoverableKeyException var7) {
//            throw new CertificadoException(var7.getMessage());
//        }
//    }
//
//    public static Certificado certificadoPfxBytes(byte[] certificadoBytes, String senha) throws CertificadoException {
//        Certificado certificado = new Certificado();
//
//        try {
//            certificado.setArquivoBytes(certificadoBytes);
//            certificado.setSenha(senha);
//            certificado.setTipo("arquivo_bytes");
//            KeyStore keyStore = getKeyStore(certificado);
//            Enumeration<String> aliasEnum = keyStore.aliases();
//            String aliasKey = (String)aliasEnum.nextElement();
//            certificado.setNome(aliasKey);
//            certificado.setVencimento(DataValidade(certificado).toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
//            certificado.setDiasRestantes(diasRestantes(certificado));
//            certificado.setValido(valido(certificado));
//            return certificado;
//        } catch (KeyStoreException var6) {
//            throw new CertificadoException("Erro ao carregar informações do certificado:" + var6.getMessage());
//        }
//    }
//
//    public static Certificado certificadoPfx(String caminhoCertificado, String senha) throws CertificadoException {
//        Certificado certificado = new Certificado();
//
//        try {
//            certificado.setArquivo(caminhoCertificado);
//            certificado.setSenha(senha);
//            certificado.setTipo("arquivo");
//            KeyStore keyStore = getKeyStore(certificado);
//            Enumeration<String> aliasEnum = keyStore.aliases();
//            String aliasKey = (String)aliasEnum.nextElement();
//            certificado.setNome(aliasKey);
//            certificado.setVencimento(DataValidade(certificado).toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
//            certificado.setDiasRestantes(diasRestantes(certificado));
//            certificado.setValido(valido(certificado));
//            return certificado;
//        } catch (KeyStoreException var6) {
//            throw new CertificadoException("Erro ao carregar informações do certificado:" + var6.getMessage());
//        }
//    }
//
//    public static Certificado certificadoA3(String marca, String dll, String senha) throws CertificadoException {
//        Certificado certificado = new Certificado();
//
//        try {
//            certificado.setMarcaA3(marca);
//            certificado.setSenha(senha);
//            certificado.setDllA3(dll);
//            certificado.setTipo("a3");
//            Enumeration<String> aliasEnum = getKeyStore(certificado).aliases();
//            certificado.setNome((String)aliasEnum.nextElement());
//            certificado.setVencimento(DataValidade(certificado).toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
//            certificado.setDiasRestantes(diasRestantes(certificado));
//            certificado.setValido(valido(certificado));
//            return certificado;
//        } catch (KeyStoreException var5) {
//            throw new CertificadoException("Erro ao carregar informações do certificado:" + var5.getMessage());
//        }
//    }
//
//    public static Certificado certificadoA3(String marca, String dll, String senha, String alias) throws CertificadoException {
//        Certificado certificado = new Certificado();
//        certificado.setMarcaA3(marca);
//        certificado.setSenha(senha);
//        certificado.setDllA3(dll);
//        certificado.setTipo("a3");
//        certificado.setNome(alias);
//        certificado.setVencimento(DataValidade(certificado).toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
//        certificado.setDiasRestantes(diasRestantes(certificado));
//        certificado.setValido(valido(certificado));
//        return certificado;
//    }
//
//    public static Certificado certificadoA3(String marca, String dll, String senha, String alias, String serialToken) throws CertificadoException {
//        Certificado certificado = new Certificado();
//        certificado.setMarcaA3(marca);
//        certificado.setSenha(senha);
//        certificado.setDllA3(dll);
//        certificado.setTipo("a3");
//        certificado.setSerialToken(serialToken);
//        certificado.setNome(alias);
//        certificado.setVencimento(DataValidade(certificado).toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
//        certificado.setDiasRestantes(diasRestantes(certificado));
//        certificado.setValido(valido(certificado));
//        return certificado;
//    }
//
//    public static List<Certificado> listaCertificadosWindows() throws CertificadoException {
//        List<Certificado> listaCert = new ArrayList(20);
//        Certificado certificado = new Certificado();
//        certificado.setTipo("windows");
//
//        try {
//            KeyStore ks = getKeyStore(certificado);
//            Enumeration aliasEnum = ks.aliases();
//
//            while(aliasEnum.hasMoreElements()) {
//                String aliasKey = (String)aliasEnum.nextElement();
//                if (aliasKey != null) {
//                    Certificado cert = new Certificado();
//                    cert.setNome(aliasKey);
//                    cert.setTipo("windows");
//                    cert.setSenha("");
//                    Date dataValidade = DataValidade(cert);
//                    if (dataValidade == null) {
//                        cert.setNome("(INVALIDO)" + aliasKey);
//                        cert.setVencimento(LocalDate.of(2000, 1, 1));
//                        cert.setDiasRestantes(0L);
//                        cert.setValido(false);
//                    } else {
//                        cert.setVencimento(dataValidade.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
//                        cert.setDiasRestantes(diasRestantes(cert));
//                        cert.setValido(valido(cert));
//                    }
//
//                    listaCert.add(cert);
//                }
//            }
//
//            return listaCert;
//        } catch (KeyStoreException var7) {
//            throw new CertificadoException("Erro ao Carregar Certificados:" + var7.getMessage());
//        }
//    }
//
//    public static List<String> listaAliasCertificadosA3(String marca, String dll, String senha) throws CertificadoException {
//        try {
//            List<String> listaCert = new ArrayList(20);
//            Certificado certificado = new Certificado();
//            certificado.setTipo("a3");
//            certificado.setMarcaA3(marca);
//            certificado.setSenha(senha);
//            certificado.setDllA3(dll);
//            certificado.setTipo("a3");
//            Enumeration aliasEnum = getKeyStore(certificado).aliases();
//
//            while(aliasEnum.hasMoreElements()) {
//                String aliasKey = (String)aliasEnum.nextElement();
//                if (aliasKey != null) {
//                    listaCert.add(aliasKey);
//                }
//            }
//
//            return listaCert;
//        } catch (KeyStoreException var7) {
//            throw new CertificadoException("Erro ao Carregar Certificados A3:" + var7.getMessage());
//        }
//    }
//
//    private static Date DataValidade(Certificado certificado) throws CertificadoException {
//        KeyStore keyStore = getKeyStore(certificado);
//        if (keyStore == null) {
//            throw new CertificadoException("Erro Ao pegar Keytore, verifique o Certificado");
//        } else {
//            X509Certificate certificate = getCertificate(certificado, keyStore);
//            return certificate.getNotAfter();
//        }
//    }
//
//    private static Long diasRestantes(Certificado certificado) throws CertificadoException {
//        Date data = DataValidade(certificado);
//        if (data == null) {
//            return null;
//        } else {
//            long differenceMilliSeconds = data.getTime() - (new Date()).getTime();
//            return differenceMilliSeconds / 1000L / 60L / 60L / 24L;
//        }
//    }
//
//    private static boolean valido(Certificado certificado) throws CertificadoException {
//        return DataValidade(certificado) != null && DataValidade(certificado).after(new Date());
//    }
//
//    public static KeyStore getKeyStore(Certificado certificado) throws CertificadoException {
//        try {
//            String var2 = certificado.getTipo();
//            byte var3 = -1;
//            switch(var2.hashCode()) {
//            case -734784851:
//                if (var2.equals("arquivo")) {
//                    var3 = 1;
//                }
//                break;
//            case 3058:
//                if (var2.equals("a3")) {
//                    var3 = 3;
//                }
//                break;
//            case 1349493379:
//                if (var2.equals("windows")) {
//                    var3 = 0;
//                }
//                break;
//            case 1957609561:
//                if (var2.equals("arquivo_bytes")) {
//                    var3 = 2;
//                }
//            }
//
//            KeyStore keyStore;
//            switch(var3) {
//            case 0:
//                keyStore = KeyStore.getInstance("Windows-MY", "SunMSCAPI");
//                keyStore.load((InputStream)null, (char[])null);
//                return keyStore;
//            case 1:
//                File file = new File(certificado.getArquivo());
//                if (!file.exists()) {
//                    throw new CertificadoException("Certificado Digital não Encontrado");
//                }
//
//                keyStore = KeyStore.getInstance("PKCS12");
//                keyStore.load(new ByteArrayInputStream(getBytesFromInputStream(new FileInputStream(file))), certificado.getSenha().toCharArray());
//                return keyStore;
//            case 2:
//                keyStore = KeyStore.getInstance("PKCS12");
//                keyStore.load(new ByteArrayInputStream(certificado.getArquivoBytes()), certificado.getSenha().toCharArray());
//                return keyStore;
//            case 3:
//                System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
//                String slot = null;
//                if (certificado.getSerialToken() != null) {
//                    slot = getSlot(certificado.getDllA3(), certificado.getSerialToken());
//                }
//
//                InputStream conf = configA3(certificado.getMarcaA3(), certificado.getDllA3(), slot);
//                Provider p = new SunPKCS11(conf);
//                Security.addProvider(p);
//                keyStore = KeyStore.getInstance("PKCS11");
//                if (keyStore.getProvider() == null) {
//                    keyStore = KeyStore.getInstance("PKCS11", p);
//                }
//
//                keyStore.load((InputStream)null, certificado.getSenha().toCharArray());
//                return keyStore;
//            default:
//                return null;
//            }
//        } catch (CertificateException | IOException | KeyStoreException | NoSuchProviderException | NoSuchAlgorithmException var8) {
//            throw new CertificadoException("Erro Ao pegar KeyStore: " + var8.getMessage());
//        }
//    }
//
//    public static X509Certificate getCertificate(Certificado certificado, KeyStore keystore) throws CertificadoException {
//        try {
//            return (X509Certificate)keystore.getCertificate(certificado.getNome());
//        } catch (KeyStoreException var3) {
//            throw new CertificadoException("Erro Ao pegar X509Certificate: " + var3.getMessage());
//        }
//    }
//
//    private static byte[] getBytesFromInputStream(InputStream is) throws IOException {
//        ByteArrayOutputStream os = new ByteArrayOutputStream();
//        Throwable var2 = null;
//
//        try {
//            byte[] buffer = new byte['\uffff'];
//
//            int len;
//            while((len = is.read(buffer)) != -1) {
//                os.write(buffer, 0, len);
//            }
//
//            os.flush();
//            byte[] var15 = os.toByteArray();
//            return var15;
//        } catch (Throwable var13) {
//            var2 = var13;
//            throw var13;
//        } finally {
//            if (os != null) {
//                if (var2 != null) {
//                    try {
//                        os.close();
//                    } catch (Throwable var12) {
//                        var2.addSuppressed(var12);
//                    }
//                } else {
//                    os.close();
//                }
//            }
//
//        }
//    }
//
//    private static InputStream configA3(String marca, String dll, String slot) throws UnsupportedEncodingException {
//        String slotInfo = "";
//        if (slot != null) {
//            slotInfo = "\n\rslot = " + slot;
//        }
//
//        String conf = "name = " + marca + "\n\rlibrary = " + dll + slotInfo + "\n\rshowInfo = true";
//        return new ByteArrayInputStream(conf.getBytes("UTF-8"));
//    }
//
//    private static String getSlot(String libraryPath, String serialNumber) throws IOException, CertificadoException {
//        CK_C_INITIALIZE_ARGS initArgs = new CK_C_INITIALIZE_ARGS();
//        String functionList = "C_GetFunctionList";
//        initArgs.flags = 0L;
//        String slotSelected = null;
//
//        PKCS11 tmpPKCS11;
//        try {
//            try {
//                tmpPKCS11 = PKCS11.getInstance(libraryPath, functionList, initArgs, false);
//            } catch (IOException var15) {
//                var15.printStackTrace();
//                throw var15;
//            }
//        } catch (PKCS11Exception var16) {
//            try {
//                tmpPKCS11 = PKCS11.getInstance(libraryPath, functionList, (CK_C_INITIALIZE_ARGS)null, true);
//            } catch (Exception var14) {
//                throw new CertificadoException("Erro ao pegar Slot A3: " + var16.getMessage());
//            }
//        }
//
//        try {
//            long[] slotList = tmpPKCS11.C_GetSlotList(true);
//            long[] var7 = slotList;
//            int var8 = slotList.length;
//
//            for(int var9 = 0; var9 < var8; ++var9) {
//                long slot = var7[var9];
//                CK_TOKEN_INFO tokenInfo = tmpPKCS11.C_GetTokenInfo(slot);
//                if (serialNumber.equals(String.valueOf(tokenInfo.serialNumber))) {
//                    slotSelected = String.valueOf(slot);
//                }
//            }
//
//            return slotSelected;
//        } catch (Exception var13) {
//            throw new CertificadoException("Erro Ao pegar SlotA3: " + var13.getMessage());
//        }
//    }
//
//    public static Certificado getCertificadoByCnpj(String cnpj) throws CertificadoException {
//        return getCertificadoByDados(cnpj, CNPJ);
//    }
//
//    public static Certificado getCertificadoByCpf(String cnpj) throws CertificadoException {
//        return getCertificadoByDados(cnpj, CPF);
//    }
//
//    private static Certificado getCertificadoByDados(String dados, DERObjectIdentifier tipo) throws CertificadoException {
//        try {
//            Iterator var2 = listaCertificadosWindows().iterator();
//
//            while(var2.hasNext()) {
//                Certificado cert = (Certificado)var2.next();
//
//                try {
//                    KeyStore keyStore = getKeyStore(cert);
//                    X509Certificate certificate = getCertificate(cert, keyStore);
//                    Collection<?> alternativeNames = X509ExtensionUtil.getSubjectAlternativeNames(certificate);
//                    Iterator var7 = alternativeNames.iterator();
//
//                    while(var7.hasNext()) {
//                        Object alternativeName = var7.next();
//                        if (alternativeName instanceof ArrayList) {
//                            ArrayList<?> listOfValues = (ArrayList)alternativeName;
//                            Object value = listOfValues.get(1);
//                            if (value instanceof DERSequence) {
//                                DERSequence derSequence = (DERSequence)value;
//                                DERObjectIdentifier derObjectIdentifier = (DERObjectIdentifier)derSequence.getObjectAt(0);
//                                DERTaggedObject derTaggedObject = (DERTaggedObject)derSequence.getObjectAt(1);
//                                ASN1Object derObject = derTaggedObject.getObject();
//                                String valueOfTag = "";
//                                if (derObject instanceof DEROctetString) {
//                                    DEROctetString octet = (DEROctetString)derObject;
//                                    valueOfTag = new String(octet.getOctets());
//                                } else if (derObject instanceof DERPrintableString) {
//                                    DERPrintableString octet = (DERPrintableString)derObject;
//                                    valueOfTag = new String(octet.getOctets());
//                                } else if (derObject instanceof DERUTF8String) {
//                                    DERUTF8String str = (DERUTF8String)derObject;
//                                    valueOfTag = str.getString();
//                                }
//
//                                if (valueOfTag != null && !"".equals(valueOfTag) && derObjectIdentifier.equals(tipo)) {
//                                    if (tipo.equals(CPF)) {
//                                        if (valueOfTag.length() > 25 && valueOfTag.substring(8, 19).equals(dados)) {
//                                            return cert;
//                                        }
//                                    } else if (valueOfTag.equals(dados)) {
//                                        return cert;
//                                    }
//                                }
//                            }
//                        }
//                    }
//                } catch (Exception var17) {
//                    throw new RuntimeException("Erro ao pegar Certificado Pelo Cnpj" + var17.getMessage());
//                }
//            }
//
//            return null;
//        } catch (Exception var18) {
//            throw new CertificadoException("Erro ao pegar Certificado Pelo Cnpj" + var18.getMessage());
//        }
//    }
//}
