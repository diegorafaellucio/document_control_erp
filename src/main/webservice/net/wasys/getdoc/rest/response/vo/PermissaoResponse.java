package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.rest.request.vo.SuperVo;

@ApiModel(value = "PermissaoResponse")
public class PermissaoResponse extends SuperVo {

    @ApiModelProperty
    private boolean acessoBloqueado;

    @ApiModelProperty
    private boolean podeAdicionarDocumento;

    @ApiModelProperty
    private boolean podeAdicionarSolicitacao;

    @ApiModelProperty
    private boolean podeAlterarPrazoExpirado;

    @ApiModelProperty
    private boolean podeCancelar;

    @ApiModelProperty
    private boolean podeConcluir;

    @ApiModelProperty
    private boolean podeConcluirSituacao;

    @ApiModelProperty
    private boolean podeDigitalizarDocumento;

    @ApiModelProperty
    private boolean podeDigitalizarDocumentoTwain;

    @ApiModelProperty
    private boolean podeEditar;

    @ApiModelProperty
    private boolean podeEditarCampos;

    @ApiModelProperty
    private boolean podeEditarProcesso;

    @ApiModelProperty
    private boolean podeEmAcompanhamento;

    @ApiModelProperty
    private boolean podeEnviarEmail;

    @ApiModelProperty
    private boolean podeEnviarProcesso;

    @ApiModelProperty
    private boolean podeExcluir;

    @ApiModelProperty
    private boolean podeMarcarEmailLido;

    @ApiModelProperty
    private boolean podeReenviarAnalise;

    @ApiModelProperty
    private boolean podeResponderPendencia;

    @ApiModelProperty
    private boolean podePendenciar;

    @ApiModelProperty
    private boolean podeRegistrarEvidencia;

    @ApiModelProperty
    private boolean podeTrocarAnalista;

    @ApiModelProperty
    private boolean podeTrocarTipoProcesso;

    @ApiModelProperty
    private boolean podeSubirNivelPrioridade;

    @ApiModelProperty
    private boolean podeVerificarProxima;

    @ApiModelProperty
    private boolean podeIniciarTrabalho;

    @ApiModelProperty
    private boolean podeMarcarEmailNaoLido;

    @ApiModelProperty
    private boolean mostrarAbaAjuda;

    @ApiModelProperty
    private boolean mostrarNumeroProcesso;

    @ApiModelProperty
    private boolean mostrarAbaEmails;

    @ApiModelProperty
    private boolean mostrarAbaAcompanhamento;

    @ApiModelProperty
    private boolean mostrarAbaSolicitacoes;

    @ApiModelProperty
    private boolean podeIniciarTarefa;

    @ApiModelProperty
    private boolean podeReprocessarTodasRegras;

    @ApiModelProperty
    private boolean podeReprocessarErrosRegras;

    @ApiModelProperty
    private boolean podeResponderSolicitacao;

    @ApiModelProperty
    private boolean podeNaoAceitar;

    @ApiModelProperty
    private boolean podeRecusarSolicitacao;

    @ApiModelProperty
    private boolean mostrarAbaAnexos;

    public boolean isAcessoBloqueado() {
        return acessoBloqueado;
    }

    public void setAcessoBloqueado(boolean acessoBloqueado) {
        this.acessoBloqueado = acessoBloqueado;
    }

    public boolean isPodeAdicionarDocumento() {
        return podeAdicionarDocumento;
    }

    public void setPodeAdicionarDocumento(boolean podeAdicionarDocumento) {
        this.podeAdicionarDocumento = podeAdicionarDocumento;
    }

    public boolean isPodeAdicionarSolicitacao() {
        return podeAdicionarSolicitacao;
    }

    public void setPodeAdicionarSolicitacao(boolean podeAdicionarSolicitacao) {
        this.podeAdicionarSolicitacao = podeAdicionarSolicitacao;
    }

    public boolean isPodeAlterarPrazoExpirado() {
        return podeAlterarPrazoExpirado;
    }

    public void setPodeAlterarPrazoExpirado(boolean podeAlterarPrazoExpirado) {
        this.podeAlterarPrazoExpirado = podeAlterarPrazoExpirado;
    }

    public boolean isPodeCancelar() {
        return podeCancelar;
    }

    public void setPodeCancelar(boolean podeCancelar) {
        this.podeCancelar = podeCancelar;
    }

    public boolean isPodeConcluir() {
        return podeConcluir;
    }

    public void setPodeConcluir(boolean podeConcluir) {
        this.podeConcluir = podeConcluir;
    }

    public boolean isPodeConcluirSituacao() {
        return podeConcluirSituacao;
    }

    public void setPodeConcluirSituacao(boolean podeConcluirSituacao) {
        this.podeConcluirSituacao = podeConcluirSituacao;
    }

    public boolean isPodeDigitalizarDocumento() {
        return podeDigitalizarDocumento;
    }

    public void setPodeDigitalizarDocumento(boolean podeDigitalizarDocumento) {
        this.podeDigitalizarDocumento = podeDigitalizarDocumento;
    }

    public boolean isPodeDigitalizarDocumentoTwain() {
        return podeDigitalizarDocumentoTwain;
    }

    public void setPodeDigitalizarDocumentoTwain(boolean podeDigitalizarDocumentoTwain) {
        this.podeDigitalizarDocumentoTwain = podeDigitalizarDocumentoTwain;
    }

    public boolean isPodeEditar() {
        return podeEditar;
    }

    public void setPodeEditar(boolean podeEditar) {
        this.podeEditar = podeEditar;
    }

    public boolean isPodeEditarCampos() {
        return podeEditarCampos;
    }

    public void setPodeEditarCampos(boolean podeEditarCampos) {
        this.podeEditarCampos = podeEditarCampos;
    }

    public boolean isPodeEditarProcesso() {
        return podeEditarProcesso;
    }

    public void setPodeEditarProcesso(boolean podeEditarProcesso) {
        this.podeEditarProcesso = podeEditarProcesso;
    }

    public boolean isPodeEmAcompanhamento() {
        return podeEmAcompanhamento;
    }

    public void setPodeEmAcompanhamento(boolean podeEmAcompanhamento) {
        this.podeEmAcompanhamento = podeEmAcompanhamento;
    }

    public boolean isPodeEnviarEmail() {
        return podeEnviarEmail;
    }

    public void setPodeEnviarEmail(boolean podeEnviarEmail) {
        this.podeEnviarEmail = podeEnviarEmail;
    }

    public boolean isPodeEnviarProcesso() {
        return podeEnviarProcesso;
    }

    public void setPodeEnviarProcesso(boolean podeEnviarProcesso) {
        this.podeEnviarProcesso = podeEnviarProcesso;
    }

    public boolean isPodeExcluir() {
        return podeExcluir;
    }

    public void setPodeExcluir(boolean podeExcluir) {
        this.podeExcluir = podeExcluir;
    }

    public boolean isPodeMarcarEmailLido() {
        return podeMarcarEmailLido;
    }

    public void setPodeMarcarEmailLido(boolean podeMarcarEmailLido) {
        this.podeMarcarEmailLido = podeMarcarEmailLido;
    }

    public boolean isPodeReenviarAnalise() {
        return podeReenviarAnalise;
    }

    public void setPodeReenviarAnalise(boolean podeReenviarAnalise) {
        this.podeReenviarAnalise = podeReenviarAnalise;
    }

    public boolean isPodeResponderPendencia() {
        return podeResponderPendencia;
    }

    public void setPodeResponderPendencia(boolean podeResponderPendencia) {
        this.podeResponderPendencia = podeResponderPendencia;
    }

    public boolean isPodePendenciar() {
        return podePendenciar;
    }

    public void setPodePendenciar(boolean podePendenciar) {
        this.podePendenciar = podePendenciar;
    }

    public boolean isPodeRegistrarEvidencia() {
        return podeRegistrarEvidencia;
    }

    public void setPodeRegistrarEvidencia(boolean podeRegistrarEvidencia) {
        this.podeRegistrarEvidencia = podeRegistrarEvidencia;
    }

    public boolean isPodeTrocarAnalista() {
        return podeTrocarAnalista;
    }

    public void setPodeTrocarAnalista(boolean podeTrocarAnalista) {
        this.podeTrocarAnalista = podeTrocarAnalista;
    }

    public boolean isPodeTrocarTipoProcesso() {
        return podeTrocarTipoProcesso;
    }

    public void setPodeTrocarTipoProcesso(boolean podeTrocarTipoProcesso) {
        this.podeTrocarTipoProcesso = podeTrocarTipoProcesso;
    }

    public boolean isPodeSubirNivelPrioridade() {
        return podeSubirNivelPrioridade;
    }

    public void setPodeSubirNivelPrioridade(boolean podeSubirNivelPrioridade) {
        this.podeSubirNivelPrioridade = podeSubirNivelPrioridade;
    }

    public boolean isPodeVerificarProxima() {
        return podeVerificarProxima;
    }

    public void setPodeVerificarProxima(boolean podeVerificarProxima) {
        this.podeVerificarProxima = podeVerificarProxima;
    }

    public boolean isPodeIniciarTrabalho() {
        return podeIniciarTrabalho;
    }

    public void setPodeIniciarTrabalho(boolean podeIniciarTrabalho) {
        this.podeIniciarTrabalho = podeIniciarTrabalho;
    }

    public boolean isPodeMarcarEmailNaoLido() {
        return podeMarcarEmailNaoLido;
    }

    public void setPodeMarcarEmailNaoLido(boolean podeMarcarEmailNaoLido) {
        this.podeMarcarEmailNaoLido = podeMarcarEmailNaoLido;
    }

    public boolean isMostrarAbaAjuda() {
        return mostrarAbaAjuda;
    }

    public void setMostrarAbaAjuda(boolean mostrarAbaAjuda) {
        this.mostrarAbaAjuda = mostrarAbaAjuda;
    }

    public boolean isMostrarNumeroProcesso() {
        return mostrarNumeroProcesso;
    }

    public void setMostrarNumeroProcesso(boolean mostrarNumeroProcesso) {
        this.mostrarNumeroProcesso = mostrarNumeroProcesso;
    }

    public boolean isMostrarAbaEmails() {
        return mostrarAbaEmails;
    }

    public void setMostrarAbaEmails(boolean mostrarAbaEmails) {
        this.mostrarAbaEmails = mostrarAbaEmails;
    }

    public boolean isMostrarAbaAcompanhamento() {
        return mostrarAbaAcompanhamento;
    }

    public void setMostrarAbaAcompanhamento(boolean mostrarAbaAcompanhamento) {
        this.mostrarAbaAcompanhamento = mostrarAbaAcompanhamento;
    }

    public boolean isMostrarAbaSolicitacoes() {
        return mostrarAbaSolicitacoes;
    }

    public void setMostrarAbaSolicitacoes(boolean mostrarAbaSolicitacoes) {
        this.mostrarAbaSolicitacoes = mostrarAbaSolicitacoes;
    }

    public boolean isPodeIniciarTarefa() {
        return podeIniciarTarefa;
    }

    public void setPodeIniciarTarefa(boolean podeIniciarTarefa) {
        this.podeIniciarTarefa = podeIniciarTarefa;
    }

    public boolean isPodeReprocessarTodasRegras() {
        return podeReprocessarTodasRegras;
    }

    public void setPodeReprocessarTodasRegras(boolean podeReprocessarTodasRegras) {
        this.podeReprocessarTodasRegras = podeReprocessarTodasRegras;
    }

    public boolean isPodeReprocessarErrosRegras() {
        return podeReprocessarErrosRegras;
    }

    public void setPodeReprocessarErrosRegras(boolean podeReprocessarErrosRegras) {
        this.podeReprocessarErrosRegras = podeReprocessarErrosRegras;
    }

    public boolean isPodeResponderSolicitacao() {
        return podeResponderSolicitacao;
    }

    public void setPodeResponderSolicitacao(boolean podeResponderSolicitacao) {
        this.podeResponderSolicitacao = podeResponderSolicitacao;
    }

    public boolean isPodeNaoAceitar() {
        return podeNaoAceitar;
    }

    public void setPodeNaoAceitar(boolean podeNaoAceitar) {
        this.podeNaoAceitar = podeNaoAceitar;
    }

    public boolean isPodeRecusarSolicitacao() {
        return podeRecusarSolicitacao;
    }

    public void setPodeRecusarSolicitacao(boolean podeRecusarSolicitacao) {
        this.podeRecusarSolicitacao = podeRecusarSolicitacao;
    }

    public boolean isMostrarAbaAnexos() {
        return mostrarAbaAnexos;
    }

    public void setMostrarAbaAnexos(boolean mostrarAbaAnexos) {
        this.mostrarAbaAnexos = mostrarAbaAnexos;
    }


}