package net.wasys.getdoc.domain.repository;

import java.util.*;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import net.wasys.getdoc.domain.entity.RegraTipoProcesso;
import net.wasys.getdoc.domain.entity.TipoCampoGrupo;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.enumeration.CampoMap;
import net.wasys.util.ddd.HibernateRepository;

@Repository
@SuppressWarnings("unchecked")
public class TipoCampoGrupoRepository extends HibernateRepository<TipoCampoGrupo> {

	public TipoCampoGrupoRepository() {
		super(TipoCampoGrupo.class);
	}

	public List<TipoCampoGrupo> findByTipoProcesso(Long tipoProcessoId) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" g where g.tipoProcesso.id = ? ");
		params.add(tipoProcessoId);

		hql.append(" order by g.ordem ");

		Query query = createQuery(hql, params);

		return query.list();
	}

	public List<TipoCampoGrupo> findByTipoProcessoAndNaoAdicionado(Long tipoProcessoId) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" g");
		hql.append(" where g.tipoProcesso.id = :tipoProcessoId ");
		hql.append(" and g.criacaoProcesso = false ");
		params.put("tipoProcessoId", tipoProcessoId);

		hql.append(" order by g.ordem ");

		Query query = createQuery(hql, params);

		return query.list();
	}

	public TipoCampoGrupo getAnterior(Long tipoProcessoId, Integer ordem) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" where tipoProcesso.id = ? ");
		params.add(tipoProcessoId);

		hql.append(" and ordem < ? ");
		params.add(ordem);

		hql.append(" order by ordem desc ");

		Query query = createQuery(hql, params);

		query.setMaxResults(1);

		return (TipoCampoGrupo) query.uniqueResult();
	}

	public TipoCampoGrupo getProximo(Long tipoProcessoId, Integer ordem) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" where tipoProcesso.id = ? ");
		params.add(tipoProcessoId);

		hql.append(" and ordem > ? ");
		params.add(ordem);

		hql.append(" order by ordem asc ");

		Query query = createQuery(hql, params);

		query.setMaxResults(1);

		return (TipoCampoGrupo) query.uniqueResult();
	}

	public Set<String> findNomesByRegraId(Long regraId) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select g.nome ");
		hql.append(getStartQuery()).append(" g ");
		hql.append(" join " + TipoProcesso.class.getName() + " tp on tp.id = g.tipoProcesso.id ");
		hql.append(" join " + RegraTipoProcesso.class.getName() + " rtp on rtp.tipoProcesso.id = tp.id ");
		hql.append(" where rtp.regra.id = ? ");
		params.add(regraId);

		hql.append(" order by g.nome ");

		Query query = createQuery(hql, params);

		List<String> list = query.list();

		return new LinkedHashSet<>(list);
	}

	public TipoCampoGrupo getByTipoProcessoAndGrupoNome(Long tipoProcessoId, String nomeGrupo) {

		List<Object> params = new ArrayList<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" g where g.tipoProcesso.id = ? and g.nome like ? ");
		params.add(tipoProcessoId);
		params.add(nomeGrupo);

		Query query = createQuery(hql, params);

		return (TipoCampoGrupo) query.uniqueResult();
	}

	public List<TipoCampoGrupo> findGruposSuperiores(TipoCampoGrupo tipoCampoGrupo) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" g ");
		hql.append(" where 1=1 ");
		hql.append(" and g.tipoProcesso.id = :tipoProcessoId ");
		hql.append(" and g.subgrupo = :tipoCampoGrupo ");

		params.put("tipoProcessoId", tipoCampoGrupo.getTipoProcesso().getId());
		params.put("tipoCampoGrupo", tipoCampoGrupo);

		Query query = createQuery(hql, params);

		return query.list();
	}
}
