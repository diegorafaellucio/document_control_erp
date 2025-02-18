package net.wasys.getdoc.domain.vo.filtro;

import java.util.List;

import net.wasys.getdoc.domain.entity.Subperfil;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.enumeration.StatusProcessoRegra;
import net.wasys.getdoc.domain.enumeration.TipoConsultaExterna;
import net.wasys.getdoc.domain.enumeration.TipoExecucaoRegra;

public class ProcessoRegraFiltro implements Cloneable {

    private Long processoId;
    private Long regraId;
    private String regraNome;
    private Long processoIgnorarId;
    private List<StatusProcessoRegra> statusList;
    private Boolean possuiConsultaExterna;
    private TipoProcesso tipoProcesso;
    private TipoExecucaoRegra tipoExecucao;
    private Long situacaoId;
    private Boolean decisaoFluxo;
    private List<Long> regrasIds;
    private boolean reprocessaRegraEditarCampos;
    private boolean reprocessaRegraAtualizarDocumentos;
    private TipoConsultaExterna tipoConsultaExterna;
    private boolean ativo;
    private Subperfil subperfilPermitido;
    private Boolean desconsiderarProcessoComDocumentoTipificando;

    public Long getProcessoId() {
        return processoId;
    }

    public void setProcessoId(Long processoId) {
        this.processoId = processoId;
    }

    public Long getRegraId() {
        return regraId;
    }

    public void setRegraId(Long regraId) {
        this.regraId = regraId;
    }

    public String getRegraNome() {
        return regraNome;
    }

    public void setRegraNome(String regraNome) {
        this.regraNome = regraNome;
    }

    public ProcessoRegraFiltro getClone() {
        try {
            return (ProcessoRegraFiltro) super.clone();
        } catch (CloneNotSupportedException e) {
            return this;
        }
    }

    public Long getProcessoIgnorarId() {
        return processoIgnorarId;
    }

    public void setProcessoIgnorarId(Long processoIgnorarId) {
        this.processoIgnorarId = processoIgnorarId;
    }

    public List<StatusProcessoRegra> getStatusList() {
        return statusList;
    }

    public void setStatusList(List<StatusProcessoRegra> statusList) {
        this.statusList = statusList;
    }

    public Boolean getPossuiConsultaExterna() {
        return possuiConsultaExterna;
    }

    public void setPossuiConsultaExterna(Boolean possuiConsultaExterna) {
        this.possuiConsultaExterna = possuiConsultaExterna;
    }

    public TipoProcesso getTipoProcesso() {
        return tipoProcesso;
    }

    public void setTipoProcesso(TipoProcesso tipoProcesso) {
        this.tipoProcesso = tipoProcesso;
    }

    public TipoExecucaoRegra getTipoExecucao() {
        return tipoExecucao;
    }

    public void setTipoExecucao(TipoExecucaoRegra tipoExecucao) {
        this.tipoExecucao = tipoExecucao;
    }

    public Long getSituacaoId() {
        return situacaoId;
    }

    public void setSituacaoId(Long situacaoId) {
        this.situacaoId = situacaoId;
    }

    public Boolean getDecisaoFluxo() {
        return decisaoFluxo;
    }

    public void setDecisaoFluxo(Boolean decisaoFluxo) {
        this.decisaoFluxo = decisaoFluxo;
    }

    public List<Long> getRegrasIds() {
        return regrasIds;
    }

    public void setRegrasIds(List<Long> regrasIds) {
        this.regrasIds = regrasIds;
    }

    public boolean isReprocessaRegraEditarCampos() {
        return reprocessaRegraEditarCampos;
    }

    public void setReprocessaRegraEditarCampos(boolean reprocessaRegraEditarCampos) {
        this.reprocessaRegraEditarCampos = reprocessaRegraEditarCampos;
    }

    public boolean isReprocessaRegraAtualizarDocumentos() {
        return reprocessaRegraAtualizarDocumentos;
    }

    public void setReprocessaRegraAtualizarDocumentos(boolean reprocessaRegraAtualizarDocumentos) {
        this.reprocessaRegraAtualizarDocumentos = reprocessaRegraAtualizarDocumentos;
    }

    public TipoConsultaExterna getTipoConsultaExterna() {
        return tipoConsultaExterna;
    }

    public void setTipoConsultaExterna(TipoConsultaExterna tipoConsultaExterna) {
        this.tipoConsultaExterna = tipoConsultaExterna;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public Subperfil getSubperfilPermitido() {
        return subperfilPermitido;
    }

    public void setSubperfilPermitido(Subperfil subperfilPermitido) {
        this.subperfilPermitido = subperfilPermitido;
    }

    public Boolean getDesconsiderarProcessoComDocumentoTipificando() {
        return desconsiderarProcessoComDocumentoTipificando;
    }

    public void setDesconsiderarProcessoComDocumentoTipificando(Boolean desconsiderarProcessoComDocumentoTipificando) {
        this.desconsiderarProcessoComDocumentoTipificando = desconsiderarProcessoComDocumentoTipificando;
    }
}
