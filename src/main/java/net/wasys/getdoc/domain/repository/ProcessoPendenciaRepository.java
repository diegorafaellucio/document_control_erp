package net.wasys.getdoc.domain.repository;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import net.wasys.getdoc.domain.entity.ProcessoLog;
import net.wasys.getdoc.domain.entity.ProcessoPendencia;
import net.wasys.getdoc.domain.vo.ProcessoPendenciaVO;
import net.wasys.util.ddd.HibernateRepository;

@Repository
public class ProcessoPendenciaRepository extends HibernateRepository<ProcessoPendencia> {

	public ProcessoPendenciaRepository() {
		super(ProcessoPendencia.class);
	}

	public ProcessoPendenciaVO getLastPendenciaByProcesso(Long processoId) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select pl2 ");
		hql.append(" from ").append(ProcessoLog.class.getName()).append(" pl2 ");
		hql.append(" 	left outer join fetch pl2.pendencia pp2 ");
		hql.append(" where pl2.id = ( ");

		hql.append(" select ");
		hql.append(" 	max(pl.id) ");
		hql.append(" from ");
		hql.append(" 	").append(ProcessoLog.class.getName()).append(" pl ");
		hql.append(" 	left outer join pl.pendencia pp ");

		hql.append(" where pl.pendencia is not null ");
		hql.append(" and pp.dataFinalizacao is null ");
		hql.append(" and pl.pendencia.processo.id = ? ");
		params.add(processoId);

		hql.append(" ) ");

		Query query = createQuery(hql.toString(), params);

		ProcessoLog pl = (ProcessoLog) query.uniqueResult();

		if(pl == null) {
			return null;
		}

		ProcessoPendencia pendencia = pl.getPendencia();

		ProcessoPendenciaVO vo = new ProcessoPendenciaVO();
		vo.setLogCriacao(pl);
		vo.setPendencia(pendencia);

		return vo;
	}
}
