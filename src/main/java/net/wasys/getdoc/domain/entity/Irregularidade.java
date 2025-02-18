package net.wasys.getdoc.domain.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints=@UniqueConstraint(columnNames="NOME"))
public class Irregularidade extends net.wasys.util.ddd.Entity {

	public static final Long DEVOLVIDO_PEDIDO_ATENDENTE_ID = 109L;
	public static final Long DOCUMENTO_FALTANTE_ID = 111L;
	public static final Long DOCUMENTO_ILEGIVEL_ID = 101L;
	public static final Long DOCUMENTACAO_INCOMPLETA_ID = 110L;
	public static final Long DOCUMENTO_DIVERGENTE_ID = 102L;
	public static final Long DOCUMENTO_VENCIDO_ID = 106L;
	public static final Long PROPOSTA_VENCIDA_ID = 112L;

	private Long id;
	private String nome;
	private boolean ativa = true;
	private boolean irregularidadePastaAmarela = false;

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

	@Column(name="ATIVA", nullable=false)
	public boolean getAtiva() {
		return ativa;
	}

	public void setAtiva(boolean ativa) {
		this.ativa = ativa;
	}

	@Column(name="IRREGULARIDADE_PASTA_AMARELA", nullable=false)
	public boolean getIrregularidadePastaAmarela() {
		return irregularidadePastaAmarela;
	}

	public void setIrregularidadePastaAmarela(boolean irregularidadePastaAmarela) {
		this.irregularidadePastaAmarela = irregularidadePastaAmarela;
	}
}
