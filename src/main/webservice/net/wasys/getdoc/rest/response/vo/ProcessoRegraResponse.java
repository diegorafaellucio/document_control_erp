package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.ProcessoRegra;
import net.wasys.getdoc.domain.enumeration.FarolRegra;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.enumeration.StatusProcessoRegra;
import net.wasys.getdoc.domain.vo.ProcessoVO;
import net.wasys.getdoc.rest.request.vo.SuperVo;

import java.util.Date;

@ApiModel(value = "ProcessoRegraResponse")
public class ProcessoRegraResponse extends SuperVo {

    @ApiModelProperty(notes = "ID.")
    private Long id;
   /* @ApiModelProperty(notes = "data da Regra.")
    private Date dataRegra;
    @ApiModelProperty(notes = "data da Execucao.")
    private Date dataExecucao;*/
    @ApiModelProperty(notes = "farol.")
    private FarolRegra farol;
    @ApiModelProperty(notes = "status.")
    private StatusProcessoRegra status;
   /* @ApiModelProperty(notes = "tempo.")
    private Long tempo;*/
    @ApiModelProperty(notes = "mensagem.")
    private String mensagem;
    @ApiModelProperty(notes = "subRegraFinalId.")
    private Long subRegraFinalId;
    @ApiModelProperty(notes = "regra.")
    private RegraResponse regra;
    @ApiModelProperty(notes = "Id do processo.")
    private Long processoId;
   /* @ApiModelProperty(notes = "situacao do Destino.")
    private Situacao situacaoDestino;*/
   /* @ApiModelProperty(notes = "ProcessoRegraLog.")
    private List<ProcessoRegraLog> processoRegraLogs = new ArrayList<>(0);*/


    public ProcessoRegraResponse() {
    }

    public ProcessoRegraResponse(ProcessoRegra processoRegra) {
        this.id = processoRegra.getId();
        this.farol = processoRegra.getFarol();
        this.status = processoRegra.getStatus();
        this.mensagem = processoRegra.getMensagem();
        this.subRegraFinalId = processoRegra.getSubRegraFinalId();
        this.regra = new RegraResponse(processoRegra.getRegra());
        this.processoId = processoRegra.getProcesso().getId();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FarolRegra getFarol() {
        return farol;
    }

    public void setFarol(FarolRegra farol) {
        this.farol = farol;
    }

    public StatusProcessoRegra getStatus() {
        return status;
    }

    public void setStatus(StatusProcessoRegra status) {
        this.status = status;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public Long getSubRegraFinalId() {
        return subRegraFinalId;
    }

    public void setSubRegraFinalId(Long subRegraFinalId) {
        this.subRegraFinalId = subRegraFinalId;
    }

    public RegraResponse getRegra() {
        return regra;
    }

    public void setRegra(RegraResponse regra) {
        this.regra = regra;
    }

    public Long getProcessoId() {
        return processoId;
    }

    public void setProcessoId(Long processoId) {
        this.processoId = processoId;
    }
}