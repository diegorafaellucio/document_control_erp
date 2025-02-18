package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.ProximaSituacao;
import net.wasys.getdoc.domain.entity.Situacao;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.enumeration.TipoPrazo;
import net.wasys.getdoc.rest.request.vo.SuperVo;
import org.apache.commons.collections.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;

@ApiModel(value = "DetalhesSituacaoResponse")
public class DetalhesSituacaoResponse extends SuperVo {

    @ApiModelProperty(value = "ID")
    private Long id;

    @ApiModelProperty(value = "Tipo processo")
    private SituacaoTipoProcessoResponse tipoProcesso;

    @ApiModelProperty(value = "Nome")
    private String nome;

    @ApiModelProperty(value = "Status inicial")
    private StatusProcesso statusInicial;

    @ApiModelProperty(value = "Prazo")
    private Integer prazo;

    @ApiModelProperty(value = "Prazo formatado")
    private String prazoDescricao;

    @ApiModelProperty(value = "Tipo do prazo")
    private TipoPrazo tipoPrazo;

    @ApiModelProperty(value = "Ativa?")
    private boolean ativa;

    @ApiModelProperty(value = "Distribuição automática")
    private boolean distribuicaoAutomatica;

    @ApiModelProperty(value = "Notificar autor cliente")
    private boolean notificarAutorClient;

    @ApiModelProperty(value = "Próximas situações possíveis selecionadas")
    private List<ProximaSituacaoResponse> proximasSelecionadas;

    @ApiModelProperty(value = "Próximas situações possíveis disponíveis")
    private List<ProximaSituacaoResponse> proximasDisponiveis;

    public DetalhesSituacaoResponse() {
    }

    public DetalhesSituacaoResponse(Situacao situacao) {
        if (situacao != null) {
            this.id = situacao.getId();
            this.nome = situacao.getNome();
            this.tipoProcesso = new SituacaoTipoProcessoResponse(situacao.getTipoProcesso());
            this.tipoPrazo = situacao.getTipoPrazo();
            this.statusInicial = situacao.getStatus();
            this.ativa = situacao.getAtiva();
            this.notificarAutorClient = situacao.isNotificarAutor();
            this.distribuicaoAutomatica = situacao.isDistribuicaoAutomatica();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<ProximaSituacaoResponse> getProximasSelecionadas() {
        return proximasSelecionadas;
    }

    public void setProximasSelecionadas(List<ProximaSituacaoResponse> proximasSelecionadas) {
        this.proximasSelecionadas = proximasSelecionadas;
    }

    public List<ProximaSituacaoResponse> getProximasDisponiveis() {
        return proximasDisponiveis;
    }

    public void setProximasDisponiveis(List<ProximaSituacaoResponse> proximasDisponiveis) {
        this.proximasDisponiveis = proximasDisponiveis;
    }

    public SituacaoTipoProcessoResponse getTipoProcesso() {
        return tipoProcesso;
    }

    public void setTipoProcesso(SituacaoTipoProcessoResponse tipoProcesso) {
        this.tipoProcesso = tipoProcesso;
    }

    public Integer getPrazo() {
        return prazo;
    }

    public void setPrazo(Integer prazo) {
        this.prazo = prazo;
    }

    public TipoPrazo getTipoPrazo() {
        return tipoPrazo;
    }

    public void setTipoPrazo(TipoPrazo tipoPrazo) {
        this.tipoPrazo = tipoPrazo;
    }

    public StatusProcesso getStatusInicial() {
        return statusInicial;
    }

    public void setStatusInicial(StatusProcesso statusInicial) {
        this.statusInicial = statusInicial;
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

    public String getPrazoDescricao() {
        return prazoDescricao;
    }

    public void setPrazoDescricao(String prazoDescricao) {
        this.prazoDescricao = prazoDescricao;
    }

    public void setSituacoesDisponiveis(List<Situacao> situacoesDisponiveis) {
        this.proximasDisponiveis = null;
        if(CollectionUtils.isNotEmpty(situacoesDisponiveis)) {
            this.proximasDisponiveis = new ArrayList<>();

            for (Situacao pS : situacoesDisponiveis) {
                this.proximasDisponiveis.add(new ProximaSituacaoResponse(pS));
            }
        }
    }

    public void setSituacoesSelecionadas(List<Situacao> proximasSelecionadas) {
        this.proximasSelecionadas = null;
        if(CollectionUtils.isNotEmpty(proximasSelecionadas)) {
            this.proximasSelecionadas = new ArrayList<>();
            for (Situacao pS : proximasSelecionadas) {
                this.proximasSelecionadas.add(new ProximaSituacaoResponse(pS));
            }
        }
    }
}
