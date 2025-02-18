package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.enumeration.TipoPrazo;
import net.wasys.getdoc.rest.annotations.NotNull;

import java.math.BigDecimal;
import java.util.List;

@ApiModel(value = "RequestCadastroSituacao")
public class RequestCadastroSituacao extends SuperVo {

    @ApiModelProperty(notes = "ID do tipo de processo")
    private Long tipoProcessoId;

    @ApiModelProperty(notes = "NOme.")
    private String nome;

    @ApiModelProperty(notes = "Status inicial qunado o processo por criado.")
    private StatusProcesso statusInicial;

    @ApiModelProperty(notes = "Prazo.")
    private BigDecimal prazo;

    @ApiModelProperty(notes = "Tipo do prazo.")
    private TipoPrazo tipoPrazo;

    @ApiModelProperty(notes = "Próximas situações.")
    private List<Long> proximasSituacoes;

    @ApiModelProperty(value = "Ativa?")
    private boolean ativa;

    @ApiModelProperty(value = "Distribuição automática")
    private boolean distribuicaoAutomatica;

    @ApiModelProperty(value = "Notificar autor cliente")
    private boolean notificarAutorClient;

    @NotNull
    public Long getTipoProcessoId() {
        return tipoProcessoId;
    }

    public void setTipoProcessoId(Long tipoProcessoId) {
        this.tipoProcessoId = tipoProcessoId;
    }

    @NotNull
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public StatusProcesso getStatusInicial() {
        return statusInicial;
    }

    public void setStatusInicial(StatusProcesso statusInicial) {
        this.statusInicial = statusInicial;
    }

    public BigDecimal getPrazo() {
        return prazo;
    }

    public void setPrazo(BigDecimal prazo) {
        this.prazo = prazo;
    }

    public TipoPrazo getTipoPrazo() {
        return tipoPrazo;
    }

    public void setTipoPrazo(TipoPrazo tipoPrazo) {
        this.tipoPrazo = tipoPrazo;
    }

    public List<Long> getProximasSituacoes() {
        return proximasSituacoes;
    }

    public void setProximasSituacoes(List<Long> proximasSituacoes) {
        this.proximasSituacoes = proximasSituacoes;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public void setAtiva(boolean ativa) {
        this.ativa = ativa;
    }

    public boolean isDistribuicaoAutomatica() {
        return distribuicaoAutomatica;
    }

    public void setDistribuicaoAutomatica(boolean distribuicaoAutomatica) {
        this.distribuicaoAutomatica = distribuicaoAutomatica;
    }

    public boolean isNotificarAutorClient() {
        return notificarAutorClient;
    }

    public void setNotificarAutorClient(boolean notificarAutorClient) {
        this.notificarAutorClient = notificarAutorClient;
    }
}

