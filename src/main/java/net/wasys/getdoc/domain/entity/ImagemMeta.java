package net.wasys.getdoc.domain.entity;

import javax.persistence.*;
import java.util.Date;

@Entity(name="IMAGEM_META")
@Table(uniqueConstraints=@UniqueConstraint(columnNames="IMAGEM_ID"))
public class ImagemMeta extends net.wasys.util.ddd.Entity implements Cloneable {

	private Long id;
	private Long imagemId;
	private Long tamanho;
	private Integer width;
	private Integer height;
	private String metaDados;
	private Date dataDigitalizacao;
	private Integer paginas;
	private boolean tipificado;
	private boolean precisaTipificar;
	private String fullText;

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

	@Column(name="IMAGEM_ID", unique=true, nullable=false)
	public Long getImagemId() {
		return imagemId;
	}

	public void setImagemId(Long imagemId) {
		this.imagemId = imagemId;
	}

	@Column(name="TAMANHO", unique=false, nullable=true)
	public Long getTamanho() {
		return tamanho;
	}

	public void setTamanho(Long tamanho) {
		this.tamanho = tamanho;
	}

	@Column(name="WIDTH", unique=false, nullable=true)
	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	@Column(name="HEIGHT", unique=false, nullable=true)
	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	@Column(name="METADADOS", unique=false, nullable=true)
	public String getMetaDados() {
		return metaDados;
	}

	public void setMetaDados(String metaDados) {
		this.metaDados = metaDados;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="DATA_DIGITALIZACAO")
	public Date getDataDigitalizacao() {
		return dataDigitalizacao;
	}

	public void setDataDigitalizacao(Date dataDigitalizacao) {
		this.dataDigitalizacao = dataDigitalizacao;
	}

	@Column(name="PAGINAS")
	public Integer getPaginas() {
		return paginas;
	}

	public void setPaginas(Integer paginas) {
		this.paginas = paginas;
	}

	@Column(name="TIPIFICADO")
	public boolean getTipificado() {
		return tipificado;
	}

	public void setTipificado(boolean tipificado) {
		this.tipificado = tipificado;
	}

	@Column(name="PRECISA_TIPIFICAR")
	public boolean getPrecisaTipificar() {
		return precisaTipificar;
	}

	public void setPrecisaTipificar(boolean precisaTipificar) {
		this.precisaTipificar = precisaTipificar;
	}

	@Override
	public ImagemMeta clone() {
		try {
			ImagemMeta clone = (ImagemMeta) super.clone();
			clone.setId(null);
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	@Column(name="FULL_TEXT", unique=false, nullable=true)
	public String getFullText() {
		return fullText;
	}

	public void setFullText(String fullText) {
		this.fullText = fullText;
	}
}
