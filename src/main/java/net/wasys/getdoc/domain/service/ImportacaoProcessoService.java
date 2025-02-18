package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.*;
import net.wasys.getdoc.domain.vo.*;
import net.wasys.getdoc.domain.vo.filtro.AlunoFiltro;
import net.wasys.getdoc.domain.vo.filtro.ProcessoFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.other.Bolso;
import net.wasys.util.other.ExcelCsvWriter;
import net.wasys.util.rest.jackson.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.jpedal.parser.shape.S;
import org.primefaces.model.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class ImportacaoProcessoService {

	@Autowired private AlunoService alunoService;
	@Autowired private ProcessoService processoService;
	@Autowired private ProcessoLogService processoLogService;
	@Autowired private TipoCampoService tipoCampoService;
	@Autowired private CampoService campoService;
	@Autowired private BaseRegistroService baseRegistroService;
	@Autowired private ApplicationContext applicationContext;
	@Autowired private ResourceService resourceService;
	@Autowired private LogImportacaoService logImportacaoService;
	@Autowired private TipoProcessoService tipoProcessoService;

	private static String colunaProcesso = "PROCESSO ID";

	private void processarArquivo(File file, Usuario usuario, String nomeOriginalFile, List<String> cabecalho, TipoProcesso tipoProcesso, boolean isReimportacao) {

		long inicio = System.currentTimeMillis();

		int i = 0;
		LogImportacao logImportacao = null;
		try {
			ResultVO resultVO = new ResultVO();
			boolean startsWith = StringUtils.startsWith(nomeOriginalFile, "modelo");
			TipoImportacao tipoImportacao;
			if (startsWith) {
				tipoImportacao = TipoImportacao.MODELO;
			} else {
				tipoImportacao = TipoImportacao.PROCESSO;
			}
			logImportacao = criarLogImportacaoInicial(tipoImportacao, file, usuario, nomeOriginalFile);

			List<Map<String, String>> mapList = criarMap(file, logImportacao, cabecalho);

			Long tipoProcessoId = tipoProcesso.getId();
			tipoProcesso = tipoProcessoService.get(tipoProcessoId);
			if(!mapList.isEmpty()) {
				if(isReimportacao) {
					reimportarProcessos(resultVO, mapList, logImportacao, inicio);
				} else {
					criarProcessos(resultVO, mapList, logImportacao, inicio, tipoProcesso);
				}
			}
			else if (TipoImportacao.MODELO.equals(tipoImportacao)){
				finalizarCriacaoModelo(resultVO, logImportacao, inicio, tipoProcesso);
			} else {
				atualizarLogDeImportacao(resultVO, "Arquivo invalido para importação", logImportacao);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			salvarLogImportacao(logImportacao);
			handleException(file, e, i);
		}
	}

	private void salvarLogImportacao(LogImportacao logImportacao) {
		LogImportacao logImportacao2 = logImportacao;
		TransactionWrapper tw = new TransactionWrapper(applicationContext);
		tw.setRunnable(() -> {
			logImportacao2.setStatus(StatusImportacao.ERRO);
			logImportacaoService.saveOrUpdate(logImportacao2);
		});
		tw.runNewThread(10 * 1000);
	}

	private LogImportacao criarLogImportacaoInicial(TipoImportacao tipoImportacao, File file, Usuario usuario, String nomeOriginalFile) throws Exception {
		Bolso<LogImportacao> logBolso = new Bolso<>();
		TransactionWrapper tw = new TransactionWrapper(applicationContext);
		tw.setRunnable(() -> {
			ResultVO resultVO = new ResultVO();
			LogImportacao logImportacao = logImportacaoService.criarLogImportacao(tipoImportacao, usuario, resultVO, file, nomeOriginalFile, StatusImportacao.PROCESSANDO);
			logBolso.setObjeto(logImportacao);
		});
		tw.runNewThread();
		tw.throwException();

		return logBolso.getObjeto();
	}

	private void criarProcessos(ResultVO resultVO, List<Map<String, String>> maps, LogImportacao logImportacao, long inicio, TipoProcesso tipoProcessoVazio) throws Exception {

		int linha = 1;
		Bolso<TipoProcesso> bolso = new Bolso<>();
		for (Map<String, String> map : maps) {
			systraceThread("Importando processo FIES/PROUNI " + linha + " de " + maps.size(), LogLevel.DEBUG);

			TransactionWrapper tw = new TransactionWrapper(applicationContext);
			int finalLinha = linha +1;
			tw.setRunnable(() -> {
				DummyUtils.sleep(150);
				try {
					TipoProcesso tipoProcesso = criarProcesso(map, resultVO, logImportacao, finalLinha, tipoProcessoVazio);
					bolso.setObjeto(tipoProcesso);
				}
				catch (Exception e) {
					String message = DummyUtils.getExceptionMessage(e);
					systraceThread("Erro: " + message + " map: " + map, LogLevel.ERROR);
					e.printStackTrace();
					atualizarLogDeImportacao(resultVO, message, logImportacao);
					throw e;
				}
			});
			tw.runNewThread();
			tw.throwException();

			linha++;
		}

		long fim = System.currentTimeMillis();
		long tempo = fim - inicio;

		TipoProcesso tipoProcesso = bolso.getObjeto();
		logImportacao.setTipoProcesso(tipoProcesso);
		logImportacao.setTempo(tempo);
		atualizarLogDeImportacao(resultVO, null, logImportacao);

		systraceThread("FIM DA IMPORTACAO DO ARQUIVO " + tipoProcesso.getNome());
	}

	private void reimportarProcessos(ResultVO resultVO, List<Map<String, String>> maps, LogImportacao logImportacao, long inicio) throws Exception {

		int linha = 1;
		Bolso<TipoProcesso> bolso = new Bolso<>();
		for (Map<String, String> map : maps) {
			systraceThread("Reimportando processo FIES/PROUNI " + linha + " de " + maps.size());

			TransactionWrapper tw = new TransactionWrapper(applicationContext);
			int finalLinha = linha +1;
			tw.setRunnable(() -> {
				DummyUtils.sleep(150);
				try {
					TipoProcesso tipoProcesso = reimportarProcesso(map, resultVO, logImportacao, finalLinha);
					bolso.setObjeto(tipoProcesso);
				}
				catch (Exception e) {
					String message = DummyUtils.getExceptionMessage(e);
					systraceThread("Erro: " + message + " map: " + map, LogLevel.ERROR);
					e.printStackTrace();
					atualizarLogDeImportacao(resultVO, message, logImportacao);
					throw e;
				}
			});
			tw.runNewThread();
			tw.throwException();

			linha++;
		}

		long fim = System.currentTimeMillis();
		long tempo = fim - inicio;

		TipoProcesso tipoProcesso = bolso.getObjeto();
		logImportacao.setTipoProcesso(tipoProcesso);
		logImportacao.setTempo(tempo);
		atualizarLogDeImportacao(resultVO, null, logImportacao);

		systraceThread("FIM DA IMPORTACAO DO ARQUIVO " + tipoProcesso.getNome());
	}

	private void finalizarCriacaoModelo(ResultVO resultVO, LogImportacao logImportacao, long inicio, TipoProcesso tipoProcesso) throws Exception {

		systraceThread("Importando modelo FIES/PROUNI");

		long fim = System.currentTimeMillis();
		long tempo = fim - inicio;

		logImportacao.setTipoProcesso(tipoProcesso);
		logImportacao.setTempo(tempo);
		atualizarLogDeImportacao(resultVO, null, logImportacao);

		systraceThread("FIM DA IMPORTACAO DO ARQUIVO " + tipoProcesso.getNome());
	}

	private void atualizarLogDeImportacao(ResultVO resultVO, String message, LogImportacao logImportacao) throws Exception {

		TransactionWrapper tw = new TransactionWrapper(applicationContext);
		tw.setRunnable(() -> {
			logImportacao.setInserts(resultVO.getInserts());
			logImportacao.setUpdates(resultVO.getUpdates());
			logImportacao.setCancelados(resultVO.getCancelados());
			logImportacao.setErro(message);
			logImportacao.setStatus(StringUtils.isNotBlank(message) ? StatusImportacao.ERRO : StatusImportacao.SUCESSO);
			logImportacaoService.saveOrUpdate(logImportacao);
		});
		tw.runNewThread();
		tw.throwException();
	}

	public Processo isCriarNovoProcesso(Map<String, String> map, TipoProcesso tipoProcesso, Aluno aluno) {

		Processo processo = null;

		ProcessoFiltro filtro = new ProcessoFiltro();
		filtro.setTiposProcesso(asList(tipoProcesso));
		filtro.setAluno(aluno);

		String periodoDeIngresso = map.get("PERIODO DE INGRESSO");
		if(StringUtils.isNotBlank(periodoDeIngresso)) {
			filtro.setCamposFiltro(CampoMap.CampoEnum.PERIODO_DE_INGRESSO_IMPORTACAO, Arrays.asList(periodoDeIngresso));
		}

		String chamada = map.get("CHAMADA");
		if(StringUtils.isNotBlank(chamada)) {
			filtro.setCamposFiltro(CampoMap.CampoEnum.NUMERO_CHAMADA, Arrays.asList(chamada));
		}

		List<Long> processoIds = processoService.findIdsByFiltro(filtro, null, null);
		if(!processoIds.isEmpty()) {
			Long processoId = processoIds.get(0);
			processo = processoService.get(processoId);
			systraceThread("Processo ja existente com o mesmo curso... Processo: " + processoId) ;
		}

		return processo;
	}

	public Processo buscaProcessoReimportacao(String processoIdStr) {

		Long processoId = Long.valueOf(processoIdStr);
		Processo processo = processoService.get(processoId);

		return processo;

	}

	public TipoProcesso criarProcesso(Map<String, String> map, ResultVO resultVO, LogImportacao logImportacao, int linha, TipoProcesso tipoProcesso) throws Exception {

		tratarDadosSisProuni(map);

		Long tipoProcessoId = tipoProcesso.getId();
		Set<TipoCampo> camposList = preencherCampos(map, tipoProcessoId);

		if(camposList != null) {
			String cpf = map.get("CPF");
			Aluno aluno = getAluno(map, cpf);
			Processo processo = isCriarNovoProcesso(map, tipoProcesso, aluno);
			boolean isAtualizar = (processo != null);
			if(isAtualizar) {
				//NÃO FAZ NADA. Alinhamento feito com Andreia: Importação não atualiza mais processos
				//atualizarProcesso(processo, camposList);
				//resultVO.addUpdate();
				resultVO.addCancelado();
			}
			else {
				processo = criarNovoProcesso(aluno, tipoProcesso, camposList);
				resultVO.addInsert();
				gravarLogDeImportacao(map, logImportacao, linha, processo, isAtualizar);
			}
		} else {
			resultVO.addCancelado();
		}

		return tipoProcesso;
	}

	public TipoProcesso reimportarProcesso(Map<String, String> map, ResultVO resultVO, LogImportacao logImportacao, int linha) throws Exception {

		//tratarDadosSisProuni(map);

		String processoIdStr = map.get(colunaProcesso);
		Processo processo = buscaProcessoReimportacao(processoIdStr);
		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();
		Set<TipoCampo> camposList = preencherCamposReimportacao(map, tipoProcessoId);
		if(camposList.isEmpty()) {
			return null;
		}

		boolean isAtualizar = (processo != null);
		if(isAtualizar) {
			atualizarProcesso(processo, camposList);
			resultVO.addUpdate();
		}
		else {
			systraceThread("Processo não encontrado. Processo ID: " + map.get("PROCESSO ID") + ".");
		}

		gravarLogDeImportacao(map, logImportacao, linha, processo, isAtualizar);

		return tipoProcesso;
	}

	private List<String> validarImportacao(List<String> cabecalhos, TipoProcesso tipoProcesso, boolean isReimportacao) throws Exception {

		Long tipoProcessoId = tipoProcesso.getId();
		List<String> camposFaltante = new ArrayList<>();

		List<TipoCampo> tipoCampos = tipoCampoService.findCamposComOrigemByTipoProcesso(tipoProcessoId);
		if(isReimportacao) {
			if(!cabecalhos.contains(colunaProcesso)) {
				camposFaltante.add(colunaProcesso);
			}

			List<String> origens = tipoCampos.stream().map(tipoCampo -> {
				String origem = tipoCampo.getOrigem().toUpperCase();
				origem = DummyUtils.substituirCaracteresEspeciais(origem);
				return origem;
			}).collect(Collectors.toList());
			for (String cabecalho : cabecalhos) {
				if(!origens.contains(cabecalho) && !colunaProcesso.equals(cabecalho)) {
					throw new MessageKeyException("campoNaoEncontradoOrigem.error", cabecalho);
				}
			}
		} else {
			for (TipoCampo tipoCampo : tipoCampos) {

				String origem = tipoCampo.getOrigem();
				origem = DummyUtils.substituirCaracteresEspeciais(origem);
				origem = origem.toUpperCase();
				if(isReimportacao) {
					continue;
				}
				if(!cabecalhos.contains(origem)) {
					camposFaltante.add(origem);
				}
			}
		}
			return camposFaltante;
	}

	private List<String> criarCabecalho(File file) {

		String readLine = null;
		List<String> cabecalhos = new ArrayList<>();

		try {
			BufferedReader bufferedReader = getBufferedReader(file);
			readLine = bufferedReader.readLine();
			List<String> valorLinhas = asList(readLine.split(";"));

			for (String valorLinha : valorLinhas) {
				valorLinha = valorLinha.trim();
				String key = DummyUtils.htmlToString(valorLinha);
				key = DummyUtils.substituirCaracteresEspeciais(key);
				key = key.toUpperCase();
				cabecalhos.add(key);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return cabecalhos;
	}

	private void gravarLogDeImportacao(Map<String, String> map, LogImportacao logImportacao, int linha, Processo processo, boolean atualizou) {

		StringBuilder observacao = new StringBuilder();
		observacao.append(atualizou ? "Registro de Atualização <br>" : "");
		observacao.append("Dados do arquivo ").append("<br><br>");
		observacao.append("Linha: " + linha).append(" <br>");
		observacao.append("Arquivo: " + logImportacao.getNomeArquivo()).append(" <br>");
		observacao.append("Valores: ").append(" <br>");
		Set<String> keys = map.keySet();
		for (String key : keys) {
			String value = map.get(key);
			observacao.append(" - ").append(key).append(": ").append(value).append("<br>");
		}

		ProcessoLog log = new ProcessoLog();
		log.setAcao(AcaoProcesso.IMPORTACAO_PROCESSO);
		log.setObservacao(observacao.toString());
		log.setProcesso(processo);
		log.setLogImportacao(logImportacao);

		processoLogService.saveOrUpdate(log);
	}

	private Processo criarNovoProcesso(Aluno aluno, TipoProcesso tipoProcesso, Set<TipoCampo> list) throws Exception {

		Set<CampoAbstract> valoresCampos = new LinkedHashSet<>();
		valoresCampos.addAll(list);

		CriacaoProcessoVO vo = new CriacaoProcessoVO();
		vo.setTipoProcesso(tipoProcesso);
		vo.setAluno(aluno);
		vo.setValoresCampos(valoresCampos);
		vo.setOrigem(Origem.WEB);
		vo.setAcao(AcaoProcesso.IMPORTACAO);

		ProcessoService.LOCK_ENABLED.set(false);

		return processoService.criaProcesso(vo);
	}

	private void atualizarProcesso(Processo processo, Set<TipoCampo> list) throws Exception {
		Long processoId = processo.getId();
		systraceThread("Atualizando processo: " + processoId, LogLevel.DEBUG);

		Map<Long, TipoCampo> tipoCampoMap = new HashMap<>();
		for (TipoCampo tipoCampo : list) {
			Long tipoCampoId = tipoCampo.getId();
			tipoCampoMap.put(tipoCampoId, tipoCampo);
		}
		List<Campo> campos = campoService.findByProcesso(processoId);
		Map<Long, String> valores = new LinkedHashMap<>();
		for (Campo campo : campos) {
			Long tipoCampoId = campo.getTipoCampoId();
			TipoCampo tipoCampo = tipoCampoMap.get(tipoCampoId);
			if(tipoCampo != null) {
				String valor = tipoCampo.getValor();
				Long campoId = campo.getId();
				valores.put(campoId, valor);
			}
		}

		Set<CampoGrupo> gruposCampos = processo.getGruposCampos();
		EditarProcessoVO vo = new EditarProcessoVO();
		vo.setProcessoId(processoId);
		vo.setValores(valores);
		vo.setGrupos((new ArrayList<>(gruposCampos)));
		vo.setValidarCampos(false);
		processoService.atualizarProcesso(vo);
	}

	private Set<TipoCampo> preencherCampos(Map<String, String> map, Long tipoProcessoId) {
		Map<TipoCampoGrupo, List<TipoCampo>> mapCampos = tipoCampoService.findMapByTipoProcesso(tipoProcessoId);

		Set<TipoCampo> list = new LinkedHashSet<>();
		for (TipoCampoGrupo grupo : mapCampos.keySet()) {
			Set<TipoCampo> campos = grupo.getCampos();
			for(TipoCampo campo : campos) {
				list.add(campo);
			}
		}

		Set<TipoCampo> camposComOrigemList = new LinkedHashSet<>();
		for(TipoCampo tipoCampo : list) {
			String origem = tipoCampo.getOrigem();

			if(StringUtils.isBlank(origem)) continue;

			origem = DummyUtils.substituirCaracteresEspeciais(origem);
			origem = origem.toUpperCase();
			String valor = map.get(origem);

			boolean obrigatorio = tipoCampo.getObrigatorio();

			if(StringUtils.isBlank(valor) && obrigatorio) {
				return null;
			}

			tipoCampo.setValor(valor);
			camposComOrigemList.add(tipoCampo);
		}
		return camposComOrigemList;
	}

	private Set<TipoCampo> preencherCamposReimportacao(Map<String, String> map, Long tipoProcessoId) {

		List<TipoCampo> camposComOrigemByTipoProcesso = tipoCampoService.findCamposComOrigemByTipoProcesso(tipoProcessoId);

		Set<TipoCampo> camposComOrigemList = new LinkedHashSet<>();
		if(!camposComOrigemByTipoProcesso.isEmpty()) {
			for(TipoCampo tipoCampo : camposComOrigemByTipoProcesso) {
				String origem = tipoCampo.getOrigem();

				origem = DummyUtils.substituirCaracteresEspeciais(origem);
				origem = origem.toUpperCase();
				String valor = map.get(origem);
				if(valor != null) {
					tipoCampo.setValor(valor);
					camposComOrigemList.add(tipoCampo);
				}
			}
		}

		return camposComOrigemList;
	}

	private Aluno getAluno(Map<String, String> map, String cpf) throws Exception {

		AlunoFiltro filtro = new AlunoFiltro();
		filtro.setCpf(cpf);
		filtro.setOrdenar("aluno.id", SortOrder.DESCENDING);
		List<Aluno> alunos = alunoService.findByFiltro(filtro, null, null);
		Aluno aluno = null;
		if (alunos.isEmpty()) {
			systraceThread("Aluno não existe na base.");
			Bolso<Aluno> alunoBolso = new Bolso<>();
			TransactionWrapper tw = new TransactionWrapper(applicationContext);
			tw.setRunnable(() -> {
				Aluno aluno1 = criarAluno(map);
				alunoBolso.setObjeto(aluno1);
			});
			tw.runNewThread();
			tw.throwException();
			aluno = alunoBolso.getObjeto();
		}
		else {
			Aluno aluno1 = alunos.get(0);
			if (alunos.size() > 1) {
				String nomeAluno = aluno1.getNome();
				nomeAluno = nomeAluno.toUpperCase();

				String nomeCandidato = map.get("NOME CANDIDATO");
				nomeCandidato = nomeCandidato.toUpperCase();

				if (!nomeCandidato.equals(nomeAluno)) {
					aluno1.setNome(nomeCandidato);
					alunoService.saveOrUpdate(aluno1, null);
				}
				aluno = aluno1;
			} else {
				aluno = aluno1;
			}
		}
		return aluno;
	}

	private void tratarDadosSisProuni(Map<String, String> map) {

		String cpf = map.get("CPF");

		if(StringUtils.isBlank(cpf)) {
			cpf = Aluno.CPF_ALUNO_GENERICO;
			String curso = map.get("CURSO");
			String[] split = curso.split("-");
			curso = split.length > 1 ? split[1].trim() : curso;
			map.put("CURSO", curso);
		}

		map.put("CPF", DummyUtils.getCpfCnpj(cpf));
	}

	private String getChaveUnicidadeToCampo(String valor, String colunaBaseInterna, Long baseInternaId) {
		String chaveUnicidade = "";
		List<RegistroValorVO> registros = baseRegistroService.findByRelacionados(baseInternaId, asList(valor), colunaBaseInterna);
		for(RegistroValorVO registro : registros) {
			String label = registro.getLabel();
			if(label.equals(valor)) {
				BaseRegistro baseRegistro = registro.getBaseRegistro();
				chaveUnicidade = baseRegistro.getChaveUnicidade();
			}
		}
		return chaveUnicidade;
	}

	private void handleException(File file, Exception e, int idx) {
		handleException(file, e, idx, true);
	}

	private void handleException(File file, Exception e, int idx, boolean moveFile) {

		e.printStackTrace();
		String parent = file.getParent();
		String name = file.getName();
		String name2 = "ERRO-idx" + idx + "-" + name;

		try {
			File logFile = new File(parent, name2 + ".log");
			String stackTrace = ExceptionUtils.getStackTrace(e);
			FileUtils.writeStringToFile(logFile, stackTrace, "UTF-8");

			if(moveFile) {
				File destFile = new File(parent, "ERRO-idx" + idx + "-" + name);
				systraceThread("tentando copiar " + file.getAbsolutePath() + " para " + destFile.getAbsolutePath());
				FileUtils.moveFile(file, destFile);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
	}

	private List<Map<String, String>> criarMap(File file, LogImportacao logImportacao, List<String> cabecalhos) throws IOException {

		BufferedReader bufferedReader = getBufferedReader(file);
		List<Map<String, String>> listMap = new ArrayList<>();

		int linha = 0;
		String line = null;
		try {

			Map<String, String> map = new LinkedHashMap<>();
			Map<String, String> dadosAdicionaisMap = new LinkedHashMap<>();
			while ((line = bufferedReader.readLine()) != null) {
				linha++;
				systraceThread("Processando linha: " + linha);

				List<String> valorLinhas = asList(line.split(";"));
				int idx = 0;
				boolean isCabecalho = (linha == 1);
				for(String valorLinha : valorLinhas) {
					valorLinha = valorLinha.trim();

					if(!cabecalhos.isEmpty() && !isCabecalho) {
						String key = cabecalhos.get(idx);
						String value = DummyUtils.htmlToString(valorLinha);
						map.put(key, value);
						idx++;
					}
				}

				if(!map.isEmpty()) {
					map.putAll(dadosAdicionaisMap);
					listMap.add(map);
					map = new LinkedHashMap<>();
				}
			}
		}
		catch (Exception e) {
			String exceptionMessage = DummyUtils.getExceptionMessage(e);
			systraceThread("Erro na linha " + linha + " do arquivo " + file.getAbsolutePath() + ": " + exceptionMessage, LogLevel.ERROR);
			String erro = "Linha " + linha + ": " + line;
			systraceThread(erro, LogLevel.ERROR);
			logImportacao.setErro("Erro ao processar Arquivo: " + erro);
			e.printStackTrace();
			throw e;
		}
		finally {
			bufferedReader.close();
		}

		return listMap;
	}

	private BufferedReader getBufferedReader(File file) throws FileNotFoundException {
		String name = file.getName();
		String extensao = DummyUtils.getExtensao(name);
		extensao = StringUtils.isNotBlank(extensao) ? extensao.toLowerCase() : null;
		Charset charset = "csv".equals(extensao) ? StandardCharsets.ISO_8859_1 : StandardCharsets.UTF_8;
		FileInputStream fis = new FileInputStream(file);
		return new BufferedReader(new InputStreamReader(fis, charset));
	}

	private Aluno criarAluno(Map<String, String> map) {

		String cpfString = map.get("CPF");
		String cpf = DummyUtils.getCpf(cpfString);

		AlunoFiltro filtro = new AlunoFiltro();
		filtro.setCpf(cpf);
		List<Aluno> list = alunoService.findByFiltro(filtro, 0, 1);
		if (!list.isEmpty()) {
			//DummyUtils.systraceThread("Aluno já existe na base");
			return list.iterator().next();
		}

		String nome = map.get("NOME CANDIDATO");

		Aluno aluno = new Aluno();
		aluno.setNome(nome);
		aluno.setCpf(cpf);

		alunoService.saveOrUpdate(aluno, null);

		return aluno;
	}
	public String getPath(String matricula) {
		String path = resourceService.getValue(ResourceService.IMPORTACAO_DADOS_PATH);
		path += matricula;
		return path;
	}

	@Transactional(rollbackFor=Exception.class)
	public void iniciarProcessamentoDoArquivo(File file, Usuario usuario, String nomeOriginalFile, boolean isReimportacao) throws Exception {

		final String path = getPath("matricula");
		final String fileName = file.getName();

		List<String> cabecalho = criarCabecalho(file);

		TipoProcesso tipoProcesso = cabecalho.contains("TIPO PROUNI") ? new TipoProcesso(TipoProcesso.SIS_PROUNI) : new TipoProcesso(TipoProcesso.SIS_FIES);

		List<String> camposFaltantes = validarImportacao(cabecalho, tipoProcesso, isReimportacao);
		if(!camposFaltantes.isEmpty()) {
			throw new MessageKeyException("campoNaoEncontrado.error", camposFaltantes);
		}

		systraceThread("processando arquivo matricula: " + file.getAbsolutePath(), LogLevel.DEBUG);

		TransactionWrapper tw = new TransactionWrapper(applicationContext);
		tw.setRunnable(() -> {
			processarArquivo(file, usuario, nomeOriginalFile, cabecalho, tipoProcesso, isReimportacao);
			if(file.exists()) {
				File destDir = new File(path + "/historico");
				File destFile = DummyUtils.getFileDestino(destDir, fileName);
				try {
					FileUtils.moveFile(file, destFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		tw.startThread();

		DummyUtils.sleep(450);
	}

	public File criaColunaProcessoId(File file, String fileName, LogImportacao logImportacao) throws IOException {

		BufferedReader bufferedReader = getBufferedReader(file);
		int linha = 0;
		String line = null;

		File fileTemp = File.createTempFile("importacao-processo", ".csv");
		DummyUtils.deleteOnExitFile(fileTemp);

		Long logImportacaoId = logImportacao.getId();

		try {
			ExcelCsvWriter ecw = new ExcelCsvWriter();
			String absolutePath = fileTemp.getAbsolutePath();
			BufferedWriter writer = Files.newBufferedWriter(Paths.get(absolutePath));
			CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
					.withDelimiter(';')
					.withAllowMissingColumnNames()
					.withTrim()
			);
			ecw.setWriter(csvPrinter);

			int idxCpf = 0;
			int sizeCabecalho = 0;

			while ((line = bufferedReader.readLine()) != null) {

				systraceThread("Processando linha: " + linha);
				ecw.criaLinha(linha);

				List<String> valorLinhas = asList(line.split(";"));
				valorLinhas = new ArrayList<>(valorLinhas);

				if (linha == 0) {
					List<String> cabecalho = criarCabecalho(file);
					sizeCabecalho = cabecalho.size();

					if (cabecalho.contains(colunaProcesso)) {
						return file;
					} else {
						cabecalho.add(colunaProcesso);
					}
					for (int i = 0; i <= sizeCabecalho; i++) {
						String coluna = cabecalho.get(i);
						if (coluna.equals("CPF")) {
							idxCpf = i;
							break;
						}
					}

					cabecalho.stream().forEach(coluna -> ecw.escrever(coluna));
					linha++;
					continue;
				}
				String cpf = valorLinhas.get(idxCpf);
				cpf = DummyUtils.getCpf(cpf);

				Long processoId = null;

				AlunoFiltro filtro = new AlunoFiltro();
				filtro.setCpf(cpf);
				filtro.setOrdenar("aluno.id", SortOrder.DESCENDING);
				List<Aluno> alunos = alunoService.findByFiltro(filtro, 0, 1);
				if(!alunos.isEmpty()) {
					Aluno aluno = alunos.get(0);
					ProcessoFiltro processoFiltro = new ProcessoFiltro();
					processoFiltro.setAluno(aluno);
					processoFiltro.setTiposProcesso(Arrays.asList(new TipoProcesso(TipoProcesso.SIS_PROUNI), new TipoProcesso(TipoProcesso.SIS_FIES)));
					processoFiltro.setLogImportacaoId(logImportacaoId);

					List<Processo> processos = processoService.findByFiltro(processoFiltro, null, null);
					Processo processo = processos.isEmpty() ? null : processos.get(0);
					processoId = processo != null ? processo.getId() : null;
				}

				int sizeValores = valorLinhas.size();
				if(sizeCabecalho > sizeValores) {
					for(int i = sizeValores; i < sizeCabecalho; i++) {
						valorLinhas.add("");
					}
				}

				valorLinhas.stream().forEach(valor -> 
						ecw.escrever(valor)
				);

				ecw.escrever(processoId);
				linha++;
			}

			ecw.close(fileTemp);
			bufferedReader.close();
		}
		catch (Exception e) {
			String exceptionMessage = DummyUtils.getExceptionMessage(e);
			systraceThread("Erro na linha " + linha + " do arquivo " + file.getAbsolutePath() + ": " + exceptionMessage, LogLevel.ERROR);
			String erro = "Linha " + linha + ": " + line;
			systraceThread(erro, LogLevel.ERROR);

			e.printStackTrace();
			throw e;
		}

		return fileTemp;
	}
}