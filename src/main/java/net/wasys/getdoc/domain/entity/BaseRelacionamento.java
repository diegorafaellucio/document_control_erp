package net.wasys.getdoc.domain.entity;

import net.wasys.util.rest.jackson.ObjectMapper;

import javax.persistence.*;
import java.util.List;

@Entity(name="BASE_RELACIONAMENTO")
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"BASE_EXTRANGEIRA_ID", "CHAVE_EXTRANGEIRA"}))
public class BaseRelacionamento extends net.wasys.util.ddd.Entity {

	private Long id;
	private String chaveExtrangeira;

	private BaseInterna baseInterna;
	private BaseInterna baseExtrangeira;

	@Id
	@Column(name="ID", unique=true, nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name="CHAVE_EXTRANGEIRA", length=200, nullable=false)
	public String getChaveExtrangeira() {
		return chaveExtrangeira;
	}

	public void setChaveExtrangeira(String chaveExtrangeira) {
		this.chaveExtrangeira = chaveExtrangeira;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="BASE_INTERNA_ID", nullable=false)
	public BaseInterna getBaseInterna() {
		return baseInterna;
	}

	public void setBaseInterna(BaseInterna baseInterna) {
		this.baseInterna = baseInterna;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="BASE_EXTRANGEIRA_ID", nullable=false)
	public BaseInterna getBaseExtrangeira() {
		return baseExtrangeira;
	}

	public void setBaseExtrangeira(BaseInterna baseExtrangeira) {
		this.baseExtrangeira = baseExtrangeira;
	}

	@Transient
	public void setChaveExtrangeiraList(List<String> chaveExtrangeiraList) {
		ObjectMapper om = new ObjectMapper();
		this.chaveExtrangeira = chaveExtrangeiraList != null ? om.writeValueAsString(chaveExtrangeiraList) : null;
	}

	@Transient
	public List<String> getChaveExtrangeiraList() {
		ObjectMapper om = new ObjectMapper();
		return chaveExtrangeira != null ? om.readValue(chaveExtrangeira, List.class) : null;
	}
}