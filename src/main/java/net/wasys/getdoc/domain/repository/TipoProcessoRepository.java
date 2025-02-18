package net.wasys.getdoc.domain.repository;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.wasys.getdoc.domain.entity.*;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.primefaces.model.SortOrder;
import org.springframework.stereotype.Repository;

import net.wasys.getdoc.domain.enumeration.PermissaoTP;
import net.wasys.getdoc.domain.vo.TipoProcessoVO;
import net.wasys.util.ddd.HibernateRepository;

@Repository
@SuppressWarnings("unchecked")
public class TipoProcessoRepository extends HibernateRepository<TipoProcesso> {

	public TipoProcessoRepository() {
		super(TipoProcesso.class);
	}

	public List<TipoProcesso> findAtivos(List<PermissaoTP> permissoes) {

		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<Object>();

		hql.append(" from ").append(clazz.getName()).append(" tp ");
		hql.append(" where tp.ativo is true ");

		if(permissoes != null) {
			hql.append(" and ( ");
			hql.append(" 	select count(*) from tp.permissoes p where p.permissao in( ''");
			for (PermissaoTP p : permissoes) {
				hql.append(", ?");
				params.add(p);
			}
			hql.append(" )) > 0 ");
		}

		hql.append(" order by tp.nome ");

		Query query = createQuery(hql, params);

		return query.list();
	}

	public List<TipoProcessoVO> findAll(Integer inicio, Integer max, String sortField, SortOrder sortOrder) {

		StringBuilder hql = new StringBuilder();

		hql.append(" select new ").append(TipoProcessoVO.class.getName()).append(" ( ");
		hql.append(" 	tp, ");
		hql.append(" 	(select count(*) from ").append(Campanha.class.getName()).append(" c where c.tipoProcesso = tp.id), ");
		hql.append(" 	(select count(*) from ").append(TipoCampo.class.getName()).append(" tc where tc.grupo.tipoProcesso = tp.id), ");
		hql.append(" 	(select count(*) from ").append(TipoDocumento.class.getName()).append(" td where td.tipoProcesso = tp.id), ");
		hql.append(" 	(select count(*) from ").append(Situacao.class.getName()).append(" s where s.tipoProcesso = tp.id) ");
		hql.append(" ) ");
		hql.append(" from ").append(clazz.getName()).append(" tp ");

		hql.append(" order by ");
		if(StringUtils.isNotBlank(sortField)) {

			sortField = sortField.replace("tipoProcesso.", "tp.");
			String ordemStr = SortOrder.DESCENDING.equals(sortOrder) ? " desc " : " asc ";
			hql.append(sortField).append(ordemStr).append(", ");
		}
		hql.append(" nome ");

		Query query = createQuery(hql.toString());

		query.setFirstResult(inicio);
		query.setMaxResults(max);

		List<TipoProcessoVO> list = query.list();
		for (TipoProcessoVO vo : list) {
			TipoProcesso tipoProcesso = vo.getTipoProcesso();
			Set<TipoProcessoPermissao> permissoes = tipoProcesso.getPermissoes();
			Hibernate.initialize(permissoes);
		}

		return list;
	}

	public List<TipoProcesso> findAll(Integer inicio, Integer max) {

		StringBuilder hql = new StringBuilder();

		hql.append(" from ").append(clazz.getName()).append(" tp ");
		hql.append(" order by tp.nome ");

		Query query = createQuery(hql.toString());

		if(inicio != null) {
			query.setFirstResult(inicio);
		}
		if(max != null) {
			query.setMaxResults(max);
		}

		return query.list();
	}

	public int count() {

		Query query = createQuery(" select count(*) from " + clazz.getName());

		return ((Number) query.uniqueResult()).intValue();
	}

	public TipoProcesso getByNome(String nome) {

		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<>();

		hql.append(" from ").append(clazz.getName()).append(" tp ");
		hql.append(" where upper(tp.nome) = ? ");
		params.add(nome.toUpperCase());

		Query query = createQuery(hql.toString(), params);

		return (TipoProcesso) query.uniqueResult();
	}

	public boolean existsByNome(String nome) {

		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<Object>();

		hql.append(" select count(*) from ").append(clazz.getName()).append(" tp ");
		hql.append(" where tp.nome = ? ");
		params.add(nome);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue() > 0;
	}

	public List<TipoProcesso> findByIds(List<Long> ids) {
		StringBuilder hql = new StringBuilder();
		hql.append(getStartQuery()).append(" s ");

		hql.append(" where s.id in (-1");
		for (Long id : ids) {
			hql.append(", ").append(id);
		}
		hql.append(") ");

		hql.append(" order by s.id ");

		Query query = createQuery(hql.toString());
		query.setFetchSize(100);

		return query.list();
	}
}
