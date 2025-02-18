package net.wasys.getdoc.domain.vo;

public class RegistrosDeDigitalizacaoVO {

    public static final String DATA_GIGITALIZACAO = "dataDigitalizacao";
    public static final String ORIGEM = "origem";
    public static final String STATUS = "status";
    public static final String MOTIVO_REJEITO = "motivoRejeito";
    public static final String MOTIVO_REJEITO_OBS = "motivoRejeitoObs";
    public static final String PASTA_AMARELA = "pastaAmarela";
    public static final String MODELO_DOCUMENTO = "modeloDocumento";

    public String dataDigitalizacao;
    public String origem;
    public String status;
    public String motivoRejeito;
    public String motivoRejeitoObs;
    public String pastaAmarela;
    public String modeloDocumento;

    public String getDataDigitalizacao() {
        return dataDigitalizacao;
    }

    public void setDataDigitalizacao(String dataDigitalizacao) {
        this.dataDigitalizacao = dataDigitalizacao;
    }

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMotivoRejeito() {
        return motivoRejeito;
    }

    public void setMotivoRejeito(String motivoRejeito) {
        this.motivoRejeito = motivoRejeito;
    }

    public String getMotivoRejeitoObs() {
        return motivoRejeitoObs;
    }

    public void setMotivoRejeitoObs(String motivoRejeitoObs) {
        this.motivoRejeitoObs = motivoRejeitoObs;
    }

    public String getPastaAmarela() {
        return pastaAmarela;
    }

    public void setPastaAmarela(String pastaAmarela) {
        this.pastaAmarela = pastaAmarela;
    }

    public String getModeloDocumento() {
        return modeloDocumento;
    }

    public void setModeloDocumento(String modeloDocumento) {
        this.modeloDocumento = modeloDocumento;
    }
}
