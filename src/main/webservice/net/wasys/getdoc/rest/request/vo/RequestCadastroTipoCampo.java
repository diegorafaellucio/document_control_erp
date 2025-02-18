package net.wasys.getdoc.rest.request.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.Campo;
import net.wasys.getdoc.domain.entity.TipoCampo;
import net.wasys.getdoc.domain.enumeration.TipoEntradaCampo;
import net.wasys.getdoc.rest.annotations.NotNull;
import net.wasys.getdoc.rest.response.vo.CampoResponse;

import java.util.List;

@ApiModel(value = "RequestCadastroTipoCampo")
public class RequestCadastroTipoCampo extends CampoResponse {

    @ApiModelProperty(value = "ID do grupo.")
    private Long tipoCampoGrupoId;

    @ApiModelProperty(value = "Base Interna ID")
    private Long baseInternaId;


    public RequestCadastroTipoCampo(){}

    public RequestCadastroTipoCampo(Campo c) {
        super(c);
    }

    public RequestCadastroTipoCampo(TipoCampo tc) {
        super(tc);
    }

    @NotNull
    public Long getTipoCampoGrupoId() {
        return tipoCampoGrupoId;
    }

    public void setTipoCampoGrupoId(Long tipoCampoGrupoId) {
        this.tipoCampoGrupoId = tipoCampoGrupoId;
    }

    @Override
    public String getOpcoes() {
        return super.getOpcoes();
    }

    @Override
    public List<String> getOpcoesList() {
        return super.getOpcoesList();
    }

    @NotNull
    @Override
    public int getOrdem() {
        return super.getOrdem();
    }

    @NotNull
    @Override
    public Integer getTamanhoMaximo() {
        return super.getTamanhoMaximo();
    }

    @NotNull
    @Override
    public Integer getTamanhoMinimo() {
        return super.getTamanhoMinimo();
    }


    @Override
    public Long getId() {
        return super.getId();
    }

    @Override
    public String getDica() {
        return super.getDica();
    }

    @NotNull
    @Override
    public String getNome() {
        return super.getNome();
    }

    @Override
    public String getValor() {
        return super.getValor();
    }

    @NotNull
    @Override
    public TipoEntradaCampo getTipo() {
        return super.getTipo();
    }

    public Long getBaseInternaId() {
        return baseInternaId;
    }

    public void setBaseInternaId(Long baseInternaId) {
        this.baseInternaId = baseInternaId;
    }
}