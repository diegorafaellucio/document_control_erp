package net.wasys.getdoc.bean;

import java.io.Serializable;
import java.util.*;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.vo.*;
import net.wasys.getdoc.domain.vo.filtro.SituacaoFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.other.SuperBeanComparator;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;

import net.wasys.getdoc.domain.enumeration.TipoConsultaExterna;
import net.wasys.getdoc.domain.enumeration.TipoSubRegra;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.faces.AbstractBean;
import net.wasys.util.rest.jackson.ObjectMapper;

@ManagedBean
@ViewScoped
public class SubRegraCrudBean extends AbstractBean {

	@Autowired private RegraService regraService;
	@Autowired private SubRegraService subRegraService;
	@Autowired private BaseInternaService baseInternaService;
	@Autowired private TipoCampoGrupoService tipoCampoGrupoService;
	@Autowired private RegraLinhaService regraLinhaService;
	@Autowired private FuncaoJsService funcaoJsService;
	@Autowired private TipoProcessoService tipoProcessoService;
	@Autowired private FontesExternasService fontesExternasService;
	@Autowired private ParametroService parametroService;
	@Autowired private SituacaoService situacaoService;
	@Autowired private TipoCampoService tipoCampoService;
	@Autowired private TipoDocumentoService tipoDocumentoService;

	private SubRegra subRegra;
	private Regra regra;
	private Long regraId;
	private List<Regra> regras;
	private Set<String> campoGrupos;
	private List<TipoCampo> camposEditaveis;
	private List<TipoCampo> campos;
	private boolean forcado;
	private RegraLinha linhaRaiz;
	private List<RegraLinha> linhas = new ArrayList<>();
	private List<FuncaoJs> funcoes = new ArrayList<>();
	private List<DeparaParamVO> deparaParamVOs = new ArrayList<>();
	private List<DeparaRetornoVO> deparaRetornoVOs = new ArrayList<>();
	private List<FonteVO> fontes = new ArrayList<>(0);
	private List<BaseInterna> basesInternas;
	private Map<String, String> fonteCampos = new HashMap<>(0);
	private List<String> subConsultasExternas;
	private List<Situacao> situacoes;
	private DeparaRetornoVO removerDeparaRetornoVO;
	private Map<String, TipoCampo> destinos = new LinkedHashMap<>();
	private List<TipoDocumento> tipoDocumentos;
	private List<TipoCampoGrupo> tipoCampoGrupoList;

	protected void initBean() {

		regra = regraService.get(regraId);
		Set<RegraTipoProcesso> tiposProcessos1 = regra.getTiposProcessos();
		Hibernate.initialize(tiposProcessos1);
		Situacao situacao = regra.getSituacao();
		Hibernate.initialize(situacao);
		regras = regraService.findAtivosDiferenteDeId(regraId);
		campoGrupos = tipoCampoGrupoService.findNomesByRegraId(regraId);
		basesInternas = baseInternaService.findAtivos();
		funcoes = funcaoJsService.findAll();

		//considera que sempre vai ter apenas um
		List<Long> tiposProcessos = regraService.findTiposProcessoByRegraId(regraId);
		Long tipoProcessoId = tiposProcessos.isEmpty() ? null : tiposProcessos.get(0);
		TipoProcesso tipoProcesso = tipoProcessoService.get(tipoProcessoId);

		tipoCampoGrupoList = tipoCampoGrupoService.findByTipoProcessoAndNaoAdicionado(tipoProcessoId);

		List<TipoCampo> campos = tipoCampoService.findByTipoProcesso(tipoProcessoId);
		for (TipoCampo campo : campos) {
			String chaveFonte = campo.getChaveFonte();
			destinos.put(chaveFonte, campo);
		}

		fonteCampos = subRegraService.carregarFonteCampos(Arrays.asList(tipoProcesso));
		carregar();

		SituacaoFiltro filtro = new SituacaoFiltro();
		filtro.setAtiva(true);
		filtro.setTipoProcessoId(tipoProcessoId);
		situacoes = situacaoService.findByFiltro(filtro, null, null);
		tipoDocumentos = tipoDocumentoService.findByTipoProcesso(tipoProcessoId, null, null);
		this.camposEditaveis = tipoCampoService.findTipoCamposByTipoProcesso(tipoProcesso, true);
		this.campos = tipoCampoService.findTipoCamposByTipoProcesso(tipoProcesso, false);
		for (TipoCampo campo : this.camposEditaveis){
			String nomeGrupo = campo.getGrupo().getNome();
			Hibernate.initialize(nomeGrupo);
		}

		for (TipoCampo campo : this.campos){
			String nomeGrupo = campo.getGrupo().getNome();
			Hibernate.initialize(nomeGrupo);
		}
	}

	private void carregar() {

		regraLinhaService.clearSession();
		linhaRaiz = regraLinhaService.getRaiz(regraId);
		if(linhaRaiz == null) {
			linhaRaiz = new RegraLinha();
			linhaRaiz.setRegra(regra);
			regraLinhaService.saveOrUpdate(linhaRaiz);
		}
		Regra regra = linhaRaiz.getRegra();
		Hibernate.initialize(regra);

		Set<SubRegra> subRegras = linhaRaiz.getSubRegras();
		if(subRegras.isEmpty()) {
			subRegra = new SubRegra();
			subRegra.setLinha(linhaRaiz);
			subRegras.add(subRegra);
		}

		linhas = new ArrayList<>();
		addLinha(linhaRaiz);
	}

	private void addLinha(RegraLinha linha) {

		linhas.add(linha);

		RegraLinha filha = linha.getFilha();
		if(filha != null) {
			addLinha(filha);
		}
	}

	public boolean isSimFim(RegraLinha linha) {

		Set<SubRegra> subRegras = linha.getSubRegras();
		for (SubRegra sr : subRegras) {
			Boolean filhoSim = sr.getFilhoSim();
			if(filhoSim != null && filhoSim) {
				TipoSubRegra tipo = sr.getTipo();
				return TipoSubRegra.FIM.equals(tipo) || TipoSubRegra.CHAMADA_REGRA.equals(tipo);
			}
		}

		return false;
	}

	public boolean isNaoFim(RegraLinha linha) {

		Set<SubRegra> subRegras = linha.getSubRegras();
		for (SubRegra sb : subRegras) {
			Boolean filhoSim = sb.getFilhoSim();
			if(filhoSim != null && !filhoSim) {
				TipoSubRegra tipo = sb.getTipo();
				return TipoSubRegra.FIM.equals(tipo);
			}
		}

		return false;
	}

	public void adicionar(Long subRegraId) {

		deparaParamVOs.clear();
		deparaRetornoVOs.clear();
		forcado = false;
		subRegra = new SubRegra();

		if (subRegraId != null) {
			SubRegra subRegraPai = subRegraService.get(subRegraId);
			RegraLinha linha = subRegraPai.getLinha();
			RegraLinha linhaFilha = linha.getFilha();
			if(linhaFilha == null) {
				linhaFilha = new RegraLinha();
				linhaFilha.setRegra(regra);
				linhaFilha.setLinhaPai(linha);
			}
			else {
				Regra regra = linhaFilha.getRegra();
				Hibernate.initialize(regra);
				Set<SubRegra> subRegras = linhaFilha.getSubRegras();
				Hibernate.initialize(subRegras);
				for (SubRegra sb : subRegras) {
					initSubRegra(sb);
				}
			}
			subRegra.setLinha(linhaFilha);
		}
		else {
			subRegra.setLinha(linhaRaiz);
		}

		carregarFontes();
	}

	public void editar(Long id) {

		subRegraService.clear();
		subRegra = subRegraService.get(id);
		initSubRegra(subRegra);
		SubRegra subRegraPai = subRegraService.get(id);
		TipoSubRegra tipoPai = subRegraPai.getTipo();
		forcado = TipoSubRegra.CONDICAO.equals(tipoPai);

		TipoSubRegra subRegraTipo = subRegra.getTipo();
		if(TipoSubRegra.CONSULTA_EXTERNA.equals(subRegraTipo)) {
			atualizarDeparaParamConsultaExterna();
		}
		else if(TipoSubRegra.BASE_INTERNA.equals(subRegraTipo)) {
			atualizarDeparaParamBaseInterna();
		}

		atualizarDeparaRetornoVOs();
	}

	private void initSubRegra(SubRegra subRegra) {
		RegraLinha linha = subRegra.getLinha();
		BaseInterna baseInterna = subRegra.getBaseInterna();
		Hibernate.initialize(baseInterna);
		initLinha(linha);
	}

	private void initLinha(RegraLinha linha) {
		if(linha == null) return;
		Hibernate.initialize(linha);
		Regra regra = linha.getRegra();
		Hibernate.initialize(regra);
		Set<SubRegra> subRegras = linha.getSubRegras();
		Hibernate.initialize(subRegras);
		for (SubRegra sb : subRegras) {
			List<DeparaParam> deparaParams = sb.getDeparaParams();
			Hibernate.initialize(deparaParams);
			List<DeparaRetorno> deparaRetornos = sb.getDeparaRetornos();
			Hibernate.initialize(deparaRetornos);
		}
		RegraLinha linhaPai = linha.getLinhaPai();
		initLinha(linhaPai);
	}

	public boolean mostrarDeparaParams() {

		if(subRegra != null) {
			TipoSubRegra tipoSubRegra = subRegra.getTipo();

			return (TipoSubRegra.BASE_INTERNA.equals(tipoSubRegra) && subRegra.getBaseInterna() != null)
					|| (TipoSubRegra.CONSULTA_EXTERNA.equals(tipoSubRegra) && subRegra.getConsultaExterna() != null);
		}
		else {
			return false;
		}
	}

	public void salvar() {
		try {
			boolean insert = isInsert(subRegra);
			Usuario usuario = getUsuarioLogado();
			subRegraService.salvar(subRegra, usuario, deparaParamVOs, deparaRetornoVOs);
			addMessage(insert ? "registroCadastrado.sucesso" : "registroAlterado.sucesso");
			carregar();
		}
		catch (MessageKeyException e) {
			addMessageError(e);
		}
	}

	public boolean podeAdicionar(Long subRegraId) {

		SubRegra sr = subRegraService.get(subRegraId);

		RegraLinha linhaFilha = sr.getLinha().getFilha();
		if (linhaFilha == null) {
			return true;
		}
		else {
			return linhaFilha.getSubRegras().size() == 1 && TipoSubRegra.CONDICAO.equals(sr.getTipo());
		}
	}

	public boolean isFilhoVazio(Long subRegraId) {

		SubRegra sr = subRegraService.get(subRegraId);

		return sr.getLinha().getFilha() == null;
	}

	public void excluir() {

		Usuario usuarioLogado = getUsuarioLogado();
		Long subRegraId = subRegra.getId();

		try {
			subRegraService.excluir(subRegraId, usuarioLogado);

			addMessage("registroExcluido.sucesso");
		}
		catch (Exception e) {
			addMessageError(e);
		}

		carregar();
	}

	@SuppressWarnings("unchecked")
	public void atualizarDeparaParamBaseInterna() {

		BaseInterna baseInterna = subRegra.getBaseInterna();

		ObjectMapper om = new ObjectMapper();
		String colunasUnicidade = baseInterna.getColunasUnicidade();
		List<String> colunasList = om.readValue(colunasUnicidade, List.class);

		atualizarDeparaParamVOs(colunasList);
	}

	public void atualizarDeparaParamConsultaExterna() {

		TipoConsultaExterna consultaExterna = subRegra.getConsultaExterna();

		List<String> colunasList = new ArrayList<>();

		if (TipoConsultaExterna.DETRAN_ARN.equals(consultaExterna)) {
			colunasList.add(DetranArnRequestVO.CHASSI);
			colunasList.add(DetranArnRequestVO.PLACA);
			colunasList.add(DetranArnRequestVO.UF);
		}
		else if(TipoConsultaExterna.LEILAO.equals(consultaExterna)) {
			colunasList.add(LeilaoInfoCarRequestVO.PLACA);
		}
		else if(TipoConsultaExterna.DECODE.equals(consultaExterna)) {
			colunasList.add(DecodeInfoCarRequestVO.CHASSI);
		}
		else if(TipoConsultaExterna.CRIVO.equals(consultaExterna)) {

			ConfiguracoesWsCrivoVO confCrivoVO = parametroService.getConfiguracaoAsObject(ParametroService.P.CONFIGURACOES_CRIVO, ConfiguracoesWsCrivoVO.class);
			String politicas = confCrivoVO.getPolitica();
			List<String> subConsultasExternas = DummyUtils.getPoliticasCrivo(politicas);
			this.subConsultasExternas = subConsultasExternas;

			String politicaSelecionada = subRegra.getSubConsultaExterna();
			if(StringUtils.isNotBlank(politicaSelecionada)) {
				List<String> parans = DummyUtils.getParametrosPoliticaCrivo(politicas, politicaSelecionada);
				colunasList.addAll(parans);
			}
		}
		else if(TipoConsultaExterna.SANTANDER_PROPOSTA_DETALHADA.equals(consultaExterna)) {
			colunasList.add(SantanderPropostaDetalhadaRequestVO.NUMERO_INTERMEDIARIO);
			colunasList.add(SantanderPropostaDetalhadaRequestVO.CODIGO_PROPOSTA);
		}
		else if(TipoConsultaExterna.CREDILINK.equals(consultaExterna)) {
			colunasList.add(CredilinkRequestVO.CAMPO_NOME);
			colunasList.add(CredilinkRequestVO.CAMPO_CPF_CNPJ);
			colunasList.add(CredilinkRequestVO.CAMPO_TELEFONE);
		}
		/*else if(TipoConsultaExterna.NFE_INTERESSE.equals(consultaExterna)) {
			colunasList.add(NfeInteresseRequestVO.CHAVE_NFE);
			colunasList.add(NfeInteresseRequestVO.UF_AUTOR);
		}*/
		else if(TipoConsultaExterna.DATA_VALID.equals(consultaExterna)) {
			colunasList.add(DataValidRequestVO.CPF);
			colunasList.add(DataValidRequestVO.DATA_VALIDADE_CNH);
			colunasList.add(DataValidRequestVO.NOME);
			colunasList.add(DataValidRequestVO.DATA_NASCIMENTO);
			colunasList.add(DataValidRequestVO.NOME_MAE);
		}
		else if(TipoConsultaExterna.DATA_VALID_BIOMETRIA.equals(consultaExterna)) {
			colunasList.add(DataValidBiometriaRequestVO.FOTO);
			colunasList.add(DataValidBiometriaRequestVO.CPF);
			colunasList.add(DataValidBiometriaRequestVO.DATA_VALIDADE_CNH);
			colunasList.add(DataValidBiometriaRequestVO.NOME);
			colunasList.add(DataValidBiometriaRequestVO.DATA_NASCIMENTO);
			colunasList.add(DataValidBiometriaRequestVO.NOME_MAE);
		}
		else if(TipoConsultaExterna.RENAVAM_INDICADORES_CHASSI.equals(consultaExterna)) {
			colunasList.add(RenavamIndicadoresChassiRequestVO.CHASSI);
		}
		else if(TipoConsultaExterna.BRSCAN.equals(consultaExterna)) {
			colunasList.add(BrScanRequestVO.DOCUMENTO);
			colunasList.add(BrScanRequestVO.CPF);
			colunasList.add(BrScanRequestVO.SELFIE);
		}

		atualizarDeparaParamVOs(colunasList);
	}

	public void atualizarCampos(DeparaParamVO deParaParamVO) {

		deParaParamVO.getCampos().clear();
		FonteVO fonte = deParaParamVO.getFonte();
		if(fonte != null && !StringUtils.isBlank(fonte.getNome())) {

			Set<String> campos = new LinkedHashSet<>();
			FonteExterna fonteExterna1 = fonte.getFonteExterna();
			TipoProcesso tipoProcesso = fonte.getTipoProcesso();
			BaseInterna baseInterna = fonte.getBaseInterna();

			if(fonteExterna1 != null) {
				FonteExterna fonteExterna = fontesExternasService.findByNome(fonteExterna1.getNome());
				if(fonteExterna != null) {
					String[] split = fonteExterna.getCamposResultado().split("\\r?\\n");
					List<String> asList = Arrays.asList(split);
					Collections.sort(asList);
					campos.addAll(asList);
				}
			} else if(tipoProcesso != null) {
				Long tipoProcessoId = tipoProcesso.getId();
				List<String> camposProcesso = subRegraService.carregaFontesByTipoProcesso(tipoProcessoId);
				campos.addAll(camposProcesso);
			} else if(baseInterna != null) {
				Long baseInternaId = baseInterna.getId();
				Set<String> camposBaseInterna = baseInternaService.findCamposById(baseInternaId);
				campos.addAll(camposBaseInterna);
			}

			String campo = deParaParamVO.getCampo();
			if(campo != null && !campos.contains(campo)) {
				deParaParamVO.setCampo("");
			}

			deParaParamVO.setCampos(campos);
		}
	}

	public void atualizarDeparaParamVOs(List<String> colunasList) {

		deparaParamVOs.clear();
		carregarFontes();

		Long subRegraId = this.subRegra.getId();
		SubRegra subRegra = subRegraId == null ? this.subRegra : subRegraService.get(subRegraId);
		List<DeparaParam> deparaParams = subRegra.getDeparaParams();
		Map<String, DeparaParam> map = new HashMap<>();
		for (DeparaParam dpp : deparaParams) {
			String destino = dpp.getDestino();
			map.put(destino, dpp);
		}

		DeparaParamVO deParaParamVO;
		for (String coluna : colunasList) {

			deParaParamVO = new DeparaParamVO();
			deParaParamVO.setColuna(coluna);

			DeparaParam deparaParam = map.get(coluna);
			if(deparaParam != null) {

				deParaParamVO.setDeparaParamId(deparaParam.getId());
				String origem = deparaParam.getOrigem();
				if(StringUtils.isNotBlank(origem)) {
					FonteVO fonteVO = buscarFonte(deparaParam);
					deParaParamVO.setFonte(fonteVO);
					deParaParamVO.setCampo(origem);
				}
			}

			atualizarCampos(deParaParamVO);
			deparaParamVOs.add(deParaParamVO);
		}
	}

	public void atualizarDeparaRetornoVOs() {

		deparaRetornoVOs.clear();

		Long subRegraId = this.subRegra.getId();
		SubRegra subRegra = subRegraId == null ? this.subRegra : subRegraService.get(subRegraId);
		List<DeparaParam> deparaParams = subRegra.getDeparaParams();
		Map<String, DeparaParam> map = new HashMap<>();
		for (DeparaParam dpp : deparaParams) {
			String destino = dpp.getDestino();
			map.put(destino, dpp);
		}

        DeparaRetornoVO deParaParamVO;
        List<DeparaRetorno> deparaRetornos = this.subRegra.getDeparaRetornos();

		SuperBeanComparator<DeparaRetorno> sbc = new SuperBeanComparator<>("tipoCampo.nome");
		Collections.sort(deparaRetornos, sbc);

        for (DeparaRetorno dr: deparaRetornos) {
            deParaParamVO = new DeparaRetornoVO();

			Long id = dr.getId();
			TipoCampo tipoCampo = dr.getTipoCampo();

			String origem = dr.getOrigem();
			Boolean sobrescreverValor = dr.getSobrescreverValor();

			deParaParamVO.setDeparaParamId(id);
			deParaParamVO.setTipoCampo(tipoCampo);
			deParaParamVO.setOrigem(origem);
			deParaParamVO.setSobrescreveValor(sobrescreverValor);

            deparaRetornoVOs.add(deParaParamVO);
        }
	}

	public List<String> getDestinos(String query) {

		subRegra = subRegraService.get(subRegra.getId());
		initSubRegra(subRegra);
		RegraLinha linha = subRegra.getLinha();
		Regra regra = linha.getRegra();
		Set<RegraTipoProcesso> tiposProcessos = regra.getTiposProcessos();
		if (tiposProcessos.isEmpty()) {
			return new ArrayList<>();
		}
		TipoProcesso tipoProcesso = tiposProcessos.iterator().next().getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();

		List<String> result = new ArrayList<>();
		for (Map.Entry<String, TipoCampo> entry : destinos.entrySet()) {
			String campoNome = entry.getKey();
			if(campoNome.toUpperCase().contains(query.toUpperCase())) {
				result.add(campoNome);
			}
		}

		return result;
	}

	private FonteVO buscarFonte(DeparaParam deparaParam) {

		FonteVO fonteVO = null;

		if(deparaParam.getFonteExterna() != null) {

			FonteExterna fonteExterna = deparaParam.getFonteExterna();
			fonteVO = buscarNaListaFontes(fonteExterna, null, null);
		}
		else if(deparaParam.getBaseInterna() != null) {

			BaseInterna baseInterna = deparaParam.getBaseInterna();
			fonteVO = buscarNaListaFontes(null, baseInterna, null);
		}
		else if(deparaParam.getTipoProcesso() != null) {

			TipoProcesso tipoProcesso = deparaParam.getTipoProcesso();
			fonteVO = buscarNaListaFontes(null, null, tipoProcesso);
		}

		return fonteVO;
	}

	public String getVariaveisDisponiveisJson(SubRegra subRegra) {

		if(subRegra == null) {
			return "{}";
		}

		List<String> list = new ArrayList<>();
		List<String> variaveisDisponiveis = subRegraService.getVariaveisDisponiveis(subRegra, regra);
		list.addAll(variaveisDisponiveis);

		ObjectMapper om = new ObjectMapper();
		return om.writeValueAsString(list);
	}

	private void carregarFontes() {

		fontes.clear();
		List<Long> tiposProcessoRegra = regraService.findTiposProcessoByRegraId(regra.getId());
		for (Long tipoProcessoId : tiposProcessoRegra) {
			adicionarProcessosNasFontes(tipoProcessoId);
		}

		carregarFontesExternas();
		carregarFontesInternas();
	}

	private void carregarFontesExternas() {

		List<FonteVO> fontesExternasDisponiveis = regraService.buscarConsultasExternasDisponiveis(subRegra);
		for (FonteVO vo : fontesExternasDisponiveis) {
			vo.setNome(vo.getNome());
			vo.setFonteExterna(vo.getFonteExterna());
			fontes.add(vo);
		}
	}

	private void carregarFontesInternas() {

		FonteVO fonteVO = null;
		List<FonteVO> basesInternasDisponiveis = regraService.buscarBasesInternasDisponiveis(subRegra);
		for (FonteVO vo : basesInternasDisponiveis) {

			fonteVO = new FonteVO();
			fonteVO.setNome(vo.getNome());
			fonteVO.setBaseInterna(vo.getBaseInterna());
			fontes.add(fonteVO);
		}
	}

	private void adicionarProcessosNasFontes(Long... ids) {

		FonteVO fonteVO;
		for (Long id : ids) {
			TipoProcesso tp = tipoProcessoService.get(id);
			fonteVO = new FonteVO();
			fonteVO.setNome(tp.getNome());
			fonteVO.setTipoProcesso(tp);
			fontes.add(fonteVO);
		}
	}

	private FonteVO buscarNaListaFontes(FonteExterna fonteExterna, BaseInterna baseInterna, TipoProcesso tipoProcesso) {

		for (FonteVO fonte : fontes) {
			if(fonteExterna != null && fonteExterna.equals(fonte.getFonteExterna())) {
				return fonte;
			}
			else if(baseInterna != null && baseInterna.equals(fonte.getBaseInterna())) {
				return fonte;
			}
			else if(tipoProcesso != null && tipoProcesso.equals(fonte.getTipoProcesso())) {
				return fonte;
			}
		}

		return null;
	}

	public SubRegra getSubRegra() {
		return subRegra;
	}

	public void setSubRegra(SubRegra subRegra) {
		if(subRegra == null) {
			subRegra = new SubRegra();
		}
		this.subRegra = subRegra;
	}

	public void adicionarDeparaRetorno() {
		DeparaRetornoVO deparaRetornoVO = new DeparaRetornoVO();
		deparaRetornoVOs.add(deparaRetornoVO);
	}

	public void adicionarAcao(){
		SubRegraAcao subRegraAcao = new SubRegraAcao();
		List<SubRegraAcao> subRegraAcoes = this.subRegra.getSubRegraAcoes();
		subRegraAcoes.add(subRegraAcao);
	}

	public void removerDeparaRetorno(DeparaRetornoVO vo) {
		deparaRetornoVOs.remove(vo);
	}

	public void removerSubRegraAcao(SubRegraAcao subRegraAcao) {
		List<SubRegraAcao> subRegraAcoes = this.subRegra.getSubRegraAcoes();
		subRegraAcoes.remove(subRegraAcao);
	}

	public boolean isForcado() {
		return forcado;
	}

	public Regra getRegra() {
		return regra;
	}

	public Long getRegraId() {
		return regraId;
	}

	public void setRegraId(Long regraId) {
		this.regraId = regraId;
	}

	public List<BaseInterna> getBasesInternas() {
		return basesInternas;
	}

	public List<Regra> getRegras() {
		return regras;
	}

	public List<DeparaParamVO> getDeParaParamVOs() {
		return deparaParamVOs;
	}

	public Set<String> getCampoGrupos() {
		return campoGrupos;
	}

	public List<TipoCampo> getCamposEditaveis() {
		return camposEditaveis;
	}

	public List<TipoCampo> getCampos() {
		return campos;
	}

	public List<RegraLinha> getLinhas() {
		return linhas;
	}

	public List<FonteVO> getFontes() {
		return fontes;
	}

	public List<FuncaoJs> getFuncoes() {
		return funcoes;
	}

	public Map<String, String> getFonteCampos() {
		return fonteCampos;
	}

	public List<Situacao> getSituacoes() {
		return situacoes;
	}

	public List<String> getSubConsultasExternas() {
		return subConsultasExternas;
	}

	public List<DeparaRetornoVO> getDeparaRetornoVOs() {
		return deparaRetornoVOs;
	}

	public DeparaRetornoVO getRemoverDeparaRetornoVO() {
		return removerDeparaRetornoVO;
	}

	public void setRemoverDeparaRetornoVO(DeparaRetornoVO removerDeparaRetornoVO) {
		this.removerDeparaRetornoVO = removerDeparaRetornoVO;
	}

	public class DeparaParamVO implements Serializable {

		private Long deparaParamId;
		private String coluna;
		private String campo;
		private FonteVO fonte;

		private Set<String> campos = new HashSet<>(0);

		public Long getDeparaParamId() {
			return deparaParamId;
		}

		public void setDeparaParamId(Long deparaParamId) {
			this.deparaParamId = deparaParamId;
		}

		public String getColuna() {
			return coluna;
		}

		public void setColuna(String coluna) {
			this.coluna = coluna;
		}

		public String getCampo() {
			return campo;
		}

		public void setCampo(String campo) {
			this.campo = campo;
		}

		public Set<String> getCampos() {
			return campos;
		}

		public List<String> getCampos(String query) {

			List<String> result = new ArrayList<>();
			for (String c : campos) {
				if(c.toUpperCase().contains(query.toUpperCase())) {
					result.add(c);
				}
			}

			return result;
		}

		public void setCampos(Set<String> campos) {
			this.campos = campos;
		}

		public FonteVO getFonte() {
			return fonte;
		}

		public void setFonte(FonteVO fonte) {
			this.fonte = fonte;
		}
	}

	public class DeparaRetornoVO implements Serializable {

		private Long deparaParamId;
		private TipoCampo tipoCampo;
		private String origem;
		private Boolean sobrescreveValor;

		public Long getDeparaParamId() {
			return deparaParamId;
		}

		public void setDeparaParamId(Long deparaParamId) {
			this.deparaParamId = deparaParamId;
		}

		public TipoCampo getTipoCampo() {
			return tipoCampo;
		}

		public void setTipoCampo(TipoCampo tipoCampo) {
			this.tipoCampo = tipoCampo;
		}

		public String getOrigem() {
			return origem;
		}

		public void setOrigem(String origem) {
			this.origem = origem;
		}

		public Boolean getSobrescreveValor() { return sobrescreveValor; }

		public void setSobrescreveValor(Boolean sobrescreveValor) { this.sobrescreveValor = sobrescreveValor; }

		public String getTipoCampoStr() {
			return tipoCampo != null ? tipoCampo.getChaveFonte() : null;
		}

		public void setTipoCampoStr(String chaveFonte) {
			tipoCampo = destinos.get(chaveFonte);
		}
	}

	public List<TipoDocumento> getTipoDocumentos() {
		return tipoDocumentos;
	}

	public void setTipoDocumentos(List<TipoDocumento> tipoDocumentos) {
		this.tipoDocumentos = tipoDocumentos;
	}

	public List<TipoCampoGrupo> getTipoCampoGrupoList() {
		return tipoCampoGrupoList;
	}
}
