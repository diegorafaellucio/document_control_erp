package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.AdminAjuda;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.vo.filtro.AdminAjudaFiltro;
import net.wasys.util.ddd.HibernateRepository;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.primefaces.model.SortOrder;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Repository
@SuppressWarnings("unchecked")
public class AdminAjudaRepository extends HibernateRepository<AdminAjuda> {

	public AdminAjudaRepository() {
		super(AdminAjuda.class);
	}


	public int countByFiltro(AdminAjudaFiltro filtro) {

		int count = 0;

		StringBuilder hql = new StringBuilder();

		hql.append(" select count(*) from ").append(clazz.getName()).append(" a ");

		List<Object> params = makeQuery(filtro, hql);

		Query query = createQuery(hql.toString(), params);

		Object result = query.uniqueResult();

		if(result != null){
			count = ((Number)result).intValue();
		}

		return count;
	}

	private void makeOrderBy(AdminAjudaFiltro filtro, StringBuilder hql) {

		String campoOrdem = filtro.getCampoOrdem();
		if(StringUtils.isNotBlank(campoOrdem)) {
			campoOrdem = campoOrdem.replace("admin_ajuda.", "a.");

			SortOrder ordem = filtro.getOrdem();
			String ordemStr = SortOrder.DESCENDING.equals(ordem) ? " desc " : " asc ";

			hql.append(" order by ").append(campoOrdem).append(ordemStr);
		} else {
			hql.append(" order by a.id desc ");
		}
	}

	public List<AdminAjuda> findByFiltro(AdminAjudaFiltro filtro, Integer inicio, Integer max) {

		StringBuilder hql = new StringBuilder();
		hql.append(getStartQuery()).append(" a ");

		List<Object> params = makeQuery(filtro, hql);
		makeOrderBy(filtro, hql);

		Query query = createQuery(hql.toString(), params);

		if(inicio != null) {
			query.setFirstResult(inicio);
		}
		if(max != null) {
			query.setMaxResults(max);
		}

		List<AdminAjuda> list = query.list();
		return list;
	}

	public List<Long> findIdsByFiltro(AdminAjudaFiltro filtro, Integer inicio, Integer max) {

		StringBuilder hql = new StringBuilder();

		hql.append(" select a.id from ").append(AdminAjuda.class.getName()).append(" a ");

		List<Object> params = makeQuery(filtro, hql);

		makeOrderBy(filtro, hql);

		Query query = createQuery(hql.toString(), params);

		if(inicio != null) {
			query.setFirstResult(inicio);
		}
		if(max != null) {
			query.setMaxResults(max);
		}

		List<Long> list = query.list();
		return list;
	}

	private List<Object> makeQuery(AdminAjudaFiltro filtro, StringBuilder hql) {

		List<Object> params = new ArrayList<>();

		Date dataInicio = filtro.getDataInicio();
		Date dataFim = filtro.getDataFim();
		Usuario analista = filtro.getAnalista();

		hql.append(" where 1=1 ");

		if(dataInicio != null) {
			hql.append(" and a.dataCriacao >= ? ");
			params.add(dataInicio);
		}

		if(dataFim != null) {
			Calendar c = Calendar.getInstance();
			c.setTime(dataFim);
			c.add(Calendar.DAY_OF_MONTH, 1);
			c.add(Calendar.SECOND, -1);
			dataFim = c.getTime();

			hql.append(" and a.dataCriacao <= ? ");
			params.add(dataFim);
		}

		String aux = " and ";
		if(analista != null) {
			Long analistaId = analista.getId();
			hql.append(aux + " ( a.analista.id = ? ");
			params.add(analistaId);
			hql.append(" ) ");
		}

		return params;
	}
}
