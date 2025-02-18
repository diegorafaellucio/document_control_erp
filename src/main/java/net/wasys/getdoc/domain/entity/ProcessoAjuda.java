package net.wasys.getdoc.domain.entity;

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

import net.wasys.getdoc.domain.entity.Ajuda.Objetivo;
import net.wasys.getdoc.domain.entity.Ajuda.Tipo;
import net.wasys.getdoc.domain.enumeration.Resposta;

@Entity(name="processo_ajuda")
public class ProcessoAjuda extends net.wasys.util.ddd.Entity {

	private Long id;
	private Tipo tipo;
	private Resposta resposta;
	private ProcessoAjuda proximo;
	private Objetivo objetivo;
	private TipoDocumento tipoDocumento;
	private boolean pendencia;
	private String solucao;
	private boolean ativo = true;
	private String texto;
	private boolean marcado = false;
	private int ordem;

	private ProcessoAjuda superior;
	private Processo processo;

	private List<ProcessoAjuda> inferiores = new ArrayList<>();

	public ProcessoAjuda() {

	}

	public ProcessoAjuda(Ajuda ajuda, Processo processo) {
		copy(this, ajuda);
		this.processo = processo;
		List<Ajuda> inferiores2 = ajuda.getInferiores();
		for (Ajuda a : inferiores2) {
			ProcessoAjuda pa = new ProcessoAjuda(a, processo);
			pa.setSuperior(this);
			inferiores.add(pa);
		}
	}

	private void copy(ProcessoAjuda pa, Ajuda ajuda) {
		pa.setTipo(ajuda.getTipo());
		pa.setTexto(ajuda.getTexto());
		pa.setObjetivo(ajuda.getObjetivo());
		pa.setTipo(ajuda.getTipo());
		pa.setResposta(ajuda.getResposta());
		pa.setSolucao(ajuda.getSolucao());
		pa.setPendencia(ajuda.isPendencia());
		pa.setOrdem(ajuda.getOrdem());
		pa.setTipoDocumento(ajuda.getTipoDocumento());
	}

	public ProcessoAjuda(Long id) {
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

	public void add(ProcessoAjuda ajuda) {
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

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="superior_id")
	public ProcessoAjuda getSuperior() {
		return superior;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="processo_id", nullable=false)
	public Processo getProcesso() {
		return processo;
	}

	@OneToMany(mappedBy="superior", fetch=FetchType.LAZY)
	public List<ProcessoAjuda> getInferiores() {
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

	public void setSuperior(ProcessoAjuda superior) {
		this.superior = superior;
	}

	public void setProcesso(Processo processo) {
		this.processo = processo;
	}

	public void setInferiores(List<ProcessoAjuda> inferiores) {
		this.inferiores = inferiores;
	}

	@Column(name="marcado")
	public boolean isMarcado() {
		return marcado;
	}

	public void setMarcado(boolean marcado) {
		this.marcado = marcado;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="proximo_id")	
	public ProcessoAjuda getProximo() {
		return proximo;
	}

	public void setProximo(ProcessoAjuda proximo) {
		this.proximo = proximo;
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

	@Column(name="ativo")
	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	@Column(name="solucao", length=500)
	public String getSolucao() {
		return solucao;
	}

	public void setSolucao(String solucao) {
		this.solucao = solucao;
	}

	@Column(name="ordem", nullable=false)
	public int getOrdem() {
		return ordem;
	}

	public void setOrdem(int ordem) {
		this.ordem = ordem;
	}
}