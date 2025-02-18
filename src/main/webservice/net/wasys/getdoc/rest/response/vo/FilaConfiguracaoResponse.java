package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.Area;
import net.wasys.getdoc.domain.entity.FilaConfiguracao;
import net.wasys.getdoc.rest.request.vo.RequestFilaConfiguracao;
import net.wasys.getdoc.rest.request.vo.SuperVo;

import java.util.Date;

@ApiModel(value = "FilaConfiguracaoResponse")
public class FilaConfiguracaoResponse extends SuperVo {

    @ApiModelProperty(notes = "ID.")
    private Long id;
    @ApiModelProperty(notes = "descricao.")
    private String descricao;
    @ApiModelProperty(notes = "status.")
    private String status;
    @ApiModelProperty(notes = "exibir Nao Associados.")
    private boolean exibirNaoAssociados;
    @ApiModelProperty(notes = "exibir Associados Outros.")
    private boolean exibirAssociadosOutros;
    @ApiModelProperty(notes = "permissao Editar Outros.")
    private boolean permissaoEditarOutros;
    @ApiModelProperty(notes = "verificar Proxima Requisicao.")
    private boolean verificarProximaRequisicao;

    public FilaConfiguracaoResponse() {
    }

    public FilaConfiguracaoResponse(FilaConfiguracao filaConfiguracao) {
        this.id = filaConfiguracao.getId();
        this.descricao = filaConfiguracao.getDescricao();
        this.status = filaConfiguracao.getStatus();
        this.exibirNaoAssociados = filaConfiguracao.isExibirNaoAssociados();
        this.exibirAssociadosOutros = filaConfiguracao.isExibirAssociadosOutros();
        this.permissaoEditarOutros = filaConfiguracao.isPermissaoEditarOutros();
        this.verificarProximaRequisicao = filaConfiguracao.isVerificarProximaRequisicao();
    }

    public FilaConfiguracaoResponse(RequestFilaConfiguracao filaConfiguracao) {
        this.id = filaConfiguracao.getId();
        this.descricao = filaConfiguracao.getDescricao();
        this.status = filaConfiguracao.getStatus();
        this.exibirNaoAssociados = filaConfiguracao.isExibirNaoAssociados();
        this.exibirAssociadosOutros = filaConfiguracao.isExibirAssociadosOutros();
        this.permissaoEditarOutros = filaConfiguracao.isPermissaoEditarOutros();
        this.verificarProximaRequisicao = filaConfiguracao.isVerificarProximaRequisicao();
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isExibirNaoAssociados() {
        return exibirNaoAssociados;
    }

    public void setExibirNaoAssociados(boolean exibirNaoAssociados) {
        this.exibirNaoAssociados = exibirNaoAssociados;
    }

    public boolean isExibirAssociadosOutros() {
        return exibirAssociadosOutros;
    }

    public void setExibirAssociadosOutros(boolean exibirAssociadosOutros) {
        this.exibirAssociadosOutros = exibirAssociadosOutros;
    }

    public boolean isPermissaoEditarOutros() {
        return permissaoEditarOutros;
    }

    public void setPermissaoEditarOutros(boolean permissaoEditarOutros) {
        this.permissaoEditarOutros = permissaoEditarOutros;
    }

    public boolean isVerificarProximaRequisicao() {
        return verificarProximaRequisicao;
    }

    public void setVerificarProximaRequisicao(boolean verificarProximaRequisicao) {
        this.verificarProximaRequisicao = verificarProximaRequisicao;
    }
}