package net.wasys.getdoc.domain.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import net.wasys.getdoc.domain.entity.CampoOcr;
import net.wasys.getdoc.domain.entity.Documento;
import net.wasys.getdoc.domain.entity.Imagem;
import net.wasys.util.DummyUtils;

public class CampoOcrVO extends CampoOcr {

	private Map<String, CampoOcrVO> comparativos = new LinkedHashMap<String, CampoOcrVO>();
	private String valorFinalBaseComparacao;
	private boolean temInconsistencia;
	private CampoOcrVO anterior;
	private CampoOcrVO proximo;
	private String contextPath;
	private String caminhoImagem;
	private int widthImagem;
	private int heightImagem;

	public void addComparativo(CampoOcrVO ocr, String valorFinalBaseComparacao) {

		String chave = "[FORM]";
		Imagem imagem = ocr.getImagem();
		if(imagem != null) {
			Documento documento = imagem.getDocumento();
			chave = documento.getNome();
		}

		String nome = ocr.getNome();

		String valorSemMascara = "";

		if(StringUtils.isNotBlank(nome)){
			String valor = ocr.getValor();
			if(nome.contains("CPF")){
				valorFinalBaseComparacao = DummyUtils.getCpfCnpjDesformatado(valorFinalBaseComparacao);
				valorSemMascara = DummyUtils.getCpfCnpjDesformatado(valor);
			}else if (nome.contains("DATA")){
				if(StringUtils.isNotBlank(valorFinalBaseComparacao) && StringUtils.isNotBlank(valor)){
					try{
						valorFinalBaseComparacao = valorFinalBaseComparacao.replaceAll("[^\\d.]", "");
						valorFinalBaseComparacao = StringUtils.substring(valorFinalBaseComparacao, 0, 8);
						valorFinalBaseComparacao = valorFinalBaseComparacao.length() > 8 ? StringUtils.substring(valorFinalBaseComparacao, 0, 8) : valorFinalBaseComparacao;
						valorFinalBaseComparacao = DummyUtils.formatDate(DummyUtils.parseDate4(valorFinalBaseComparacao));

						valor = valor.replaceAll("[^\\d.]", "");
						valor = valor.length() > 8 ? StringUtils.substring(valor, 0, 8) : valor;
						valorSemMascara = DummyUtils.formatDate(DummyUtils.parseDate4(valor));
					}catch (Exception e){

					}
				}
			}
		}

		ocr.setValorFinalBaseComparacao(valorFinalBaseComparacao);
		ocr.setRecognizedValue(valorSemMascara);

		comparativos.put(chave, ocr);

	}

	public CampoOcrVO getProximo() {
		return proximo;
	}

	public void setProximo(CampoOcrVO proximo) {
		this.proximo = proximo;
	}

	public CampoOcrVO getAnterior() {
		return anterior;
	}

	public void setAnterior(CampoOcrVO anterior) {
		this.anterior = anterior;
	}

	public Map<String, CampoOcrVO> getComparativos() {
		return comparativos;
	}

	public boolean getTemInconsistencia() {
		return temInconsistencia;
	}

	public void setTemInconsistencia(boolean temInconsistencia) {
		this.temInconsistencia = temInconsistencia;
	}

	public String getCaminhoImagem() {
		return caminhoImagem;
	}

	public void setCaminhoImagem(String caminhoImagem) {
		this.caminhoImagem = caminhoImagem;
	}

	public int getWidthImagem() {
		return widthImagem;
	}

	public void setWidthImagem(int widthImagem) {
		this.widthImagem = widthImagem;
	}

	public int getHeightImagem() {
		return heightImagem;
	}

	public void setHeightImagem(int heightImagem) {
		this.heightImagem = heightImagem;
	}

	public String getNomeHtml() {

		String nome = getNome();
		if(StringUtils.isBlank(nome)) {
			return "";
		}

		nome = nome.replace("_", " ");
		nome = nome.trim();
		nome = DummyUtils.capitalize(nome);
		return nome;
	}

	public String getValorHtml() {

		String valorEditado = getValorEditado();
		if(StringUtils.isNotBlank(valorEditado)) {
			return valorEditado;
		}

		String valor = getValor();

		String recognizedValue = getRecognizedValue();
		if(StringUtils.isNotBlank(recognizedValue)) {
			valor = recognizedValue;
		}

		if(StringUtils.isBlank(valor)) {
			return "";
		}

		String suspiciousSymbols = getSuspiciousSymbols();
		if(StringUtils.isBlank(suspiciousSymbols) || !suspiciousSymbols.contains("1")) {
			return valor;
		}

		StringBuilder html = new StringBuilder();
		char[] suspiciousSymbolsArray = suspiciousSymbols.toCharArray();

		if(valor.length() == suspiciousSymbolsArray.length) {

			for (int i = 0; i < suspiciousSymbolsArray.length; i++) {

				char suspiciousSymbol = suspiciousSymbolsArray[i];
				char valorChar = valor.charAt(i);

				if(suspiciousSymbol == '1') {
					html.append("<span style='color: #00F'>").append(valorChar).append("</span>");
				} else {
					html.append(valorChar);
				}
			}
		}
		else {
			html.append(valor);
		}

		return html.toString();
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public String getContextPath() {
		return contextPath;
	}

	public void setValorFinalBaseComparacao(String valorFinalBaseComparacao) {
		this.valorFinalBaseComparacao = valorFinalBaseComparacao;
	}

	public String getValorFinalHtml() {
		return getValorFinalHtml(valorFinalBaseComparacao);
	}

	public String getValorFinalSimilaridade() {

		String valorFinal = StringUtils.upperCase(getValorFinal());
		String comparar = StringUtils.upperCase(valorFinalBaseComparacao);

		BigDecimal percentBigDecimal = new BigDecimal(0L);
		if (StringUtils.isNotBlank(valorFinal) && StringUtils.isNotBlank(comparar)){
			double similaridade1 = DummyUtils.getSimilaridade(comparar, valorFinal);
			percentBigDecimal = new BigDecimal(String.valueOf(similaridade1));
			percentBigDecimal = percentBigDecimal.multiply(new BigDecimal("100"));
			percentBigDecimal = percentBigDecimal.setScale(0, RoundingMode.HALF_UP);
		}

		StringBuilder html = new StringBuilder();
		html.append(" <span style='margin-left: 5px; margin-top: 7px; float: left;'>(");
		html.append(percentBigDecimal.toString()).append("% <i class='fa fa-info' aria-hidden='true' title='Similaridade'></i>");
		html.append(")</span>");

		return html.toString();
	}

	public String getValorOriginalHtml() {

		String valorFinal = getValorFinal();
		String valorOriginal = getValorOriginal();

		StringBuffer html = toValorHtml(valorFinal, valorOriginal, true);

		return html.toString();
	}

	public String getValorFinalHtml(String comparar) {

		String valorFinal = getValorFinal();

		StringBuffer html = toValorHtml(comparar, valorFinal, false);

		return html.toString();
	}

	private StringBuffer toValorHtml(String comparar, String valorFinal, boolean marcarSuspeitos) {

		valorFinal = valorFinal != null ? valorFinal : "";
		int valorFinalLength = valorFinal.length();
		boolean comp = comparar != null;
		int compararLength = comp ? comparar.length() : 0;
		int max = Math.max(valorFinalLength, compararLength);
		StringBuffer html = new StringBuffer();
		html.append("<table style='float: left'><tr>");
		for (int i = 0; i < max; i++) {

			String char1 = comp && compararLength > i ? String.valueOf(comparar.charAt(i)) : "";
			String char2 = valorFinalLength > i ? String.valueOf(valorFinal.charAt(i)) : "";

			html.append("<td class='caractereComparacao'");
			if(i > 0) {
				html.append(" style='border-left: 1px #ccc solid;' ");
			}

			boolean erroComparacao = comp && !char1.equalsIgnoreCase(char2);
			boolean suspeito = false;
			if(marcarSuspeitos) {
				String suspiciousSymbols = getSuspiciousSymbols();

				if(StringUtils.isNotBlank(suspiciousSymbols) && suspiciousSymbols.length() > i) {

					char suspeitoChar = suspiciousSymbols.charAt(i);
					suspeito = suspeitoChar == '1';
				}
			}

			if(suspeito && erroComparacao) {

				html.append("color: red;");
				html.append("border-bottom: 1px blue solid;");
			}
			else if(erroComparacao) {
				html.append("color: red;");
			}
			else if(suspeito) {
				html.append("color: blue;");
			}
			else {
				html.append("");
			}

			html.append("'>").append(char2);
			html.append("</td>");
		}

		html.append("</tr></table>");
		return html;
	}
}