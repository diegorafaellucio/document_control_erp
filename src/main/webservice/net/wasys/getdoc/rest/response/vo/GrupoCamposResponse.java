package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.rest.request.vo.SuperVo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApiModel(value = "GrupoCamposResponse")
public class GrupoCamposResponse extends SuperVo {

    @ApiModelProperty(value = "ID do grupo.")
    private Long id;

    @ApiModelProperty(value = "Nome do grupo.")
    private String nome;

    @ApiModelProperty(value = "Ordem de exibição do campo na tela.")
    private int ordem;

    @ApiModelProperty(value = "Grupo deve aparecer expandido na tela de consulta.")
    private Boolean abertoPadrao;

    @ApiModelProperty(value = "Situacao onde utiliza esse campos.")
    private Situacao situacao;

    @ApiModelProperty(value = "Lista de campos.")
    private List<CampoResponse> campos;

    public GrupoCamposResponse(){}

    public GrupoCamposResponse(CampoGrupo cg) {
        this.id = cg.getId();
        this.nome = cg.getNome();
        this.ordem = cg.getOrdem();
        this.abertoPadrao = cg.getAbertoPadrao();
    }

    public GrupoCamposResponse(TipoCampoGrupo tcg) {
        this.id = tcg.getId();
        this.nome = tcg.getNome();
        this.ordem = tcg.getOrdem();
        this.abertoPadrao = tcg.getAbertoPadrao();
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

    public List<CampoResponse> getCampos() {
        return campos;
    }

    public void setCampos(List<CampoResponse> campos) {
        this.campos = campos;
    }

    public int getOrdem() {
        return ordem;
    }

    public void setOrdem(int ordem) {
        this.ordem = ordem;
    }

    public void parserCampos(List<? extends CampoAbstract> list) {
        if(list != null && list.size() > 0) {
            this.campos = new ArrayList<>();
            for (CampoAbstract c : list) {
                if (c != null) {
                    CampoResponse cr = new CampoResponse(c);
                    this.campos.add(cr);
                }
            }
        }
    }

    public Situacao getSituacao() {
        return situacao;
    }

    public void setSituacao(Situacao situacao) {
        this.situacao = situacao;
    }

    public Boolean getAbertoPadrao() {
        return abertoPadrao;
    }

    public void setAbertoPadrao(Boolean abertoPadrao) {
        this.abertoPadrao = abertoPadrao;
    }
}