package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.Situacao;
import net.wasys.getdoc.domain.entity.TipoProcessoPermissao;
import net.wasys.getdoc.domain.enumeration.PermissaoTP;
import net.wasys.getdoc.rest.annotations.NotNull;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@ApiModel(value = "RequestCadastroTipoProcesso")
public class RequestCadastroTipoProcesso extends SuperVo {

    @ApiModelProperty(notes = "Nome")
    private String nome;

    @ApiModelProperty(notes = "Horas")
    private BigDecimal horasPrazo;

    @ApiModelProperty(notes = "Ativo")
    private boolean ativo;

    @ApiModelProperty(notes = "Dica/Descrição")
    private String dica;

    @ApiModelProperty(notes = "Preenchimento com OCR")
    private boolean preencherViaOcr;

    @ApiModelProperty(notes = "ID da situação inicial")
    private Long situacaoInicialId;

    @ApiModelProperty(notes = "Permissões.")
    private Set<PermissaoTP> permissoes;

    @NotNull
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @NotNull
    public BigDecimal getHorasPrazo() {
        return horasPrazo;
    }

    public void setHorasPrazo(BigDecimal horasPrazo) {
        this.horasPrazo = horasPrazo;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public String getDica() {
        return dica;
    }

    public void setDica(String dica) {
        this.dica = dica;
    }

    public boolean isPreencherViaOcr() {
        return preencherViaOcr;
    }

    public void setPreencherViaOcr(boolean preencherViaOcr) {
        this.preencherViaOcr = preencherViaOcr;
    }

    public Long getSituacaoInicialId() {
        return situacaoInicialId;
    }

    public void setSituacaoInicialId(Long situacaoInicialId) {
        this.situacaoInicialId = situacaoInicialId;
    }

    @NotNull
    public Set<PermissaoTP> getPermissoes() {
        return permissoes;
    }

    public void setPermissoes(Set<PermissaoTP> permissoes) {
        this.permissoes = permissoes;
    }
}
