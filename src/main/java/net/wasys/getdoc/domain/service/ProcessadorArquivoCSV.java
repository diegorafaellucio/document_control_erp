package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.wasys.getdoc.domain.vo.LinhaVO;
import net.wasys.util.ddd.MessageKeyException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

public class ProcessadorArquivoCSV extends ProcessadorArquivo {

	static final String COMMA = ";";

	public ProcessadorArquivoCSV(File file, List<String> colunasUnicidade) throws FileNotFoundException {
		super(file, colunasUnicidade);
	}

	@Override
	public List<String> carregarCabecalho() throws MessageKeyException {

		List<String> colunas = new ArrayList<>();
		BufferedReader br = null;
		try {
			br = getBufferedReader();
			String primeiraLinha = br.readLine();

			if(!isBlank(primeiraLinha)) {
				colunas = asList(primeiraLinha.split(COMMA));
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new MessageKeyException("erroInesperado.error", e.getMessage());
		} finally {
			fecharBufferedReader(br);
		}

		List<String> colunas2 = new ArrayList<>();
		for (String coluna : colunas) {
			coluna = coluna.replaceAll("^\"", "");
			coluna = coluna.replaceAll("\"$", "");
			colunas2.add(coluna);
		}

		return colunas2;
	}

	@Override
	List<LinhaVO> carregarLinhas() throws MessageKeyException {

		List<LinhaVO> linhas = new ArrayList<>();
		
		if(!cabecalho.isEmpty()) {
			String linha;

			BufferedReader br = null;
			try {
				br = getBufferedReader();
				br.readLine(); //pular cabecalho
				for (int numeroLinha = 2; (linha = br.readLine()) != null; numeroLinha++) {

					LinhaVO l = processarLinha(numeroLinha, Arrays.asList(linha.split(COMMA)));
					if(l != null) {
						linhas.add(l);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new MessageKeyException("erroInesperado.error", e.getMessage());
			} finally {
				fecharBufferedReader(br);
			}
		}

		return linhas;
	}

	private BufferedReader getBufferedReader() throws FileNotFoundException {

		FileInputStream fis = new FileInputStream(file);
		return new BufferedReader(new InputStreamReader(fis, StandardCharsets.ISO_8859_1));
	}

	private void fecharBufferedReader(BufferedReader br) {
		
		if (br != null) {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}