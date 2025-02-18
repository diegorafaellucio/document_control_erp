package net.wasys.getdoc.domain.repository;

import java.util.*;

import net.wasys.getdoc.domain.entity.TipoProcesso;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import net.wasys.getdoc.domain.entity.TipoCampo;
import net.wasys.getdoc.domain.entity.TipoCampoGrupo;
import net.wasys.util.ddd.HibernateRepository;

@Repository
@SuppressWarnings("unchecked")
public class TipoCampoRepository extends HibernateRepository<TipoCampo> {

	public TipoCampoRepository() {
		super(TipoCampo.class);
	}

	public List<TipoCampo> findByGrupo(Long grupoId) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" where grupo.id = ? ");
		params.add(grupoId);

		hql.append(" order by ordem ");

		Query query = createQuery(hql, params);

		return query.list();
	}

	public List<TipoCampo> findByNome(TipoCampoGrupo tipoCampoGrupo, String nome, Long exceptId) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" where nome = ? and id <> ? and grupo.nome = ? ");
		params.add(nome);
		params.add(exceptId);
		params.add(tipoCampoGrupo.getNome());

		hql.append(" order by ordem ");

		Query query = createQuery(hql, params);

		return query.list();
	}

	public List<TipoCampo> findByTipoCampoGrupo(TipoCampoGrupo tipoCampoGrupo) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" where grupo.id = :grupoId ");
		params.put("grupoId", tipoCampoGrupo.getId());

		hql.append(" order by ordem ");

		Query query = createQuery(hql, params);

		return query.list();
	}

	public TipoCampo getByGrupoNomeAndNome(Long tipoProcessoId, String tipoCampoGrupoNome, String nome) {

		List<Object> params = new ArrayList<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" where nome = ? and  grupo.nome = ? and grupo.tipoProcesso.id = ? ");
		params.add(nome);
		params.add(tipoCampoGrupoNome);
		params.add(tipoProcessoId);

		Query query = createQuery(hql, params);

		return (TipoCampo) query.uniqueResult();
	}

	public TipoCampo getAnterior(Long grupoId, Integer ordem) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" tc where tc.grupo.id = ? ");
		params.add(grupoId);

		hql.append(" and tc.ordem < ? ");
		params.add(ordem);

		hql.append(" order by tc.ordem desc ");

		Query query = createQuery(hql, params);

		query.setMaxResults(1);

		return (TipoCampo) query.uniqueResult();
	}

	public TipoCampo getProximo(Long grupoId, Integer ordem) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" tc where tc.grupo.id = ? ");
		params.add(grupoId);

		hql.append(" and tc.ordem > ? ");
		params.add(ordem);

		hql.append(" order by tc.ordem asc ");

		Query query = createQuery(hql, params);

		query.setMaxResults(1);

		return (TipoCampo) query.uniqueResult();
	}

	public Map<TipoCampoGrupo, List<TipoCampo>> findMapByTipoProcesso(Long tipoProcessoId, Boolean criacaoProcesso) {

		List<TipoCampo> list = findByTipoProcessoSituacao(tipoProcessoId, null, criacaoProcesso);

		Map<TipoCampoGrupo, List<TipoCampo>> map = new LinkedHashMap<>();
		for (TipoCampo tipoCampo : list) {
			TipoCampoGrupo grupo = tipoCampo.getGrupo();
			List<TipoCampo> list2 = map.get(grupo);
			list2 = list2 != null ? list2 : new ArrayList<TipoCampo>();
			map.put(grupo, list2);
			list2.add(tipoCampo);
		}

		return map;
	}

	public List<TipoCampo> findByTipoProcessoSituacao(Long tipoProcessoId, Long situacaoId, Boolean criacaoProcesso) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" tc where tc.grupo.tipoProcesso.id = ? ");
		params.add(tipoProcessoId);

		if(situacaoId != null) {
			hql.append(" and (select count(*) from net.wasys.getdoc.domain.entity.TipoCampoGrupoSituacao tcgs where tcgs.tipoCampoGrupo=tc.grupo and tcgs.situacao.id = ? ) > 0 ");
			params.add(situacaoId);
		}

		if(criacaoProcesso != null){
			hql.append(" and tc.grupo.criacaoProcesso = ? ");
			params.add(criacaoProcesso);
		}

		hql.append(" order by tc.grupo.ordem, tc.ordem ");

		Query query = createQuery(hql, params);

		return (List<TipoCampo>) query.list();
	}

	public List<TipoCampo> findCamposComOrigemByTipoProcesso(Long tipoProcessoId) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" tc where tc.grupo.tipoProcesso.id = ? ");
		hql.append ("and origem is not null and origem != ''");
		params.add(tipoProcessoId);

		hql.append(" order by tc.grupo.ordem, tc.ordem ");

		Query query = createQuery(hql, params);

		return (List<TipoCampo>) query.list();
	}

	public List<TipoCampo> findPossiveisPais(TipoCampo tipoCampo) {

		TipoCampoGrupo grupo = tipoCampo.getGrupo();
//		Long grupoId = grupo.getId();
		TipoProcesso tipoProcesso = grupo.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();
		Long tipoCampoId = tipoCampo.getId();

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" c ");
		hql.append(" where c.id <> :campoId ");
		params.put("campoId", tipoCampoId);

		hql.append(" and c.grupo.tipoProcesso.id = :tipoProcessoId ");
		params.put("tipoProcessoId", tipoProcessoId);

//		hql.append(" and c.grupo.id = :grupoId ");
//		params.put("grupoId", grupoId);

		hql.append(" order by c.grupo.id, c.ordem ");

		Query query = createQuery(hql, params);
		return query.list();
	}

	public List<TipoCampo> findTipoCamposByTipoProcesso(TipoProcesso tipoProcesso, boolean apenasEditavel) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();
		Long tipoProcessoId = tipoProcesso.getId();

		if (tipoProcesso != null) {
			hql.append(getStartQuery()).append(" tc ");
			hql.append(" where tc.grupo.tipoProcesso.id = :tipoProcessoId ");
			params.put("tipoProcessoId", tipoProcessoId);
		}

		if(apenasEditavel) {
			hql.append(" and tc.editavel is :apenasEditavel ");
			params.put("apenasEditavel", apenasEditavel);
		}

//		hql.append(" and c.grupo.id = :grupoId ");
//		params.put("grupoId", grupoId);

		hql.append(" order by tc.grupo.id, tc.ordem ");

		Query query = createQuery(hql, params);
		return query.list();
	}

	public List<TipoCampo> findDefinemUnicidade(Long tipoProcessoId) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" where grupo.tipoProcesso.id = :tipoProcessoId ");
		hql.append(" and defineUnicidade is true ");
		hql.append(" order by grupo.ordem, ordem ");

		params.put("tipoProcessoId", tipoProcessoId);

		Query query = createQuery(hql, params);

		return query.list();
	}

	public List<String> findListByGrupoAndNome(List<Long> tiposProcessoIds) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select tc.grupo.nome || ' -> ' || tc.nome ");
		hql.append(getStartQuery());
		hql.append(" tc where 1=1 ");
		if(tiposProcessoIds != null && !tiposProcessoIds.isEmpty()) {
			hql.append(" and tc.grupo.tipoProcesso.id in ( '-1' ");
			for(Long tipoProcessoId : tiposProcessoIds){
				hql.append(", ?");
				params.add(tipoProcessoId);
			}
			hql.append(" ) ");
		}

		hql.append(" group by tc.grupo.nome, tc.nome ");
		hql.append(" order by tc.grupo.nome, tc.nome ");

		Query query = createQuery(hql, params);

		return (List<String>) query.list();
	}

	public List<Long> findIdsByNomesGrupoAndCampo(List<String> nomesGrupoAndCampo) {

		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<>();

		hql.append(" select tc.id ");
		hql.append(getStartQuery()).append(" tc ");
		hql.append(" where 1 = 2 ");
		for(String nomes : nomesGrupoAndCampo){
			String nome[] = nomes.split(" -> ");
			String nomeGrupo = nome[0];
			String nomeCampo = nome[1];
			hql.append(" or (tc.grupo.nome = ? and tc.nome = ?) ");
			params.add(nomeGrupo);
			params.add(nomeCampo);
		}
		hql.append(" order by tc.grupo.id, tc.id ");

		Query query = createQuery(hql, params);

		return (List<Long>) query.list();
	}

	public List<String> findNomesGrupoAndCampoByIds(List<Long> tipoCampoIds) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new LinkedHashMap<>();

		hql.append(" select tc.grupo.nome || ' -> ' || tc.nome ");
		hql.append(getStartQuery()).append(" tc ");
		hql.append(" where tc.id in (:tipoCampoIds) ");
		hql.append(" group by tc.grupo.nome, tc.nome ");
		hql.append(" order by tc.grupo.nome, tc.nome ");;

		params.put("tipoCampoIds", tipoCampoIds);

		Query query = createQuery(hql, params);

		return (List<String>) query.list();
	}

	public List<Long> findBasesInternas() {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new LinkedHashMap<>();

		hql.append(" select distinct tc.baseInterna.id ");
		hql.append(getStartQuery()).append(" tc ");
		hql.append(" where tc.baseInterna.id is not null ");

		Query query = createQuery(hql, params);

		return (List<Long>) query.list();
	}
}
