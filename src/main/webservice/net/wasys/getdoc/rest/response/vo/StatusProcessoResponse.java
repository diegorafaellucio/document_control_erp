package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.rest.request.vo.SuperVo;

import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "StatusProcessoResponse")
public class StatusProcessoResponse extends SuperVo {

    @ApiModelProperty(value = "ID")
    private StatusProcesso id;

    @ApiModelProperty(value = "Descrição")
    private String descricao;

    @ApiModelProperty(value = "Indica se esse atributo deve ser exibido como selecionado na tela.")
    private boolean selecionado;

    public StatusProcesso getId() {
        return id;
    }

    public void setId(StatusProcesso id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public boolean isSelecionado() {
        return selecionado;
    }

    public void setSelecionado(boolean selecionado) {
        this.selecionado = selecionado;
    }

    public static List<StatusProcessoResponse> get(){
        List<StatusProcessoResponse> list = new ArrayList<>();
        for (StatusProcesso statusProcesso : StatusProcesso.values()) {

            StatusProcessoResponse statusProcessoResponse = new StatusProcessoResponse();
            statusProcessoResponse.setId(statusProcesso);

            switch (statusProcesso) {
                case RASCUNHO:
                    statusProcessoResponse.setDescricao("Rascunho");
                    break;
                case PENDENTE:
                    statusProcessoResponse.setDescricao("Pendente");
                    break;
                case CONCLUIDO:
                    statusProcessoResponse.setDescricao("Concluído");
                    break;
                case CANCELADO:
                    statusProcessoResponse.setDescricao("Cancelado");
                    break;
                case AGUARDANDO_ANALISE:
                    statusProcessoResponse.setDescricao("Aguardando análise");
                    break;
                case EM_ANALISE:
                    statusProcessoResponse.setDescricao("Em análise");
                    break;
                case EM_ACOMPANHAMENTO:
                    statusProcessoResponse.setDescricao("Em acompanhamento");
                    break;
                case ENCAMINHADO:
                    statusProcessoResponse.setDescricao("Encaminhado");
                    break;

            }
            list.add(statusProcessoResponse);
        }
        return list;
    }

    public static StatusProcessoResponse from(StatusProcesso s) {
        List<StatusProcessoResponse> statusProcessoResponses = get();
        for(StatusProcessoResponse spr : statusProcessoResponses){
            if(spr.id == s){
                return spr;
            }
        }
        return null;
    }

}