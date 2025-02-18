package net.wasys.getdoc.domain.repository;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import net.wasys.getdoc.domain.entity.Ajuda;
import net.wasys.getdoc.domain.entity.Ajuda.Objetivo;
import net.wasys.getdoc.domain.vo.filtro.AjudaFiltro;
import net.wasys.util.ddd.HibernateRepository;

@Repository
@SuppressWarnings("unchecked")
public class AjudaRepository extends HibernateRepository<Ajuda> {

	public AjudaRepository() {
		super(Ajuda.class);
	}

	public Ajuda getSuperiorById(Long id) {
		StringBuilder hql = new StringBuilder();
		hql	.append("select ");
		hql	.append(	"ajuda.superior ");
		hql	.append("from ").append(clazz.getName()).append(" ajuda ");
		hql	.append("where ajuda.id = :id ");
		Query query = createQuery(hql.toString());
		query.setLong("id", id);
		return (Ajuda) query.uniqueResult();
	}

	public List<Ajuda> getByTipoProcesso(Long tipoProcessoId) {
		StringBuilder hql = new StringBuilder();
		hql	.append("from ").append(clazz.getName()).append(" ");
		hql	.append("where tipoProcesso.id = :tipoProcessoId ");
		hql	.append("and objetivo = :objetivo ");
		hql	.append("and superior.id = null ");
		Query query = createQuery(hql.toString());
		query.setLong("tipoProcessoId", tipoProcessoId);
		query.setParameter("objetivo", Objetivo.REQUISICAO);
		return query.list();
	}
	
	public List<Ajuda> findByTipoProcesso(Long tipoProcessoId, Integer inicio, Integer max) {
		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append("from ").append(clazz.getName()).append(" ");
		hql.append(" where tipoProcesso.id = ? ");
		params.add(tipoProcessoId);

		hql.append(" order by ordem ");
		Query query = createQuery(hql, params);

		if(inicio != null) {
			query.setFirstResult(inicio);
		}
		if(max != null) {
			query.setMaxResults(max);
		}

		return query.list();
	}

	public List<Ajuda> getInferioresBySuperior(Long superiorId, boolean recursivo) {
		StringBuilder hql = new StringBuilder();
		hql	.append("from ").append(clazz.getName()).append(" ");
		hql	.append("where superior.id = :superiorId ");
		Query query = createQuery(hql.toString());
		query.setLong("superiorId", superiorId);
		List<Ajuda> inferiores = query.list();
		if (CollectionUtils.isNotEmpty(inferiores)) {
			for (Ajuda inferior : inferiores) {
				inferior.setInferiores(getInferioresBySuperior(inferior.getId(), recursivo));
			}
		}
		return inferiores;
	}

	public Ajuda getByTipoProcessoAndObjetivo(Long tipoProcessoId, Objetivo objetivo, boolean recursivo) {
		StringBuilder hql = new StringBuilder();
		hql	.append("from ").append(clazz.getName()).append(" ");
		hql	.append("where tipoProcesso.id = :tipoProcessoId ");
		hql	.append("and objetivo = :objetivo ");
		hql	.append("and superior.id = null ");
		Query query = createQuery(hql.toString());
		query.setLong("tipoProcessoId", tipoProcessoId);
		query.setParameter("objetivo", objetivo);
		Ajuda ajuda = (Ajuda) query.uniqueResult();
		if (ajuda != null && recursivo) {
			ajuda.setInferiores(getInferioresBySuperior(ajuda.getId(), recursivo));
		}
		return ajuda;
	}

	public List<Ajuda> findByFiltro(AjudaFiltro filtro, Integer inicio, Integer max) {
		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<Object>();

		hql	.append("from ").append(clazz.getName()).append(" a ");
		hql	.append(	"inner join fetch a.tipoProcesso tipoProcesso ");

		params = makeQuery(filtro, hql);
		hql.append(" order by a.ordem ");
		Query query = createQuery(hql, params);

		if (inicio != null) {
			query.setFirstResult(inicio);
		}
		if (max != null) {
			query.setMaxResults(max);
		}

		List<Ajuda> list = query.list();
		Set<Ajuda> set = new LinkedHashSet<>(list);//pra tirar os repetidos
		return new ArrayList<Ajuda>(set);
	}
	
	private List<Object> makeQuery(AjudaFiltro filtro, StringBuilder hql) {
		Long tipoProcessoId = filtro.getTipoProcessoId();
		boolean somenteNoInicial = filtro.isSomenteNoInicial();
		List<Object> params = new ArrayList<Object>();
		
		hql.append(" where 1=1");
		
		if(tipoProcessoId != null) {
			hql.append(" and a.tipoProcesso.id = ?");
			params.add(tipoProcessoId);
		}
		
		if (somenteNoInicial) {
			hql	.append(" and a.superior is null");
		}
		
		return params;
	}

	public int countByFiltro(AjudaFiltro filtro) {
		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<Object>();

		hql	.append("select count(*) ");
		hql	.append("from ").append(clazz.getName()).append(" a ");

		params = makeQuery(filtro, hql);
		Query query = createQuery(hql.toString(), params);
		return ((Number) query.uniqueResult()).intValue();
	}
	
	public Ajuda getAnterior(Long tipoProcessoId, Integer ordem) {
		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql	.append("from ").append(clazz.getName()).append(" ");
		hql.append(" where superior.id is null ");
		hql.append(" and tipoProcesso.id = ? ");
		params.add(tipoProcessoId);

		hql.append(" and ordem < ? ");
		params.add(ordem);

		hql.append(" order by ordem desc ");
		Query query = createQuery(hql, params);
		query.setMaxResults(1);
		return (Ajuda) query.uniqueResult();
	}
	
	public Ajuda getProximo(Long tipoProcessoId, Integer ordem) {
		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql	.append("from ").append(clazz.getName()).append(" ");
		hql.append(" where superior.id is null ");
		hql.append(" and tipoProcesso.id = ? ");
		params.add(tipoProcessoId);

		hql.append(" and ordem > ? ");
		params.add(ordem);

		hql.append(" order by ordem asc ");
		Query query = createQuery(hql, params);
		query.setMaxResults(1);
		return (Ajuda) query.uniqueResult();
	}
}