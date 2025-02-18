package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.vo.DocumentoVO;
import net.wasys.getdoc.rest.request.vo.SuperVo;

@ApiModel(value = "DocumentoExcluidoResponse")
public class DocumentoExcluidoResponse extends SuperVo {

    @ApiModelProperty(value = "ID.")
    private Long id;

    @ApiModelProperty(value = "Nome.")
    private String nome;


    public DocumentoExcluidoResponse() {
    }

    public DocumentoExcluidoResponse(DocumentoVO doc) {
        this.id = doc.getId();
        this.nome = doc.getNome();
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
