package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.GetdocConstants;
import net.wasys.getdoc.domain.entity.ProcessoLogAnexo;
import net.wasys.getdoc.domain.vo.EmailVO;
import net.wasys.getdoc.rest.request.vo.RequestHorarioExpedientePermitido;
import net.wasys.getdoc.rest.request.vo.SuperVo;
import net.wasys.util.DummyUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@ApiModel(value = "HorarioExpedientePermitidoResponse")
public class HorarioExpedientePermitidoResponse extends SuperVo {


    @ApiModelProperty(value = "chegada Manha.")
    private String chegadaManha;

    @ApiModelProperty(value = "saida Manha.")
    private String saidaManha;

    @ApiModelProperty(value = "chegada Tarde.")
    private String chegadaTarde;

    @ApiModelProperty(value = "saida Tarde.")
    private String saidaTarde;

    @ApiModelProperty(value = "horario Abertura.")
    private String horarioAbertura;

    @ApiModelProperty(value = "horario Fechamento.")
    private String horarioFechamento;

    @ApiModelProperty(value = "permitir Acesso Final Semana Feriado.")
    private boolean permitirAcessoFinalSemanaFeriado;



    public HorarioExpedientePermitidoResponse(){}

    public HorarioExpedientePermitidoResponse(RequestHorarioExpedientePermitido request) {
        this.chegadaManha = request.getChegadaManha();
        this.saidaManha = request.getSaidaManha();
        this.chegadaTarde = request.getChegadaTarde();
        this.saidaTarde = request.getSaidaTarde();
        this.horarioAbertura = request.getHorarioAbertura();
        this.horarioFechamento = request.getHorarioFechamento();
        this.permitirAcessoFinalSemanaFeriado = request.isPermitirAcessoFinalSemanaFeriado();
    }

    public String getChegadaManha() {
        return chegadaManha;
    }

    public void setChegadaManha(String chegadaManha) {
        this.chegadaManha = chegadaManha;
    }

    public String getSaidaManha() {
        return saidaManha;
    }

    public void setSaidaManha(String saidaManha) {
        this.saidaManha = saidaManha;
    }

    public String getChegadaTarde() {
        return chegadaTarde;
    }

    public void setChegadaTarde(String chegadaTarde) {
        this.chegadaTarde = chegadaTarde;
    }

    public String getSaidaTarde() {
        return saidaTarde;
    }

    public void setSaidaTarde(String saidaTarde) {
        this.saidaTarde = saidaTarde;
    }

    public String getHorarioAbertura() {
        return horarioAbertura;
    }

    public void setHorarioAbertura(String horarioAbertura) {
        this.horarioAbertura = horarioAbertura;
    }

    public String getHorarioFechamento() {
        return horarioFechamento;
    }

    public void setHorarioFechamento(String horarioFechamento) {
        this.horarioFechamento = horarioFechamento;
    }

    public boolean isPermitirAcessoFinalSemanaFeriado() {
        return permitirAcessoFinalSemanaFeriado;
    }

    public void setPermitirAcessoFinalSemanaFeriado(boolean permitirAcessoFinalSemanaFeriado) {
        this.permitirAcessoFinalSemanaFeriado = permitirAcessoFinalSemanaFeriado;
    }
}