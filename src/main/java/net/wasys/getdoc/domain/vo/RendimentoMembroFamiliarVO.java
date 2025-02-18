package net.wasys.getdoc.domain.vo;

public class RendimentoMembroFamiliarVO {

	private String parentesco = "";
	private String rendimento1 = "";
	private String rendimento2 = "";
	private String rendimento3 = "";
	private String rendimento4 = "";
	private String rendimento5 = "";
	private String rendimento6 = "";
	private String media = "";

	public RendimentoMembroFamiliarVO() {
	}

	public RendimentoMembroFamiliarVO(String parentesco) {
		this.parentesco = parentesco;
	}

	public RendimentoMembroFamiliarVO(String parentesco, String rendimento1, String rendimento2, String rendimento3, String rendimento4, String rendimento5, String rendimento6, String media) {
		this.parentesco = parentesco;
		this.rendimento1 = rendimento1;
		this.rendimento2 = rendimento2;
		this.rendimento3 = rendimento3;
		this.rendimento4 = rendimento4;
		this.rendimento5 = rendimento5;
		this.rendimento6 = rendimento6;
		this.media = media;
	}

	public String getParentesco() {
		return parentesco;
	}

	public void setParentesco(String parentesco) {
		this.parentesco = parentesco;
	}

	public String getRendimento1() {
		return rendimento1;
	}

	public void setRendimento1(String rendimento1) {
		this.rendimento1 = rendimento1;
	}

	public String getRendimento2() {
		return rendimento2;
	}

	public void setRendimento2(String rendimento2) {
		this.rendimento2 = rendimento2;
	}

	public String getRendimento3() {
		return rendimento3;
	}

	public void setRendimento3(String rendimento3) {
		this.rendimento3 = rendimento3;
	}

	public String getRendimento4() {
		return rendimento4;
	}

	public void setRendimento4(String rendimento4) {
		this.rendimento4 = rendimento4;
	}

	public String getRendimento5() {
		return rendimento5;
	}

	public void setRendimento5(String rendimento5) {
		this.rendimento5 = rendimento5;
	}

	public String getRendimento6() {
		return rendimento6;
	}

	public void setRendimento6(String rendimento6) {
		this.rendimento6 = rendimento6;
	}

	public String getMedia() {
		return media;
	}

	public void setMedia(String media) {
		this.media = media;
	}

	@Override public String toString() {
		return "RendimentoMembroFamiliarVO{" +
				"parentesco='" + parentesco + '\'' +
				", rendimento1='" + rendimento1 + '\'' +
				", rendimento2='" + rendimento2 + '\'' +
				", rendimento3='" + rendimento3 + '\'' +
				", rendimento4='" + rendimento4 + '\'' +
				", rendimento5='" + rendimento5 + '\'' +
				", rendimento6='" + rendimento6 + '\'' +
				", media='" + media + '\'' +
				'}';
	}
}