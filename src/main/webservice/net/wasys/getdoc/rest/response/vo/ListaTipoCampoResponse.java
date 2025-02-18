package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.rest.request.vo.SuperVo;

import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "ListaTipoCampoResponse")
public class ListaTipoCampoResponse extends SuperVo {


    @ApiModelProperty(value = "Nome do processo.")
    private String processoNome;

    @ApiModelProperty(value = "Lista de grupos de campos.")
    private List<GrupoCamposResponse> gruposCampo;

    public List<GrupoCamposResponse> getGruposCampo() {
        return gruposCampo;
    }

    public void setGruposCampo(List<GrupoCamposResponse> gruposCampo) {
        this.gruposCampo = gruposCampo;
    }

    public void add(GrupoCamposResponse tipoDocumentoResponse) {
        if(this.gruposCampo == null){
            this.gruposCampo = new ArrayList<>();
        }
        this.gruposCampo.add(tipoDocumentoResponse);
    }

    public String getProcessoNome() {
        return processoNome;
    }

    public void setProcessoNome(String processoNome) {
        this.processoNome = processoNome;
    }
}