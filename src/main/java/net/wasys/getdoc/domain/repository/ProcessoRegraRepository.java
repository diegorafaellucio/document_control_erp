package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.StatusDocumento;
import net.wasys.getdoc.domain.enumeration.StatusProcessoRegra;
import net.wasys.getdoc.domain.enumeration.TipoConsultaExterna;
import net.wasys.getdoc.domain.enumeration.TipoExecucaoRegra;
import net.wasys.getdoc.domain.vo.filtro.ProcessoRegraFiltro;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@SuppressWarnings("unchecked")
public class ProcessoRegraRepository extends HibernateRepository<ProcessoRegra> {

	public ProcessoRegraRepository() {
		super(ProcessoRegra.class);
	}

	public List<ProcessoRegra> findLasts(ProcessoRegraFiltro filtro) {
		StringBuilder hql = new StringBuilder();
		hql.append(startQuery()).append(" pr2 ");
		hql.append(" left outer join fetch pr2.regra ");
		hql.append(" where pr2.id in ( ");
		hql.append(" 	select max(pr.id) from ").append(clazz.getName()).append(" pr ");
		Map<String, Object> params = makeQuery(filtro, hql);
		hql.append(" 	group by pr.regra.id ");
		hql.append(" ) ");
		hql.append(" order by pr2.regra.nome ");

		Query query = createQuery(hql, params);
		return query.list();
	}

	private List<?> findLasts(ProcessoRegraFiltro filtro, StringBuilder hql) {
		hql.append(" left outer join fetch pr2.regra ");
		hql.append(" where pr2.id in ( ");
		hql.append(" 	select max(pr.id) from ").append(clazz.getName()).append(" pr ");
		Map<String, Object> params = makeQuery(filtro, hql);
		hql.append(" 	group by pr.regra.id ");
		hql.append(" ) ");
		hql.append(" order by pr2.regra.nome ");

		Query query = createQuery(hql, params);
		return query.list();
	}

	public ProcessoRegra getById(Long id) {
		StringBuilder hql = new StringBuilder();
		hql	.append("select pr from ");
		hql	.append(clazz.getName()).append(" pr ");
		hql	.append("where id = :id ");
		Query query = createQuery(hql.toString());
		query.setLong("id", id);
		return (ProcessoRegra) query.uniqueResult();
	}

	public List<ProcessoRegra> findByFiltro(ProcessoRegraFiltro filtro, Integer inicio, Integer max) {
		StringBuilder hql = new StringBuilder();

		hql.append("select distinct pr ");
		hql.append(getStartQuery());
		hql.append(" pr ");

		Map<String, Object> params = makeQuery(filtro, hql);
		hql.append(" order by pr.id DESC");

		Query query = createQuery(hql.toString(), params);

		if(inicio != null && max != null) {
			query.setFirstResult(inicio);
			query.setMaxResults(max);
		}

		List<ProcessoRegra> list = query.list();
		Set<ProcessoRegra> set = new LinkedHashSet<>(list);
		return new ArrayList<>(set);
	}

	public int countByFiltro(ProcessoRegraFiltro filtro) {
		StringBuilder hql = new StringBuilder();
		hql.append(" select count(*) from ").append(clazz.getName()).append(" pr ");

		Map<String, Object> params = makeQuery(filtro, hql);

		Query query = createQuery(hql.toString(), params);
		return ((Number) query.uniqueResult()).intValue();
	}

	private Map<String, Object> makeQuery(ProcessoRegraFiltro filtro, StringBuilder hql) {

		Map<String, Object> params = new HashMap<String, Object>();
		Long processoId = filtro.getProcessoId();
		Long regraId = filtro.getRegraId();
		String regraNome = filtro.getRegraNome();
		Long processoIgnorarId = filtro.getProcessoIgnorarId();
		Boolean possuiConsultaExterna = filtro.getPossuiConsultaExterna();
		TipoProcesso tipoProcesso = filtro.getTipoProcesso();
		TipoExecucaoRegra tipoExecucao = filtro.getTipoExecucao();
		List<StatusProcessoRegra> statusList = filtro.getStatusList();
		Long situacaoId = filtro.getSituacaoId();
		Boolean decisaoFluxo = filtro.getDecisaoFluxo();
		List<Long> regrasIds = filtro.getRegrasIds();
		boolean reprocessaRegraEditarCampos = filtro.isReprocessaRegraEditarCampos();
		boolean reprocessaRegraAtualizarDocumentos = filtro.isReprocessaRegraAtualizarDocumentos();
		TipoConsultaExterna tipoConsultaExterna = filtro.getTipoConsultaExterna();
		Subperfil subperfilPermitido = filtro.getSubperfilPermitido();
		Boolean desconsiderarProcessoComDocumentoTipificando = filtro.getDesconsiderarProcessoComDocumentoTipificando();
		boolean isAtivo = filtro.isAtivo();

		hql.append(" where 1=1 ");

		if(processoId != null) {
			hql.append(" and pr.processo.id = :processoId ");
			params.put("processoId", processoId);
		}

		if(subperfilPermitido != null) {
			hql.append(" and (select count(*) from ").append(RegraSubperfil.class.getName()).append(" rs");
			hql.append(" where rs.subperfil = :subperfil and rs.regra = pr.regra) > 0");
			params.put("subperfil", subperfilPermitido);
		}

		if(statusList != null) {
			hql.append(" and pr.status in (:statusList)");
			params.put("statusList", statusList);
		}

		if(processoIgnorarId != null) {
			hql.append(" and pr.processo.id != :processoIgnorarId ");
			params.put("processoIgnorarId", processoIgnorarId);
		}

		if(regraId != null) {
			hql.append(" and pr.regra.id = :regraId ");
			params.put("regraId", regraId);
		}

		if(regrasIds != null && !regrasIds.isEmpty()) {
			hql.append(" and pr.regra.id in ( -1 ");
			for (Long id : regrasIds) {
				hql.append(", ").append(id);
			}
			hql.append(" ) ");
		}

		if(regraNome != null && !regraNome.equals("")) {
			String lowerRegraNome = regraNome.toLowerCase();
			hql.append(" and lower(pr.regra.nome) = :lowerRegraNome ");
			params.put("lowerRegraNome", lowerRegraNome);
		}

		if(possuiConsultaExterna != null) {
			hql.append(" and ( ");
			hql.append(" 	select count(*) from ").append(SubRegra.class.getName()).append(" sr ");
			hql.append(" 	where sr.linha.regra.id = pr.regra.id and sr.consultaExterna is not null ");
			hql.append(" ) ").append(possuiConsultaExterna ? " > 0 " : " = 0 ");
		}

		if(desconsiderarProcessoComDocumentoTipificando != null && desconsiderarProcessoComDocumentoTipificando) {
			hql.append(" and ( ");
			hql.append(" 	select count(d.id) from ").append(Documento.class.getName()).append(" d ");
			hql.append(" 	where d.processo.id = pr.processo.id ");
			hql.append(" 	and d.nome = :documentoTipificando ");
			hql.append(" 	and d.status = :statusProcessando ");
			hql.append(" ) = 0 ");
			params.put("statusProcessando", StatusDocumento.PROCESSANDO);
			params.put("documentoTipificando", Documento.NOME_TIFICANDO);
		}

		if(tipoProcesso != null) {
			hql.append(" and pr.processo.tipoProcesso.id = :tipoProcessoId ");
			params.put("tipoProcessoId", tipoProcesso.getId());
		}

		if(tipoExecucao != null) {
			hql.append(" and pr.regra.tipoExecucao = :tipoExecucao ");
			params.put("tipoExecucao", tipoExecucao);
		}

		if(situacaoId != null) {
			hql.append(" and pr.regra.situacao.id = :situacaoId ");
			params.put("situacaoId", situacaoId);
		}

		if(decisaoFluxo != null) {
			hql.append(" and pr.regra.decisaoFluxo ").append(decisaoFluxo ? " = 1 " : " is null ");
		}

		if(reprocessaRegraEditarCampos){
			hql.append(" and pr.regra.reprocessaEditarCampos = true ");
		}

		if(reprocessaRegraAtualizarDocumentos){
			hql.append(" and pr.regra.reprocessaAtualizarDocumentos = true ");
		}

		if(tipoConsultaExterna != null){
			hql.append(" and (");
			hql.append("	select count(*) from ").append(Regra.class.getName()).append(" r, ").append(RegraLinha.class.getName()).append(" rl, ").append(SubRegra.class.getName()).append(" sr ");
			hql.append("	where sr.consultaExterna = '").append(TipoConsultaExterna.BRSCAN).append("' and ");
			hql.append("	rl.regra.id = r.id and ");
			hql.append("	rl.id = sr.linha.id and ");
			hql.append("	pr.regra.id = r.id ");
			hql.append(") > 0 ");
		}

		if (isAtivo) {
			hql.append(" and pr.regra.ativa = true");
		}

		return params;
	}

	public List<String> getDistinctRegrasNomes() {
		StringBuilder hql = new StringBuilder();
		hql	.append("select distinct r.nome from ");
		hql	.append(clazz.getName()).append(" pr ");
		hql.append(", ").append(Regra.class.getName()).append(" r ");
		hql.append("where r.id = pr.regra.id");
		hql.append(" order by r.nome asc ");
		Query query = createQuery(hql.toString());
		return query.list();
	}

	public List<Object[]> findToExpurgo(Date dataCorte, int preservar, int max) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new LinkedHashMap<>();

		hql.append(" select pr.processo.id, pr.regra.id ");
		hql.append(" from ").append(clazz.getName()).append(" pr ");
		hql.append(" where pr.dataExecucao < :dataCorte ");
		hql.append(" group by pr.processo.id, pr.regra.id ");
		hql.append(" having count(*) > :preservar ");

		params.put("dataCorte", dataCorte);
		params.put("preservar", (long) preservar);

		Query query = createQuery(hql, params);

		query.setMaxResults(max);

		List<Object[]> list = query.list();
		return list;
	}

	public void expurgar(Long processoId, Long regraId, int preservar) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new LinkedHashMap<>();

		hql.append(" delete from processo_regra ");
		hql.append(" where processo_id = :processoId ");
		hql.append(" and regra_id = :regraId ");
		hql.append(" and id not in ( ");
		hql.append(" 	select pr.id ");
		hql.append(" 	from processo_regra pr ");
		hql.append(" 	where pr.processo_id = :processoId ");
		hql.append(" 	and pr.regra_id = :regraId ");
		hql.append(" 	order by pr.id desc limit :preservar ");
		hql.append(" ) ");

		params.put("processoId", processoId);
		params.put("regraId", regraId);
		params.put("preservar", preservar);

		Query query = createSQLQuery(hql, params);
		query.executeUpdate();
	}
}
