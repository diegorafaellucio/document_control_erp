package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.TipoDocumento;
import net.wasys.getdoc.rest.request.vo.SuperVo;

@ApiModel(value = "TipoDocumentoResponse")
public class TipoDocumentoResponse extends SuperVo {

    @ApiModelProperty(value = "ID.")
    private Long id;

    @ApiModelProperty(value = "Nome.")
    private String nome;

    @ApiModelProperty(value = "Obrigatório.")
    private boolean obrigatorio;

    @ApiModelProperty(value = "Reconhecimento facial.")
    private boolean reconhecimentoFacial;

    @ApiModelProperty(value = "Ordem.")
    private Integer ordem;

    @ApiModelProperty(value = "Taxa de compressao.")
    private short taxaCompressao;

    @ApiModelProperty(value = "Número máximo de páginas.")
    private Integer maximoPaginas;

    public TipoDocumentoResponse() {
    }

    public TipoDocumentoResponse(TipoDocumento tipoDocumento) {
        this.id = tipoDocumento.getId();
        this.nome = tipoDocumento.getNome();
        this.obrigatorio = tipoDocumento.getObrigatorio();
        this.ordem = tipoDocumento.getOrdem();
        this.reconhecimentoFacial = tipoDocumento.getReconhecimentoFacial();
        this.taxaCompressao = tipoDocumento.getTaxaCompressao();
        this.maximoPaginas = tipoDocumento.getMaximoPaginas();
    }

    /*
    //TODO pendende de fazer o parse desses campos.
    private ModeloOcr modeloOcr;
    private ModeloDocumento modeloDocumento;
    private TipoProcesso tipoProcesso;
    */

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

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }

    public short getTaxaCompressao() {
        return taxaCompressao;
    }

    public void setTaxaCompressao(short taxaCompressao) {
        this.taxaCompressao = taxaCompressao;
    }

    public Integer getMaximoPaginas() { return maximoPaginas; }

    public void setMaximoPaginas(Integer maximoPaginas) { this.maximoPaginas = maximoPaginas; }
}