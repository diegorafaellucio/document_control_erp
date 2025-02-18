package net.wasys.getdoc.rest.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RequestJustificarDocumento {

    @SerializedName(value = "justificativa")
    @Expose
    private String justificativa;

    public String getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }

}
