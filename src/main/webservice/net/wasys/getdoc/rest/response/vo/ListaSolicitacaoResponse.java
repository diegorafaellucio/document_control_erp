package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.rest.request.vo.SuperVo;

import java.util.List;

@ApiModel(value = "ListaSolicitacaoResponse")
public class ListaSolicitacaoResponse extends SuperVo {

    @ApiModelProperty(notes = "Indica se possui alguma solicitação pendente. Se for > 0, considerar o badge na aba.")
    private int badgeSolicitacaoPendente;

    @ApiModelProperty(notes = "Lista de solicitações.")
    private List<SolicitacaoResponse> solicitacoes;

    public List<SolicitacaoResponse> getSolicitacoes() {
        return solicitacoes;
    }

    public void setSolicitacoes(List<SolicitacaoResponse> solicitacoes) {
        this.solicitacoes = solicitacoes;
    }

    public int getBadgeSolicitacaoPendente() {
        return badgeSolicitacaoPendente;
    }

    public void setBadgeSolicitacaoPendente(int badgeSolicitacaoPendente) {
        this.badgeSolicitacaoPendente = badgeSolicitacaoPendente;
    }
}