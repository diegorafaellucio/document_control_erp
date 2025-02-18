package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.enumeration.StatusDocumento;
import net.wasys.getdoc.rest.request.vo.SuperVo;

import java.util.Map;

@ApiModel(value = "DocumentoUploadResponse")
public class DocumentoUploadResponse extends SuperVo {

    @ApiModelProperty(value = "ID.")
    private Long id;

    @ApiModelProperty(value = "Nome.")
    private String nome;

    @ApiModelProperty(value = "Status de cada uma das imagens que foram subidas.")
    private Map<String, String> mapImagens;

    @ApiModelProperty(value = "Status do documento.")
    private StatusDocumento status;

    @ApiModelProperty(value = "Irregularidade.")
    private String irregularidade;

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

    public StatusDocumento getStatus() {
        return status;
    }

    public void setStatus(StatusDocumento status) {
        this.status = status;
    }

    public String getIrregularidade() {
        return irregularidade;
    }

    public void setIrregularidade(String irregularidade) {
        this.irregularidade = irregularidade;
    }

    public Map<String, String> getMapImagens() {
        return mapImagens;
    }

    public void setMapImagens(Map<String, String> mapImagens) {
        this.mapImagens = mapImagens;
    }
}