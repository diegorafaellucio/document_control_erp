package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.Documento;
import net.wasys.getdoc.domain.entity.TipoDocumento;
import net.wasys.getdoc.domain.enumeration.StatusDocumento;
import net.wasys.getdoc.domain.enumeration.StatusFacial;
import net.wasys.getdoc.domain.enumeration.StatusOcr;
import net.wasys.getdoc.domain.vo.DocumentoVO;
import net.wasys.getdoc.rest.request.vo.SuperVo;

import java.util.Date;

@ApiModel(value = "DocumentoResponse")
public class DocumentoResponse extends SuperVo {

    @ApiModelProperty(value = "ID.")
    private Long id;

    @ApiModelProperty(value = "Nome.")
    private String nome;

    @ApiModelProperty(value = "Status do documento.")
    private StatusDocumento status;

    @ApiModelProperty(value = "Aprovavel.")
    private boolean aprovavel;

    @ApiModelProperty(value = "Rejeitavel.")
    private boolean rejeitavel;

    @ApiModelProperty(value = "Possui comentário.")
    private boolean temComentario;

    @ApiModelProperty(value = "Permite copiar.")
    private boolean podeCopiar;

    @ApiModelProperty(value = "Permite mover.")
    private boolean podeMover;

    @ApiModelProperty(value = "Permite excluir.")
    private boolean podeExcluir;

    @ApiModelProperty(value = "Digitalizável.")
    private boolean digitalizavel;

    @ApiModelProperty(value = "Justificável.")
    private boolean justificavel;

    @ApiModelProperty(value = "Quantidade de inconsistências.")
    private int countInconsistencias;

    @ApiModelProperty(value = "Data da digitalização.")
    private Date dataDigitalizacao;

    @ApiModelProperty(value = "Nome da irregularidade.")
    private String irregularidadeNome;

    @ApiModelProperty(value = "Observação sobre a pendência.")
    private String pendenciaObservacao;

    @ApiModelProperty(value = "Justificativa sobre a pendência.")
    private String pendenciaJustificativa;

    @ApiModelProperty(value = "Obrigatório.")
    private boolean obrigatorio;

    @ApiModelProperty(value = "Tipo do documento.")
    private TipoDocumentoResponse tipoDocumento;

    @ApiModelProperty(value = "Versão atual.")
    private Integer versaoAtual;

    @ApiModelProperty(value = "Status do OCR.")
    private StatusOcr statusOcr;

    @ApiModelProperty(value = "Status do reconhecimento facial.")
    private StatusFacial statusFacial;

    @ApiModelProperty(value = "Face reconhecida.")
    private boolean faceReconhecida;

    @ApiModelProperty(value = "Quantidade de imagens vinculados para esse documento.")
    private int qtdeImagens;

    public DocumentoResponse(){}

    public DocumentoResponse(DocumentoVO doc) {
        this.id = doc.getId();
        this.nome = doc.getNome();
        this.status = doc.getStatus();
        this.aprovavel = doc.getAprovavel();
        this.rejeitavel = doc.getRejeitavel();
        this.temComentario = doc.getTemComentario();
        this.podeCopiar = doc.getPodeCopiar();
        this.podeMover = doc.getPodeMover();
        this.podeExcluir = doc.getPodeExcluir();
        this.digitalizavel = doc.getDigitalizavel();
        this.justificavel = doc.getJustificavel();
        this.countInconsistencias = doc.getCountInconsistencias();
        this.dataDigitalizacao = doc.getDataDigitalizacao();
        this.irregularidadeNome = doc.getIrregularidadeNome();
        this.pendenciaObservacao = doc.getPendenciaObservacao();
        this.pendenciaJustificativa = doc.getPendenciaJustificativa();
        this.obrigatorio = doc.getObrigatorio();
        if(doc.getTipoDocumento() != null) {
            this.tipoDocumento = new TipoDocumentoResponse(doc.getTipoDocumento());
        }
        this.versaoAtual = doc.getVersaoAtual();
        this.statusOcr = doc.getStatusOcr();
        this.statusFacial = doc.getStatusFacial();
        this.faceReconhecida = doc.getFaceReconhecida();
        this.qtdeImagens = doc.getCountImagens();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public StatusDocumento getStatus() {
        return status;
    }

    public void setStatus(StatusDocumento status) {
        this.status = status;
    }

    public boolean isAprovavel() {
        return aprovavel;
    }

    public void setAprovavel(boolean aprovavel) {
        this.aprovavel = aprovavel;
    }

    public boolean isRejeitavel() {
        return rejeitavel;
    }

    public void setRejeitavel(boolean rejeitavel) {
        this.rejeitavel = rejeitavel;
    }

    public boolean isTemComentario() {
        return temComentario;
    }

    public void setTemComentario(boolean temComentario) {
        this.temComentario = temComentario;
    }

    public boolean isPodeCopiar() {
        return podeCopiar;
    }

    public void setPodeCopiar(boolean podeCopiar) {
        this.podeCopiar = podeCopiar;
    }

    public boolean isPodeMover() {
        return podeMover;
    }

    public void setPodeMover(boolean podeMover) {
        this.podeMover = podeMover;
    }

    public boolean isPodeExcluir() {
        return podeExcluir;
    }

    public void setPodeExcluir(boolean podeExcluir) {
        this.podeExcluir = podeExcluir;
    }

    public boolean isDigitalizavel() {
        return digitalizavel;
    }

    public void setDigitalizavel(boolean digitalizavel) {
        this.digitalizavel = digitalizavel;
    }

    public boolean isJustificavel() {
        return justificavel;
    }

    public void setJustificavel(boolean justificavel) {
        this.justificavel = justificavel;
    }

    public int getCountInconsistencias() {
        return countInconsistencias;
    }

    public void setCountInconsistencias(int countInconsistencias) {
        this.countInconsistencias = countInconsistencias;
    }

    public Date getDataDigitalizacao() {
        return dataDigitalizacao;
    }

    public void setDataDigitalizacao(Date dataDigitalizacao) {
        this.dataDigitalizacao = dataDigitalizacao;
    }

    public String getIrregularidadeNome() {
        return irregularidadeNome;
    }

    public void setIrregularidadeNome(String irregularidadeNome) {
        this.irregularidadeNome = irregularidadeNome;
    }

    public String getPendenciaObservacao() {
        return pendenciaObservacao;
    }

    public void setPendenciaObservacao(String pendenciaObservacao) {
        this.pendenciaObservacao = pendenciaObservacao;
    }

    public String getPendenciaJustificativa() {
        return pendenciaJustificativa;
    }

    public void setPendenciaJustificativa(String pendenciaJustificativa) {
        this.pendenciaJustificativa = pendenciaJustificativa;
    }

    public boolean isObrigatorio() {
        return obrigatorio;
    }

    public void setObrigatorio(boolean obrigatorio) {
        this.obrigatorio = obrigatorio;
    }

    public TipoDocumentoResponse getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(TipoDocumentoResponse tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public Integer getVersaoAtual() {
        return versaoAtual;
    }

    public void setVersaoAtual(Integer versaoAtual) {
        this.versaoAtual = versaoAtual;
    }

    public StatusOcr getStatusOcr() {
        return statusOcr;
    }

    public void setStatusOcr(StatusOcr statusOcr) {
        this.statusOcr = statusOcr;
    }

    public StatusFacial getStatusFacial() {
        return statusFacial;
    }

    public void setStatusFacial(StatusFacial statusFacial) {
        this.statusFacial = statusFacial;
    }

    public boolean isFaceReconhecida() {
        return faceReconhecida;
    }

    public void setFaceReconhecida(boolean faceReconhecida) {
        this.faceReconhecida = faceReconhecida;
    }

    public int getQtdeImagens() {
        return qtdeImagens;
    }

    public void setQtdeImagens(int qtdeImagens) {
        this.qtdeImagens = qtdeImagens;
    }
}