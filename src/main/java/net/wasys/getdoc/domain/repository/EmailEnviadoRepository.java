package net.wasys.getdoc.domain.repository;

import java.util.*;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import net.wasys.getdoc.domain.entity.EmailEnviado;
import net.wasys.getdoc.domain.entity.EmailRecebido;
import net.wasys.getdoc.domain.entity.ProcessoLog;
import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.getdoc.domain.vo.EmailVO;
import net.wasys.util.ddd.HibernateRepository;

@Repository
public class EmailEnviadoRepository extends HibernateRepository<EmailEnviado> {

	public EmailEnviadoRepository() {
		super(EmailEnviado.class);
	}

	public EmailEnviado getByCodigo(String codigo) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" where codigo = ? ");
		params.add(codigo);

		Query query = createQuery(hql.toString(), params);

		return (EmailEnviado) query.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	public List<EmailVO> findVosByProcesso(Long processoId) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select ");
		hql.append(" 	ee, pl, er ");
		hql.append(" from ");
		hql.append(" 	").append(clazz.getName()).append(" ee ");
		hql.append(" 	left outer join ").append(EmailRecebido.class.getName()).append(" er on er.emailEnviado.id = ee.id ");
		hql.append(" 	left outer join ").append(ProcessoLog.class.getName()).append(" pl on pl.emailEnviado.id = ee.id and pl.acao in ( '-1' ");
		for(AcaoProcesso acaoProcesso : AcaoProcesso.envioDeEmails){
			hql.append(", ?");
			params.add(acaoProcesso);
		}
		hql.append(" ) ");
		hql.append(" where ee.processo.id = ? ");
		params.add(processoId);

		hql.append(" order by ee.id, er.sentDate ");

		Query query = createQuery(hql.toString(), params);

		List<Object[]> list = query.list();
		Map<EmailEnviado, EmailVO> map = new LinkedHashMap<>();

		for (Object[] objs : list) {

			EmailEnviado ee = (EmailEnviado) objs[0];
			ProcessoLog pl = (ProcessoLog) objs[1];
			EmailRecebido er = (EmailRecebido) objs[2];

			EmailVO emailVO = map.get(ee);
			emailVO = emailVO != null ? emailVO : new EmailVO();
			map.put(ee, emailVO);

			emailVO.setLogCriacao(pl);
			emailVO.setEmailEnviado(ee);
			emailVO.addEmailRecebido(er);
		}

		return new ArrayList<>(map.values());
	}

    public Integer countByProcesso(Long processoId) {
		StringBuilder hql = new StringBuilder("select count(*) from " + clazz.getName() + " m ");

		Map<String, Object> params = new HashMap<>();
		hql.append(" where processo.id = :processoId ");
		params.put("processoId", processoId);

		Query query = createQuery(hql.toString(), params);

		return ((Long) query.uniqueResult()).intValue();
    }
}
