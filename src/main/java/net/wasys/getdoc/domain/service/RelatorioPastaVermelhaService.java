package net.wasys.getdoc.domain.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.wasys.getdoc.GetdocConstants;
import net.wasys.getdoc.domain.enumeration.StatusDocumento;
import net.wasys.getdoc.domain.enumeration.StatusGeracaoPastaVermelha;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.vo.ExecucaoPastaVermelhaVO;
import net.wasys.getdoc.domain.vo.RelatorioPastaVermelhaVO;
import net.wasys.getdoc.domain.vo.filtro.RelatorioPastaVermelhaFiltro;
import net.wasys.getdoc.rest.request.vo.IniciarCriacaoPastaVermelhaRequestVO;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.other.ExcelCsvWriter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.singletonList;
import static net.wasys.util.DummyUtils.systraceThread;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
public class RelatorioPastaVermelhaService {

	@Autowired private DocumentoService documentoService;
	@Autowired private ApplicationContext applicationContext;

	private final Cache<String, ExecucaoPastaVermelhaVO> cacheArquivosPorUuid = CacheBuilder.newBuilder()
			.expireAfterWrite(60, TimeUnit.MINUTES)
			.maximumSize(100)
			.build();

	public int countByFiltro(RelatorioPastaVermelhaFiltro filtro) {

		validarParametros(filtro);

		return documentoService.countRelatorioPastaVermelhaByFiltro(filtro);
	}

	public List<RelatorioPastaVermelhaVO> findByFiltro(RelatorioPastaVermelhaFiltro filtro, Integer first, Integer pageSize) {

		validarParametros(filtro);

		return documentoService.findRelatorioPastaVermelhaByFiltro(filtro, first, pageSize);
	}

	public void validarParametros(RelatorioPastaVermelhaFiltro filtro) throws MessageKeyException {

		String periodoIngresso = filtro.getPeriodoIngresso();
		if (isBlank(periodoIngresso)) {
			throw new MessageKeyException("validacao-obrigatorio.error", "Período de ingresso");
		}

	}

	public String iniciarCriacaoRelatorio(IniciarCriacaoPastaVermelhaRequestVO request) {

		String uuidRequest = UUID.randomUUID().toString();
		cacheArquivosPorUuid.put(uuidRequest, new ExecucaoPastaVermelhaVO(StatusGeracaoPastaVermelha.EXECUTANDO, null));

		TransactionWrapper tw = new TransactionWrapper(applicationContext);
		tw.setRunnable(() -> {

			systraceThread("Iniciando geração relatório para UUID=" + uuidRequest);
			File file = gerarArquivoCompactado(request);
			cacheArquivosPorUuid.put(uuidRequest, new ExecucaoPastaVermelhaVO(StatusGeracaoPastaVermelha.FINALIZADO, file));
			systraceThread("Fim geração relatório para UUID=" + uuidRequest);

		});
		tw.startThread();

		return uuidRequest;
	}

	private File gerarArquivoCompactado(IniciarCriacaoPastaVermelhaRequestVO request) {

		String periodo = request.getPeriodo();
		List<String> statusProcessos = request.getStatusProcessos();
		List<String> statusDocumentos = request.getStatusDocumentos();
		Boolean ignorarFiesProuniConcluido = request.getIgnorarFiesProuniConcluido();

		RelatorioPastaVermelhaFiltro filtro = new RelatorioPastaVermelhaFiltro();
		filtro.setPeriodoIngresso(periodo);
		filtro.setStatusProcessos(StatusProcesso.statusParaValores(statusProcessos));
		filtro.setStatusDocumentos(StatusDocumento.statusParaValores(statusDocumentos));
		filtro.setIgnorarFiesProuniConcluido(ignorarFiesProuniConcluido);

		List<RelatorioPastaVermelhaVO> registros = findByFiltro(filtro, null, null);

		try {
			return gerarRelatorioCsvCompactado(registros);
		}
		catch (IOException e) {

			e.printStackTrace();
			String rootCauseMessage = ExceptionUtils.getRootCauseMessage(e);
			throw new MessageKeyException("erroInesperado.error", rootCauseMessage);
		}
	}

	private File gerarRelatorioCsvCompactado(List<RelatorioPastaVermelhaVO> registros) throws IOException {

		String nomeArquivo = "pasta-vermelha-";
		File csvPastaVermelha = File.createTempFile(nomeArquivo, ".csv");
		DummyUtils.deleteOnExitFile(csvPastaVermelha);

		ExcelCsvWriter ecw = new ExcelCsvWriter();

		String absolutePath = csvPastaVermelha.getAbsolutePath();
		BufferedWriter writer = Files.newBufferedWriter(Paths.get(absolutePath));
		CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
				.withDelimiter(';')
				.withAllowMissingColumnNames()
				.withTrim()
		);
		ecw.setWriter(csvPrinter);
		ecw.criaLinha(0);

		String colunasStr = "Processo ID;Situação;Tipo de Processo;Data Criação;Data Envio Análise;Usa Termo;Forma de Ingresso;Período de Ingresso;Regional;Cód. Instituição;Instituição;Cód. Campus;Campus;Polo Parceiro;Cód. Curso;Curso;CPF;Matrícula;Número Candidato;Número Inscrição;Nome do Aluno;Telefone;Celular;E-mail;Cód. Documento;Nome Documento;Status Documento;Data Digitalização;Irregularidade;Indicação Fies;Indicação Prouni;Situação Aluno";
		for (String col : colunasStr.split(";")) {
			ecw.escrever(col);
		}

		escreverRegistrosCsv(registros, csvPastaVermelha, ecw);

		File csvPastaVermelhaZip = File.createTempFile(nomeArquivo, "." + GetdocConstants.EXTENSAO_DEFINICAO_ZIP);
		String fileTmpAbsolutePath = csvPastaVermelhaZip.getAbsolutePath();
		DummyUtils.compactarParaZip(fileTmpAbsolutePath, singletonList(csvPastaVermelha.getPath()));

		return csvPastaVermelhaZip;
	}

	private void escreverRegistrosCsv(List<RelatorioPastaVermelhaVO> registros, File csvPastaVermelha, ExcelCsvWriter ecw) {

		int rowNum = 1;
		for (RelatorioPastaVermelhaVO registro : registros) {

			ecw.criaLinha(rowNum++);

			Long processoId = registro.getProcessoId();
			ecw.escreverSemPontoVirgula(processoId);

			String nomeSituacao = registro.getNomeSituacao();
			ecw.escreverSemPontoVirgula(nomeSituacao);

			String nomeTipoProcesso = registro.getNomeTipoProcesso();
			ecw.escreverSemPontoVirgula(nomeTipoProcesso);

			String dataCriacao = registro.getDataCriacao();
			ecw.escreverSemPontoVirgula(dataCriacao);

			String dataEnvioAnalise = registro.getDataEnvioAnalise();
			ecw.escreverSemPontoVirgula(dataEnvioAnalise);

			Boolean usaTermo = registro.getUsaTermo();
			ecw.escreverSemPontoVirgula(usaTermo);

			String formaDeIngresso = registro.getFormaDeIngresso();
			ecw.escreverSemPontoVirgula(formaDeIngresso);

			String periodoDeIngresso = registro.getPeriodoDeIngresso();
			ecw.escreverSemPontoVirgula(periodoDeIngresso);

			String regional = registro.getRegional();
			ecw.escreverSemPontoVirgula(regional);

			String codInstituicao = registro.getCodInstituicao();
			ecw.escreverSemPontoVirgula(codInstituicao);

			String instituicao = registro.getInstituicao();
			ecw.escreverSemPontoVirgula(instituicao);

			String codCampus = registro.getCodCampus();
			ecw.escreverSemPontoVirgula(codCampus);

			String campus = registro.getCampus();
			ecw.escreverSemPontoVirgula(campus);

			String poloParceiro = registro.getPoloParceiro();
			ecw.escreverSemPontoVirgula(poloParceiro);

			String codCurso = registro.getCodCurso();
			ecw.escreverSemPontoVirgula(codCurso);

			String curso = registro.getCurso();
			ecw.escreverSemPontoVirgula(curso);

			String cpf = registro.getCpf();
			ecw.escreverSemPontoVirgula(cpf);

			String matricula = registro.getMatricula();
			ecw.escreverSemPontoVirgula(matricula);

			String numeroCandidato = registro.getNumeroCandidato();
			ecw.escreverSemPontoVirgula(numeroCandidato);

			String numeroInscricao = registro.getNumeroInscricao();
			ecw.escreverSemPontoVirgula(numeroInscricao);

			String nomeAluno = registro.getNomeAluno();
			ecw.escreverSemPontoVirgula(nomeAluno);

			String telefone = registro.getTelefone();
			ecw.escreverSemPontoVirgula(telefone);

			String celular = registro.getCelular();
			ecw.escreverSemPontoVirgula(celular);

			String email = registro.getEmail();
			ecw.escreverSemPontoVirgula(email);

			Long codDocumento = registro.getCodDocumento();
			ecw.escreverSemPontoVirgula(codDocumento);

			String nomeDocumento = registro.getNomeDocumento();
			ecw.escreverSemPontoVirgula(nomeDocumento);

			StatusDocumento statusDocumento = registro.getStatusDocumento();
			ecw.escreverSemPontoVirgula(statusDocumento);

			String dataDigitalizacao = registro.getDataDigitalizacao();
			ecw.escreverSemPontoVirgula(dataDigitalizacao);

			String irregularidade = registro.getIrregularidade();
			ecw.escreverSemPontoVirgula(irregularidade);

			String indicacaoFies = registro.getIndicacaoFies();
			ecw.escreverSemPontoVirgula(indicacaoFies);

			String indicacaoProuni = registro.getIndicacaoProuni();
			ecw.escreverSemPontoVirgula(indicacaoProuni);

			String situacaoAluno = registro.getSituacaoAluno();
			ecw.escreverSemPontoVirgula(situacaoAluno);
		}

		ecw.close(csvPastaVermelha);
	}

	public boolean estaExecutandoUUID(String uuid) {
		ExecucaoPastaVermelhaVO execucao = cacheArquivosPorUuid.getIfPresent(uuid);
		return execucao != null && StatusGeracaoPastaVermelha.EXECUTANDO.equals(execucao.getStatusGeracaoPastaVermelha());
	}

	public File buscarRelatorioPorUUID(String uuid) {
		ExecucaoPastaVermelhaVO execucao = cacheArquivosPorUuid.getIfPresent(uuid);
		return execucao != null ? execucao.getArquivo() : null;
	}
}
