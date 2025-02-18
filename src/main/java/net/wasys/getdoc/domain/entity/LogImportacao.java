package net.wasys.getdoc.domain.entity;

import net.wasys.getdoc.domain.enumeration.StatusImportacao;
import net.wasys.getdoc.domain.enumeration.TipoImportacao;

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
import javax.persistence.Transient;

@Entity(name="LOG_IMPORTACAO")
public class LogImportacao extends net.wasys.util.ddd.Entity {

	private Long id;
	private Usuario usuario;
	private TipoImportacao tipo;
	private Date data;
	private int inserts;
	private int updates;
	private int deletes;
	private int cancelados;
	private String pathArquivo;
	private String nomeArquivo;
	private long tamanhoArquivo;
	private String erro;
	private StatusImportacao status;
	private Long tempo;

	private BaseInterna baseInterna;
	private TipoProcesso tipoProcesso;

	@Id
	@Column(name="ID", unique=true, nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="USUARIO_ID")
	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="BASE_INTERNA_ID")
	public BaseInterna getBaseInterna() {
		return baseInterna;
	}

	public void setBaseInterna(BaseInterna baseInterna) {
		this.baseInterna = baseInterna;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="TIPO", length=30, nullable=false)	
	public TipoImportacao getTipo() {
		return tipo;
	}

	public void setTipo(TipoImportacao tipo) {
		this.tipo = tipo;
	}

	@Column(name="DATA", nullable=false)
	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	@Column(name="INSERTS")
	public int getInserts() {
		return inserts;
	}

	public void setInserts(int inserts) {
		this.inserts = inserts;
	}

	@Column(name="UPDATES")
	public int getUpdates() {
		return updates;
	}

	public void setUpdates(int updates) {
		this.updates = updates;
	}

	@Column(name="DELETES")
	public int getDeletes() {
		return deletes;
	}

	public void setDeletes(int deletes) {
		this.deletes = deletes;
	}

	@Column(name="CANCELADOS")
	public int getCancelados() {
		return cancelados;
	}

	public void setCancelados(int cancelados) {
		this.cancelados = cancelados;
	}

	@Column(name="PATH_ARQUIVO", length=200)
	public String getPathArquivo() {
		return pathArquivo;
	}

	public void setPathArquivo(String pathArquivo) {
		this.pathArquivo = pathArquivo;
	}

	@Column(name="NOME_ARQUIVO", length=100)
	public String getNomeArquivo() {
		return nomeArquivo;
	}

	public void setNomeArquivo(String nomeArquivo) {
		this.nomeArquivo = nomeArquivo;
	}

	@Column(name="TAMANHO_ARQUIVO")
	public long getTamanhoArquivo() {
		return tamanhoArquivo;
	}

	public void setTamanhoArquivo(long tamanhoArquivo) {
		this.tamanhoArquivo = tamanhoArquivo;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="TIPO_PROCESSO_ID")
	public TipoProcesso getTipoProcesso() {
		return tipoProcesso;
	}

	public void setTipoProcesso(TipoProcesso tipoProcesso) {
		this.tipoProcesso = tipoProcesso;
	}

	@Column(name = "ERRO_IMPORTACAO")
	public String getErro() {
		return erro;
	}

	public void setErro(String erro) {
		this.erro = erro;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="STATUS", length=30)
	public StatusImportacao getStatus() {
		return status;
	}

	public void setStatus(StatusImportacao status) {
		this.status = status;
	}

	@Column(name="TEMPO")
	public Long getTempo() {
		return tempo;
	}

	public void setTempo(Long tempo) {
		this.tempo = tempo;
	}

	@Transient
	public boolean isStatusErro() {
		return StatusImportacao.ERRO.equals(status);
	}

	@Transient
	public boolean isStatusProcessando() {
		return StatusImportacao.PROCESSANDO.equals(status);
	}

	@Transient
	public String criarPath(String basePath, String nomeArquivo) {

		String separador = System.getProperty("file.separator");

		StringBuilder path = new StringBuilder(basePath);

		Long idLog = getId();

		if(TipoImportacao.BASE_INTERNA.equals(tipo)) {
			path
			.append(baseInterna.getId())
			.append("_")
			.append(baseInterna.getNome())
			.append(separador)
			.append(idLog)
			.append("_")
			.append(nomeArquivo);
		}

		return path.toString();
	}
}