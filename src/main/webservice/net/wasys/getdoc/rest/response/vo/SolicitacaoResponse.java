package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.GetdocConstants;
import net.wasys.getdoc.domain.entity.ProcessoLog;
import net.wasys.getdoc.domain.entity.ProcessoLogAnexo;
import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.getdoc.domain.enumeration.StatusSolicitacao;
import net.wasys.getdoc.domain.service.MessageService;
import net.wasys.getdoc.domain.vo.SolicitacaoVO;
import net.wasys.getdoc.rest.request.vo.SuperVo;
import net.wasys.util.DummyUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ApiModel(value = "SolicitacaoResponse")
public class SolicitacaoResponse extends SuperVo {

    @ApiModelProperty(notes = "ID da solicitação.")
    private Long id;

    @ApiModelProperty(notes = "Nome da área.")
    private String areaNome;

    @ApiModelProperty(notes = "Nome do usuário.")
    private String usuarioNome;

    @ApiModelProperty(notes = "Nome da sub área.")
    private String subAreaNome;

    @ApiModelProperty(notes = "ID.")
    private String para;

    @ApiModelProperty(notes = "Conteúdo HTML da solicitação.")
    private String observacaoHtml;

    @ApiModelProperty(notes = "Descrição referente ao tempo.")
    private String descricao;

    @ApiModelProperty(notes = "Status.")
    private StatusSolicitacao status;

    @ApiModelProperty(notes = "Logs/Histórico.")
    private List<ProcessoLogResponse> logs;

    @ApiModelProperty(notes = "Acão.")
    private AcaoProcesso acao;

    @ApiModelProperty(value = "Anexos.")
    private List<AnexoSolicitacaoResponse> anexos;

    public SolicitacaoResponse() {
    }

    public SolicitacaoResponse(SolicitacaoVO sVo) {
        this.id = sVo.getSolicitacao().getId();
        this.areaNome = sVo.getSolicitacao().getSubarea().getArea().getDescricao();
        this.subAreaNome = sVo.getSolicitacao().getSubarea().getDescricao();
        this.usuarioNome = sVo.getLogCriacao().getUsuario().getNome();

        StringBuffer sbPara = new StringBuffer("Solicitação para ");
        sbPara.append(sVo.getSolicitacao().getSubarea().getArea().getDescricao()).append(" / ").append(sVo.getSolicitacao().getSubarea().getDescricao());
        sbPara.append(". ").append(DummyUtils.formatDateTime(sVo.getLogCriacao().getData())).append(".");
        this.para = sbPara.toString();

        this.observacaoHtml = DummyUtils.stringToHTML(sVo.getLogCriacao().getObservacao());
        this.acao = sVo.getAcao();
        this.status = sVo.getSolicitacao().getStatus();
        this.acao = sVo.getAcao();

        Date dataResposta = sVo.getSolicitacao().getDataResposta();
        if(dataResposta == null){
            StringBuffer sb = new StringBuffer("Atender até ");
            sb.append(DummyUtils.formatDateTime(sVo.getSolicitacao().getPrazoLimite()));
            sb.append("Tempo restante: ").append(sVo.getHorasRestantes()).append("h.");
            this.descricao = sb.toString();

        }else{

            switch (this.status) {
                case RESPONDIDA: {
                    StringBuffer sb = new StringBuffer("Solicitação respondida em ");
                    sb.append(DummyUtils.formatDateTime(sVo.getSolicitacao().getDataResposta()));
                    this.descricao = sb.toString();
                    break;
                }

                case RECUSADA: {
                    StringBuffer sb = new StringBuffer("Solicitação recusada pela área em ");
                    sb.append(DummyUtils.formatDateTime(sVo.getSolicitacao().getDataResposta()));
                    this.descricao = sb.toString();
                    break;
                }
            }
        }

        if(sVo.getLogs() != null && sVo.getLogs().size() > 0){
            this.logs = new ArrayList<>();

            for(ProcessoLog pLog : sVo.getLogs()){
                this.logs.add(new ProcessoLogResponse(pLog));
            }
        }

        if (sVo.getLogCriacao().getAnexos()!= null && sVo.getLogCriacao().getAnexos().size() > 0) {
            this.anexos = new ArrayList<>();

            for (ProcessoLogAnexo anexo : sVo.getLogCriacao().getAnexos()) {
                this.anexos.add(new AnexoSolicitacaoResponse(anexo));
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPara() {
        return para;
    }

    public void setPara(String para) {
        this.para = para;
    }

    public String getObservacaoHtml() {
        return observacaoHtml;
    }

    public void setObservacaoHtml(String observacaoHtml) {
        this.observacaoHtml = observacaoHtml;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public StatusSolicitacao getStatus() {
        return status;
    }

    public void setStatus(StatusSolicitacao status) {
        this.status = status;
    }

    public List<ProcessoLogResponse> getLogs() {
        return logs;
    }

    public void setLogs(List<ProcessoLogResponse> logs) {
        this.logs = logs;
    }

    public AcaoProcesso getAcao() {
        return acao;
    }

    public void setAcao(AcaoProcesso acao) {
        this.acao = acao;
    }

    public String getAreaNome() {
        return areaNome;
    }

    public void setAreaNome(String areaNome) {
        this.areaNome = areaNome;
    }

    public String getSubAreaNome() {
        return subAreaNome;
    }

    public void setSubAreaNome(String subAreaNome) {
        this.subAreaNome = subAreaNome;
    }

    public String getUsuarioNome() {
        return usuarioNome;
    }

    public void setUsuarioNome(String usuarioNome) {
        this.usuarioNome = usuarioNome;
    }

    public List<AnexoSolicitacaoResponse> getAnexos() {
        return anexos;
    }

    public void setAnexos(List<AnexoSolicitacaoResponse> anexos) {
        this.anexos = anexos;
    }
}