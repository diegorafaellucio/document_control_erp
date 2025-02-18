package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.Parametro;
import net.wasys.getdoc.domain.enumeration.LogAcessoJob;
import net.wasys.getdoc.domain.repository.ParametroRepository;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.other.Bolso;
import net.wasys.util.other.HorasUteisCalculator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ParametroService {

	private final static long TIMEOUT_CACHE = (1000 * 60 * 10);//10 minutos
	private final static ConcurrentHashMap<String, Bolso<?>> MAP_CACHE = new ConcurrentHashMap<>();

	public enum P {
        CHEGADA_MANHA,
        SAIDA_MANHA,
        CHEGADA_TARDE,
        SAIDA_TARDE,
        ULTIMA_DATA_RELATORIO_GERAL,
		ULTIMA_DATA_ATUALIZACAO_GERAL,
		ULTIMA_DATA_ATUALIZACAO_TERMO,
		ULTIMA_DATA_DIGITALIZACAO_DOCUMENTO,
        INTERVALO_DISTRIBUICAO,
        CONFIGURACOES_WS_INFOCAR,
        CONFIGURACOES_WS_ARN,
		CONFIGURACOES_CRIVO,
        HORARIO_ABERTURA,
        HORARIO_FECHAMENTO,
        PERMITIR_ACESSO_FINAL_SEMANA_FERIADO,
		EXTENSOES_WHITE_LIST,
		EXTENSOES_BLACK_LIST,
        MOBILE_CACHE_VERSION,
        COR_BARRA,
        COR_MENU,
        COR_FONTE_TITULO_BARRA,
        COR_FONTE_MENU,
        COR_FONTE_MENU_SELECIONADO,
        HASH_LOGO,
        COR_MENU_SELECIONADO,
		CONFIGURACOES_WS_SANTANDER,
		NAVBAR,
		ACCENT,
		CONFIGURACOES_WS_CREDILINK,
		CONFIGURACOES_WS_NFE_INTERESSE,
		CONFIGURACOES_WS_DATAVALID,
		CONFIGURACOES_WS_RENAVAM,
		CONFIGURACOES_WS_BRSCAN,
		CONFIGURACOES_JOBS,
		CONFIGURACOES_WS_AZURE,
		CONFIGURACOES_WS_SIA,
		CONFIGURACOES_WS_DARKNET,
		EMAIL_PADRAO,
		CONFIGURACOES_WS_ALUNO,
		ULTIMA_DATA_RELATORIO_GERAL_ETAPA,
		CONFIGURACOES_WS_OCRSPACE,
		CONFIGURACOES_WS_ATILA,
		AGENDAMENTO_BACALHAU_GERAL,
		COMPRIMIR_IMAGEM,
		HABILITAR_LOCK_PROXIMO_PROCESSO,
		INTERVALO_DISTRIBUICAO_AUTOMATICA,
		INTERVALO_NOTIFICACAO_SEM_DEMANDA,
		ULTIMA_DATA_ATUALIZACAO_DATAS_PROCESSO,
		EMAILS_ENVIO_ANALISE_JOB,
		ULTIMA_DATA_GERACAO_RELATORIOS,
		EMAILS_NOTIFICACAO,
        BACALHAU_REVERSO,
		ULTIMO_PROCESSO_ID_PROCESSADO_RG,
		VERIFICAR_LOCK_SITUACAO_PROXIMO_PROCESSO;
    }

	private static List<P> EDITAVEIS = Arrays.asList(P.CHEGADA_MANHA, P.SAIDA_MANHA, P.CHEGADA_TARDE, P.SAIDA_TARDE, P.INTERVALO_DISTRIBUICAO, P.HORARIO_ABERTURA, P.HORARIO_FECHAMENTO, P.PERMITIR_ACESSO_FINAL_SEMANA_FERIADO, P.EXTENSOES_WHITE_LIST, P.EXTENSOES_BLACK_LIST, P.EMAIL_PADRAO, P.COMPRIMIR_IMAGEM, P.HABILITAR_LOCK_PROXIMO_PROCESSO, P.INTERVALO_DISTRIBUICAO_AUTOMATICA, P.INTERVALO_NOTIFICACAO_SEM_DEMANDA, P.EMAILS_ENVIO_ANALISE_JOB, P.EMAILS_NOTIFICACAO, P.VERIFICAR_LOCK_SITUACAO_PROXIMO_PROCESSO);
	private static List<P> CUSTOMIZACAO_TELA = Arrays.asList(P.COR_BARRA, P.COR_MENU, P.COR_FONTE_TITULO_BARRA, P.COR_MENU_SELECIONADO, P.COR_FONTE_MENU, P.COR_FONTE_MENU_SELECIONADO);
	private static List<P> CONFIGURACOES_CONSULTA_EXTERNA = Arrays.asList(P.CONFIGURACOES_WS_ARN, P.CONFIGURACOES_WS_INFOCAR, P.CONFIGURACOES_CRIVO, P.CONFIGURACOES_WS_CREDILINK, P.CONFIGURACOES_WS_NFE_INTERESSE, P.CONFIGURACOES_WS_DATAVALID, P.CONFIGURACOES_WS_RENAVAM, P.CONFIGURACOES_WS_BRSCAN, P.CONFIGURACOES_WS_AZURE, P.CONFIGURACOES_WS_SIA, P.CONFIGURACOES_WS_DARKNET, P.CONFIGURACOES_WS_ALUNO, P.CONFIGURACOES_WS_OCRSPACE, P.CONFIGURACOES_WS_ATILA);
	private static List<P> CUSTOMIZACAO_TELA_ANGULAR = Arrays.asList(P.NAVBAR, P.ACCENT);

	@Autowired private ParametroRepository parametroRepository;
	@Autowired private ResourceService resourceService;
	@Autowired private FeriadoService feriadoService;

	public Parametro get(Long id) {
		return parametroRepository.get(id);
	}

	public String[] getExpediente() {
		return new String[]{ getValorCache(P.CHEGADA_MANHA), getValorCache(P.SAIDA_MANHA), getValorCache(P.CHEGADA_TARDE), getValorCache(P.SAIDA_TARDE)};
	}

	public String[] getExpedienteAcesso() {
		String horarioAbertura = getValorCache(P.HORARIO_ABERTURA, String.class);
		String horarioFechamento = getValorCache(P.HORARIO_FECHAMENTO, String.class);
		return new String[]{ horarioAbertura, horarioFechamento, horarioAbertura, horarioFechamento};
	}

	public Integer getMobileCacheVersion() {
		Integer number = getValor(P.MOBILE_CACHE_VERSION, Integer.class);
		return number;
	}

	public String getValorCache(P p) {
		return getValorCache(p.name(), String.class);
	}

	public String getValorCache(String chave) {
		return getValorCache(chave, String.class);
	}

	public <T> T getValorCache(P p, Class<T> resultType) {
		return getValorCache(p.name(), resultType);
	}

	public <T> T getValorCache(String chave, Class<T> resultType) {

		Bolso<T> cache = (Bolso<T>) MAP_CACHE.get(chave);
		cache = cache != null ? cache : new Bolso<T>();
		MAP_CACHE.put(chave, cache);

		T result = cache.getObjeto();
		long finalTime = cache.getFinalTime();

		long now = System.currentTimeMillis();
		if(result == null || finalTime < now) {

			result = getValor(chave, resultType);
			cache.setObjeto(result);
			cache.setFinalTime(now + TIMEOUT_CACHE);
		}

		return result;
	}

	@Transactional(readOnly=true)
	public String getValor(P p) {
		return getValor(p.name());
	}

	@Transactional(readOnly=true)
	public String getValor(String chave) {

		Parametro param = parametroRepository.getByChave(chave);
		String valor = param != null ? param.getValor() : null;
		return valor;
	}

	public <T> T getValor(P p, Class<T> resultType) {
		return getValor(p.name(), resultType);
	}

	public <T> T getValor(String chave, Class<T> resultType) {

		String valor = getValor(chave);
		T result = DummyUtils.convertTypes(valor, resultType);
		return result;
	}

	public Date getValorDate(P p) {

		String valor = getValor(p);
		if(StringUtils.isBlank(valor)) {
			return null;
		}

		Date data = DummyUtils.parseDateTime(valor);
		return data;
	}

	@Transactional(rollbackFor=Exception.class)
	public void setValorDate(P p, Date data) {

		Parametro parametro = parametroRepository.getByChave(p.name());

		if(parametro == null) {
			parametro = new Parametro();
			parametro.setChave(p.name());
		}

		String dataStr = DummyUtils.formatDateTime(data);
		parametro.setValor(dataStr);
		parametroRepository.saveOrUpdate(parametro);
	}

	@Transactional(rollbackFor=Exception.class)
	public void setValor(P p, String valor) {

		Parametro parametro = parametroRepository.getByChave(p.name());

		if(parametro == null) {
			parametro = new Parametro();
			parametro.setChave(p.name());
		}

		parametro.setValor(valor);
		parametroRepository.saveOrUpdate(parametro);
	}

	public Map<String, String> getConfiguracao () {

		Map<String, String> map = new HashMap<>();
		for (P p : EDITAVEIS) {
			String valor = getValor(p);
			map.put(p.name(), valor);
		}

		return map;
	}

	@Transactional(readOnly=true)
	public Map<String, String> getCustomizacao () {

		Map<String, String> map = new HashMap<>();
		for (P p : CUSTOMIZACAO_TELA) {
			String valor = getValorCache(p);
			map.put(p.name(), valor);
		}

		return map;
	}

	@Transactional(readOnly = true)
	public Map<String, String> getCustomizacaoAngular() {

		Map<String, String> map = new HashMap<>();
		for (P p : CUSTOMIZACAO_TELA_ANGULAR) {
			String valor = getValorCache(p);
			map.put(p.name(), valor);
		}

		return map;
	}

	@Transactional(rollbackFor=Exception.class)
	public void salvarConfiguracao(Map<String, String> map) {

		for (P p : EDITAVEIS) {
			String valor = map.get(p.name());
			setValor(p, valor);
		}

		MAP_CACHE.clear();
	}

	@Transactional(rollbackFor=Exception.class)
	public void salvarConfiguracaoConsultaExterna(Map<String, String> map) {

		for (P p : CONFIGURACOES_CONSULTA_EXTERNA) {
			String valor = map.get(p.name());
			setValor(p, valor);
		}

		MAP_CACHE.clear();
	}

	@Transactional(rollbackFor = Exception.class)
    public void restaurarPadrao() throws IOException {

		Map<String, String> map = getCustomizacao();
		map.put(P.COR_MENU.name(), "ffffff");
		map.put(P.COR_BARRA.name(), "034168");
		map.put(P.COR_MENU_SELECIONADO.name(), "013454");
		map.put(P.COR_FONTE_TITULO_BARRA.name(), "ffffff");
		map.put(P.COR_FONTE_MENU.name(), "333333");
		map.put(P.COR_FONTE_MENU_SELECIONADO.name(), "ffffff");

		File file = getLogoDefault();

		salvarCustomizacao(map, file);
	}

	@Transactional(rollbackFor=Exception.class)
	public void salvarCustomizacao(Map<String, String> map, File file) throws IOException {

		for (P p : CUSTOMIZACAO_TELA) {
			String valor = map.get(p.name());
			setValor(p, valor);
		}

		MAP_CACHE.clear();

		if(file != null) {
			salvarLogo(file);
		}
	}

	@Transactional(rollbackFor = Exception.class)
	public void salvarCustomizacaoAngular(Map<P, String> map) throws IOException {

		for (P p : CUSTOMIZACAO_TELA_ANGULAR) {
			String valor = map.get(p);
			setValor(p, valor);
		}

		MAP_CACHE.clear();

	}

	public void salvarLogo(File file) throws IOException {

		String path = resourceService.getValue(ResourceService.IMAGEM_LAYOUT_PATH);
		File fileDest = new File(path, "logo.png");

		FileUtils.copyFile(file, fileDest);

		String hashChecksum = DummyUtils.getHashChecksum(fileDest);
		setValor(P.HASH_LOGO, hashChecksum);
	}

	public File getLogo() {

		String path = resourceService.getValue(ResourceService.IMAGEM_LAYOUT_PATH);
		File file = new File(path, "logo.png");

		if(!file.exists()) {

			file = getLogoDefault();
		}

		return file;
	}

	public File getLogoDefault() {
		File tempFile = DummyUtils.getFileFromResource("/net/wasys/getdoc/layout/logo.png");
		return tempFile;
	}

	@Transactional(rollbackFor=Exception.class)
	public String getHashLogo() {

		String hashLogo = getValor(P.HASH_LOGO);

		if(hashLogo == null) {
			File logo = getLogo();
			hashLogo = DummyUtils.getHashChecksum(logo);
			setValor(P.HASH_LOGO, hashLogo);
		}

		return hashLogo;
	}

	public void salvarCustomizacoes(Map<String, String> map, File file, List<P> lista) throws IOException {

		for (P p : lista) {
			String valor = map.get(p.name());
			setValor(p, valor);
		}

		MAP_CACHE.clear();

		if(file != null) {
			salvarLogo(file);
		}
	}

	public <T> T getConfiguracaoAsObject(P p, Class<T> clazz){
		T object = DummyUtils.jsonToObject(getValor(p), clazz);
		return object;
	}

	public Map<String, String> getConfiguracoesConsultaExterna(){
		Map<String, String> cce = new HashMap<>();
		for (P p : CONFIGURACOES_CONSULTA_EXTERNA) {
			cce.put(p.name(), getValor(p));
		}
		return cce;
	}

	public Map<String, Boolean> getConfiguracoesJobs() {
		String server = DummyUtils.getServer();
		String chave = P.CONFIGURACOES_JOBS + "-" + server;
		String valor = getValorCache(chave);
		if(StringUtils.isBlank(valor)) {
			return new HashMap<>();
		}
		return (Map<String, Boolean>) DummyUtils.jsonStringToMap(valor);
	}

	public void salvarConfiguracoesJobs(Map<String, Boolean> map) {

		String json = DummyUtils.objectToJson(map);

		String server = DummyUtils.getServer();
		String chave = P.CONFIGURACOES_JOBS + "-" + server;
		Parametro parametro = parametroRepository.getByChave(chave);

		MAP_CACHE.remove(chave);

		if(parametro == null) {
			parametro = new Parametro();
			parametro.setChave(chave);
		}

		parametro.setValor(json);
		parametroRepository.saveOrUpdate(parametro);
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public boolean isExecutarJob(LogAcessoJob logAcessoJob) {

		if(logAcessoJob == null) {
			String msg = "logAcessoJob null!!!";
			DummyUtils.systraceThread(msg, LogLevel.ERROR);
			return true;
		}

		Map<String, Boolean> configuracoesJobs = getConfiguracoesJobs();
		String key = logAcessoJob.getKey();
		Object valor = configuracoesJobs.get(key);
		return "true".equals(String.valueOf(valor));
	}

	public void validarArquivoPermitido(String fileName){
		String extensao = DummyUtils.getExtensao(fileName);
		String whiteList = getValor(P.EXTENSOES_WHITE_LIST);

		if(StringUtils.isBlank(extensao) || !whiteList.contains(extensao)) {
			throw new MessageKeyException("arquivoExtensaoNaoPermitida.error", fileName);
		}
	}

	public void validarArquivoBloqueado(String fileName){
		String extensao = DummyUtils.getExtensao(fileName);
		String blackList = getValor(P.EXTENSOES_BLACK_LIST);

		if(blackList.contains(extensao)) {
			throw new MessageKeyException("arquivoExtensaoNaoPermitida.error", fileName);
		}
	}

	public HorasUteisCalculator buildHUC() {

		List<Date> feriados = feriadoService.findAllDatas();
		String[] expedienteArray = getExpediente();
		HorasUteisCalculator.Expediente expediente = new HorasUteisCalculator.Expediente(expedienteArray);
		HorasUteisCalculator huc = new HorasUteisCalculator(expediente, null);
		huc.setFeriados(feriados);

		return huc;
	}

	public List<Parametro> findLikeChave(P p) {
		return parametroRepository.findLikeChave(p);
	}

	public List<LogAcessoJob> getJobsAtivos() {

		Map<String, Boolean> valoresMap = getConfiguracoesJobs();
		List<LogAcessoJob> jobsAtivos = new ArrayList<>();

		for (LogAcessoJob job : LogAcessoJob.values()) {
			String key = job.getKey();
			Object valor = valoresMap.get(key);
			Boolean valorB = valor != null ? Boolean.valueOf(String.valueOf(valor)) : false;
			if(valorB) {
				jobsAtivos.add(job);
			}
		}

		return jobsAtivos;
	}
}
