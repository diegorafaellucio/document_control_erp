package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.rest.annotations.NotNull;

import java.util.List;

@ApiModel(value = "UploadArquivo")
public class UploadArquivo extends SuperVo {

    @ApiModelProperty(notes = "Nome do arquivo.")
    private String nome;

    @ApiModelProperty(notes = "Local do arquivo no tmp.")
    private String path;

    public UploadArquivo() {
    }

    public UploadArquivo(String nome, String path) {
        this.nome = nome;
        this.path = path;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
