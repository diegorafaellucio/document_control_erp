package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.vo.ProcessoVO;
import net.wasys.getdoc.rest.request.vo.SuperVo;

import java.util.Date;

@ApiModel(value = "ProcessoMesmoClienteResponse")
public class ProcessoMesmoClienteResponse extends SuperVo {

    @ApiModelProperty(notes = "ID do processo.")
    private Long processoId;

    @ApiModelProperty(value = "Número do processo.")
    private String processoNumero;

    @ApiModelProperty(value = "Tipo do processo.")
    private String tipoProcesso;

    @ApiModelProperty(value = "Status do processo.")
    private String status;

    @ApiModelProperty(value = "Data de envio.")
    private Date dataEnvio;

    @ApiModelProperty(value = "Analista.")
    private String analista;

    public ProcessoMesmoClienteResponse(ProcessoVO processoVo) {
        this.processoId = processoVo.getProcesso().getId();
        this.processoNumero = processoVo.getNumero();
        this.tipoProcesso = processoVo.getProcesso().getTipoProcesso().getNome();
        this.status = StatusProcessoResponse.from(processoVo.getProcesso().getStatus()).getDescricao();
        this.dataEnvio = processoVo.getProcesso().getDataEnvioAnalise();

        Usuario analista = processoVo.getProcesso().getAnalista();
        if(analista != null) {
            this.analista = analista.getNome();
        }
    }

    public Long getProcessoId() {
        return processoId;
    }

    public void setProcessoId(Long processoId) {
        this.processoId = processoId;
    }

    public String getProcessoNumero() {
        return processoNumero;
    }

    public void setProcessoNumero(String processoNumero) {
        this.processoNumero = processoNumero;
    }

    public String getTipoProcesso() {
        return tipoProcesso;
    }

    public void setTipoProcesso(String tipoProcesso) {
        this.tipoProcesso = tipoProcesso;
    }

    public Date getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(Date dataEnvio) {
        this.dataEnvio = dataEnvio;
    }

    public String getAnalista() {
        return analista;
    }

    public void setAnalista(String analista) {
        this.analista = analista;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}