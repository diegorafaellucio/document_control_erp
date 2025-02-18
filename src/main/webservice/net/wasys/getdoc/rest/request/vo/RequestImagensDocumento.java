package net.wasys.getdoc.rest.request.vo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;

import java.util.List;

@ApiModel(value = "RequestImagensDocumento")
public class RequestImagensDocumento {

    @SerializedName(value = "excluir")
    @Expose
    private List<Long> excluir;

    @SerializedName(value = "listHash")
    @Expose
    private List<String> listHash;

    public List<Long> getExcluir() {
        return excluir;
    }

    public void setExcluir(List<Long> excluir) {
        this.excluir = excluir;
    }

    public List<String> getListHash() {
        return listHash;
    }

    public void setListHash(List<String> listHash) {
        this.listHash = listHash;
    }
}
