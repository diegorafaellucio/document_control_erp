package net.wasys.getdoc.domain.service;

import lombok.extern.slf4j.Slf4j;
import net.wasys.getdoc.GetdocConstants;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.*;
import net.wasys.getdoc.domain.repository.LogAnaliseIARepository;
import net.wasys.getdoc.domain.repository.RelatorioGeralRepository;
import net.wasys.getdoc.domain.service.ParametroService.P;
import net.wasys.getdoc.domain.vo.GrupoSuperiorVO;
import net.wasys.getdoc.domain.vo.LogAnaliseIAVO;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.getdoc.domain.vo.TempoStatusVO;
import net.wasys.getdoc.domain.vo.filtro.*;
import net.wasys.getdoc.domain.vo.filtro.RelatorioGeralFiltro.Tipo;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.excel.ExcelFormat;
import net.wasys.util.excel.ExcelWriter;
import net.wasys.util.other.ExcelCsvWriter;
import net.wasys.util.other.HorasUteisCalculator;
import net.wasys.util.other.HorasUteisCalculator.Expediente;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class LogAnaliseIAService {

	@Autowired private LogAnaliseIARepository logAnaliseIARepository;
	@Autowired private SessionFactory sessionFactory;

	@Transactional(rollbackFor = Exception.class)
	public void saveOrUpdate(LogAnaliseIA logAnaliseIA) throws MessageKeyException {
		logAnaliseIARepository.saveOrUpdate(logAnaliseIA);
	}

	public int countByFiltro(LogAnaliseIAFiltro filtro) {
		return logAnaliseIARepository.countByFiltro(filtro);
	}

	public List<LogAnaliseIA> findByFiltro(LogAnaliseIAFiltro filtro) {
		return logAnaliseIARepository.findByFiltro(filtro);
	}

	public List<LogAnaliseIA> findByFiltro(LogAnaliseIAFiltro filtro, int first, int pageSize) {
		return logAnaliseIARepository.findByFiltro(filtro, first, pageSize);
	}

	public LogAnaliseIA findByProcessoAndDocumento(Processo processo, Documento documento) {
		LogAnaliseIA logAnaliseIA = logAnaliseIARepository.findByProcessoAndDocumento(processo, documento);

		if (logAnaliseIA == null) {
			logAnaliseIA = new LogAnaliseIA();
		}

		return logAnaliseIA;
	}

	public void updateStatusMotivoProcesso(Processo processo, String statusProcesso, String motivoProcesso) {
		logAnaliseIARepository.updateStatusMotivoProcesso(processo, statusProcesso, motivoProcesso);
	}

    public File render(LogAnaliseIAFiltro filtro) {
		try {
			String extensao = GetdocConstants.EXTENSAO_DEFINICAO_XLSX;
			File file = File.createTempFile("relatorio-log-analise-ia", "." + extensao);
			DummyUtils.deleteOnExitFile(file);

			ExcelCsvWriter ecw = new ExcelCsvWriter();

			ExcelWriter ew = new ExcelWriter();
			ew.criarArquivo(extensao);
			Workbook workbook = ew.getWorkbook();
			ExcelFormat ef = new ExcelFormat(workbook);
			ew.setExcelFormat(ef);
			ew.criaPlanilha("LogAnaliseIA");
			ecw.setWriter(ew);

			ecw.criaLinha(0);
			renderHeaderRow(ecw);
			renderRows(ecw, filtro);

			ecw.close(file);

			Session session = sessionFactory.getCurrentSession();
			session.clear();

			return file;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void renderRows(ExcelCsvWriter ecw, LogAnaliseIAFiltro filtro) {

		List<LogAnaliseIAVO> list = logAnaliseIARepository.findToReport(filtro);

		int rowNum = 1;
		for (LogAnaliseIAVO logAnaliseIAVO : list) {
			ecw.criaLinha(rowNum++);
			ecw.escrever(logAnaliseIAVO.getProcessoId());
			ecw.escrever(logAnaliseIAVO.getProcessoNome());
			ecw.escrever(logAnaliseIAVO.getDocumentoId());
			ecw.escrever(logAnaliseIAVO.getDocumentoNome());
			ecw.escrever(DummyUtils.formatDateTime2(logAnaliseIAVO.getData()));
			ecw.escrever(logAnaliseIAVO.getDataDigitalizacao());
			ecw.escrever(logAnaliseIAVO.getStatusDocumento());
			ecw.escrever(logAnaliseIAVO.getMotivoDocumento());
			ecw.escrever(logAnaliseIAVO.getStatusProcesso());
			ecw.escrever(logAnaliseIAVO.getMotivoProcesso());
			ecw.escrever(logAnaliseIAVO.isTipificou() ? "Sim" : "Não");
			ecw.escrever(logAnaliseIAVO.isOcr() ? "Sim" : "Não");
			escreverLinhaMetadados(ecw, logAnaliseIAVO.getMetadados(), "NOME");
			escreverLinhaMetadados(ecw, logAnaliseIAVO.getMetadados(), "CPF");
			ecw.escrever(logAnaliseIAVO.getModeloDocumento());
			ecw.escrever(logAnaliseIAVO.getLabelTipificacao());
			ecw.escrever(logAnaliseIAVO.getScoreTipificacao());
			ecw.escrever(logAnaliseIAVO.getMetadados());
		}

	}

	private void escreverLinhaMetadados(ExcelCsvWriter ecw, String metadados, String nome) {
		Map<String, ?> map = (Map<String, String>) DummyUtils.jsonStringToMap(metadados);
		if(map != null) {
			List<LinkedHashMap<String, ?>> camposOCR = (List<LinkedHashMap<String, ?>>) map.get("camposOCR");

			if (CollectionUtils.isNotEmpty(camposOCR)) {
				for (LinkedHashMap<String, ?> campoOCR : camposOCR) {
					String nomeCampo = (String) campoOCR.get("nomeCampo");
					if (nome.equalsIgnoreCase(nomeCampo)) {
						Object valorOCR = campoOCR.get("valorOCR");
						Object valorProcesso = campoOCR.get("valorProcesso");
						Object percentualValidacao = campoOCR.get("percentualValidacao");

						ecw.escrever(valorOCR);
						ecw.escrever(valorProcesso);
						ecw.escrever(percentualValidacao);

						return;
					}
				}
			}
		}

		ecw.escrever("");
		ecw.escrever("");
		ecw.escrever("");

	}

	private void renderHeaderRow(ExcelCsvWriter ecw) {
		ecw.escrever(LogAnaliseIACampos.ColunasEnum.PROCESSO_ID.getNome());
		ecw.escrever(LogAnaliseIACampos.ColunasEnum.PROCESSO_NOME.getNome());
		ecw.escrever(LogAnaliseIACampos.ColunasEnum.DOCUMENTO_ID.getNome());
		ecw.escrever(LogAnaliseIACampos.ColunasEnum.DOCUMENTO_NOME.getNome());
		ecw.escrever(LogAnaliseIACampos.ColunasEnum.DATA.getNome());
		ecw.escrever(LogAnaliseIACampos.ColunasEnum.DATA_DIGITALIZACAO.getNome());
		ecw.escrever(LogAnaliseIACampos.ColunasEnum.STATUS_DOCUMENTO.getNome());
		ecw.escrever(LogAnaliseIACampos.ColunasEnum.MOTIVO_DOCUMENTO.getNome());
		ecw.escrever(LogAnaliseIACampos.ColunasEnum.STATUS_PROCESSO.getNome());
		ecw.escrever(LogAnaliseIACampos.ColunasEnum.MOTIVO_PROCESSO.getNome());
		ecw.escrever(LogAnaliseIACampos.ColunasEnum.TIPIFICOU.getNome());
		ecw.escrever(LogAnaliseIACampos.ColunasEnum.OCR.getNome());

		ecw.escrever(LogAnaliseIACampos.ColunasEnum.OCR_NOME.getNome());
		ecw.escrever(LogAnaliseIACampos.ColunasEnum.OCR_NOME_PROCESSO.getNome());
		ecw.escrever(LogAnaliseIACampos.ColunasEnum.SCORE_NOME.getNome());
		ecw.escrever(LogAnaliseIACampos.ColunasEnum.OCR_CPF.getNome());
		ecw.escrever(LogAnaliseIACampos.ColunasEnum.OCR_CPF_PROCESSO.getNome());
		ecw.escrever(LogAnaliseIACampos.ColunasEnum.SCORE_CPF.getNome());
		ecw.escrever(LogAnaliseIACampos.ColunasEnum.MODELO_DOCUMENTO.getNome());
		ecw.escrever(LogAnaliseIACampos.ColunasEnum.LABEL_TIPIFICACAO.getNome());
		ecw.escrever(LogAnaliseIACampos.ColunasEnum.SCORE_TIPIFICACAO.getNome());
		ecw.escrever(LogAnaliseIACampos.ColunasEnum.METADADOS.getNome());

	}


	public List<Object[]> findRelatorioLicenciamento(RelatorioLicenciamentoOCRFiltro filtro) {
		return logAnaliseIARepository.findRelatorioLicenciamento(filtro);
	}
}
