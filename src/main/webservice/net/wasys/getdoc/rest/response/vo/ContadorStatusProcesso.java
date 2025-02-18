package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.rest.request.vo.SuperVo;

@ApiModel(value = "ContadorStatusProcesso")
public class ContadorStatusProcesso extends SuperVo {

    @ApiModelProperty(notes = "Status processo.")
    private StatusProcessoResponse statusProcesso;

    @ApiModelProperty(notes = "Quantidade.")
    private Long quantidade;

    public StatusProcessoResponse getStatusProcesso() {
        return statusProcesso;
    }

    public void setStatusProcesso(StatusProcessoResponse statusProcesso) {
        this.statusProcesso = statusProcesso;
    }

    public Long getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Long quantidade) {
        this.quantidade = quantidade;
    }
}