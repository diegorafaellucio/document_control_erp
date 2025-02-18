package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.Campo;
import net.wasys.getdoc.domain.entity.TipoCampoGrupo;
import net.wasys.getdoc.domain.enumeration.CampoMap;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CampoRepository extends HibernateRepository<Campo> {

	public CampoRepository() {
		super(Campo.class);
	}

	@SuppressWarnings("unchecked")
	public List<Campo> findByProcessoSituacao(Long processoId, Long situacaoId) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(startQuery()).append(" c ");
		hql.append(" where c.grupo.processo.id = :processoId ");
		params.put("processoId", processoId);

		if(situacaoId != null) {
			hql.append(" and ( ");
			hql.append(" 	select count(*) ");
			hql.append(" 	from ").append(TipoCampoGrupo.class.getName()).append(" cg ");
			hql.append(" 	left outer join cg.situacoes cgs ");
			hql.append(" 	where cg.id = c.grupo.tipoCampoGrupoId ");
			hql.append(" 	and cgs.situacao.id = :situacaoId ");
			hql.append(" ) > 0 ");
			params.put("situacaoId", situacaoId);
		}

		hql.append(" order by c.grupo.ordem, c.ordem ");

		Query query = createQuery(hql.toString(), params);
		return query.list();
	}

	public List<Campo> findByProcessoNomeGrupo(Long processoId, String nomeGrupo) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(startQuery()).append(" c ");
		hql.append(" where c.grupo.processo.id = :processoId ");
		params.put("processoId", processoId);
		hql.append(" and ( ");
		hql.append(" 	select count(*) ");
		hql.append(" 	from ").append(TipoCampoGrupo.class.getName()).append(" cg ");
		hql.append(" 	where cg.id = c.grupo.tipoCampoGrupoId ");
		hql.append(" 	and cg.nome = :nomeGrupo ");
		hql.append(" ) > 0 ");
		params.put("nomeGrupo", nomeGrupo);

		hql.append(" order by c.grupo.ordem, c.ordem ");

		Query query = createQuery(hql.toString(), params);
		return query.list();
	}

	public List<Campo> findByGrupoId(Long grupoId) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(startQuery()).append(" c ");
		hql.append(" where c.grupo.id = :grupoId ");
		params.put("grupoId", grupoId);

		Query query = createQuery(hql.toString(), params);
		return query.list();
	}

	public Map<String, Map<String, String>> findValoresMapByProcesso(Long processoId) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select c.grupo.nome, c.id || '-' || c.nome, c.valor from ").append(clazz.getName()).append(" c ");
		hql.append(" where c.grupo.processo.id = :processoId ");
		hql.append(" order by c.grupo.ordem, c.ordem ");
		params.put("processoId", processoId);

		Query query = createQuery(hql.toString(), params);

		List<Object[]> result1 = (List<Object[]>) query.list();
		Map<String, Map<String, String>> result2 = new LinkedHashMap<>();
		for (Object[] objs : result1) {
			String grupo = (String) objs[0];
			String campo = (String) objs[1];
			String valor = (String) objs[2];
			Map<String, String> campos = result2.get(grupo);
			campos = campos != null ? campos : new LinkedHashMap<>();
			result2.put(grupo, campos);
			campos.put(campo, valor);
		}
		return result2;
	}

	public List<Campo> findByProcessoTipoCampos(Long processoId, List<Long> tipoCampoIds) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(startQuery()).append(" c ");
		hql.append(" where c.grupo.processo.id = :processoId ");
		hql.append(" and c.tipoCampoId in (:tipoCampoIds) ");

		params.put("processoId", processoId);
		params.put("tipoCampoIds", tipoCampoIds);

		hql.append(" order by c.nome ");

		Query query = createQuery(hql.toString(), params);
		return query.list();
	}

	public List<Campo> findByProcessoTipo(Long processoId, String tipo) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(startQuery()).append(" c ");
		hql.append(" where c.grupo.processo.id = :processoId ");
		hql.append(" and c.tipo = :tipo ");

		params.put("processoId", processoId);
		params.put("tipo", tipo);

		hql.append(" order by c.nome ");

		Query query = createQuery(hql.toString(), params);
		return query.list();
	}

	public List<String> findValoresByCampo(CampoMap.CampoEnum campo) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select c.valor ");
		hql.append(" from ").append(clazz.getName()).append(" c ");
		hql.append(" where c.grupo.nome = :grupoNome ");
		hql.append(" and c.nome = :campoNome ");
		hql.append(" group by c.valor ");
		hql.append(" order by c.valor ");

		params.put("grupoNome", campo.getGrupo().getNome());
		params.put("campoNome", campo.getNome());

		Query query = createQuery(hql,params);

		return (List<String>) query.list();
	}

	public List<Campo> findByGrupoIdAndNome(Long grupoId, String nome) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(startQuery()).append(" c ");
		hql.append(" where c.grupo.id = :grupoId ");
		hql.append(" and c.nome = :nome ");

		params.put("grupoId", grupoId);
		params.put("nome", nome);

		Query query = createQuery(hql, params);

		return query.list();
	}

	public List<String> getLocalDeOferta() {

		Map<String, Object> params = new LinkedHashMap<>();

		StringBuilder hql = new StringBuilder();

		hql.append("select c.valor from ").append(clazz.getName()).append(" c");
		hql.append(" where c.nome = :localDeOferta");
		hql.append(" group by c.valor ");

		params.put("localDeOferta", CampoMap.CampoEnum.LOCAL_DE_OFERTA.getNome());

		Query query = createQuery(hql, params);

		return query.list();
	}

	public String findValorByProcessoId(Long processoId, CampoMap.CampoEnum campoEnum) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select ");
		hql.append(" 	c.valor ");
		hql.append(" from ").append(clazz.getName()).append(" c ");
		hql.append(" where 1=1 ");
		hql.append(" 	and c.grupo.processo.id = :processoId ");
		hql.append(" 	and c.grupo.nome = :nomeCampoGrupo ");
		hql.append(" 	and c.nome = :nomeCampo ");

		params.put("processoId", processoId);
		params.put("nomeCampoGrupo", campoEnum.getGrupo().getNome());
		params.put("nomeCampo", campoEnum.getNome());

		Query query = createQuery(hql, params);

		return query.uniqueResult().toString();
	}
}
