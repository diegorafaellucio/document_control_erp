package net.wasys.getdoc.domain.entity;

import java.io.File;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import net.wasys.getdoc.domain.enumeration.StatusLogOcr;

@Entity(name="LOG_OCR")
public class LogOcr extends net.wasys.util.ddd.Entity {

	private Long id;
	private Date data;
	private Long processoId;
	private Long documentoId;
	private Long imagemId;
	private String caminhoImagem;
	private String extensaoImagem;
	private String resultado;
	private String mensagemErro;
	private String stackTrace;
	private StatusLogOcr statusOcr;
	private Long tempoProcessamento;
	private Date fimProcessamento;
	private Date inicioProcessamento;
	private Long agendamentoId;

	private Usuario usuario;
	private ModeloOcr modeloOcr;

	@Id
	@Override
	@Column(name="ID", unique=true, nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setData(Date data) {
		this.data = data;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA", nullable=false)
	public Date getData() {
		return data;
	}

	@Column(name="PROCESSO_ID", nullable=false)
	public Long getProcessoId() {
		return processoId;
	}

	public void setProcessoId(Long processoId) {
		this.processoId = processoId;
	}

	@Column(name="DOCUMENTO_ID", nullable=false)
	public Long getDocumentoId() {
		return documentoId;
	}

	public void setDocumentoId(Long documentoId) {
		this.documentoId = documentoId;
	}

	@Column(name="IMAGEM_ID", nullable=false)
	public Long getImagemId() {
		return imagemId;
	}

	public void setImagemId(Long imagemId) {
		this.imagemId = imagemId;
	}

	@Column(name="CAMINHO_IMAGEM", nullable=false)
	public String getCaminhoImagem() {
		return caminhoImagem;
	}

	public void setCaminhoImagem(String caminhoImagem) {
		this.caminhoImagem = caminhoImagem;
	}

	@Column(name="EXTENSAO_IMAGEM", nullable=false)
	public String getExtensaoImagem() {
		return extensaoImagem;
	}

	public void setExtensaoImagem(String extensaoImagem) {
		this.extensaoImagem = extensaoImagem;
	}

	@Column(name="STATUS_OCR")
	@Enumerated(EnumType.STRING)
	public StatusLogOcr getStatusOcr() {
		return statusOcr;
	}

	public void setStatusOcr(StatusLogOcr statusOcr) {
		this.statusOcr = statusOcr;
	}

	@Column(name="RESULTADO")
	public String getResultado() {
		return resultado;
	}

	public void setResultado(String resultado) {
		this.resultado = resultado;
	}

	@Column(name="MENSAGEM_ERRO", length=1000)
	public String getMensagemErro() {
		return mensagemErro;
	}

	public void setMensagemErro(String mensagemErro) {
		this.mensagemErro = mensagemErro;
	}

	@Column(name="STACK_TRACE", length=1000)
	public String getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="USUARIO_ID")
	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	@Column(name="INICIO_PROCESSAMENTO")
	public Date getInicioProcessamento() {
		return inicioProcessamento;
	}

	public void setInicioProcessamento(Date inicioProcessamento) {
		this.inicioProcessamento = inicioProcessamento;
	}

	@Column(name="FIM_PROCESSAMENTO")
	public Date getFimProcessamento() {
		return fimProcessamento;
	}

	public void setFimProcessamento(Date fimProcessamento) {
		this.fimProcessamento = fimProcessamento;
	}

	@Column(name="TEMPO_PROCESSAMENTO")
	public Long getTempoProcessamento() {
		return tempoProcessamento;
	}

	public void setTempoProcessamento(Long tempoProcessamento) {
		this.tempoProcessamento = tempoProcessamento;
	}

	@Column(name="AGENDAMENTO_ID")
	public Long getAgendamentoId() {
		return agendamentoId;
	}

	public void setAgendamentoId(Long agendamentoId) {
		this.agendamentoId = agendamentoId;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="MODELO_OCR_ID")
	public ModeloOcr getModeloOcr() {
		return modeloOcr;
	}

	public void setModeloOcr(ModeloOcr modeloOcr) {
		this.modeloOcr = modeloOcr;
	}

	public String criaCaminhoImagemOcr(String imagemOcrPath) {

		String processoIdStr = processoId.toString();
		int length = processoIdStr.length();
		String agrupador = processoIdStr.substring(length - 3, length);

		StringBuilder caminho = new StringBuilder();

		caminho.append(imagemOcrPath);
		caminho.append(agrupador).append(File.separator);
		caminho.append(documentoId).append("_").append(imagemId).append("_").append(id).append(".").append(extensaoImagem);

		String caminhoStr = caminho.toString();
		return caminhoStr;
	}
}
