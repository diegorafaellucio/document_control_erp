package net.wasys.getdoc.domain.entity;

import javax.persistence.*;

@Entity(name="TIPO_EVIDENCIA_ROLE")
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"TIPO_EVIDENCIA_ID"}))
public class TipoEvidenciaRole extends net.wasys.util.ddd.Entity {

	private Long id;
    private String role;

	private TipoEvidencia tipoEvidencia;

    public TipoEvidenciaRole() {
    }

    public TipoEvidenciaRole(TipoEvidencia tipoEvidencia, String role) {
        this.tipoEvidencia = tipoEvidencia;
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
    @JoinColumn(name="TIPO_EVIDENCIA_ID", nullable=false)
    public TipoEvidencia getTipoEvidencia() {
        return this.tipoEvidencia;
    }

    public void setTipoEvidencia(TipoEvidencia tipoEvidencia) {
        this.tipoEvidencia = tipoEvidencia;
    }
}