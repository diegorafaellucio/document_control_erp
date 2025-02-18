package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.ModeloOcr;
import net.wasys.getdoc.rest.request.vo.SuperVo;

@ApiModel(value = "ModeloOcrResponse")
public class ModeloOcrResponse extends SuperVo {

    @ApiModelProperty(value = "ID.")
    private Long id;

    @ApiModelProperty(value = "Nome.")
    private String nome;


    public ModeloOcrResponse(){}


    public ModeloOcrResponse(ModeloOcr modeloOcr) {
        this.id = modeloOcr.getId();
        this.nome = modeloOcr.getDescricao();
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
}