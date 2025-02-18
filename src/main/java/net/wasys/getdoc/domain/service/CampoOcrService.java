package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.vo.EditarProcessoVO;
import net.wasys.getdoc.domain.vo.filtro.DocumentoFiltro;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.wasys.getdoc.domain.enumeration.CampoMap;
import net.wasys.getdoc.domain.repository.CampoOcrRepository;
import net.wasys.getdoc.domain.vo.CampoOcrVO;
import net.wasys.getdoc.http.ImagemFilter;
import net.wasys.util.DummyUtils;
import net.wasys.util.ocrws.dto.callback.CampoOcrDTO;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class CampoOcrService {

	private static final List<String> NOMES_CPF = Arrays.asList("_cnpj_cpf_financiado", "_cpf_cnpj", "_cpf", "_cnpj");
	private static final List<String> NOMES_NOMES = Arrays.asList("_nome_financiado", "_nome");

	@Resource(name="resource") private MessageSource resource;
	@Autowired private CampoOcrRepository campoOcrRepository;
	@Autowired private DocumentoService documentoService;
	@Autowired private ProcessoService processoService;
	@Autowired private CampoService campoService;
	@Autowired private ImagemService imagemService;

	public CampoOcr get(Long id) {
		return campoOcrRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(CampoOcr campoOcr) {
		campoOcrRepository.saveOrUpdate(campoOcr);
	}

	public int countByDocumento(Long documentoId) {
		return campoOcrRepository.countByDocumento(documentoId);
	}

	public List<CampoOcr> findByDocumento(Long documentoId) {
		return campoOcrRepository.findByDocumento(documentoId);
	}

	public CampoOcr getByNome(Long processoId, Long documentoId, String documentoNome, String campoNome) {
		return campoOcrRepository.getByNome(processoId, documentoId, documentoNome, campoNome);
	}

	public List<CampoOcr> getValoresTodasFontes(Documento documento, String campoNome) {

		Set<String> campoNomes = getNomeCampoByDocumento(documento, campoNome);
		List<CampoOcr> list = new ArrayList<>();

		CampoOcr ocr = new CampoOcr();
		ocr.setNome(campoNome);
		for (String campoNome2 : campoNomes) {
			String valorProcesso = getValorProcessoByNomeCampo(documento, campoNome2);
			if(StringUtils.isNotBlank(valorProcesso)) {
				ocr.setValor(valorProcesso);
				break;
			}
		}
		list.add(ocr);

		Long processoId = documento.getProcesso().getId();
		ModeloOcr modeloOcr = documento.getModeloOcr();
		if(modeloOcr != null) {
			Long documentoId = documento.getId();
			String nome = documento.getNome();

			for (String campoNome2 : campoNomes) {
				ocr = getByNome(processoId, documentoId, nome, campoNome2);
				if(ocr != null) {
					addCampoOcr(list, ocr);
					break;
				}
			}
		}

		return list;
	}

	private Set<String> getNomeCampoByDocumento(Documento documento, String campoNome) {

		Set<String> campoNomes = new LinkedHashSet<>();
		campoNomes.add(campoNome);

		if(NOMES_CPF.contains(campoNome)) {
			campoNomes.addAll(NOMES_CPF);
		}
		else {
			if(NOMES_NOMES.contains(campoNome)) {
				campoNomes.addAll(NOMES_NOMES);
			}
		}

		return campoNomes;
	}

	public String getValorProcessoByNomeCampo(Documento documento, String campoNome) {

		String valorProcesso = "";
		Processo processo = documento.getProcesso();

		Aluno aluno = processo.getAluno();
		if(isPresent(NOMES_CPF, campoNome)) {
			valorProcesso = aluno.getCpf();
		}
		else if(isPresent(NOMES_NOMES, campoNome)) {
			valorProcesso = aluno.getNome();
		}

		return valorProcesso;
	}

	private boolean isPresent(List<String> lista, String valor) {
		return lista.stream().anyMatch(s -> valor.toLowerCase().contains(s.toLowerCase()));
	}

	private String getCurrency(String valorProcesso) {
		BigDecimal bigDecimal = DummyUtils.stringToCurrency(valorProcesso);
		valorProcesso = DummyUtils.formatCurrency(bigDecimal);
		return "R$ " + valorProcesso;
	}

	private void addCampoOcr(List<CampoOcr> list, CampoOcr ocr) {

		CampoOcr ocr2 = new CampoOcr();
		BeanUtils.copyProperties(ocr, ocr2);
		list.add(ocr2);
	}

	public boolean temOcr(Long documentoId) {
		return campoOcrRepository.temOcr(documentoId);
	}

	public boolean temInconsistenciaByDocumento(Long documentoId) {

		Documento documento = documentoService.get(documentoId);
		ModeloOcr modeloOcr = documento.getModeloOcr();
		if(modeloOcr == null) {
			return false;
		}

		List<CampoOcr> campos = findByDocumento(documentoId);
		for (CampoOcr ocr : campos) {

			boolean temInconsistencia = temInconsistencia(ocr);
			if(temInconsistencia) {
				return true;
			}
		}

		return false;
	}

	public int countInconsistenciasByDocumento(Long documentoId) {

		int count = 0;

		Documento documento = documentoService.get(documentoId);
		ModeloOcr modeloOcr = documento.getModeloOcr();
		if(modeloOcr == null) {
			return 0;
		}

		List<CampoOcr> campos = findByDocumento(documentoId);
		for (CampoOcr ocr : campos) {

			boolean temInconsistencia = temInconsistencia(ocr);
			if(temInconsistencia) {
				count++;;
			}
		}

		return count;
	}

	public boolean temInconsistencia(CampoOcr ocr) {
		return false;
	}

	private boolean isNomeFinanciado(CampoOcr ocr) {

		String nomeCampo = ocr.getNome();

		if("_nome_financiado".equals(nomeCampo)) {
			return true;
		}

		Imagem imagem = ocr.getImagem();
		Documento documento = imagem.getDocumento();
		String nomeDocumento = documento.getNome();

		if("CNH".equals(nomeDocumento) || "RG".equals(nomeDocumento) || "CPF".equals(nomeDocumento)) {

			if("_nome".equals(nomeCampo)) {
				return true;
			}
		}

		return false;
	}

	public boolean temNaoChecadoByProcesso(Long processoId) {
		return campoOcrRepository.temNaoChecadoByProcesso(processoId);
	}

	public boolean temNaoChecadoByDocumento(Long documentoId) {
		return campoOcrRepository.temNaoChecadoByDocumento(documentoId);
	}

	public void atualizarEstatisticas(Documento documento) {

		Long documentoId = documento.getId();
		List<CampoOcr> imagensOcr = campoOcrRepository.findByDocumento(documentoId);

		for (CampoOcr campoOcr : imagensOcr) {

			atualizarEstatisticas(documento, campoOcr);

			campoOcrRepository.saveOrUpdate(campoOcr);
		}
	}

	public void atualizarEstatisticas(Documento documento, CampoOcr campoOcr) {

		String nome = campoOcr.getNome();
		String valorProcesso = getValorProcessoByNomeCampo(documento, nome);

		if(StringUtils.isNotBlank(valorProcesso)) {

			String valorFinal = campoOcr.getValorFinal();

			valorFinal = DummyUtils.substituirCaracteresEspeciais(valorFinal);
			valorProcesso = DummyUtils.substituirCaracteresEspeciais(valorProcesso);

			boolean iguais = StringUtils.equals(valorFinal, valorProcesso);
			campoOcr.setValorIgualProcesso(iguais);

			double similaridade = DummyUtils.getSimilaridade(valorProcesso, valorFinal);
			BigDecimal similaridadeBD = new BigDecimal(String.valueOf(similaridade));
			similaridadeBD = similaridadeBD.multiply(new BigDecimal("100"));
			similaridadeBD = similaridadeBD.setScale(0, RoundingMode.HALF_UP);
			campoOcr.setSimilaridade(similaridadeBD.intValue());
		}
	}

	public List<CampoOcrVO> getCampoOcrVoList(Documento documento, List<CampoOcr> list, String contextPath) {

		List<CampoOcrVO> voList = new ArrayList<CampoOcrVO>();
		for (CampoOcr ocr : list) {

			String nome = ocr.getNome();

			CampoOcrVO vo = new CampoOcrVO();
			BeanUtils.copyProperties(ocr, vo);

			List<CampoOcr> todasFontes = getValoresTodasFontes(documento, nome);
			String valorFinalBaseComparacao = null;

			for (CampoOcr ocr2 : todasFontes) {

				valorFinalBaseComparacao = StringUtils.isNotBlank(valorFinalBaseComparacao) ? valorFinalBaseComparacao : ocr2.getValorFinal();

				CampoOcrVO vo2 = new CampoOcrVO();
				BeanUtils.copyProperties(ocr2, vo2);

				vo.addComparativo(vo2, valorFinalBaseComparacao);
			}

			voList.add(vo);
		}

		for (int i = 0; i < voList.size(); i++) {

			CampoOcrVO vo = voList.get(i);

			if(i > 0) {
				CampoOcrVO anterior = voList.get(i - 1);
				vo.setAnterior(anterior);
			}

			if(i < voList.size() - 1) {
				CampoOcrVO proximo = voList.get(i + 1);
				vo.setProximo(proximo);
			}
		}

		return voList;
	}

	@Transactional(rollbackFor=Exception.class)
	public void preencheProcesso(Long processoId, Long documentoId, Usuario usuario) throws Exception {

		Map<Long, String> valores = new LinkedHashMap<>();
		Map<String, String> valoresOcr = new LinkedHashMap<>();

		List<CampoOcr> camposOcr = campoOcrRepository.findByDocumento(documentoId);
		for (CampoOcr campoOcr : camposOcr) {
			String nome = campoOcr.getNome();
			String nome2 = DummyUtils.substituirCaracteresEspeciais(nome);
			nome2 = nome2.replace("_", " ");
			nome2 = nome2.trim();
			nome2 = nome2.toLowerCase();
			String valorFinal = campoOcr.getValorFinal();

			valoresOcr.put(nome2, valorFinal);

			if("_cnpj_cpf".equals(nome) || "_cpf_financiado".equals(nome) || "_cnpj_cpf_financiado".equals(nome) || "_cpf".equals(nome)) {

				valoresOcr.put("cnpj cpf", valorFinal);
				valoresOcr.put("cpf financiado", valorFinal);
				valoresOcr.put("cnpj cpf financiado", valorFinal);
				valoresOcr.put("cpf", valorFinal);
				valoresOcr.put("cpf/cnpj", valorFinal);
				valoresOcr.put("cpf / cnpj", valorFinal);
			}
			else if("_nome".equals(nome) || "_nome_financiado".equals(nome)) {
				valoresOcr.put("nome", valorFinal);
				valoresOcr.put("nome financiado", valorFinal);
			}
		}

		systraceThread("processoId: " + processoId + ", valoresOcr: " + valoresOcr);

		List<Campo> campos = campoService.findByProcesso(processoId);
		for (Campo campo : campos) {

			Long campoId = campo.getId();
			String nome = campo.getNome();
			String valorFim = campo.getValor();

			nome = DummyUtils.substituirCaracteresEspeciais(nome);
			nome = nome.trim();
			nome = nome.toLowerCase();
			nome = nome.replace("-", "");

			String valor = valoresOcr.get(nome);
			if(StringUtils.isNotBlank(valor)) {
				systraceThread("processoId: " + processoId + ", campo " + nome + ": " + valor);
				valorFim = valor;;
			}

			valores.put(campoId, valorFim);
		}

		systraceThread("processoId: " + processoId + ", valores: " + valores);
		if(!valores.isEmpty()) {
			EditarProcessoVO editarProcessoVO = new EditarProcessoVO();
			editarProcessoVO.setProcessoId(processoId);
			editarProcessoVO.setUsuario(usuario);
			editarProcessoVO.setValores(valores);
			editarProcessoVO.setValidarCampos(false);
			processoService.atualizarProcesso(editarProcessoVO);
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public void criarCampos(Long imagemId, CampoOcrDTO[] camposOcr) {

		campoOcrRepository.deleteFromImagem(imagemId);

		Imagem imagem = imagemService.get(imagemId);
		Documento documento = imagem.getDocumento();

		for (CampoOcrDTO dto : camposOcr) {

			String blockRef = dto.getBlockRef();
			String errorRef = dto.getErrorRef();
			String erros = dto.getErros();
			String fieldId = dto.getFieldId();
			Integer nivelConfianca = dto.getNivelConfianca();
			String nome = dto.getNome();
			int positionBottom = dto.getPositionBottom();
			int positionLeft = dto.getPositionLeft();
			int positionRight = dto.getPositionRight();
			int positionTop = dto.getPositionTop();
			String recognizedValue = dto.getRecognizedValue();
			String suspiciousSymbols = dto.getSuspiciousSymbols();
			String valor = dto.getValor();
			valor = valor != null ? valor : "";

			CampoOcr campoOcr = new CampoOcr();
			campoOcr.setImagem(imagem);
			campoOcr.setBlockRef(blockRef);
			campoOcr.setErrorRef(errorRef);
			campoOcr.setErros(erros);
			campoOcr.setFieldId(fieldId);
			campoOcr.setNivelConfianca(nivelConfianca);
			campoOcr.setNome(nome);
			campoOcr.setPositionBottom(positionBottom);
			campoOcr.setPositionLeft(positionLeft);
			campoOcr.setPositionRight(positionRight);
			campoOcr.setPositionTop(positionTop);
			campoOcr.setRecognizedValue(recognizedValue);
			campoOcr.setSuspiciousSymbols(suspiciousSymbols);
			campoOcr.setValor(valor);

			atualizarEstatisticas(documento, campoOcr);

			campoOcrRepository.saveOrUpdate(campoOcr);
		}
	}
}
