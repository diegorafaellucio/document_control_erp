package net.wasys.getdoc.domain.service;

import com.fasterxml.jackson.databind.SerializationFeature;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.repository.BaseRegistroRepository;
import net.wasys.getdoc.domain.vo.ColunaNovaVO;
import net.wasys.getdoc.domain.vo.LinhaVO;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.getdoc.domain.vo.ResultVO;
import net.wasys.getdoc.domain.vo.filtro.BaseRegistroFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.excel.ExcelFormat;
import net.wasys.util.excel.ExcelWriter;
import net.wasys.util.other.SuperBeanComparator;
import net.wasys.util.rest.jackson.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import static net.wasys.util.DummyUtils.systraceThread;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
public class BaseRegistroService {

	@Autowired private BaseInternaService baseInternaService;
	@Autowired private BaseRegistroRepository baseRegistroRepository;
	@Autowired private BaseRegistroValorService baseRegistroValorService;
	@Autowired private BaseRelacionamentoService baseRelacionamentoService;
	@Autowired private ApplicationContext applicationContext;
	@Autowired private TipoCampoService tipoCampoService;
	@Autowired private SessionFactory sessionFactory;

	public BaseRegistro get(Long id) {
		return baseRegistroRepository.get(id);
	}

	@Transactional(rollbackFor = Exception.class)
	public void saveOrUpdate(BaseRegistro baseRegistro, Map<String, String> valoresMap, Usuario usuario) throws MessageKeyException {
		try {
			Long registroId = baseRegistro.getId();
			BaseInterna baseInterna = baseRegistro.getBaseInterna();

			String chaveUnicidade = gerarChaveUnicidadeJson(baseInterna, valoresMap);
			baseRegistro.setChaveUnicidade(chaveUnicidade);
			baseRegistro.setDataAtualizacao(new Date());
			baseRegistroRepository.saveOrUpdate(baseRegistro);

			if (registroId != null) {
				baseRegistroValorService.deleteByRegistro(registroId);
			}

			baseRegistroValorService.criarRegistroValor(valoresMap, baseRegistro);
		} catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
		}
	}

	public ResultVO atualizarRegistros(BaseInterna baseInterna, List<LinhaVO> linhas, Usuario usuario, File file, String fileName) throws Exception {

		ResultVO result = new ResultVO();
		Set<Long> idsUtilizados = new HashSet<>();

		try {
			int size = linhas.size();
			systraceThread("Inicio da atualização de registros. Quantidade de linhas: " + size);
			int count = 0;
			do {
				List<LinhaVO> linhas2 = new ArrayList<>();
				for (int i = 0; i < 200 && !linhas.isEmpty(); i++) {
					LinhaVO linha = linhas.remove(0);
					linhas2.add(linha);
				}

				long inicio = System.currentTimeMillis();

				TransactionWrapper tw = new TransactionWrapper();
				tw.setApplicationContext(applicationContext);
				tw.setRunnable(() -> {
					BaseRegistroService baseRegistroService = applicationContext.getBean(BaseRegistroService.class);
					Set<Long> idsUtilizados2 = baseRegistroService.atualizarRegistros(baseInterna, linhas2, result);
					idsUtilizados.addAll(idsUtilizados2);
				});
				tw.runNewThread();

				Exception exception = tw.getException();
				if (exception != null) {
					throw exception;
				}

				count += linhas2.size();
				long fim = System.currentTimeMillis();
				long tempo = fim - inicio;
				systraceThread("Atualizando registros. " + count + " de " + size + ". " + tempo + "ms.");
			}
			while (!linhas.isEmpty());

			if (!idsUtilizados.isEmpty()) {

				TransactionWrapper tw = new TransactionWrapper();
				tw.setApplicationContext(applicationContext);
				tw.setRunnable(() -> {
					BaseRegistroService baseRegistroService = applicationContext.getBean(BaseRegistroService.class);
					int deletes = baseRegistroService.deleteNotIn(baseInterna, idsUtilizados);
					result.addDeletes(deletes);
				});
				tw.runNewThread();

				Exception exception = tw.getException();
				if (exception != null) {
					throw exception;
				}
			}
		} catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
		} finally {
			systraceThread("Finalização da atualização de registros. " + result.toString());
			TransactionWrapper tw = new TransactionWrapper();
			tw.setApplicationContext(applicationContext);
			tw.setRunnable(() -> {
				baseInternaService.finalizarImportacao(baseInterna, usuario, result, file, fileName);
			});
			tw.runNewThread();
		}

		return result;
	}

	@Transactional(rollbackFor = Exception.class)
	public int deleteNotIn(BaseInterna baseInterna, Set<Long> idsUtilizados) {
		return baseRegistroRepository.deleteNotIn(baseInterna.getId(), idsUtilizados);
	}

	@Transactional(rollbackFor = Exception.class)
	public Set<Long> atualizarRegistros(BaseInterna baseInterna, List<LinhaVO> linhas, ResultVO result) {

		Set<Long> idsUtilizados = new LinkedHashSet<>();
		for (LinhaVO linha : linhas) {

			String chave = linha.getChaveUnicidade();
			Map<String, String> colunaValor = linha.getColunaValor();
			BaseRegistro registro = criarOuCarregarBaseRegistro(baseInterna.getId(), chave);

			if (registro.getId() == null) {
				registro.setBaseInterna(baseInterna);
				registro.setChaveUnicidade(chave);
				result.addInsert();
			} else {
				baseRegistroValorService.removerRegistroValores(registro.getId());
				result.addUpdate();
			}

			registro.setDataAtualizacao(new Date());
			baseRegistroRepository.saveOrUpdateWithoutFlush(registro);

			baseRegistroValorService.criarRegistroValor(colunaValor, registro);

			idsUtilizados.add(registro.getId());
		}

		return idsUtilizados;
	}

	public void adicionarColuna(BaseInterna baseInterna, ColunaNovaVO colunaNova) {

		Long baseInternaId = baseInterna.getId();
		List<BaseRegistro> registrosBaseInterna = baseRegistroRepository.findByBaseInternaId(baseInternaId);

		for (BaseRegistro baseRegistro : registrosBaseInterna) {

			Map<String, String> colunaValor = new HashMap<>(0);
			colunaValor.put(colunaNova.getNome(), "");

			baseRegistroValorService.criarRegistroValor(colunaValor, baseRegistro);
		}
	}

	private BaseRegistro criarOuCarregarBaseRegistro(Long baseInternaId, String chaveUnicidade) {

		BaseRegistro registro = baseRegistroRepository.findByBaseInternaIdAndChaveUnicidade(baseInternaId, chaveUnicidade);

		return registro == null ? new BaseRegistro() : registro;
	}

	public BaseRegistro carregarBaseRegistro(Long baseInternaId, String chaveUnicidade) {

		BaseRegistro registro = baseRegistroRepository.findByBaseInternaIdAndChaveUnicidade(baseInternaId, chaveUnicidade);

		return registro;
	}

	public int countByFiltro(BaseRegistroFiltro filtro) {
		return baseRegistroRepository.countByFiltro(filtro);
	}

	public List<RegistroValorVO> findByFiltro(BaseRegistroFiltro filtro, Integer first, Integer pageSize) {
		return findByFiltro(filtro, first, pageSize, false);
	}

	public List<RegistroValorVO> findByFiltro(BaseRegistroFiltro filtro, Integer first, Integer pageSize, boolean cache) {

		BaseInterna baseInterna = filtro.getBaseInterna();
		if(baseInterna == null) {
			return new ArrayList<>();
		}

		Integer qntColunas = filtro.getQntColunas();
		if (qntColunas == null && (first != null || pageSize != null)) {
			Long baseInternaId = baseInterna.getId();
			List<String> colunas = baseRegistroValorService.getColunasRegistro(baseInternaId);
			filtro.setQntColunas(colunas.size());
		}

		List<Map<String, Object>> mapRegistro = baseRegistroRepository.findByFiltro(filtro, first, pageSize, cache);

		List<RegistroValorVO> vos = groupRegistroValor(mapRegistro);
		Collections.sort(vos, new SuperBeanComparator<>("label"));
		return vos;
	}

	public List<RegistroValorVO> findValoresByBaseInternaAndChaveUnicidade(Long id, String chaveUnicidade) {

		List<Map<String, Object>> mapRegistro = baseRegistroRepository.findValoresByBaseInternaAndChaveUnicidade(id, chaveUnicidade);

		return groupRegistroValor(mapRegistro);
	}

	public String montarChaveUnicidadeSemCoringa(BaseInterna baseInterna, Map<String, String> colunaValor) {

		String colunasUnicidade = baseInterna.getColunasUnicidade();

		String chaveUnicidade = null;

		if (!colunaValor.isEmpty()) {
			chaveUnicidade = trocarColunaPorValorSemCoringa(colunasUnicidade, colunaValor);
		}

		return chaveUnicidade;
	}

	public String montarChaveUnicidade(BaseInterna baseInterna, Map<String, String> colunaValor) {

		String colunasUnicidade = baseInterna.getColunasUnicidade();

		String chaveUnicidade = null;

		if (!colunaValor.isEmpty()) {
			chaveUnicidade = trocarColunaPorValor(colunasUnicidade, colunaValor);
			chaveUnicidade = trocarColunaRestantePorCoringa(colunasUnicidade, chaveUnicidade);
		}

		return chaveUnicidade;
	}

	private String trocarColunaPorValor(String colunasUnicidade, Map<String, String> colunaValor) {

		String chaveUnicidade = colunasUnicidade;

		for (Entry<String, String> entry : colunaValor.entrySet()) {

			if (!StringUtils.isBlank(entry.getValue())) {
				chaveUnicidade = chaveUnicidade.replace(entry.getKey(), "%" + entry.getValue() + "%");
			}
		}

		return chaveUnicidade.equals(colunasUnicidade) ? null : chaveUnicidade;
	}

	private String trocarColunaPorValorSemCoringa(String colunasUnicidade, Map<String, String> colunaValor) {

		String chaveUnicidade = colunasUnicidade;

		for (Entry<String, String> entry : colunaValor.entrySet()) {

			if (!StringUtils.isBlank(entry.getValue())) {
				chaveUnicidade = chaveUnicidade.replace(entry.getKey(), entry.getValue());
			}
		}

		return chaveUnicidade.equals(colunasUnicidade) ? null : chaveUnicidade;
	}

	private String trocarColunaRestantePorCoringa(String colunasUnicidade, String chaveUnicidade) {

		if (chaveUnicidade != null) {
			JSONArray colunasUnicidadeJson = new JSONArray(colunasUnicidade);
			for (Object coluna : colunasUnicidadeJson) {
				chaveUnicidade = chaveUnicidade.replace((String) coluna, "%%");
			}
		}

		return chaveUnicidade;
	}

	private List<RegistroValorVO> groupRegistroValor(List<Map<String, Object>> mapRegistro) {

		Map<Long, RegistroValorVO> mapRegistroValor = new LinkedHashMap<>(mapRegistro.size());
		BaseRegistroValor valor;
		for (Map<String, Object> map : mapRegistro) {

			valor = mapearRegistroValor(map);
			Long registroId = (Long) map.get("0");
			BaseRegistro baseRegistro = (BaseRegistro) map.get("4");

			if (mapRegistroValor.containsKey(registroId)) {
				RegistroValorVO vo = mapRegistroValor.get(registroId);
				vo.getMapColunaRegistroValor().put(valor.getNome(), valor);
			} else {
				RegistroValorVO vo = new RegistroValorVO();
				vo.setRegistroId(registroId);
				vo.setBaseRegistro(baseRegistro);
				vo.getMapColunaRegistroValor().put(valor.getNome(), valor);
				mapRegistroValor.put(registroId, vo);
			}
		}

		List<RegistroValorVO> vos = new ArrayList<>(mapRegistroValor.values());
		return vos;
	}

	private BaseRegistroValor mapearRegistroValor(Map<String, Object> map) {
		BaseRegistroValor valor = new BaseRegistroValor();
		valor.setId((Long) map.get("1"));
		valor.setNome((String) map.get("2"));
		valor.setValor((String) map.get("3"));
		valor.setBaseRegistro((BaseRegistro) map.get("4"));
		return valor;
	}

	private String gerarChaveUnicidadeJson(BaseInterna baseInterna, Map<String, String> valores) {

		List<String> colunasUnicidade = baseInterna.getColunasUnicidadeList();
		String chaveUnicidade = "";
		for (String coluna : colunasUnicidade) {
			String valor = valores.get(coluna);
			chaveUnicidade = atualizarChaveUnicidadeJson(colunasUnicidade, chaveUnicidade, coluna, valor);
		}
		return chaveUnicidade;
	}

	public static String atualizarChaveUnicidadeJson(List<String> colunasUnicidade, String chaveAtualJson, String nomeColuna, String valorColuna) {

		ObjectMapper om = new ObjectMapper();
		om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

		String chaveUnicidade = null;
		int indexColunaUnicidade = colunasUnicidade.indexOf(nomeColuna);
		if (isBlank(chaveAtualJson)) {
			JSONArray jsonArray = new JSONArray();
			jsonArray.put(indexColunaUnicidade, valorColuna);
			chaveUnicidade = om.writeValueAsString(jsonArray);
		} else {
			JSONArray chaveJson = new JSONArray(chaveAtualJson);
			chaveJson.put(indexColunaUnicidade, valorColuna);
			chaveUnicidade = om.writeValueAsString(chaveJson);
		}

		return chaveUnicidade;
	}

	public void excluir(Long registroId, Usuario usuario) {
		try {
			baseRegistroRepository.deleteById(registroId);
		} catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
	}

	public Map<String, String> valoresBaseEstrangeira(Collection<BaseRelacionamento> relacionamentos, String nomeColuna) {

		BaseRelacionamento relacionamento = baseRelacionamentoService.buscarRelacionamentoDaColuna(relacionamentos, nomeColuna);

		Map<String, String> valoresChaveEstrangeira = new LinkedHashMap<>();

		if (relacionamento != null) {

			BaseInterna baseEstrangeira = relacionamento.getBaseExtrangeira();
			List<String> colunasUnicidadeBaseEstrangeira = baseEstrangeira.getColunasUnicidadeList();
			String colunaLabel = baseEstrangeira.getColunaLabel();

			BaseRegistroFiltro baseRegistroFiltro = new BaseRegistroFiltro();
			baseRegistroFiltro.setBaseInterna(baseEstrangeira);
			List<RegistroValorVO> valores = findByFiltro(baseRegistroFiltro, null, null);

			for (RegistroValorVO v : valores) {

				BaseRegistro baseRegistro = v.getBaseRegistro();
				baseRegistroRepository.deatach(baseRegistro);

				Map<String, BaseRegistroValor> mapColunaRegistroValor = v.getMapColunaRegistroValor();

				StringBuilder valoresUnicidadeBaseEstrangeira = new StringBuilder();
				for (String colunaUnicidadeBaseEstrangeira : colunasUnicidadeBaseEstrangeira) {

					if (StringUtils.isNotBlank(valoresUnicidadeBaseEstrangeira)) {
						valoresUnicidadeBaseEstrangeira.append(" - ");
					}

					BaseRegistroValor baseRegistroValor = mapColunaRegistroValor.get(colunaUnicidadeBaseEstrangeira);
					String valor = baseRegistroValor != null ? baseRegistroValor.getValor() : "";
					valoresUnicidadeBaseEstrangeira.append(valor);
				}

				BaseRegistroValor labelValor = mapColunaRegistroValor.get(colunaLabel);
				String label = labelValor != null ? labelValor.getValor() : "";
				valoresChaveEstrangeira.put(valoresUnicidadeBaseEstrangeira.toString(), label);
			}
		}

		return valoresChaveEstrangeira;
	}

	public Date getUltimaDataAtualizacao(Long baseInternaId) {
		return baseRegistroRepository.getUltimaDataAtualizacao(baseInternaId);
	}

	@Transactional(rollbackFor = Exception.class)
	public void atualizar(Long baseInternaId, List<Map<String, String>> list) {

		BaseInterna baseInterna = baseInternaService.get(baseInternaId);

		systraceThread("Atualizando " + baseInterna + ", " + list.size() + " registros: " + list);

		for (Map<String, String> map : list) {

			String chaveUnicidade = gerarChaveUnicidadeJson(baseInterna, map);
			BaseRegistroFiltro filtro = new BaseRegistroFiltro();
			filtro.setBaseInterna(baseInterna);
			String geralId = map.get("id");
			filtro.addCampoFiltro("id", geralId);

			List<BaseRegistro> baseRegistroList = baseRegistroRepository.findRegistroByFiltro(filtro);
			BaseRegistro  br = baseRegistroList.size() > 0 ? baseRegistroList.get(0) : null;
			if(br == null) {
				br = baseRegistroRepository.getByChaveUnicidade(baseInternaId, chaveUnicidade);
			}
			br = br != null ? br : new BaseRegistro();
			br.setChaveUnicidade(chaveUnicidade);
			br.setBaseInterna(baseInterna);

			String dataAtualizacaoStr = map.get("dataAtualizacao");
			if (StringUtils.isBlank(dataAtualizacaoStr)) {
				br.setDataAtualizacao(new Date());
			} else {
				Date dataAtualizacao = DummyUtils.parseDateTime2(dataAtualizacaoStr);
				br.setDataAtualizacao(dataAtualizacao);
				map.remove("dataAtualizacao");
			}

			String ativoStr = map.get("ativo");
			if (StringUtils.isBlank(ativoStr)) {
				ativoStr = map.get("ativa");
			}
			if (StringUtils.isBlank(ativoStr)) {
				br.setAtivo(true);
			} else {
				br.setAtivo(Boolean.valueOf(ativoStr));
				map.remove("ativo");
				map.remove("ativa");
			}
			map.remove("usuarioUltimaAtualizacaoId");
			try {
				baseRegistroRepository.saveOrUpdate(br);
				Long registroId = br.getId();
				if (registroId != null) {
					baseRegistroValorService.deleteByRegistro(registroId);
				}
				baseRegistroValorService.criarRegistroValor(map, br);
			} catch (RuntimeException e) {
				HibernateRepository.verifyConstrantViolation(e);
			}
		}
	}

	public Map<String, String> toChaveLabelMap(BaseInterna bi, List<RegistroValorVO> valores) {
		Map<String, String> opcoesMap = new LinkedHashMap<>();
		for (RegistroValorVO vo2 : valores) {
			BaseRegistro baseRegistro = vo2.getBaseRegistro();
			String chaveUnicidade = baseRegistro.getChaveUnicidade();
			String label = vo2.getLabel();
			opcoesMap.put(chaveUnicidade, label);
		}
		opcoesMap = DummyUtils.sortByValue(opcoesMap);
		return opcoesMap;
	}

	public List<String> findChaveUnicidadeByTipoCampo(long tipoCampoId, String valorPesquisa) {

		TipoCampo tipoCampo = tipoCampoService.get(tipoCampoId);
		BaseInterna baseInterna = tipoCampo.getBaseInterna();

		if(baseInterna != null) {
			long baseInternaId = baseInterna.getId();
			List<String> chavesUnicidade = findChaveUnicidadeByPesquisa(baseInternaId, valorPesquisa);
			return chavesUnicidade;
		}

		return null;
	}

	public String getLabel(Long baseInternaId, String chaveUnicidade) {
		if(StringUtils.isBlank(chaveUnicidade)) {
			return null;
		}
		return baseRegistroRepository.getLabel(baseInternaId, chaveUnicidade);
	}

	public List<String> findChaveUnicidadeByPesquisa(Long baseInternaId, String valorPesquisa) {
		return baseRegistroRepository.findChaveUnicidadeByPesquisa(baseInternaId, valorPesquisa);
	}

	public List<RegistroValorVO> findByBaseInterna(Long baseInternaId) {
		return findByBaseInterna(baseInternaId, false);
	}

	public List<RegistroValorVO> findByBaseInterna(Long baseInternaId, boolean cache) {

		BaseRegistroFiltro filtro = new BaseRegistroFiltro();
		BaseInterna baseInterna = baseInternaService.get(baseInternaId);

		if(baseInterna == null) {
			return new ArrayList<>();
		}

		filtro.setBaseInterna(baseInterna);
		filtro.setAtivo(true);

		return findByFiltro(filtro, null, null, cache);
	}

	public List<RegistroValorVO> findByRelacionamento(Long baseInternaId, RegistroValorVO registroPai, String colunaChave){
		List<RegistroValorVO> registrosPai = new ArrayList<>();
		registrosPai.add(registroPai);
		return findByRelacionamento(baseInternaId, registrosPai, colunaChave);
	}

	public List<RegistroValorVO> findByRelacionamento(Long baseInternaId, List<RegistroValorVO> registrosPai, String colunaChave) {

		BaseRegistroFiltro filtro = new BaseRegistroFiltro();
		filtro.setBaseInterna(new BaseInterna(baseInternaId));

		String[] chavesUnicidade = new String[registrosPai.size()];
		for (int i = 0; i < registrosPai.size(); i++) {

			RegistroValorVO registroPai = registrosPai.get(i);

			BaseRegistro baseRegistro = registroPai.getBaseRegistro();
			String chaveUnicidade = baseRegistro.getChaveUnicidade();
			chavesUnicidade[i] = chaveUnicidade.replaceAll("(\\]|\\[|\")", "");
		}

		filtro.addCampoFiltro(colunaChave, chavesUnicidade);

		return findByFiltro(filtro, null, null);
	}

	public List<RegistroValorVO> findByRelacionamentoChaveUnicidade(Long baseInternaId, List<String> chavesUnicidade, String colunaChave) {

		BaseRegistroFiltro filtro = new BaseRegistroFiltro();
		filtro.setBaseInterna(new BaseInterna(baseInternaId));

		String[] chaves = chavesUnicidade.stream().map(cu -> cu.replaceAll("(\\]|\\[|\")", "")).toArray(String[]::new);

		filtro.addCampoFiltro(colunaChave, chaves);

		return findByFiltro(filtro, null, null);
	}

	public List<RegistroValorVO> findByRelacionados(Long baseInternaId, List<String> selecionados, String tipoCampo){

		List<String> cpSelect = new ArrayList<>();
		for (String str : selecionados) {
			str = str.replaceAll("\\[\"(.*)\"\\]", "$1");
			cpSelect.add(str);
		}
		String[] array = cpSelect.toArray(new String[0]);
		BaseRegistroFiltro filtro = new BaseRegistroFiltro();
		filtro.setBaseInterna(new BaseInterna(baseInternaId));
		filtro.addCampoFiltro(tipoCampo, array);

		return findByFiltro(filtro, null, null);
	}

	public File render(BaseRegistroFiltro filtro) throws Exception {
		try {

			String fileOrigemNome = "base-interna.xlsx";

			String extensao = DummyUtils.getExtensao(fileOrigemNome);

			File fileOrigem = DummyUtils.getFileFromResource("/net/wasys/getdoc/excel/" + fileOrigemNome);

			File file = File.createTempFile("base-interna-", "." + extensao);
			DummyUtils.deleteOnExitFile(file);
			FileUtils.copyFile(fileOrigem, file);
			ExcelWriter ew = new ExcelWriter();
			ew.abrirArquivo(file);
			Workbook workbook = ew.getWorkbook();
			ExcelFormat ef = new ExcelFormat(workbook);
			ew.setExcelFormat(ef);
			boolean result = false;
			final List<RegistroValorVO> registros = findByFiltro(filtro, null, null);
			if(registros.size() > 0) {
				String abaTitulo = "Base Interna";
				Sheet sheet = workbook.createSheet(abaTitulo);
				renderRows(sheet, ew, registros);
				result = true;
				if(result) {
					workbook.removeSheetAt(0);
				}
			}

			file.delete();
			File fileDestino = File.createTempFile("base-interna", ".xlsx");
			DummyUtils.deleteOnExitFile(fileDestino);

			FileOutputStream fos = new FileOutputStream(fileDestino);
			workbook.write(fos);
			workbook.close();

			return fileDestino;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		catch (InvalidFormatException e) {
			throw new RuntimeException(e);
		}
	}

	private void renderRows(Sheet sheet, ExcelWriter ew, List<RegistroValorVO> registros) {

		int rowNum = 0;
		List<String> ordemHeader = new ArrayList<>();
		do {
			for (int i = 0; i < registros.size(); i++) {

				RegistroValorVO rg = registros.get(i);

				if (rowNum == 0) {
					ew.criaLinha(sheet, rowNum);
					Map<String, BaseRegistroValor> header = rg.getMapColunaRegistroValor();
					for(String cabecalho : header.keySet()) {
						ew.escrever(cabecalho);
						ordemHeader.add(cabecalho);
					}
				}
				rowNum++;

				ew.criaLinha(sheet, rowNum);
				renderBody(ew, rg, ordemHeader);

				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			Session session = sessionFactory.getCurrentSession();
			session.clear();
		}
		while(rowNum > registros.size());
	}

	private void renderBody(ExcelWriter ew, RegistroValorVO rg, List<String> ordemHeader) {
		for(String ordem : ordemHeader) {
			BaseRegistroValor baseRegistroValor = rg.getMapColunaRegistroValor().get(ordem);
			ew.escrever(baseRegistroValor.getValor());
		}
	}
}