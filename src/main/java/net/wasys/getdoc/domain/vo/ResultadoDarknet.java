package net.wasys.getdoc.domain.vo;

import java.math.BigDecimal;

public class ResultadoDarknet {

	private String label;
	private BigDecimal confidence;
	private Integer topleftX;
	private Integer topleftY;
	private Integer bottomRightX;
	private Integer bottomRightY;
	private String json;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public BigDecimal getConfidence() {
		return confidence;
	}

	public void setConfidence(BigDecimal confidence) {
		this.confidence = confidence;
	}

	public Integer getTopleftX() {
		return topleftX;
	}

	public void setTopleftX(Integer topleftX) {
		this.topleftX = topleftX;
	}

	public Integer getTopleftY() {
		return topleftY;
	}

	public void setTopleftY(Integer topleftY) {
		this.topleftY = topleftY;
	}

	public Integer getBottomRightX() {
		return bottomRightX;
	}

	public void setBottomRightX(Integer bottomRightX) {
		this.bottomRightX = bottomRightX;
	}

	public Integer getBottomRightY() {
		return bottomRightY;
	}

	public void setBottomRightY(Integer bottomRightY) {
		this.bottomRightY = bottomRightY;
	}

	public String getJson() {
		return json;
	}

	public void setJson(String json) {
		this.json = json;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{label:" + label + ",confidence:" + confidence + ",topleftX:" + topleftX + ",topleftY:" + topleftY + ",bottomRightX:" + bottomRightX + ",bottomRightY:" + bottomRightY + "}";
	}
}

