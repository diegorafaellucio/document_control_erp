package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.RelatorioGeral;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.vo.RelatorioAcompanhamentoVO;
import net.wasys.getdoc.rest.request.vo.SuperVo;

import java.util.Date;

@ApiModel(value = "RelatorioGeralResponse")
public class RelatorioGeralResponse extends SuperVo {

    @ApiModelProperty(value = "ID da requisiçãp.")
    private Long requisicaoId;

    @ApiModelProperty(value = "Data da criação.")
    private Date dataCriacao;

    @ApiModelProperty(value = "Data da finalização.")
    private Date dataFinalizacao;

    @ApiModelProperty(value = "Status.")
    private StatusProcesso statusProcesso;

    @ApiModelProperty(value = "Tipo do processo.")
    private String tipoProcesso;

    public RelatorioGeralResponse(){}

    public RelatorioGeralResponse(RelatorioGeral relatorioGeral) {
        this.requisicaoId = relatorioGeral.getProcessoId();
        this.dataCriacao = relatorioGeral.getDataCriacao();
        this.dataFinalizacao = relatorioGeral.getDataFinalizacao();
        this.statusProcesso = relatorioGeral.getStatus();
        this.tipoProcesso = relatorioGeral.getTipoProcesso().getNome();
    }

    public Long getRequisicaoId() {
        return requisicaoId;
    }

    public void setRequisicaoId(Long requisicaoId) {
        this.requisicaoId = requisicaoId;
    }

    public Date getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(Date dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public Date getDataFinalizacao() {
        return dataFinalizacao;
    }

    public void setDataFinalizacao(Date dataFinalizacao) {
        this.dataFinalizacao = dataFinalizacao;
    }

    public StatusProcesso getStatusProcesso() {
        return statusProcesso;
    }

    public void setStatusProcesso(StatusProcesso statusProcesso) {
        this.statusProcesso = statusProcesso;
    }

    public String getTipoProcesso() {
        return tipoProcesso;
    }

    public void setTipoProcesso(String tipoProcesso) {
        this.tipoProcesso = tipoProcesso;
    }
}