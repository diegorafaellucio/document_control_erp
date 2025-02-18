package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.BaseInterna;
import net.wasys.getdoc.domain.entity.BaseRegistro;
import net.wasys.getdoc.domain.entity.BaseRegistroValor;
import net.wasys.getdoc.domain.vo.filtro.BaseRegistroFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.ddd.HibernateRepository;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Query;
import org.hibernate.transform.AliasToEntityMapResultTransformer;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@SuppressWarnings("unchecked")
public class BaseRegistroRepository extends HibernateRepository<BaseRegistro> {

	private static long TIMEOUT_CACHE_1 = 1000 * 60 * 5;//5 min
	private static long TIMEOUT_CACHE_2 = 1000 * 60 * 2;//2 min

	public BaseRegistroRepository() {
		super(BaseRegistro.class);
	}

	public BaseRegistro findByBaseInternaIdAndChaveUnicidade(Long baseInternaId, String chaveUnicidade) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select b ");
		hql.append(getStartQuery()).append(" b ");
		hql.append(" where b.chaveUnicidade = ? ");
		params.add(chaveUnicidade);
		hql.append(" and b.baseInterna.id = ? ");
		params.add(baseInternaId);

		Query query = createQuery(hql.toString(), params);

		return (BaseRegistro) query.uniqueResult();
	}

	public int deleteNotIn(Long baseInternaId, Set<Long> idsUtilizados) {

		StringBuilder hql = new StringBuilder();

		hql.append(" delete ");
		hql.append(getStartQuery()).append(" b ");
		hql.append(" where 1=1 ");
		hql.append(" and b.baseInterna.id = :base_interna_id ");
		hql.append(" and b.id not in (:ids) ");

		Query query = createQuery(hql.toString());
		query.setParameter("base_interna_id", baseInternaId);
		query.setParameterList("ids", idsUtilizados);

		return query.executeUpdate();
	}

	public int countByFiltro(BaseRegistroFiltro filtro) {

		StringBuilder hql = new StringBuilder();

		hql.append(" select count(distinct b.id) ");
		hql.append(getStartQuery()).append(" b ");
		hql.append(" join ").append(BaseRegistroValor.class.getName()).append(" bv ");
		hql.append(" on b.id = bv.baseRegistro.id");

		Map<String, Object> params = makeQuery(filtro, hql);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue();

	}

	public List<Map<String, Object>> findByFiltro(BaseRegistroFiltro filtro, Integer first, Integer pageSize, boolean cache) {

		StringBuilder hql = new StringBuilder();

		hql.append(" select b.id, bv.id, bv.nome, bv.valor, b ");
		hql.append(" from ").append(BaseRegistro.class.getName()).append(" b ");
		hql.append(" join ").append(BaseRegistroValor.class.getName()).append(" bv ");
		hql.append(" on b.id = bv.baseRegistro.id ");

		Map<String, Object> params = makeQuery(filtro, hql);

		hql.append(" order by b.chaveUnicidade asc ");

		Query query = createQuery(hql.toString(), params);
		query.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);

		/* A query acima vai retornar vários base_registro_valores para cada base_registro de acordo com o número de colunas, portanto é
		 * necessário corrigir a paginação para retornar a quantidade correta de base_registro_valores por cada base_registro.
		 * */
		Integer qntColunas = filtro.getQntColunas();
		corrigirPaginacao(qntColunas, first, pageSize, query);

		if(cache) {
			List<Map<String, Object>> list = listCache(params, query, TIMEOUT_CACHE_2);
			return list;
		}
		else {
			return query.list();
		}
	}

	public List<BaseRegistro> findRegistroByFiltro(BaseRegistroFiltro filtro) {

		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" b ");
		Map<String, Object> params = makeQuery(filtro, hql);

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}

	public List<Map<String, Object>> findValoresByBaseInternaAndChaveUnicidade(Long baseInternaId, String chaveUnicidade) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append("select b.id, bv.id, bv.nome, bv.valor, b ");
		hql.append(getStartQuery()).append(" b ");
		hql.append(" join " ).append(BaseRegistroValor.class.getName()).append(" bv ");
		hql.append(" on b.id = bv.baseRegistro.id ");
		hql.append(" and b.baseInterna.id = :baseInternaId ");
		hql.append(" and upper(b.chaveUnicidade) like :chaveUnicidade ");

		params.put("baseInternaId", baseInternaId);
		params.put("chaveUnicidade", chaveUnicidade == null ? "" : chaveUnicidade.toUpperCase());

		hql.append(" order by bv.id");

		Query query = createQuery(hql.toString(), params);
		query.setResultTransformer(AliasToEntityMapResultTransformer.INSTANCE);

		return listCache(params, query, TIMEOUT_CACHE_1);
	}

	public List<BaseRegistro> findByBaseInternaId(Long baseInternaId) {

		List<Object> params = new ArrayList<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" b ");
		hql.append(" where b.baseInterna.id = ? ");
		params.add(baseInternaId);

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}

	private void corrigirPaginacao(Integer qntColunas, Integer first, Integer pageSize, Query query) {

		if(first != null) {
			query.setFirstResult(first * qntColunas);
		}

		if(pageSize != null) {
			query.setMaxResults(pageSize * qntColunas);
		}
	}

	private Map<String, Object> makeQuery(BaseRegistroFiltro filtro, StringBuilder hql) {

		BaseInterna baseInterna = filtro.getBaseInterna();
		Long baseInternaId = baseInterna.getId();
		String chaveUnicidade = filtro.getChaveUnicidade();
		Map<String, String[]> camposFiltro = filtro.getCamposFiltro();
		Boolean ativo = filtro.getAtivo();
		boolean usarLikeCampos = filtro.isUsarLikeCampos();

		Map<String, Object> params = new LinkedHashMap<>();

		hql.append(" where 1=1 ");

		if(ativo != null) {
			hql.append(" and b.ativo is ").append(ativo ? "" : "not").append(" true ");
		}

		if(baseInternaId != null) {
			hql.append(" and b.baseInterna.id = :baseInternaId ");
			params.put("baseInternaId", baseInternaId);
		}

		if(!StringUtils.isBlank(chaveUnicidade)) {
			hql.append(" and upper(b.chaveUnicidade) = :chaveUnicidade ");
			chaveUnicidade = chaveUnicidade.toUpperCase();
			params.put("chaveUnicidade", chaveUnicidade);
		}

		if(camposFiltro != null && !camposFiltro.isEmpty()) {
			camposFiltro.forEach((nome, valores) -> {
				hql.append(" and ( ");
				hql.append(" 	select count(*) from ").append(BaseRegistroValor.class.getName()).append(" brv ");
				hql.append(" 	where brv.baseRegistro.id = b.id ");
				hql.append(" 	and brv.nome = '").append(nome).append("' ");
				hql.append(" 	and ( 1 != 1 ");
				for (String valor : valores) {
					if(usarLikeCampos) {
						hql.append(" or brv.valor like '%").append(valor).append("%'");
					} else {
						hql.append(" or brv.valor = '").append(valor).append("'");
					}
				}
				hql.append(" 	) ");
				hql.append(" ) > 0 ");
			});
		}

		return params;
	}

	public Date getUltimaDataAtualizacao(Long baseInternaId) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> param = new LinkedHashMap<>();

		hql.append(" select max(br.dataAtualizacao) from ").append(clazz.getName()).append(" br ");
		hql.append(" where br.baseInterna.id = :baseInternaId ");
		param.put("baseInternaId", baseInternaId);

		Query query = createQuery(hql.toString(), param);
		return (Date) query.uniqueResult();
	}

	public BaseRegistro getByChaveUnicidade(Long baseInternaId, String chaveUnicidade) {

		try {
			StringBuilder hql = new StringBuilder();
			Map<String, Object> param = new LinkedHashMap<>();

			hql.append(" from ").append(clazz.getName()).append(" br ");
			hql.append(" where br.baseInterna.id = :baseInternaId ");
			hql.append(" and br.chaveUnicidade = :chaveUnicidade ");

			param.put("baseInternaId", baseInternaId);
			param.put("chaveUnicidade", chaveUnicidade);

			Query query = createQuery(hql.toString(), param);
			return (BaseRegistro) query.uniqueResult();
		}
		catch (NonUniqueResultException e) {
			String message = DummyUtils.getExceptionMessage(e);
			DummyUtils.systraceThread("falha ao buscar pela chave de unicidade: " + message + ". Base Interna: " + baseInternaId + ". chave: " + chaveUnicidade, LogLevel.ERROR);
			throw e;
		}
	}

	public String getLabel(Long baseInternaId, String chaveUnicidade) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> param = new LinkedHashMap<>();

		hql.append(" select brv.valor from ").append(BaseRegistroValor.class.getName()).append(" brv ");
		hql.append(" where brv.baseRegistro.baseInterna.id = :baseInternaId ");
		hql.append(" and brv.baseRegistro.chaveUnicidade = :chaveUnicidade ");
		hql.append(" and brv.nome = brv.baseRegistro.baseInterna.colunaLabel ");
		hql.append(" order by brv.id ");
		param.put("baseInternaId", baseInternaId);
		param.put("chaveUnicidade", chaveUnicidade);

		Query query = createQuery(hql.toString(), param);
		query.setFirstResult(0);
		query.setMaxResults(1);
		return (String) query.uniqueResult();
	}

	public List<String> findChaveUnicidadeByPesquisa(Long baseInternaId, String valorPesquisa) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> param = new LinkedHashMap<>();

		hql.append(" select brv.baseRegistro.chaveUnicidade ");
		hql.append(" from ").append(BaseRegistroValor.class.getName()).append(" brv ");
		hql.append(" where brv.baseRegistro.baseInterna.id = :baseInternaId ");
		hql.append(" and brv.nome = brv.baseRegistro.baseInterna.colunaLabel ");
		hql.append(" and upper(brv.valor) like upper(:valorPesquisa) ");
		hql.append(" order by brv.id ");

		param.put("baseInternaId", baseInternaId);
		param.put("valorPesquisa", "%" + valorPesquisa + "%");

		Query query = createQuery(hql.toString(), param);
		return (List<String>) query.list();
	}
}