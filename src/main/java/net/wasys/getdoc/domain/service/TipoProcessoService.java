package net.wasys.getdoc.domain.service;

import java.math.BigDecimal;
import java.util.*;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.*;
import net.wasys.getdoc.domain.vo.filtro.RegraFiltro;
import net.wasys.getdoc.domain.vo.filtro.SituacaoFiltro;
import org.hibernate.Hibernate;
import org.primefaces.model.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.wasys.getdoc.domain.repository.TipoProcessoRepository;
import net.wasys.getdoc.domain.vo.TipoProcessoVO;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;

@Service
public class TipoProcessoService {

	@Autowired private LogAlteracaoService logAlteracaoService;
	@Autowired private TipoProcessoRepository tipoProcessoRepository;
	@Autowired private TipoCampoGrupoService tipoCampoGrupoService;
	@Autowired private TipoCampoService tipoCampoService;
	@Autowired private TipoProcessoPermissaoService tipoProcessoPermissaoService;
	@Autowired private IrregularidadeTipoDocumentoService irregularidadeTipoDocumentoService;
	@Autowired private TipoDocumentoService tipoDocumentoService;
	@Autowired private SituacaoService situacaoService;
	@Autowired private RegraService regraService;
	@Autowired private CampanhaService campanhaService;

	public TipoProcesso get(Long id) {
		return tipoProcessoRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluir(Long tipoProcessoId, Usuario usuario) throws MessageKeyException {

		TipoProcesso tipoProcesso = get(tipoProcessoId);
		logAlteracaoService.registrarAlteracao(tipoProcesso, usuario, TipoAlteracao.EXCLUSAO);

		try {
			tipoProcessoRepository.deleteById(tipoProcessoId);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}
	}

	@Transactional(rollbackFor=Exception.class)
	public void desabilitar(TipoProcesso tipoProcesso) {

		tipoProcesso.setAtivo(false);

		tipoProcessoRepository.saveOrUpdate(tipoProcesso);
	}

	@Transactional(rollbackFor=Exception.class)
	public void habilitar(TipoProcesso tipoProcesso) {

		tipoProcesso.setAtivo(true);

		tipoProcessoRepository.saveOrUpdate(tipoProcesso);
	}

	@Transactional(rollbackFor=Exception.class)
	public void duplicar(TipoProcesso tipoProcesso, Usuario usuario) throws MessageKeyException {

		boolean ativo = tipoProcesso.getAtivo();
		BigDecimal horasPrazo = tipoProcesso.getHorasPrazo();
		String nome = tipoProcesso.getNome();
		TipoPrazo tipoPrazo = tipoProcesso.getTipoPrazo();
		BigDecimal horasPrazoAdvertir1 = tipoProcesso.getHorasPrazoAdvertir();
		TipoPrazo tipoPrazoAdvertir1 = tipoProcesso.getTipoPrazoAdvertir();
		String novoNome = "";

		boolean ok = false;
		int count = 2;
		do {
			novoNome = nome + " (" + count + ")";
			ok = !tipoProcessoRepository.existsByNome(novoNome);
			count++;
		}
		while(!ok);

		TipoProcesso tipoProcesso2 = new TipoProcesso();
		tipoProcesso2.setAtivo(ativo);
		tipoProcesso2.setHorasPrazo(horasPrazo);
		tipoProcesso2.setNome(novoNome);
		tipoProcesso2.setTipoPrazo(tipoPrazo);
		tipoProcesso2.setHorasPrazoAdvertir(horasPrazoAdvertir1);
		tipoProcesso2.setTipoPrazoAdvertir(tipoPrazoAdvertir1);

		tipoProcessoRepository.saveOrUpdate(tipoProcesso2);

		List<TipoProcessoPermissao> permissoes = tipoProcessoPermissaoService.findByTipoProcesso(tipoProcesso.getId());
		for (TipoProcessoPermissao permissao : permissoes) {
			PermissaoTP p = permissao.getPermissao();
			TipoProcessoPermissao permissao2 = new TipoProcessoPermissao();
			permissao2.setTipoProcesso(tipoProcesso2);
			permissao2.setPermissao(p);
			tipoProcessoPermissaoService.saveOrUpdate(permissao2);
		}

		List<TipoCampoGrupo> grupos2 = new ArrayList<>();
		Long tipoProcessoId = tipoProcesso.getId();
		List<TipoCampoGrupo> grupos = tipoCampoGrupoService.findByTipoProcesso(tipoProcessoId);
		Map<TipoCampoGrupo, List<TipoCampo>> mapCampos = tipoCampoService.findMapByTipoProcesso(tipoProcessoId, false);
		for (TipoCampoGrupo grupo : grupos) {
			String nome2 = grupo.getNome();
			Integer ordem = grupo.getOrdem();
			Boolean abertoPadrao = grupo.getAbertoPadrao();
			Set<TipoCampoGrupoSituacao> situacoes = grupo.getSituacoes();
			Boolean criacaoProcesso = grupo.getCriacaoProcesso();
			List<TipoCampo> campos = mapCampos.get(grupo);

			TipoCampoGrupo grupo2 = new TipoCampoGrupo();
			grupo2.setNome(nome2);
			grupo2.setOrdem(ordem);
			grupo2.setTipoProcesso(tipoProcesso2);
			grupo2.setAbertoPadrao(abertoPadrao);
			grupo2.setCriacaoProcesso(criacaoProcesso);
			tipoCampoGrupoService.saveOrUpdate(grupo2, usuario);
			grupos2.add(grupo2);

			if (!situacoes.isEmpty()) {
				for (TipoCampoGrupoSituacao tcgs: situacoes) {
					TipoCampoGrupoSituacao tcgs2 = new TipoCampoGrupoSituacao();
					tcgs2.setSituacao(tcgs.getSituacao());
					tcgs2.setTipoCampoGrupo(grupo2);
					grupo2.getSituacoes().add(tcgs2);
				}
			}

			if (campos != null){
				Map<Long, Long> deParaCampo = new HashMap<>();
				List<TipoCampo> campos2 = new ArrayList<>();
				for (TipoCampo tipoCampo : campos) {
					String dica = tipoCampo.getDica();
					String nome3 = tipoCampo.getNome();
					boolean obrigatorio = tipoCampo.getObrigatorio();
					String opcoes = tipoCampo.getOpcoes();
					Integer ordem2 = tipoCampo.getOrdem();
					Integer tamanhoMaximo = tipoCampo.getTamanhoMaximo();
					Integer tamanhoMinimo = tipoCampo.getTamanhoMinimo();
					TipoEntradaCampo tipo = tipoCampo.getTipo();
					BaseInterna baseInterna = tipoCampo.getBaseInterna();
					Boolean editavel = tipoCampo.getEditavel();
					String opcaoId = tipoCampo.getOpcaoId();
					String pais = tipoCampo.getPais();
					String criterioExibicao = tipoCampo.getCriterioExibicao();
					String criterioFiltro = tipoCampo.getCriterioFiltro();

					TipoCampo tipoCampo2 = new TipoCampo();
					tipoCampo2.setDica(dica);
					tipoCampo2.setGrupo(grupo2);
					tipoCampo2.setNome(nome3);
					tipoCampo2.setObrigatorio(obrigatorio);
					tipoCampo2.setOpcoes(opcoes);
					tipoCampo2.setOrdem(ordem2);
					tipoCampo2.setTamanhoMaximo(tamanhoMaximo);
					tipoCampo2.setTamanhoMinimo(tamanhoMinimo);
					tipoCampo2.setTipo(tipo);
					tipoCampo2.setBaseInterna(baseInterna);
					tipoCampo2.setEditavel(editavel);
					tipoCampo2.setOpcaoId(opcaoId);
					tipoCampo2.setPais(pais);
					tipoCampo2.setCriterioExibicao(criterioExibicao);
					tipoCampo2.setCriterioFiltro(criterioFiltro);
					tipoCampoService.saveOrUpdate(tipoProcesso2, tipoCampo2, usuario);
					Long tipoCampoId = tipoCampo.getId();
					Long tipoCampo2Id = tipoCampo2.getId();
					deParaCampo.put(tipoCampoId, tipoCampo2Id);
					campos2.add(tipoCampo2);
				}
				for (TipoCampo tipoCampo2 : campos2) {
					String pais = tipoCampo2.getPais();
					if(pais != null) {
						List<CampoAbstract.CampoPai> filhoObject = tipoCampo2.getPaisObject();
						for (CampoAbstract.CampoPai campoPai : filhoObject) {
							Long paiId = campoPai.getPaiId();
							Long paiId2 = deParaCampo.get(paiId);
							campoPai.setPaiId(paiId2);
						}
						tipoCampo2.setPaisObject(filhoObject);
						tipoCampoService.saveOrUpdate(tipoProcesso2, tipoCampo2, usuario);
					}
				}
			}
		}

		List<TipoDocumento> tiposDocumentos = tipoDocumentoService.findByTipoProcesso(tipoProcessoId, null, null);
		for (TipoDocumento tipoDocumento : tiposDocumentos) {
			String nome2 = tipoDocumento.getNome();
			boolean obrigatorio = tipoDocumento.getObrigatorio();
			Integer ordem = tipoDocumento.getOrdem();
			short taxaCompressao = tipoDocumento.getTaxaCompressao();
			ModeloOcr modeloOcr = tipoDocumento.getModeloOcr();
			Long codSia = tipoDocumento.getCodOrigem();
			Integer maximoPaginas = tipoDocumento.getMaximoPaginas();
			BigDecimal horasPrazo1 = tipoDocumento.getHorasPrazo();
			TipoPrazo tipoPrazo1 = tipoDocumento.getTipoPrazo();
			String descricao = tipoDocumento.getDescricao();

			TipoDocumento tipoDocumento2 = new TipoDocumento();
			tipoDocumento2.setNome(nome2);
			tipoDocumento2.setObrigatorio(obrigatorio);
			tipoDocumento2.setOrdem(ordem);
			tipoDocumento2.setTaxaCompressao(taxaCompressao);
			tipoDocumento2.setTipoProcesso(tipoProcesso2);
			tipoDocumento2.setModeloOcr(modeloOcr);
			tipoDocumento2.setCodOrigem(codSia);
			tipoDocumento2.setMaximoPaginas(maximoPaginas);
			tipoDocumento.setHorasPrazo(horasPrazo1);
			tipoDocumento.setTipoPrazo(tipoPrazo1);
			tipoDocumento.setDescricao(descricao);

			List<Irregularidade> irregularidadeList = new ArrayList<>();
			Long tipoDocumentoId = tipoDocumento.getId();
			List<IrregularidadeTipoDocumento> irregularidadeTipoDocumentoList = irregularidadeTipoDocumentoService.findByTipoDocumentoId(tipoDocumentoId);
			for (IrregularidadeTipoDocumento irregularidadeTipoDocumento : irregularidadeTipoDocumentoList) {
				Irregularidade irregularidade = irregularidadeTipoDocumento.getIrregularidade();
				irregularidadeList.add(irregularidade);
			}

			List<ModeloDocumento> modelosDocumentos = tipoDocumentoService.findModelosDocumentos(tipoDocumento.getId());
			List<ModeloDocumento> modelosDocumentosSelecionadosParaExpiracao = tipoDocumentoService.findModelosDocumentoToRequisitarExpiracao(tipoDocumentoId);
			tipoDocumentoService.saveOrUpdate(tipoDocumento2, usuario, modelosDocumentos, modelosDocumentosSelecionadosParaExpiracao, null, irregularidadeList, null, null);
		}

		SituacaoFiltro filtro = new SituacaoFiltro();
		filtro.setTipoProcessoId(tipoProcessoId);
		filtro.setAtiva(true);
		List<Situacao> situacoes = situacaoService.findByFiltro(filtro, null, null);
		Map<Situacao, Set<ProximaSituacao>> situacaoProximasMap = new LinkedHashMap<>();
		Map<Situacao, Situacao> situacao1Map = new LinkedHashMap<>();
		Map<Situacao, Situacao> situacao2Map = new LinkedHashMap<>();
		situacoes.forEach(situacao -> {
			String nome1 = situacao.getNome();
			BigDecimal horasPrazo1 = situacao.getHorasPrazo();
			TipoPrazo tipoPrazo1 = situacao.getTipoPrazo();
			StatusProcesso status = situacao.getStatus();
			boolean ativa = situacao.getAtiva();
			boolean distribuicaoAutomatica = situacao.isDistribuicaoAutomatica();
			boolean notificarAutor = situacao.isNotificarAutor();
			TipoPrazo tipoPrazoAdvertir = situacao.getTipoPrazoAdvertir();
			BigDecimal horasPrazoAdvertir = situacao.getHorasPrazoAdvertir();
			Set<ProximaSituacao> proximas = situacao.getProximas();

			Situacao situacao2 = new Situacao();
			situacao2.setHorasPrazo(horasPrazo1);
			situacao2.setTipoProcesso(tipoProcesso2);
			situacao2.setAtiva(ativa);
			situacao2.setDistribuicaoAutomatica(distribuicaoAutomatica);
			situacao2.setNome(nome1);
			situacao2.setStatus(status);
			situacao2.setTipoPrazo(tipoPrazo1);
			situacao2.setNotificarAutor(notificarAutor);
			situacao2.setTipoPrazoAdvertir(tipoPrazoAdvertir);
			situacao2.setHorasPrazoAdvertir(horasPrazoAdvertir);

			situacaoService.saveOrUpdate(situacao2, usuario);

			situacaoProximasMap.put(situacao2, proximas);
			situacao1Map.put(situacao, situacao2);
			situacao2Map.put(situacao2, situacao);
		});

		situacaoProximasMap.forEach((situacao2, proximaSituacaos) -> {
			proximaSituacaos.forEach(proximaSituacao -> {
				Situacao proxima = proximaSituacao.getProxima();
				Situacao proxima2 = situacao1Map.get(proxima);
				ProximaSituacao proximaSituacao2 = new ProximaSituacao();
				proximaSituacao2.setAtual(situacao2);
				proximaSituacao2.setProxima(proxima2);
				situacao2.getProximas().add(proximaSituacao2);
			});
			situacaoService.saveOrUpdate(situacao2, usuario);
		});

		Situacao situacaoInicial = tipoProcesso.getSituacaoInicial();
		Situacao situacaoInicial2 = situacao1Map.get(situacaoInicial);
		tipoProcesso2.setSituacaoInicial(situacaoInicial2);
		tipoProcessoRepository.saveOrUpdate(tipoProcesso2);

		for (TipoCampoGrupo grupo2 : grupos2) {
			Set<TipoCampoGrupoSituacao> situacoes2 = grupo2.getSituacoes();
			for (TipoCampoGrupoSituacao tcgs : situacoes2) {
				Situacao situacao = tcgs.getSituacao();
				Situacao situacao2 = situacao1Map.get(situacao);
				tcgs.setSituacao(situacao2);
				tipoCampoGrupoService.saveOrUpdate(grupo2, usuario);
			}
		}

		RegraFiltro filtro2 = new RegraFiltro();
		filtro2.setAtiva(true);
		filtro2.setTipoProcessoId(tipoProcesso.getId());
		List<Regra> regras = regraService.findByFiltro(filtro2, null, null);
		for (Regra regra : regras) {
			regraService.duplicar(regra, usuario, tipoProcesso2);
		}

		logAlteracaoService.registrarAlteracao(tipoProcesso2, usuario, TipoAlteracao.CRIACAO);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(TipoProcesso tipoProcesso, List<String> permissoesSelecionadas, Usuario usuario) throws MessageKeyException {

		TipoAlteracao tipoAlteracao = TipoAlteracao.getCriacaoOuAtualizacao(tipoProcesso);

		try {
			tipoProcessoRepository.saveOrUpdate(tipoProcesso);

			atualizaPermissoes(tipoProcesso, permissoesSelecionadas);

			if(TipoAlteracao.CRIACAO.equals(tipoAlteracao)) {
				criaSituacoesIniciais(tipoProcesso, usuario);
				criaCampanhaInicial(tipoProcesso, usuario);
			}
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}

		logAlteracaoService.registrarAlteracao(tipoProcesso, usuario, tipoAlteracao);
	}

	private void criaSituacoesIniciais(TipoProcesso tipoProcesso, Usuario usuario) {

		Situacao situacao1 = new Situacao();
		situacao1.setNome("1.1 - Aguardando Análise");
		situacao1.setTipoProcesso(tipoProcesso);
		situacao1.setTipoPrazo(TipoPrazo.HORAS);
		situacao1.setHorasPrazo(new BigDecimal(8));
		situacao1.setTipoPrazoAdvertir(TipoPrazo.HORAS);
		situacao1.setHorasPrazoAdvertir(new BigDecimal(8));
		situacao1.setAtiva(true);
		situacao1.setDistribuicaoAutomatica(true);
		situacao1.setStatus(StatusProcesso.AGUARDANDO_ANALISE);
		situacaoService.saveOrUpdate(situacao1, usuario);

		tipoProcesso.setSituacaoInicial(situacao1);
		tipoProcessoRepository.saveOrUpdate(tipoProcesso);

		Situacao situacao0 = new Situacao();
		situacao0.setNome("1.0 - Novo");
		situacao0.setTipoProcesso(tipoProcesso);
		situacao0.setTipoPrazo(TipoPrazo.HORAS);
		situacao0.setHorasPrazo(new BigDecimal(40));
		situacao0.setTipoPrazoAdvertir(TipoPrazo.HORAS);
		situacao0.setHorasPrazoAdvertir(new BigDecimal(40));
		situacao0.setAtiva(true);
		situacao0.setDistribuicaoAutomatica(false);
		situacao0.setStatus(StatusProcesso.RASCUNHO);
		Set<ProximaSituacao> proximas = situacao0.getProximas();
		ProximaSituacao proxima = new ProximaSituacao();
		proxima.setAtual(situacao0);
		proxima.setProxima(situacao1);
		proximas.add(proxima);
		situacao0.setProximas(proximas);
		situacaoService.saveOrUpdate(situacao0, usuario);
	}

	private void criaCampanhaInicial(TipoProcesso tipoProcesso, Usuario usuario) {

		Campanha campanha = new Campanha();
		campanha.setDescricao("Campanha Padrão");
		campanha.setTipoProcesso(tipoProcesso);
		campanha.setPadrao(true);
		campanhaService.saveOrUpdate(campanha, usuario, new ArrayList<>(), new ArrayList<>());
	}

	private void atualizaPermissoes(TipoProcesso tipoProcesso, List<String> permissoesSelecionadas) {

		Set<PermissaoTP> presentes = new HashSet<>();
		Long tipoProcessoId = tipoProcesso.getId();
		List<TipoProcessoPermissao> permissoes = tipoProcessoPermissaoService.findByTipoProcesso(tipoProcessoId);
		List<TipoProcessoPermissao> list = new ArrayList<>(permissoes);
		for (TipoProcessoPermissao tpp : list) {
			PermissaoTP permissao = tpp.getPermissao();
			String permissaoStr = permissao.name();
			if(!permissoesSelecionadas.contains(permissaoStr)) {
				permissoes.remove(tpp);
				Long tppId = tpp.getId();
				tipoProcessoPermissaoService.delete(tppId);
			}
			presentes.add(permissao);
		}

		for (String permissaoStr : permissoesSelecionadas) {
			PermissaoTP permissao = PermissaoTP.valueOf(permissaoStr);
			if(!presentes.contains(permissao)) {
				TipoProcessoPermissao tpp = new TipoProcessoPermissao();
				tpp.setPermissao(permissao);
				tpp.setTipoProcesso(tipoProcesso);
				tipoProcessoPermissaoService.saveOrUpdate(tpp);
			}
		}
	}

	public TipoProcesso getByNome(String nome) {
		return tipoProcessoRepository.getByNome(nome);
	}

	public List<TipoProcesso> findAtivos(List<PermissaoTP> permissoes) {
		return tipoProcessoRepository.findAtivos(permissoes);
	}

	public List<TipoProcesso> findAtivosAndInitialize(List<PermissaoTP> permissoes) {
		List<TipoProcesso> listTP = tipoProcessoRepository.findAtivos(permissoes);

		for (TipoProcesso tipoProcesso : listTP) {
			Set<TipoCampoGrupo> tipoCampoGrupo = tipoProcesso.getTipoCampoGrupo();
			Hibernate.initialize(tipoCampoGrupo);
			for (TipoCampoGrupo tcg : tipoCampoGrupo) {
				Set<TipoCampo> campos = tcg.getCampos();
				Hibernate.initialize(campos);
			}
		}
		return listTP;
	}

	public List<TipoProcessoVO> findAll(Integer inicio, Integer max, String sortField, SortOrder sortOrder) {
		return tipoProcessoRepository.findAll(inicio, max, sortField, sortOrder);
	}

	public List<TipoProcesso> findAll(Integer inicio, Integer max) {
		return tipoProcessoRepository.findAll(inicio, max);
	}

	public int count() {
		return tipoProcessoRepository.count();
	}

	public List<TipoProcesso> findByUsuario(Usuario usuario) {
		List<PermissaoTP> permissoes = PermissaoTP.getPermissoes(usuario.getRoleGD());
		return findAtivos(permissoes);
	}

	public List<TipoProcesso> findByIds(List<Long> ids) {
		return tipoProcessoRepository.findByIds(ids);
	}
}
