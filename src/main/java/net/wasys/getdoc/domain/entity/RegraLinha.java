package net.wasys.getdoc.domain.entity;

import net.wasys.getdoc.domain.enumeration.TipoSubRegra;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity(name="REGRA_LINHA")
public class RegraLinha extends net.wasys.util.ddd.Entity {

	private Long id;

	private Regra regra;
	private RegraLinha linhaPai;
	private RegraLinha filha;

	private Set<SubRegra> subRegras = new HashSet<>();

	@Id
	@Override
	@Column(name="ID", unique=true, nullable=false)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="REGRA_ID", nullable=false)
	public Regra getRegra() {
		return regra;
	}

	public void setRegra(Regra regra) {
		this.regra = regra;
	}

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="REGRA_LINHA_PAI_ID")
	public RegraLinha getLinhaPai() {
		return linhaPai;
	}

	public void setLinhaPai(RegraLinha linhaPai) {
		this.linhaPai = linhaPai;
	}

	@OneToOne(mappedBy="linhaPai", fetch=FetchType.LAZY)
	public RegraLinha getFilha() {
		return filha;
	}

	public void setFilha(RegraLinha filha) {
		this.filha = filha;
	}

	@OrderBy("FILHO_SIM, ID")
	@OneToMany(mappedBy="linha", fetch=FetchType.LAZY)
	public Set<SubRegra> getSubRegras() {
		return subRegras;
	}

	public void setSubRegras(Set<SubRegra> subRegras) {
		this.subRegras = subRegras;
	}

	@Transient
	public List<SubRegra> getSubRegrasList() {

		List<SubRegra> subRegraList = new ArrayList<>(subRegras);

		if(linhaPai != null && linhaPai.isCondicional()) {

			//verifica se tem o filho nao
			for (SubRegra subRegra : subRegras) {
				Boolean filhoSim = subRegra.getFilhoSim();
				if(filhoSim != null && !filhoSim) {
					return subRegraList;
				}
			}

			//se não tinha o filho não, então adiciona
			SubRegra subNao = new SubRegra();
			subRegraList.add(0, subNao);
		}

		return subRegraList;
	}

	@Transient
	public boolean isPaiCondicional() {

		if(linhaPai == null) {
			return false;
		}

		return linhaPai.isCondicional();
	}

	@Transient
	public boolean isCondicional() {

		boolean condicional = false;

		if(subRegras != null && !subRegras.isEmpty()) {
			SubRegra subRegraPai = subRegras.iterator().next();
			condicional = TipoSubRegra.CONDICAO.equals(subRegraPai.getTipo());
		}

		return condicional;
	}
}
