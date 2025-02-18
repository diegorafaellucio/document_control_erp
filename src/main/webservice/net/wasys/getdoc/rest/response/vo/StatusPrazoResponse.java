package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.enumeration.StatusPrazo;
import net.wasys.getdoc.rest.request.vo.SuperVo;

import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "StatusPrazoResponse")
public class StatusPrazoResponse extends SuperVo {

    @ApiModelProperty(value = "ID")
    private StatusPrazo id;

    @ApiModelProperty(value = "Descrição")
    private String descricao;

    public static List<StatusPrazoResponse> get() {
        List<StatusPrazoResponse> list = new ArrayList<>();

        for (StatusPrazo statusPrazo : StatusPrazo.values()) {

            StatusPrazoResponse statusProcessoResponse = new StatusPrazoResponse();
            statusProcessoResponse.setId(statusPrazo);

            /**
             *
             * StatusPrazo.NORMAL.label=Ok
             * StatusPrazo.ADVERTIR.label=Alertas
             * StatusPrazo.ALERTAR.label=Atrasados
             *
             */
            switch (statusPrazo) {
                case ALERTAR:

                    statusProcessoResponse.setDescricao("Atrasados");
                    break;
                case NORMAL:
                    statusProcessoResponse.setDescricao("Ok");
                    break;
                case ADVERTIR:
                    statusProcessoResponse.setDescricao("Alertas");
                    break;

            }

            list.add(statusProcessoResponse);
        }

        return list;
    }

    public StatusPrazo getId() {
        return id;
    }

    public void setId(StatusPrazo id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}