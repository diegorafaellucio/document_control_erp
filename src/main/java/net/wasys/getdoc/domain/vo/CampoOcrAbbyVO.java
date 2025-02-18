package net.wasys.getdoc.domain.vo;

import java.util.ArrayList;
import java.util.List;

public class CampoOcrAbbyVO {

	private String nome;
	private String valor;
	private String suspiciousSymbols;
	private String recognizedValue;
	private String blockRef;
	private String errorRef;
	private String fieldId;
	private int left;
	private int right;
	private int top;
	private int bottom;
	private List<String> erros = new ArrayList<String>();

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

	public String getSuspiciousSymbols() {
		return suspiciousSymbols;
	}

	public void setSuspiciousSymbols(String suspiciousSymbols) {
		this.suspiciousSymbols = suspiciousSymbols;
	}

	public String getRecognizedValue() {
		return recognizedValue;
	}

	public void setRecognizedValue(String recognizedValue) {
		this.recognizedValue = recognizedValue;
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

	public int getLeft() {
		return left;
	}

	public void setLeft(int left) {
		this.left = left;
	}

	public int getRight() {
		return right;
	}

	public void setRight(int right) {
		this.right = right;
	}

	public int getTop() {
		return top;
	}

	public void setTop(int top) {
		this.top = top;
	}

	public int getBottom() {
		return bottom;
	}

	public void setBottom(int bottom) {
		this.bottom = bottom;
	}

	public void addErro(String erro) {
		this.erros.add(erro);
	}

	public List<String> getErros() {
		return erros;
	}
}
