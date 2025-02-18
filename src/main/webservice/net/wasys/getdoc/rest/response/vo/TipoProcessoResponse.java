package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.vo.TipoProcessoVO;
import net.wasys.getdoc.rest.request.vo.SuperVo;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@ApiModel(value = "TipoProcessoResponse")
public class TipoProcessoResponse extends SuperVo {

    @ApiModelProperty(value = "ID")
    private Long id;

    @ApiModelProperty(value = "Dica")
    private String dica;

    @ApiModelProperty(value = "Nome")
    private String nome;

    @ApiModelProperty(value = "Indicador se permite OCR")
    private Boolean viaOcr;

    @ApiModelProperty(value = "Ativo")
    private boolean ativo;

    @ApiModelProperty(value = "Quantidade de campanhas.")
    private Long qtdeCampanhas;

    @ApiModelProperty(value = "Quantidade de campos.")
    private Long qtdeCampos;

    @ApiModelProperty(value = "Quantidade de documentos.")
    private Long qtdeDocumentos;

    @ApiModelProperty(value = "Prazo (Horas).")
    private BigDecimal prazoHoras;

    @ApiModelProperty(value = "Situação inicial ID.")
    private Long situacaoInicialId;

    @ApiModelProperty(value = "Permissões.")
    private List<PermissaoTipoProcessoResponse> permissoes;

    public TipoProcessoResponse() {
    }

    public TipoProcessoResponse(TipoProcesso tipoProcesso) {
        this.id = tipoProcesso.getId();
        this.nome = tipoProcesso.getNome();
        this.dica = tipoProcesso.getDica();
        this.viaOcr = tipoProcesso.isPreencherViaOcr();
        this.ativo = tipoProcesso.getAtivo();
        this.prazoHoras = tipoProcesso.getHorasPrazo();
        if(tipoProcesso.getSituacaoInicial()!= null) {
            this.situacaoInicialId = tipoProcesso.getSituacaoInicial().getId();
        }
        if(CollectionUtils.isNotEmpty(tipoProcesso.getPermissoes())) {
            this.permissoes = new ArrayList<>();
            tipoProcesso.getPermissoes().forEach(tipoProcessoPermissao -> {
                PermissaoTipoProcessoResponse permissaoTipoProcessoResponse = new PermissaoTipoProcessoResponse();
                permissaoTipoProcessoResponse.setPermissao(tipoProcessoPermissao.getPermissao());
                this.permissoes.add(permissaoTipoProcessoResponse);
            });
        }
    }

    public TipoProcessoResponse(TipoProcessoVO tipoProcessoVo) {
        this.id = tipoProcessoVo.getTipoProcesso().getId();
        this.nome = tipoProcessoVo.getTipoProcesso().getNome();
        this.ativo = tipoProcessoVo.getTipoProcesso().getAtivo();
        this.dica = tipoProcessoVo.getTipoProcesso().getDica();
        this.viaOcr = tipoProcessoVo.getTipoProcesso().isPreencherViaOcr();
        this.qtdeCampanhas = tipoProcessoVo.getCountCampanhas();
        this.qtdeCampos = tipoProcessoVo.getCountCampos();
        this.qtdeDocumentos = tipoProcessoVo.getCountDocumentos();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDica() {
        return dica;
    }

    public void setDica(String dica) {
        this.dica = dica;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Boolean getViaOcr() {
        return viaOcr;
    }

    public void setViaOcr(Boolean viaOcr) {
        this.viaOcr = viaOcr;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public Long getQtdeCampanhas() {
        return qtdeCampanhas;
    }

    public void setQtdeCampanhas(Long qtdeCampanhas) {
        this.qtdeCampanhas = qtdeCampanhas;
    }

    public Long getQtdeCampos() {
        return qtdeCampos;
    }

    public void setQtdeCampos(Long qtdeCampos) {
        this.qtdeCampos = qtdeCampos;
    }

    public Long getQtdeDocumentos() {
        return qtdeDocumentos;
    }

    public void setQtdeDocumentos(Long qtdeDocumentos) {
        this.qtdeDocumentos = qtdeDocumentos;
    }

    public BigDecimal getPrazoHoras() {
        return prazoHoras;
    }

    public void setPrazoHoras(BigDecimal prazoHoras) {
        this.prazoHoras = prazoHoras;
    }

    public Long getSituacaoInicialId() {
        return situacaoInicialId;
    }

    public void setSituacaoInicialId(Long situacaoInicialId) {
        this.situacaoInicialId = situacaoInicialId;
    }

    public List<PermissaoTipoProcessoResponse> getPermissoes() {
        return permissoes;
    }

    public void setPermissoes(List<PermissaoTipoProcessoResponse> permissoes) {
        this.permissoes = permissoes;
    }
}
