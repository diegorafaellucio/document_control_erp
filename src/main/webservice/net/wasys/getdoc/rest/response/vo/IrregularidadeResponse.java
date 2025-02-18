package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.Irregularidade;
import net.wasys.getdoc.domain.entity.TipoEvidencia;
import net.wasys.getdoc.rest.request.vo.SuperVo;

@ApiModel(value = "IrregularidadeResponse")
public class IrregularidadeResponse extends SuperVo {

    @ApiModelProperty(notes = "ID.")
    private Long id;

    @ApiModelProperty(notes = "Nome.")
    private String nome;

    @ApiModelProperty(notes = "Ativa ou não.")
    private boolean ativa;

    public IrregularidadeResponse() {
    }

    public IrregularidadeResponse(Irregularidade irregularidade) {
        this.id = irregularidade.getId();
        this.nome = irregularidade.getNome();
        this.ativa = irregularidade.getAtiva();
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

    public boolean isAtiva() {
        return ativa;
    }

    public void setAtiva(boolean ativa) {
        this.ativa = ativa;
    }
}