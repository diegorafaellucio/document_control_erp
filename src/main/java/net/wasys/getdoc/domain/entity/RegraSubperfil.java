package net.wasys.getdoc.domain.entity;

import javax.persistence.*;

@Entity(name="REGRA_SUBPERFIL")
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"REGRA_ID"}))
public class RegraSubperfil extends net.wasys.util.ddd.Entity {

	private Long id;
    private Subperfil subperfil;

	private Regra regra;

    public RegraSubperfil() {
    }

    public RegraSubperfil(Regra regra, Subperfil subperfil) {
        this.regra = regra;
        this.subperfil = subperfil;
    }

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
	@JoinColumn(name="SUBPERFIL_ID")
	public Subperfil getSubperfil() {
		return subperfil;
	}

	public void setSubperfil(Subperfil subperfil) {
		this.subperfil = subperfil;
	}

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="REGRA_ID", nullable=false)
    public Regra getRegra() { return regra; }

    public void setRegra(Regra regra) { this.regra = regra; }
}