package net.wasys.getdoc.rest.response.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.GetdocConstants;
import net.wasys.getdoc.domain.entity.ProcessoLog;
import net.wasys.getdoc.domain.entity.ProcessoLogAnexo;
import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.service.MessageService;
import net.wasys.getdoc.rest.request.vo.SuperVo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ApiModel(value = "ProcessoLogResponse")
public class ProcessoLogResponse extends SuperVo {

    @ApiModelProperty(notes = "RoleGD.")
    private  RoleGD roleGd;

    @ApiModelProperty(notes = "ID.")
    private Long id;

    @ApiModelProperty(notes = "Descrição.")
    private String descricao;

    @ApiModelProperty(notes = "Data.")
    private Date data;

    @ApiModelProperty(notes = "Ação processo.")
    private AcaoProcesso acao;

    @ApiModelProperty(notes = "Observação.")
    private String observacao;

    @ApiModelProperty(notes = "Status processo.")
    private StatusProcesso statusProcesso;

    @ApiModelProperty(notes = "Lido.")
    private Boolean lido;

    @ApiModelProperty(notes = "SLA em horas da situação.")
    private BigDecimal horasPrazoSituacao;

    @ApiModelProperty(notes = "Prazo limite da situação.")
    private Date prazoLimiteSituacao;

    @ApiModelProperty(notes = "Anexos.")
    private List<AnexoSolicitacaoResponse> anexos;

    public ProcessoLogResponse() {
    }

    public ProcessoLogResponse(ProcessoLog pLog) {
        this.id = pLog.getId();
        this.roleGd = pLog.getUsuario().getRoleGD();
        this.acao = pLog.getAcao();
        this.data = pLog.getData();

        this.observacao = pLog.getObservacao();
        this.statusProcesso = pLog.getStatusProcesso();
        this.lido = pLog.getLido();

        if (pLog.getAnexos() != null && pLog.getAnexos().size() > 0) {
            this.anexos = new ArrayList<>();

            for (ProcessoLogAnexo anexo : pLog.getAnexos()) {
                this.anexos.add(new AnexoSolicitacaoResponse(anexo));
            }
        }
    }

    /*
    public ProcessoLogResponse(MessageService messageService, ProcessoLog pLog) {
        this.id = pLog.getId();
        this.data = pLog.getData();
        this.acao = pLog.getAcao();
        this.observacao = pLog.getObservacao();
        this.statusProcesso = pLog.getStatusProcesso();
        this.lido = pLog.getLido();
        this.horasPrazoSituacao = pLog.getHorasPrazoSituacao();
        this.prazoLimiteSituacao = pLog.getPrazoLimiteSituacao();

        if (pLog.getAnexos() != null && pLog.getAnexos().size() > 0) {
            this.anexos = new ArrayList<>();

            for (ProcessoLogAnexo anexo : pLog.getAnexos()) {
                this.anexos.add(new AnexoSolicitacaoResponse(anexo));
            }
        }
    }
    */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public AcaoProcesso getAcao() {
        return acao;
    }

    public void setAcao(AcaoProcesso acao) {
        this.acao = acao;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public StatusProcesso getStatusProcesso() {
        return statusProcesso;
    }

    public void setStatusProcesso(StatusProcesso statusProcesso) {
        this.statusProcesso = statusProcesso;
    }

    public Boolean getLido() {
        return lido;
    }

    public void setLido(Boolean lido) {
        this.lido = lido;
    }

    public BigDecimal getHorasPrazoSituacao() {
        return horasPrazoSituacao;
    }

    public void setHorasPrazoSituacao(BigDecimal horasPrazoSituacao) {
        this.horasPrazoSituacao = horasPrazoSituacao;
    }

    public Date getPrazoLimiteSituacao() {
        return prazoLimiteSituacao;
    }

    public void setPrazoLimiteSituacao(Date prazoLimiteSituacao) {
        this.prazoLimiteSituacao = prazoLimiteSituacao;
    }

    public List<AnexoSolicitacaoResponse> getAnexos() {
        return anexos;
    }

    public void setAnexos(List<AnexoSolicitacaoResponse> anexos) {
        this.anexos = anexos;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public RoleGD getRoleGd() {
        return roleGd;
    }

    public void setRoleGd(RoleGD roleGd) {
        this.roleGd = roleGd;
    }
}