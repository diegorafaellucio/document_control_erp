package net.wasys.util.other;

import net.wasys.util.excel.ExcelWriter;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelCsvWriter {

	private ExcelWriter ew;
	private CSVPrinter csvw;
	private List<String> currentLinha;

	public void setWriter(ExcelWriter ew) {
		this.ew = ew;
	}

	public void setWriter(CSVPrinter csvw) {
		this.csvw = csvw;
	}

	public void criaLinha(int i) {
		if(ew != null) {
			ew.criaLinha(i);
		}
		else {
			if(currentLinha != null) {
				try {
					csvw.printRecord(currentLinha);
					csvw.flush();
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			currentLinha = new ArrayList<>();
		}
	}

	public void escrever(Object valor, Integer width) {
		if(ew != null) {
			if(valor instanceof Number) {
				ew.escrever((Number) valor, width);
			} else {
				ew.escrever((String) valor, width);
			}
		}
		else {
			String valorStr = valor != null ? String.valueOf(valor) : "";
			valorStr = valorStr.replace("\r", " ");
			valorStr = valorStr.replace("\n", " ");
			currentLinha.add(valorStr);
		}
	}

	public void escrever(Object valor) {
		if(ew != null) {
			if(valor instanceof Number) {
				ew.escrever((Number) valor);
			} else {
				ew.escrever((String) valor);
			}
		}
		else {
			String valorStr = valor != null ? String.valueOf(valor) : "";
			valorStr = valorStr.replace("\r", " ");
			valorStr = valorStr.replace("\n", " ");
			currentLinha.add(valorStr);
		}
	}

	public void escreverSemPontoVirgula(Object valor) {

		if(ew != null) {
			if(valor instanceof Number) {
				ew.escrever((Number) valor);
			} else {
				ew.escrever(((String) valor).replace(";", ""));
			}
		}
		else {
			String valorStr = valor != null ? String.valueOf(valor) : "";
			valorStr = valorStr.replace("\r", " ");
			valorStr = valorStr.replace("\n", " ");
			valorStr = valorStr.replace(";", "");
			currentLinha.add(valorStr);
		}
	}

	public void close(File fileDestino) {
		try {
			if(ew != null) {
				Workbook workbook = ew.getWorkbook();
				FileOutputStream fos = new FileOutputStream(fileDestino);
				workbook.write(fos);
				workbook.close();
			}
			else {
				if(currentLinha != null) {
					csvw.printRecord(currentLinha);
				}
				csvw.flush();
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
