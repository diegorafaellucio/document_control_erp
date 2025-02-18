package net.wasys.getdoc.domain.vo;

public class ReaproveitamentoProcessoVO {

	private Long situacaoId;
	private ReaproveitamentoCampanhaVO dadosCampanha;
	private Boolean usaTermo;
	private Boolean isFies;
	private Boolean isProuni;
	private String area;

	public Long getSituacaoId() {
		return situacaoId;
	}

	public void setSituacaoId(Long situacaoId) {
		this.situacaoId = situacaoId;
	}

	public ReaproveitamentoCampanhaVO getDadosCampanha() {
		return dadosCampanha;
	}

	public void setDadosCampanha(ReaproveitamentoCampanhaVO dadosCampanha) {
		this.dadosCampanha = dadosCampanha;
	}

	public Boolean getUsaTermo() {
		return usaTermo;
	}

	public void setUsaTermo(Boolean usaTermo) {
		this.usaTermo = usaTermo;
	}

	@Override public String toString() {
		return "ReaproveitamentoProcessoVO{" +
				"situacaoId=" + situacaoId +
				", dadosCampanha=" + dadosCampanha +
				", usaTermo=" + usaTermo +
				'}';
	}

	public Boolean getFies() {
		return isFies;
	}

	public void setFies(Boolean fies) {
		isFies = fies;
	}

	public Boolean getProuni() {
		return isProuni;
	}

	public void setProuni(Boolean prouni) {
		isProuni = prouni;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}
}
