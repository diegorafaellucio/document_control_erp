//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package br.com.samuelweb.certificado;

import java.time.LocalDate;

public class Certificado {
    public static final String WINDOWS = "windows";
    public static final String ARQUIVO = "arquivo";
    public static final String ARQUIVO_BYTES = "arquivo_bytes";
    public static final String TSLv1_2 = "TLSv1.2";
    public static final String A3 = "a3";
    private String nome;
    private LocalDate vencimento;
    private Long diasRestantes;
    private String arquivo;
    private byte[] arquivoBytes;
    private String senha;
    private String tipo;
    private String dllA3;
    private String marcaA3;
    private String serialToken;
    private boolean valido;
    private boolean ativarProperties = false;
    private String sslProtocol = "TLSv1.2";

    public Certificado() {
    }

    public String getSerialToken() {
        return this.serialToken;
    }

    public void setSerialToken(String serialToken) {
        this.serialToken = serialToken;
    }

    public boolean isAtivarProperties() {
        return this.ativarProperties;
    }

    public void setAtivarProperties(boolean ativarProperties) {
        this.ativarProperties = ativarProperties;
    }

    public String getSslProtocol() {
        return this.sslProtocol;
    }

    public void setSslProtocol(String sslProtocol) {
        this.sslProtocol = sslProtocol;
    }

    public String getNome() {
        return this.nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDate getVencimento() {
        return this.vencimento;
    }

    public void setVencimento(LocalDate vencimento) {
        this.vencimento = vencimento;
    }

    public Long getDiasRestantes() {
        return this.diasRestantes;
    }

    public void setDiasRestantes(Long diasRestantes) {
        this.diasRestantes = diasRestantes;
    }

    public boolean isValido() {
        return this.valido;
    }

    public void setValido(boolean valido) {
        this.valido = valido;
    }

    public String getArquivo() {
        return this.arquivo;
    }

    public void setArquivo(String arquivo) {
        this.arquivo = arquivo;
    }

    public byte[] getArquivoBytes() {
        return this.arquivoBytes;
    }

    public void setArquivoBytes(byte[] arquivo_bytes) {
        this.arquivoBytes = arquivo_bytes;
    }

    public String getTipo() {
        return this.tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getSenha() {
        return this.senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getDllA3() {
        return this.dllA3;
    }

    public void setDllA3(String dllA3) {
        this.dllA3 = dllA3;
    }

    public String getMarcaA3() {
        return this.marcaA3;
    }

    public void setMarcaA3(String marcaA3) {
        this.marcaA3 = marcaA3;
    }
}
