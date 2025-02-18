package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.GrupoModeloDocumento;
import net.wasys.getdoc.domain.entity.ModeloDocumento;
import net.wasys.getdoc.domain.entity.ModeloDocumentoGrupoModeloDocumento;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@SuppressWarnings("unchecked")
public class GrupoModeloDocumentoRepository extends HibernateRepository<GrupoModeloDocumento> {

	public GrupoModeloDocumentoRepository() {
		super(GrupoModeloDocumento.class);
	}

	public List<GrupoModeloDocumento> findAll() {

		Query query = createQuery(" from " + clazz.getName() + " order by descricao ");

		return query.list();
	}

	public int count() {

		Query query = createQuery(" select count(*) from " + clazz.getName());

		return ((Number) query.uniqueResult()).intValue();
	}

	public List<ModeloDocumento> findModelosDocumento(Long modeloDocumentoGrupoId) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select dgmd.modeloDocumento from ").append(ModeloDocumentoGrupoModeloDocumento.class.getName()).append(" dgmd ");
		hql.append(" where dgmd.grupoModeloDocumento.id = :modeloDocumentoGrupoId ");
		hql.append(" order by dgmd.modeloDocumento.descricao ");

		params.put("modeloDocumentoGrupoId", modeloDocumentoGrupoId);

		Query query = createQuery(hql, params);

		return query.list();
	}

	public void saveOrUpdateModeloDocumento(ModeloDocumentoGrupoModeloDocumento esteiraModelo) {
		try {
			Session session = getSession();
			session.saveOrUpdate(esteiraModelo);
			session.flush();
		}
		catch (HibernateException e) {
			handleException(e, esteiraModelo);
		}
	}

	public ModeloDocumentoGrupoModeloDocumento findByModeloDocumentoGrupoAndModeloDocumento(GrupoModeloDocumento grupoModeloDocumento, ModeloDocumento modeloDocumento) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select mdgd from ").append(ModeloDocumentoGrupoModeloDocumento.class.getName()).append(" mdgd ");
		hql.append(" where mdgd.grupoModeloDocumento = :modeloDocumentoGrupo ");
		hql.append(" and mdgd.modeloDocumento = :modeloDocumento ");

		params.put("modeloDocumentoGrupo", grupoModeloDocumento);
		params.put("modeloDocumento", modeloDocumento);

		Query query = createQuery(hql, params);

		return (ModeloDocumentoGrupoModeloDocumento) query.uniqueResult();
	}

	public void deleteModeloDocumentoById(Long modeloDocumentoGrupoId, Long modeloId) {
		Query query = createQuery("delete " + ModeloDocumentoGrupoModeloDocumento.class.getName() + " mdgd where mdgd.grupoModeloDocumento.id = :modeloDocumentoGrupoId and mdgd.modeloDocumento.id = :modeloId");
		query.setParameter("modeloDocumentoGrupoId", modeloDocumentoGrupoId);
		query.setParameter("modeloId", modeloId);
		query.executeUpdate();
	}

}
