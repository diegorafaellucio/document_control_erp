package net.wasys.getdoc.domain.entity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity(name="TEXTO_PADRAO")
public class TextoPadrao extends net.wasys.util.ddd.Entity {

	private Long id;
	private String nome;
	private String texto;
	private boolean ativo = true;
	private boolean fixo = false;
	private String permissoesDeUso;

	private Set<TextoPadraoTipoProcesso> tiposProcessos = new HashSet<>(0);

	public static final String REGISTRO_EVIDENCIA = "Registro de Evidência";
	public static final String SOLICITACAO_AREA = "Solicitação à Área";
	public static final String ENVIO_EMAIL = "Envio de E-mail";

	public static final Long NOTIFICACAO_CANDIDATO_SISPROUNI_ID = (101L);
	public static final Long NOTIFICACAO_CANDIDATO_SISFIES_ID = (107L);
	public static final Long NOTIFICACAO_PENDENCIA_CANDIDATO_SISPROUNI_ID = (108L);
	public static final Long NOTIFICACAO_CANDIDATO_RASCUNHO_ID = (111L);
	public static final Long NOTIFICACAO_CANDIDATO_REPROVACAO_ID = (112L);
	public static final Long NOTIFICACAO_CANDIDATO_APROVACAO_ID = (113L);

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

	@Column(name="NOME", nullable=false)
	public String getNome() {
		return this.nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Column(name="ATIVO", nullable=false)
	public boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}

	@Column(name="FIXO", nullable=false)
	public boolean getFixo() {
		return fixo;
	}

	public void setFixo(boolean fixo) {
		this.fixo = fixo;
	}

	@Column(name="TEXTO", nullable=false)
	public String getTexto() {
		return texto;
	}

	public void setTexto(String texto) {
		this.texto = texto;
	}

	@Column(name="PERMISSOES_DE_USO")
	public String getPermissoesDeUso() {
		return permissoesDeUso;
	}

	public void setPermissoesDeUso(String permissoesDeUso) {
		this.permissoesDeUso = permissoesDeUso;
	}

	@OneToMany(fetch=FetchType.LAZY, mappedBy="textoPadrao", cascade=CascadeType.ALL, orphanRemoval=true)
	public Set<TextoPadraoTipoProcesso> getTiposProcessos() {
		return tiposProcessos;
	}

	public void setTiposProcessos(Set<TextoPadraoTipoProcesso> tiposProcessos) {
		this.tiposProcessos = tiposProcessos;
	}
}
