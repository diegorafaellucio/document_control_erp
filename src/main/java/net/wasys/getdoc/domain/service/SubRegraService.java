package net.wasys.getdoc.domain.service;

import net.wasys.getdoc.bean.SubRegraCrudBean;
import net.wasys.getdoc.bean.SubRegraCrudBean.DeparaParamVO;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.*;
import net.wasys.getdoc.domain.repository.SubRegraRepository;
import net.wasys.getdoc.domain.vo.FonteVO;
import net.wasys.getdoc.domain.vo.filtro.RegraFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.ddd.MessageKeyException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SubRegraService {

	private final Pattern p = Pattern.compile("^[^a-zA-Z_$]|[^\\w$]");

	@Autowired private LogAlteracaoService logAlteracaoService;
	@Autowired private RegraService regraService;
	@Autowired private RegraLinhaService regraLinhaService;
	@Autowired private SubRegraRepository subRegraRepository;
	@Autowired private TipoProcessoService tipoProcessoService;
	@Autowired private TipoCampoService tipoCampoService;
	@Autowired private BaseInternaService baseInternaService;
	@Autowired private FuncaoJsService funcaoJsService;
	@Autowired private BaseRegistroService baseRegistroService;
	@Autowired private BaseRegistroValorService baseRegistroValorService;
	@Autowired private TipoDocumentoService tipoDocumentoService;
	@Autowired private SubRegraAcaoService subRegraAcaoService;

	public SubRegra get(Long id) {
		return subRegraRepository.get(id);
	}

	@Transactional(rollbackFor=Exception.class)
	public void saveOrUpdate(SubRegra subRegra, Usuario usuario) throws MessageKeyException {

		RegraLinha linha = subRegra.getLinha();
		Regra regra = linha.getRegra();
		regra.setDataAlteracao(new Date());
		regraService.saveOrUpdate(regra, usuario, null, null);

		TipoAlteracao tipoAlteracao = TipoAlteracao.getCriacaoOuAtualizacao(subRegra);

		try {
			subRegraRepository.saveOrUpdate(subRegra);
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}

		logAlteracaoService.registrarAlteracao(subRegra, usuario, tipoAlteracao);
	}

	@Transactional(rollbackFor=Exception.class)
	public void excluir(Long subRegraId, Usuario usuarioLogado) throws MessageKeyException {

		SubRegra subRegra = get(subRegraId);

		try {
			subRegraRepository.deleteById(subRegraId);

			RegraLinha linha = subRegra.getLinha();
			linha = regraLinhaService.get(linha.getId());
			Set<SubRegra> subRegras = linha.getSubRegras();
			if(subRegras.isEmpty()) {
				regraLinhaService.excluir(linha.getId());
			}
		}
		catch (RuntimeException e) {
			HibernateRepository.verifyConstrantViolation(e);
			throw e;
		}

		logAlteracaoService.registrarAlteracao(subRegra, usuarioLogado, TipoAlteracao.EXCLUSAO);
	}

	public void clear() {
		subRegraRepository.clear();
	}

	@Transactional(rollbackFor=Exception.class)
	public void duplicar(SubRegra subRegra, RegraLinha linha2, Usuario usuario) throws MessageKeyException {

		BaseInterna baseInterna = subRegra.getBaseInterna();
		String condicionalJs = subRegra.getCondicionalJs();
		TipoConsultaExterna consultaExterna = subRegra.getConsultaExterna();
		FarolRegra farol = subRegra.getFarol();
		Boolean filhoSim = subRegra.getFilhoSim();
		String observacao = subRegra.getObservacao();
		Regra regraFilha = subRegra.getRegraFilha();
		TipoSubRegra tipo = subRegra.getTipo();
		String varConsulta = subRegra.getVarConsulta();

		SubRegra subRegra2 = new SubRegra();
		subRegra2.setLinha(linha2);
		subRegra2.setBaseInterna(baseInterna);
		subRegra2.setCondicionalJs(condicionalJs);
		subRegra2.setConsultaExterna(consultaExterna);
		subRegra2.setFarol(farol);
		subRegra2.setFilhoSim(filhoSim);
		subRegra2.setObservacao(observacao);
		subRegra2.setRegraFilha(regraFilha);
		subRegra2.setTipo(tipo);
		subRegra2.setVarConsulta(varConsulta);

		List<DeparaParam> deparaParams = subRegra.getDeparaParams();
		List<DeparaParam> deparaParams2 = subRegra2.getDeparaParams();
		for (DeparaParam deparaParam : deparaParams) {

			String destino = deparaParam.getDestino();
			String origem = deparaParam.getOrigem();
			String nomeFonte = deparaParam.getNomeFonte();

			DeparaParam deparaParam2 = new DeparaParam();
			deparaParam2.setSubRegra(subRegra2);
			deparaParam2.setDestino(destino);
			deparaParam2.setOrigem(origem);
			deparaParam2.setNomeFonte(nomeFonte);
			deparaParams2.add(deparaParam2);
		}

		List<DeparaRetorno> deparaRetornos = subRegra.getDeparaRetornos();
		List<DeparaRetorno> deparaRetornos2 = subRegra2.getDeparaRetornos();
		for (DeparaRetorno deparaRetorno : deparaRetornos) {

			TipoCampo tipoCampo = deparaRetorno.getTipoCampo();
			String origem = deparaRetorno.getOrigem();

			DeparaRetorno deparaRetorno2 = new DeparaRetorno();
			deparaRetorno2.setSubRegra(subRegra2);
			deparaRetorno2.setTipoCampo(tipoCampo);
			deparaRetorno2.setOrigem(origem);
			deparaRetornos2.add(deparaRetorno2);
		}

		saveOrUpdate(subRegra2, usuario);
	}

	@Transactional(rollbackFor=Exception.class)
	public void salvar(SubRegra subRegra, Usuario usuario, List<DeparaParamVO> deparaParamVOs, List<SubRegraCrudBean.DeparaRetornoVO> deparaRetornoVOs) throws MessageKeyException {

		SubRegra paiInativo = null;

		RegraLinha linha = subRegra.getLinha();
		boolean isCondicional = subRegra.isTipoCondicional();
		boolean isPaiCondicional = subRegra.isPaiCondicional();

		if (isPaiCondicional) {
			validarFilhoSimFilhoNao(subRegra);
			if(isCondicional) {
				validarFilhaCondicional(subRegra);
				paiInativo = criarPaiInativo(subRegra);
			}
		}

		if(isCondicional) {
			Regra regra = linha.getRegra();
			List<String> variaveisDisponiveis = getVariaveisDisponiveis(subRegra, regra);
			String condicionalJs = subRegra.getCondicionalJs();
			Set<String> varsCondicao = regraService.getTodasVarsCondicao(condicionalJs);

			for (String var : varsCondicao) {
				if(!variaveisDisponiveis.contains(var)) {
					throw new MessageKeyException("subregraResultadoCondicao-variavel.error", var);
				}
			}
		}

		boolean tipoBaseInterna = subRegra.isTipoBaseInterna();
		boolean tipoConsultaExterna = subRegra.isTipoConsultaExterna();
		boolean tipoChamadaRegra = subRegra.isTipoChamadaRegra();
		boolean tipoFim = subRegra.isTipoFim();

		if(tipoBaseInterna || tipoConsultaExterna) {
			validarVarConsulta(subRegra);
		}
		if (!tipoChamadaRegra) {
			subRegra.setRegraFilha(null);
		}
		if (!tipoConsultaExterna) {
			subRegra.setConsultaExterna(null);
		}
		if (!tipoBaseInterna) {
			subRegra.setBaseInterna(null);
		}
		if (!tipoFim && !tipoChamadaRegra) {
			subRegra.setFarol(null);
		}
		if(!tipoConsultaExterna && !tipoBaseInterna) {
			subRegra.getDeparaParams().clear();
			subRegra.getDeparaRetornos().clear();
		}

		regraLinhaService.saveOrUpdate(linha);

		saveOrUpdate(subRegra, usuario);
		if (paiInativo != null) {
			saveOrUpdate(paiInativo, usuario);
		}

		atualizarSubRegraAcoes(subRegra);

		if(tipoBaseInterna || tipoConsultaExterna) {
			atualizarDeparaParams(subRegra, deparaParamVOs);
			atualizarRetornoParams(subRegra, deparaRetornoVOs);
		}
	}

	private void atualizarSubRegraAcoes(SubRegra subRegra) {
		List<SubRegraAcao> subRegraAcoes = subRegra.getSubRegraAcoes();
		for(SubRegraAcao sra: subRegraAcoes){
			sra.setSubRegra(subRegra);
			Boolean todosDocumentosAprovados = sra.getTodosDocumentosAprovados();
			if(todosDocumentosAprovados != null && todosDocumentosAprovados){
				sra.setTipoDocumentoIds("");
			}
			subRegraAcaoService.saveOrUpdate(sra);
		}

		Long subRegraId = subRegra.getId();
		List<SubRegraAcao> salvos = subRegraAcaoService.findBySubRegra(subRegraId);

		salvos.removeAll(subRegraAcoes);

		salvos.forEach(s -> subRegraAcaoService.delete(s));
	}

	private void validarFilhaCondicional(SubRegra subRegra) throws MessageKeyException {

		RegraLinha regraLinhaFilha = subRegra.getLinha().getFilha();

		if(regraLinhaFilha != null) {
			regraLinhaFilha = regraLinhaService.get(regraLinhaFilha.getId());

			if(regraLinhaFilha.isCondicional()) {
				throw new MessageKeyException("subregraResultadoCondicao-filha-condicao.error");
			}
		}
	}

	private SubRegra criarPaiInativo(SubRegra subRegra) {

		SubRegra inativo = new SubRegra();

		RegraLinha linha = subRegra.getLinha();
		inativo.setTipo(TipoSubRegra.INATIVA);
		inativo.setLinha(linha);
		inativo.setFilhoSim(subRegra.getFilhoSim());

		regraLinhaService.saveOrUpdate(linha);

		RegraLinha novaLinha = new RegraLinha();
		novaLinha.setLinhaPai(linha);
		novaLinha.setRegra(linha.getRegra());

		regraLinhaService.saveOrUpdate(novaLinha);

		subRegra.setLinha(novaLinha);

		return inativo;
	}

	private void atualizarDeparaParams(SubRegra subRegra, List<DeparaParamVO> deparaParamVOs) {

		List<Long> idsDeparaParams = new ArrayList<>();

		DeparaParam deparaParam;
		for (DeparaParamVO vo : deparaParamVOs) {
			String origem = vo.getCampo();
			String destino = vo.getColuna();
			Long deparaParamId = vo.getDeparaParamId();
			idsDeparaParams.add(deparaParamId);

			deparaParam = criarOuBuscarDeparaParam(subRegra, deparaParamId);

			FonteVO fonte = vo.getFonte();
			if(fonte != null) {
				if(fonte.getBaseInterna() != null) {
					deparaParam.setBaseInterna(fonte.getBaseInterna());
				}
				else if(fonte.getFonteExterna() != null) {
					deparaParam.setFonteExterna(fonte.getFonteExterna());
				}
				else if(fonte.getTipoProcesso() != null) {
					deparaParam.setTipoProcesso(fonte.getTipoProcesso());
				}
				String nomeFonte = fonte.getNome();
				nomeFonte = DummyUtils.formatarNomeVariavel(nomeFonte);
				deparaParam.setNomeFonte(nomeFonte);
			}

			deparaParam.setOrigem(origem);
			deparaParam.setDestino(destino);
			Long id = deparaParam.getId();
			if(id == null || id.equals(0L)) {
				subRegra.getDeparaParams().add(deparaParam);
			}
		}

		List<DeparaParam> deparaParams = subRegra.getDeparaParams();
		for (Iterator<DeparaParam> iterator = deparaParams.iterator(); iterator.hasNext();) {
			DeparaParam dpp = iterator.next();

			Long deparaParamId = dpp.getId();
			if(!idsDeparaParams.contains(deparaParamId)) {
				iterator.remove();
			}
		}
	}

	private void atualizarRetornoParams(SubRegra subRegra, List<SubRegraCrudBean.DeparaRetornoVO> deparaParamVOs) {
		List<Long> idsDeparaParams = new ArrayList<>();

		DeparaRetorno deparaParam;
		for (SubRegraCrudBean.DeparaRetornoVO vo : deparaParamVOs) {
			String origem = vo.getOrigem();
			TipoCampo tipoCampo = vo.getTipoCampo();
			Long deparaParamId = vo.getDeparaParamId();
			idsDeparaParams.add(deparaParamId);

			deparaParam = criarOuBuscarDeparaRetorno(subRegra, deparaParamId);

			deparaParam.setOrigem(origem);
			deparaParam.setTipoCampo(tipoCampo);
			Long id = deparaParam.getId();
			if(id == null || id.equals(0L)) {
				subRegra.getDeparaRetornos().add(deparaParam);
			}
		}

		List<DeparaRetorno> deparaParams = subRegra.getDeparaRetornos();
		for (Iterator<DeparaRetorno> iterator = deparaParams.iterator(); iterator.hasNext();) {
			DeparaRetorno dpp = iterator.next();

			Long deparaParamId = dpp.getId();
			if(!idsDeparaParams.contains(deparaParamId)) {
				iterator.remove();
			}
		}

	}

	private DeparaParam criarOuBuscarDeparaParam(SubRegra subRegra, Long deparaParamId) {

		DeparaParam deparaParam = null;

		if(deparaParamId != null && !deparaParamId.equals(0L)) {

			List<DeparaParam> deparaParams = subRegra.getDeparaParams();
			for (DeparaParam dpp : deparaParams) {
				Long dppId = dpp.getId();
				if(deparaParamId.equals(dppId)) {
					deparaParam = dpp;
				}
			}
		}

		if(deparaParam == null) {
			deparaParam = new DeparaParam();
			deparaParam.setSubRegra(subRegra);
		}

		return deparaParam;
	}

	private DeparaRetorno criarOuBuscarDeparaRetorno(SubRegra subRegra, Long deparaParamId) {

		DeparaRetorno deparaRetorno = null;

		if(deparaParamId != null && !deparaParamId.equals(0L)) {

			List<DeparaRetorno> deparaParams = subRegra.getDeparaRetornos();
			for (DeparaRetorno dpp : deparaParams) {
				Long dppId = dpp.getId();
				if(deparaParamId.equals(dppId)) {
					deparaRetorno = dpp;
				}
			}
		}

		if(deparaRetorno == null) {
			deparaRetorno = new DeparaRetorno();
			deparaRetorno.setSubRegra(subRegra);
		}

		return deparaRetorno;
	}

	private void validarVarConsulta(SubRegra subRegra) throws MessageKeyException {

		subRegra.setVarConsulta(subRegra.getVarConsulta().trim());
		String varConsulta = subRegra.getVarConsulta();
		Matcher m = p.matcher(varConsulta);

		if(m.find()) {
			throw new MessageKeyException("subRegra.varConsultaInvalida.error");
		}
	}

	private void validarFilhoSimFilhoNao(SubRegra subRegra) throws MessageKeyException {

		RegraLinha linha = subRegra.getLinha();
		Set<SubRegra> irmaos = linha.getSubRegras();
		Long subregraId = subRegra.getId();
		for (SubRegra irmao : irmaos) {
			Long irmaoId = irmao.getId();
			Boolean irmaoFilhoSim = irmao.getFilhoSim();
			Boolean subregraFilhoSim = subRegra.getFilhoSim();
			if(!DummyUtils.equals(irmaoId, subregraId) && DummyUtils.equals(irmaoFilhoSim, subregraFilhoSim)) {
				throw new MessageKeyException("subregraResultadoCondicao-filhoSim-unique.error");
			}
		}
	}

	public Map<String, String> carregarFonteCampos(List<TipoProcesso> tiposProcessos) {

		Map<String, String> fonteCampos = new HashMap<>();
		StringBuilder sb;

		for (TipoProcesso tipoProcesso : tiposProcessos) {

			sb = new StringBuilder();

			String nomeProcesso = tipoProcesso.getNome();
			String fonteVariavel = DummyUtils.formatarNomeVariavel(nomeProcesso);

			Long tipoProcessoId = tipoProcesso.getId();
			List<String> fontesCampos = carregaFontesByTipoProcesso(tipoProcessoId);

			for (String grupoCampo : fontesCampos) {
				sb.append(fonteVariavel).append(grupoCampo).append("\n");
			}

			String camposFonte = sb.toString();
			fonteCampos.put(nomeProcesso, camposFonte);
		}

		return fonteCampos;
	}

	public List<String> carregaFontesByTipoProcesso(Long tipoProcessoId) {

		List<String> fontesCampos = new ArrayList<>();
		List<TipoCampo> campos = tipoCampoService.findByTipoProcesso(tipoProcessoId);
		for (TipoCampo campo : campos) {
			TipoCampoGrupo grupo = campo.getGrupo();
			String grupoNome = grupo.getNome();
			String chaveGrupo = "['" + grupoNome + "']";
			if(!fontesCampos.contains(chaveGrupo)) {
				fontesCampos.add(chaveGrupo);
			}
			String fonte = campo.getChaveFonte();
			fontesCampos.add(fonte);
			BaseInterna baseInterna = campo.getBaseInterna();
			if(baseInterna != null) {
				Long baseInternaId = baseInterna.getId();
				List<String> colunas = baseRegistroValorService.getColunasRegistro(baseInternaId);
				for (String coluna : colunas) {
					fontesCampos.add(fonte + "['" + coluna + "']");
				}
			}
		}
		Collections.sort(fontesCampos);

		List<String> fontesDadosCampos = new ArrayList<>();
		for (TipoCampo campo : campos) {
			fontesDadosCampos.add("['" + TipoCampo.DADOS_CAMPOS + "']" + campo.getChaveFonte() + "['" + TipoCampo.OBRIGATORIO + "']");
			fontesDadosCampos.add("['" + TipoCampo.DADOS_CAMPOS + "']" + campo.getChaveFonte() + "['" + TipoCampo.EDITAVEL + "']");
		}
		Collections.sort(fontesDadosCampos);


		List<String> fontesDocumetos = new ArrayList<>();
		List<TipoDocumento> listDocs = tipoDocumentoService.findByTipoProcesso(tipoProcessoId, null, null);
		for (TipoDocumento doc : listDocs) {
			fontesDocumetos.add("['DOCUMENTOS']['" + doc.getNome() + "']['" + Documento.ID + "']");
			fontesDocumetos.add("['DOCUMENTOS']['" + doc.getNome() + "']['" + Documento.OBRIGATORIO + "']");
			fontesDocumetos.add("['DOCUMENTOS']['" + doc.getNome() + "']['" + Documento.STATUS + "']");
			fontesDocumetos.add("['DOCUMENTOS']['" + doc.getNome() + "']['" + Documento.DATA_DIGITALIZACAO + "']");
			fontesDocumetos.add("['DOCUMENTOS']['" + doc.getNome() + "']['" + Documento.VERSAO_ATUAL + "']");
			fontesDocumetos.add("['DOCUMENTOS']['" + doc.getNome() + "']['" + Documento.STATUS_ORC + "']");
			fontesDocumetos.add("['DOCUMENTOS']['" + doc.getNome() + "']['" + Documento.IRREGULARIDADE_ID + "']");
			if (doc.getReconhecimentoFacial()) {
				fontesDocumetos.add("['DOCUMENTOS']['" + doc.getNome() + "']['" + Documento.PORCENTAGEM_RECONHECIMENTO_FACIAL + "']");
				fontesDocumetos.add("['DOCUMENTOS']['" + doc.getNome() + "']['" + Documento.STATUS_RECONHECIMENTO_FACIAL + "']");
			}
		}
		Collections.sort(fontesDocumetos);

		List<String> fontesOutrosDados = new ArrayList<>();
		fontesOutrosDados.add("['OUTROS DADOS']['DATA CRIACAO']");
		fontesOutrosDados.add("['OUTROS DADOS']['DATA ENVIO ANALISE']");
		fontesOutrosDados.add("['OUTROS DADOS']['DATA DE FINALIZAÇÃO']");
		fontesOutrosDados.add("['OUTROS DADOS']['DATA FINALIZACAO']");
		fontesOutrosDados.add("['OUTROS DADOS']['VEZES PENDENTE']");
		fontesOutrosDados.add("['OUTROS DADOS']['PASSOU POR PENDENCIA']");
		fontesOutrosDados.add("['OUTROS DADOS']['VEZES INCLUIDO ANALISE']");
		fontesOutrosDados.add("['OUTROS DADOS']['VEZES EM ANALISE']");
		fontesOutrosDados.add("['OUTROS DADOS']['QTDE DOCUMENTOS DIGITALIZADOS']");
		fontesOutrosDados.add("['OUTROS DADOS']['QTDE DOCUMENTOS EXCLUÍDOS']");
		fontesOutrosDados.add("['OUTROS DADOS']['QTDE DOCUMENTOS INCLUÍDOS']");
		fontesOutrosDados.add("['OUTROS DADOS']['QTDE DOCUMENTOS PENDENTES']");
		fontesOutrosDados.add("['OUTROS DADOS']['QTDE DOCUMENTOS OBRIGATÓRIOS PENDENTES']");
		fontesOutrosDados.add("['OUTROS DADOS']['QTDE DOCUMENTOS APROVADOS']");
		fontesOutrosDados.add("['OUTROS DADOS']['QTDE DOCUMENTOS PROCESSANDO']");
		fontesOutrosDados.add("['OUTROS DADOS']['QTDE DOCUMENTOS OBRIGATORIOS']");
		fontesOutrosDados.add("['OUTROS DADOS']['QTDE DOCUMENTOS NAO APROVADOS <> OUTROS']");
		fontesOutrosDados.add("['OUTROS DADOS']['QTDE DOCUMENTOS OBRIGATORIOS NÃO DIGITALIZADOS']");
		fontesOutrosDados.add("['OUTROS DADOS']['SITUAÇÃO ATUAL']");
		fontesOutrosDados.add("['OUTROS DADOS']['SITUAÇÃO ANTERIOR']");

		List<String> fontes = new ArrayList<>();
		fontes.addAll(fontesCampos);
		fontes.addAll(fontesDadosCampos);
		fontes.addAll(fontesDocumetos);
		fontes.addAll(fontesOutrosDados);

		return fontes;
	}

	public List<String> getVariaveisDisponiveis(SubRegra subRegra, Regra regra) {

		List<String> list = new ArrayList<>();

		List<FonteVO> basesInternasDisponiveis = regraService.buscarBasesInternasDisponiveis(subRegra);
		for (FonteVO vo : basesInternasDisponiveis) {
			String nome = vo.getNome();
			list.add(nome);
			BaseInterna baseInterna = vo.getBaseInterna();
			Long baseInternaId = baseInterna.getId();
			Set<String> campos = baseInternaService.findCamposById(baseInternaId);
			for (String campo : campos) {
				list.add(nome + campo);
			}
		}

		List<FonteVO> consultasExternasDisponiveis = regraService.buscarConsultasExternasDisponiveis(subRegra);
		for (FonteVO vo : consultasExternasDisponiveis) {
			String nome = vo.getNome();
			list.add(nome);
			FonteExterna fonteExterna = vo.getFonteExterna();
			if(fonteExterna != null) {
				String camposResultado = fonteExterna.getCamposResultado();
				String[] campos = camposResultado.split("\n");

				for (String campo : campos) {
					list.add(nome + StringUtils.trim(campo));
				}
			}
		}

		List<FuncaoJs> funcoes = funcaoJsService.findAll();
		for (FuncaoJs vo : funcoes) {
			String nome = vo.getNome();
			list.add(nome);
		}

		Long regraId = regra.getId();
		//considera que sempre vai ter apenas um
		List<Long> tiposProcessos = regraService.findTiposProcessoByRegraId(regraId);
		Long tipoProcessoId = tiposProcessos.isEmpty() ? null : tiposProcessos.get(0);

		List<String> fontesTipoProcesso = carregaFontesByTipoProcesso(tipoProcessoId);
		TipoProcesso tipoProcesso = tipoProcessoService.get(tipoProcessoId);
		String tipoProcessoNome = tipoProcesso.getNome();
		String fonteTipoProcesso = DummyUtils.formatarNomeVariavel(tipoProcessoNome);
		list.add(fonteTipoProcesso);
		for (String fonte : fontesTipoProcesso) {
			list.add(fonteTipoProcesso + fonte);
		}

		Integer decisaoFluxo = regra.getDecisaoFluxo();
		if(decisaoFluxo != null) {
			Situacao situacao = regra.getSituacao();
			RegraFiltro filtro = new RegraFiltro();
			filtro.setSituacao(situacao);
			filtro.setAtiva(true);
			filtro.setApenasVigentes(true);
			List<Regra> regrasSituacao = regraService.findByFiltro(filtro, null, null);
			for (Regra regra1 : regrasSituacao) {
				String regra1Nome = regra1.getNome();
				subRegraRepository.deatach(regra1);
				list.add("RegrasDaSituacao[\"" + regra1Nome + "\"][\"status\"]");
				list.add("RegrasDaSituacao[\"" + regra1Nome + "\"][\"farol\"]");
				list.add("RegrasDaSituacao[\"" + regra1Nome + "\"][\"mensagem\"]");
				list.add("RegrasDaSituacao[\"" + regra1Nome + "\"][\"subRegraId\"]");
			}
			list.add("RegrasDaSituacao[\"temRegraVerde\"]");
			list.add("RegrasDaSituacao[\"temRegraAmarela\"]");
			list.add("RegrasDaSituacao[\"temRegraVermelho\"]");
			list.add("RegrasDaSituacao[\"temRegraErro\"]");
			list.add("RegrasDaSituacao[\"temRegraPendente\"]");
		}

		Collections.sort(list, String.CASE_INSENSITIVE_ORDER);

		return list;
	}
}