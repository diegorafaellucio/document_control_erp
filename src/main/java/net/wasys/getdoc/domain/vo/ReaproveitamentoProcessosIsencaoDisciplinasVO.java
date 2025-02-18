package net.wasys.getdoc.domain.vo;

import java.util.List;

public class ReaproveitamentoProcessosIsencaoDisciplinasVO {

    private Long processoCaptacaoId;
    private String matricula;
    private List<ReaproveitamentoCampoVO> campos;
    private Long situacaoId;

    public Long getProcessoCaptacaoId() {
        return processoCaptacaoId;
    }

    public void setProcessoCaptacaoId(Long processoCaptacaoId) {
        this.processoCaptacaoId = processoCaptacaoId;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public List<ReaproveitamentoCampoVO> getCampos() {
        return campos;
    }

    public void setCampos(List<ReaproveitamentoCampoVO> campos) {
        this.campos = campos;
    }

    public Long getSituacaoId() {
        return situacaoId;
    }

    public void setSituacaoId(Long situacaoId) {
        this.situacaoId = situacaoId;
    }

    @Override
    public String toString() {
        return "ReaproveitamentoProcessosIsencaoDisciplinasVO{" +
                "processoCaptacaoId=" + processoCaptacaoId +
                ", matricula='" + matricula + '\'' +
                ", campos=" + campos +
                '}';
    }
}
