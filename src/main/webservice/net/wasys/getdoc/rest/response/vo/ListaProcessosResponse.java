package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.rest.request.vo.SuperVo;

import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "ListaProcessosResponse")
public class ListaProcessosResponse extends SuperVo {

    @ApiModelProperty(notes = "Quantidade de processos.")
    private int count;

    @ApiModelProperty(notes = "Lista de processos.")
    private List<ProcessoResponse> processos;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<ProcessoResponse> getProcessos() {
        return processos;
    }

    public void setProcessos(List<ProcessoResponse> processos) {
        this.processos = processos;
    }

    public void add(ProcessoResponse processoResponse){
        if(this.processos == null){
            this.processos = new ArrayList<>();
        }
        this.processos.add(processoResponse);
    }
}