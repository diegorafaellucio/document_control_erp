package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.vo.TipoProcessoVO;
import net.wasys.getdoc.rest.request.vo.SuperVo;

import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "ListaTipoProcessoResponse")
public class ListaTipoProcessoResponse extends SuperVo {

    @ApiModelProperty(value = "Quantidade de tipos processo salvos.")
    private int count;

    @ApiModelProperty(value = "Lista de tipo processo.")
    private List<TipoProcessoResponse> lista;

    public ListaTipoProcessoResponse() {
    }

    public ListaTipoProcessoResponse(int count, List<TipoProcessoResponse> lista) {
        this.count = count;
        this.lista = lista;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<TipoProcessoResponse> getLista() {
        return lista;
    }

    public void setLista(List<TipoProcessoResponse> lista) {
        this.lista = lista;
    }

    public void add(TipoProcessoResponse tipoProcessoResponse) {
        if(this.lista == null){
            this.lista = new ArrayList<>();
        }
        this.lista.add(tipoProcessoResponse);
    }
}