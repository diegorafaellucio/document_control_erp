package net.wasys.getdoc.domain.repository;

import java.util.*;

import net.wasys.getdoc.domain.entity.TipoProcesso;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import net.wasys.getdoc.domain.entity.EmailRecebido;
import net.wasys.getdoc.domain.entity.ProcessoLogAnexo;
import net.wasys.util.ddd.HibernateRepository;

@Repository
@SuppressWarnings("unchecked")
public class EmailRecebidoRepository extends HibernateRepository<EmailRecebido> {

	public EmailRecebidoRepository() {
		super(EmailRecebido.class);
	}

	public boolean existsByMessageUid(String messageUid) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select count(*) from ").append(clazz.getName());
		hql.append(" where messageUid = :messageUid ");
		params.put("messageUid", messageUid);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).longValue() > 0;
	}

	public EmailRecebido getByMessageUid(String messageUid) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" where messageUid = :messageUid ");
		params.put("messageUid", messageUid);

		Query query = createQuery(hql.toString(), params);

		return (EmailRecebido) query.uniqueResult();
	}

	public int countNaoLidos(Long processoId) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select count(*) from ").append(clazz.getName()).append(" where dataLeitura is null and processo.id = ? ");
		params.add(processoId);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue();
	}

	public Map<Long, Boolean> getNaoLidos(List<Long> processosIds) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select er.processo.id, (count(*) > 0) ");
		hql.append(" from ").append(clazz.getName()).append(" er ");
		hql.append(" where er.dataLeitura is null ");

		hql.append(" and er.processo.id in ( -1 ");
		for (Long processoId : processosIds) {
			hql.append(", ?");
			params.add(processoId);
		}
		hql.append(" ) ");

		hql.append(" group by er.processo.id ");

		Query query = createQuery(hql.toString(), params);

		List<Object[]> list = query.list();

		Map<Long, Boolean> map = new HashMap<>();
		for (Object[] objects : list) {

			Long processoId = (Long) objects[0];
			Boolean temNaoLido = (Boolean) objects[1];

			map.put(processoId, temNaoLido);
		}

		return map;
	}

	public List<Long> findIdsByDataDigitalizacao(Date dataInicio, Date dataFim) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<String, Object>();

		hql.append(" select er.id from ").append(clazz.getName()).append(" er ");
		hql.append(" where er.data > :dataInicio ");
		hql.append(" and er.data < :dataFim ");
		hql.append(" order by er.id ");

		params.put("dataInicio", dataInicio);
		params.put("dataFim", dataFim);

		Query query = createQuery(hql, params);

		return query.list();
	}

	public List<ProcessoLogAnexo> findByIds(List<Long> ids) {

		StringBuilder hql = new StringBuilder();
		hql.append(" from ").append(clazz.getName()).append(" er ");

		hql.append(" where er.id in (-1");
		for (Long id : ids) {
			hql.append(", ").append(id);
		}
		hql.append(") ");

		Query query = createQuery(hql.toString());
		query.setFetchSize(100);

		return query.list();
	}

	public List<EmailRecebido> findByConversationId(String conversationId){
		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" where conversation_id = ? ");
		params.add(conversationId);

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}


}
