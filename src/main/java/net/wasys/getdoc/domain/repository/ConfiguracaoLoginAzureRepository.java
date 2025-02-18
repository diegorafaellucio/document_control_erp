package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.ConfiguracaoLoginAzure;
import net.wasys.getdoc.domain.entity.Subperfil;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.domain.vo.filtro.ConfiguracaoLoginAzureFiltro;
import net.wasys.util.ddd.HibernateRepository;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
@SuppressWarnings("unchecked")
public class ConfiguracaoLoginAzureRepository extends HibernateRepository<ConfiguracaoLoginAzure> {

	public ConfiguracaoLoginAzureRepository() {
		super(ConfiguracaoLoginAzure.class);
	}

	public List<ConfiguracaoLoginAzure> findAll(Integer inicio, Integer max) {

		Query query = createQuery(" from " + clazz.getName() + " order by grupo ");

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

	public int countByFiltro(ConfiguracaoLoginAzureFiltro filtro) {

		StringBuilder hql = new StringBuilder();

		hql.append(" select count(*) from ").append(clazz.getName()).append(" c ");

		Map<String, Object> params = makeQuery(filtro, hql);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue();
	}

	public List<ConfiguracaoLoginAzure> findByFiltro(ConfiguracaoLoginAzureFiltro filtro, Integer inicio, Integer max) {

		StringBuilder hql = new StringBuilder();

		hql.append("select c from ").append(clazz.getName()).append(" c ");

		Map<String, Object> params = makeQuery(filtro, hql);

		Query query = createQuery(hql.toString(), params);

		hql.append(" order by c.grupo");

		if(inicio != null) {
			query.setFirstResult(inicio);
		}
		if(max != null) {
			query.setMaxResults(max);
		}

		List<ConfiguracaoLoginAzure> list = query.list();
		return list;

	}

	private Map<String, Object> makeQuery(ConfiguracaoLoginAzureFiltro filtro, StringBuilder hql) {
		Map<String, Object> params = new LinkedHashMap<>();

		String grupo = filtro.getGrupo();
		RoleGD roleGD = filtro.getRoleGD();
		Subperfil subperfil = filtro.getSubperfil();

		hql.append(" where 1=1 ");

		if(StringUtils.isNotBlank(grupo)) {
			hql.append(" and lower(c.grupo) like :grupo");
			grupo = StringUtils.lowerCase(grupo);
			params.put("grupo", "%"+ grupo.trim() +"%");
		}

		if(roleGD != null) {
			hql.append(" and c.role = :role");
			params.put("role", roleGD);
		}

		if(subperfil != null) {
			hql.append(" and c.subperfil.id = :subperfilId");
			params.put("subperfilId", subperfil.getId());
		}

		return params;
	}

	public ConfiguracaoLoginAzure findByGrupoAD(String grupoAD) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" c");
		hql.append(" where lower(c.grupo) = :grupo");

		grupoAD = StringUtils.lowerCase(grupoAD);
		params.put("grupo", grupoAD.trim());

		Query query = createQuery(hql, params);

		return (ConfiguracaoLoginAzure) query.uniqueResult();
	}

	public List<ConfiguracaoLoginAzure> findBySubperfilId(Long subperfilId) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" c");
		hql.append(" where c.subperfil.id = :subperfilId");

		params.put("subperfilId", subperfilId);

		Query query = createQuery(hql, params);

		return query.list();
	}
}
