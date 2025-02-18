package net.wasys.getdoc.domain.entity;

import net.wasys.getdoc.domain.enumeration.StatusAtendimento;

import javax.persistence.*;
import java.math.BigInteger;

@Entity
@Table(name = "STATUS_LABORAL" ,  uniqueConstraints=@UniqueConstraint(columnNames="NOME"))
public class StatusLaboral extends net.wasys.util.ddd.Entity {

	public static final Long PAUSA_SISTEMA_ID = 0l;

	private Long id;
	private String nome;
	private StatusAtendimento statusAtendimento;
	private boolean ativa = true;
	private boolean fixo = false;
	private BigInteger prazoEstouroPermitido;
	private BigInteger duracaoPadrao;

	@Id
	@Override
	@Column(name="ID", nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name="NOME", nullable=false, length=120)
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

	@Enumerated(EnumType.STRING)
	@Column(name="STATUS_ATENDIMENTO")
	public StatusAtendimento getStatusAtendimento() {
		return statusAtendimento;
	}

	public void setStatusAtendimento(StatusAtendimento statusAtendimento) {
		this.statusAtendimento = statusAtendimento;
	}

	@Column(name = "PRAZO_ESTOURO_PERMITIDO")
	public BigInteger getPrazoEstouroPermitido() {
		return prazoEstouroPermitido;
	}

	public void setPrazoEstouroPermitido(BigInteger prazoEstouroPermitido) {
		this.prazoEstouroPermitido = prazoEstouroPermitido;
	}

	@Column(name = "FIXO")
	public boolean isFixo() {
		return fixo;
	}

	public void setFixo(boolean fixo) {
		this.fixo = fixo;
	}

	@Column(name = "DURACAO_PADRAO")
	public BigInteger getDuracaoPadrao() {
		return duracaoPadrao;
	}

	public void setDuracaoPadrao(BigInteger duracao) {
		this.duracaoPadrao = duracao;
	}
}