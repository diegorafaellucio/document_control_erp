package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.Bacalhau;
import net.wasys.getdoc.domain.vo.filtro.BacalhauFiltro;
import net.wasys.getdoc.mb.utils.DateUtils;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.Query;
import org.primefaces.model.SortOrder;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository
@SuppressWarnings("unchecked")
public class BacalhauRepository extends HibernateRepository<Bacalhau> {

	public BacalhauRepository() {
		super(Bacalhau.class);
	}

	public int countByFiltro(BacalhauFiltro filtro) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append("select count(*) from ").append(clazz.getName()).append(" la where 1=1 ");

		makeWhere(filtro, params, hql);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue();
	}

	public List<Bacalhau> findByFiltro(BacalhauFiltro filtro, int first, int pageSize) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append("from ").append(clazz.getName()).append(" la where 1=1 ");

		makeWhere(filtro, params, hql);

		String campoOrdem = filtro.getCampoOrdem();
		if(campoOrdem != null) {

			campoOrdem = campoOrdem.replace("log.", "la.");
			SortOrder ordem = filtro.getOrdem();
			String ordemStr = SortOrder.DESCENDING.equals(ordem) ? " desc " : " asc ";

			hql.append(" order by ").append(campoOrdem).append(ordemStr);
		}
		else {
			hql.append(" order by data desc ");
		}

		Query query = createQuery(hql.toString(), params);

		query.setFirstResult(first);
		query.setMaxResults(pageSize);

		return query.list();
	}

	private void makeWhere(BacalhauFiltro filtro, List<Object> params, StringBuilder hql) {

		Date dataInicio = filtro.getDataInicio();
		Date dataFim = filtro.getDataFim();
		Boolean apenasErros = filtro.getApenasErros();

		if(dataInicio != null && dataFim != null) {
			dataInicio = DateUtils.getFirstTimeOfDay(dataInicio);
			dataFim = DateUtils.getLastTimeOfDay(dataFim);

			hql.append(" and la.data >= ? ");
			params.add(dataInicio);
			hql.append(" and la.data <= ? ");
			params.add(dataFim);
		}

		if(apenasErros){
			hql.append(" and la.totalErros > 0 ");
		}

	}
}
