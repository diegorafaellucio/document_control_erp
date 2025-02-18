package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.enumeration.StatusDocumento;

public class ManutencaoDocumentoVO {

    private StatusDocumento status;
    private String observacao;
    private boolean excluir = true;

    public StatusDocumento getStatus() {
        return status;
    }

    public void setStatus(StatusDocumento status) {
        this.status = status;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public boolean isExcluir() {
        return excluir;
    }

    public void setExcluir(boolean excluir) {
        this.excluir = excluir;
    }
}
