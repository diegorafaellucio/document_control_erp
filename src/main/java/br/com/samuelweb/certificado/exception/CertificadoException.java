//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package br.com.samuelweb.certificado.exception;

public class CertificadoException extends Exception {
    private static final long serialVersionUID = -5054900660251852366L;
    String message;

    public CertificadoException(Throwable e) {
        super(e);
    }

    public CertificadoException(String message) {
        this((Throwable)null);
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
