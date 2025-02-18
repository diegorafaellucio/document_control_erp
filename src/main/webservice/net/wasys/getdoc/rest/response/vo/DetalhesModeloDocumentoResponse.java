package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.ModeloDocumento;
import net.wasys.getdoc.rest.request.vo.SuperVo;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

@ApiModel(value = "DetalhesModeloDocumentoResponse")
public class DetalhesModeloDocumentoResponse extends ModeloDocumentoResponse {

    @ApiModelProperty(value = "Palavras esperadas.")
    private String palavrasExperadas;

    @ApiModelProperty(value = "Palavras excludentes.")
    private String palavrasExcludentes;

    @ApiModelProperty(value = "Lista de palavras esperadas.")
    private List<String> palavrasExperadasList;

    @ApiModelProperty(value = "Lista de palavras excludentes.")
    private List<String> palavrasExcludentesList;

    public DetalhesModeloDocumentoResponse(){}


    public DetalhesModeloDocumentoResponse(ModeloDocumento modeloDocumento) {
        super(modeloDocumento);
        this.palavrasExcludentes = modeloDocumento.getPalavrasExcludentes();
        this.palavrasExperadas = modeloDocumento.getPalavrasEsperadas();
        if(StringUtils.isNotEmpty(this.palavrasExperadas)){
            this.palavrasExperadasList = Arrays.asList(this.palavrasExperadas.split(";"));
        }

        if(StringUtils.isNotEmpty(this.palavrasExcludentes)){
            this.palavrasExcludentesList= Arrays.asList(this.palavrasExcludentes.split(";"));
        }
    }

    public String getPalavrasExperadas() {
        return palavrasExperadas;
    }

    public void setPalavrasExperadas(String palavrasExperadas) {
        this.palavrasExperadas = palavrasExperadas;
    }

    public String getPalavrasExcludentes() {
        return palavrasExcludentes;
    }

    public void setPalavrasExcludentes(String palavrasExcludentes) {
        this.palavrasExcludentes = palavrasExcludentes;
    }

    public List<String> getPalavrasExperadasList() {
        return palavrasExperadasList;
    }

    public void setPalavrasExperadasList(List<String> palavrasExperadasList) {
        this.palavrasExperadasList = palavrasExperadasList;
    }

    public List<String> getPalavrasExcludentesList() {
        return palavrasExcludentesList;
    }

    public void setPalavrasExcludentesList(List<String> palavrasExcludentesList) {
        this.palavrasExcludentesList = palavrasExcludentesList;
    }
}