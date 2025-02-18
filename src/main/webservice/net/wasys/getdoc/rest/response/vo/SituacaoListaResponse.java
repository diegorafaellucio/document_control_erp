package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.ProximaSituacao;
import net.wasys.getdoc.domain.entity.Situacao;
import net.wasys.getdoc.rest.request.vo.SuperVo;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ApiModel(value = "SituacaoListaResponse")
public class SituacaoListaResponse extends SuperVo {

    @ApiModelProperty(value = "ID")
    private Long id;

    @ApiModelProperty(value = "Nome")
    private String nome;

    @ApiModelProperty(value = "Próximas situações")
    private List<String> proximasSituacoes;

    @ApiModelProperty(value = "Prazo")
    private String prazo;

    @ApiModelProperty(value = "Notificar cliente")
    private boolean notificarCliente;

    @ApiModelProperty(value = "Ativa")
    private boolean ativa;

    public SituacaoListaResponse() {
    }

    public SituacaoListaResponse(Situacao situacao) {
        if (situacao != null) {
            this.id = situacao.getId();
            this.nome = situacao.getNome();

            Set<ProximaSituacao> proximas = situacao.getProximas();
            if (proximas != null && CollectionUtils.isNotEmpty(proximas)) {
                this.proximasSituacoes = new ArrayList<>();

                for (ProximaSituacao pS : proximas) {
                    this.proximasSituacoes.add(pS.getProxima().getNome());
                }
            }
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

    public List<String> getProximasSituacoes() {
        return proximasSituacoes;
    }

    public void setProximasSituacoes(List<String> proximasSituacoes) {
        this.proximasSituacoes = proximasSituacoes;
    }

    public String getPrazo() {
        return prazo;
    }

    public void setPrazo(String prazo) {
        this.prazo = prazo;
    }

    public boolean isNotificarCliente() {
        return notificarCliente;
    }

    public void setNotificarCliente(boolean notificarCliente) {
        this.notificarCliente = notificarCliente;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public void setAtiva(boolean ativa) {
        this.ativa = ativa;
    }
}