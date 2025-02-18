package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.ProximaSituacao;
import net.wasys.getdoc.domain.entity.Situacao;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.vo.filtro.SituacaoFiltro;
import net.wasys.util.ddd.HibernateRepository;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@SuppressWarnings("unchecked")
public class SituacaoRepository extends HibernateRepository<Situacao> {

	private static final long TIMEOUT_CACHE = 5 * 60 * 1000;//5 min

	public SituacaoRepository() {
		super(Situacao.class);
	}

	private Map<String, Object> makeQuery(SituacaoFiltro filtro, StringBuilder hql) {

		Long tipoProcessoId = filtro.getTipoProcessoId();
		Boolean ativa = filtro.getAtiva();
		List<StatusProcesso> status = filtro.getStatusProcesso();
		String nome = filtro.getNome();
		String nomeContem = filtro.getNomeContem();

		Map<String, Object> params = new HashMap<String, Object>();

		hql.append(" where 1=1 ");

		if(tipoProcessoId != null) {
			hql.append(" and s.tipoProcesso.id = :tipoProcessoId ");
			params.put("tipoProcessoId", tipoProcessoId);
		}

		if(ativa != null) {
			hql.append(" and s.ativa = :ativa ");
			params.put("ativa", ativa);
		}

		if(status != null) {
			hql.append(" and ( 1<>1 ");
			status.forEach( s -> hql.append(" or s.status = ").append("'" + s + "'"));
			hql.append(" )");
		}

		if(StringUtils.isNotBlank(nome)) {
			hql.append(" and s.nome = :nome ");
			params.put("nome", nome);
		}

		if(StringUtils.isNotBlank(nomeContem)) {
			hql.append(" and s.nome like :nomeContem");
			params.put("nomeContem", "%" + nomeContem + "%");
		}

		return params;
	}

	public List<Situacao> findByFiltro(SituacaoFiltro filtro, Integer inicio, Integer max) {

		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" s ");

		Map<String, Object> params = makeQuery(filtro, hql);

		hql.append(" order by s.nome ");

		Query query = createQuery(hql.toString(), params);

		if(inicio != null) {
			query.setFirstResult(inicio);
		}

		if(max != null) {
			query.setMaxResults(max);
		}

		return query.list();
	}

	public int count(SituacaoFiltro filtro) {

		StringBuilder hql = new StringBuilder();

		hql.append(" select count(*) from ").append(clazz.getName()).append(" s ");

		Map<String, Object> params = makeQuery(filtro, hql);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue();
	}

	public List<Situacao> findAtivasToSelect(StatusProcesso status, Long tipoProcessoId) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select new ").append(Situacao.class.getName()).append("(s.id, s.nome, s.tipoProcesso.nome) ");
		hql.append(" from ").append(clazz.getName()).append(" s ");
		hql.append(" where s.ativa is true ");
		if(status != null) {
			hql.append(" and s.status = :status ");
			params.put("status", status);
		}

		if(tipoProcessoId != null) {
			hql.append(" and s.tipoProcesso.id = :tipoProcessoId ");
			params.put("tipoProcessoId", tipoProcessoId);
		}

		hql.append(" order by s.nome, s.tipoProcesso.nome ");

		Query query = createQuery(hql.toString(), params);

		List<Situacao> list = listCache(params, query, TIMEOUT_CACHE);
		return list;
	}

	public List<Situacao> findAtivas(StatusProcesso status, Long tipoProcessoId) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" s ");
		hql.append(" where s.ativa is true ");
		if(status != null) {
			/*hql.append(" and ( ");
			hql.append(" 	select count(*) from s.statusProcessos sp where sp.status = ? ");
			hql.append(" ) > 0 ");*/
			hql.append(" and s.status = ? ");
			params.add(status);
		}

		if(tipoProcessoId != null) {
			hql.append(" and s.tipoProcesso.id = ? ");
			params.add(tipoProcessoId);
		}

		hql.append(" order by s.nome, s.tipoProcesso.nome ");

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}

	public List<Situacao> findByIds(List<Long> ids) {

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

	public Situacao getFirstByTipoProcesso(Long tipoProcessoId) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" s ");
		hql.append(" where s.ativa is true ");

		if(tipoProcessoId != null) {
			hql.append(" and s.tipoProcesso.id = :tipoProcessoId ");
			params.put("tipoProcessoId", tipoProcessoId);
		}

		hql.append(" order by s.nome, s.tipoProcesso.nome ");

		Query query = createQuery(hql.toString(), params);
		query.setFirstResult(0);
		query.setMaxResults(1);

		return (Situacao) query.uniqueResult();
	}

	public List<Situacao> getProximasByAtual(Long situacaoAtualId){
		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append("select s from ").append(clazz.getName()).append(" s, ");
		hql.append(ProximaSituacao.class.getName()).append(" ps ");
		hql.append(" where s.ativa is true ");
		hql.append(" and ");
		hql.append(" ps.atual.id = :situacaoAtualId");
		params.put("situacaoAtualId", situacaoAtualId);
		hql.append(" and ");
		hql.append(" s.id = ps.proxima.id ");

		hql.append(" order by s.nome, s.tipoProcesso.nome ");

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}

	public List<Situacao> findByNome(String nome) {
		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" s ");
		hql.append(" where s.ativa is true ");
		hql.append(" and s.nome like :nome ");
		params.put("nome", nome);

		hql.append(" order by s.nome, s.tipoProcesso.nome ");

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}

	public Situacao getByNome(Long tipoProcessoId, String nome) {
		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" s ");
		hql.append(" where s.ativa is true ");
		hql.append(" and s.tipoProcesso.id = :tipoProcessoID ");
		hql.append(" and s.nome like :nome ");
		params.put("tipoProcessoID", tipoProcessoId);
		params.put("nome", nome);

		Query query = createQuery(hql.toString(), params);

		return (Situacao) query.uniqueResult();
	}

	public Situacao getByFinalNome(Long tipoProcessoId, String finalDoNome) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" s ");
		hql.append(" where s.ativa is true ");
		hql.append(" and s.tipoProcesso.id = :tipoProcessoID ");
		hql.append(" and upper(s.nome) like :nome ");
		params.put("tipoProcessoID", tipoProcessoId);
		params.put("nome", "%" + finalDoNome.toUpperCase());

		Query query = createQuery(hql.toString(), params);

		return (Situacao) query.uniqueResult();
	}

	public List<Situacao> findByIdsAndTipoProcesso(List<Long> contratoCanceladoIds, TipoProcesso tipoProcesso) {
		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();
		Long tipoProcessoId = tipoProcesso.getId();

		hql.append(getStartQuery()).append(" s ");
		hql.append(" where s.ativa is true ");
		hql.append(" and  1 <> 1 ");
		contratoCanceladoIds.forEach( id -> {
			hql.append("or (s.id = ").append(id).append(" and s.tipoProcesso.id = ").append(tipoProcessoId).append(")");
		});

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}
}
