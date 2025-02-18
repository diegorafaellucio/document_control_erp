package net.wasys.getdoc.domain.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import net.wasys.getdoc.domain.entity.Ajuda.Objetivo;
import net.wasys.getdoc.domain.entity.ProcessoAjuda;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.vo.filtro.AjudaFiltro;
import net.wasys.util.ddd.HibernateRepository;

@Repository
@SuppressWarnings("unchecked")
public class ProcessoAjudaRepository extends HibernateRepository<ProcessoAjuda> {

	public ProcessoAjudaRepository() {
		super(ProcessoAjuda.class);
	}

	public ProcessoAjuda getSuperiorById(Long id) {
		StringBuilder hql = new StringBuilder();
		hql	.append("select ");
		hql	.append(	"ajuda.superior ");
		hql	.append("from ").append(clazz.getName()).append(" ajuda ");
		hql	.append("where ajuda.id = :id ");
		Query query = createQuery(hql.toString());
		query.setLong("id", id);
		return (ProcessoAjuda) query.uniqueResult();
	}

	public List<ProcessoAjuda> getInferioresBySuperior(Long superiorId, boolean recursivo) {
		StringBuilder hql = new StringBuilder();
		hql	.append("from ").append(clazz.getName()).append(" ");
		hql	.append("where superior.id = :superiorId ");
		Query query = createQuery(hql.toString());
		query.setLong("superiorId", superiorId);
		List<ProcessoAjuda> inferiores = query.list();
		if (CollectionUtils.isNotEmpty(inferiores)) {
			for (ProcessoAjuda inferior : inferiores) {
				inferior.setInferiores(getInferioresBySuperior(inferior.getId(), recursivo));
			}
		}
		return inferiores;
	}

	public List<ProcessoAjuda> findByProcesso(Long processoId) {
		return findByProcesso(processoId, null);
	}

	public List<ProcessoAjuda> findByProcesso(Long processoId, Long tipoDocumentoId) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new LinkedHashMap<>();

		hql	.append(" select pa from ").append(clazz.getName()).append(" pa ");
		hql	.append(" left outer join fetch pa.inferiores ");

		hql	.append(" where pa.superior.id = null ");
		hql	.append(" and pa.processo.id = :processoId ");
		params.put("processoId", processoId);

		hql	.append("and pa.objetivo = :objetivo ");
		params.put("objetivo", Objetivo.REQUISICAO);

		if (tipoDocumentoId != null) {
			hql	.append("and pa.tipoDocumento.id = :tipoDocumentoId ");
			params.put("tipoDocumentoId", tipoDocumentoId);
		}

		hql	.append("order by pa.ordem, pa.id asc ");

		Query query = createQuery(hql.toString(), params);

		List<ProcessoAjuda> ajudas = query.list();
		ajudas = new ArrayList<>(new LinkedHashSet<>(ajudas));
		return ajudas;
	}

	public ProcessoAjuda getByProcessoAndObjetivo(Long processoId, Objetivo objetivo, boolean recursivo) {
		StringBuilder hql = new StringBuilder();
		hql	.append("from ").append(clazz.getName()).append(" ");
		hql	.append("where processo.id = :processoId ");
		hql	.append("and objetivo = :objetivo ");
		hql	.append("and superior.id = null ");
		Query query = createQuery(hql.toString());
		query.setLong("processoId", processoId);
		query.setParameter("objetivo", objetivo);
		ProcessoAjuda ajuda = (ProcessoAjuda) query.uniqueResult();
		if (ajuda != null && recursivo) {
			ajuda.setInferiores(getInferioresBySuperior(ajuda.getId(), recursivo));
		}
		return ajuda;
	}

	public List<ProcessoAjuda> findByFiltro(AjudaFiltro filtro, Integer inicio, Integer max) {

		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<Object>();

		hql	.append("from ").append(clazz.getName()).append(" ajuda ");
		hql	.append(	"inner join fetch ajuda.tipoProcesso tipoProcesso ");

		if (filtro != null) {

			hql.append("where 0 = 0 ");

			TipoProcesso tipoProcesso = filtro.getTipoProcesso();
			if (tipoProcesso != null) {
				hql	.append("and tipoProcesso.id = ? ");
				params.add(tipoProcesso.getId());
			}
		}

		hql	.append("order by tipoProcesso.nome ");

		Query query = createQuery(hql, params);

		if (inicio != null) {
			query.setFirstResult(inicio);
		}

		if (max != null) {
			query.setMaxResults(max);
		}

		return query.list();
	}

	public int countByFiltro(AjudaFiltro filtro) {

		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<Object>();

		hql	.append("select ");
		hql	.append(	"count(id) ");
		hql	.append("from ").append(clazz.getName()).append(" ");

		if (filtro != null) {

			hql.append("where 0 = 0 ");

			TipoProcesso tipoProcesso = filtro.getTipoProcesso();
			if (tipoProcesso != null) {
				hql	.append("and tipoProcesso.id = ? ");
				params.add(tipoProcesso.getId());
			}
		}

		Query query = createQuery(hql, params);
		Number number = (Number) query.uniqueResult();

		return number.intValue();
	}

	public List<ProcessoAjuda> getByPendencia(Long processoId) {
		StringBuilder hql = new StringBuilder();
		hql	.append("from ").append(clazz.getName()).append(" ");
		hql	.append("where processo.id = :processoId ");
		hql	.append("and objetivo = :objetivo ");
		hql	.append("and ativo is true and marcado is true ");
		hql.append(" and pendencia is true ");
		
		hql	.append("order by id asc ");
		Query query = createQuery(hql.toString());
		query.setLong("processoId", processoId);
		query.setParameter("objetivo", Objetivo.REQUISICAO);
		List<ProcessoAjuda> ajudas = query.list();
		return ajudas;
	}
}