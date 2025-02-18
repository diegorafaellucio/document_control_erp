package net.wasys.getdoc.domain.repository;

import java.util.*;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.util.google.cloud.Image;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import net.wasys.getdoc.domain.enumeration.StatusLogOcr;
import net.wasys.getdoc.domain.vo.RelatorioOcrVO;
import net.wasys.getdoc.domain.vo.filtro.LogOcrFiltro;
import net.wasys.util.ddd.HibernateRepository;

@Repository
@SuppressWarnings("unchecked")
public class LogOcrRepository extends HibernateRepository<LogOcr> {

	public LogOcrRepository() {
		super(LogOcr.class);
	}

	public void save(List<LogOcr> logs) {

		Session session = getSession();

		for (LogOcr log : logs) {

			session.save(log);
		}

		session.flush();
	}

	public LogOcr getLastLog(Long documentoId) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<>();

		hql.append(getStartQuery()).append(" l ");

		hql.append(" where l.id = ( ");
		hql.append(" 	select max(l2.id) from ").append(clazz.getName()).append(" l2 ");
		hql.append(" 	where l2.documentoId = :documentoId ");
		hql.append(" ) ");
		params.put("documentoId", documentoId);

		Query query = createQuery(hql.toString(), params);

		return (LogOcr) query.uniqueResult();
	}

	public List<Long> findIdsByFiltro(LogOcrFiltro filtro) {

		StringBuilder hql = new StringBuilder();
		hql.append(" select l.id from ").append(clazz.getName()).append(" l ");

		Query query = makeWhereByFiltro(hql, filtro, null, null, " l.data ");

		return query.list();
	}

	private Query makeWhereByFiltro(StringBuilder hql, LogOcrFiltro filtro, Integer inicio, Integer max, String order) {

		Date dataInicio = filtro.getDataInicio();
		Date dataFim = filtro.getDataFim();
		Boolean preparada = filtro.getPreparada();
		List<StatusLogOcr> statusList = filtro.getStatusList();

		List<Object> params = new ArrayList<Object>();
		hql.append(" where 1=1 ");

		if(dataInicio != null) {
			hql.append(" and l.data >= ? ");
			params.add(dataInicio);
		}

		if(dataFim != null) {
			hql.append(" and l.data <= ? ");
			params.add(dataFim);
		}

		if(statusList != null && !statusList.isEmpty()) {
			hql.append(" and l.statusOcr in ( '-1' ");
			for (StatusLogOcr status : statusList) {
				hql.append(", '").append(status).append("'");
			}
			hql.append(" ) ");
		}

		if(preparada != null) {

			hql.append(" and ( ");
			hql.append(" 	select count(*) from Imagem i where i.documento.id = l.documentoId and i.preparada is ?");
			hql.append(" ) = 0 ");
			params.add(!preparada);
		}

		if(order != null) {
			hql.append(" order by ").append(order);
		}

		Query query = createQuery(hql.toString(), params);

		if(inicio != null) {
			query.setFirstResult(inicio);
		}
		if(max != null) {
			query.setMaxResults(max);
		}

		return query;
	}

	public List<LogOcr> findByIds(List<Long> ids) {

		StringBuilder hql = new StringBuilder();
		hql.append(getStartQuery()).append(" l ");

		hql.append(" where l.id in (-1");
		for (Long id : ids) {
			hql.append(", ").append(id);
		}
		hql.append(") ");

		hql.append(" order by l.id ");

		Query query = createQuery(hql.toString());
		query.setFetchSize(100);
		query.setCacheable(false);

		return query.list();
	}

	public List<RelatorioOcrVO> findRelatorioByFiltro(LogOcrFiltro filtro, Integer inicio, Integer max, boolean trazerCampos) {

		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<>();

		hql.append(" select  ");
		hql.append(" 	p, d, l, ");
		hql.append(" 	( ");
		hql.append(" 		select count(*) from  ");
		hql.append("		").append(CampoOcr.class.getName()).append(" co, ");
		hql.append("		").append(Imagem.class.getName()).append(" i ");
		hql.append("		where co.imagem.id = i.id and i.documento.id = d.id ");
		hql.append(" 	) as ok, ");
		hql.append(" 	( ");
		hql.append("		select count(*) from ");
		hql.append("		").append(CampoOcr.class.getName()).append(" co, ");
		hql.append("		").append(Imagem.class.getName()).append(" i ");
		hql.append("		where co.imagem.id = i.id and i.documento.id = d.id and co.valorIgualProcesso is true ");
		hql.append(" 	) as sucesso,  ");
		hql.append(" 	( ");
		hql.append("		select count(*) from ");
		hql.append("		").append(CampoOcr.class.getName()).append(" co, ");
		hql.append("		").append(Imagem.class.getName()).append(" i ");
		hql.append("		where co.imagem.id = i.id and i.documento.id = d.id and co.valorIgualProcesso is false ");
		hql.append("	) as falha ");

		if(trazerCampos) {
			hql.append(" 	, cocr ");
		}
		hql.append(" from ");
		hql.append(LogOcr.class.getName()).append(" l, ");
		hql.append(Documento.class.getName()).append(" d, ");
		hql.append(Processo.class.getName()).append(" p ");
		if(trazerCampos) {
			hql.append(" 	,").append(CampoOcr.class.getName()).append(" cocr ");
		}

		makeWhere(trazerCampos, hql, params, filtro);

		hql.append(" order by ");
		hql.append(" 	p.id, d.id ");
		if(trazerCampos) {
			hql.append(" 	, cocr.nome ");
		}

		Query query = createQuery(hql.toString(), params);

		if(inicio != null) {
			query.setFirstResult(inicio);
		}
		if(max != null) {
			query.setMaxResults(max);
			query.setFetchSize(max);
		}

		List<Object[]> list = query.list();

		Map<LogOcr, RelatorioOcrVO> map = new LinkedHashMap<>();
		for (Object[] objects : list) {

			Processo processo = (Processo) objects[0];
			Documento documento = (Documento) objects[1];
			LogOcr logOcr = (LogOcr) objects[2];
			Long countImagemOcr = (Long) objects[3];
			Long countImagemOcrSucesso = (Long) objects[4];
			Long countImagemOcrFalha = (Long) objects[5];

			RelatorioOcrVO vo = map.get(logOcr);
			vo = vo != null ? vo : new RelatorioOcrVO();
			map.put(logOcr, vo);

			vo.setProcesso(processo);
			vo.setDocumento(documento);
			vo.setLogOcr(logOcr);
			vo.setCountCampoOcr(countImagemOcr);
			vo.setCountCampoOcrSucesso(countImagemOcrSucesso);
			vo.setCountCampoOcrFalha(countImagemOcrFalha);

			if(trazerCampos) {
				CampoOcr ocr = (CampoOcr) objects[6];
				vo.addCampoOcr(ocr);
			}
		}

		Collection<RelatorioOcrVO> values = map.values();
		return new ArrayList<>(values);
	}

	private void makeWhere(boolean trazerCampos, StringBuilder hql, List<Object> params, LogOcrFiltro filtro) {

		LogOcrFiltro.ConsiderarData considerarData = filtro.getConsiderarData();
		Date dataInicio = filtro.getDataInicio();
		if(dataInicio != null) {
			dataInicio = DateUtils.truncate(dataInicio, Calendar.DAY_OF_MONTH);
		}
		Date dataFim = filtro.getDataFim();
		if(dataFim != null) {
			dataFim = DateUtils.truncate(dataFim, Calendar.DAY_OF_MONTH);
			Calendar c = Calendar.getInstance();
			c.setTime(dataFim);
			c.add(Calendar.DAY_OF_MONTH, 1);
			c.add(Calendar.SECOND, -1);
			dataFim = c.getTime();
		}

		hql.append(" where ");
		hql.append(" l.processoId = p.id ");
		hql.append(" and l.documentoId = d.id ");
		if(trazerCampos) {
			hql.append(" and l.documentoId = cocr.imagem.documento.id ");
			hql.append(" and d.versaoAtual = cocr.imagem.versao ");
		}
		hql.append(" and d.statusOcr is not null ");
		hql.append(" and l.id = ( ");
		hql.append("	select max(l2.id) from ");
		hql.append("	").append(LogOcr.class.getName()).append(" l2 ");
		hql.append("	where l2.documentoId = d.id ");
		hql.append(" ) ");
		hql.append(" and ( ");
		hql.append("	select count(*) from ");
		hql.append("	").append(Imagem.class.getName()).append(" i ");
		hql.append("	where i.documento.id = d.id ");
		hql.append(" ) > 0 ");

		if(LogOcrFiltro.ConsiderarData.CRIACAO_LOG.equals(considerarData)) {
			if(dataInicio != null) {
				hql.append(" and l.data >= ? ");
				params.add(dataInicio);
			}
			if(dataFim != null) {
				hql.append(" and l.data <= ? ");
				params.add(dataFim);
			}
		}
		else {
			if(dataInicio != null) {
				hql.append(" and p.dataCriacao >= ? ");
				params.add(dataInicio);
			}
			if(dataFim != null) {
				hql.append(" and p.dataCriacao <= ? ");
				params.add(dataFim);
			}
		}

		Integer numeroFalhas = filtro.getNumeroFalhas();
		if(numeroFalhas != null) {
			hql.append(" and ( ");
			hql.append(" 	select count(*) ");
			hql.append(" 	from ").append(CampoOcr.class.getName()).append(" co, ").append(Imagem.class.getName()).append(" i ");
			hql.append(" 	where ");
			hql.append(" 		co.imagem.id = i.id and ");
			hql.append(" 		i.documento.id = d.id and ");
			hql.append(" 		co.valorIgualProcesso is false ");
			hql.append(" ) >= ").append(numeroFalhas);
		}

		List<String> documentos = filtro.getDocumentos();
		if(documentos != null && !documentos.isEmpty()) {
			hql.append(" and d.nome in ( ''");
			for (String documento : documentos) {
				hql.append(" ,'").append(documento).append("'");
			}
			hql.append(" ) ");
		}
	}
}
