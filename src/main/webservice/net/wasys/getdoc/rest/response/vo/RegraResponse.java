package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.Regra;
import net.wasys.getdoc.domain.entity.RegraRole;
import net.wasys.getdoc.domain.enumeration.FarolRegra;
import net.wasys.getdoc.domain.enumeration.StatusProcessoRegra;
import net.wasys.getdoc.domain.enumeration.TipoExecucaoRegra;
import net.wasys.getdoc.rest.request.vo.SuperVo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ApiModel(value = "RegraResponse")
public class RegraResponse extends SuperVo {

    @ApiModelProperty(notes = "ID.")
    private Long id;
    @ApiModelProperty(notes = "nome.")
    private String nome;
    @ApiModelProperty(notes = "descricao.")
    private String descricao;
    @ApiModelProperty(notes = "data de Alteracao.")
    private Date dataAlteracao;
    @ApiModelProperty(notes = "ativa.")
    private boolean ativa = true;
    @ApiModelProperty(notes = "inicio de Vigencia.")
    private Date inicioVigencia;
    @ApiModelProperty(notes = "fim de Vigencia.")
    private Date fimVigencia;
    @ApiModelProperty(notes = "Tipode Execucao da Regra.")
    private TipoExecucaoRegra tipoExecucao;
   /* @ApiModelProperty(notes = "situacao.")
    private Situacao situacao;
    @ApiModelProperty(notes = "RegraTipoProcesso.")
    private Set<RegraTipoProcesso> tiposProcessos = new HashSet<>(0);*/
   @ApiModelProperty(notes = "Roles que tem permissão para ver a regra.")
   private List<String> roles;


    public RegraResponse() {
    }

    public RegraResponse(Regra regra) {
        this.id = regra.getId();
        this.nome = regra.getNome();
        this.descricao = regra.getDescricao();
        this.dataAlteracao = regra.getDataAlteracao();
        this.ativa = regra.getAtiva();
        this.inicioVigencia = regra.getInicioVigencia();
        this.fimVigencia = regra.getFimVigencia();
        this.tipoExecucao = regra.getTipoExecucao();
        List<RegraRole> regraRoles = regra.getRegraRoles();
        this.roles = new ArrayList<>();
        regraRoles.forEach(regraRole -> this.roles.add(regraRole.getRole()));
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Date getDataAlteracao() {
        return dataAlteracao;
    }

    public void setDataAlteracao(Date dataAlteracao) {
        this.dataAlteracao = dataAlteracao;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public void setAtiva(boolean ativa) {
        this.ativa = ativa;
    }

    public Date getInicioVigencia() {
        return inicioVigencia;
    }

    public void setInicioVigencia(Date inicioVigencia) {
        this.inicioVigencia = inicioVigencia;
    }

    public Date getFimVigencia() {
        return fimVigencia;
    }

    public void setFimVigencia(Date fimVigencia) {
        this.fimVigencia = fimVigencia;
    }

    public TipoExecucaoRegra getTipoExecucao() {
        return tipoExecucao;
    }

    public void setTipoExecucao(TipoExecucaoRegra tipoExecucao) {
        this.tipoExecucao = tipoExecucao;
    }

    public List<String> getRoles() { return roles; }

    public void setRoles(List<String> roles) { this.roles = roles; }
}