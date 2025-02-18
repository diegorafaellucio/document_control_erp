package net.wasys.getdoc.rest.request.vo;

public class RequestDadosLastDocumentoRequestVO {

    private Long tipoDocumentoId;
    private Long processoId;

    public Long getTipoDocumentoId() {
        return tipoDocumentoId;
    }

    public void setTipoDocumentoId(Long tipoDocumentoId) {
        this.tipoDocumentoId = tipoDocumentoId;
    }

    public Long getProcessoId() {
        return processoId;
    }

    public void setProcessoId(Long processoId) {
        this.processoId = processoId;
    }

    @Override
    public String toString() {
        return "RequestDadosLastDocumentoRequestVO{" +
                "tipoDocumentoId=" + tipoDocumentoId +
                ", processoId=" + processoId +
                '}';
    }
}
