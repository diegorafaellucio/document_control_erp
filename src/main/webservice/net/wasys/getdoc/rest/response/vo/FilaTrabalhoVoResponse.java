package net.wasys.getdoc.rest.response.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.StatusOcr;
import net.wasys.getdoc.domain.enumeration.StatusPrazo;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.vo.ProcessoVO;
import net.wasys.getdoc.rest.request.vo.SuperVo;
import net.wasys.util.DummyUtils;

import java.util.Date;
import java.util.Objects;

@ApiModel(value = "FilaTrabalhoVoResponse")
public class FilaTrabalhoVoResponse extends SuperVo {

    @ApiModelProperty(notes = "ID do processo.")
    private Long processoId;

    @ApiModelProperty(value = "Número do processo.")
    private String processoNumero;

    @ApiModelProperty(value = "Tempo de restante do status.")
    private String tempoRestanteStatus;

    @ApiModelProperty(value = "Tempo de restante da situação.")
    private String tempoRestanteSituacao;

    @ApiModelProperty(value = "Tipo do processo.")
    private TipoProcessoResponse tipoProcesso;

    @ApiModelProperty(value = "Situação do processo.")
    private SituacaoResponse situacao;

    @ApiModelProperty(value = "Data de envio.")
    private Date dataEnvio;

    @ApiModelProperty(value = "Analista.")
    private UsuarioResponse analista;

    @ApiModelProperty(value = "Nome completo.")
    private String nomeCompleto;

    @ApiModelProperty(value = "Indicador de email não lido.")
    private Boolean possuiEmailNaoLido;

    @ApiModelProperty(value = "Indicador de evidência não lida.")
    private Boolean evidenciaNaoLida;

    @ApiModelProperty(value = "Indicador de reenvio não lido.")
    private Boolean reenvioNaoLido;

    @ApiModelProperty(value = "Indicador do status do OCR.")
    private StatusOcr statusOcr;

    @ApiModelProperty(value = "Status do processo.")
    private StatusProcesso statusProcesso;

    @ApiModelProperty(value = "Número da proposta.")
    private String numeroProposta;

    @ApiModelProperty(value = "Número do orçamento.")
    private String orcamento;

    @ApiModelProperty(value = "Valor financiado.")
    private String valorFinanciado;

    @ApiModelProperty(value = "ID da concessionaria.")
    private String concessionaria;

    @ApiModelProperty(value = "Tipo de venda.")
    private String tipoVenda;

    @ApiModelProperty(value = "Nome da concessionária.")
    private String nomeConcessionaria;

    @ApiModelProperty(value = "Regional.")
    private String regional;

    @ApiModelProperty(value = "Número da conta.")
    private String conta;

    @ApiModelProperty(value = "Data da Finalização.")
    private String dataFinalizacao;

    @ApiModelProperty(value = "Nome do analista que está bloqueando processo.")
    private String nomeAnalistaBloqueio;

    @ApiModelProperty(value = "Indicação de que se o processo teve pendência.")
    private Boolean passouPorPendencia;

    @ApiModelProperty(value = "Indicação do status do prazo.")
    private StatusPrazo statusPrazo;

    public FilaTrabalhoVoResponse(){}

    public FilaTrabalhoVoResponse(ProcessoVO processoVo){
        Processo processo = processoVo.getProcesso();
        this.processoId = processo.getId();
        //this.nomeCompleto = processoVo.getNome();
        this.processoNumero = processoVo.getNumero();
        this.tipoProcesso = new TipoProcessoResponse(processo.getTipoProcesso());
        this.situacao = new SituacaoResponse(processo.getSituacao());
        this.tempoRestanteSituacao = processoVo.getHorasRestantesSituacao();
        this.tempoRestanteStatus = processoVo.getHorasRestantes();
        this.evidenciaNaoLida = processoVo.getEvidenciaNaoLida();
        this.possuiEmailNaoLido = processoVo.getPossuiEmailNaoLido();
        this.reenvioNaoLido = processoVo.getReenvioNaoLido();
        this.dataEnvio = processo.getDataEnvioAnalise();
        this.statusOcr = processo.getStatusOcr();
        this.statusProcesso = processoVo.getStatusProcesso();
        this.dataEnvio = processoVo.getProcesso().getDataEnvioAnalise();
        this.nomeAnalistaBloqueio = processoVo.getNomeAnalistaBloqueio();
        this.dataFinalizacao = DummyUtils.formatDate(processoVo.getDataFinalizacao());
        this.passouPorPendencia = processoVo.getPassouPorPendencia();
        this.statusPrazo = processoVo.getStatusPrazo();

        Usuario analista = processo.getAnalista();
        if(analista != null) {
            this.analista = new UsuarioResponse(processo.getAnalista());
        }
    }

    public String getProcessoNumero() {
        return processoNumero;
    }

    public void setProcessoNumero(String processoNumero) {
        this.processoNumero = processoNumero;
    }

    public TipoProcessoResponse getTipoProcesso() {
        return tipoProcesso;
    }

    public void setTipoProcesso(TipoProcessoResponse tipoProcesso) {
        this.tipoProcesso = tipoProcesso;
    }

    public SituacaoResponse getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoResponse situacao) {
        this.situacao = situacao;
    }

    public Date getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(Date dataEnvio) {
        this.dataEnvio = dataEnvio;
    }

    public UsuarioResponse getAnalista() {
        return analista;
    }

    public void setAnalista(UsuarioResponse analista) {
        this.analista = analista;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getTempoRestanteStatus() {
        return tempoRestanteStatus;
    }

    public void setTempoRestanteStatus(String tempoRestanteStatus) {
        this.tempoRestanteStatus = tempoRestanteStatus;
    }

    public String getTempoRestanteSituacao() {
        return tempoRestanteSituacao;
    }

    public void setTempoRestanteSituacao(String tempoRestanteSituacao) {
        this.tempoRestanteSituacao = tempoRestanteSituacao;
    }

    public Boolean getPossuiEmailNaoLido() {
        return possuiEmailNaoLido;
    }

    public void setPossuiEmailNaoLido(Boolean possuiEmailNaoLido) {
        this.possuiEmailNaoLido = possuiEmailNaoLido;
    }

    public Boolean getEvidenciaNaoLida() {
        return evidenciaNaoLida;
    }

    public void setEvidenciaNaoLida(Boolean evidenciaNaoLida) {
        this.evidenciaNaoLida = evidenciaNaoLida;
    }

    public Boolean getReenvioNaoLido() {
        return reenvioNaoLido;
    }

    public void setReenvioNaoLido(Boolean reenvioNaoLido) {
        this.reenvioNaoLido = reenvioNaoLido;
    }

    public Long getProcessoId() {
        return processoId;
    }

    public void setProcessoId(Long processoId) {
        this.processoId = processoId;
    }

    public StatusProcesso getStatusProcesso() {
        return statusProcesso;
    }

    public void setStatusProcesso(StatusProcesso statusProcesso) {
        this.statusProcesso = statusProcesso;
    }

    public String getNumeroProposta() {
        return numeroProposta;
    }

    public void setNumeroProposta(String numeroProposta) {
        this.numeroProposta = numeroProposta;
    }

    public String getOrcamento() {
        return orcamento;
    }

    public void setOrcamento(String orcamento) {
        this.orcamento = orcamento;
    }

    public String getValorFinanciado() {
        return valorFinanciado;
    }

    public void setValorFinanciado(String valorFinanciado) {
        this.valorFinanciado = valorFinanciado;
    }

    public String getConcessionaria() {
        return concessionaria;
    }

    public void setConcessionaria(String concessionaria) {
        this.concessionaria = concessionaria;
    }

    public String getTipoVenda() {
        return tipoVenda;
    }

    public void setTipoVenda(String tipoVenda) {
        this.tipoVenda = tipoVenda;
    }

    public String getNomeConcessionaria() {
        return nomeConcessionaria;
    }

    public void setNomeConcessionaria(String nomeConcessionaria) {
        this.nomeConcessionaria = nomeConcessionaria;
    }

    public String getRegional() {
        return regional;
    }

    public void setRegional(String regional) {
        this.regional = regional;
    }

    public String getConta() {
        return conta;
    }

    public void setConta(String conta) {
        this.conta = conta;
    }

    public String getDataFinalizacao() {
        return dataFinalizacao;
    }

    public void setDataFinalizacao(String dataFinalizacao) {
        this.dataFinalizacao = dataFinalizacao;
    }

    public Boolean getPassouPorPendencia() {
        return passouPorPendencia;
    }

    public void setPassouPorPendencia(Boolean passouPorPendencia) {
        this.passouPorPendencia = passouPorPendencia;
    }

    public StatusPrazo getStatusPrazo() {
        return statusPrazo;
    }

    public void setStatusPrazo(StatusPrazo statusPrazo) {
        this.statusPrazo = statusPrazo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FilaTrabalhoVoResponse)) return false;
        FilaTrabalhoVoResponse that = (FilaTrabalhoVoResponse) o;
        return Objects.equals(processoId, that.processoId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(processoId);
    }

    public StatusOcr getStatusOcr() {
        return statusOcr;
    }

    public void setStatusOcr(StatusOcr statusOcr) {
        this.statusOcr = statusOcr;
    }

    public String getNomeAnalistaBloqueio() {
        return nomeAnalistaBloqueio;
    }

    public void setNomeAnalistaBloqueio(String nomeAnalistaBloqueio) {
        this.nomeAnalistaBloqueio = nomeAnalistaBloqueio;
    }
}