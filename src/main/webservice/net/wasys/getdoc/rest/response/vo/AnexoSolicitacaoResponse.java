package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.ProcessoLogAnexo;
import net.wasys.getdoc.rest.request.vo.SuperVo;
import net.wasys.util.DummyUtils;

@ApiModel(value = "AnexoSolicitacaoResponse")
public class AnexoSolicitacaoResponse extends SuperVo {


    @ApiModelProperty(value = "ID.")
    private Long id;

    @ApiModelProperty(notes = "Nome.")
    private String nome;

    @ApiModelProperty(notes = "Tamanho em disco.")
    private String tamanho;

    public AnexoSolicitacaoResponse() {
    }

    public AnexoSolicitacaoResponse(ProcessoLogAnexo anexo) {
        this.id = anexo.getId();
        this.nome = anexo.getNome();
        this.tamanho = DummyUtils.toFileSize(anexo.getTamanho());
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

    public String getTamanho() {
        return tamanho;
    }

    public void setTamanho(String tamanho) {
        this.tamanho = tamanho;
    }

}