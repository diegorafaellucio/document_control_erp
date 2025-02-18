package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.rest.request.vo.SuperVo;

import java.util.List;

@ApiModel(value = "ListaSituacaoTipoProcesso")
public class ListaSituacaoTipoProcesso extends SuperVo {

    @ApiModelProperty(notes = "Tipo de processo.")
    private SubperfilTipoProcessoResponse tipoProcesso;

    @ApiModelProperty(notes = "Situações.")
    private List<SubperfilSituacaoResponse> situacoes;

}