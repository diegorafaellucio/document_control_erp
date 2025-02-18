package net.wasys.getdoc.domain.repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import net.wasys.getdoc.domain.entity.EmailRecebidoAnexo;
import net.wasys.util.ddd.HibernateRepository;

@Repository
@SuppressWarnings("unchecked")
public class EmailRecebidoAnexoRepository extends HibernateRepository<EmailRecebidoAnexo> {

	public EmailRecebidoAnexoRepository() {
		super(EmailRecebidoAnexo.class);
	}

	public List<Long> findIdsByDataDigitalizacao(Date dataInicio, Date dataFim) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<String, Object>();

		hql.append(" select era.id from ").append(clazz.getName()).append(" era ");
		hql.append(" where era.emailRecebido.data > :dataInicio ");
		hql.append(" and era.emailRecebido.data < :dataFim ");
		hql.append(" order by era.id ");

		params.put("dataInicio", dataInicio);
		params.put("dataFim", dataFim);

		Query query = createQuery(hql, params);

		return query.list();
	}

	public List<EmailRecebidoAnexo> findByIds(List<Long> ids) {

		StringBuilder hql = new StringBuilder();
		hql.append(" from ").append(clazz.getName()).append(" era ");

		hql.append(" where era.id in (-1");
		for (Long id : ids) {
			hql.append(", ").append(id);
		}
		hql.append(") ");

		Query query = createQuery(hql.toString());
		query.setFetchSize(100);

		return query.list();
	}
}
