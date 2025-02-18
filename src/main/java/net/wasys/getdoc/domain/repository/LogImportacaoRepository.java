package net.wasys.getdoc.domain.repository;

import java.util.*;

import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.enumeration.StatusImportacao;
import net.wasys.getdoc.domain.enumeration.TipoImportacao;
import net.wasys.getdoc.domain.vo.filtro.ProcessoFiltro;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.primefaces.model.SortOrder;
import org.springframework.stereotype.Repository;

import net.wasys.getdoc.domain.entity.LogImportacao;
import net.wasys.getdoc.domain.vo.filtro.LogImportacaoFiltro;
import net.wasys.util.ddd.HibernateRepository;

@Repository
public class LogImportacaoRepository extends HibernateRepository<LogImportacao> {

	public LogImportacaoRepository() {
		super(LogImportacao.class);
	}

	public int countByFiltro(LogImportacaoFiltro filtro) {

		StringBuilder hql = new StringBuilder();

		hql.append(" select count(*) ").append(getStartQuery()).append(" l ");

		Map<String, Object> params = makeWhere(filtro, hql);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue();
	}

	@SuppressWarnings("unchecked")
	public List<LogImportacao> findByFiltro(LogImportacaoFiltro filtro, Integer first, Integer pageSize) {

		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" l ");
		Map<String, Object> params = makeWhere(filtro, hql);
		makeOrderBy(filtro, hql);

		Query query = createQuery(hql.toString(), params);

		if (first != null) {
			query.setFirstResult(first);
		}

		if (pageSize != null) {
			query.setMaxResults(pageSize);
		}

		return query.list();
	}

	public LogImportacao getLastByFiltro(LogImportacaoFiltro filtro) {

		StringBuilder hql = new StringBuilder();

		hql.append(" SELECT l FROM ").append(clazz.getName()).append(" l ");

		Map<String, Object> params = makeWhere(filtro, hql);
		hql.append(" order by l.id desc ");

		Query query = createQuery(hql.toString(), params);
		query.setMaxResults(1);

		return (LogImportacao) query.uniqueResult();
	}

	private Map<String, Object> makeWhere(LogImportacaoFiltro filtro, StringBuilder hql) {

		String usuario = filtro.getUsuario();
		TipoImportacao tipoImportacao = filtro.getTipoImportacao();
		Date dataInicio = filtro.getDataInicio();
		Date dataFim = filtro.getDataFim();
		TipoProcesso tipoProcesso = filtro.getTipoProcesso();
		StatusImportacao status = filtro.getStatus();
		String nomeArquivo = filtro.getNomeArquivo();

		Map<String, Object> params = new LinkedHashMap<>();

		hql.append(" where 1=1 ");

		if (dataInicio != null && dataFim != null) {

			dataInicio = DateUtils.truncate(dataInicio, Calendar.DAY_OF_MONTH);

			Calendar dataFimC = Calendar.getInstance();
			dataFimC.setTime(dataFim);
			dataFimC.add(Calendar.DAY_OF_MONTH, 1);
			dataFimC = DateUtils.truncate(dataFimC, Calendar.DAY_OF_MONTH);
			dataFimC.add(Calendar.MILLISECOND, -1);

			dataFim = dataFimC.getTime();

			hql.append(" and ( 1 != 1 ");

			hql.append(" or l.data between :dataInicio and :dataFim ");
			params.put("dataInicio", dataInicio);
			params.put("dataFim", dataFim);

			hql.append(" ) ");
		}

		if (!StringUtils.isBlank(usuario)) {
			hql.append(" and upper(l.usuario.nome) like :like");
			params.put("like", "%" + usuario.toUpperCase() + "%");
		}

		if (tipoImportacao != null) {
			hql.append(" and l.tipo = :tipoImportacao");
			params.put("tipoImportacao", tipoImportacao);
		}

		if(tipoProcesso != null) {
			hql.append(" and l.tipoProcesso = :tipoProcesso");
			params.put("tipoProcesso", tipoProcesso);
		}

		if(status != null) {
			hql.append(" and l.status = :status");
			params.put("status", status);
		}

		if(StringUtils.isNotBlank(nomeArquivo)) {
			hql.append(" and l.nomeArquivo like :nomeArquivo");
			params.put("nomeArquivo", "%" + nomeArquivo.toUpperCase() + "%");
		}

		return params;
	}

	private void makeOrderBy(LogImportacaoFiltro filtro, StringBuilder hql) {

		String campoOrdem = filtro.getCampoOrdem();
		if (campoOrdem != null) {

			SortOrder ordem = filtro.getOrdem();
			String ordemStr = SortOrder.DESCENDING.equals(ordem) ? " desc " : " asc ";

			hql.append(" order by ").append(campoOrdem).append(ordemStr);
		} else {
			hql.append(" order by data desc ");
		}
	}

	public boolean temImportacaoEmProcessamento() {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new LinkedHashMap<>();

		hql.append("select count(*) from ").append(clazz.getName()).append(" l");
		hql.append(" where l.status = :status");
		params.put("status", StatusImportacao.PROCESSANDO);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue() > 0;
	}
}