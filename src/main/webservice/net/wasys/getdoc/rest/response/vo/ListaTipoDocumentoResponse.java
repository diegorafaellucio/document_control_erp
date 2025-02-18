package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.rest.request.vo.SuperVo;

import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "ListaTipoDocumentoResponse")
public class ListaTipoDocumentoResponse extends SuperVo {

    @ApiModelProperty(value = "Quantidade de tipos processo salvos.")
    private int count;

    @ApiModelProperty(value = "Nome do processo.")
    private String processoNome;

    @ApiModelProperty(value = "Lista de tipo de documento.")
    private List <TipoDocumentoResponse> lista;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<TipoDocumentoResponse> getLista() {
        return lista;
    }

    public void setLista(List<TipoDocumentoResponse> lista) {
        this.lista = lista;
    }

    public void add(TipoDocumentoResponse tipoDocumentoResponse) {
        if(this.lista == null){
            this.lista = new ArrayList<>();
        }
        this.lista.add(tipoDocumentoResponse);
    }

    public String getProcessoNome() {
        return processoNome;
    }

    public void setProcessoNome(String processoNome) {
        this.processoNome = processoNome;
    }
}