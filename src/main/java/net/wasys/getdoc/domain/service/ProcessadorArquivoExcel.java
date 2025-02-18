package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import net.wasys.getdoc.domain.vo.LinhaVO;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.MessageKeyException;

public class ProcessadorArquivoExcel extends ProcessadorArquivo {

	private DataFormatter formatter = new DataFormatter(new Locale("pt-BR"));

	public ProcessadorArquivoExcel(File file, List<String> colunasUnicidade) throws IOException {
		super(file, colunasUnicidade);
	}

	@Override
	public List<String> carregarCabecalho() throws MessageKeyException {

		List<String> colunas = new ArrayList<>();
		Workbook wb = null;

		try {
			wb = getWorkbook();
			Sheet sheet = wb.getSheetAt(0);

			for (Cell cell : sheet.getRow(0)) {
				colunas.add(cell.getStringCellValue());
			}
		} finally {
			fecharWorkbook(wb);
		}

		return colunas;
	}

	@Override
	List<LinhaVO> carregarLinhas() throws MessageKeyException {

		List<LinhaVO> linhas = new ArrayList<>();
		Workbook wb = null;

		try {
			wb = getWorkbook();
			Sheet sheet = wb.getSheetAt(0);
			List<String> colunas = new ArrayList<>();

			sheet.removeRow(sheet.getRow(0)); // pular cabecalho
			int i = 2;
			for (Row row : sheet) {
				short lastCellNum = row.getLastCellNum();
				for (int j = 0; j < lastCellNum; j++) {
					Cell cell = row.getCell(j);
					if(cell == null) {
						colunas.add("");
					}
					else
					switch (cell.getCellTypeEnum()) {
					case STRING:
						colunas.add(cell.getStringCellValue());
						break;
					case NUMERIC:
						colunas.add(tratarCampoNumerico(cell));
						break;
					case BOOLEAN:
						colunas.add(Boolean.toString(cell.getBooleanCellValue()));
						break;
					case BLANK:
						colunas.add("");
						break;
					default:
						throw new MessageKeyException("importacaoBase.valorFormatoInvalido.error", cell.getAddress());
					}
				}

				LinhaVO l = processarLinha(i, colunas);
				if (l != null) {
					linhas.add(l);
				}

				colunas.clear();
				i++;
			}
		} finally {
			fecharWorkbook(wb);
		}

		return linhas;
	}

	private String tratarCampoNumerico(Cell cell) throws MessageKeyException {

		if (!DateUtil.isCellDateFormatted(cell)) {
			return formatter.formatCellValue(cell);
		} else {
			return tratarCampoData(cell);
		}
	}

	private String tratarCampoData(Cell cell) throws MessageKeyException {

		String data = null;
		String formatoData = cell.getCellStyle().getDataFormatString();

		if (formatoData != null) {
			data = formatoData.equals("General") ? cell.getDateCellValue() + "" : formatarData(cell);
		}

		if (!StringUtils.isBlank(data)) {
			return data;
		} else {
			throw new MessageKeyException("importacaoBase.valorFormatoInvalido.error", cell.getAddress());
		}
	}

	private String formatarData(Cell cell) {

		String data = null;

		try {
			String value = formatter.formatCellValue(cell);
			data = StringUtils.trim(value);
		} catch (java.lang.AbstractMethodError e) {
			String value = cell.getStringCellValue();
			String[] split = value.split("[/]");
			if (split.length == 3) {
				data = split[1] + "/" + split[0] + "/" + split[2];
			}
		}

		return data;
	}

	private Workbook getWorkbook() throws MessageKeyException {
		String extensao = DummyUtils.getExtensao(file.getName());
		FileInputStream fis;
		Workbook wb;

		try {
			fis = new FileInputStream(file);

			if("xls".equals(extensao)) {
				wb = new HSSFWorkbook(fis);
			} else {
				wb = new XSSFWorkbook(fis);
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new MessageKeyException("erroInesperado.error", e.getMessage());
		}

		return wb;
	}

	private void fecharWorkbook(Workbook wb) throws MessageKeyException {
		if(wb != null) {
			try {
				wb.close();
			} catch (IOException e) {
				e.printStackTrace();
				throw new MessageKeyException("erroInesperado.error", e.getMessage());
			}
		}
	}
}