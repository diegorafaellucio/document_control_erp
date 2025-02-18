package net.wasys.getdoc.domain.service;

import com.google.common.base.Stopwatch;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.*;
import net.wasys.getdoc.domain.repository.RelatorioPendenciaDocumentoBiRepository;
import net.wasys.getdoc.domain.repository.RelatorioPendenciaDocumentoRepository;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.getdoc.domain.vo.RelatorioPendenciaDocumentoVO;
import net.wasys.getdoc.domain.vo.filtro.RelatorioPendenciaDocumentoFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.excel.ExcelFormat;
import net.wasys.util.excel.ExcelWriter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class RelatorioPendenciaDocumentoService {

	@Autowired private RelatorioPendenciaDocumentoRepository relatorioPendenciaDocumentoRepository;
	@Autowired private RelatorioPendenciaDocumentoBiRepository relatorioPendenciaDocumentoBiRepository;
	@Autowired private RelatorioGeralService relatorioGeralService;
	@Autowired private ResourceService resourceService;
	@Autowired private SessionFactory sessionFactory;
	@Autowired private BaseRegistroService baseRegistroService;

	public File render(RelatorioPendenciaDocumentoFiltro filtro, List<RelatorioPendenciaDocumentoVO> relatorioPendenciaDocumentoVOS, int formatoArquivo) {
		try {

			Long tempoBuscaRegistrosMs = null;

			if(relatorioPendenciaDocumentoVOS == null || relatorioPendenciaDocumentoVOS.isEmpty()){

				Stopwatch inicioBuscaRegistros = Stopwatch.createStarted();

				List<RelatorioPendenciaDocumentoVO> list = findRelatorio(filtro);
				relatorioPendenciaDocumentoVOS.addAll(list);

				tempoBuscaRegistrosMs = inicioBuscaRegistros.elapsed(MILLISECONDS);
			}

			if(RelatorioPendenciaDocumentoFiltro.CSV == formatoArquivo) {
				return renderCsv(filtro, relatorioPendenciaDocumentoVOS, tempoBuscaRegistrosMs);
			}
			 else {
				return renderExcel(filtro, relatorioPendenciaDocumentoVOS, tempoBuscaRegistrosMs);
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private File renderExcel(RelatorioPendenciaDocumentoFiltro filtro, List<RelatorioPendenciaDocumentoVO> relatorioPendenciaDocumentoVOS, Long tempoBuscaRegistrosMs) throws Exception {
		String fileNome = "relatorio-pendencia-documentos-aluno.xlsx";
		File fileOrigem = DummyUtils.getFileFromResource("/net/wasys/getdoc/excel/" + fileNome);

		File file = File.createTempFile("relatorio-pendencia-documentos-", ".xlsx");
		DummyUtils.deleteOnExitFile(file);
		FileUtils.copyFile(fileOrigem, file);

		ExcelWriter ew = new ExcelWriter();
		ew.abrirArquivo(file);
		Workbook workbook = ew.getWorkbook();
		ExcelFormat ef = new ExcelFormat(workbook);
		ew.setExcelFormat(ef);

		if(workbook.getNumberOfSheets() == 1){
			workbook.removeSheetAt(0);
		}
		Sheet sheet = workbook.createSheet("Documentos Faltantes");

		ew.criaLinha(sheet, 0);
		boolean sisFiesOuSisProuni = filtro.isSisFiesOuSisProuni();
		renderCabecalho(null, ew, sheet, sisFiesOuSisProuni);
		if(sisFiesOuSisProuni) {
			renderInformacoesRelatorioSisFiesSisProuni(relatorioPendenciaDocumentoVOS, null, ew, sheet);
		}
		else {
			renderInformacoesRelatorio(relatorioPendenciaDocumentoVOS, null, ew, sheet);
		}

		Stopwatch inicioEscritaLinhas = Stopwatch.createStarted();

		long tempoEscritaLinhasMs = inicioEscritaLinhas.elapsed(MILLISECONDS);

		DummyUtils.deleteFile(file);
		File fileDestino = File.createTempFile("relatorio-pendencia-documentos-aluno", ".xlsx");
		DummyUtils.deleteOnExitFile(fileDestino);

		Stopwatch inicioEscritaArquivo = Stopwatch.createStarted();

		FileOutputStream fos = new FileOutputStream(fileDestino);
		workbook.write(fos);
		workbook.close();

		long tempoEscritaArquivoMs = inicioEscritaArquivo.elapsed(MILLISECONDS);

		MetricasGeracaoRelatorio metricas = new MetricasGeracaoRelatorio(filtro, relatorioPendenciaDocumentoVOS);
		metricas.putTempoBuscaRegistrosMs(tempoBuscaRegistrosMs);
		metricas.putTempoEscritaLinhasMs(tempoEscritaLinhasMs);
		metricas.putTempoEscritaArquivoMs(tempoEscritaArquivoMs);
		metricas.printar();

		return fileDestino;
	}

	private File renderCsv(RelatorioPendenciaDocumentoFiltro filtro, List<RelatorioPendenciaDocumentoVO> relatorioPendenciaDocumentoVOS, Long tempoBuscaRegistrosMs) throws Exception {

		File file = File.createTempFile("relatorio-pendencia-documentos", ".csv");
		String path = file.getAbsolutePath();
		DummyUtils.deleteFile(file);

		BufferedWriter writer = Files.newBufferedWriter(Paths.get(path));
		CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
				.withDelimiter(';')
				.withAllowMissingColumnNames()
				.withTrim()
		);

		boolean sisFiesOuSisProuni = filtro.isSisFiesOuSisProuni();
		renderCabecalho(csvPrinter, null, null, sisFiesOuSisProuni);
		if(sisFiesOuSisProuni) {
			renderInformacoesRelatorioSisFiesSisProuni(relatorioPendenciaDocumentoVOS, csvPrinter, null, null);
		}
		else {
			renderInformacoesRelatorio(relatorioPendenciaDocumentoVOS, csvPrinter, null, null);
		}

		csvPrinter.flush();

		file = new File(path);
		DummyUtils.deleteOnExitFile(file);

		Stopwatch inicioEscritaLinhas = Stopwatch.createStarted();

		long tempoEscritaLinhasMs = inicioEscritaLinhas.elapsed(MILLISECONDS);
		Stopwatch inicioEscritaArquivo = Stopwatch.createStarted();
		long tempoEscritaArquivoMs = inicioEscritaArquivo.elapsed(MILLISECONDS);

		MetricasGeracaoRelatorio metricas = new MetricasGeracaoRelatorio(filtro, relatorioPendenciaDocumentoVOS);
		metricas.putTempoBuscaRegistrosMs(tempoBuscaRegistrosMs);
		metricas.putTempoEscritaLinhasMs(tempoEscritaLinhasMs);
		metricas.putTempoEscritaArquivoMs(tempoEscritaArquivoMs);
		metricas.printar();

		return file;
	}

	private void renderInformacoesRelatorio(List<RelatorioPendenciaDocumentoVO> vos, CSVPrinter csvPrinter, ExcelWriter ew, Sheet sheet) throws IOException {
		int rowNum = 1;

		Map<Long, Map<String, RegistroValorVO>> map = geraRegistrosDeBaseInterna();

		for (RelatorioPendenciaDocumentoVO vo : vos) {

			if (rowNum % 10000 == 0) {
				systraceThread("Criando linha " + rowNum + " de " + vos.size());
				Session session = sessionFactory.getCurrentSession();
				session.clear();
			}

			Map<String, String> grupoDadosDoCurso = getValoresDosCamposDoProcesso(vo, CampoMap.GrupoEnum.DADOS_DO_INSCRITO.getNome());

			if (ew != null) ew.criaLinha(sheet, rowNum++);
			List<String> linha = new ArrayList<>();

			long processoId = vo.getProcessoId();
			String processoIdString = prepararValorCsv(processoId);
			escreverNaCelula(csvPrinter, ew, linha, processoIdString);

			String nomeSituacao = vo.getNomeSituacao();
			nomeSituacao = prepararValorCsv(nomeSituacao);
			escreverNaCelula(csvPrinter, ew, linha, nomeSituacao);

			String nomeRegional = getCampoGrupoValor(map, grupoDadosDoCurso, CampoMap.CampoEnum.REGIONAL.getNome(), BaseInterna.REGIONAL_ID);
			nomeRegional = prepararValorCsv(nomeRegional);
			escreverNaCelula(csvPrinter, ew, linha, nomeRegional);
			vo.setNomeRegional(nomeRegional);

			String codInstituicao = grupoDadosDoCurso.get(CampoMap.CampoEnum.INSTITUICAO.getNome());
			codInstituicao = prepararValorCsv(codInstituicao);
			codInstituicao = DummyUtils.limparCharsChaveUnicidade(codInstituicao);
			escreverNaCelula(csvPrinter, ew, linha, codInstituicao);

			String nomeInstituicao = getCampoGrupoValor(map, grupoDadosDoCurso, CampoMap.CampoEnum.INSTITUICAO.getNome(), BaseInterna.INSTITUICAO_ID);
			nomeInstituicao = prepararValorCsv(nomeInstituicao);
			escreverNaCelula(csvPrinter, ew, linha, nomeInstituicao);

			String codCampus = grupoDadosDoCurso.get(CampoMap.CampoEnum.CAMPUS.getNome());
			codCampus = prepararValorCsv(codCampus);
			codCampus = DummyUtils.limparCharsChaveUnicidade(codCampus);
			escreverNaCelula(csvPrinter, ew, linha, codCampus);

			String nomeCampus = getCampoGrupoValor(map, grupoDadosDoCurso, CampoMap.CampoEnum.CAMPUS.getNome(), BaseInterna.CAMPUS_ID);
			nomeCampus = prepararValorCsv(nomeCampus);
			escreverNaCelula(csvPrinter, ew, linha, nomeCampus);

			RegistroValorVO baseRegistroValor = getBaseRegistroValor(map, grupoDadosDoCurso, CampoMap.CampoEnum.CAMPUS.getNome(), BaseInterna.CAMPUS_ID);
			Map<String, BaseRegistroValor> mapColunaRegistroValor = baseRegistroValor != null ? baseRegistroValor.getMapColunaRegistroValor() : null;
			BaseRegistroValor baseRegistroValor1 = mapColunaRegistroValor != null ? mapColunaRegistroValor.get(BaseRegistro.POLO_PARCEIRO) : null;
			String poloParceiro = baseRegistroValor1 != null ? baseRegistroValor1.getValor() : null;
			poloParceiro = prepararValorCsv(poloParceiro);
			escreverNaCelula(csvPrinter, ew, linha, poloParceiro);

			String codCurso = grupoDadosDoCurso.get(CampoMap.CampoEnum.CURSO.getNome());
			codCurso = prepararValorCsv(codCurso);
			codCurso = DummyUtils.limparCharsChaveUnicidade(codCurso);
			escreverNaCelula(csvPrinter, ew, linha, codCurso);

			String nomeCurso = getCampoGrupoValor(map, grupoDadosDoCurso, CampoMap.CampoEnum.CURSO.getNome(), BaseInterna.CURSO_ID);
			nomeCurso = prepararValorCsv(nomeCurso);
			escreverNaCelula(csvPrinter, ew, linha, nomeCurso);

			String formaIngresso = getCampoGrupoValor(map, grupoDadosDoCurso, CampoMap.CampoEnum.FORMA_DE_INGRESSO.getNome(), BaseInterna.FORMA_INGRESSO_ID);
			formaIngresso = prepararValorCsv(formaIngresso);
			escreverNaCelula(csvPrinter, ew, linha, formaIngresso);

			String modalidadeEnsino = grupoDadosDoCurso.get(CampoMap.CampoEnum.MODALIDADE_ENSINO.getNome());
			modalidadeEnsino = prepararValorCsv(modalidadeEnsino);
			modalidadeEnsino = DummyUtils.limparCharsChaveUnicidade(modalidadeEnsino);
			escreverNaCelula(csvPrinter, ew, linha, modalidadeEnsino);

			String numInscricao = grupoDadosDoCurso.get(CampoMap.CampoEnum.NUM_INSCRICAO.getNome());
			numInscricao = prepararValorCsv(numInscricao);
			escreverNaCelula(csvPrinter, ew, linha, numInscricao);

			String numCandidato = grupoDadosDoCurso.get(CampoMap.CampoEnum.NUM_CANDIDATO.getNome());
			numCandidato = prepararValorCsv(numCandidato);
			escreverNaCelula(csvPrinter, ew, linha, numCandidato);

			String matricula = grupoDadosDoCurso.get(CampoMap.CampoEnum.MATRICULA.getNome());
			matricula = prepararValorCsv(matricula);
			escreverNaCelula(csvPrinter, ew, linha, matricula);

			String origem = vo.getOrigem();
			origem = prepararValorCsv(origem);
			escreverNaCelula(csvPrinter, ew, linha, origem);

			String tipoCurso = grupoDadosDoCurso.get(CampoMap.CampoEnum.TIPO_CURSO.getNome());
			tipoCurso = prepararValorCsv(tipoCurso);
			tipoCurso = DummyUtils.limparCharsChaveUnicidade(tipoCurso);
			escreverNaCelula(csvPrinter, ew, linha, tipoCurso);

			String periodoIngresso = grupoDadosDoCurso.get(CampoMap.CampoEnum.PERIODO_DE_INGRESSO.getNome());
			periodoIngresso = prepararValorCsv(periodoIngresso);
			escreverNaCelula(csvPrinter, ew, linha, periodoIngresso);

			String turno = grupoDadosDoCurso.get(CampoMap.CampoEnum.TURNO.getNome());
			turno = prepararValorCsv(turno);
			escreverNaCelula(csvPrinter, ew, linha, turno);

			String nomeAluno = vo.getNomeAluno();
			nomeAluno = prepararValorCsv(nomeAluno);
			escreverNaCelula(csvPrinter, ew, linha, nomeAluno);

			String cpf = vo.getCpf();
			cpf = prepararValorCsv(cpf);
			escreverNaCelula(csvPrinter, ew, linha, cpf);

			String email = grupoDadosDoCurso.get(CampoMap.CampoEnum.EMAIL.getNome());
			email = prepararValorCsv(email);
			escreverNaCelula(csvPrinter, ew, linha, email);

			String telefone = grupoDadosDoCurso.get(CampoMap.CampoEnum.TELEFONE.getNome());
			telefone = prepararValorCsv(telefone);
			escreverNaCelula(csvPrinter, ew, linha, telefone);

			String nomeDocumento = vo.getNomeDocumento();
			nomeDocumento = prepararValorCsv(nomeDocumento);
			escreverNaCelula(csvPrinter, ew, linha, nomeDocumento);

			String statusDocumento = vo.getStatusDocumento();
			String statusDocumentoStr = resourceService.getValue("StatusDocumento." + statusDocumento + ".label");
			statusDocumentoStr = prepararValorCsv(statusDocumentoStr);
			escreverNaCelula(csvPrinter, ew, linha, statusDocumentoStr);

			String observacaoPendencia = vo.getObservacaoPendencia();
			observacaoPendencia = prepararValorCsv(observacaoPendencia);
			escreverNaCelula(csvPrinter, ew, linha, observacaoPendencia);

			long totalImagens = vo.getTotalImagens();
			String totalImagensStr = prepararValorCsv(totalImagens);
			escreverNaCelula(csvPrinter, ew, linha, totalImagensStr);

			String dataEnvioAnalise = DummyUtils.formatDateTime2(vo.getDataEnvioAnalise());
			dataEnvioAnalise = prepararValorCsv(dataEnvioAnalise);
			escreverNaCelula(csvPrinter, ew, linha, dataEnvioAnalise);

			String dataFimAnalise = DummyUtils.formatDateTime2(vo.getDataFimAnalise());
			dataFimAnalise = prepararValorCsv(dataFimAnalise);
			escreverNaCelula(csvPrinter, ew, linha, dataFimAnalise);

			long totalVezesEmAnalise = vo.getTotalVezesEmAnalise();
			String totalVezesEmAnaliseStr = prepararValorCsv(totalVezesEmAnalise);
			escreverNaCelula(csvPrinter, ew, linha, totalVezesEmAnaliseStr);

			String irregularidade = vo.getIrregularidade();
			irregularidade = prepararValorCsv(irregularidade);
			escreverNaCelula(csvPrinter, ew, linha, irregularidade);

			String obrigatorio = vo.isObrigatorio() ? "Sim" : "Não";
			obrigatorio = prepararValorCsv(obrigatorio);
			escreverNaCelula(csvPrinter, ew, linha, obrigatorio);

			String tipoProcesso = vo.getTipoProcesso();
			tipoProcesso = prepararValorCsv(tipoProcesso);
			escreverNaCelula(csvPrinter, ew, linha, tipoProcesso);

			String pastaAmarela = (vo.getPastaAmarela() ? "Sim" : "Não");
			pastaAmarela = prepararValorCsv(pastaAmarela);
			escreverNaCelula(csvPrinter, ew, linha, pastaAmarela);

			String pastaVermelha = (vo.getPastaVermelha() ? "Sim" : "Não");
			pastaVermelha = prepararValorCsv(pastaVermelha);
			escreverNaCelula(csvPrinter, ew, linha, pastaVermelha);

			Integer versaoAtual = vo.getVersaoAtual();
			String versaoAtualStr = prepararValorCsv(versaoAtual != null ? versaoAtual.toString() : "");
			escreverNaCelula(csvPrinter, ew, linha, versaoAtualStr);

			String modeloDocumento = vo.getModeloDocumento();
			modeloDocumento = prepararValorCsv(modeloDocumento);
			escreverNaCelula(csvPrinter, ew, linha, modeloDocumento);

			Boolean usaTermo = vo.getUsaTermo();
			String usaTermoStr = usaTermo != null ? (usaTermo ? "Sim" : "Não") : "Não";
			usaTermoStr = prepararValorCsv(usaTermoStr);
			escreverNaCelula(csvPrinter, ew, linha, usaTermoStr);

			String origemDocumentoString = (String.valueOf(vo.getOrigemDocumento()));
			origemDocumentoString = prepararValorCsv(origemDocumentoString != "null" ? origemDocumentoString : " ");
			escreverNaCelula(csvPrinter, ew, linha, origemDocumentoString);

			if (csvPrinter != null) {
				csvPrinter.printRecord(linha);
			}
		}
	}

	private Map<String, String> getValoresDosCamposDoProcesso(RelatorioPendenciaDocumentoVO vo, String grupo) {
		Map<String, String> grupoMap = new HashMap<>();
		String camposDinamicos = vo.getCamposDinamicos();
		long processoId = vo.getProcessoId();
		if(StringUtils.isBlank(camposDinamicos)) {
			RelatorioGeral relatorioGeral = relatorioGeralService.getByProcesso(processoId);
			camposDinamicos = relatorioGeral != null ? relatorioGeral.getCamposDinamicos() : "";
			if(StringUtils.isBlank(camposDinamicos)) {
				relatorioGeralService.atualizarRelatorioGeral(Arrays.asList(processoId));
				relatorioGeral = relatorioGeralService.getByProcesso(processoId);
				camposDinamicos = relatorioGeral != null ? relatorioGeral.getCamposDinamicos() : "";
			}
		}

		if(StringUtils.isNotBlank(camposDinamicos)) {
			Map<String, Map<String, String>> mapCamposDinamicos = (Map<String, Map<String, String>>) DummyUtils.jsonStringToMap(camposDinamicos);
			grupoMap = mapCamposDinamicos.get(grupo);
		}

		return grupoMap;
	}

	private void renderInformacoesRelatorioSisFiesSisProuni(List<RelatorioPendenciaDocumentoVO> vos, CSVPrinter csvPrinter, ExcelWriter ew, Sheet sheet) throws IOException {
		int rowNum = 1;

		Map<Long, Map<String, RegistroValorVO>> map = geraRegistrosDeBaseInterna();

		for (RelatorioPendenciaDocumentoVO vo : vos) {

			if (rowNum % 10000 == 0) {
				systraceThread("Criando linha " + rowNum + " de " + vos.size());
				Session session = sessionFactory.getCurrentSession();
				session.clear();
			}

			if (ew != null) ew.criaLinha(sheet, rowNum++);
			List<String> linha = new ArrayList<>();

			Map<String, String> grupoDadosDoCurso = getValoresDosCamposDoProcesso(vo, CampoMap.GrupoEnum.DADOS_DO_INSCRITO.getNome());
			Map<String, String> grupoDadosDeImportacao = getValoresDosCamposDoProcesso(vo, CampoMap.GrupoEnum.DADOS_DE_IMPORTACAO.getNome());

			long processoId = vo.getProcessoId();
			String processoIdString = prepararValorCsv(processoId);
			escreverNaCelula(csvPrinter, ew, linha, processoIdString);

			String dataCriacaoProcesso = DummyUtils.formatDate(vo.getDataCriacaoProcesso());
			dataCriacaoProcesso = prepararValorCsv(dataCriacaoProcesso);
			escreverNaCelula(csvPrinter, ew, linha, dataCriacaoProcesso);

			Long documentoId = vo.getDocumentoId();
			String documentoIdStr = prepararValorCsv(documentoId);
			escreverNaCelula(csvPrinter, ew, linha, documentoIdStr);

			String nomeSituacao = vo.getNomeSituacao();
			nomeSituacao = prepararValorCsv(nomeSituacao);
			escreverNaCelula(csvPrinter, ew, linha, nomeSituacao);

			String nomeRegional = getCampoGrupoValor(map, grupoDadosDoCurso, CampoMap.CampoEnum.REGIONAL.getNome(), BaseInterna.REGIONAL_ID);
			nomeRegional = prepararValorCsv(nomeRegional);
			escreverNaCelula(csvPrinter, ew, linha, nomeRegional);
			vo.setNomeRegional(nomeRegional);

			String codInstituicao = grupoDadosDoCurso.get(CampoMap.CampoEnum.INSTITUICAO.getNome());
			codInstituicao = prepararValorCsv(codInstituicao);
			codInstituicao = DummyUtils.limparCharsChaveUnicidade(codInstituicao);
			escreverNaCelula(csvPrinter, ew, linha, codInstituicao);

			String nomeInstituicao = getCampoGrupoValor(map, grupoDadosDoCurso, CampoMap.CampoEnum.INSTITUICAO.getNome(), BaseInterna.INSTITUICAO_ID);
			nomeInstituicao = prepararValorCsv(nomeInstituicao);
			escreverNaCelula(csvPrinter, ew, linha, nomeInstituicao);

			String codCampus = grupoDadosDoCurso.get(CampoMap.CampoEnum.CAMPUS.getNome());
			codCampus = prepararValorCsv(codCampus);
			codCampus = DummyUtils.limparCharsChaveUnicidade(codCampus);
			escreverNaCelula(csvPrinter, ew, linha, codCampus);

			String nomeCampus = getCampoGrupoValor(map, grupoDadosDoCurso, CampoMap.CampoEnum.CAMPUS.getNome(), BaseInterna.CAMPUS_ID);
			nomeCampus = prepararValorCsv(nomeCampus);
			escreverNaCelula(csvPrinter, ew, linha, nomeCampus);

			String codCurso = grupoDadosDoCurso.get(CampoMap.CampoEnum.CURSO.getNome());
			codCurso = prepararValorCsv(codCurso);
			codCurso = DummyUtils.limparCharsChaveUnicidade(codCurso);
			escreverNaCelula(csvPrinter, ew, linha, codCurso);

			String nomeCurso = getCampoGrupoValor(map, grupoDadosDoCurso, CampoMap.CampoEnum.CURSO.getNome(), BaseInterna.CURSO_ID);
			nomeCurso = prepararValorCsv(nomeCurso);
			escreverNaCelula(csvPrinter, ew, linha, nomeCurso);

			String nomeAluno = vo.getNomeAluno();
			nomeAluno = prepararValorCsv(nomeAluno);
			escreverNaCelula(csvPrinter, ew, linha, nomeAluno);

			String cpf = vo.getCpf();
			cpf = prepararValorCsv(cpf);
			escreverNaCelula(csvPrinter, ew, linha, cpf);

			String nomeCandidato = grupoDadosDeImportacao != null ? grupoDadosDeImportacao.get(CampoMap.CampoEnum.NOME_DO_CANDIDATO.getNome()) : "";
			nomeCandidato = prepararValorCsv(nomeCandidato);
			escreverNaCelula(csvPrinter, ew, linha, nomeCandidato);

			String numCandidato = grupoDadosDoCurso.get(CampoMap.CampoEnum.NUM_CANDIDATO.getNome());
			numCandidato = prepararValorCsv(numCandidato);
			escreverNaCelula(csvPrinter, ew, linha, numCandidato);

			String numInscricao = grupoDadosDoCurso.get(CampoMap.CampoEnum.NUM_INSCRICAO.getNome());
			numInscricao = prepararValorCsv(numInscricao);
			escreverNaCelula(csvPrinter, ew, linha, numInscricao);

			String matricula = grupoDadosDoCurso.get(CampoMap.CampoEnum.MATRICULA.getNome());
			matricula = prepararValorCsv(matricula);
			escreverNaCelula(csvPrinter, ew, linha, matricula);

			String localDeOferta = grupoDadosDeImportacao != null ? grupoDadosDeImportacao.get(CampoMap.CampoEnum.LOCAL_DE_OFERTA.getNome()) : "";
			localDeOferta = prepararValorCsv(localDeOferta);
			escreverNaCelula(csvPrinter, ew, linha, localDeOferta);

			String chamada = grupoDadosDeImportacao != null ? grupoDadosDeImportacao.get(CampoMap.CampoEnum.NUMERO_CHAMADA.getNome()) : "";
			chamada = prepararValorCsv(chamada);
			escreverNaCelula(csvPrinter, ew, linha, chamada);

			String formaIngresso = getCampoGrupoValor(map, grupoDadosDoCurso, CampoMap.CampoEnum.FORMA_DE_INGRESSO.getNome(), BaseInterna.FORMA_INGRESSO_ID);
			formaIngresso = prepararValorCsv(formaIngresso);
			escreverNaCelula(csvPrinter, ew, linha, formaIngresso);

			String periodoIngresso = grupoDadosDoCurso.get(CampoMap.CampoEnum.PERIODO_DE_INGRESSO.getNome());
			periodoIngresso = prepararValorCsv(periodoIngresso);
			escreverNaCelula(csvPrinter, ew, linha, periodoIngresso);

			String tipoProcesso = vo.getTipoProcesso();
			tipoProcesso = prepararValorCsv(tipoProcesso);
			escreverNaCelula(csvPrinter, ew, linha, tipoProcesso);

			String numeroMembro = vo.getNumeroMembro();
			numeroMembro = prepararValorCsv(numeroMembro);
			numeroMembro = StringUtils.substringBetween(numeroMembro, "(", ")");
			numeroMembro = StringUtils.isNotBlank(numeroMembro) ? "(".concat(numeroMembro).concat(")") : "";

			String nomeDocumento = vo.getNomeDocumento();
			nomeDocumento = prepararValorCsv(nomeDocumento);
			nomeDocumento = StringUtils.isNotBlank(numeroMembro) ? nomeDocumento.replace(numeroMembro, "").trim() : nomeDocumento;
			escreverNaCelula(csvPrinter, ew, linha, nomeDocumento);

			escreverNaCelula(csvPrinter, ew, linha, numeroMembro);

			String statusDocumento = vo.getStatusDocumento();
			String statusDocumentoStr = resourceService.getValue("StatusDocumento." + statusDocumento + ".label");
			statusDocumentoStr = prepararValorCsv(statusDocumentoStr);
			escreverNaCelula(csvPrinter, ew, linha, statusDocumentoStr);

			String irregularidade = vo.getIrregularidade();
			irregularidade = prepararValorCsv(irregularidade);
			escreverNaCelula(csvPrinter, ew, linha, irregularidade);

			String observacaoPendencia = vo.getObservacaoPendencia();
			observacaoPendencia = prepararValorCsv(observacaoPendencia);
			escreverNaCelula(csvPrinter, ew, linha, observacaoPendencia);

			String analistaNome = vo.getAnalistaNome();
			analistaNome = prepararValorCsv(analistaNome);
			escreverNaCelula(csvPrinter, ew, linha, analistaNome);

			String analistaLogin = vo.getAnalistaLogin();
			analistaLogin = prepararValorCsv(analistaLogin);
			escreverNaCelula(csvPrinter, ew, linha, analistaLogin);

			String nomeSituacaoAnterior = vo.getSituacaoAnterior();
			nomeSituacaoAnterior = prepararValorCsv(nomeSituacaoAnterior);
			escreverNaCelula(csvPrinter, ew, linha, nomeSituacaoAnterior);

			String dataEnvioAnalise = DummyUtils.formatDate(vo.getDataEnvioAnalise());
			dataEnvioAnalise = prepararValorCsv(dataEnvioAnalise);
			escreverNaCelula(csvPrinter, ew, linha, dataEnvioAnalise);

			String dataFimAnalise = DummyUtils.formatDate(vo.getDataFimAnalise());
			dataFimAnalise = prepararValorCsv(dataFimAnalise);
			escreverNaCelula(csvPrinter, ew, linha, dataFimAnalise);

			Long numeroDePaginas = vo.getNumeroDePaginas();
			String numeroDePaginasStr = prepararValorCsv(numeroDePaginas);
			escreverNaCelula(csvPrinter, ew, linha, numeroDePaginasStr);

			String professorRedePublica = grupoDadosDoCurso.get(CampoMap.CampoEnum.PROFESSOR_DA_REDE_PUBLICA.getNome());
			professorRedePublica = prepararValorCsv(professorRedePublica);
			escreverNaCelula(csvPrinter, ew, linha, professorRedePublica);

			String ensinoMedioEm = grupoDadosDoCurso.get(CampoMap.CampoEnum.ENSINO_MEDIO_EM.getNome());
			ensinoMedioEm = prepararValorCsv(ensinoMedioEm);
			escreverNaCelula(csvPrinter, ew, linha, ensinoMedioEm);

			String candidatoEhDeficiente = grupoDadosDoCurso.get(CampoMap.CampoEnum.CANDIDATO_E_DEFICIENTE.getNome());
			candidatoEhDeficiente = prepararValorCsv(candidatoEhDeficiente);
			escreverNaCelula(csvPrinter, ew, linha, candidatoEhDeficiente);

			String poloParceiro = grupoDadosDeImportacao != null ? grupoDadosDeImportacao.get(CampoMap.CampoEnum.POLO_PARCEIRO.getNome()): "";
			poloParceiro = prepararValorCsv(poloParceiro);
			escreverNaCelula(csvPrinter, ew, linha, poloParceiro);

			String tipoDeBolsa = grupoDadosDeImportacao != null ? grupoDadosDeImportacao.get(CampoMap.CampoEnum.TIPO_BOLSA.getNome()): "";
			tipoDeBolsa = prepararValorCsv(tipoDeBolsa);
			escreverNaCelula(csvPrinter, ew, linha, tipoDeBolsa);

			String sisProuniTurno = grupoDadosDeImportacao != null ? grupoDadosDeImportacao.get(CampoMap.CampoEnum.TURNO_IMPORTACAO.getNome()): "";
			sisProuniTurno = prepararValorCsv(sisProuniTurno);
			escreverNaCelula(csvPrinter, ew, linha, sisProuniTurno);

			String endereco = grupoDadosDeImportacao != null ? grupoDadosDeImportacao.get(CampoMap.CampoEnum.ENDERECO.getNome()) : "";
			endereco = prepararValorCsv(endereco);
			escreverNaCelula(csvPrinter, ew, linha, endereco);

			String cidade = grupoDadosDeImportacao != null ? grupoDadosDeImportacao.get(CampoMap.CampoEnum.CIDADE.getNome()) : "";
			cidade = prepararValorCsv(cidade);
			escreverNaCelula(csvPrinter, ew, linha, cidade);

			String cep = grupoDadosDeImportacao != null ? grupoDadosDeImportacao.get(CampoMap.CampoEnum.CEP.getNome()) : "";
			cep = prepararValorCsv(cep);
			escreverNaCelula(csvPrinter, ew, linha, cep);

			String email = grupoDadosDeImportacao != null ? grupoDadosDeImportacao.get(CampoMap.CampoEnum.E_MAIL.getNome()) : "";
			email = prepararValorCsv(email);
			escreverNaCelula(csvPrinter, ew, linha, email);

			String dddTelefone = grupoDadosDeImportacao != null ? grupoDadosDeImportacao.get(CampoMap.CampoEnum.DDD_TELEFONE.getNome()) : "";
			dddTelefone = prepararValorCsv(dddTelefone);
			escreverNaCelula(csvPrinter, ew, linha, dddTelefone);

			String notaMedia = grupoDadosDeImportacao != null ? grupoDadosDeImportacao.get(CampoMap.CampoEnum.NOTA_MEDIA.getNome()) : "";
			notaMedia = prepararValorCsv(notaMedia);
			escreverNaCelula(csvPrinter, ew, linha, notaMedia);

			String tipoProuni = grupoDadosDeImportacao != null ? grupoDadosDeImportacao.get(CampoMap.CampoEnum.TIPO_PROUNI.getNome()) : "";
			tipoProuni = prepararValorCsv(tipoProuni);
			escreverNaCelula(csvPrinter, ew, linha, tipoProuni);

			String curso = grupoDadosDoCurso != null ? grupoDadosDoCurso.get(CampoMap.CampoEnum.CURSO_NOME_IMPORTACAO.getNome()) : "";
			curso = prepararValorCsv(curso);
			escreverNaCelula(csvPrinter, ew, linha, curso);

			String cpfImportacao = grupoDadosDeImportacao != null ? grupoDadosDeImportacao.get(CampoMap.CampoEnum.CPF_IMPORTACAO.getNome()) : "";
			cpfImportacao = prepararValorCsv(cpfImportacao);
			escreverNaCelula(csvPrinter, ew, linha, cpfImportacao);

			String dataVinculoStr = grupoDadosDoCurso.get(CampoMap.CampoEnum.DATA_VINCULO_SIA.getNome());
			dataVinculoStr = prepararValorCsv(dataVinculoStr);
			escreverNaCelula(csvPrinter, ew, linha, dataVinculoStr);

			String pastaVermelha = prepararValorCsv(vo.getPastaVermelha() != null && vo.getPastaVermelha() ? "Sim" : "Não");
			escreverNaCelula(csvPrinter, ew, linha, pastaVermelha);

			Boolean usaTermo = vo.getUsaTermo();
			String usaTermoStr = usaTermo != null ? (usaTermo ? "Sim" : "Não") : "Não";
			usaTermoStr = prepararValorCsv(usaTermoStr);
			escreverNaCelula(csvPrinter, ew, linha, usaTermoStr);

			String periodoIngressoImportacao = grupoDadosDeImportacao != null ? grupoDadosDeImportacao.get(CampoMap.CampoEnum.PERIODO_DE_INGRESSO_IMPORTACAO.getNome()) : "";
			periodoIngressoImportacao = prepararValorCsv(periodoIngressoImportacao);
			escreverNaCelula(csvPrinter, ew, linha, periodoIngressoImportacao);

			if (csvPrinter != null) {
				csvPrinter.printRecord(linha);
			}
		}
	}

	private Map<Long, Map<String, RegistroValorVO>> geraRegistrosDeBaseInterna() {
		Map<Long, Map<String, RegistroValorVO>> map = new HashMap<>();

		Long baseInternaRegionalId = BaseInterna.REGIONAL_ID;
		List<RegistroValorVO> baseInternaList = baseRegistroService.findByBaseInterna(baseInternaRegionalId);
		geraValorDeBaseInterna(baseInternaRegionalId, baseInternaList, map);

		Long baseInternaFormaIngressoId = BaseInterna.FORMA_INGRESSO_ID;
		List<RegistroValorVO> baseInternaFormaIngressoList = baseRegistroService.findByBaseInterna(baseInternaFormaIngressoId);
		geraValorDeBaseInterna(baseInternaFormaIngressoId, baseInternaFormaIngressoList, map);

		Long baseInternaInstituicaoId = BaseInterna.INSTITUICAO_ID;
		List<RegistroValorVO> baseInternaInstituicaoList = baseRegistroService.findByBaseInterna(baseInternaInstituicaoId);
		geraValorDeBaseInterna(baseInternaInstituicaoId, baseInternaInstituicaoList, map);

		Long baseInternaCampusId = BaseInterna.CAMPUS_ID;
		List<RegistroValorVO> baseInternaCampusList = baseRegistroService.findByBaseInterna(baseInternaCampusId);
		geraValorDeBaseInterna(baseInternaCampusId, baseInternaCampusList, map);

		Long baseInternaCursoId = BaseInterna.CURSO_ID;
		List<RegistroValorVO> baseInternaCursoList = baseRegistroService.findByBaseInterna(baseInternaCursoId);
		geraValorDeBaseInterna(baseInternaCursoId, baseInternaCursoList, map);

		return map;
	}

	private String getCampoGrupoValor(Map<Long, Map<String, RegistroValorVO>> map, Map<String, String> grupoDadosDoCurso, String campoNome, Long baseInternaId) {
		RegistroValorVO registroValorVO = getBaseRegistroValor(map, grupoDadosDoCurso, campoNome, baseInternaId);
		return registroValorVO != null ? registroValorVO.getLabel() : null;
	}

	private RegistroValorVO getBaseRegistroValor(Map<Long, Map<String, RegistroValorVO>> map, Map<String, String> grupoDadosDoCurso, String campoNome, Long baseInternaId) {
		String valor = grupoDadosDoCurso.get(campoNome);
		if(StringUtils.isBlank(valor)) return null;

		valor = DummyUtils.limparCharsChaveUnicidade(valor);
		Map<String, RegistroValorVO> valoresMap = map.get(baseInternaId);
		return valoresMap.get(valor);
	}

	private void geraValorDeBaseInterna(Long baseInternaId, List<RegistroValorVO> baseInternaList, Map<Long, Map<String, RegistroValorVO>> map) {
		Map<String, RegistroValorVO> mapRegional = new HashMap<>();
		for (RegistroValorVO registroValorVO : baseInternaList) {
			String chaveUnicidade = registroValorVO.getChaveUnicidade();
			mapRegional.put(chaveUnicidade, registroValorVO);
		}
		map.put(baseInternaId, mapRegional);
	}

	private String prepararValorCsv(Long valor) {
		String value = valor != null ? String.valueOf(valor) : "";
		return prepararValorCsv(value);
	}

	private String prepararValorCsv(String valor) {
		valor = StringUtils.isNotBlank(valor) ? valor.replaceAll("(\\r|\\n)", "") : "";
		return valor.replace(";", ",");
	}

	private void escreverNaCelula(CSVPrinter csvPrinter, ExcelWriter ew, List<String> linha, String valor) {
		if (ew != null) ew.escrever(valor);
		if (csvPrinter != null) linha.add(valor);
	}

	private void renderCabecalho(CSVPrinter csvPrinter, ExcelWriter ew, Sheet sheet, boolean isSisFiesSisProuni) throws Exception {

		Map<String, Integer> colunasMap = null;
		if(isSisFiesSisProuni) {
			colunasMap = getHeaderSisFiesSisProuni();
		}
		else {
			colunasMap = getHeader();
		}

		Set<String> colunas = colunasMap.keySet();

		if(csvPrinter != null) {
			csvPrinter.printRecord(colunas);
			return;
		}

		int i=0;
		for(String header : colunas) {
			ew.escrever(header);
			int ColumnWidth = colunasMap.get(header);
			sheet.setColumnWidth(i, ColumnWidth);
			i++;
		}
	}

	public Map<String, Integer> getHeader() {

		Map<String, Integer> map = new LinkedHashMap<>();

		String processoId = RelatorioPendenciaDocumentos.ColunasEnum.ID.getNome();
		map.put(processoId, 3000);

		String situacaoNome = RelatorioPendenciaDocumentos.ColunasEnum.SITUACAO.getNome();
		map.put(situacaoNome, 8000);

		String regional = RelatorioPendenciaDocumentos.ColunasEnum.REGIONAL.getNome();
		map.put(regional, 4000);

		String instituicaoCodigo = RelatorioPendenciaDocumentos.ColunasEnum.COD_INSTITUICAO.getNome();
		map.put(instituicaoCodigo, 4000);

		String instituicaoNome = RelatorioPendenciaDocumentos.ColunasEnum.INSTITUICAO.getNome();
		map.put(instituicaoNome, 11000);

		String campusCodigo = RelatorioPendenciaDocumentos.ColunasEnum.COD_CAMPUS.getNome();
		map.put(campusCodigo, 3000);

		String campusNome = RelatorioPendenciaDocumentos.ColunasEnum.CAMPUS.getNome();
		map.put(campusNome, 9000);

		String poloParceiro = RelatorioPendenciaDocumentos.ColunasEnum.POLO_PARCEIRO.getNome();
		map.put(poloParceiro, 4000);

		String cursoCodigo = RelatorioPendenciaDocumentos.ColunasEnum.COD_CURSO.getNome();
		map.put(cursoCodigo, 3000);

		String cursoNome = RelatorioPendenciaDocumentos.ColunasEnum.CURSO.getNome();
		map.put(cursoNome, 9000);

		String formaIngresso = RelatorioPendenciaDocumentos.ColunasEnum.FORMA_INGRESSO.getNome();
		map.put(formaIngresso, 5000);

		String modalidadeEnsino = RelatorioPendenciaDocumentos.ColunasEnum.MODALIDADE_ENSINO.getNome();
		map.put(modalidadeEnsino, 5000);

		String numeroInscricao = RelatorioPendenciaDocumentos.ColunasEnum.NUM_INSCRICAO.getNome();
		map.put(numeroInscricao, 5000);

		String candidatoNumero = RelatorioPendenciaDocumentos.ColunasEnum.NUM_CANDIDATO.getNome();
		map.put(candidatoNumero, 4000);

		String matricula = RelatorioPendenciaDocumentos.ColunasEnum.MATRICULA.getNome();
		map.put(matricula, 10000);

		String origem = RelatorioPendenciaDocumentos.ColunasEnum.ORIGEM.getNome();
		map.put(origem, 10000);

		String tipoCurso = RelatorioPendenciaDocumentos.ColunasEnum.TIPO_CURSO.getNome();
		map.put(tipoCurso, 4000);

		String periodoIngresso = RelatorioPendenciaDocumentos.ColunasEnum.PERIODO_INGRESSO.getNome();
		map.put(periodoIngresso, 4000);

		String turnoNome = RelatorioPendenciaDocumentos.ColunasEnum.TURNO.getNome();
		map.put(turnoNome, 4000);

		String alunoNome = RelatorioPendenciaDocumentos.ColunasEnum.ALUNO.getNome();
		map.put(alunoNome, 4000);

		String cpf = RelatorioPendenciaDocumentos.ColunasEnum.CPF.getNome();
		map.put(cpf, 4000);

		String email = RelatorioPendenciaDocumentos.ColunasEnum.EMAIL.getNome();
		map.put(email, 8000);

		String telefone = RelatorioPendenciaDocumentos.ColunasEnum.TELEFONE.getNome();
		map.put(telefone, 5000);

		String documento = RelatorioPendenciaDocumentos.ColunasEnum.DOCUMENTO.getNome();
		map.put(documento, 14000);

		String statusDocumento = RelatorioPendenciaDocumentos.ColunasEnum.STATUS_DOCUMENTO.getNome();
		map.put(statusDocumento, 5000);

		String observacao = RelatorioPendenciaDocumentos.ColunasEnum.OBSERVACAO.getNome();
		map.put(observacao, 800);

		String totalImagens = RelatorioPendenciaDocumentos.ColunasEnum.TOTAL_IMAGENS.getNome();
		map.put(totalImagens, 4000);

		String dataEnvioAnalise = RelatorioPendenciaDocumentos.ColunasEnum.DATA_ENVIO_ANALISE.getNome();
		map.put(dataEnvioAnalise, 5000);

		String dataFinalizacaoAnalise = RelatorioPendenciaDocumentos.ColunasEnum.DATA_FINALIZACAO_ANALISE.getNome();
		map.put(dataFinalizacaoAnalise, 5000);

		String totalVezesEmAnalise = RelatorioPendenciaDocumentos.ColunasEnum.TOTAL_VEZES_EM_ANALISE.getNome();
		map.put(totalVezesEmAnalise, 4000);

		String motivoRejeite = RelatorioPendenciaDocumentos.ColunasEnum.MOTIVO_DE_REJEITE.getNome();
		map.put(motivoRejeite, 4400);

		String obrigatorio = RelatorioPendenciaDocumentos.ColunasEnum.OBRIGATORIO.getNome();
		map.put(obrigatorio, 4200);

		String tipoProcessoNome = RelatorioPendenciaDocumentos.ColunasEnum.TIPO_PROCESSO.getNome();
		map.put(tipoProcessoNome, 5000);

		String pastaAmarela = RelatorioPendenciaDocumentos.ColunasEnum.PASTA_AMARELA.getNome();
		map.put(pastaAmarela, 4400);

		String pastaVermelha = RelatorioPendenciaDocumentos.ColunasEnum.PASTA_VERMELHA.getNome();
		map.put(pastaVermelha, 4400);

		String versaoAtual = RelatorioPendenciaDocumentos.ColunasEnum.VERSAO_ATUAL.getNome();
		map.put(versaoAtual, 4400);

		String modeloDocumento = RelatorioPendenciaDocumentos.ColunasEnum.MODELO_DOCUMENTO.getNome();
		map.put(modeloDocumento, 4400);

		String usaTermo = RelatorioPendenciaDocumentos.ColunasEnum.USA_TERMO.getNome();
		map.put(usaTermo, 5000);

		String origemDocumento = RelatorioPendenciaDocumentos.ColunasEnum.ORIGEM_DOCUMENTO.getNome();
		map.put(origemDocumento, 5000);

		return map;
	}

	public Map<String, Integer> getHeaderSisFiesSisProuni() {

		Map<String, Integer> map = new LinkedHashMap<>();

		String processoId = RelatorioPendenciaDocumentos.ColunasEnum.ID.getNome();
		map.put(processoId, 3000);

		String dataCriacao = RelatorioPendenciaDocumentos.ColunasEnum.DATA_CRIACAO.getNome();
		map.put(dataCriacao, 5000);

		String documentoId = RelatorioPendenciaDocumentos.ColunasEnum.DOCUMENTO_ID.getNome();
		map.put(documentoId, 5000);

		String situacaoNome = RelatorioPendenciaDocumentos.ColunasEnum.SITUACAO.getNome();
		map.put(situacaoNome, 8000);

		String regional = RelatorioPendenciaDocumentos.ColunasEnum.REGIONAL.getNome();
		map.put(regional, 4000);

		String instituicaoCodigo = RelatorioPendenciaDocumentos.ColunasEnum.COD_INSTITUICAO.getNome();
		map.put(instituicaoCodigo, 4000);

		String instituicaoNome = RelatorioPendenciaDocumentos.ColunasEnum.INSTITUICAO.getNome();
		map.put(instituicaoNome, 11000);

		String campusCodigo = RelatorioPendenciaDocumentos.ColunasEnum.COD_CAMPUS.getNome();
		map.put(campusCodigo, 3000);

		String campusNome = RelatorioPendenciaDocumentos.ColunasEnum.CAMPUS.getNome();
		map.put(campusNome, 9000);

		String cursoCodigo = RelatorioPendenciaDocumentos.ColunasEnum.COD_CURSO.getNome();
		map.put(cursoCodigo, 3000);

		String cursoNome = RelatorioPendenciaDocumentos.ColunasEnum.CURSO.getNome();
		map.put(cursoNome, 9000);

		String aluno = RelatorioPendenciaDocumentos.ColunasEnum.ALUNO.getNome();
		map.put(aluno, 5000);

		String cpf = RelatorioPendenciaDocumentos.ColunasEnum.CPF.getNome();
		map.put(cpf, 5000);

		String nomeImportacao = RelatorioPendenciaDocumentos.ColunasEnum.NOME_IMPORTACAO.getNome();
		map.put(nomeImportacao, 5000);

		String numCandidato = RelatorioPendenciaDocumentos.ColunasEnum.NUM_CANDIDATO.getNome();
		map.put(numCandidato, 5000);

		String numInscricao = RelatorioPendenciaDocumentos.ColunasEnum.NUM_INSCRICAO.getNome();
		map.put(numInscricao, 5000);

		String matricula = RelatorioPendenciaDocumentos.ColunasEnum.MATRICULA.getNome();
		map.put(matricula, 5000);

		String localOferta = RelatorioPendenciaDocumentos.ColunasEnum.LOCAL_OFERTA.getNome();
		map.put(localOferta, 5000);

		String chamada = RelatorioPendenciaDocumentos.ColunasEnum.CHAMADA.getNome();
		map.put(chamada, 5000);

		String formaIngresso = RelatorioPendenciaDocumentos.ColunasEnum.FORMA_INGRESSO.getNome();
		map.put(formaIngresso, 5000);

		String periodoIngresso = RelatorioPendenciaDocumentos.ColunasEnum.PERIODO_INGRESSO.getNome();
		map.put(periodoIngresso, 5000);

		String tipoProcesso = RelatorioPendenciaDocumentos.ColunasEnum.TIPO_PROCESSO.getNome();
		map.put(tipoProcesso, 5000);

		String documento = RelatorioPendenciaDocumentos.ColunasEnum.DOCUMENTO.getNome();
		map.put(documento, 5000);

		String numeroMembro = RelatorioPendenciaDocumentos.ColunasEnum.NUMERO_MENBRO.getNome();
		map.put(numeroMembro, 5000);

		String status = RelatorioPendenciaDocumentos.ColunasEnum.STATUS_DOCUMENTO.getNome();
		map.put(status, 5000);

		String irregularidade = RelatorioPendenciaDocumentos.ColunasEnum.MOTIVO_DE_REJEITE.getNome();
		map.put(irregularidade, 5000);

		String observacao = RelatorioPendenciaDocumentos.ColunasEnum.OBSERVACAO.getNome();
		map.put(observacao, 5000);

		String analista = RelatorioPendenciaDocumentos.ColunasEnum.ANALISTA.getNome();
		map.put(analista, 5000);

		String analistaLogin = RelatorioPendenciaDocumentos.ColunasEnum.ANALISTA_LOGIN.getNome();
		map.put(analistaLogin, 5000);

		String situacaoAnteriorNome = RelatorioPendenciaDocumentos.ColunasEnum.SITUACAO_ANTERIOR.getNome();
		map.put(situacaoAnteriorNome, 8000);

		String dataEnvioAnalise = RelatorioPendenciaDocumentos.ColunasEnum.DATA_ENVIO_ANALISE.getNome();
		map.put(dataEnvioAnalise, 5000);

		String dataFinalizacaoAnalise = RelatorioPendenciaDocumentos.ColunasEnum.DATA_FINALIZACAO_ANALISE.getNome();
		map.put(dataFinalizacaoAnalise, 5000);

		String numeroDePaginas = RelatorioPendenciaDocumentos.ColunasEnum.NUMERO_DE_PAGINAS.getNome();
		map.put(numeroDePaginas, 5000);

		String professorDeRedePublica = RelatorioPendenciaDocumentos.ColunasEnum.PROFESSOR_DE_REDE_PUBLICA.getNome();
		map.put(professorDeRedePublica, 5000);

		String ensinoMedioEm = RelatorioPendenciaDocumentos.ColunasEnum.ENSINO_MEDIO_EM.getNome();
		map.put(ensinoMedioEm, 5000);

		String candidatoEhDeficiente = RelatorioPendenciaDocumentos.ColunasEnum.CANDIDATO_EH_DEFICIENTE.getNome();
		map.put(candidatoEhDeficiente, 5000);

		String poloParceiro = RelatorioPendenciaDocumentos.ColunasEnum.POLO_PARCEIRO.getNome();
		map.put(poloParceiro, 4000);

		String tipoDeBolsa = RelatorioPendenciaDocumentos.ColunasEnum.TIPO_DE_BOLSA_IMPORTACAO.getNome();
		map.put(tipoDeBolsa, 5000);

		String turnoImportacao = RelatorioPendenciaDocumentos.ColunasEnum.TURNO_IMPORTACAO.getNome();
		map.put(turnoImportacao, 5000);

		String enderecoImportacao = RelatorioPendenciaDocumentos.ColunasEnum.ENDERECO_IMPORTACAO.getNome();
		map.put(enderecoImportacao, 5000);

		String cidadeImportacao = RelatorioPendenciaDocumentos.ColunasEnum.CIDADE_IMPORTACAO.getNome();
		map.put(cidadeImportacao, 5000);

		String cepImportacao = RelatorioPendenciaDocumentos.ColunasEnum.CEP_IMPORTACAO.getNome();
		map.put(cepImportacao, 5000);

		String emailImportacao = RelatorioPendenciaDocumentos.ColunasEnum.EMAIL_IMPORTACAO.getNome();
		map.put(emailImportacao, 5000);

		String dddTelefoneImportacao = RelatorioPendenciaDocumentos.ColunasEnum.DDD_TELEFONE_IMPORTACAO.getNome();
		map.put(dddTelefoneImportacao, 5000);

		String notaMediaImportacao = RelatorioPendenciaDocumentos.ColunasEnum.NOTA_MEDIA_IMPORTACAO.getNome();
		map.put(notaMediaImportacao, 5000);

		String tipoProuniImportacao = RelatorioPendenciaDocumentos.ColunasEnum.TIPO_PROUNI_IMPORTACAO.getNome();
		map.put(tipoProuniImportacao, 5000);

		String cursoImportacaoImportacao = RelatorioPendenciaDocumentos.ColunasEnum.CURSO_IMPORTACAO.getNome();
		map.put(cursoImportacaoImportacao, 5000);

		String cpfImportacao = RelatorioPendenciaDocumentos.ColunasEnum.CPF_IMPORTACAO.getNome();
		map.put(cpfImportacao, 5000);

		String dataVinculo = RelatorioPendenciaDocumentos.ColunasEnum.DATA_VINCULO.getNome();
		map.put(dataVinculo, 5000);

		String pastaVermelha = RelatorioPendenciaDocumentos.ColunasEnum.PASTA_VERMELHA.getNome();
		map.put(pastaVermelha, 5000);

		String usaTermo = RelatorioPendenciaDocumentos.ColunasEnum.USA_TERMO.getNome();
		map.put(usaTermo, 5000);

		String periodoIngressoImportacao = RelatorioPendenciaDocumentos.ColunasEnum.PERIODO_INGRESSO_IMPORTACAO.getNome();
		map.put(periodoIngressoImportacao, 5000);

		return map;
	}

	public List<RelatorioPendenciaDocumentoVO> findRelatorio(RelatorioPendenciaDocumentoFiltro filtro) {

		boolean sisFiesOuSisProuni = filtro.isSisFiesOuSisProuni();
		if(sisFiesOuSisProuni) {
			return gerarRelatorioSisFiesSisProuni(filtro);
		}
		else {
			return gerarRelatorio(filtro);
		}
	}

	public List<RelatorioPendenciaDocumentoVO> gerarRelatorio(RelatorioPendenciaDocumentoFiltro filtro) {

		List<RelatorioPendenciaDocumentoVO> relatorioPendenciaDocumentoVOS = new ArrayList<>();
		List<Object[]> list = null;
		boolean dehMenosUm = filtro.isDehMenosUm();
		if(dehMenosUm) {
			list = relatorioPendenciaDocumentoBiRepository.findRelatorio(filtro);
		}
		else {
			list = relatorioPendenciaDocumentoRepository.findRelatorio(filtro);
		}

		if (list != null && list.size() > 0) {
			boolean pagina = filtro.isPagina();

			List<String> documentosList = new ArrayList<String>();
			String cpfAnterior = "";
			for (Object[] obj : list) {

				final String cpfAluno = (String) obj[0];
				final String nomeAluno = (String) obj[1];
				final long processoId = (Long) obj[2];
				final String strSituacao = (String) obj[3];
				final Origem origem = (Origem) obj[4];
				final String strOrigem = origem != null ? origem.name().toString() : "";
				final String nomeDocumento = (String) obj[5];
				final StatusDocumento statusDocumento = (StatusDocumento) obj[6];
				final String oberservacaoPendencia = (String) obj[7];
				final long totalImagens = (Long) obj[8];

				// Data Criação		Data Envio		Data Análise	Data Finalização
				ProcessoLog processoLog = (ProcessoLog) obj[9];
				final Date dataEnvioAnalise = processoLog != null ? processoLog.getData() : null;
				final Date dataFinalizacaoAnalise = (Date) obj[10];
				int vezesEmAnalise = obj[11] == null ? 0 : (int) obj[11];

				String irregularidade = obj[12] == null ? "" : (String) obj[12];
				boolean obrigatorio = obj[13] != null && (boolean) obj[13];
				Integer versaoAtual = obj[14] == null ? null : (Integer) obj[14];
				boolean usaTermo = obj[15] != null && (boolean) obj[15];
				final String nomeTipoProcesso = (String) obj[16];
				boolean pastaAmarela = obj[17] != null && (boolean) obj[17];
				String modeloDocumento = (String) obj[18];
				String camposDinamicos = (String) obj[19];
				final StatusProcesso statusProcesso = (StatusProcesso) obj[20];
				final Origem origemDocumento = (Origem) obj[21];
				Boolean pastaVermelha = false;
				if(usaTermo || (StatusProcesso.PENDENTE.equals(statusProcesso) || !StatusProcesso.CONCLUIDO.equals(statusProcesso))) {
					pastaVermelha = true;
				}
				else if(!StatusProcesso.PENDENTE.equals(statusProcesso)) {
					pastaVermelha = false;
				}

				RelatorioPendenciaDocumentoVO vo = new RelatorioPendenciaDocumentoVO();
				vo.setProcessoId(processoId);
				vo.setNomeSituacao(strSituacao);
				vo.setOrigem(strOrigem);
				vo.setNomeAluno(nomeAluno);
				vo.setCpf(cpfAluno);
				vo.setNomeDocumento(nomeDocumento);
				vo.setStatusDocumento(statusDocumento.name());
				vo.setObservacaoPendencia(oberservacaoPendencia);
				vo.setTotalImagens(totalImagens);
				vo.setDataEnvioAnalise(dataEnvioAnalise);
				vo.setDataFimAnalise(dataFinalizacaoAnalise);
				vo.setTotalVezesEmAnalise(vezesEmAnalise);
				vo.setIrregularidade(irregularidade);
				vo.setObrigatorio(obrigatorio);
				vo.setVersaoAtual(versaoAtual);
				vo.setUsaTermo(usaTermo);
				vo.setTipoProcesso(nomeTipoProcesso);
				vo.setPastaAmarela(pastaAmarela);
				vo.setModeloDocumento(modeloDocumento);
				vo.setCamposDinamicos(camposDinamicos);
				vo.setPastaVermelha(pastaVermelha);
				vo.setOrigemDocumento(origemDocumento);


				if(pagina) {
					if (StringUtils.isBlank(cpfAnterior) || !cpfAnterior.equals(cpfAluno)) {
						documentosList = new ArrayList<>();
					}

					documentosList.add(nomeDocumento);
					vo.setDocumentos(documentosList);
					cpfAnterior = cpfAluno;
				}

				relatorioPendenciaDocumentoVOS.add(vo);
			}
		}

		Collections.sort(relatorioPendenciaDocumentoVOS);

		return relatorioPendenciaDocumentoVOS;
	}

	private List<RelatorioPendenciaDocumentoVO> gerarRelatorioSisFiesSisProuni(RelatorioPendenciaDocumentoFiltro filtro) {

		List<RelatorioPendenciaDocumentoVO> relatorioPendenciaDocumentoVOS = new ArrayList<>();
		List<Object[]> list = null;
		boolean dehMenosUm = filtro.isDehMenosUm();
		if(dehMenosUm) {
			list = relatorioPendenciaDocumentoBiRepository.findRelatorioSisFiesSisProuni(filtro);
		}
		else {
			list = relatorioPendenciaDocumentoRepository.findRelatorioSisFiesSisProuni(filtro);
		}

		createVoList(filtro, relatorioPendenciaDocumentoVOS, list);

		Collections.sort(relatorioPendenciaDocumentoVOS);

		return relatorioPendenciaDocumentoVOS;
	}

	private void createVoList(RelatorioPendenciaDocumentoFiltro filtro, List<RelatorioPendenciaDocumentoVO> relatorioPendenciaDocumentoVOS, List<Object[]> list) {
		if (list != null && list.size() > 0) {
			boolean pagina = filtro.isPagina();

			List<String> documentosList = new ArrayList<String>();
			String cpfAnterior = "";
			for (Object[] obj : list) {

				final long processoId = (Long) obj[0];
				final Date dataCriacaoProcesso = (Date) obj[1];
				final Long documentoId = (Long) obj[2];
				final String strSituacao = (String) obj[3];
				final String nomeAluno = (String) obj[4];
				final String cpfAluno = (String) obj[5];
				final String nomeTipoProcesso = (String) obj[6];
				final String nomeDocumento = (String) obj[7];
				final String numeroMembro = (String) obj[8];
				final StatusDocumento statusDocumento = (StatusDocumento) obj[9];
				final String irregularidade = (String) obj[10];
				final String observacaoPendencia = (String) obj[11];
				final String nomeAnalista = (String) obj[12];
				final String loginAnalista = (String) obj[13];
				final String strSituacaoAnterior = (String) obj[14];
				final Date dataEnvioAnalise = (Date) obj[15];
				final Date dataFinalizacaoAnalise = (Date) obj[16];
				final Long numeroDePaginas = (Long) obj[17];

				final StatusProcesso statusProcesso = (StatusProcesso) obj[18];
				final boolean usaTermo = (boolean) obj[19];
				Boolean pastaVermelha = false;
				if(usaTermo || (StatusProcesso.PENDENTE.equals(statusProcesso) || !StatusProcesso.CONCLUIDO.equals(statusProcesso))) {
					pastaVermelha = true;
				}
				else if(!StatusProcesso.PENDENTE.equals(statusProcesso)) {
					pastaVermelha = false;
				}
				final String camposDinamicos = (String) obj[20];

				RelatorioPendenciaDocumentoVO vo = new RelatorioPendenciaDocumentoVO();
				vo.setNumeroMembro(numeroMembro);
				vo.setSituacaoAnterior(strSituacaoAnterior);
				vo.setProcessoId(processoId);
				vo.setNomeSituacao(strSituacao);
				vo.setNomeAluno(nomeAluno);
				vo.setCpf(cpfAluno);
				vo.setNomeDocumento(nomeDocumento);
				vo.setStatusDocumento(statusDocumento.name());
				vo.setObservacaoPendencia(observacaoPendencia);
				vo.setDataEnvioAnalise(dataEnvioAnalise);
				vo.setDataFimAnalise(dataFinalizacaoAnalise);
				vo.setIrregularidade(irregularidade);
				vo.setTipoProcesso(nomeTipoProcesso);
				vo.setDataCriacaoProcesso(dataCriacaoProcesso);
				vo.setDocumentoId(documentoId);
				vo.setAnalistaLogin(loginAnalista);
				vo.setAnalistaNome(nomeAnalista);
				vo.setNumeroDePaginas(numeroDePaginas);
				vo.setCamposDinamicos(camposDinamicos);
				vo.setPastaVermelha(pastaVermelha);
				vo.setUsaTermo(usaTermo);

				if (pagina) {
					if (StringUtils.isBlank(cpfAnterior) || !cpfAnterior.equals(cpfAluno)) {
						documentosList = new ArrayList<>();
					}

					documentosList.add(nomeDocumento);
					vo.setDocumentos(documentosList);
					cpfAnterior = cpfAluno;
				}

				relatorioPendenciaDocumentoVOS.add(vo);
			}
		}
	}

	private static class MetricasGeracaoRelatorio {

		private final JSONObject metricas = new JSONObject();

		public MetricasGeracaoRelatorio(RelatorioPendenciaDocumentoFiltro filtro, List<RelatorioPendenciaDocumentoVO> relatorioPendenciaDocumentoVOS) {

			Map<String, Long> qntRegistrosPorSituacao = relatorioPendenciaDocumentoVOS.stream()
					.map(RelatorioPendenciaDocumentoVO::getNomeSituacao)
					.filter(Objects::nonNull).collect(Collectors.groupingBy(n -> n, Collectors.counting()));

			Map<String, Long> qntRegistrosPorRegional = relatorioPendenciaDocumentoVOS.stream()
					.map(RelatorioPendenciaDocumentoVO::getNomeRegional)
					.filter(Objects::nonNull).collect(Collectors.groupingBy(n -> n, Collectors.counting()));

			long qntProcessosDistintos = relatorioPendenciaDocumentoVOS.stream()
					.map(RelatorioPendenciaDocumentoVO::getProcessoId)
					.distinct().count();

			metricas.put("regionaisFiltro", filtro.getRegionais());
			metricas.put("qntRegistros", relatorioPendenciaDocumentoVOS.size());
			metricas.put("qntRegistrosPorSituacao", qntRegistrosPorSituacao);
			metricas.put("qntRegistrosPorRegional", qntRegistrosPorRegional);
			metricas.put("qntProcessosDistintos", qntProcessosDistintos);
		}

		public void printar() {
			systraceThread("Métricas exportação RelatorioPendenciaDocumento=" + metricas);
		}

		public void putTempoEscritaLinhasMs(long tempoEscritaLinhasMs) {
			metricas.put("tempoEscritaLinhasMs", tempoEscritaLinhasMs);
		}

		public void putTempoEscritaArquivoMs(long tempoEscritaArquivoMs) {
			metricas.put("tempoEscritaArquivoMs", tempoEscritaArquivoMs);
		}

		public void putTempoBuscaRegistrosMs(Long tempoBuscaRegistrosMs) {
			metricas.put("tempoBuscaRegistrosMs", tempoBuscaRegistrosMs);
		}
	}
}
