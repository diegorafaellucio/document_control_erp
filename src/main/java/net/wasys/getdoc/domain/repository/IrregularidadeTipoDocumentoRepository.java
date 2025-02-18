package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.Irregularidade;
import net.wasys.getdoc.domain.entity.IrregularidadeTipoDocumento;
import net.wasys.getdoc.domain.entity.TipoDocumento;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
@SuppressWarnings("unchecked")
public class IrregularidadeTipoDocumentoRepository extends HibernateRepository<IrregularidadeTipoDocumento> {

	public IrregularidadeTipoDocumentoRepository() {
		super(IrregularidadeTipoDocumento.class);
	}

	public List<IrregularidadeTipoDocumento> findAll(Integer inicio, Integer max) {

		Query query = createQuery(" from " + clazz.getName() + " order by nome ");

		query.setFirstResult(inicio);
		query.setMaxResults(max);

		return query.list();
	}

	public int count() {

		Query query = createQuery(" select count(*) from " + clazz.getName());

		return ((Number) query.uniqueResult()).intValue();
	}

	public List<IrregularidadeTipoDocumento> findByTipoDocumentoId(Long tipoDocumentoId) {

		Map<String, Object> params = new LinkedHashMap<>();

		StringBuilder hql = new StringBuilder();

		hql.append(startQuery()).append(" itp ");
		hql.append(" where itp = :tipoDocumentoId ");

		params.put("tipoDocumentoId", tipoDocumentoId);

		Query query = createQuery(hql, params);
		return query.list();
	}

	public List<Irregularidade> findIrregularidadesByTipoDocumentoId(Long tipoDocumentoId) {

		Map<String, Object> params = new LinkedHashMap<>();

		StringBuilder hql = new StringBuilder();

		hql.append("select itp.irregularidade from ").append(clazz.getName()).append(" itp ");
		hql.append(" where itp.tipoDocumento.id = :tipoDocumentoId ");

		params.put("tipoDocumentoId", tipoDocumentoId);

		Query query = createQuery(hql, params);
		return query.list();
	}

	public List<TipoDocumento> findTipoDocumentoByIrregularidades(Irregularidade irregularidade) {

		Map<String, Object> params = new LinkedHashMap<>();

		StringBuilder hql = new StringBuilder();

		hql.append("select itp.tipoDocumento from ").append(clazz.getName()).append(" itp ");
		hql.append(" where itp.irregularidade.id = :irregularidade ");

		params.put("irregularidade", irregularidade);

		Query query = createQuery(hql, params);
		return query.list();
	}

	public List<IrregularidadeTipoDocumento> findByIrregularidades(Irregularidade irregularidade) {

		Map<String, Object> params = new LinkedHashMap<>();

		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" itp ");
		hql.append(" where itp.irregularidade.id = :irregularidade ");

		params.put("irregularidade", irregularidade);

		Query query = createQuery(hql, params);
		return query.list();
	}

	public IrregularidadeTipoDocumento findIrregularidadesByTipoDocumentoIdAndIrregularidade(Long tipoDocumentoId, Irregularidade irregularidade) {

		Map<String, Object> params = new LinkedHashMap<>();

		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" itp ");
		hql.append(" where itp.tipoDocumento.id = :tipoDocumentoId ");
		hql.append(" and itp.irregularidade = :irregularidade ");

		params.put("tipoDocumentoId", tipoDocumentoId);
		params.put("irregularidade", irregularidade);

		Query query = createQuery(hql, params);
		return (IrregularidadeTipoDocumento) query.uniqueResult();
	}
}
