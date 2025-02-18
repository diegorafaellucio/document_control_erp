package net.wasys.getdoc.domain.entity;

import javax.persistence.*;

@Entity(name = "IRREGULARIDADE_TIPO_DOCUMENTO")
public class IrregularidadeTipoDocumento extends net.wasys.util.ddd.Entity {

    private Long id;
    private TipoDocumento tipoDocumento;
    private Irregularidade irregularidade;

    @Id
    @Override
    @Column(name="ID", unique=true, nullable=false)
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="TIPO_DOCUMENTO_ID")
    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(TipoDocumento tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="IRREGULARIDADE_ID")
    public Irregularidade getIrregularidade() {
        return irregularidade;
    }

    public void setIrregularidade(Irregularidade irregularidade) {
        this.irregularidade = irregularidade;
    }
}
