package net.wasys.getdoc.domain.entity;

import net.wasys.getdoc.domain.enumeration.TipoTipificacao;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity(name="RELATORIO_TIPIFICACAO")
public class RelatorioTipificacao extends net.wasys.util.ddd.Entity {

	private Long id;
	private Documento documento;
	private TipoTipificacao tipoTipificacao;
	private String resultado;
	private Long tempo;
	private Date data;

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

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="DOCUMENTO_ID", nullable=false)
	public Documento getDocumento() {
		return documento;
	}

	public void setDocumento(Documento documento) {
		this.documento = documento;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA")
	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	@Column(name="RESULTADO")
	public String getResultado() {
		return resultado;
	}

	public void setResultado(String resultado) {
		this.resultado = resultado;
	}

	@Column(name="TEMPO")
	public Long getTempo() {
		return tempo;
	}

	public void setTempo(Long tempo) {
		this.tempo = tempo;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="TIPO_TIPIFICACAO", length=20)
	public TipoTipificacao getTipoTipificacao() {
		return tipoTipificacao;
	}

	public void setTipoTipificacao(TipoTipificacao tipoTipificacao) {
		this.tipoTipificacao = tipoTipificacao;
	}
}
