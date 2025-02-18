package net.wasys.getdoc.rest.request.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.rest.annotations.NotNull;

import java.util.List;

/**
 *
 */
@ApiModel(value = "RequestCadastrarUsuario")
public class RequestCadastrarUsuario extends SuperVo {

    @ApiModelProperty(notes = "Nome.")
    private String nome;

    @ApiModelProperty(notes = "Login.")
    private String login;

    @ApiModelProperty(notes = "E-mail.")
    private String email;

    @ApiModelProperty(notes = "Indica se o usuário é gestor da área.")
    private boolean gestorArea;

    @ApiModelProperty(notes = "RoleGD.")
    private RoleGD roleGd;

    @ApiModelProperty(notes = "Telefone.")
    private String telefone;

    @ApiModelProperty(notes = "ID do Subperfil.")
    private Long subperfilAtivoId;

    @ApiModelProperty(notes = "ID da área.")
    private Long areaId;

    @ApiModelProperty(notes = "Processos vinculados ao usuário.")
    private List<Long> processoId;

    @ApiModelProperty(notes = "Subperfis vinculados ao usuário.")
    private List<Long> subperfilId;

    @ApiModelProperty(notes = "Ordem de trabalho definida pelo sistema.")
    private boolean ordemAtividadeFixa;

    @ApiModelProperty(notes = "Notificar atraso nas requisições.")
    private boolean notificarAtrasoRequisicoes;

    @ApiModelProperty(notes = "Notificar atraso nas solicitações.")
    private boolean notificarAtrasoSolicitacao;

    @NotNull
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @NotNull
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    @NotNull
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @NotNull
    public RoleGD getRoleGd() {
        return roleGd;
    }

    public void setRoleGd(RoleGD roleGd) {
        this.roleGd = roleGd;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public Long getSubperfilAtivoId() {
        return subperfilAtivoId;
    }

    public void setSubperfilAtivoId(Long subperfilAtivoId) {
        this.subperfilAtivoId = subperfilAtivoId;
    }

    public List<Long> getProcessoId() {
        return processoId;
    }

    public void setProcessoId(List<Long> processoId) {
        this.processoId = processoId;
    }

    public boolean isOrdemAtividadeFixa() {
        return ordemAtividadeFixa;
    }

    public void setOrdemAtividadeFixa(boolean ordemAtividadeFixa) {
        this.ordemAtividadeFixa = ordemAtividadeFixa;
    }

    public boolean isNotificarAtrasoRequisicoes() {
        return notificarAtrasoRequisicoes;
    }

    public void setNotificarAtrasoRequisicoes(boolean notificarAtrasoRequisicoes) {
        this.notificarAtrasoRequisicoes = notificarAtrasoRequisicoes;
    }

    public boolean isNotificarAtrasoSolicitacao() {
        return notificarAtrasoSolicitacao;
    }

    public void setNotificarAtrasoSolicitacao(boolean notificarAtrasoSolicitacao) {
        this.notificarAtrasoSolicitacao = notificarAtrasoSolicitacao;
    }

    public boolean isGestorArea() {
        return gestorArea;
    }

    public void setGestorArea(boolean gestorArea) {
        this.gestorArea = gestorArea;
    }

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public List<Long> getSubperfilId() {
        return subperfilId;
    }

    public void setSubperfilId(List<Long> subperfilId) {
        this.subperfilId = subperfilId;
    }
}
