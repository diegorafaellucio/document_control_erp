package net.wasys.getdoc.domain.entity;

import java.util.Date;

import javax.persistence.*;

import net.wasys.getdoc.domain.enumeration.StatusConsultaExterna;
import net.wasys.getdoc.domain.enumeration.TipoConsultaExterna;

@Entity(name="CONSULTA_EXTERNA")
public class ConsultaExterna extends net.wasys.util.ddd.Entity {

	private Long id;
	private TipoConsultaExterna tipo;
	private Date data;
	private String parametros;
	private String resultado;
	private String stackTrace;
	private StatusConsultaExterna status;
	private String mensagem;
	private Long tempoExecucao;

	private Usuario usuario;

	@Id
	@Override
	@Column(name="ID", unique=true, nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="TIPO", length=20, nullable=false)
	public TipoConsultaExterna getTipo() {
		return tipo;
	}

	public void setTipo(TipoConsultaExterna tipo) {
		this.tipo = tipo;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA", nullable=false)
	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	@Column(name="PARAMETROS", length=1000)
	public String getParametros() {
		return parametros;
	}

	public void setParametros(String parametros) {
		this.parametros = parametros;
	}

	@Column(name="RESULTADO")
	public String getResultado() {
		return resultado;
	}

	public void setResultado(String resultado) {
		this.resultado = resultado;
	}

	@Column(name="STACK_TRACE", length=10000)
	public String getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="STATUS", length=20)
	public StatusConsultaExterna getStatus() {
		return status;
	}

	public void setStatus(StatusConsultaExterna status) {
		this.status = status;
	}

	@Column(name="MENSAGEM")
	public String getMensagem() {
		return mensagem;
	}

	public void setMensagem(String mensagem) {
		this.mensagem = mensagem;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="USUARIO_ID")
	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	@Column(name = "TEMPO_EXECUCAO")
	public Long getTempoExecucao() {
		return tempoExecucao;
	}

	public void setTempoExecucao(Long tempoExecucao) {
		this.tempoExecucao = tempoExecucao;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "#" + id + "{tipo:" + tipo + ", data:" + data + ", parametros:" + parametros +
				", resultado:" + resultado + ", stackTrace:" + stackTrace + ", status:" + status + ", tempoExecucao:" + tempoExecucao + "}";
	}
}
