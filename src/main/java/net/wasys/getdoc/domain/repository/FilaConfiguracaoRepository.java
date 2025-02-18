package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.FilaConfiguracao;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@SuppressWarnings("unchecked")
public class FilaConfiguracaoRepository extends HibernateRepository<FilaConfiguracao> {

	public FilaConfiguracaoRepository() {
		super(FilaConfiguracao.class);
	}

	public List<FilaConfiguracao> findAll(Integer inicio, Integer max) {

		Query query = createQuery(" from " + clazz.getName() + " order by descricao ");

		query.setFirstResult(inicio);
		query.setMaxResults(max);

		return query.list();
	}

	public int count() {

		Query query = createQuery(" select count(*) from " + clazz.getName());

		return ((Number) query.uniqueResult()).intValue();
	}

	public boolean possuiPadrao() {

		Query query = createQuery(" select count(*) from " + clazz.getName() + " where padrao is true ");

		int result = ((Number) query.uniqueResult()).intValue();

		if (result > 0){
			return true;
		}
		return false;
	}

	public FilaConfiguracao getPadrao() {

		Query query = createQuery(" from " + clazz.getName() + " where padrao is true ");
		query.setMaxResults(1);

		return  (FilaConfiguracao) query.uniqueResult();
	}

}
