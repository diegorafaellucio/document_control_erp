package net.wasys.getdoc.domain.entity;

import net.wasys.getdoc.domain.enumeration.Resposta;

import java.util.ArrayList;
import java.util.List;

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
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

@Entity
public class Ajuda extends net.wasys.util.ddd.Entity {

	private Long id;
	private Tipo tipo;
	private Resposta resposta;
	private Objetivo objetivo;
	private boolean pendencia;
	private TipoDocumento tipoDocumento;
	private String texto;
	private String solucao;
	private int ordem;
	
	private Ajuda superior;
	private TipoProcesso tipoProcesso;

	private List<Ajuda> inferiores = new ArrayList<>();

	public enum Tipo {
		DICA,
		PERGUNTA
	}

	public enum Objetivo {
		REQUISICAO,
		SOLICITACAO
	}

	public Ajuda() {

	}

	public Ajuda(Long id) {
		this.id = id;
	}

	public String breakTextLine(String text) {
		String[] split = text.split("\\n");
		StringBuilder builder = new StringBuilder();
		for (String string : split) {
			builder.append("<p>").append(string).append("</p>");
		}
		return builder.toString();
	}

	public void add(Ajuda ajuda) {
		if (inferiores == null) {
			inferiores = new ArrayList<>();
		}
		inferiores.add(ajuda);
	}

	@Id
	@Column(name="id", unique=true, nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="tipo", length=8, nullable=false)
	public Tipo getTipo() {
		return tipo;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="resposta", length=3, nullable=false)
	public Resposta getResposta() {
		return resposta;
	}

	@Enumerated(EnumType.STRING)
	@Column(name="objetivo", length=12, nullable=false)
	public Objetivo getObjetivo() {
		return objetivo;
	}

	@Column(name="texto", length=255, nullable=false)
	public String getTexto() {
		return texto;
	}

	@Column(name="solucao", length=500)
	public String getSolucao() {
		return solucao;
	}

	@Column(name="ordem", nullable=false)
	public int getOrdem() {
		return ordem;
	}

	public void setOrdem(int ordem) {
		this.ordem = ordem;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="superior_id")
	public Ajuda getSuperior() {
		return superior;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="tipo_processo_id", nullable=false)
	public TipoProcesso getTipoProcesso() {
		return tipoProcesso;
	}

	@OrderBy("resposta")
	@OneToMany(mappedBy="superior", fetch=FetchType.LAZY)
	public List<Ajuda> getInferiores() {
		return inferiores;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setTipo(Tipo tipo) {
		this.tipo = tipo;
	}

	public void setResposta(Resposta resposta) {
		this.resposta = resposta;
	}

	public void setObjetivo(Objetivo objetivo) {
		this.objetivo = objetivo;
	}

	public void setTexto(String texto) {
		this.texto = texto;
	}

	public void setSuperior(Ajuda superior) {
		this.superior = superior;
	}
	
	public void setSolucao(String solucao) {
		this.solucao = solucao;
	}

	public void setTipoProcesso(TipoProcesso tipoProcesso) {
		this.tipoProcesso = tipoProcesso;
	}

	public void setInferiores(List<Ajuda> inferiores) {
		this.inferiores = inferiores;
	}

	@Column(name="pendencia")
	public boolean isPendencia() {
		return pendencia;
	}

	public void setPendencia(boolean pendencia) {
		this.pendencia = pendencia;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="tipo_documento_id")
	public TipoDocumento getTipoDocumento() {
		return tipoDocumento;
	}

	public void setTipoDocumento(TipoDocumento tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}
}