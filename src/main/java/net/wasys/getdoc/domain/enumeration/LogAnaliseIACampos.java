package net.wasys.getdoc.domain.enumeration;

import java.util.Arrays;
import java.util.List;

public class LogAnaliseIACampos {

	public enum ColunasEnum {
		PROCESSO_ID("Processo Id"),
		PROCESSO_NOME("Tipo Processo"),
		DOCUMENTO_ID("Documento Id"),
		DOCUMENTO_NOME("Tipo Documento"),
		DATA("Data"),
		DATA_DIGITALIZACAO("Data de Digitalizacao"),
		STATUS_DOCUMENTO("Status Documento"),
		MOTIVO_DOCUMENTO("Motivo Documento"),
		STATUS_PROCESSO("Status Processo"),
		MOTIVO_PROCESSO("Motivo Processo"),
		TIPIFICOU("Tipificou"),
		OCR("Realizou OCR"),
		OCR_NOME("OCR Nome"),
		OCR_NOME_PROCESSO("Nome Processo"),
		SCORE_NOME("Score Nome"),
		OCR_CPF("OCR Cpf"),
		OCR_CPF_PROCESSO("CPF Processo"),
		SCORE_CPF("Score CPF"),
		MODELO_DOCUMENTO("Modelo Documento"),
		LABEL_TIPIFICACAO("Label Tipificacao"),
		SCORE_TIPIFICACAO("SCORE Tipificacao"),
		METADADOS("Metadados")
		;

		private String nome;
		ColunasEnum(String nome) {
			this.nome = nome;
		}
		public String getNome() {
			return nome;
		}
	}
}