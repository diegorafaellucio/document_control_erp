package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.ModeloDocumento;
import net.wasys.getdoc.domain.entity.RelatorioTipificacao;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.enumeration.TipoTipificacao;
import net.wasys.getdoc.domain.vo.filtro.RelatorioTipificacaoFiltro;
import net.wasys.util.ddd.HibernateRepository;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@SuppressWarnings("unchecked")
public class RelatorioTipificacaoRepository extends HibernateRepository<RelatorioTipificacao> {

	public RelatorioTipificacaoRepository() {
		super(RelatorioTipificacao.class);
	}

	public int count() {

		Query query = createQuery(" select count(*) from " + clazz.getName());
		return ((Number) query.uniqueResult()).intValue();
	}

	public List<RelatorioTipificacao> findByFiltro(RelatorioTipificacaoFiltro filtro, Integer inicio, Integer max) {

		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" r ");

		Map<String, Object> params = makeQuery(filtro, hql);

		hql.append("order by r.id desc");

		Query query = createQuery(hql.toString(), params);

		if (inicio != null) {
			query.setFirstResult(inicio);
		}
		if (max != null) {
			query.setMaxResults(max);
		}

		return query.list();
	}

	public Integer countByFiltro(RelatorioTipificacaoFiltro filtro) {

		StringBuilder hql = new StringBuilder();

		hql.append("select count(*) from ").append(clazz.getName()).append(" r");

		Map<String, Object> params = makeQuery(filtro, hql);

		Query query = createQuery(hql.toString(), params);

		return ((Long) query.uniqueResult()).intValue();

	}

	private Map<String, Object> makeQuery(RelatorioTipificacaoFiltro filtro, StringBuilder hql) {

		Date dataInicio = filtro.getDataInicio();
		Date dataFim = filtro.getDataFim();
		TipoTipificacao tipoTipificacao = filtro.getTipoTipificacao();
		List<ModeloDocumento> modeloDocumentoList = filtro.getModeloDocumentoList();
		List<TipoProcesso> tipoProcessoList = filtro.getTipoProcessoList();

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

			hql.append(" or r.data between :dataInicio and :dataFim ");
			params.put("dataInicio", dataInicio);
			params.put("dataFim", dataFim);

			hql.append(" ) ");
		}

		if (tipoTipificacao != null) {
			hql.append(" AND r.tipoTipificacao = :tipoTipificacao ");
			params.put("tipoTipificacao", tipoTipificacao);
		}

		if(modeloDocumentoList != null && !modeloDocumentoList.isEmpty()) {
			int idx = 0;
			hql.append(" and r.documento.modeloDocumento in (");
			for(ModeloDocumento modeloDocumento : modeloDocumentoList) {
				hql.append(":modeloDocumentoList_" + idx);
				params.put("modeloDocumentoList_" + idx, modeloDocumento);
			}
			hql.append(")");
		}

		if(tipoProcessoList != null && !tipoProcessoList.isEmpty()) {
			int idx = 0;
			hql.append(" and r.documento.processo.tipoProcesso in (");
			for(TipoProcesso tipoProcesso : tipoProcessoList) {
				hql.append(":tipoProcessoList_" + idx);
				params.put("tipoProcessoList_" + idx, tipoProcesso);
			}
			hql.append(")");
		}

		return params;
	}
}