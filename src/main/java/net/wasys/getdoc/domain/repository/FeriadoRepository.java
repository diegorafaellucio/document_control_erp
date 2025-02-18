package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.Feriado;
import net.wasys.getdoc.domain.entity.FeriadoParalizacao;
import net.wasys.getdoc.domain.entity.FeriadoSituacao;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@SuppressWarnings("unchecked")
public class FeriadoRepository extends HibernateRepository<Feriado> {

	private final static long TIMEOUT_CACHE = (1000 * 60 * 2);//2 minutos

	public FeriadoRepository() {
		super(Feriado.class);
	}

	public List<Feriado> findAll(Boolean paralizacao, Integer inicio, Integer max) {

		Query query = createQuery(" from " + clazz.getName() + " where paralizacao = " + paralizacao + " order by data desc ");

		query.setFirstResult(inicio);
		query.setMaxResults(max);

		return query.list();
	}

	public int count(Boolean paralizacao) {

		Query query = createQuery(" select count(*) from " + clazz.getName()+ " where paralizacao = " + paralizacao);

		return ((Number) query.uniqueResult()).intValue();
	}

	public List<Date> findAllDatas() {

		Query query = createQuery(" select data from " + clazz.getName() + " where paralizacao is false order by data ");

		return query.list();
	}

	public Date getUltimaDataAtualizacao() {

		StringBuilder hql = new StringBuilder();

		hql.append(" select max(f.dataAtualizacao) from ").append(clazz.getName()).append(" f ");

		Query query = createQuery(hql.toString());

		return (Date) query.uniqueResult();
	}

	public Feriado getByGeralId(Long geralId) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" where geralId = ? ");
		params.add(geralId);

		Query query = createQuery(hql.toString(), params);

		return (Feriado) query.uniqueResult();
	}

	public Feriado getByData(Date data) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" where data = ? ");
		params.add(data);

		Query query = createQuery(hql.toString(), params);

		return (Feriado) query.uniqueResult();
	}

    public List<Date> findBySituacao(Long situacaoId) {
		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append("select fp.data from ");
		hql.append(FeriadoParalizacao.class.getName()).append(" fp, ");
		hql.append(FeriadoSituacao.class.getName()).append(" fs ");
		hql.append(" where fs.situacao.id = :situacaoId ");
		hql.append(" and fs.feriado.id = fp.feriado.id ");
		hql.append(" order by data");
		params.put("situacaoId", situacaoId);

		Query query = createQuery(hql.toString(), params);

		List<Date> list = listCache(params, query, TIMEOUT_CACHE);

		return list;
    }
}
