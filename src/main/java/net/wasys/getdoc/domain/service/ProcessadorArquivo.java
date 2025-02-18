package net.wasys.getdoc.domain.service;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONArray;

import com.fasterxml.jackson.databind.SerializationFeature;

import net.wasys.getdoc.domain.vo.LinhaVO;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.ddd.MessageKeyListException;
import net.wasys.util.rest.jackson.ObjectMapper;

public abstract class ProcessadorArquivo {

	private static int TAMANHO_MAXIMO_CHAVE_UNICIDADE = 400;
	private static int TAMANHO_MAXIMO_VALOR = 5000;
	private static int TAMANHO_MAXIMO_NOME_COLUNA = 100;

	protected List<String> colunasUnicidade;
	protected List<String> cabecalho;
	protected File file;
	protected ObjectMapper om = new ObjectMapper();

	ProcessadorArquivo(File file, List<String> colunasUnicidade) {
		this.file = file;
		this.colunasUnicidade = colunasUnicidade;

		om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	}

	public abstract List<String> carregarCabecalho() throws MessageKeyException;
	abstract List<LinhaVO> carregarLinhas() throws MessageKeyException;

	public List<LinhaVO> processar() throws MessageKeyException, MessageKeyListException {

		List<LinhaVO> linhas = null;

		cabecalho = carregarCabecalho();
		validarCabecalho();

		linhas = carregarLinhas();

		validarUnicidade(linhas);

		return linhas;
	}

	LinhaVO processarLinha(int numeroLinha, List<String> colunas) throws MessageKeyException {

		LinhaVO vo = new LinhaVO();

		if(colunas == null || colunas.isEmpty()) {
			return null;
		}

		Map<String, String> colunaValor = new LinkedHashMap<>(colunas.size());

		for (int i = 0; i < cabecalho.size(); i++) {
			String nomeColuna = cabecalho.get(i);
			String valorColuna;
			try {
				valorColuna = colunas.get(i);
			} catch (IndexOutOfBoundsException e) {
				valorColuna = "";
			}
			valorColuna = valorColuna.replaceAll("^\"", "");
			valorColuna = valorColuna.replaceAll("\"$", "");

			validarTamanhoValor(valorColuna, numeroLinha, nomeColuna);

			if(colunasUnicidade.contains(nomeColuna)) {
				String chaveUnicidade = getChaveUnicidadeJson(valorColuna, vo.getChaveUnicidade(), nomeColuna);
				validarTamanhoChaveUnicidade(chaveUnicidade, numeroLinha, nomeColuna);
				vo.setChaveUnicidade(chaveUnicidade);
			}

			colunaValor.put(nomeColuna.trim(), valorColuna.trim());
		}

		vo.setNumeroLinha(numeroLinha);
		vo.setColunaValor(colunaValor);
		return vo;
	}

	private void validarUnicidade(List<LinhaVO> linhas) throws MessageKeyListException {

		List<MessageKeyException> exceptions = new ArrayList<>();

		Map<LinhaVO, Set<Integer>> chavesLinhasDuplicadas = buscarChavesRepetidas(linhas, exceptions);

		if(!chavesLinhasDuplicadas.isEmpty()) {

			for (Entry<LinhaVO, Set<Integer>> entry : chavesLinhasDuplicadas.entrySet()) {

				LinhaVO linha = entry.getKey();
				String chaveUnicidade = linha.getChaveUnicidade();
				chaveUnicidade = chaveUnicidade.replace("[", "");
				chaveUnicidade = chaveUnicidade.replace("]", "");

				Set<Integer> linhasSet = entry.getValue();
				List<Integer> linhasList = new ArrayList<>(linhasSet);
				String linhasStr = DummyUtils.listToString(linhasList);

				exceptions.add(new MessageKeyException("importacaoBase.linhasDuplicadas.error", chaveUnicidade, linhasStr));
			}
		}

		if(!exceptions.isEmpty()) {
			throw new MessageKeyListException(exceptions);
		}
	}

	private Map<LinhaVO, Set<Integer>> buscarChavesRepetidas(List<LinhaVO> linhas, List<MessageKeyException> exceptions) {

		Map<LinhaVO, Set<Integer>> chavesLinhasDuplicadas = new HashMap<>();
		Set<String> setChavesUnicidade = new HashSet<String>();

		for (LinhaVO linha : linhas) {

			String chave = linha.getChaveUnicidade();
			if(isBlank(chave)) {
				int numeroLinha = linha.getNumeroLinha();
				exceptions.add(new MessageKeyException("importacaoBase.colunaUnicidadeVazia.error", numeroLinha));
			}

			if (!setChavesUnicidade.add(chave) && !chavesLinhasDuplicadas.containsKey(linha)) {

				Set<Integer> linhasRepetidas = linhas.stream()
						.filter(l -> l.getChaveUnicidade() != null && l.getChaveUnicidade().equals(chave))
						.map(l -> l.getNumeroLinha())
						.collect(Collectors.toSet());

				chavesLinhasDuplicadas.put(linha, linhasRepetidas);
			}
		}

		return chavesLinhasDuplicadas;
	}

	private String getChaveUnicidadeJson(String valorColuna, String chaveAtualJson, String nomeColuna) throws MessageKeyException {

		String chaveUnicidade = null;
		int indexColunaUnicidade = colunasUnicidade.indexOf(nomeColuna);
		if(isBlank(chaveAtualJson)) {

			JSONArray jsonArray = new JSONArray();
			jsonArray.put(indexColunaUnicidade, valorColuna);
			chaveUnicidade = om.writeValueAsString(jsonArray);
		}
		else {

			JSONArray chaveJson = new JSONArray(chaveAtualJson);
			chaveJson.put(indexColunaUnicidade, valorColuna);
			chaveUnicidade = om.writeValueAsString(chaveJson);
		}

		return chaveUnicidade;
	}

	private void validarTamanhoValor(String valor, int numeroLinha, String cabecalho) throws MessageKeyException {

		if (valor.length() > TAMANHO_MAXIMO_VALOR) {
			throw new MessageKeyException("importacaoBase.tamanhoExcedente.error", numeroLinha, cabecalho, TAMANHO_MAXIMO_VALOR);
		}
	}

	private void validarTamanhoChaveUnicidade(String chave, int numeroLinha, String cabecalho) throws MessageKeyException {

		if(chave.length() > TAMANHO_MAXIMO_CHAVE_UNICIDADE) {
			throw new MessageKeyException("importacaoBase.tamanhoExcedente.error", numeroLinha, cabecalho, TAMANHO_MAXIMO_CHAVE_UNICIDADE, chave);
		}
	}

	private void validarCabecalho() throws MessageKeyException {

		if(cabecalho == null) {
			throw new MessageKeyException("importacaoBase.arquivoVazio.error");
		}

		for (String col : cabecalho) {
			if(col.length() == 0) {
				throw new MessageKeyException("importacaoBase.cabecalhoVazio.error");
			}

			if (col.length() > TAMANHO_MAXIMO_NOME_COLUNA) {
				throw new MessageKeyException("importacaoBase.tamanhoCabecalhoExcedente.error", col, TAMANHO_MAXIMO_NOME_COLUNA);
			}
		}

		for (String colunaUnicidade : colunasUnicidade) {

			if(!cabecalho.contains(colunaUnicidade)) {
				throw new MessageKeyException("importacaoBase.colunaUnicidadeNaoEncontrada.error", colunaUnicidade);
			}
		}
	}
}