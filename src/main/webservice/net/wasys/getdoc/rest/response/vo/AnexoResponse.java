package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.enumeration.StatusDocumento;
import net.wasys.getdoc.domain.enumeration.StatusFacial;
import net.wasys.getdoc.domain.enumeration.StatusOcr;
import net.wasys.getdoc.domain.service.MessageService;
import net.wasys.getdoc.domain.vo.DocumentoVO;
import net.wasys.getdoc.domain.vo.LogVO;
import net.wasys.getdoc.rest.request.vo.SuperVo;
import net.wasys.util.DummyUtils;

import java.util.Date;

@ApiModel(value = "AnexoResponse")
public class AnexoResponse extends SuperVo {

    @ApiModelProperty(value = "ID.")
    private Long id;

    @ApiModelProperty(value = "Data da digitalização.")
    private Date data;

    @ApiModelProperty(value = "Usuário.")
    private String usuario;

    @ApiModelProperty(value = "Ação.")
    private String acao;

    @ApiModelProperty(value = "Ação contendo a informação de qual área e subárea estão envolvidas.")
    private String acaoInfoArea;

    @ApiModelProperty(value = "Nome do arquivo.")
    private String arquivoNome;

    @ApiModelProperty(value = "Tamanho em disco do arquivo.")
    private String arquivoTamanho;

    @ApiModelProperty(value = "Observações.")
    private String observacao;


    public AnexoResponse(){}


    public AnexoResponse(MessageService messageService, LogVO logVo) {
        this.id = logVo.getProcessoLogAnexo().getId();
        this.data = logVo.getData();
        this.usuario = logVo.getUsuario().getNome();
        this.acao = messageService.getValue(logVo.getAcaoKey());

        StringBuffer sb = new StringBuffer();
        sb.append(messageService.getValue(logVo.getAcaoKey()));

        if(logVo.getDocumento() != null){
            sb.append(" - ").append(logVo.getDocumento().getNome());
        }

        if(logVo.getSolicitacao() != null){
            sb.append(" - ").append(logVo.getSolicitacao().getSubarea().getArea().getDescricao());
            sb.append(" / ").append(logVo.getSolicitacao().getSubarea().getDescricao());
        }
        this.acaoInfoArea = sb.toString();

        this.arquivoNome = logVo.getProcessoLogAnexo().getNome();
        this.arquivoTamanho = DummyUtils.toFileSize(logVo.getProcessoLogAnexo().getTamanho());
        this.observacao = logVo.getObservacaoFull();
    }

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

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getAcaoInfoArea() {
        return acaoInfoArea;
    }

    public void setAcaoInfoArea(String acaoInfoArea) {
        this.acaoInfoArea = acaoInfoArea;
    }

    public String getAcao() {
        return acao;
    }

    public void setAcao(String acao) {
        this.acao = acao;
    }

    public String getArquivoNome() {
        return arquivoNome;
    }

    public void setArquivoNome(String arquivoNome) {
        this.arquivoNome = arquivoNome;
    }

    public String getArquivoTamanho() {
        return arquivoTamanho;
    }

    public void setArquivoTamanho(String arquivoTamanho) {
        this.arquivoTamanho = arquivoTamanho;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}