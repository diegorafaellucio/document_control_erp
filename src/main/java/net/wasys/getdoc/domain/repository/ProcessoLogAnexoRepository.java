package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.ProcessoLogAnexo;
import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.getdoc.domain.vo.filtro.AnexoFiltro;
import net.wasys.util.ddd.HibernateRepository;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@SuppressWarnings("unchecked")
public class ProcessoLogAnexoRepository extends HibernateRepository<ProcessoLogAnexo> {

	public ProcessoLogAnexoRepository() {
		super(ProcessoLogAnexo.class);
	}

	public List<Long> findIdsByDataDigitalizacao(Date dataInicio, Date dataFim) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<String, Object>();

		hql.append(" select pla.id from ").append(clazz.getName()).append(" pla ");
		hql.append(" where pla.processoLog.data > :dataInicio ");
		hql.append(" and pla.processoLog.data < :dataFim ");
		hql.append(" order by pla.id ");

		params.put("dataInicio", dataInicio);
		params.put("dataFim", dataFim);

		Query query = createQuery(hql, params);

		return query.list();
	}

	public List<ProcessoLogAnexo> findByIds(List<Long> ids) {

		StringBuilder hql = new StringBuilder();
		hql.append(" from ").append(clazz.getName()).append(" pla ");

		hql.append(" where pla.id in (-1");
		for (Long id : ids) {
			hql.append(", ").append(id);
		}
		hql.append(") ");

		Query query = createQuery(hql.toString());
		query.setFetchSize(100);

		return query.list();
	}

	public List<ProcessoLogAnexo> findByProcessoAnexos(AnexoFiltro filtro) {

		Map<String,Object> params = new HashMap<String,Object>();
		StringBuilder hql = new StringBuilder();
		List<AcaoProcesso> acoes = filtro.getAcoesSelecionadas();

		hql.append("select pla from ").append(clazz.getName()).append(" pla ");
		hql.append(" left join pla.processoLog.usuario as usuario ");

		hql.append(" where pla.processoLog.processo.id = :processoId ");
		params.put("processoId",filtro.getProcessoId());

		if (filtro.getDataInicio()!=null){
			Date dataInicio = DateUtils.truncate(filtro.getDataInicio(), Calendar.DAY_OF_MONTH);
			hql.append(" and date_trunc('day',pla.processoLog.data)>= :dataInicio ");
			params.put("dataInicio", dataInicio);
		}

		if (filtro.getDataFim()!=null){
			Date dataFim = DateUtils.truncate(filtro.getDataFim(), Calendar.DAY_OF_MONTH);
			hql.append(" and date_trunc('day',pla.processoLog.data)<= :dataFim ");
			params.put("dataFim", dataFim);
		}

		String busca = filtro.getBusca();
		if (StringUtils.isNotBlank(busca)){

			hql.append(" and ( upper(pla.nome) like upper(:busca)  ");
			hql.append(" or upper(usuario.nome) like upper(:busca) ");
			hql.append(" or upper(pla.processoLog.observacao) like upper(:busca)");
			hql.append(" or upper(pla.extensao) like upper(:busca) )");

			params.put("busca", "%" + busca.toUpperCase() + "%");
		}

		int i=0;
		if(acoes != null && !acoes.isEmpty()) {

			hql.append(" and pla.processoLog.acao in ( ");

			for (Iterator<AcaoProcesso> it = acoes.iterator(); it.hasNext();) {
				AcaoProcesso acao =it.next();
				hql.append(" :acao"+i);
				params.put("acao"+i, acao);
				if (it.hasNext()){
					hql.append(" , ");
				}
                i++;
			}
			hql.append(" ) ");
		}

		hql.append(" order by pla.id desc");

		Query query = createQuery(hql.toString(), params);

		List<ProcessoLogAnexo> list = query.list();
		return list;
	}
	
	public List<ProcessoLogAnexo> findAll() {
		StringBuilder hql = new StringBuilder();
		hql.append(" from ").append(clazz.getName()).append(" pla ");
		Query query = createQuery(hql.toString());
		return query.list();
	}
}
