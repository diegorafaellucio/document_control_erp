package net.wasys.getdoc.rest.request.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.TipoCampoGrupo;
import net.wasys.getdoc.rest.annotations.NotNull;

@ApiModel(value = "RequestCadastroTextoPadrao")
public class RequestCadastroTextoPadrao extends SuperVo {

    @ApiModelProperty(value = "ID do tipo de processo.")
    private Long tipoProcessoId;

    @ApiModelProperty(value = "Nome do texto padrão.")
    private String nome;

    @ApiModelProperty(value = "Envia e-email.")
    private boolean envioEmail;

    @ApiModelProperty(value = "Resgistro de evidência.")
    private boolean registroEvidencia;

    @ApiModelProperty(value = "Solicitação da área.")
    private boolean solicitacaoArea;

    @ApiModelProperty(value = "Ativo?.")
    private boolean ativo;

    @ApiModelProperty(value = "Texto em si.")
    private String texto;

    @NotNull
    public Long getTipoProcessoId() {
        return tipoProcessoId;
    }

    public void setTipoProcessoId(Long tipoProcessoId) {
        this.tipoProcessoId = tipoProcessoId;
    }

    @NotNull
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public boolean isEnvioEmail() {
        return envioEmail;
    }

    public void setEnvioEmail(boolean envioEmail) {
        this.envioEmail = envioEmail;
    }

    public boolean isRegistroEvidencia() {
        return registroEvidencia;
    }

    public void setRegistroEvidencia(boolean registroEvidencia) {
        this.registroEvidencia = registroEvidencia;
    }

    public boolean isSolicitacaoArea() {
        return solicitacaoArea;
    }

    public void setSolicitacaoArea(boolean solicitacaoArea) {
        this.solicitacaoArea = solicitacaoArea;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    @NotNull
    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }
}