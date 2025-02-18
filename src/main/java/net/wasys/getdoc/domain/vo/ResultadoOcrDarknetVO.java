package net.wasys.getdoc.domain.vo;

public class ResultadoOcrDarknetVO {


	public static final int INDEX_OCR_VALUE = 0;
	public static final int INDEX_OCR_COORDS = 1;
	public static final int INDEX_OCR_COORDS_POSITION_LEFT = 0;
	public static final int INDEX_OCR_COORDS_POSITION_TOP = 1;
	public static final int INDEX_OCR_COORDS_POSITION_RIGHT = 2;
	public static final int INDEX_OCR_COORDS_POSITION_BOTTOM = 3;

    private String labelOcr;
    private String valueOcr;
    private Integer positionTop;
    private Integer positionLeft;
    private Integer positionBottom;
    private Integer positionRight;


	public String getLabelOcr() {
		return labelOcr;
	}

	public void setLabelOcr(String labelOcr) {
		this.labelOcr = labelOcr;
	}

	public String getValueOcr() {
		return valueOcr;
	}

	public void setValueOcr(String valueOcr) {
		this.valueOcr = valueOcr;
	}

	public Integer getPositionTop() {
		return positionTop;
	}

	public void setPositionTop(Integer positionTop) {
		this.positionTop = positionTop;
	}

	public Integer getPositionLeft() {
		return positionLeft;
	}

	public void setPositionLeft(Integer positionLeft) {
		this.positionLeft = positionLeft;
	}

	public Integer getPositionBottom() {
		return positionBottom;
	}

	public void setPositionBottom(Integer positionBottom) {
		this.positionBottom = positionBottom;
	}

	public Integer getPositionRight() {
		return positionRight;
	}

	public void setPositionRight(Integer positionRight) {
		this.positionRight = positionRight;
	}

	@Override
    public String toString() {
        return getClass().getSimpleName() + "{label_ocr:" + labelOcr + ", value_ocr:" + valueOcr + ", position_top:" + positionTop + ", position_left:" + positionLeft + ", position_bottom:" + positionBottom + ", position_right:" + positionRight + " }";
    }
}

