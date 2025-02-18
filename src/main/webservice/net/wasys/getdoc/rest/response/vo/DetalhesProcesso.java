package net.wasys.getdoc.rest.response.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.entity.RelatorioGeral;
import net.wasys.getdoc.rest.request.vo.SuperVo;

import java.math.BigDecimal;
import java.util.Date;

@ApiModel(value = "DetalhesProcesso")
public class DetalhesProcesso extends SuperVo {

    @ApiModelProperty(notes = "Permissões do usuários.")
    private PermissaoResponse permissao;

    @ApiModelProperty(notes = "Flag que indica se o acesso ao processo está bloqueado.")
    private boolean acessoBloqueado;

    @ApiModelProperty(notes = "Flag para exibir alerta de rascunho.")
    private boolean exibirAlertaRascunho;

    @ApiModelProperty(notes = "Flag que indica se o processo possui regras.")
    private boolean possuiRegras;

    @ApiModelProperty(notes = "Quantidade de processos abertos.")
    private int quantidadeProcessosAbertos;

    @ApiModelProperty(notes = "Quantidade de processos fechados.")
    private int quantidadeProcessosFechados;

    @ApiModelProperty(notes = "Tempo restante do status.")
    private String tempoRestanteStatus;

    @ApiModelProperty(notes = "Tempo restante situação.")
    private String tempoRestanteSituacao;

    @ApiModelProperty(notes = "Registro ID.")
    private Long id;

    @ApiModelProperty(notes = "Processo ID.")
    private Long processoId;

    @ApiModelProperty(notes = "Data da criação.")
    private Date dataCriacao;

    @ApiModelProperty(notes = "Data de envio para análise.")
    private Date dataEnvioAnalise;

    @ApiModelProperty(notes = "Data que entrou em análise.")
    private Date dataEmAnalise;

    @ApiModelProperty(notes = "Data que entrou em acompanhamento.")
    private Date dataEmAcompanhamento;

    @ApiModelProperty(notes = "Data em que foi finalizado.")
    private Date dataFinalizacao;

    @ApiModelProperty(notes = "Data da última tratativa.")
    private Date dataUltimaTratativa;

    @ApiModelProperty(notes = "Status do processo.")
    private StatusProcessoResponse status;

    @ApiModelProperty(notes = "Nível de prioridade.")
    private Integer nivelPrioridade;

    @ApiModelProperty(notes = "Prazo em horas para análise.")
    private BigDecimal horasPrazoAnalise;

    @ApiModelProperty(notes = "Data limite para análise.")
    private Date prazoLimiteAnalise;

    @ApiModelProperty(notes = "Data limite para em acompanhamento.")
    private Date prazoLimiteEmAcompanhamento;

    @ApiModelProperty(notes = "Data de finalização da análise.")
    private Date dataFinalizacaoAnalise;

    @ApiModelProperty(notes = "Número.")
    private String numero;

    @ApiModelProperty(notes = "Temo que ficou em rascunho.")
    private BigDecimal tempoRascunho;

    @ApiModelProperty(notes = "Quantidade de vezes que ficou aguardando análise.")
    private Integer vezesAguardandoAnalise;

    @ApiModelProperty(notes = "Temo aguardando análise.")
    private BigDecimal tempoAguardandoAnalise;

    @ApiModelProperty(notes = "Quantidade de vezes que ficou pendente.")
    private Integer vezesPendente;

    @ApiModelProperty(notes = "Tempo pendente.")
    private BigDecimal tempoPendente;

    @ApiModelProperty(notes = "Quantidade de vezes que ficou em análise.")
    private Integer vezesEmAnalise;

    @ApiModelProperty(notes = "Tempo em análise.")
    private BigDecimal tempoEmAnalise;

    @ApiModelProperty(notes = "Tempo até finalizar.")
    private BigDecimal tempoAteFinalizacao;

    @ApiModelProperty(notes = "Tempo até finalizar análise.")
    private BigDecimal tempoAteFinalizacaoAnalise;

    @ApiModelProperty(notes = "Tempo SLA criação.")
    private BigDecimal tempoSlaCriacao;

    @ApiModelProperty(notes = "Tempo SLA tratativa.")
    private BigDecimal tempoSlaTratativa;

    @ApiModelProperty(notes = "Hora de expediente.")
    private BigDecimal horasExpediente;

    @ApiModelProperty(notes = "Situação do processo.")
    private SituacaoResponse situacao;

    @ApiModelProperty(notes = "Autor.")
    private UsuarioResponse autor;

    @ApiModelProperty(notes = "Analista.")
    private UsuarioResponse analista;

    @ApiModelProperty(notes = "Tipo do processo.")
    private TipoProcessoResponse tipoProcesso;

    @ApiModelProperty(notes = "Nome do analista que está bloqueando processo.")
    private String nomeAnalistaBloqueio;

    public void parserProcesso(Processo processo) {
        this.processoId = processo.getId();
        this.status = StatusProcessoResponse.from(processo.getStatus());
        this.numero = String.valueOf(processo.getId());
        this.situacao = new SituacaoResponse(processo.getSituacao());
        this.autor = processo.getAutor() != null ? new UsuarioResponse(processo.getAutor()) : null;
        this.analista = processo.getAnalista() != null ? new UsuarioResponse(processo.getAnalista()) : null;
        this.tipoProcesso = new TipoProcessoResponse(processo.getTipoProcesso());
        this.nivelPrioridade = processo.getNivelPrioridade();
    }

    public void parserProcesso(RelatorioGeral relatorioGeral) {
        this.id = relatorioGeral.getId();
        this.dataCriacao = relatorioGeral.getDataCriacao();
        this.dataEnvioAnalise = relatorioGeral.getDataEnvioAnalise();
        this.dataEmAnalise = relatorioGeral.getDataEmAnalise();
        this.dataEmAcompanhamento = relatorioGeral.getDataEmAcompanhamento();
        this.dataFinalizacao = relatorioGeral.getDataFinalizacao();
        this.dataUltimaTratativa = relatorioGeral.getDataUltimaTratativa();
        this.horasPrazoAnalise = relatorioGeral.getHorasPrazoAnalise();
        this.prazoLimiteAnalise = relatorioGeral.getPrazoLimiteAnalise();
        this.prazoLimiteEmAcompanhamento = relatorioGeral.getPrazoLimiteEmAcompanhamento();
        this.dataFinalizacaoAnalise = relatorioGeral.getDataFinalizacaoAnalise();
        this.tempoRascunho = relatorioGeral.getTempoRascunho();
        this.vezesAguardandoAnalise = relatorioGeral.getVezesAguardandoAnalise();
        this.tempoAguardandoAnalise = relatorioGeral.getTempoAguardandoAnalise();
        this.vezesPendente = relatorioGeral.getVezesPendente();
        this.tempoPendente = relatorioGeral.getTempoPendente();
        this.vezesEmAnalise = relatorioGeral.getVezesEmAnalise();
        this.tempoEmAnalise = relatorioGeral.getTempoEmAnalise();
        this.tempoAteFinalizacao = relatorioGeral.getTempoAteFinalizacao();
        this.tempoAteFinalizacaoAnalise = relatorioGeral.getTempoAteFinalizacaoAnalise();
        this.tempoSlaCriacao = relatorioGeral.getTempoSlaCriacao();
        this.tempoSlaTratativa = relatorioGeral.getTempoSlaTratativa();
        this.horasExpediente = relatorioGeral.getHorasExpediente();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isPossuiRegras() {
        return possuiRegras;
    }

    public void setPossuiRegras(boolean possuiRegras) {
        this.possuiRegras = possuiRegras;
    }

    public Long getProcessoId() {
        return processoId;
    }

    public void setProcessoId(Long processoId) {
        this.processoId = processoId;
    }

    public boolean isExibirAlertaRascunho() {
        return exibirAlertaRascunho;
    }

    public void setExibirAlertaRascunho(boolean exibirAlertaRascunho) {
        this.exibirAlertaRascunho = exibirAlertaRascunho;
    }

    public Date getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(Date dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public Date getDataEnvioAnalise() {
        return dataEnvioAnalise;
    }

    public void setDataEnvioAnalise(Date dataEnvioAnalise) {
        this.dataEnvioAnalise = dataEnvioAnalise;
    }

    public Date getDataEmAnalise() {
        return dataEmAnalise;
    }

    public void setDataEmAnalise(Date dataEmAnalise) {
        this.dataEmAnalise = dataEmAnalise;
    }

    public Date getDataEmAcompanhamento() {
        return dataEmAcompanhamento;
    }

    public void setDataEmAcompanhamento(Date dataEmAcompanhamento) {
        this.dataEmAcompanhamento = dataEmAcompanhamento;
    }

    public Date getDataFinalizacao() {
        return dataFinalizacao;
    }

    public void setDataFinalizacao(Date dataFinalizacao) {
        this.dataFinalizacao = dataFinalizacao;
    }

    public Date getDataUltimaTratativa() {
        return dataUltimaTratativa;
    }

    public void setDataUltimaTratativa(Date dataUltimaTratativa) {
        this.dataUltimaTratativa = dataUltimaTratativa;
    }

    public StatusProcessoResponse getStatus() {
        return status;
    }

    public void setStatus(StatusProcessoResponse status) {
        this.status = status;
    }

    public Integer getNivelPrioridade() {
        return nivelPrioridade;
    }

    public void setNivelPrioridade(Integer nivelPrioridade) {
        this.nivelPrioridade = nivelPrioridade;
    }

    public BigDecimal getHorasPrazoAnalise() {
        return horasPrazoAnalise;
    }

    public void setHorasPrazoAnalise(BigDecimal horasPrazoAnalise) {
        this.horasPrazoAnalise = horasPrazoAnalise;
    }

    public Date getPrazoLimiteAnalise() {
        return prazoLimiteAnalise;
    }

    public void setPrazoLimiteAnalise(Date prazoLimiteAnalise) {
        this.prazoLimiteAnalise = prazoLimiteAnalise;
    }

    public Date getPrazoLimiteEmAcompanhamento() {
        return prazoLimiteEmAcompanhamento;
    }

    public void setPrazoLimiteEmAcompanhamento(Date prazoLimiteEmAcompanhamento) {
        this.prazoLimiteEmAcompanhamento = prazoLimiteEmAcompanhamento;
    }

    public Date getDataFinalizacaoAnalise() {
        return dataFinalizacaoAnalise;
    }

    public void setDataFinalizacaoAnalise(Date dataFinalizacaoAnalise) {
        this.dataFinalizacaoAnalise = dataFinalizacaoAnalise;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public BigDecimal getTempoRascunho() {
        return tempoRascunho;
    }

    public void setTempoRascunho(BigDecimal tempoRascunho) {
        this.tempoRascunho = tempoRascunho;
    }

    public Integer getVezesAguardandoAnalise() {
        return vezesAguardandoAnalise;
    }

    public void setVezesAguardandoAnalise(Integer vezesAguardandoAnalise) {
        this.vezesAguardandoAnalise = vezesAguardandoAnalise;
    }

    public BigDecimal getTempoAguardandoAnalise() {
        return tempoAguardandoAnalise;
    }

    public void setTempoAguardandoAnalise(BigDecimal tempoAguardandoAnalise) {
        this.tempoAguardandoAnalise = tempoAguardandoAnalise;
    }

    public Integer getVezesPendente() {
        return vezesPendente;
    }

    public void setVezesPendente(Integer vezesPendente) {
        this.vezesPendente = vezesPendente;
    }

    public BigDecimal getTempoPendente() {
        return tempoPendente;
    }

    public void setTempoPendente(BigDecimal tempoPendente) {
        this.tempoPendente = tempoPendente;
    }

    public Integer getVezesEmAnalise() {
        return vezesEmAnalise;
    }

    public void setVezesEmAnalise(Integer vezesEmAnalise) {
        this.vezesEmAnalise = vezesEmAnalise;
    }

    public BigDecimal getTempoEmAnalise() {
        return tempoEmAnalise;
    }

    public void setTempoEmAnalise(BigDecimal tempoEmAnalise) {
        this.tempoEmAnalise = tempoEmAnalise;
    }

    public BigDecimal getTempoAteFinalizacao() {
        return tempoAteFinalizacao;
    }

    public void setTempoAteFinalizacao(BigDecimal tempoAteFinalizacao) {
        this.tempoAteFinalizacao = tempoAteFinalizacao;
    }

    public BigDecimal getTempoAteFinalizacaoAnalise() {
        return tempoAteFinalizacaoAnalise;
    }

    public void setTempoAteFinalizacaoAnalise(BigDecimal tempoAteFinalizacaoAnalise) {
        this.tempoAteFinalizacaoAnalise = tempoAteFinalizacaoAnalise;
    }

    public BigDecimal getTempoSlaCriacao() {
        return tempoSlaCriacao;
    }

    public void setTempoSlaCriacao(BigDecimal tempoSlaCriacao) {
        this.tempoSlaCriacao = tempoSlaCriacao;
    }

    public BigDecimal getTempoSlaTratativa() {
        return tempoSlaTratativa;
    }

    public void setTempoSlaTratativa(BigDecimal tempoSlaTratativa) {
        this.tempoSlaTratativa = tempoSlaTratativa;
    }

    public BigDecimal getHorasExpediente() {
        return horasExpediente;
    }

    public void setHorasExpediente(BigDecimal horasExpediente) {
        this.horasExpediente = horasExpediente;
    }

    public SituacaoResponse getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoResponse situacao) {
        this.situacao = situacao;
    }

    public UsuarioResponse getAutor() {
        return autor;
    }

    public void setAutor(UsuarioResponse autor) {
        this.autor = autor;
    }

    public UsuarioResponse getAnalista() {
        return analista;
    }

    public void setAnalista(UsuarioResponse analista) {
        this.analista = analista;
    }

    public TipoProcessoResponse getTipoProcesso() {
        return tipoProcesso;
    }

    public void setTipoProcesso(TipoProcessoResponse tipoProcesso) {
        this.tipoProcesso = tipoProcesso;
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

    public int getQuantidadeProcessosAbertos() {
        return quantidadeProcessosAbertos;
    }

    public void setQuantidadeProcessosAbertos(int quantidadeProcessosAbertos) {
        this.quantidadeProcessosAbertos = quantidadeProcessosAbertos;
    }

    public int getQuantidadeProcessosFechados() {
        return quantidadeProcessosFechados;
    }

    public void setQuantidadeProcessosFechados(int quantidadeProcessosFechados) {
        this.quantidadeProcessosFechados = quantidadeProcessosFechados;
    }

    public boolean isAcessoBloqueado() {
        return acessoBloqueado;
    }

    public void setAcessoBloqueado(boolean acessoBloqueado) {
        this.acessoBloqueado = acessoBloqueado;
    }

    public PermissaoResponse getPermissao() {
        return permissao;
    }

    public void setPermissao(PermissaoResponse permissao) {
        this.permissao = permissao;
    }

    public String getNomeAnalistaBloqueio() {
        return nomeAnalistaBloqueio;
    }

    public void setNomeAnalistaBloqueio(String nomeAnalistaBloqueio) {
        this.nomeAnalistaBloqueio = nomeAnalistaBloqueio;
    }
}