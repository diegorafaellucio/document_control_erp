package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.ConfiguracaoGeracaoRelatorio;
import net.wasys.getdoc.domain.enumeration.TipoConfiguracaoRelatorio;
import net.wasys.getdoc.domain.vo.filtro.ConfiguracaoGeracaoRelatorioFiltro;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@SuppressWarnings("unchecked")
@Repository
public class ConfiguracaoGeracaoRelatorioRepository extends HibernateRepository<ConfiguracaoGeracaoRelatorio> {

	public ConfiguracaoGeracaoRelatorioRepository() {
		super(ConfiguracaoGeracaoRelatorio.class);
	}

	public List<ConfiguracaoGeracaoRelatorio> findAtivosEntreHorarios(String inicio, String fim, boolean isViradaDeDia) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" cgr ");
		hql.append(" where 1=1 ");

		if (!isViradaDeDia) {
			hql.append(" and cgr.horario > :inicio and cgr.horario <= :fim ");
		}
		else {

			hql.append(" and (cgr.horario > :inicio and cgr.horario <= :fim) or (cgr.horario >= :primeiroHorarioDia and cgr.horario <= :fim or cgr.horario > :inicio and cgr.horario <= :ultimoHorarioDoDia) ");

			params.put("primeiroHorarioDia", "00:00");
			params.put("ultimoHorarioDoDia", "23:59");
		}

		hql.append(" and cgr.ativo is true ");

		params.put("inicio", inicio);
		params.put("fim", fim);

		Query query = createQuery(hql, params);

		return query.list();
	}

	public int countByFiltro(ConfiguracaoGeracaoRelatorioFiltro filtro) {

		StringBuilder hql = new StringBuilder();

		hql.append(" select count(*) ");
		hql.append(" from ").append(clazz.getName()).append(" cgr ");

		Map<String, Object> params = makeQuery(filtro, hql);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue();
	}

	private Map<String, Object> makeQuery(ConfiguracaoGeracaoRelatorioFiltro filtro, StringBuilder hql) {

		hql.append(" where 1=1 ");
		Map<String, Object> params = new HashMap<>();

		String nome = filtro.getNome();
		TipoConfiguracaoRelatorio tipo = filtro.getTipo();
		Boolean ativo = filtro.getAtivo();

		if (isNotBlank(nome)) {

			hql.append(" and cgr.nome like :nome ");
			params.put("nome", "%" + nome + "%");
		}

		if (tipo != null) {

			hql.append(" and cgr.tipo = :tipo ");
			params.put("tipo", tipo);
		}

		if (ativo != null) {

			hql.append(" and cgr.ativo = :ativo ");
			params.put("ativo", ativo);
		}

		return params;
	}

	public List<ConfiguracaoGeracaoRelatorio> findByFiltro(ConfiguracaoGeracaoRelatorioFiltro filtro, Integer inicio, Integer max) {

		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" cgr ");

		Map<String, Object> params = makeQuery(filtro, hql);

		hql.append(" order by cgr.horario, cgr.nome");

		Query query = createQuery(hql.toString(), params);

		if (inicio != null) {
			query.setFirstResult(inicio);
		}

		if (max != null) {
			query.setMaxResults(max);
		}

		return query.list();
	}

	public boolean existsByNome(String nome) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select count(*) from ").append(clazz.getName()).append(" cgr ");
		hql.append(" where cgr.nome = :nome ");
		params.put("nome", nome);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue() > 0;
	}
}
