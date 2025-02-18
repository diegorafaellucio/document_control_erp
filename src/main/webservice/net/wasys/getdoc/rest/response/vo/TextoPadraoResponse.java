package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.TextoPadrao;
import net.wasys.getdoc.rest.request.vo.SuperVo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

@ApiModel(value = "TextoPadraoResponse")
public class TextoPadraoResponse extends SuperVo {

    @ApiModelProperty(notes = "ID.")
    private Long id;

    @ApiModelProperty(notes = "NOme.")
    private String nome;

    @ApiModelProperty(notes = "Texto.")
    private String texto;

    @ApiModelProperty(notes = "Indica se está ativo ou não.")
    private boolean ativo;

    @ApiModelProperty(notes = "Permissões de uso.")
    private String permissoesDeUso;

    @ApiModelProperty(notes = "Nome do processo.")
    private String processoNome;

    @ApiModelProperty(value = "Envia e-email.")
    private boolean envioEmail;

    @ApiModelProperty(value = "Resgistro de evidência.")
    private boolean registroEvidencia;

    @ApiModelProperty(value = "Solicitação da área.")
    private boolean solicitacaoArea;

    public TextoPadraoResponse() {
    }

    public TextoPadraoResponse(TextoPadrao textoPadrao) {
        this.id = textoPadrao.getId();
        this.nome = textoPadrao.getNome();
        this.ativo = textoPadrao.getAtivo();
        this.texto = textoPadrao.getTexto();
        this.permissoesDeUso = textoPadrao.getPermissoesDeUso();
        //this.processoNome = textoPadrao.getTipoProcesso().getNome();

        String permissoesDeUso = textoPadrao.getPermissoesDeUso();
        this.envioEmail = StringUtils.contains(permissoesDeUso, TextoPadrao.ENVIO_EMAIL);
        this.registroEvidencia = StringUtils.contains(permissoesDeUso, TextoPadrao.REGISTRO_EVIDENCIA);
        this.solicitacaoArea = StringUtils.contains(permissoesDeUso, TextoPadrao.SOLICITACAO_AREA);
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

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public String getPermissoesDeUso() {
        return permissoesDeUso;
    }

    public void setPermissoesDeUso(String permissoesDeUso) {
        this.permissoesDeUso = permissoesDeUso;
    }

    public String getProcessoNome() {
        return processoNome;
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

    public void setProcessoNome(String processoNome) {
        this.processoNome = processoNome;
    }

}