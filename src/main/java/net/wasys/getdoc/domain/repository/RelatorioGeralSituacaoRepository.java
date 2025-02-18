package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.vo.CampoDinamicoVO;
import net.wasys.getdoc.domain.vo.filtro.RelatorioGeralFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.HibernateRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.*;

@Repository
@SuppressWarnings("unchecked")
public class RelatorioGeralSituacaoRepository extends HibernateRepository<RelatorioGeralSituacao> {

	public RelatorioGeralSituacaoRepository() {
		super(RelatorioGeralSituacao.class);
	}

	public List<RelatorioGeralSituacao> findByProcessosIds(List<Long> processosIds2) {

		StringBuilder hql = new StringBuilder();
		hql.append(getStartQuery());

		hql.append(" where processoId in (-1");
		for (Long processoId : processosIds2) {
			hql.append(", ").append(processoId);
		}
		hql.append(" ) ");
		hql.append(" order by id ");

		Query query = createQuery(hql.toString());

		return query.list();
	}

	public List<RelatorioGeralSituacao> findByFiltro(RelatorioGeralFiltro filtro, Integer inicio, Integer max) {

		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" r ");

		List<Object> params = makeQuery(filtro, hql);

		hql.append(" order by r.id");

		Query query = createQuery(hql.toString(), params);

		if(inicio != null) {
			query.setFirstResult(inicio);
		}

		if(max != null) {
			query.setMaxResults(max);
		}

		return query.list();
	}

	public int countByFiltro(RelatorioGeralFiltro filtro) {

		StringBuilder hql = new StringBuilder();

		hql.append(" select count(*) from ").append(clazz.getName()).append(" r ");

		List<Object> params = makeQuery(filtro, hql);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue();
	}

	private List<Object> makeQuery(RelatorioGeralFiltro filtro, StringBuilder hql) {

		Date dataInicio = filtro.getDataInicio();
		dataInicio = DummyUtils.truncateInicioDia(dataInicio);
		Date dataFim = filtro.getDataFim();
		dataFim = DummyUtils.truncateFinalDia(dataFim);

		List<TipoProcesso> tiposProcessoList = filtro.getTiposProcessoList();
		List<StatusProcesso> statusList = filtro.getStatusList();

		List<Object> params = new ArrayList<Object>();

		hql.append(" where r.data between ? and ? ");
		params.add(dataInicio);
		params.add(dataFim);

		if (tiposProcessoList != null && !tiposProcessoList.isEmpty()) {
			hql.append(" and r.situacao.tipoProcesso.id in ( -1 ");
			for (TipoProcesso tipoProcesso : tiposProcessoList) {
				hql.append(", ?");
				params.add(tipoProcesso.getId());
			}
			hql.append(" )");
		}

		if(statusList != null && !statusList.isEmpty()) {
			hql.append(" and r.situacao.status in ( '-1' ");
			for (StatusProcesso status : statusList) {
				hql.append(", ?");
				params.add(status);
			}
			hql.append(" ) ");
		}

		return params;
	}

	public List<Long> findIdsByFiltro(RelatorioGeralFiltro filtro) {

		Date dataInicio = filtro.getDataInicio();
		dataInicio = net.wasys.getdoc.mb.utils.DateUtils.getFirstTimeOfDay(dataInicio);
		Date dataFim = filtro.getDataFim();
		dataFim = net.wasys.getdoc.mb.utils.DateUtils.getLastTimeOfDay(dataFim);
		List<TipoProcesso> tipoProcessoList = filtro.getTiposProcessoList();
		List<Situacao> situacoes = filtro.getSituacoes();
		List<Long> situacoesIds = filtro.getSituacoesIds();
		List<Long> tiposProcessoIds = filtro.getTiposProcessoIds();
		List<CampoDinamicoVO> camposFiltro = filtro.getCamposFiltro();

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select id from ").append(clazz.getName()).append(" rg where 1=1 ");

		if(dataFim != null && dataInicio != null) {
			hql.append(" and rg.data between ? and ? ");
			params.add(dataInicio);
			params.add(dataFim);
		}

		if(tipoProcessoList!= null && !tipoProcessoList.isEmpty()){
			hql.append(" and rg.relatorioGeral.tipoProcesso.id in( -1 ");
			for (TipoProcesso tipoProcesso : tipoProcessoList) {
				hql.append(", ?");
				params.add(tipoProcesso.getId());
			}
			hql.append(" ) ");
		}

		if(tiposProcessoIds != null && !tiposProcessoIds.isEmpty()){
			hql.append(" and rg.relatorioGeral.tipoProcesso.id in( -1 ");
			for (Long tipoProcessoId : tiposProcessoIds) {
				hql.append(", ?");
				params.add(tipoProcessoId);
			}
			hql.append(" ) ");
		}

		if(situacoes != null && !situacoes.isEmpty()) {
			hql.append(" and rg.situacao.id in ( -1 ");
			for (Situacao situacaoAdd : situacoes) {
				hql.append(", ?");
				params.add(situacaoAdd.getId());
			}
			hql.append(" )");
		}

		if(situacoesIds != null && !situacoesIds.isEmpty()) {
			hql.append(" and rg.situacao.id in ( -1 ");
			for (Long situacaoId : situacoesIds) {
				hql.append(", ?");
				params.add(situacaoId);
			}
			hql.append(" )");
		}

		if(camposFiltro != null && !camposFiltro.isEmpty()) {
			for (CampoDinamicoVO cd : camposFiltro) {
				String nomeGrupo = cd.getNomeGrupo();
				String nomeCampo = cd.getNomeCampo();

				List<String> chavesUnicidade = cd.getChavesUnicidade();
				String valorCampo = cd.getValorCampo();
				if (StringUtils.isBlank(valorCampo) && CollectionUtils.isEmpty(chavesUnicidade)) {
					continue;
				}

				hql.append(" and rg.relatorioGeral.processoId in ( ");
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

		hql.append(" order by rg.data ");

		Query query = createQuery(hql.toString(), params);
		query.setFetchSize(100);
		return query.list();
	}

	public List<RelatorioGeralSituacao> findByIds(List<Long> ids) {

		StringBuilder hql = new StringBuilder();
		hql.append(getStartQuery()).append(" rgs ");

		hql.append(" where rgs.id in (-1");
		for (Long id : ids) {
			hql.append(", ").append(id);
		}
		hql.append(") ");

		hql.append(" order by rgs.processoId, rgs.data ");

		Query query = createQuery(hql.toString());
		query.setFetchSize(100);

		return query.list();
	}

	public List<RelatorioGeralSituacao> findByRelatorioGeralIds(List<Long> relatorioGeralIds, RelatorioGeralFiltro filtro) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<>();

		Date dataInicio = filtro.getDataInicio();
		Date dataFim = filtro.getDataFim();

		dataInicio = dataInicio != null ? DummyUtils.truncateInicioDia(dataInicio) : null;
		dataFim = dataFim != null ? DummyUtils.truncateFinalDia(dataFim) : null;

		hql.append(getStartQuery()).append(" rgs ");
		hql.append(" 	left outer join fetch rgs.relatorioGeral rg ");
		hql.append(" 	left outer join fetch rg.tipoProcesso tp ");
		hql.append(" 	left outer join fetch rgs.usuarioInicio u ");
		hql.append(" 	left outer join fetch rgs.situacao s ");

		hql.append(" where rgs.relatorioGeral.id in (-1");
		for (Long id : relatorioGeralIds) {
			hql.append(", ").append(id);
		}
		hql.append(") ");

		if(dataInicio != null && dataFim != null){
			hql.append(" and rgs.data between :dataInicio and :dataFim ");
		}

		hql.append(" order by rgs.processoId, rgs.data ");

		params.put("dataInicio", dataInicio);
		params.put("dataFim", dataFim);

		Query query = createQuery(hql, params);
		query.setFetchSize(100);

		return query.list();
	}

	public List<RelatorioGeralSituacao> find(Area area, Date dataInicio, Date dataFim) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<>();

		hql.append(getStartQuery());
		hql.append(" where dataFinalizacao is not null ");

		if(area != null) {
			hql.append(" and area.id = :areaId ");
			Long areaId = area.getId();
			params.put("areaId", areaId);
		}

		if(dataInicio != null) {
			hql.append(" and data >= :dataInicio ");
			dataInicio = DummyUtils.truncateInicioDia(dataInicio);
			params.put("dataInicio", dataInicio);
		}

		if(dataFim != null) {
			hql.append(" and data <= :dataFim ");
			dataFim = DummyUtils.truncateFinalDia(dataFim);
			params.put("dataFim", dataFim);
		}

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}

	public List<Processo> findProcessos(Area area, int mes, int ano, Date dataInicio, Date dataFim) {
		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<>();

		hql.append(" select p from net.wasys.getdoc.domain.entity.Processo p  ");
		hql.append(" where p.id in (select s.processoId from net.wasys.getdoc.domain.entity.RelatorioGeralSolicitacao s where s.dataFinalizacao is not null ");

		if(area != null) {
			hql.append(" and s.area.id = :areaId ");
			Long areaId = area.getId();
			params.put("areaId", areaId);
		}

		if(dataInicio != null) {
			hql.append(" and s.dataSolicitacao >= :dataInicio and date_part('month', s.dataSolicitacao) = :mes and date_part('year',s.dataSolicitacao) = :ano ");
			dataInicio = DummyUtils.truncateInicioDia(dataInicio);
			params.put("dataInicio", dataInicio);
			params.put("mes", mes);
			params.put("ano", ano);
		}

		if(dataFim != null) {
			hql.append(" and s.dataSolicitacao <= :dataFim ");
			dataFim = DummyUtils.truncateFinalDia(dataFim);
			params.put("dataFim", dataFim);
		}
		hql.append(" ) ");

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}

	public Map<String, BigDecimal> findTempoMedioSituacao(Date dataInicio, Date dataFim) {

		StringBuilder hql = new StringBuilder();
		Map<String, BigDecimal> result = new HashMap<>();
		Map<String, Object> params = new HashMap<>();

		hql.append(" select rgs.situacao.nome, avg(rgs.tempo) ");
		hql.append(" from ").append(clazz.getName()).append(" rgs ");
		hql.append(" where rgs.dataFim between :dataInicio and :dataFim ");
		hql.append(" group by rgs.situacao.nome ");
		params.put("dataInicio", dataInicio);
		params.put("dataFim", dataFim);

		Query query = createQuery(hql.toString(), params);

		List<Object[]> list = query.list();

		for (Object[] objs : list) {
			String situacaoNome = (String) objs[0];
			Double tempoMedioDouble = (Double) objs[1];
			BigDecimal tempoMedio = new BigDecimal(tempoMedioDouble);
			result.put(situacaoNome, tempoMedio);
		}

		return result;
	}

	public Map<StatusProcesso, BigDecimal> findTempoMedioStatusProcesso(Date dataInicio, Date dataFim) {

		StringBuilder hql = new StringBuilder();
		Map<StatusProcesso, BigDecimal> result = new HashMap<>();
		Map<String, Object> params = new HashMap<>();

		hql.append(" select tbl.status, avg(tbl.tempo) from ( ");
		hql.append(" 	select ");
		hql.append(" 		s.status, sum(rgs.tempo) as tempo ");
		hql.append(" 	from ");
		hql.append(" 		relatorio_geral_situacao rgs, situacao s ");
		hql.append(" 	where ");
		hql.append(" 		rgs.situacao_id = s.id ");
		hql.append(" 		and s.status in ('EM_ANALISE') ");
		hql.append(" 		and rgs.data_fim between :dataInicio and :dataFim ");
		hql.append(" 	group by ");
		hql.append(" 		s.status, rgs.processo_id ");
		hql.append(" ) as tbl ");
		hql.append(" group by tbl.status ");

		params.put("dataInicio", dataInicio);
		params.put("dataFim", dataFim);
		Query query = createSQLQuery(hql.toString(), params);

		List<Object[]> list = query.list();

		for (Object[] objs : list) {
			String statusProcessoStr = (String) objs[0];
			StatusProcesso statusProcesso = StatusProcesso.valueOf(statusProcessoStr);
			BigDecimal tempoMedio = (BigDecimal) objs[1];
			result.put(statusProcesso, tempoMedio);
		}

		return result;
	}

	public BigDecimal getTempoMedioSituacaoTotal(Date dataInicio, Date dataFim) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<>();

		hql.append(" select avg(rgs.tempo) ");
		hql.append(" from ").append(clazz.getName()).append(" rgs ");
		hql.append(" where rgs.dataFim between :dataInicio and :dataFim ");
		params.put("dataInicio", dataInicio);
		params.put("dataFim", dataFim);

		Query query = createQuery(hql.toString(), params);

		Double d = (Double) query.uniqueResult();
		BigDecimal result = new BigDecimal(0);

		if (d != null){
			result = new BigDecimal(d);
		}

		return result;
	}
}
