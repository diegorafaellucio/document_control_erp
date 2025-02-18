package net.wasys.getdoc.domain.repository;

import java.util.*;

import net.wasys.getdoc.domain.entity.Situacao;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import net.wasys.getdoc.domain.entity.Regra;
import net.wasys.getdoc.domain.entity.RegraTipoProcesso;
import net.wasys.getdoc.domain.vo.filtro.RegraFiltro;
import net.wasys.util.ddd.HibernateRepository;

@Repository
@SuppressWarnings("unchecked")
public class RegraRepository extends HibernateRepository<Regra> {

	public RegraRepository() {
		super(Regra.class);
	}

	public boolean existsByFiltro(RegraFiltro filtro) {

		StringBuilder hql = new StringBuilder();

		hql.append(" select 1 ");
		hql.append(" from ").append(clazz.getName()).append(" r ");

		Map<String, Object> params = makeQuery(filtro, hql);

		Query query = createQuery(hql.toString(), params);

		query.setMaxResults(1);

		return query.uniqueResult() != null;
	}

	public int countByFiltro(RegraFiltro filtro) {

		StringBuilder hql = new StringBuilder();

		hql.append(" select count(*) ");
		hql.append(" from ").append(clazz.getName()).append(" r ");

		Map<String, Object> params = makeQuery(filtro, hql);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue();
	}

	public List<Regra> findByFiltro(RegraFiltro filtro, Integer inicio, Integer max) {

		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" r ");
		hql.append(" left outer join fetch r.situacao s ");
		hql.append(" left outer join fetch r.tiposProcessos tps ");

		Map<String, Object> params = makeQuery(filtro, hql);

		String ordem = filtro.getOrdem();
		ordem = ordem != null ? ordem : "r.nome";
		hql.append(" order by ").append(ordem);

		Query query = createQuery(hql.toString(), params);

		if(inicio != null) {
			query.setFirstResult(inicio);
		}

		if(max != null) {
			query.setMaxResults(max);
		}

		List<Regra> list = query.list();
		Set set = new LinkedHashSet(list);
		return new ArrayList<>(set);
	}

	private Map<String, Object> makeQuery(RegraFiltro filtro, StringBuilder hql) {

		Long tipoProcessoId = filtro.getTipoProcessoId();
		Long processoId = filtro.getProcessoId();
		Boolean ativa = filtro.getAtiva();
		Boolean apenasVigentes = filtro.getApenasVigentes();
		Date vigencia = filtro.getVigencia();
		List<Boolean> statusList = filtro.getStatusList();
		Situacao situacao = filtro.getSituacao();

		Map<String, Object> params = new HashMap<String, Object>();

		hql.append(" where 1=1 ");

		if(tipoProcessoId != null) {
			hql.append(" and ( ");
			hql.append("  select count(*) from ").append(RegraTipoProcesso.class.getName()).append(" rtp ");
			hql.append("  where rtp.regra.id = r.id ");
			hql.append("  and rtp.tipoProcesso.id = :tipoProcessoId ");
			hql.append(" ) > 0 ");
			params.put("tipoProcessoId", tipoProcessoId);
		}

		if(ativa != null) {
			hql.append(" and r.ativa is ").append(ativa);
		}

		if(processoId != null) {
			hql.append(" and r.processoId = :processoId");
			params.put("processoId", processoId);
		}

		if(vigencia != null && (apenasVigentes == null || apenasVigentes)) {
			hql.append(" and (r.inicioVigencia is null or r.inicioVigencia <= :inicioVigencia) ");
			hql.append(" and (r.fimVigencia is null or r.fimVigencia >= :fimVigencia) ");
			params.put("inicioVigencia", vigencia);
			params.put("fimVigencia", vigencia);
		}

		if(statusList != null && !statusList.isEmpty()) {
			hql.append(" and r.ativa in (:statusList) ");
			params.put("statusList", statusList);
		}

		if(situacao != null) {
			Long situacaoId = situacao.getId();
			hql.append(" and s.id = :situacaoId ");
			params.put("situacaoId", situacaoId);
		}

		return params;
	}

	public List<Regra> findRegrasAtivas() {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" r ");
		hql.append(" where r.ativa = :ativa");
		params.put("ativa", true);

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}

	public List<Regra> findAtivosDiferenteDeId(Long regraId) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" r ");
		hql.append(" where r.ativa = :ativa ");
		params.put("ativa", true);
		hql.append(" and r.id <> :regraId ");
		params.put("regraId", regraId);
		hql.append(" order by r.nome ");

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}

	public boolean existsByNome(String nome) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select count(*) from ").append(clazz.getName()).append(" r ");
		hql.append(" where r.nome = :nome ");
		params.put("nome", nome);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue() > 0;
	}

	public List<Long> findTiposProcessoByRegraId(Long regraId) {

		List<Object> params = new ArrayList<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select rtp.tipoProcesso.id from ").append(clazz.getName()).append(" r ");
		hql.append(" join ").append(RegraTipoProcesso.class.getName()).append(" rtp ");
		hql.append(" on  rtp.regra.id = r.id ");
		hql.append(" where r.id = ? ");
		params.add(regraId);

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}
}
