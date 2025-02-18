package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.FaseEtapa;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.vo.filtro.RelatorioGeralFiltro;
import net.wasys.getdoc.mb.utils.DateUtils;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@SuppressWarnings("unchecked")
public class RelatorioGeralEtapaRepository extends HibernateRepository<RelatorioGeralEtapa> {

	public RelatorioGeralEtapaRepository() {
		super(RelatorioGeralEtapa.class);
	}

	public List<RelatorioGeralEtapa> findByProcessosIds(List<Long> processosIds2) {

		StringBuilder hql = new StringBuilder();
		hql.append(getStartQuery());

		hql.append(" where relatorioGeral.processoId in (-1");
		for (Long processoId : processosIds2) {
			hql.append(", ").append(processoId);
		}
		hql.append(" ) ");
		hql.append(" order by id ");

		Query query = createQuery(hql.toString());

		return query.list();
	}

	public List<RelatorioGeralEtapa> findByFiltro(RelatorioGeralFiltro filtro, Integer inicio, Integer max) {

		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" rge ");

		Map<String, Object> params = makeQuery(filtro, hql);

		hql.append(" order by rge.id");

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

		hql.append(" select count(*) from ").append(clazz.getName()).append(" rge ");

		Map<String, Object> params = makeQuery(filtro, hql);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue();
	}

	private Map<String, Object> makeQuery(RelatorioGeralFiltro filtro, StringBuilder hql) {

		Date dataInicio = filtro.getDataInicio();
		dataInicio = dataInicio != null ? DateUtils.getFirstTimeOfDay(dataInicio) : null;
		Date dataFim = filtro.getDataFim();
		dataFim = dataFim != null ? DateUtils.getLastTimeOfDay(dataFim) : null;

		List<TipoProcesso> tiposProcessoList = filtro.getTiposProcessoList();
		List<StatusProcesso> statusList = filtro.getStatusList();
		Etapa etapa = filtro.getEtapa();
		Long relatorioGeralId = filtro.getRelatorioGeralId();
		Boolean dataFimVazio = filtro.getDataFimEtapaVazio();

		Map<String, Object> params = new LinkedHashMap<>();

		hql.append(" where 1=1 ");

		if(dataInicio != null && dataFim != null && (dataFimVazio == null || !dataFimVazio)) {
			hql.append(" and rge.dataInicio between :dataInicio and :dataFim ");
		}

		if(dataInicio != null && dataFim != null && (dataFimVazio != null && dataFimVazio)) {
			hql.append(" and rge.dataFim between :dataInicio and :dataFim ");
		}

		if(dataFimVazio != null && !dataFimVazio) {
			hql.append(" and rge.dataFim is not null");
		}

		RelatorioGeralFiltro.ConsiderarData considerarData = filtro.getConsiderarData();
		if(considerarData != null) {
			if(RelatorioGeralFiltro.ConsiderarData.CRIACAO.equals(considerarData)) {
				hql.append(" and rge.relatorioGeral.dataCriacao between :dataInicio and :dataFim ");
			}
			else if(RelatorioGeralFiltro.ConsiderarData.ENVIO_ANALISE.equals(considerarData)) {
				hql.append(" and rge.etapa.fase = :emConferencia ");
				params.put("emConferencia", FaseEtapa.EM_CONFERENCIA);
			}
			else if(RelatorioGeralFiltro.ConsiderarData.ATUALIZACAO.equals(considerarData)) {
				hql.append(" and rge.etapa.fase = :documentacaoAprovada ");
				params.put("documentacaoAprovada", FaseEtapa.DOCUMENTACAO_APROVADA);
			}
		}

		params.put("dataInicio", dataInicio);
		params.put("dataFim", dataFim);

		if (tiposProcessoList != null && !tiposProcessoList.isEmpty()) {
			hql.append(" and rge.etapa.tipoProcesso in (:tiposProcessoList) ");
			params.put("tiposProcessoList", tiposProcessoList);
		}

		if(statusList != null && !statusList.isEmpty()) {
			hql.append(" and rge.etapa.status in (:statusList) ");
			params.put("statusList", statusList);
		}

		if(relatorioGeralId != null) {
			hql.append(" and rge.relatorioGeral.id = :relatorioGeralId");
			params.put("relatorioGeralId", relatorioGeralId);
		}

		if(etapa != null) {
			hql.append(" and rge.etapa = :etapa");
			params.put("etapa", etapa);
		}

		if(dataFimVazio != null && dataFimVazio) {
			hql.append(" and rge.dataFim is not null");
		}

		return params;
	}

	public List<Long> findIdsByFiltro(RelatorioGeralFiltro filtro) {

		StringBuilder hql = new StringBuilder();
		hql.append(" select rge.id from ").append(clazz.getName()).append(" rge ");
		Map<String, Object> params = makeQuery(filtro, hql);
		hql.append(" order by rge.dataInicio ");

		Query query = createQuery(hql.toString(), params);
		query.setFetchSize(100);
		return query.list();
	}

	public List<RelatorioGeralEtapa> findByIds(List<Long> ids) {

		StringBuilder hql = new StringBuilder();
		hql.append(" from ").append(clazz.getName()).append(" rge ");
		hql.append(" left outer join fetch rge.situacaoDe sd ");
		hql.append(" left outer join fetch rge.situacaoPara sp ");
		hql.append(" left outer join fetch rge.relatorioGeral rg ");
		hql.append(" left outer join fetch rge.relatorioGeral.autor au ");
		hql.append(" left outer join fetch rge.relatorioGeral.analista an ");
		hql.append(" left outer join fetch rge.etapa et ");

		hql.append(" where rge.id in (-1");
		for (Long id : ids) {
			hql.append(", ").append(id);
		}
		hql.append(") ");

		Query query = createQuery(hql.toString());
		query.setFetchSize(500);

		return query.list();
	}

	public List<Long> findEtapasPendetesDeProcessamento() {
		StringBuilder hql = new StringBuilder();

		hql.append("select rg.id from ").append(RelatorioGeral.class.getName()).append(" rg ");
		hql.append(" where ( select count(*) from ").append(clazz.getName()).append(" rge ");
		hql.append("		  		where rge.relatorioGeral.id = rg.id) = 0 ");

		Query query = createQuery(hql);

		return query.list();
	}
}
