package net.wasys.getdoc.domain.vo;

import java.util.Date;

public class ModeloDocumentoVO {

    private Long id;
    private String descricao;
    private String palavrasEsperadas;
    private String palavrasExcludentes;
    private boolean ativo;
    private Date dataAlteracao;
    private Integer percentualMininoTipificacao;
    private boolean darknetApiHabilitada;
    private boolean visionApiHabilitada;
    private String labelDarknet;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getPalavrasEsperadas() {
        return palavrasEsperadas;
    }

    public void setPalavrasEsperadas(String palavrasEsperadas) {
        this.palavrasEsperadas = palavrasEsperadas;
    }

    public String getPalavrasExcludentes() {
        return palavrasExcludentes;
    }

    public void setPalavrasExcludentes(String palavrasExcludentes) {
        this.palavrasExcludentes = palavrasExcludentes;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public Date getDataAlteracao() {
        return dataAlteracao;
    }

    public void setDataAlteracao(Date dataAlteracao) {
        this.dataAlteracao = dataAlteracao;
    }

    public Integer getPercentualMininoTipificacao() {
        return percentualMininoTipificacao;
    }

    public void setPercentualMininoTipificacao(Integer percentualMininoTipificacao) {
        this.percentualMininoTipificacao = percentualMininoTipificacao;
    }

    public boolean isDarknetApiHabilitada() {
        return darknetApiHabilitada;
    }

    public void setDarknetApiHabilitada(boolean darknetApiHabilitada) {
        this.darknetApiHabilitada = darknetApiHabilitada;
    }

    public boolean isVisionApiHabilitada() {
        return visionApiHabilitada;
    }

    public void setVisionApiHabilitada(boolean visionApiHabilitada) {
        this.visionApiHabilitada = visionApiHabilitada;
    }

    public String getLabelDarknet() {
        return labelDarknet;
    }

    public void setLabelDarknet(String labelDarknet) {
        this.labelDarknet = labelDarknet;
    }
}
