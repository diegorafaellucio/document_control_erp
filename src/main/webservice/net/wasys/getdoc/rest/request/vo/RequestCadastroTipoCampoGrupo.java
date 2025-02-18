package net.wasys.getdoc.rest.request.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.Campo;
import net.wasys.getdoc.domain.entity.TipoCampo;
import net.wasys.getdoc.domain.entity.TipoCampoGrupo;
import net.wasys.getdoc.domain.enumeration.TipoEntradaCampo;
import net.wasys.getdoc.rest.annotations.NotNull;
import net.wasys.getdoc.rest.response.vo.CampoResponse;

import java.util.List;

@ApiModel(value = "RequestCadastroTipoCampoGrupo")
public class RequestCadastroTipoCampoGrupo extends SuperVo {

    @ApiModelProperty(value = "Nome do grupo.")
    private String nome;

    @ApiModelProperty(value = "Adicionar na criação do processo.")
    private boolean adicionarCriacaoProcesso;

    @ApiModelProperty(value = "Sempre visível.")
    private boolean sempreVisivel;

    @ApiModelProperty(value = "Id da situação")
    private Long situacaoId;

    public RequestCadastroTipoCampoGrupo() {
    }

    public RequestCadastroTipoCampoGrupo(TipoCampoGrupo tipoCampoGrupo) {
        this.nome = tipoCampoGrupo.getNome();
        this.adicionarCriacaoProcesso = tipoCampoGrupo.getCriacaoProcesso();
        this.sempreVisivel = tipoCampoGrupo.getAbertoPadrao();
        // FIXME
        /*if(tipoCampoGrupo. getSituacao() != null){
            this.situacaoId = tipoCampoGrupo.getSituacao().getId();
        }*/
    }

    @NotNull
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @NotNull
    public boolean isAdicionarCriacaoProcesso() {
        return adicionarCriacaoProcesso;
    }

    public void setAdicionarCriacaoProcesso(boolean adicionarCriacaoProcesso) {
        this.adicionarCriacaoProcesso = adicionarCriacaoProcesso;
    }

    @NotNull
    public boolean isSempreVisivel() {
        return sempreVisivel;
    }

    public void setSempreVisivel(boolean sempreVisivel) {
        this.sempreVisivel = sempreVisivel;
    }

    public Long getSituacaoId() {
        return situacaoId;
    }

    public void setSituacaoId(Long situacaoId) {
        this.situacaoId = situacaoId;
    }
}