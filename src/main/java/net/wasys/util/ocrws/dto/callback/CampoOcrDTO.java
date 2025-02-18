package net.wasys.util.ocrws.dto.callback;

public class CampoOcrDTO {

	private Long id;
	private String nome;
	private String valor;
	private int positionTop;
	private int positionLeft;
	private int positionRight;
	private int positionBottom;
	private String recognizedValue;
	private String suspiciousSymbols;
	private String blockRef;
	private String errorRef;
	private String fieldId;
	private String erros;
	private Integer nivelConfianca;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

	public int getPositionTop() {
		return positionTop;
	}

	public void setPositionTop(int positionTop) {
		this.positionTop = positionTop;
	}

	public int getPositionLeft() {
		return positionLeft;
	}

	public void setPositionLeft(int positionLeft) {
		this.positionLeft = positionLeft;
	}

	public int getPositionRight() {
		return positionRight;
	}

	public void setPositionRight(int positionRight) {
		this.positionRight = positionRight;
	}

	public int getPositionBottom() {
		return positionBottom;
	}

	public void setPositionBottom(int positionBottom) {
		this.positionBottom = positionBottom;
	}

	public String getRecognizedValue() {
		return recognizedValue;
	}

	public void setRecognizedValue(String recognizedValue) {
		this.recognizedValue = recognizedValue;
	}

	public String getSuspiciousSymbols() {
		return suspiciousSymbols;
	}

	public void setSuspiciousSymbols(String suspiciousSymbols) {
		this.suspiciousSymbols = suspiciousSymbols;
	}

	public String getBlockRef() {
		return blockRef;
	}

	public void setBlockRef(String blockRef) {
		this.blockRef = blockRef;
	}

	public String getErrorRef() {
		return errorRef;
	}

	public void setErrorRef(String errorRef) {
		this.errorRef = errorRef;
	}

	public String getFieldId() {
		return fieldId;
	}

	public void setFieldId(String fieldId) {
		this.fieldId = fieldId;
	}

	public String getErros() {
		return erros;
	}

	public void setErros(String erros) {
		this.erros = erros;
	}

	public Integer getNivelConfianca() {
		return nivelConfianca;
	}

	public void setNivelConfianca(Integer nivelConfianca) {
		this.nivelConfianca = nivelConfianca;
	}
}