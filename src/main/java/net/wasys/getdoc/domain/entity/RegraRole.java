package net.wasys.getdoc.domain.entity;

import javax.persistence.*;

@Entity(name="REGRA_ROLE")
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"REGRA_ID"}))
public class RegraRole extends net.wasys.util.ddd.Entity {

	private Long id;
    private String role;

	private Regra regra;

    public RegraRole() {
    }

    public RegraRole(Regra regra, String role) {
        this.regra = regra;
        this.role = role;
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

	@Column(name="ROLE", nullable=false)
	public String getRole() { return role; }

	public void setRole(String role) { this.role = role; }

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="REGRA_ID", nullable=false)
    public Regra getRegra() { return regra; }

    public void setRegra(Regra regra) { this.regra = regra; }
}