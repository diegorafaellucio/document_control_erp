package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.getdoc.domain.enumeration.CampoMap;
import net.wasys.getdoc.domain.enumeration.TipoEntradaCampo;
import net.wasys.getdoc.domain.repository.CampoRepository;
import net.wasys.getdoc.domain.vo.CampoDinamicoFiltroVO;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.getdoc.domain.vo.filtro.BaseRegistroFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.ddd.TransactionWrapper;
import net.wasys.util.other.Bolso;
import net.wasys.util.rest.jackson.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static net.wasys.util.DummyUtils.systraceThread;

@Service
public class CampoService {

	public final static List<String> CAMPOS_SITUACAO_ALUNO_ELIMINADO_CANCELADO = Arrays.asList("ELIMINADO", "INSCRIÇÃO CANCELADA");
	private final static ConcurrentHashMap<CampoMap.CampoEnum, Bolso<?>> MAP_CAMPO_CACHE = new ConcurrentHashMap<>();
	private static final long TIMEOUT_CAMPO_CACHE = (1000 * 60 * 10);//10 minutos;

	@Autowired private CampoRepository campoRepository;
	@Autowired private ProcessoService processoService;
	@Autowired private TipoCampoService tipoCampoService;
	@Autowired private CampoGrupoService campoGrupoService;
	@Autowired private TipoCampoGrupoService tipoCampoGrupoService;
	@Autowired private BaseRegistroService baseRegistroService;
	@Autowired public MessageService messageService;
	@Autowired private ApplicationContext applicationContext;
	@Autowired private ProcessoLogService processoLogService;

	public Campo get(Long id) {
		return campoRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(Campo campo) {

		String valor = campo.getValor();
		valor = valor == null ? "" : valor;
		campo.setValor(valor);

		campoRepository.saveOrUpdate(campo);
	}

	public List<Campo> findByProcesso(Long processoId) {
		return campoRepository.findByProcessoSituacao(processoId, null);
	}

	public List<Campo> findByGrupoId(Long grupoId) {
		return campoRepository.findByGrupoId(grupoId);
	}

	public List<Campo> findByGrupoIdAndNome(Long grupoId, String nome) {
		return campoRepository.findByGrupoIdAndNome(grupoId, nome);
	}

	public List<Campo> findByProcessoSituacaoOrNomeGrupo(Usuario usuario, Long processoId, Long situacaoId, String nomeGrupo, List<Campo> valores) {
		List<Campo> campos;
		if(situacaoId != null) {
			campos = campoRepository.findByProcessoSituacao(processoId, situacaoId);
		}else{
			campos = campoRepository.findByProcessoNomeGrupo(processoId, nomeGrupo);
		}
		Set<Long> tiposCamposIds = new LinkedHashSet<>();
		Map<String, CampoGrupo> gruposMap = new HashMap<>();
		campos.forEach((campo) -> {
			tiposCamposIds.add(campo.getTipoCampoId());
			CampoGrupo grupo = campo.getGrupo();
			String grupoNome = grupo.getNome();
			gruposMap.put(grupoNome, grupo);
		});

		Processo processo = processoService.get(processoId);
		TipoProcesso tipoProcesso = processo.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();
		List<TipoCampo> tiposCampos;
		if(situacaoId != null){
			tiposCampos = tipoCampoService.findByTipoProcessoSituacao(tipoProcessoId, situacaoId);
		}else{
			TipoCampoGrupo tipoCampoGrupo = tipoCampoGrupoService.getByTipoProcessoAndGrupoNome(tipoProcessoId, nomeGrupo);
			tiposCampos = tipoCampoService.findByTipoCampoGrupo(tipoCampoGrupo, false);
		}
		for (TipoCampo tipoCampo : tiposCampos) {
			Long tipoCampoId = tipoCampo.getTipoCampoId();
			if(!tiposCamposIds.contains(tipoCampoId)) {
				TipoCampoGrupo tipoGrupo = tipoCampo.getGrupo();
				String grupoNome = tipoGrupo.getNome();
				CampoGrupo grupo = gruposMap.get(grupoNome);
				if(grupo == null) {
					grupo = campoGrupoService.criaGrupo(processo, tipoGrupo);
					gruposMap.put(grupoNome, grupo);
				}
				Campo campo = criaCampo(grupo, tipoCampo);
				if(valores != null) {
					for (Campo campoTmp : valores) {
						String nome = campo.getNome();
						String nomeTmp = campoTmp.getNome();
						if(nomeTmp.equals(nome)){
							String valor1 = campoTmp.getValor();
							campo.setValor(valor1);
						}
					}
				}
				campos.add(campo);
			}
		}

		for (Campo campo : campos) {
			TipoEntradaCampo tipo = campo.getTipo();
			BaseInterna baseInterna = campo.getBaseInterna();
			if(TipoEntradaCampo.COMBO_BOX_ID.equals(tipo) && baseInterna != null) {
				carregarOpcoesDinamicas(usuario, campo, true, campos);
			}
		}

		Collections.sort(campos, (c1, c2) -> {
			CampoGrupo g1 = c1.getGrupo();
			CampoGrupo g2 = c2.getGrupo();
			int compareTo = g1.getOrdem().compareTo(g2.getOrdem());
			if(compareTo != 0) {
				return compareTo;
			}
			return c1.getOrdem().compareTo(c2.getOrdem());
		});

		return campos;
	}

	private void carregarOpcoesDinamicas(Usuario usuario, Campo campo, boolean carregarTodasOpcoes, List<Campo> campos) {

		BaseRegistroFiltro filtro = new BaseRegistroFiltro();
		filtro.setAtivo(true);
		BaseInterna baseInterna = campo.getBaseInterna();
		filtro.setBaseInterna(baseInterna);

		if(!carregarTodasOpcoes) {//carrega apenas a opção correspondente ao valor atual do campo
			filtro.setChaveUnicidade(null);
			String valor = campo.getValor();
			if(StringUtils.isBlank(valor)) {
				return;//se não tem valor não preenche
			}
			filtro.setChaveUnicidade(valor);
			List<RegistroValorVO> opcoes = baseRegistroService.findByFiltro(filtro, null, null);
			campo.setOpcoesBaseInterna(opcoes);
		}
		else {//carrega todas as opções, levando em consideração o filtro
			String criterioFiltroStr = campo.getCriterioFiltro();
			String paisStr = campo.getPais();

			if(StringUtils.isNotBlank(criterioFiltroStr)) {
				String[] filtros = criterioFiltroStr.split(",");
				for(String f: filtros) {
					String array[] = f.split(":");
					String nomePai = array[0].trim();
					String campoNome = array[1].trim();
					paisStr = paisStr.replaceAll("\\[", "").replace("]", "");
					JSONArray pais = new JSONArray("[" + paisStr + "]");

					Map<String, String> valoresFiltro = new LinkedHashMap<>();
					for (int i=0; i < pais.length(); i++) {
						JSONObject jsonObject = pais.getJSONObject(i);
						String nome = jsonObject.get("nome").toString();
						if(nome.equals(nomePai)) {
							valoresFiltro.put(nome, jsonObject.get("paiId").toString());
						}
					}

					for (Campo campo2 : campos) {
						Long tipoCampoId = campo2.getTipoCampoId();
						for(String pai: valoresFiltro.keySet()) {
							String valorPai = valoresFiltro.get(pai);
							Long paiId = Long.parseLong(valorPai);
							if (tipoCampoId != null && tipoCampoId.equals(paiId)) {
								String valor = campo2.getValor();
								String paiValor = valor != null ? valor.replaceAll("\\[", "").replace("]", "").replace("\"", "") : "";
								filtro.addCampoFiltro(campoNome, paiValor);
							}
						}
					}
				}
			}

			List<RegistroValorVO> opcoes = baseRegistroService.findByFiltro(filtro, null, null);
			campo.setOpcoesBaseInterna(opcoes);
		}
	}

	public Campo criaCampo(CampoGrupo grupo, TipoCampo tipoCampo) {

		String nome = tipoCampo.getNome();
		boolean obrigatorio = tipoCampo.getObrigatorio();
		Integer ordem = tipoCampo.getOrdem();
		Integer tamanhoMaximo = tipoCampo.getTamanhoMaximo();
		Integer tamanhoMinimo = tipoCampo.getTamanhoMinimo();
		TipoEntradaCampo tipo = tipoCampo.getTipo();
		String opcoes = tipoCampo.getOpcoes();
		Boolean editavel = tipoCampo.getEditavel();
		Long tipoCampoId = tipoCampo.getId();
		BaseInterna baseInterna = tipoCampo.getBaseInterna();
		String opcaoId = tipoCampo.getOpcaoId();
		String dica = tipoCampo.getDica();
		String pais = tipoCampo.getPais();
		String criterioExibicao = tipoCampo.getCriterioExibicao();
		String criterioFiltro = tipoCampo.getCriterioFiltro();
		List<RegistroValorVO> opcoesBaseInterna = tipoCampo.getOpcoesBaseInterna();

		Campo campo = new Campo();
		campo.setNome(nome);
		campo.setObrigatorio(obrigatorio);
		campo.setOrdem(ordem);
		campo.setTamanhoMaximo(tamanhoMaximo);
		campo.setTamanhoMinimo(tamanhoMinimo);
		campo.setTipo(tipo);
		campo.setOpcoes(opcoes);
		campo.setEditavel(editavel);
		campo.setGrupo(grupo);
		campo.setTipoCampoId(tipoCampoId);
		campo.setBaseInterna(baseInterna);
		campo.setOpcaoId(opcaoId);
		campo.setDica(dica	);
		campo.setPais(pais);
		campo.setCriterioExibicao(criterioExibicao);
		campo.setCriterioFiltro(criterioFiltro);
		campo.setOpcoesBaseInterna(opcoesBaseInterna);

		return campo;
	}

	public Map<String, Map<String, String>> findValoresMapByProcesso(Long processoId) {
		return campoRepository.findValoresMapByProcesso(processoId);
	}

	public Map<CampoGrupo, List<Campo>> findMapByProcesso(Usuario usuario, Long processoId, boolean carregarOpcoesDinamicas) {

		List<Campo> list = campoRepository.findByProcessoSituacao(processoId, null);
		Map<CampoGrupo, List<Campo>> map = toMap(list, usuario, carregarOpcoesDinamicas);

		List<CampoGrupo> gruposSemCampos = campoGrupoService.findGruposSemCamposByProcessoId(processoId);
		for (CampoGrupo grupoSemCampo : gruposSemCampos) {
			map.put(grupoSemCampo, new ArrayList<>());
		}

		return map;
	}

	private Map<CampoGrupo, List<Campo>> toMap(List<Campo> list, Usuario usuario, boolean carregarOpcoesDinamicas) {

		Map<CampoGrupo, List<Campo>> map = new LinkedHashMap<>();
		for (Campo campo : list) {
			CampoGrupo grupo = campo.getGrupo();
			List<Campo> list2 = map.get(grupo);
			list2 = list2 != null ? list2 : new ArrayList<>();
			list2.add(campo);
			map.put(grupo, list2);

			TipoEntradaCampo tipo = campo.getTipo();
			if(TipoEntradaCampo.COMBO_BOX_ID.equals(tipo)) {
				carregarOpcoesDinamicas(usuario, campo, carregarOpcoesDinamicas, list);
			}
		}
		return map;
	}

	public Map<String, String> findOpcoesCampoDinamico(Usuario usuario, CampoDinamicoFiltroVO campoDinamicoFiltroVO) {
		List<String> criterios = campoDinamicoFiltroVO.getCriterios();
        List<String> valoresPais = campoDinamicoFiltroVO.getValoresPais();
        if(criterios.size() != valoresPais.size() || criterios == null) {
            return new HashMap<>();
        }

        BaseRegistroFiltro filtro = new BaseRegistroFiltro();

		List<String> camposFiltro = new ArrayList<>();

		for(String criterio: criterios){
			ObjectMapper om = new ObjectMapper();
			Map criterioMap = om.readValue(criterio, Map.class);
            String filtrar = (String) criterioMap.get("filtrar");
            camposFiltro.add(filtrar);
		}

		for(int i = 0; i < camposFiltro.size(); i++){
			String valorPai = valoresPais.get(i).replaceAll("^\\[\"", "");
			valorPai = valorPai.replaceAll("\"\\]$", "");
			filtro.addCampoFiltro(camposFiltro.get(i), valorPai);
		}

        Long tipoCampoId = campoDinamicoFiltroVO.getTipoCampoId();
        TipoCampo campo = tipoCampoService.get(new Long(tipoCampoId));
		BaseInterna bi = campo.getBaseInterna();
		if(bi == null) {
			return new HashMap<>();
		}

		filtro.setAtivo(true);
		filtro.setBaseInterna(bi);

		List<RegistroValorVO> opcoes = baseRegistroService.findByFiltro(filtro, null, null);
		return baseRegistroService.toChaveLabelMap(bi, opcoes);
	}

	public Map<String, String> findOpcoesCampoDinamico(String criterio, String chaveFiltro, Long paiId, Long tipoCampoId) {

		if(StringUtils.isBlank(chaveFiltro)) {
			return new HashMap<>();
		}

		ObjectMapper om = new ObjectMapper();
		Map criterioMap = om.readValue(criterio, Map.class);
		String campoFiltro = (String) criterioMap.get("filtrar");

		TipoCampo campoPai = tipoCampoService.get(new Long(paiId));
		BaseInterna biPai = campoPai.getBaseInterna();

		BaseRegistroFiltro filtro = new BaseRegistroFiltro();
		String valorPai;

		if(biPai == null) {
			filtro.addCampoFiltro(campoFiltro, chaveFiltro);
		}
		else {
			valorPai = chaveFiltro;
			valorPai = valorPai.replaceAll("^\\[\"", "");
			valorPai = valorPai.replaceAll("\"\\]$", "");
			filtro = new BaseRegistroFiltro();
			filtro.addCampoFiltro(campoFiltro, valorPai);
		}

		TipoCampo campo = tipoCampoService.get(new Long(tipoCampoId));
		BaseInterna bi = campo.getBaseInterna();
		if(bi == null) {
			return new HashMap<>();
		}
		filtro.setAtivo(true);
		filtro.setBaseInterna(bi);
		List<RegistroValorVO> opcoes2 = baseRegistroService.findByFiltro(filtro, null, null);
		return baseRegistroService.toChaveLabelMap(bi, opcoes2);
	}

	public List<Campo> findByProcessoTipoCampos(Long processoId, List<Long> tipoCampoIds) {
		return campoRepository.findByProcessoTipoCampos(processoId, tipoCampoIds);
	}

	public Campo findByProcessoTipoCampo(Long processoId, Long tipoCampoId) {
		List<Campo> campoList = findByProcessoTipoCampos(processoId, Arrays.asList(tipoCampoId));
		return CollectionUtils.isNotEmpty(campoList) ? campoList.get(0) : null;
	}

	public List<Campo> findByProcessoTipo(Long processoId, String tipo) {
		return campoRepository.findByProcessoTipo(processoId, tipo);
	}

	public List<String> findValoresByCampo(CampoMap.CampoEnum campo){

		Bolso<List<String>> cache = (Bolso<List<String>>) MAP_CAMPO_CACHE.get(campo);
		cache = cache != null ? cache : new Bolso<>();
		MAP_CAMPO_CACHE.put(campo, cache);

		List<String> result = cache.getObjeto();
		long finalTime = cache.getFinalTime();

		long now = System.currentTimeMillis();
		if(result == null || finalTime < now) {
			result = campoRepository.findValoresByCampo(campo);
			cache.setObjeto(result);
			cache.setFinalTime(now + TIMEOUT_CAMPO_CACHE);
		}

		return result;
	}

	public Date getDataNascimentoGrupoMembroFamiliar(Long grupoMembroFamiliarId) {

		Date dataNascimento = null;

		List<Campo> campos = findByGrupoIdAndNome(grupoMembroFamiliarId, CampoMap.CampoEnum.DATA_NASCIMENTO.getNome());

		if (!campos.isEmpty()) {

			Campo campoDataNascimento = campos.get(0);
			String dataNascimentoValor = campoDataNascimento.getValor();

			try {
				dataNascimento = DummyUtils.parseDate(dataNascimentoValor);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		return dataNascimento;
	}

	public BigDecimal getRendaGrupoMembroFamiliar(Long grupoMembroFamiliarId) {

		BigDecimal rendaValor = null;

		List<Campo> campos = findByGrupoIdAndNome(grupoMembroFamiliarId, CampoMap.CampoEnum.RENDA.getNome());

		if (!campos.isEmpty()) {

			Campo campoRenda = campos.get(0);
			String valor = campoRenda.getValor();
			rendaValor = DummyUtils.stringToCurrency(valor);
		}

		return rendaValor;
	}

	public List<String> getLocalDeOferta() {
		return campoRepository.getLocalDeOferta();
	}

	public String findValorByProcessoId(Long processoId, CampoMap.CampoEnum campoEnum) {
		return campoRepository.findValorByProcessoId(processoId, campoEnum);
	}

	public void criarCamposEmMassaAndReordenado(List<Processo> processos, Usuario usuarioLogado, TipoCampoGrupo tipoGrupo, TipoCampo novoTipoCampo) {

		Long novoTipoCampoId = novoTipoCampo.getId();
		int count = 1;
		ExecutorService executor = Executors.newFixedThreadPool(10);
		try {
			for (Processo processo : processos) {
				Long processoId = processo.getId();
				AtomicReference<Campo> campoNovo = new AtomicReference<>(findByProcessoTipoCampo(processoId, novoTipoCampoId));
				systraceThread("Processo: " + processoId + " Quantidade: " + count + " de " + processos.size());
				count ++;
				if (campoNovo.get() != null) continue;

				TransactionWrapper tw = new TransactionWrapper(applicationContext);
				tw.setRunnable(() -> {
					systraceThread("Reordenando todos os campos do processo: " + processoId, LogLevel.INFO);
					String tipoGrupoNome = tipoGrupo.getNome();
					List<CampoGrupo> campoGrupos = campoGrupoService.findByProcessoIdAndNome(processoId, tipoGrupoNome);
					CampoGrupo campoGrupo = campoGrupos.get(0);

					Set<Campo> campos = campoGrupo.getCampos();
					Long tipoCampoGrupoId = campoGrupo.getTipoCampoGrupoId();
					List<TipoCampo> tipoCampoGrupoList = tipoCampoService.findByTipoCampoGrupo(new TipoCampoGrupo(tipoCampoGrupoId), false);
					for (TipoCampo tipoCampo1 : tipoCampoGrupoList) {
						Long tipoCampoId = tipoCampo1.getId();
						for (Campo campo : campos) {
							Long atualTipoCampoId = campo.getTipoCampoId();
							if(atualTipoCampoId.equals(tipoCampoId)) {
								Integer ordem = tipoCampo1.getOrdem();
								campo.setOrdem(ordem);
								saveOrUpdate(campo);
								break;
							}
						}
					}

					systraceThread("Criando campo no processo: " + processoId);
					campoNovo.set(criaCampo(campoGrupo, novoTipoCampo));
					campoGrupo.getCampos().add(campoNovo.get());

					String observacao = ("Criou campo em massa: " + novoTipoCampo.getNome());
					processoLogService.criaLog(processo, usuarioLogado, AcaoProcesso.CRIOU_CAMPO, observacao);
					this.saveOrUpdate(campoNovo.get());
				});
				executor.submit(tw);
			}
		}
		finally {
			executor.shutdown();
		}

	}
}
