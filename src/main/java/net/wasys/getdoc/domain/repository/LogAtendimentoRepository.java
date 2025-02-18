package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.LogAtendimento;
import net.wasys.getdoc.domain.entity.Processo;
import net.wasys.getdoc.domain.entity.Situacao;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.StatusAtendimento;
import net.wasys.getdoc.domain.vo.filtro.LogAtendimentoFiltro;
import net.wasys.util.ddd.HibernateRepository;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@SuppressWarnings("unchecked")
public class LogAtendimentoRepository extends HibernateRepository<LogAtendimento> {

	public LogAtendimentoRepository() {
		super(LogAtendimento.class);
	}

	public int countByFiltro(LogAtendimentoFiltro filtro) {

		StringBuilder hql = new StringBuilder();

		hql.append("select count(*) from ").append(LogAtendimento.class.getName()).append(" la ");

		Map<String, Object> params = makeQuery(filtro, hql);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue();
	}

	public List<LogAtendimento> findByFiltro(LogAtendimentoFiltro filtro, Integer first, Integer pageSize) {

		StringBuilder hql = new StringBuilder();

		hql.append("from ").append(LogAtendimento.class.getName()).append(" la ");

		Map<String, Object> params = makeQuery(filtro, hql);

		hql.append(" order by la.inicio asc");

		Query query = createQuery(hql.toString(), params);

		if (first != null) {
			query.setFirstResult(first);
		}

		if (pageSize != null) {
			query.setMaxResults(pageSize);
		}

		return query.list();
	}

	private Map<String, Object> makeQuery(LogAtendimentoFiltro filtro, StringBuilder hql) {

		Map<String, Object> params = new HashMap<>();
		List<Long> statusLaboralIdList = filtro.getStatusLaboralIdList();
		List<Long> usuarioIdList = filtro.getUsuarioIdList();
		StatusAtendimento statusAtendimento = filtro.getStatusAtendimento();
		Boolean emAndamento = filtro.getEmAndamento();

		Date inicio = filtro.getInicio();
		Date fim = filtro.getFim();

		hql.append(" where 1=1 ");

		if (CollectionUtils.isNotEmpty(statusLaboralIdList)) {
			hql.append(" and la.statusLaboral.id in (:statusLaboralIdList) ");
			params.put("statusLaboralIdList", statusLaboralIdList);
		}

		if (CollectionUtils.isNotEmpty(usuarioIdList)) {
			hql.append(" and la.analista.id in ( :usuarioIdList ) ");
			params.put("usuarioIdList", usuarioIdList);
		}

		if (statusAtendimento != null) {
			hql.append(" and la.statusLaboral.statusAtendimento = :statusAtendimento ");
			params.put("statusAtendimento", statusAtendimento);
		}

		if (emAndamento != null) {
			if (emAndamento) {
				hql.append(" and la.fim is null ");
			}else {
				hql.append(" and la.fim is not null ");
			}
		}

		if (inicio != null && fim != null) {

			inicio = DateUtils.truncate(inicio, Calendar.DAY_OF_MONTH);

			Calendar dataFimC = Calendar.getInstance();
			dataFimC.setTime(fim);
			dataFimC.add(Calendar.DAY_OF_MONTH, 1);
			dataFimC = DateUtils.truncate(dataFimC, Calendar.DAY_OF_MONTH);
			dataFimC.add(Calendar.MILLISECOND, -1);

			fim = dataFimC.getTime();

			hql.append(" and ( 1 != 1 ");

			hql.append(" or la.inicio between :inicio and :fim ");
			params.put("inicio", inicio);
			params.put("fim", fim);

			hql.append(" ) ");
		}

		return params;
	}

	public LogAtendimento getUltimoLogAtendimento(Usuario analista) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" la ");
		hql.append(" where la.id = ( ");
		hql.append(" 	select max(la1.id) from ").append(clazz.getName()).append(" la1 ");
		hql.append(" 		where la1.analista.id = :analistaId ");
		hql.append(" 		and la1.fim is null) ");

		params.put("analistaId", analista.getId());

		Query query = createQuery(hql.toString(), params);

		return (LogAtendimento) query.uniqueResult();
	}

	public LogAtendimento getUltimoLogAtendimentoByAnalistaAndProcesso(Usuario analista, Processo processo) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" la ");
		hql.append(" where la.id = ( ");
		hql.append(" 	select max(la1.id) from ").append(clazz.getName()).append(" la1 ");
		hql.append(" 		where la1.analista.id = :analistaId ");
		hql.append(" 		and la1.processo.id = :processoId ");
		hql.append(" 		and la1.fim is null) ");

		params.put("analistaId", analista.getId());
		params.put("processoId", processo.getId());

		Query query = createQuery(hql.toString(), params);

		return (LogAtendimento) query.uniqueResult();
	}

	public List<Long> findIdsByFiltro(LogAtendimentoFiltro filtro) {

		StringBuilder hql = new StringBuilder("select la.id ");
		hql.append("from ").append(LogAtendimento.class.getName()).append(" la ");

		Map<String, Object> params = makeQuery(filtro, hql);

		hql.append(" order by id desc ");

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}

	public List<LogAtendimento> findByIds(List<Long> ids) {
		StringBuilder hql = new StringBuilder();
		hql.append(getStartQuery());

		hql.append(" where id in (-1");
		for (Long id : ids) {
			hql.append(", ").append(id);
		}
		hql.append(") ");

		hql.append(" order by id desc ");

		Query query = createQuery(hql.toString());
		query.setFetchSize(100);

		return query.list();
	}

	public void atualizarSituacaoFinal(LogAtendimento logAtendimento, Situacao situacao) {
		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<>();
		hql	.append("update ").append(clazz.getName()).append(" ");
		hql	.append("	set situacaoFinal.id = :situacaoId ");
		hql	.append("	where id = :logAtendimentoId ");

		params.put("situacaoId", situacao.getId());
		params.put("logAtendimentoId", logAtendimento.getId());
		Query query = createQuery(hql, params);
		query.executeUpdate();
	}
}
