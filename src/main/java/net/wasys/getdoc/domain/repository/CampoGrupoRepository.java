package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.Campo;
import net.wasys.getdoc.domain.entity.CampoGrupo;
import net.wasys.getdoc.domain.entity.Documento;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@SuppressWarnings("unchecked")
public class CampoGrupoRepository extends HibernateRepository<CampoGrupo> {

	public CampoGrupoRepository() {
		super(CampoGrupo.class);
	}
	
	public List<CampoGrupo> findByProcesso(Long processoId) {

		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<>();

		hql.append(startQuery()).append(" cg ");
		hql.append(" where cg.processo.id = ? ");
		params.add(processoId);
		hql.append(" order by cg.ordem");

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}

	public List<CampoGrupo> findByProcessoIdAndTipoCampoGrupoId(Long processoId, Long tipoCampoGrupoId) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new LinkedHashMap<>();

		hql.append(startQuery()).append(" cg ");
		hql.append(" where cg.processo.id = :processoId ");
		hql.append(" and cg.tipoCampoGrupoId = :tipoCampoGrupoId");
		params.put("processoId", processoId);
		params.put("tipoCampoGrupoId", tipoCampoGrupoId);

		hql.append(" order by cg.nome");

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}

	public int getMaxOrdemGrupoByProcessoAndTipoCampoGrupo(Long processoId, Long tipoCampoGrupoId) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new LinkedHashMap<>();

		hql.append("select max(cg.ordem) from ").append(clazz.getName()).append(" cg ");
		hql.append(" where cg.processo.id = :processoId ");
		hql.append(" and tipoCampoGrupoId = :tipoCampoGrupoId");
		params.put("processoId", processoId);
		params.put("tipoCampoGrupoId", tipoCampoGrupoId);

		Query query = createQuery(hql.toString(), params);

		Object result = query.uniqueResult();
		return result == null ? 0 : ((Number) result).intValue();
	}

	public boolean existByProcesso(Long processoId, Long tipoGrupoId) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new LinkedHashMap<>();

		hql.append(" select count(*) from ").append(clazz.getName()).append(" cg ");
		hql.append(" where cg.processo.id = :processoId ");
		params.put("processoId", processoId);
		hql.append(" and cg.tipoCampoGrupoId = :tipoGrupoId ");
		params.put("tipoGrupoId", tipoGrupoId);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue() > 0;
	}

	public List<CampoGrupo> findGruposSemCamposByProcessoId(Long processoId) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" g ");
		hql.append(" where 1=1 ");
		hql.append(" and g.processo.id = :processoId  ");
		hql.append(" and (select count(*) from ").append(Campo.class.getName()).append(" c where c.grupo = g ) = 0");

		hql.append(" order by g.ordem ");

		params.put("processoId", processoId);

		Query query = createQuery(hql, params);

		return query.list();
	}

	public List<CampoGrupo> findByProcessoIdAndContainsNome(Long processoId, String nome) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new LinkedHashMap<>();

		hql.append(startQuery()).append(" cg ");
		hql.append(" where cg.processo.id = :processoId ");
		hql.append(" and upper(cg.nome) like :nome ");

		params.put("processoId", processoId);
		params.put("nome", "%" + nome.toUpperCase() + "%");

		hql.append(" order by cg.ordem ");

		Query query = createQuery(hql.toString(), params);
		return query.list();
	}

	public boolean grupoRelacionadoFoiApagado(Documento documento) {

		Long documentoId = documento.getId();

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new LinkedHashMap<>();

		hql.append(" select 1 from ").append(CampoGrupo.class.getName()).append(" cg ");
		hql.append(", ").append(Documento.class.getName()).append(" d ");
		hql.append(" where d.id = :documentoId and cg.id = d.grupoRelacionado.id ");

		params.put("documentoId", documentoId);

		Query query = createQuery(hql, params);

		Object result = query.uniqueResult();
		return result == null;
	}
}
