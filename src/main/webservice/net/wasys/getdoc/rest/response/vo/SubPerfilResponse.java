package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.Subperfil;
import net.wasys.getdoc.rest.request.vo.SuperVo;

@ApiModel(value = "SubPerfilResponse")
public class SubPerfilResponse extends SuperVo {

    @ApiModelProperty(notes = "ID.")
    private Long id;

    @ApiModelProperty(notes = "Descrição.")
    private String descricao;

    @ApiModelProperty(notes = "Permite conclusão em massa.")
    private Boolean permiteConclusaoEmMassa;

    public SubPerfilResponse() {
    }

    public SubPerfilResponse(Subperfil subperfil) {
        this.id = subperfil.getId();
        this.descricao = subperfil.getDescricao();
        this.permiteConclusaoEmMassa = subperfil.getPermiteConclusaoEmMassa();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Boolean getPermiteConclusaoEmMassa() { return permiteConclusaoEmMassa; }

    public void setPermiteConclusaoEmMassa(Boolean permiteConclusaoEmMassa) { this.permiteConclusaoEmMassa = permiteConclusaoEmMassa; }
}