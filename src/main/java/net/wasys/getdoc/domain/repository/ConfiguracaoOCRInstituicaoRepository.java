package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.ConfiguracaoOCR;
import net.wasys.getdoc.domain.entity.ConfiguracaoOCRInstituicao;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@SuppressWarnings("unchecked")
public class ConfiguracaoOCRInstituicaoRepository extends HibernateRepository<ConfiguracaoOCRInstituicao> {

	public ConfiguracaoOCRInstituicaoRepository() {
		super(ConfiguracaoOCRInstituicao.class);
	}

	public List<ConfiguracaoOCRInstituicao> findAll() {

		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<Object>();

		hql.append(" from ").append(clazz.getName()).append(" co ");
		hql.append(" order by co.nome ");

		Query query = createQuery(hql, params);

		return query.list();
	}

	public boolean existsByNome(String nome) {

		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<Object>();

		hql.append(" select count(*) from ").append(clazz.getName()).append(" co ");
		hql.append(" where co.nome = ? ");
		params.add(nome);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue() > 0;
	}

	public int countAll() {
		Query query = createQuery(" select count(*) from " + clazz.getName());
		return query.list().size();
	}

	public int countByConfiguracaoOCR(Long configuracaoOCRId) {

		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<Object>();

		hql.append(" select count(*) from ").append(clazz.getName()).append(" cmo where cmo.configuracaoOCR.id = ? ");
		params.add(configuracaoOCRId);

		Query query = createQuery(hql.toString(), params);
		return ((Number)query.uniqueResult()).intValue();
	}

    public List<ConfiguracaoOCRInstituicao> findByConfiguracaoOCR(Long configuracaoOCRId) {
		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<Object>();

		hql.append(" from ").append(clazz.getName()).append(" cmo where cmo.configuracaoOCR.id = ? ");
		params.add(configuracaoOCRId);

		Query query = createQuery(hql.toString(), params);
		return query.list();
    }
}
