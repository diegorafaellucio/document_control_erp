package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.*;
import net.wasys.getdoc.domain.vo.CampoDinamicoVO;
import net.wasys.getdoc.domain.vo.filtro.ProcessoFiltro;
import net.wasys.getdoc.domain.vo.filtro.ProcessoFiltro.ConsiderarData;
import net.wasys.getdoc.domain.vo.filtro.ProcessoFiltro.TipoOrdenacao;
import net.wasys.getdoc.domain.vo.filtro.RelatorioLicenciamentoFiltro;
import net.wasys.getdoc.mb.utils.DateUtils;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.ddd.HibernateRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.primefaces.model.SortOrder;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
@SuppressWarnings("unchecked")
public class ProcessoRepository extends HibernateRepository<Processo> {

	public ProcessoRepository() {
		super(Processo.class);
	}

	public void atualizarUltimaAjuda(Long processoId, Long ultimaAjudaId) {
		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<Object>();
		hql	.append("update ").append(clazz.getName()).append(" ");
		if (ultimaAjudaId != null) {
			params.add(ultimaAjudaId);
			hql	.append("set ultimaAjudaId = ? ");
		} else {
			hql	.append("set ultimaAjudaId = null ");
		}
		hql	.append("where id = ? ");
		params.add(processoId);
		Query query = createQuery(hql, params);
		query.executeUpdate();
	}

	public void atualizarUltimaAcao(Long processoId, Date data) {
		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<>();
		hql	.append("update ").append(clazz.getName()).append(" ");
		if (data != null) {
			hql	.append("set dataUltimaAcaoAnalista = :data ");
		} else {
			hql	.append("set dataUltimaAcaoAnalista = null ");
		}
		hql	.append("where id = :processoId ");
		params.put("data", data);
		params.put("processoId", processoId);
		Query query = createQuery(hql, params);
		query.executeUpdate();
	}

	public Map<StatusProcesso, Long> countStatusByFiltro(ProcessoFiltro filtro) {

		StringBuilder hql = new StringBuilder();

		hql.append(" select status, count(*) from ").append(clazz.getName()).append(" p ");

		List<Object> params = makeQuery(filtro, hql);

		hql.append(" group by p.status ");
		hql.append(" order by p.status ");

		Query query = createQuery(hql.toString(), params);

		Map<StatusProcesso, Long> map = new LinkedHashMap<>();
		List<Object[]> list = query.list();
		for (Object[] objects : list) {
			StatusProcesso status = (StatusProcesso) objects[0];
			Long count = (Long) objects[1];
			map.put(status, count);
		}

		return map;
	}

	public List<Long> findIdsByFiltro(ProcessoFiltro filtro) {

		StringBuilder hql = new StringBuilder();

		hql.append(" select p.id ").append(getStartQuery()).append(" p ");

		List<Object> params = makeQuery(filtro, hql);

		makeOrderBy(filtro, hql);

		Query query = createQuery(hql.toString(), params);

		List<Long> list = query.list();
		return list;
	}

	public int countByFiltro(ProcessoFiltro filtro) {

		StringBuilder hql = new StringBuilder();

		hql.append(" select count(*) from ").append(clazz.getName()).append(" p ");

		List<Object> params = makeQuery(filtro, hql);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue();
	}

	private void makeOrderBy(ProcessoFiltro filtro, StringBuilder hql) {

		TipoOrdenacao tipoOrdenacao = filtro.getTipoOrdenacao();
		String campoOrdem = filtro.getCampoOrdem();
		if(StringUtils.isNotBlank(campoOrdem)) {

			campoOrdem = campoOrdem.replace("processo.", "p.");

			SortOrder ordem = filtro.getOrdem();
			String ordemStr = SortOrder.DESCENDING.equals(ordem) ? " desc " : " asc ";

			hql.append(" order by ").append(campoOrdem).append(ordemStr);

			if(StringUtils.upperCase(campoOrdem).contains("ACOMPANHAMENTO")){
				hql.append(", p.id");
			}

		}
		else if(TipoOrdenacao.PENDENCIAS_ANALISTA.equals(tipoOrdenacao)) {

			hql.append(" order by p.nivelPrioridade desc, p.tipoProcesso.nivelPrioridade desc, p.prazoLimiteSituacao asc, p.dataEnvioAnalise, p.id ");
		}
		else if(TipoOrdenacao.PENDENCIAS_AREA.equals(tipoOrdenacao)) {

			hql.append(" order by p.nivelPrioridade desc, p.prazoLimiteSituacao asc, p.id ");
		}
        else if(TipoOrdenacao.RECENTEMENTE_APROVADO.equals(tipoOrdenacao)) {

            hql.append(" order by p.dataFinalizacao desc");
        }
		else {

			hql.append(" order by p.id desc ");
		}
	}

	public List<Processo> findByFiltro(ProcessoFiltro filtro, Integer inicio, Integer max) {

		StringBuilder hql = new StringBuilder();

		TipoCampo tipoCampo = null;
		BaseInterna baseInterna = null;

		boolean isDinamycSort = filtro.isDynamicSort();

		if(isDinamycSort) {
			tipoCampo = filtro.getCampoDinamico();
			baseInterna = tipoCampo.getBaseInterna();

			hql.append("select p from ").append(clazz.getName()).append(" p ");
			hql.append(", ").append(Campo.class.getName()).append(" c ");
			hql.append(", ").append(BaseRegistroValor.class.getName()).append(" brv ");
		}else {
			hql.append(getStartQuery()).append(" p ");
		}

		List<ProcessoFiltro.Fetch> fetch = filtro.getFetch();
		if(fetch != null) {
			for (ProcessoFiltro.Fetch fetch1 : fetch) {
				hql.append(" left outer join fetch p.").append(fetch1.getColumn());
			}
		}

		List<Object> params = makeQuery(filtro, hql);

		if(isDinamycSort){
			hql.append(" 	and c.grupo.processo.id = p.id ");

			if(baseInterna == null) {
				TipoCampoGrupo grupo = tipoCampo.getGrupo();
				String grupoNome = grupo.getNome();
				String campoNome = tipoCampo.getNome();

				hql.append(" 	and c.grupo.nome = ? and c.nome = ? ");
				params.add(grupoNome);
				params.add(campoNome);
			} else {
				Long baseInternaId = baseInterna.getId();

				hql.append(" 	and brv.baseRegistro.baseInterna.id = c.baseInterna.id ");
				hql.append(" 	and brv.baseRegistro.chaveUnicidade = c.valor ");
				hql.append(" 	and brv.nome = brv.baseRegistro.baseInterna.colunaLabel ");
				hql.append(" 	and c.baseInterna.id = ? ");

				params.add(baseInternaId);
			}
		}

		if(isDinamycSort) {
			makeDynamicOrderBy(filtro, hql);
		} else {
			makeOrderBy(filtro, hql);
		}

		Query query = createQuery(hql.toString(), params);

		if(inicio != null) {
			query.setFirstResult(inicio);
		}
		if(max != null) {
			query.setMaxResults(max);
		}

		List<Processo> list = query.list();
		return list;
	}

	public List<Long> findIdsByFiltro(ProcessoFiltro filtro, Integer inicio, Integer max) {

		StringBuilder hql = new StringBuilder();

		hql.append(" select p.id from ").append(Processo.class.getName()).append(" p ");

		List<Object> params = makeQuery(filtro, hql);

		makeOrderBy(filtro, hql);

		Query query = createQuery(hql.toString(), params);

		if(inicio != null) {
			query.setFirstResult(inicio);
		}
		if(max != null) {
			query.setMaxResults(max);
		}

		List<Long> list = query.list();
		return list;
	}

	private void makeDynamicOrderBy(ProcessoFiltro filtro, StringBuilder hql) {
		TipoCampo campo = filtro.getCampoDinamico();

		if(campo!=null){
			SortOrder ordem = filtro.getOrdem();
			String ordemStr = SortOrder.DESCENDING.equals(ordem) ? " desc " : " asc ";
			String orderBy = " order by ";

			BaseInterna baseInterna = campo.getBaseInterna();
			if(baseInterna == null){
				orderBy = orderBy + " c.valor ";
			} else {
				orderBy = orderBy + " brv.valor ";
			}

			hql.append(orderBy).append(ordemStr);

		}

	}

	private List<Object> makeQuery(ProcessoFiltro filtro, StringBuilder hql) {

		List<Object> params = new ArrayList<>();

		Date dataInicio = filtro.getDataInicio();
		Date dataFim = filtro.getDataFim();
		Usuario analista = filtro.getAnalista();
		List<StatusProcesso> statusList = filtro.getStatusList();
		List<TipoProcesso> tiposProcesso = filtro.getTiposProcesso();
		List<Long> tiposProcessoNot = filtro.getTiposProcessoNot();
		Usuario analistaProx = filtro.getAnalistaProx();
		Subperfil subperfil = filtro.getSubperfil();
		Area areaPendencia = filtro.getAreaPendencia();
		Area areaPendenciaAnalista = filtro.getAreaPendenciaAnalista();
		Usuario usuarioRascunhos = filtro.getUsuarioRascunhos();
		Usuario autor = filtro.getAutor();
		ConsiderarData considerarData = filtro.getConsiderarData();
		considerarData = considerarData == null ? ProcessoFiltro.ConsiderarData.CRIACAO : considerarData;
		String cpfCnpj = filtro.getCpfCnpj();
		Date dataUltimaAlteracaoAnalistaFim = filtro.getDataUltimaAlteracaoAnalistaFim();
		List<Situacao> situacao = filtro.getSituacao();
		List<Situacao> proximasSituacoes = filtro.getProximaSituacao();
		Long processoId = filtro.getProcessoId();
		List<Long> processoIdList = filtro.getProcessoIdList();
		String nomeCliente = filtro.getNomeCliente();
		String texto = filtro.getTexto();
		Boolean possuiEmailNaoLido = filtro.getPossuiEmailNaoLido();
		List<StatusPrazo> statusPrazo = filtro.getStatusPrazo();
		Boolean regrasExecutadas = filtro.getRegrasExecutadas();
		String numCandidatoInscricao = filtro.getNumCandidatoInscricao();
		Aluno aluno = filtro.getAluno();
		List<CampoDinamicoVO> camposFiltro = filtro.getCamposFiltro();
		List<String> nomesSituacoes = filtro.getNomesSituacoes();
		List<Long> regionais = filtro.getRegionais();
		List<Long> campus = filtro.getCampus();
		Boolean poloParceiro = filtro.getPoloParceiro();
		Boolean utilizaTermo = filtro.getUtilizaTermo();
		List<Etapa> etapas = filtro.getEtapas();
		List<String> nomesEtapas = filtro.getNomesEtapas();
		List<String> cursos = filtro.getCursos();
		String area = filtro.getArea();
		String localDeOferta = filtro.getLocalDeOferta();
		Boolean dataFimDoDia = filtro.isDataFimDoDia();
		FaseEtapa faseEtapa = filtro.getFaseEtapa();
		List<String> periodosIngresso = filtro.getPeriodosIngresso();
		String matricula = filtro.getMatricula();
		List<Long> desconsiderarTipoProcessoIds = filtro.getDesconsiderarTipoProcessoIds();
		List<Long> desconsiderarSituacoesIds = filtro.getDesconsiderarSituacoesIds();
		boolean processoOriginalTransformado = filtro.isProcessoOriginalTransformado();
		List<StatusProcesso> statusProcessoListDesconsiderar = filtro.getDesconsiderarStatusProcesso();
		Long logImportacaoId = filtro.getLogImportacaoId();

		hql.append(" where 1=1 ");

		if(StringUtils.isNotBlank(cpfCnpj) || StringUtils.isNotBlank(numCandidatoInscricao) || StringUtils.isNotBlank(nomeCliente)) {

			if (StringUtils.isNotBlank(cpfCnpj)) {
				hql.append(" and ( ");
				hql.append(" 	p.aluno.id in ( ");
				hql.append(" 		select a2.id from ").append(Aluno.class.getName()).append(" a2 where a2.cpf = ? ");
				hql.append(" 	) ");
				hql.append(" 	or p.id in ( ");
				hql.append(" 		select c.grupo.processo.id ");
				hql.append(" 		from ").append(Campo.class.getName()).append(" c ");
				hql.append(" 		where c.grupo.nome = ? and c.nome = ? and upper(c.valor) like ? ");
				hql.append(" 	) ");
				hql.append(" ) ");
				params.add(cpfCnpj);
				params.add(CampoMap.CampoEnum.CPF_IMPORTACAO.getGrupo().getNome());
				params.add(CampoMap.CampoEnum.CPF_IMPORTACAO.getNome());
				params.add(cpfCnpj);
			}

			if(StringUtils.isNotBlank(numCandidatoInscricao)) {
				hql.append(" and (p.numCandidato = ? or p.numInscricao = ? ) ");
				params.add(numCandidatoInscricao.trim());
				params.add(numCandidatoInscricao.trim());
			}

			if(StringUtils.isNotBlank(nomeCliente)) {
				hql.append(" and ( ");
				hql.append(" 	p.aluno.id in ( ");
				hql.append(" 		select a.id from ").append(Aluno.class.getName()).append(" a where upper(a.nome) like ? ");
				hql.append(" 	) ");
				hql.append(" 	or p.id in ( ");
				hql.append(" 		select c.grupo.processo.id ");
				hql.append(" 		from ").append(Campo.class.getName()).append(" c ");
				hql.append(" 		where c.nome = ? ");
				hql.append(" 		and upper(c.valor) like ? ");
				hql.append(" 	) ");
				hql.append(" ) ");

				params.add("%" + StringUtils.upperCase(nomeCliente) + "%");
				params.add(CampoMap.CampoEnum.NOME_DO_CANDIDATO.getNome());
				nomeCliente = DummyUtils.substituirCaracteresEspeciais(nomeCliente);
				params.add("%" + StringUtils.upperCase(nomeCliente) + "%");
			}
		}
		else {

			if(dataInicio != null && processoId == null) {
				if (considerarData.equals(ConsiderarData.CRIACAO)) {
					hql.append(" and p.dataCriacao >= ? ");
				}
				else if (considerarData.equals(ConsiderarData.ENVIO_ANALISE)) {
					hql.append(" and p.dataEnvioAnalise >= ? ");
				}
				else if (considerarData.equals(ConsiderarData.FINALIZACAO)) {
					hql.append(" and p.dataFinalizacao >= ? ");
				}
				params.add(dataInicio);
			}

			if(dataFim != null) {
				if(dataFimDoDia) {
					Calendar c = Calendar.getInstance();
					c.setTime(dataFim);
					c.add(Calendar.DAY_OF_MONTH, 1);
					c.add(Calendar.SECOND, -1);
					dataFim = c.getTime();
				}

				if (considerarData.equals(ConsiderarData.CRIACAO)) {
					hql.append(" and p.dataCriacao <= ? ");
				}
				else if (considerarData.equals(ConsiderarData.ENVIO_ANALISE)) {
					hql.append(" and p.dataEnvioAnalise <= ? ");
				}
				else if (considerarData.equals(ConsiderarData.FINALIZACAO)) {
					hql.append(" and p.dataFinalizacao <= ? ");
				}

				params.add(dataFim);
			}
		}

		if(dataUltimaAlteracaoAnalistaFim != null) {
			hql.append(" and ( p.dataUltimaAcaoAnalista is null or p.dataUltimaAcaoAnalista <= ?) ");
			params.add(dataUltimaAlteracaoAnalistaFim);
		}

		if(situacao != null && !situacao.isEmpty()) {
			hql.append(" and p.situacao.id in ( -1 ");
			for (Situacao situacaoAdd : situacao) {
				hql.append(", ?");
				params.add(situacaoAdd.getId());
			}
			hql.append(" )");
		}

		if(proximasSituacoes != null && !proximasSituacoes.isEmpty()) {
			hql.append(" and p.situacao.id in ( select ps.atual.id from ").append(ProximaSituacao.class.getName()).append(" ps ");
			hql.append(" where ps.proxima.id in ( -1 ");
			for (Situacao situacaoAdd : proximasSituacoes) {
				hql.append(", ?");
				params.add(situacaoAdd.getId());
			}
			hql.append(" )) ");
		}

		if (etapas != null && !etapas.isEmpty()) {
			hql.append(" and p.situacao.etapa.id in ( -1 ");
			for (Etapa etapaAdd : etapas) {
				hql.append(", ?");
				params.add(etapaAdd.getId());
			}
			hql.append(" )");
		}

		String aux = " and ";
		if(analista != null && areaPendencia != null) {
			aux = " or ";
			hql.append(" and ( 1 != 1 ");
		}
		if(analista != null) {
			Long analistaId = analista.getId();
			hql.append(aux + " ( p.analista.id = ? ");
			params.add(analistaId);
			Boolean trazerNaoAssociados = filtro.getTrazerNaoAssociados();
			if(trazerNaoAssociados != null && trazerNaoAssociados) {
				hql.append(" or p.analista is null ");
			}
			hql.append(" ) ");
		}
		if(areaPendencia != null) {
			Long areaId = areaPendencia.getId();
			hql.append(aux + " ( p.id in ( ");
			hql.append(" 	select s.processo.id from ").append(Solicitacao.class.getName()).append(" s ");
			hql.append(" 	where s.status = ? ");
			params.add(StatusSolicitacao.ENVIADA);
			hql.append(" 	and s.subarea.area.id = ? ");
			params.add(areaId);
			hql.append(" ) ");
			appendUsuarioRascunho(hql, params, usuarioRascunhos);
		}
		if(analista != null && areaPendencia != null) {
			hql.append(" ) ");
		}

		if(areaPendenciaAnalista != null) {
			Long areaId = areaPendenciaAnalista.getId();
			hql.append(aux + " ( p.id in ( ");
			hql.append(" 	select s.processo.id from ").append(Solicitacao.class.getName()).append(" s ");
			hql.append(" 	where s.dataFinalizacao is null ");
			hql.append(" 	and s.subarea.area.id = ? ");
			params.add(areaId);
			hql.append(" ) ");
			appendUsuarioRascunho(hql, params, usuarioRascunhos);
		}

		if(autor != null) {
			Long autorId = autor.getId();
			hql.append(" and p.autor.id = ? ");
			params.add(autorId);
		}

		if(processoId != null) {
			hql.append(" and p.id = ").append(processoId);
		}

		if(CollectionUtils.isNotEmpty(processoIdList)) {
			hql.append(" and p.id in ( -1 ");
			for (Long processoId1 : processoIdList) {
				hql.append(", ?");
				params.add(processoId1);
			}
			hql.append(" )");
		}

		if(aluno != null) {
			hql.append(" and p.aluno = ? ");
			params.add(aluno);
		}

		if(StringUtils.isNotBlank(texto)) {

			texto = StringUtils.trim(texto);
			texto = DummyUtils.substituirCaracteresEspeciais(texto);
			texto = StringUtils.upperCase(texto);

			hql.append(" and ( ");
			hql.append(" p.id in ( ");
			hql.append(" 	select i.documento.processo.id ");
			hql.append(" 	from ").append(Imagem.class.getName()).append(" i ");
			hql.append(" 	where i.fullText like ? ");
			params.add("%" + texto + "%");
			hql.append("    ) ");
			hql.append(" or p.id in ( ");
			hql.append(" 	select c.grupo.processo.id ");
			hql.append(" 	from ").append(Campo.class.getName()).append(" c ");
			hql.append(" 	where upper(c.valor) like ? ");
			params.add("%" + texto + "%");
			hql.append("   ) ");
			hql.append(" or p.id in ( ");
			hql.append(" 	select pl.processo.id ");
			hql.append(" 	from ").append(ProcessoLog.class.getName()).append(" pl ");
			hql.append(" 	where upper(pl.observacao) like ? ");
			params.add("%" + texto + "%");
			hql.append("   ) ");
			hql.append(" ) ");
		}

		if (possuiEmailNaoLido != null && possuiEmailNaoLido) {
			hql.append(" and (select count(*) from net.wasys.getdoc.domain.entity.EmailRecebido er where er.dataLeitura is null and er.processo = p ) > 0 ");
		}

		if (utilizaTermo != null && utilizaTermo) {
			hql.append(" and (select count(*) from ").append(Documento.class.getName()).append(" d where d.status not in (?, ?) and upper(d.nome) like ? and d.processo = p ) > 0 ");
			params.add(StatusDocumento.EXCLUIDO);
			params.add(StatusDocumento.INCLUIDO);
			params.add("%TERMO%");
		}

		if (poloParceiro != null && poloParceiro) {

			hql.append(" and p.id in ( ");
			hql.append(" 	select c.grupo.processo.id from ").append(Campo.class.getName()).append(" c ");
			hql.append(" 	where c.nome = ? and upper(c.valor) in ( ");
			hql.append(" 		select br.chaveUnicidade ");
			hql.append(" 		from ").append(BaseRegistro.class.getName()).append(" br, ").append(BaseInterna.class.getName()).append(" bi ");
			hql.append(" 		where br.baseInterna.id = bi.id and br.baseInterna.id = ? and ");
			hql.append(" 		br.id in (  ");
			hql.append(" 			select brv.baseRegistro.id from ").append(BaseRegistroValor.class.getName()).append(" brv ");
			hql.append(" 			where brv.nome = ? and brv.valor = 'Sim'  ");
			hql.append(" 		) ");
			hql.append(" 	) ");
			hql.append(" ) ");

			params.add(CampoMap.CampoEnum.CAMPUS.getNome());
			params.add(BaseInterna.CAMPUS_ID);
			params.add(TipoCampo.POLO_PARCEIRO);
		}

		if(regrasExecutadas != null) {
			hql.append(" and p.id in ( ");
			hql.append(" 	select pr.processo.id from ").append(ProcessoRegra.class.getName()).append(" pr where pr.status = ? ");
			hql.append(" ) ");
			params.add(StatusProcessoRegra.PENDENTE);
		}

		if(tiposProcesso != null && !tiposProcesso.isEmpty()) {
			hql.append(" and p.tipoProcesso.id in ( -1 ");
			for (TipoProcesso tipoProcesso : tiposProcesso) {
				hql.append(", ?");
				params.add(tipoProcesso.getId());
			}
			hql.append(" )");
		}

		if(tiposProcessoNot != null && !tiposProcessoNot.isEmpty()) {
			hql.append(" and p.tipoProcesso.id not in ( -1 ");
			for (Long tipoProcessoId : tiposProcessoNot) {
				hql.append(", ?");
				params.add(tipoProcessoId);
			}
			hql.append(" )");
		}

		if(desconsiderarTipoProcessoIds != null && !desconsiderarTipoProcessoIds.isEmpty()) {
			hql.append(" and p.tipoProcesso.id not in ( -1 ");
			for (Long tipoProcessoId : desconsiderarTipoProcessoIds) {
				hql.append(", ?");
				params.add(tipoProcessoId);
			}
			hql.append(" )");
		}

		if(CollectionUtils.isNotEmpty(desconsiderarSituacoesIds)) {
			hql.append(" and p.situacao.id not in ( -1 ");
			for (Long situacaoId : desconsiderarSituacoesIds) {
				hql.append(", ?");
				params.add(situacaoId);
			}
			hql.append(" )");
		}

		if(statusProcessoListDesconsiderar != null && !statusProcessoListDesconsiderar.isEmpty()) {
			hql.append(" and p.status not in ( '' ");
			for (StatusProcesso status : statusProcessoListDesconsiderar) {
				hql.append(", ?");
				params.add(status);
			}
			hql.append(" )");
		}

		if(processoOriginalTransformado) {
			hql.append(" and UPPER(p.processoOriginal.situacao.nome) like '% - ALUNO%'");
		}

		if(camposFiltro != null && !camposFiltro.isEmpty()) {
			for (CampoDinamicoVO cd : camposFiltro) {
				String nomeGrupo = cd.getNomeGrupo();
				String nomeCampo = cd.getNomeCampo();

				List<String> chavesUnicidade = cd.getChavesUnicidade();
				String valorCampo = cd.getValorCampo();
				if(StringUtils.isBlank(valorCampo) && CollectionUtils.isEmpty(chavesUnicidade)) {
					continue;
				}

				if(CampoMap.CampoEnum.NUM_INSCRICAO.getNome().equals(nomeCampo)) {
					if(StringUtils.isNotBlank(valorCampo)) {
						hql.append(" and p.numInscricao = ? ");
						valorCampo = valorCampo.trim();
						params.add(valorCampo);
					}
					if(CollectionUtils.isNotEmpty(chavesUnicidade)) {
						hql.append(" and ( 1=2 ");
						for (String chaveUnicidade : chavesUnicidade) {
							hql.append(" 	or p.numInscricao = ? ");
							chaveUnicidade = chaveUnicidade.trim();
							params.add(chaveUnicidade);
						}
						hql.append(" )");
					}

					continue;
				}
				else if (CampoMap.CampoEnum.NUM_CANDIDATO.getNome().equals(nomeCampo)) {
					if(StringUtils.isNotBlank(valorCampo)) {
						hql.append(" and p.numCandidato = ? ");
						valorCampo = valorCampo.trim();
						params.add(valorCampo);
					}
					if(CollectionUtils.isNotEmpty(chavesUnicidade)) {
						hql.append(" and ( 1=2 ");
						for (String chaveUnicidade : chavesUnicidade) {
							hql.append(" 	or p.numCandidato = ? ");
							chaveUnicidade = chaveUnicidade.trim();
							params.add(chaveUnicidade);
						}
						hql.append(" )");
					}

					continue;
				}

				hql.append(" and p.id in ( ");
				hql.append(" 	select c.grupo.processo.id ");
				hql.append(" 	from ").append(Campo.class.getName()).append(" c ");
				hql.append(" 	where c.grupo.nome = ?  ");
				params.add(nomeGrupo);
				hql.append(" 	and c.nome = ? ");
				params.add(nomeCampo);
				if(chavesUnicidade != null){
					hql.append(" and ( 1=2 ");
					for(String chaveUnicidade : chavesUnicidade){
						hql.append(" 	or upper(c.valor) like ? ");
						chaveUnicidade = StringUtils.upperCase(chaveUnicidade);
						params.add(chaveUnicidade);
					}
					hql.append(" )");
				}
				else{
					hql.append(" and upper(c.valor) like ? ");
					valorCampo = valorCampo.toUpperCase();
					valorCampo = valorCampo.trim();
					params.add("%" + valorCampo + "%");
				}
				hql.append(" ) ");
			}
		}

		if(statusList != null && !statusList.isEmpty()) {
			hql.append(" and (p.status in ( '-1' ");
			for (StatusProcesso status : statusList) {
				hql.append(", ?");
				params.add(status);
			}

			appendUsuarioRascunho(hql, params, usuarioRascunhos);

			hql.append(" ) ");
		}

		if(analistaProx != null) {
			Long analistaId2 = analistaProx.getId();

			hql.append(" and (p.analista.id is null or p.analista.id = ?)");
			params.add(analistaId2);

			hql.append(" and p.tipoProcesso.id in ( ");
			hql.append(" 	select utp.tipoProcesso.id from ").append(UsuarioTipoProcesso.class.getName()).append(" utp where utp.usuario.id = ? ");
			hql.append(" ) ");
			params.add(analistaId2);

			hql.append(" and p.situacao.distribuicaoAutomatica is true ");

			if(subperfil == null) {
				hql.append(" and p.situacao.id in (");
				hql.append(" 	select sps.situacao.id ");
				hql.append(" 	from ").append(SubperfilSituacao.class.getName()).append(" sps, ");
				hql.append(" 	").append(Usuario.class.getName()).append(" u ");
				hql.append(" 	where sps.subperfil.id = u.subperfilAtivo.id ");
				hql.append(" 	and u.id =? ");
				hql.append(" ) ");
				params.add(analistaId2);
			}
		}

		if(subperfil != null) {
			hql.append(" and p.situacao.id in (");
			hql.append(" 	select sps.situacao.id ");
			hql.append(" 	from ").append(SubperfilSituacao.class.getName()).append(" sps ");
			hql.append(" 	where sps.subperfil.id = ? ");
			hql.append(" ) ");
			Long subperfilId = subperfil.getId();
			params.add(subperfilId);
		}

		if(statusPrazo != null && !statusPrazo.isEmpty()) {
			hql.append(" and p.statusPrazo in ('' ");
			for (StatusPrazo sp : statusPrazo) {
				hql.append(", ?");
				params.add(sp);
			}
			hql.append(" )");
		}

		if(nomesSituacoes != null && !nomesSituacoes.isEmpty()) {
			hql.append(" and p.situacao.nome in ( '-1' ");
			for (String nomeSituacao : nomesSituacoes) {
				hql.append(", ?");
				params.add(nomeSituacao);
			}
			hql.append(" )");
		}

		if (nomesEtapas != null && !nomesEtapas.isEmpty()) {
			hql.append(" and p.situacao.etapa.nome in ( '-1' ");
			for (String nomeEtapa : nomesEtapas) {
				hql.append(", ?");
				params.add(nomeEtapa);
			}
			hql.append(" )");
		}

		if(regionais != null && !regionais.isEmpty()) {
			hql.append(" and p.id in ( ");
			hql.append(" 	select c.grupo.processo.id ");
			hql.append(" 	from ").append(Campo.class.getName()).append(" c ");
			hql.append(" 	where c.nome = ? ");
			hql.append(" 	and upper(c.valor) in ('-1', '' ");
			params.add(CampoMap.CampoEnum.REGIONAL.getNome());
			for (Long regional : regionais) {
				hql.append(", ?");
				params.add("[\"" + regional + "\"]");
			}
			hql.append(") ) ");
		}

		if(campus != null && !campus.isEmpty()) {
			hql.append(" and p.id in ( ");
			hql.append(" 	select c.grupo.processo.id ");
			hql.append(" 	from ").append(Campo.class.getName()).append(" c ");
			hql.append(" 	where c.nome = ? ");
			params.add(CampoMap.CampoEnum.CAMPUS.getNome());
			hql.append(" 	and upper(c.valor) in ('-1' ");
			for (Long campusl : campus) {
				hql.append(", ?");
				params.add("[\"" + campusl + "\"]");
			}
			hql.append(") ) ");
		}

		if(cursos != null && !cursos.isEmpty()) {
			hql.append(" and p.id in ( ");
			hql.append(" 	select c.grupo.processo.id ");
			hql.append(" 	from ").append(Campo.class.getName()).append(" c ");
			hql.append(" 	where c.nome = ? ");
			params.add(CampoMap.CampoEnum.CURSO.getNome());
			hql.append(" 	and upper(c.valor) in ('-1' ");
			for (String cursos1 : cursos) {
				hql.append(", ?");
				params.add(cursos1);
			}
			hql.append(") ) ");
		}

		if (periodosIngresso != null && !periodosIngresso.isEmpty()) {
			hql.append(" and p.id in ( ");
			hql.append(" 	select c.grupo.processo.id ");
			hql.append(" 	from ").append(Campo.class.getName()).append(" c ");
			hql.append(" 	where c.nome = ? ");
			params.add(CampoMap.CampoEnum.PERIODO_DE_INGRESSO.getNome());
			hql.append(" 	and upper(c.valor) in ('-1' ");
			for (String periodoIngresso : periodosIngresso) {
				hql.append(", ?");
				params.add(periodoIngresso);
			}
			hql.append(") ) ");
		}

		if (StringUtils.isNotBlank(matricula)) {
			hql.append(" and p.id in ( ");
			hql.append(" 	select c.grupo.processo.id ");
			hql.append(" 	from ").append(Campo.class.getName()).append(" c ");
			hql.append(" 	where c.nome = ? ");
			params.add(CampoMap.CampoEnum.MATRICULA.getNome());
			hql.append(" 	and upper(c.valor) like upper(?) ");
			params.add(matricula);
			hql.append(" ) ");
		}

		if(StringUtils.isNotBlank(area)) {
			hql.append(" and p.id in ( ");
			hql.append(" 	select c.grupo.processo.id ");
			hql.append(" 	from ").append(Campo.class.getName()).append(" c ");
			hql.append(" 	where c.nome = ? ");
			params.add(CampoMap.CampoEnum.AREA.getNome());
			hql.append(" 	and upper(c.valor) like upper(?) ");
			params.add(area);
			hql.append(" ) ");
		}

		if(StringUtils.isNotBlank(localDeOferta)) {
			hql.append(" and p.id in ( ");
			hql.append(" 	select c.grupo.processo.id ");
			hql.append(" 	from ").append(Campo.class.getName()).append(" c ");
			hql.append(" 	where c.nome = ? ");
			hql.append(" 	and upper(c.valor) like (?) )");
			params.add(CampoMap.CampoEnum.LOCAL_DE_OFERTA.getNome());
			params.add(localDeOferta);
		}

		if(faseEtapa != null) {
			hql.append(" and p.situacao.etapa.fase = ?");
			params.add(faseEtapa);
		}

		if(logImportacaoId != null ) {
			hql.append(" and p.id in ( ");
			hql.append(" 	select pl.processo.id ");
			hql.append(" 	from ").append(ProcessoLog.class.getName()).append(" pl ");
			hql.append(" 	where pl.logImportacao.id = ? ) ");
			params.add(logImportacaoId);
		}

		return params;
	}

	public List<Long> findIdsByUnicidade(Collection<Long> definemUnicidade, Map<Long, String> valoresUnicidade, Long tipoProcessoId, Long processoId) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new LinkedHashMap<>();

		hql.append(" select p.id from ").append(Processo.class.getName()).append(" p ");
        hql.append(" where 1=1 ");

		AtomicInteger i = new AtomicInteger(0);
		definemUnicidade.forEach((tipoCampoId) -> {
			hql.append(" and p.id in ( ");
			hql.append(" 	select c.grupo.processo.id from ").append(Campo.class.getName()).append(" c where ");
			hql.append(" 	c.tipoCampoId = "+":tipoCampoId_" + i + " and upper(c.valor) like " + ":valor_" + i +" ");
			hql.append(" ) ");

			params.put("tipoCampoId_" + i, tipoCampoId);
			String value = valoresUnicidade.get(tipoCampoId);
			value = value != null ? value : "";
			params.put("valor_" + i, value);
			i.getAndIncrement();
		});

		hql.append(" and p.status <>  :cancelado ");
		params.put("cancelado", StatusProcesso.CANCELADO);

		if (processoId != null){
            hql.append(" and p.id <> :id ");
            params.put("id", processoId);
        }

		if (tipoProcessoId != null){
			hql.append(" and p.id = :tipoProcessoId ");
			params.put("tipoProcessoId", tipoProcessoId);
		}

		hql.append(" order by p.id desc ");

		Query query = createQuery(hql.toString(), params);

		List<Long> list = query.list();
		return list;
	}

	private void appendUsuarioRascunho(StringBuilder hql, List<Object> params, Usuario usuarioRascunhos) {

		if(usuarioRascunhos != null) {

			hql.append(" ) or ( ");
			hql.append(" 	p.status in ( ?, ? ) and p.autor.id = ? ");
			hql.append(" ) ");

			params.add(StatusProcesso.RASCUNHO);
			params.add(StatusProcesso.PENDENTE);
			Long autorId = usuarioRascunhos.getId();
			params.add(autorId);
		}
		else {
			hql.append(" ) ");
		}
	}

	public List<Object[]> findToRelatorioGeral(Date dataInicio, Date dataFim, Long tipoProcessoId) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();
		hql.append(" select p.id,  ");
		hql.append(" (");
		hql.append(" 	select max(pl.data) from ").append(ProcessoLog.class.getName()).append(" pl where pl.processo.id = p.id ");
		hql.append(" ) as data_log ");
		hql.append(" from Processo p ");
		//a dataUltimaAtualizacao é atualizada sempre em RelatorioGeralJob.atualizarDataUltimaAtualizacaoProcessos()
		hql.append(" where p.dataUltimaAtualizacao between :dataInicio and :dataFim ");

		params.put("dataInicio", dataInicio);
		params.put("dataFim", dataFim);

		if(tipoProcessoId != null) {
			hql.append(" and p.tipoProcesso.id = :tipoProcessoId ");
			params.put("tipoProcessoId", tipoProcessoId);
		}

		hql.append(" order by data_log ");

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}

	public List<Processo> findByIds(List<Long> processosIds) {

		StringBuilder hql = new StringBuilder();
		hql.append(" select p from ").append(clazz.getName()).append(" p ");

		hql.append(" where p.id in (-1");
		for (Long processoId : processosIds) {
			hql.append(", ").append(processoId);
		}
		hql.append(") ");

		Query query = createQuery(hql.toString());
		query.setFetchSize(100);

		List<Processo> list = query.list();
		Set<Processo> set = new LinkedHashSet<Processo>();
		for (Processo processo : list) {
			set.add(processo);
		}

		return new ArrayList<Processo>(set);
	}

	public List<Processo> findAtrasosAnalistas(Date dataCorte) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" p ");
		hql.append(" where p.prazoLimiteAnalise < ? ");
		params.add(dataCorte);

		List<StatusProcesso> pendenciaAnalista = Arrays.asList(StatusProcesso.EM_ANALISE, StatusProcesso.PENDENTE);
		hql.append(" and p.status in ( '-1' ");
		for (StatusProcesso status : pendenciaAnalista) {
			hql.append(", ?");
			params.add(status);
		}
		hql.append(" )");

		hql.append(" order by p.prazoLimiteAnalise ");

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}

	public Integer countStatusByCpfCnpj(String cpfCnpj, List<StatusProcesso> statusList) {
		StringBuilder hql = new StringBuilder();

		List<Object> params = new ArrayList<Object>();
		hql.append(" select count(*) from net.wasys.getdoc.domain.entity.Processo p ");
		hql.append(" inner join p.aluno a ");
		hql.append(" where a.cpf = ?");
		params.add(cpfCnpj);

		hql.append(" and p.status in ( '-1' ");

		for (StatusProcesso status : statusList) {
			hql.append(", ?");
			params.add(status);
		}
		hql.append(" ) ");

		Query query = createQuery(hql.toString(), params);
		return ((Number) query.uniqueResult()).intValue();
	}

	public List<Processo> testeCemProcessos() {

		StringBuilder hql = new StringBuilder();

		hql.append(" from ").append(clazz.getName()).append(" p ");
		hql.append(" order by p.id desc ");

		Query query = createQuery(hql.toString());
		query.setMaxResults(100);

		return query.list();
	}

	public List<Object[]> findRelatorioLicenciamento(RelatorioLicenciamentoFiltro filtro) {

		List<Date> meses = filtro.getMeses();

		List<Object> params = new ArrayList<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select p.tipoProcesso.id, ");
		hql.append(" p.tipoProcesso.nome, ");

		for(Date mes : meses){
			hql.append(" count(CASE WHEN MONTH(p.dataCriacao) = ? and YEAR(p.dataCriacao) = ? then dataCriacao END)");
			params.add(DateUtils.getMonth(mes));
			params.add(DateUtils.getYear(mes));
			if (meses.indexOf(mes) != meses.size()-1){
				hql.append(", ");
			}
		}
		hql.append(" from ").append(clazz.getName()).append(" p ");
		hql.append(" group by p.tipoProcesso.nome, p.tipoProcesso.id ");
		hql.append(" order by p.tipoProcesso.nome ");

		Query query = createQuery(hql,params);

		List<Object[]> list = query.list();
		return list;
	}

	public Processo getLastByFiltro(ProcessoFiltro filtro) {

		StringBuilder hql = new StringBuilder();

		hql.append(" SELECT p FROM ").append(clazz.getName()).append(" p ");
		hql.append(" INNER JOIN ").append(CampoGrupo.class.getName()).append(" cg ").append(" on p.id = cg.processo.id ");
		hql.append( "INNER JOIN ").append(Campo.class.getName()).append(" c ").append(" on cg.id = c.grupo.id ");

		List<Object> params = makeQuery(filtro, hql);
		hql.append(" and c.nome = ? ");
		params.add(CampoMap.CampoEnum.PERIODO_DE_INGRESSO.getNome());
		hql.append(" order by c.valor desc ");

		Query query = createQuery(hql.toString(), params);
		query.setMaxResults(1);

		return (Processo) query.uniqueResult();

	}

	public List<Long> fintToNotificarAprovacao(Date dataCorte) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new LinkedHashMap<>();

		hql.append(" select p.id ");
		hql.append(" from ").append(clazz.getName()).append(" p ");
		hql.append(" where ");
		hql.append("  (select count(*) from ").append(Campo.class.getName()).append(" c where c.grupo.processo.id = p.id and c.nome = :campoEmail and c.valor <> '' and c.valor <> '^[A-Za-z0-9._%-]+@[A-Za-z0-9.-]+[.][A-Za-z]+$' ) > 0");
		hql.append("  	and p.situacao.id in (:situacoesAprovacao) and ( ");
		hql.append(" 		select count(*) from ").append(ProcessoLog.class.getName()).append(" pl ");
		hql.append(" 		where pl.processo.id = p.id and pl.acao = :envioEmailNotificacaoAprovacao ");
		hql.append(" 	) = 0 ");
		hql.append(" and (select count(*) from ").append(ProcessoLog.class.getName()).append(" pl ");
		hql.append(" 		where pl.processo.id = p.id and pl.acao = :erroAoNotificar ");
		hql.append(" 	) = 0 ");
		hql.append(" and p.dataCriacao > :dataCorte ");
		hql.append(" order by p.id ");

		params.put("situacoesAprovacao", Situacao.DOCUMENTACAO_APROVADA_IDS);
		params.put("envioEmailNotificacaoAprovacao", AcaoProcesso.ENVIO_EMAIL_NOTIFICACAO_APROVACAO);
		params.put("erroAoNotificar", AcaoProcesso.ERRO_NOTIFICAR_POR_EMAIL);
		params.put("campoEmail", CampoMap.CampoEnum.EMAIL.getNome());
		params.put("dataCorte", dataCorte);

		Query query = createQuery(hql, params);
		return query.list();
	}

	public List<Long> findToNotificarPendencia() {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select p.id ");
		hql.append(" from ").append(clazz.getName()).append(" p ");
		hql.append(" where ");
		hql.append("  (select count(*) from ").append(Campo.class.getName()).append(" c where c.grupo.processo.id = p.id and c.nome = :campoEmail and c.valor <> '' and c.valor <> '^[A-Za-z0-9._%-]+@[A-Za-z0-9.-]+[.][A-Za-z]+$' ) > 0");
		hql.append("  	and p.situacao.id in (:situacoesPendencia) and ( ");
		hql.append(" 		select count(*) from ").append(ProcessoLog.class.getName()).append(" pl ");
		hql.append(" 		where pl.processo.id = p.id and pl.acao = :envioEmailNotificacaoPendencia ");
		hql.append(" 	) = 0 ");
		hql.append(" and (select count(*) from ").append(ProcessoLog.class.getName()).append(" pl ");
		hql.append(" 		where pl.processo.id = p.id and pl.acao = :erroAoNotificar ");
		hql.append(" 	) = 0 ");
		hql.append(" order by p.id ");

		params.put("campoEmail", CampoMap.CampoEnum.EMAIL.getNome());
		params.put("situacoesPendencia", Situacao.DOCUMENTO_PENDENTE_IDS);
		params.put("envioEmailNotificacaoPendencia", AcaoProcesso.ENVIO_EMAIL_NOTIFICACAO_PENDENCIA);
		params.put("erroAoNotificar", AcaoProcesso.ERRO_NOTIFICAR_POR_EMAIL);


		Query query = createQuery(hql, params);
		List list = query.list();
		return list;
	}

    public List<Long> findToNotificarPendenciaRascunho(Date dataCorte) {

        Map<String, Object> params = new LinkedHashMap<>();
        StringBuilder hql = new StringBuilder();

		hql.append(" select p.id ");
		hql.append(" from ").append(clazz.getName()).append(" p ");
		hql.append(" where ");
		hql.append("  (select count(*) from ").append(Campo.class.getName()).append(" c where c.grupo.processo.id = p.id and c.nome = :campoEmail and c.valor <> '' and c.valor <> '^[A-Za-z0-9._%-]+@[A-Za-z0-9.-]+[.][A-Za-z]+$' ) > 0");
		hql.append("  	and p.situacao.id in (:situacoesRascunho) and ( ");
		hql.append(" 		select count(*) from ").append(ProcessoLog.class.getName()).append(" pl ");
		hql.append(" 		where pl.processo.id = p.id and pl.acao = :envioEmailNotificacaoRascunho ");
		hql.append(" 	) = 0 ");
		hql.append(" and (select count(*) from ").append(ProcessoLog.class.getName()).append(" pl ");
		hql.append(" 		where pl.processo.id = p.id and pl.acao = :erroAoNotificar ");
		hql.append(" 	) = 0 ");
		hql.append(" and p.dataCriacao > :dataCorte ");
		hql.append(" order by p.id ");

        params.put("dataCorte", dataCorte);
		params.put("campoEmail", CampoMap.CampoEnum.EMAIL.getNome());
        params.put("situacoesRascunho", Situacao.RASCUNHO_IDS);
        params.put("envioEmailNotificacaoRascunho", AcaoProcesso.ENVIO_EMAIL_NOTIFICACAO_RASCUNHO);
		params.put("erroAoNotificar", AcaoProcesso.ERRO_NOTIFICAR_POR_EMAIL);

        Query query = createQuery(hql, params);
        return query.list();
    }

	public List<Long> findIdsProcessoParaEnvioDeEmailSisFiesAndSisProuni(Integer max, List<Long> tiposProcessosIds) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new LinkedHashMap<>();

		hql.append("select p.id from ").append(clazz.getName()).append(" p");
		hql.append(" where p.tipoProcesso.id in (:tiposProcesso)");
		hql.append(" and p.status = :status");
		hql.append(" and (select count(*) from ").append(ProcessoLog.class.getName()).append(" pl");
		hql.append(" where pl.processo.id = p.id and pl.acao = :acao) = 0");

		params.put("tiposProcesso", tiposProcessosIds);
		params.put("status", StatusProcesso.RASCUNHO);
		params.put("acao", AcaoProcesso.ENVIO_EMAIL_NOTIFICACAO_CANDIDATO_SISFIES_SISPROUNI);

		Query query = createQuery(hql.toString(), params);

		if(max != null) {
			query.setMaxResults(max);
		}

		return query.list();
	}

	public List<Long> findIdsProcessoParaEnvioDeEmailPendenteSisProuni(Integer max) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new LinkedHashMap<>();

		hql.append("select p.id from ").append(clazz.getName()).append(" p");
		hql.append(" where p.tipoProcesso.id = :tipoProcesso ");
		hql.append(" and p.situacao.id in (:situacoesPendente) ");
		hql.append(" and p.status = :status");
		hql.append(" and (select count(*) from ").append(ProcessoLog.class.getName()).append(" pl");
		hql.append(" where pl.processo.id = p.id and pl.acao = :acao) = 0");

		params.put("tiposProcesso", TipoProcesso.SIS_PROUNI);
		params.put("situacoesPendente", Situacao.SITUACAO_PENDENTE_PROUNI_ID);
		params.put("status", StatusProcesso.PENDENTE);
		params.put("acao", AcaoProcesso.ENVIO_EMAIL_PENDENCIA_CANDIDATO_SISPROUNI);

		Query query = createQuery(hql.toString(), params);

		if (max != null) {
			query.setMaxResults(max);
		}

		return query.list();
	}

	public List<Long> findProcessoAnaliseDeIsencaoParaEnvioDeEmail(Integer max) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new LinkedHashMap<>();

		hql.append("select p.id from ").append(clazz.getName()).append(" p, ");
		hql.append(CampoGrupo.class.getName()).append(" cg ");
		hql.append(" where cg.processo.id = p.id ");
		hql.append(" and cg.nome = :nomeCampoGrupo ");
		hql.append(" and (select count(*) from ").append(ProcessoLog.class.getName()).append(" pl");
		hql.append(" where pl.processo.id = p.id and pl.acao = :acao) = 0");

		params.put("nomeCampoGrupo", CampoMap.GrupoEnum.ISENCAO_DISCIPLINA.getNome());
		params.put("acao", AcaoProcesso.ENVIO_EMAIL_NOTIFICACAO_ANALISE_ISENCAO);

		Query query = createQuery(hql.toString(), params);

		if(max != null) {
			query.setMaxResults(max);
		}

		return query.list();
	}

	public List<Long> findIdsProcessosGraduacaoComDocumentosDigitalizado() {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new LinkedHashMap<>();

		hql.append(" select p.id from ").append(Processo.class.getName()).append(" p ");
		hql.append(" where p.id in (select d.processo.id from ").append(Documento.class.getName()).append(" d ");
		hql.append(" where d.status = :statusDocumento ");
		hql.append(" and d.tipoDocumento.id not in (:tiposDocumentoIds) ");
		hql.append(" and d.processo.status = :statusProcesso and d.processo.tipoProcesso.id in (:tiposProcessoIds) ");
		hql.append(" group by d.processo.id, d.dataDigitalizacao ");
		hql.append(" having count(*) > 0 ");
		hql.append(" order by d.dataDigitalizacao asc) group by p.id ");

		params.put("tiposProcessoIds", TipoProcesso.GRADUACAO_IDS);
		params.put("tiposDocumentoIds", TipoDocumento.DOCUMENTOS_CONTRATO_PORTAL_IDS);
		params.put("statusDocumento", StatusDocumento.DIGITALIZADO);
		params.put("statusProcesso", StatusProcesso.RASCUNHO);

		Query query = createQuery(hql, params);
		query.setMaxResults(100);

		return query.list();
	}

	public List<Long> findIdsToEnvioParaAnalise() {
		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select distinct(d.processo.id) from ").append(Documento.class.getName()).append(" d ");
		hql.append(" where d.processo.status =:statusProcessoPendente  ");
		hql.append(" and d.processo.tipoProcesso.id not in (:tiposProcessoFiesProuni) ");
		hql.append(" and d.status = :statusDocumento ");
		hql.append(" and d.nome <> :documentoOutros ");
		hql.append(" order by d.processo.id asc ");

		params.put("statusProcessoPendente", StatusProcesso.PENDENTE);
		params.put("tiposProcessoFiesProuni", Arrays.asList(TipoProcesso.SIS_FIES, TipoProcesso.SIS_PROUNI));
		params.put("statusDocumento", StatusDocumento.DIGITALIZADO);
		params.put("documentoOutros", Documento.NOME_OUTROS);

		Query query = createQuery(hql.toString(), params);
		query.setFetchSize(100);
		query.setMaxResults(100);

		return query.list();
	}

	public List<Long> findIdsToEnvioParaAnaliseSisFiesProuniConcluido() {
		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select p.id from ").append(clazz.getName()).append(" p ");
		hql.append(" inner join ").append(Documento.class.getName()).append(" d ").append(" on p.id = d.processo.id ");
		hql.append(" where p.situacao.id in (:situacoesFiesProuniConcluido) ");
		hql.append(" and d.nome <> :documentoOutros ");
		hql.append(" and d.status = :statusDocumento ");
		hql.append(" and (d.dataDigitalizacao >= '2022-01-01 00:00:00.000000') ");
		hql.append(" group by p.id ");
		hql.append(" order by max(d.dataDigitalizacao) asc ");

		params.put("situacoesFiesProuniConcluido", Situacao.SITUACAO_CONCLUIDO_FIES_PROUNI_ENVIAR_PARA_ANALISE_ID);
		params.put("statusDocumento", StatusDocumento.DIGITALIZADO);
		params.put("documentoOutros", Documento.NOME_OUTROS);

		Query query = createQuery(hql.toString(), params);
		query.setFetchSize(100);
		query.setMaxResults(100);

		return query.list();
	}

	public Boolean isProcessoOriginal(Long processoId) {
		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select 1 from ").append(Processo.class.getName()).append(" p ");
		hql.append(" where p.processoOriginal.id = :processoId ");
		params.put("processoId", processoId);

		Query query = createQuery(hql.toString(), params);

		query.setMaxResults(1);

		return query.uniqueResult() != null;
	}

	public List<Processo> findProcessosOriginados(Long processoId){
		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append("select p from ").append(clazz.getName()).append(" p ");
		hql.append(" where p.processoOriginal.id = :processoId ");

		params.put("processoId", processoId);

		Query query = createQuery(hql.toString(), params);
		return query.list();
	}

	public String getModalidade(Long processoId) {

		StringBuilder hql = new StringBuilder();

		hql.append("select c.valor from ").append(clazz.getName()).append(" p ");
		hql.append(" inner join ").append(CampoGrupo.class.getName()).append(" cg ");
		hql.append(" on (p.id=cg.processo.id) ");
		hql.append(" inner join ").append(Campo.class.getName()).append(" c ");
		hql.append(" on (cg.id=c.grupo.id)  ");
		hql.append(" inner join ").append(TipoCampo.class.getName()).append(" tc ");
		hql.append(" on (c.tipoCampoId=tc.id)  ");
		hql.append(" where tc.nome = :modalidade and ");
		hql.append(" cg.nome = :grupoDadosDeImportacao and ");
		hql.append(" cg.processo.id = :processoId");

		Query query = createQuery(hql.toString());

		query.setParameter("modalidade", TipoCampo.MODALIDADE.toString());
		query.setParameter("grupoDadosDeImportacao", CampoMap.GrupoEnum.DADOS_DE_IMPORTACAO.getNome());
		query.setParameter("processoId", processoId);

		return (String) query.uniqueResult();
	}

	public List<Long> findIdsEnvioProcessoProuni(Long tipoProcessoId, String periodoIngresso, TipoParceiro tipoParceiro, TipoProuni tipoProuni, ListaChamada listaChamada) {
		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append("select p.id from ").append(clazz.getName()).append(" p ");
		hql.append(" where p.tipoProcesso.id = :tipoProcessoId ");
		hql.append(" and p.situacao.id not in (:situacoesIds) ");

		if (StringUtils.isNotBlank(periodoIngresso)) {
			hql.append(" and p.id in ( ");
			hql.append(" 	select c.grupo.processo.id ");
			hql.append(" 	from ").append(Campo.class.getName()).append(" c ");
			hql.append(" 	where c.nome = :campoPeriodoIngresso and c.grupo.nome = :grupoPeriodoIngresso ");
			hql.append(" 	and upper(c.valor) like upper(:valorPeriodoIngresso) )");
			params.put("campoPeriodoIngresso", CampoMap.CampoEnum.PERIODO_DE_INGRESSO_IMPORTACAO.getNome());
			params.put("grupoPeriodoIngresso", CampoMap.CampoEnum.PERIODO_DE_INGRESSO_IMPORTACAO.getGrupo().getNome());
			params.put("valorPeriodoIngresso", periodoIngresso);
		}

		if (tipoProuni != null && !tipoProuni.equals(TipoProuni.NULO)) {
			hql.append(" and p.id in ( ");
			hql.append(" 	select c.grupo.processo.id ");
			hql.append(" 	from ").append(Campo.class.getName()).append(" c ");
			hql.append(" 	where c.nome = :campoTipoProuni and c.grupo.nome = :grupoTipoProuni ");
			hql.append(" 	and upper(c.valor) like upper(:valorTipoProuni) )");
			params.put("campoTipoProuni", CampoMap.CampoEnum.TIPO_PROUNI.getNome());
			params.put("grupoTipoProuni", CampoMap.CampoEnum.TIPO_PROUNI.getGrupo().getNome());
			params.put("valorTipoProuni", tipoProuni.getTipoProuni());
		}

		if (listaChamada != null) {
			hql.append(" and p.id in ( ");
			hql.append(" 	select c.grupo.processo.id ");
			hql.append(" 	from ").append(Campo.class.getName()).append(" c ");
			hql.append(" 	where c.nome = :campoChamada and c.grupo.nome = :grupoChamada ");
			hql.append(" 	and upper(c.valor) like upper(:valorChamada) )");
			params.put("campoChamada", CampoMap.CampoEnum.NUMERO_CHAMADA.getNome());
			params.put("grupoChamada", CampoMap.CampoEnum.NUMERO_CHAMADA.getGrupo().getNome());
			params.put("valorChamada", listaChamada.getChamada());
		}

		if (tipoParceiro != null && !tipoParceiro.equals(TipoParceiro.NULO)) {
			hql.append(" and p.id in ( ");
			hql.append(" 	select c.grupo.processo.id ");
			hql.append(" 	from ").append(Campo.class.getName()).append(" c ");
			hql.append(" 	where c.nome = :campoPoloParceiro and c.grupo.nome = :grupoPoloParceiro ");
			hql.append(" 	and upper(c.valor) like upper(:valorPoloParceiro) )");
			params.put("campoPoloParceiro", CampoMap.CampoEnum.POLO_PARCEIRO.getNome());
			params.put("grupoPoloParceiro", CampoMap.CampoEnum.POLO_PARCEIRO.getGrupo().getNome());
			params.put("valorPoloParceiro", tipoParceiro.getEhPoloParceiro());
		}

		params.put("tipoProcessoId", tipoProcessoId);
		params.put("situacoesIds", Situacao.SITUACAO_NAO_ENVIAR_PARA_EMITIR_TR_PROUNI);

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}

	public int atualizarDataUltimaAtualizacao(Long processoId, Date dataUltimaAtualizacao) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<>();

		hql.append(" update ").append(clazz.getName()).append(" p ");
		hql.append(" set p.dataUltimaAtualizacao = :data ");
		hql.append("where id = :processoId ");

		params.put("data", dataUltimaAtualizacao);
		params.put("processoId", processoId);

		Query query = createQuery(hql, params);
		return query.executeUpdate();
	}

    public int atualizarStatusPrazo(Long processoId, StatusPrazo statusPrazo) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<>();

		hql.append(" update ").append(clazz.getName()).append(" p ");
		hql.append(" set p.statusPrazo = :statusPrazo ");
		hql.append("where id = :processoId ");

		params.put("statusPrazo", statusPrazo);
		params.put("processoId", processoId);

		Query query = createQuery(hql, params);
		return query.executeUpdate();
	}

	public List<Long> findProcessosIds(Long processoId, int limit) {
		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select p.id from ").append(clazz.getName()).append(" p ");
		hql.append(" where p.id > :processoId");
		hql.append(" order by p.id asc ");
		params.put("processoId", processoId);

		Query query = createQuery(hql, params);
		query.setMaxResults(limit);
		return query.list();

	}

	public boolean existsProcessoDuplicado(Processo processo, Long tipoProcessoId, String numCandidato){

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();
		Long processoId = processo.getId();
		TipoProcesso tipoProcesso = processo.getTipoProcesso();

		hql.append(" select 1 from ").append(clazz.getName()).append(" p ");
		hql.append(" where ");
		hql.append(" 	p.tipoProcesso.id = :tipoProcessoId ");
		hql.append(" 	and p.id <> :processoId ");
		hql.append(" 	and p.numCandidato = :numCandidato ");

		params.put("processoId", processoId);
		params.put("tipoProcessoId", tipoProcessoId);
		params.put("numCandidato", numCandidato);

		Query query = createQuery(hql.toString(), params);
		query.setMaxResults(1);

		Object result = query.uniqueResult();
		return result != null;
	}

	public List<Processo> findProcessosFinanciamentoNovoComAlteracao(Long tipoProcessoId, List<Long> situacaoList) {
		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select p from ").append(clazz.getName()).append(" p ");
		hql.append(" where ");
		hql.append(" 	p.tipoProcesso.id = :tipoProcessoId ");
		hql.append(" 	and p.situacao.id in ( :situacao ) ");
		hql.append(" 	and p.dataCriacao >= '2022-01-01 00:00:00.000000' ");
		hql.append(" 	and p.id in ( select c1.grupo.processo.id from ").append(Campo.class.getName()).append(" c1 ");
		hql.append(" 	where c1.grupo.processo.id = p.id and c1.grupo.nome = :dadosImportacao ");
		hql.append(" 	and c1.nome = :tipoProuni and upper(c1.valor) like 'FEDERAL' ) ");
		hql.append(" 	and p.id in ( select c2.grupo.processo.id from ").append(Campo.class.getName()).append(" c2 ");
		hql.append(" 	where c2.grupo.processo.id = p.id and c2.grupo.nome = :dadosDeAnaliseDeProcesso ");
		hql.append(" 	and c2.nome = :entrevistaConcluida and upper(c2.valor) like 'SIM' )");

		params.put("tipoProcessoId", tipoProcessoId);
		params.put("situacao", situacaoList);
		params.put("dadosImportacao", CampoMap.GrupoEnum.DADOS_DE_IMPORTACAO.getNome());
		params.put("tipoProuni", CampoMap.CampoEnum.TIPO_PROUNI.getNome());
		params.put("dadosDeAnaliseDeProcesso", CampoMap.GrupoEnum.DADOS_DE_ANALISE_DE_PROCESSO.getNome());
		params.put("entrevistaConcluida", CampoMap.CampoEnum.ENTREVISTA_CONCLUIDA.getNome());

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}

	public List<Processo> findProcessosFinanciamentoProcessadosComAlteracao(Long tipoProcessoId, List<Long> situacaoList) {
		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select p from ").append(clazz.getName()).append(" p ");
		hql.append(" where ");
		hql.append(" 	p.tipoProcesso.id = :tipoProcessoId ");
		hql.append(" 	and p.situacao.id in ( :situacao ) ");
		hql.append(" 	and p.dataCriacao >= '2022-01-01 00:00:00.000000' ");
		hql.append(" 	and (((select max(d.dataDigitalizacao) from ").append(Documento.class.getName()).append(" d ");
		hql.append(" 	where d.processo.id = p.id ) > ");
		hql.append("  	(select max(pl.data) from ").append(ProcessoLog.class.getName()).append(" pl ");
		hql.append(" 	where pl.processo.id = p.id and pl.acao = :acaoVerificacaoAtualizacaoJob )) or");
		hql.append(" 	((select max(pl2.data) from ").append(ProcessoLog.class.getName()).append(" pl2 ");
		hql.append(" 	where pl2.processo.id = p.id and pl2.acao = :acaoAtualizacaoCampos ) > ");
		hql.append("  	(select max(pl.data) from ").append(ProcessoLog.class.getName()).append(" pl ");
		hql.append(" 	where pl.processo.id = p.id and pl.acao = :acaoVerificacaoAtualizacaoJob ))) ");
		hql.append(" 	and (p.id in ( select cg.processo.id from ").append(Campo.class.getName()).append(" c, ").append(CampoGrupo.class.getName()).append(" cg ");
		hql.append(" 	where cg.processo.id = p.id and cg.id = c.grupo.id and cg.nome = :dadosImportacao ");
		hql.append(" 	and c.nome = :tipoProuni and upper(c.valor) like 'FEDERAL' )) ");

		params.put("tipoProcessoId", tipoProcessoId);
		params.put("situacao", situacaoList);
		params.put("statusDocumento", StatusDocumento.DIGITALIZADO);
		params.put("dadosImportacao", CampoMap.GrupoEnum.DADOS_DE_IMPORTACAO.getNome());
		params.put("tipoProuni", CampoMap.CampoEnum.TIPO_PROUNI.getNome());
		params.put("acaoVerificacaoAtualizacaoJob", AcaoProcesso.VERIFICACAO_ATUALIZACAO_JOB);
		params.put("acaoAtualizacaoCampos", AcaoProcesso.ATUALIZACAO_CAMPOS);

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}

	public List<Processo> findProcessosPendentesComDocsAprovados() {
		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select p from ").append(clazz.getName()).append(" p ");
		hql.append(" where ");
		hql.append(" 	p.tipoProcesso.id not in ( :tipoProcessosFinanciamento ) ");
		hql.append(" 	and p.status = :statusPendente  ");
		hql.append(" 	and p.dataCriacao >= '2022-01-01 00:00:00.000000' ");

		hql.append("	and (p.usaTermo = false ");
		hql.append(" 	and (select count(d.id) from ").append(Documento.class.getName()).append(" d ");
		hql.append(" 	where d.processo.id = p.id and d.obrigatorio is true and d.status in (:statusDocumento)) = 0 ");

		hql.append("	or p.usaTermo = true");
		hql.append(" 	and (select count(d.id) from ").append(Documento.class.getName()).append(" d ");
		hql.append(" 	where d.processo.id = p.id and d.status in ( :DocPendente ) and d.obrigatorio is true) = 0 ");
		hql.append(" 	and (select count(d.id) from ").append(Documento.class.getName()).append(" d ");
		hql.append(" 	where d.processo.id = p.id and d.status in ( :DocDigitalizado ) and d.obrigatorio is true) = 0 ");
		hql.append(" 	and (select count(d.id) from ").append(Documento.class.getName()).append(" d ");
		hql.append(" 	where d.processo.id = p.id and d.status in ( :DocIncluido ) and d.obrigatorio is true) = 1 ");
		hql.append(" 	and (select count(d.id) from ").append(Documento.class.getName()).append(" d ");
		hql.append("    inner join ").append(TipoDocumento.class.getName()).append(" td on td.id = d.tipoDocumento.id ");
		hql.append(" 	where d.processo.id = p.id and d.status in ( :DocIncluido ) and d.obrigatorio is true ");
		hql.append("	and td.codOrigem in (14, 185, 174)) = 1");

		hql.append("	or p.usaTermo = true");
		hql.append(" 	and (select count(d.id) from ").append(Documento.class.getName()).append(" d ");
		hql.append(" 	where d.processo.id = p.id and d.status in ( :DocPendente ) and d.obrigatorio is true) = 1 ");
		hql.append(" 	and (select count(d.id) from ").append(Documento.class.getName()).append(" d ");
		hql.append(" 	where d.processo.id = p.id and d.status in ( :DocDigitalizado ) and d.obrigatorio is true) = 0 ");
		hql.append(" 	and (select count(d.id) from ").append(Documento.class.getName()).append(" d ");
		hql.append(" 	where d.processo.id = p.id and d.status in ( :DocIncluido ) and d.obrigatorio is true) = 0 ");
		hql.append(" 	and (select count(d.id) from ").append(Documento.class.getName()).append(" d ");
		hql.append("    inner join ").append(TipoDocumento.class.getName()).append(" td on td.id = d.tipoDocumento.id ");
		hql.append(" 	where d.processo.id = p.id and d.status in ( :DocPendente ) and d.obrigatorio is true ");
		hql.append("	and td.codOrigem in (14, 185, 174)) = 1)");

		params.put("tipoProcessosFinanciamento", Arrays.asList(TipoProcesso.SIS_PROUNI, TipoProcesso.TE_PROUNI, TipoProcesso.ISENCAO_DISCIPLINAS, TipoProcesso.SIS_FIES, TipoProcesso.TE_FIES));
		params.put("statusPendente", StatusProcesso.PENDENTE);
		params.put("statusDocumento", Arrays.asList(StatusDocumento.PENDENTE, StatusDocumento.DIGITALIZADO, StatusDocumento.INCLUIDO));
		params.put("DocPendente", StatusDocumento.PENDENTE);
		params.put("DocDigitalizado", StatusDocumento.DIGITALIZADO);
		params.put("DocIncluido", StatusDocumento.INCLUIDO);

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}


	public List<Processo> findProcessoFormaDeIngressoVestibular() {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();
		Date dataInicial = DummyUtils.parseDate("23/02/2022");

		hql.append(" select p from ").append(Processo.class.getName()).append(" p ");
		hql.append(", ").append(CampoGrupo.class.getName()).append(" cg ");
		hql.append(", ").append(Campo.class.getName()).append(" c ");
		hql.append(" where p.tipoProcesso.id in (:tiposProcessosIds)");
		hql.append(" and p.dataCriacao >= :dataInicial ");
		hql.append(" and p.id = cg.processo.id ");
		hql.append(" and cg.id = c.grupo.id ");
		hql.append(" and c.nome = :formaDeIngresso ");
		hql.append(" and c.valor = :vestibular ");
		hql.append(" and (p.numCandidato is not null and p.numCandidato <> '') ");
		hql.append(" and p.id in ( ");
		hql.append("    select d.processo.id from ").append(Documento.class.getName()).append(" d ");
		hql.append(", ").append(Documento.class.getName()).append(" d2 ");
		hql.append(" where d.processo.id = d2.processo.id");
		hql.append(" and d.tipoDocumento.id in (:tipoDocumentoIdsPlano)" );
		hql.append(" and d.status = :statusDocumento  ");
		hql.append(" and d2.tipoDocumento.id in (:tipoDocumentoIdsHistorico)" );
		hql.append(" and d2.status = :statusDocumento ) ");
		hql.append(" and ( ");
		hql.append("    select count(p2.id) from ").append(Processo.class.getName()).append(" p2 ");
		hql.append(" where p.numCandidato = p2.numCandidato and p.numInscricao = p2.numInscricao" );
		hql.append(" and p2.tipoProcesso.id = :tipoProcessoId ");
		hql.append(" ) = 0 ");

		Long vestibularLong = TipoProcessoPortal.VESTIBULAR.getClassificacao();
		String vestibularCod = vestibularLong.toString();
		String vestibularCodFormatada = DummyUtils.formatarChaveUnicidadeBaseInterna(vestibularCod);

		params.put("tiposProcessosIds", Arrays.asList(TipoProcesso.VESTIBULAR, TipoProcesso.ENEM));
		params.put("dataInicial", dataInicial);
		params.put("formaDeIngresso", CampoMap.CampoEnum.FORMA_DE_INGRESSO.getNome());
		params.put("vestibular", vestibularCodFormatada);
		params.put("statusDocumento", StatusDocumento.APROVADO);
		params.put("tipoProcessoId", TipoProcesso.ISENCAO_DISCIPLINAS);
		params.put("tipoDocumentoIdsPlano", Arrays.asList(TipoDocumento.VESTIBULAR_PLANO_ENSINO_INSTITUIÇÃO_ORIGEM,
				TipoDocumento.VESTIBULAR_HISTÓRICO_ORIGINAL_INSTITUIÇÃO_ORIGEM_DECLARAÇÃO_MATRÍCULA_ORIGINAL_IES_ORIGEM));
		params.put("tipoDocumentoIdsHistorico", Arrays.asList(TipoDocumento.VESTIBULAR_ENEM_PLANO_ENSINO_INSTITUIÇÃO_ORIGEM,
				TipoDocumento.VESTIBULAR_ENEM_HISTÓRICO_ORIGINAL_INSTITUIÇÃO_ORIGEM_DECLARAÇÃO_MATRÍCULA_ORIGINAL_IES_ORIGEM));

		Query query = createQuery(hql.toString(), params);
		return query.list();
	}

	public List<Long> findProcessoIdByNumCandidatoNumInscricao (String numCandidato, String numInscricao) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select p.id from ").append(Processo.class.getName()).append(" p ");
		hql.append(" where p.numCandidato = :numCandidato ");

		if(numInscricao != null){
			hql.append(" 	and p.numInscricao = :numInscricao ");
			params.put("numInscricao", numInscricao);
		}

		params.put("numCandidato", numCandidato);

		Query query = createQuery(hql, params);
		return query.list();
	}
}
