package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.CategoriaGrupoModeloDocumento;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@SuppressWarnings("unchecked")
public class CategoriaGrupoDocumentoRepository extends HibernateRepository<CategoriaGrupoModeloDocumento> {

	public CategoriaGrupoDocumentoRepository() {
		super(CategoriaGrupoModeloDocumento.class);
	}

	public List<CategoriaGrupoModeloDocumento> findAll() {

		Query query = createQuery(" from " + clazz.getName() + " order by descricao desc ");

		return query.list();
	}

	public int count() {

		Query query = createQuery(" select count(*) from " + clazz.getName());


		return query.list().size();
	}
}
