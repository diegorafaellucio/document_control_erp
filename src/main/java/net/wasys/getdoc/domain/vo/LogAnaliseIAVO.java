package net.wasys.getdoc.domain.vo;

import java.util.Date;

public class LogAnaliseIAVO {

    private Long processoId;
    private String processoNome;
    private Long documentoId;
    private String documentoNome;
    private Date data;
    private String statusDocumento;
    private String motivoDocumento;
    private String statusProcesso;
    private String motivoProcesso;
    private boolean tipificou;
    private boolean ocr;
    private String ocrNome;
    private String scoreNome;
    private String ocrCpf;
    private String scoreCpf;
    private String modeloDocumento;
    private String labelTipificacao;
    private String scoreTipificacao;
    private String dataDigitalizacao;
    private String metadados;

    public Long getProcessoId() {
        return processoId;
    }

    public void setProcessoId(Long processoId) {
        this.processoId = processoId;
    }

    public String getProcessoNome() {
        return processoNome;
    }

    public void setProcessoNome(String processoNome) {
        this.processoNome = processoNome;
    }

    public Long getDocumentoId() {
        return documentoId;
    }

    public void setDocumentoId(Long documentoId) {
        this.documentoId = documentoId;
    }

    public String getDocumentoNome() {
        return documentoNome;
    }

    public void setDocumentoNome(String documentoNome) {
        this.documentoNome = documentoNome;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public String getStatusDocumento() {
        return statusDocumento;
    }

    public void setStatusDocumento(String statusDocumento) {
        this.statusDocumento = statusDocumento;
    }

    public String getMotivoDocumento() {
        return motivoDocumento;
    }

    public void setMotivoDocumento(String motivoDocumento) {
        this.motivoDocumento = motivoDocumento;
    }

    public String getStatusProcesso() {
        return statusProcesso;
    }

    public void setStatusProcesso(String statusProcesso) {
        this.statusProcesso = statusProcesso;
    }

    public String getMotivoProcesso() {
        return motivoProcesso;
    }

    public void setMotivoProcesso(String motivoProcesso) {
        this.motivoProcesso = motivoProcesso;
    }

    public boolean isTipificou() {
        return tipificou;
    }

    public void setTipificou(boolean tipificou) {
        this.tipificou = tipificou;
    }

    public boolean isOcr() {
        return ocr;
    }

    public void setOcr(boolean ocr) {
        this.ocr = ocr;
    }

    public String getOcrNome() {
        return ocrNome;
    }

    public void setOcrNome(String ocrNome) {
        this.ocrNome = ocrNome;
    }

    public String getScoreNome() {
        return scoreNome;
    }

    public void setScoreNome(String scoreNome) {
        this.scoreNome = scoreNome;
    }

    public String getOcrCpf() {
        return ocrCpf;
    }

    public void setOcrCpf(String ocrCpf) {
        this.ocrCpf = ocrCpf;
    }

    public String getScoreCpf() {
        return scoreCpf;
    }

    public void setScoreCpf(String scoreCpf) {
        this.scoreCpf = scoreCpf;
    }

    public String getModeloDocumento() {
        return modeloDocumento;
    }

    public void setModeloDocumento(String modeloDocumento) {
        this.modeloDocumento = modeloDocumento;
    }

    public String getLabelTipificacao() {
        return labelTipificacao;
    }

    public void setLabelTipificacao(String labelTipificacao) {
        this.labelTipificacao = labelTipificacao;
    }

    public String getScoreTipificacao() {
        return scoreTipificacao;
    }

    public void setScoreTipificacao(String scoreTipificacao) {
        this.scoreTipificacao = scoreTipificacao;
    }

    public String getMetadados() {
        return metadados;
    }

    public void setMetadados(String metadados) {
        this.metadados = metadados;
    }

    public String getDataDigitalizacao() {
        return dataDigitalizacao;
    }

    public void setDataDigitalizacao(String dataDigitalizacao) {
        this.dataDigitalizacao = dataDigitalizacao;
    }
}
