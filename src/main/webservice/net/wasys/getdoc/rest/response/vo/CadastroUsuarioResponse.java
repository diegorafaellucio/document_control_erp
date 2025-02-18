package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.entity.UsuarioTipoProcesso;
import net.wasys.getdoc.domain.enumeration.MotivoBloqueioUsuario;
import net.wasys.getdoc.domain.enumeration.StatusUsuario;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@ApiModel(value = "CadastroUsuarioResponse")
public class CadastroUsuarioResponse extends UsuarioResponse {

    @ApiModelProperty(value = "Status")
    private StatusUsuario status;

    @ApiModelProperty(value = "Data do cadastro")
    private Date dataCadastro;

    @ApiModelProperty(value = "Data do último acesso")
    private Date dataUltimoAcesso;

    @ApiModelProperty(value = "Data do último acesso")
    private Date dataBloqueio;

    @ApiModelProperty(value = "Data expiração bloqueio")
    private Date dataExpiracaoBloqueio;

    @ApiModelProperty(notes = "Ordem de trabalho definida pelo sistema.")
    private boolean ordemAtividadeFixa;

    @ApiModelProperty(notes = "Notificar atraso nas requisições.")
    private boolean notificarAtrasoRequisicoes;

    @ApiModelProperty(notes = "Notificar atraso nas solicitações.")
    private boolean notificarAtrasoSolicitacao;

    @ApiModelProperty(notes = "Indica se o usuário é gestor da área.")
    private boolean gestorArea;

    @ApiModelProperty(notes = "Processos vinculados ao usuário.")
    private List<Long> processoId;

    @ApiModelProperty(notes = "ID da área.")
    private Long areaId;

    @ApiModelProperty(notes = "Login do usuario.")
    private String login;

    @ApiModelProperty(notes = "Telefone.")
    private String telefone;

    @ApiModelProperty(notes = "Motivo do bloqueio.")
    private MotivoBloqueioUsuario motivoBloqueio;

    @ApiModelProperty(notes = "Subperfil.")
    private SubPerfilResponse subPerfil;

    @ApiModelProperty(notes = "Subperfil.")
    private MotivoDesativarUsuarioResponse motivoDesativacao;

    public CadastroUsuarioResponse(){}

    public CadastroUsuarioResponse(Usuario usuario){
        super(usuario);
        if(usuario != null) {
            this.telefone = usuario.getTelefone();
            this.login = usuario.getLogin();
            this.status = usuario.getStatus();
            this.dataCadastro = usuario.getDataCadastro();
            this.dataUltimoAcesso = usuario.getDataUltimoAcesso();
            this.notificarAtrasoRequisicoes = usuario.getNotificarAtrasoRequisicoes();
            this.ordemAtividadeFixa = usuario.getOrdemAtividadesFixa();
            this.gestorArea = usuario.getGestorArea();
            this.dataBloqueio = usuario.getDataBloqueio();
            this.notificarAtrasoSolicitacao = usuario.getNotificarAtrasoSolicitacoes();
            this.dataExpiracaoBloqueio = usuario.getDataExpiracaoBloqueio();
            this.motivoBloqueio = usuario.getMotivoBloqueio();
            this.motivoDesativacao = new MotivoDesativarUsuarioResponse(usuario.getMotivoDesativacao(), null);

            if(CollectionUtils.isNotEmpty(usuario.getTiposProcessos())){
                this.processoId = new ArrayList<>();

                for (UsuarioTipoProcesso processo : usuario.getTiposProcessos()) {
                    this.processoId.add(processo.getId());
                }
            }

            if(usuario.getSubperfilAtivo() != null){
                this.subPerfil = new SubPerfilResponse(usuario.getSubperfilAtivo());
            }

            if(usuario.getArea() != null){
                this.areaId = usuario.getArea().getId();
            }
        }
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public StatusUsuario getStatus() {
        return status;
    }

    public void setStatus(StatusUsuario status) {
        this.status = status;
    }

    public Date getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(Date dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public Date getDataUltimoAcesso() {
        return dataUltimoAcesso;
    }

    public void setDataUltimoAcesso(Date dataUltimoAcesso) {
        this.dataUltimoAcesso = dataUltimoAcesso;
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

    public List<Long> getProcessoId() {
        return processoId;
    }

    public void setProcessoId(List<Long> processoId) {
        this.processoId = processoId;
    }

    public SubPerfilResponse getSubPerfil() {
        return subPerfil;
    }

    public void setSubPerfil(SubPerfilResponse subPerfil) {
        this.subPerfil = subPerfil;
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

    public Date getDataBloqueio() {
        return dataBloqueio;
    }

    public void setDataBloqueio(Date dataBloqueio) {
        this.dataBloqueio = dataBloqueio;
    }

    public MotivoDesativarUsuarioResponse getMotivoDesativacao() {
        return motivoDesativacao;
    }

    public void setMotivoDesativacao(MotivoDesativarUsuarioResponse motivoDesativacao) {
        this.motivoDesativacao = motivoDesativacao;
    }

    public Date getDataExpiracaoBloqueio() {
        return dataExpiracaoBloqueio;
    }

    public void setDataExpiracaoBloqueio(Date dataExpiracaoBloqueio) {
        this.dataExpiracaoBloqueio = dataExpiracaoBloqueio;
    }

    public boolean isNotificarAtrasoSolicitacao() {
        return notificarAtrasoSolicitacao;
    }

    public void setNotificarAtrasoSolicitacao(boolean notificarAtrasoSolicitacao) {
        this.notificarAtrasoSolicitacao = notificarAtrasoSolicitacao;
    }

    public MotivoBloqueioUsuario getMotivoBloqueio() {
        return motivoBloqueio;
    }

    public void setMotivoBloqueio(MotivoBloqueioUsuario motivoBloqueio) {
        this.motivoBloqueio = motivoBloqueio;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
}