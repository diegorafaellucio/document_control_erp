package net.wasys.getdoc.domain.vo;

public class ModeloDocumentoReaproveitamentoVO {

	private Long modeloDocumentoId;
	private String labelDarknet;

	public ModeloDocumentoReaproveitamentoVO() {
	}

	public ModeloDocumentoReaproveitamentoVO(Long modeloDocumentoId, String labelDarknet) {
		this.modeloDocumentoId = modeloDocumentoId;
		this.labelDarknet = labelDarknet;
	}

	public Long getModeloDocumentoId() {
		return modeloDocumentoId;
	}

	public void setModeloDocumentoId(Long modeloDocumentoId) {
		this.modeloDocumentoId = modeloDocumentoId;
	}

	public String getLabelDarknet() {
		return labelDarknet;
	}

	public void setLabelDarknet(String labelDarknet) {
		this.labelDarknet = labelDarknet;
	}

	@Override public String toString() {
		return "ModeloDocumentoReaproveitamentoVO{" +
				"modeloDocumentoId=" + modeloDocumentoId +
				", labelDarknet='" + labelDarknet + '\'' +
				'}';
	}
}
