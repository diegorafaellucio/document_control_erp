package net.wasys.getdoc.rest.request.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.rest.annotations.NotNull;

@ApiModel(value = "RequestCadastroTipoDocumento")
public class RequestCadastroTipoDocumento extends SuperVo {

    @ApiModelProperty(value = "ID do modelo de OCR.")
    private Long modeloOcrId;

    @ApiModelProperty(value = "ID do modelo de OCR.")
    private Long modeloDocumentoId;

    @ApiModelProperty(value = "Nome.")
    private String nome;

    @ApiModelProperty(value = "Obrigatoriedade.")
    private boolean obrigatorio;

    @ApiModelProperty(value = "Reconhecimento facial.")
    private boolean reconhecimentoFacial;

    @ApiModelProperty(value = "Ordem.")
    private int ordem;

    @ApiModelProperty(value = "Taxa de compressão.")
    private short taxaCompressao = 100;

    @ApiModelProperty(value = "Número máximo de páginas.")
    private Integer maximoPaginas;

    public Long getModeloOcrId() {
        return modeloOcrId;
    }

    public void setModeloOcrId(Long modeloOcrId) {
        this.modeloOcrId = modeloOcrId;
    }

    public Long getModeloDocumentoId() {
        return modeloDocumentoId;
    }

    public void setModeloDocumentoId(Long modeloDocumentoId) {
        this.modeloDocumentoId = modeloDocumentoId;
    }

    @NotNull
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public boolean isObrigatorio() {
        return obrigatorio;
    }

    public void setObrigatorio(boolean obrigatorio) {
        this.obrigatorio = obrigatorio;
    }

    public boolean isReconhecimentoFacial() {
        return reconhecimentoFacial;
    }

    public void setReconhecimentoFacial(boolean reconhecimentoFacial) {
        this.reconhecimentoFacial = reconhecimentoFacial;
    }

    @NotNull
    public int getOrdem() {
        return ordem;
    }

    public void setOrdem(int ordem) {
        this.ordem = ordem;
    }

    @NotNull
    public short getTaxaCompressao() {
        return taxaCompressao;
    }

    public void setTaxaCompressao(short taxaCompressao) {
        this.taxaCompressao = taxaCompressao;
    }

    @NotNull

    public Integer getMaximoPaginas() { return maximoPaginas; }

    public void setMaximoPaginas(Integer maximoPaginas) { this.maximoPaginas = maximoPaginas; }
}