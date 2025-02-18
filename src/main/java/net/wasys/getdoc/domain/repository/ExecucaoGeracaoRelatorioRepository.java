package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.ExecucaoGeracaoRelatorio;
import net.wasys.getdoc.domain.vo.ExecucaoGeracaoRelatorioVO;
import net.wasys.getdoc.domain.vo.filtro.ExecucaoGeracaoRelatorioFiltro;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.Query;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@SuppressWarnings("unchecked")
public class ExecucaoGeracaoRelatorioRepository extends HibernateRepository<ExecucaoGeracaoRelatorio> {

	public ExecucaoGeracaoRelatorioRepository() {
		super(ExecucaoGeracaoRelatorio.class);
	}

	public int countByFiltro(ExecucaoGeracaoRelatorioFiltro filtro) {

		StringBuilder hql = new StringBuilder();

		hql.append(" select count(*) ");
		hql.append(" from ").append(clazz.getName()).append(" egr ");

		Map<String, Object> params = makeQuery(filtro, hql);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue();
	}

	public List<ExecucaoGeracaoRelatorio> findByFiltro(ExecucaoGeracaoRelatorioFiltro filtro, Integer inicio, Integer max) {

		StringBuilder hql = new StringBuilder();
		hql.append(startQuery()).append(" egr ");

		Query query = findByFiltro(hql, filtro, inicio, max);

		return query.list();
	}

	public List<ExecucaoGeracaoRelatorioVO> findVOsByFiltro(ExecucaoGeracaoRelatorioFiltro filtro, Integer inicio, Integer max) {

		StringBuilder hql = new StringBuilder();

		hql.append(" select ");
		hql.append("    egr.id as id, ");
		hql.append("    egr.configuracaoGeracaoRelatorio.id as configuracaoGeracaoRelatorioId, ");
		hql.append("    egr.configuracaoGeracaoRelatorio.nome as nome, ");
		hql.append("    egr.configuracaoGeracaoRelatorio.horario as horario, ");
		hql.append("    egr.caminhoArquivo as caminhoArquivo, ");
		hql.append("    egr.sucesso as sucesso, ");
		hql.append("    egr.observacao as observacao, ");
		hql.append("    egr.inicio as inicio, ");
		hql.append("    egr.fim as fim, ");
		hql.append("    egr.extensao as extensao  ");
		hql.append(" from ").append(clazz.getName()).append(" egr ");

		Query query = findByFiltro(hql, filtro, inicio, max);

		query.setResultTransformer(Transformers.aliasToBean(ExecucaoGeracaoRelatorioVO.class));

		return query.list();
	}

	private Query findByFiltro(StringBuilder hql, ExecucaoGeracaoRelatorioFiltro filtro, Integer inicio, Integer max) {

		Map<String, Object> params = makeQuery(filtro, hql);

		String ordem = filtro.getOrdem();
		ordem = ordem != null ? ordem : "egr.inicio desc";
		hql.append(" order by ").append(ordem);

		Query query = createQuery(hql.toString(), params);

		if (inicio != null) {
			query.setFirstResult(inicio);
		}

		if (max != null) {
			query.setMaxResults(max);
		}

		return query;
	}

	private Map<String, Object> makeQuery(ExecucaoGeracaoRelatorioFiltro filtro, StringBuilder hql) {

		hql.append(" where 1=1 ");
		Map<String, Object> params = new HashMap<>();

		Date dataInicioMenorQue = filtro.getDataInicioMenorQue();

		if (dataInicioMenorQue != null) {

			hql.append(" and egr.inicio < :dataCorte ");
			params.put("dataCorte", dataInicioMenorQue);
		}

		return params;
	}
}