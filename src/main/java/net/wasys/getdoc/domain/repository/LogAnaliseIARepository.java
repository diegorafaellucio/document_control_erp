package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.CamposMetadadosTipificacao;
import net.wasys.getdoc.domain.enumeration.StatusDocumento;
import net.wasys.getdoc.domain.vo.LogAnaliseIAVO;
import net.wasys.getdoc.domain.vo.filtro.LogAnaliseIAFiltro;
import net.wasys.getdoc.domain.vo.filtro.RelatorioLicenciamentoFiltro;
import net.wasys.getdoc.domain.vo.filtro.RelatorioLicenciamentoOCRFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.ddd.HibernateRepository;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.Query;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@SuppressWarnings("unchecked")
public class LogAnaliseIARepository extends HibernateRepository<LogAnaliseIA> {

	public LogAnaliseIARepository() {
		super(LogAnaliseIA.class);
	}

	public int countByFiltro(LogAnaliseIAFiltro filtro) {

		StringBuilder hql = new StringBuilder();

		hql.append("select count(*) from ").append(LogAnaliseIA.class.getName()).append(" la ");

		Map<String, Object> params = makeQuery(filtro, hql);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue();
	}

	public List<LogAnaliseIA> findByFiltro(LogAnaliseIAFiltro filtro, Integer first, Integer pageSize) {

		StringBuilder hql = new StringBuilder();

		hql.append("from ").append(LogAnaliseIA.class.getName()).append(" la ");

		Map<String, Object> params = makeQuery(filtro, hql);

		hql.append(" order by la.data asc");

		Query query = createQuery(hql.toString(), params);

		if (first != null) {
			query.setFirstResult(first);
		}

		if (pageSize != null) {
			query.setMaxResults(pageSize);
		}

		return query.list();
	}

	public List<LogAnaliseIA> findByFiltro(LogAnaliseIAFiltro filtro) {

		StringBuilder hql = new StringBuilder();

		hql.append("from ").append(LogAnaliseIA.class.getName()).append(" la ");

		Map<String, Object> params = makeQuery(filtro, hql);

		hql.append(" order by la.data asc");

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}

	private Map<String, Object> makeQuery(LogAnaliseIAFiltro filtro, StringBuilder hql) {

		Map<String, Object> params = new HashMap<>();

		LogAnaliseIAFiltro.ConsiderarData considerarData = filtro.getConsiderarData();
		Date inicio = filtro.getDataInicio();
		Date fim = filtro.getDataFim();
		List<TipoDocumento> tipoDocumentoList = filtro.getTipoDocumentoList();
		List<Long> tipoDocumentoIdList = filtro.getTipoDocumentoIdList();
		List<StatusDocumento> statusDocumentoList = filtro.getStatusDocumentoList();

		boolean tipificado = filtro.isTipificado();
		boolean ocr = filtro.isOcr();

		hql.append(" where 1=1 ");



		if (inicio != null && fim != null) {

			inicio = DateUtils.truncate(inicio, Calendar.DAY_OF_MONTH);

			Calendar dataFimC = Calendar.getInstance();
			dataFimC.setTime(fim);
			dataFimC.add(Calendar.DAY_OF_MONTH, 1);
			dataFimC = DateUtils.truncate(dataFimC, Calendar.DAY_OF_MONTH);
			dataFimC.add(Calendar.MILLISECOND, -1);

			fim = dataFimC.getTime();

			hql.append(" and ( 1 != 1 ");

			if(LogAnaliseIAFiltro.ConsiderarData.CRIACAO.equals(considerarData)) {
				hql.append(" or la.processo.id in (  ");
				hql.append(" select p.id from ").append(Processo.class.getName()).append(" p ");
				hql.append(" where p.dataCriacao between :inicio and :fim ) ");
				params.put("inicio", inicio);
				params.put("fim", fim);
			} else if (LogAnaliseIAFiltro.ConsiderarData.TIPIFICACAO.equals(considerarData)) {
				hql.append(" or la.data between :inicio and :fim ");
				params.put("inicio", inicio);
				params.put("fim", fim);
			}
			hql.append(" ) ");
		}

		if (tipificado) {
			hql.append(" and la.tipificou is true ");
		}

		if (ocr) {
			hql.append(" and la.ocr is true ");
		}

		if (CollectionUtils.isNotEmpty(tipoDocumentoList)){
			hql.append(" and la.documento.tipoDocumento in (:tipoDocumentoList)");
			params.put("tipoDocumentoList", tipoDocumentoList);
		}

		if (CollectionUtils.isNotEmpty(tipoDocumentoIdList)){
			hql.append(" and la.documento.tipoDocumento.id in (:tipoDocumentoIdList)");
			params.put("tipoDocumentoIdList", tipoDocumentoIdList);
		}

		if(CollectionUtils.isNotEmpty(statusDocumentoList)) {
			hql.append(" and la.documento.status in (:statusDocumentoList)");
			params.put("statusDocumentoList", statusDocumentoList);
		}

		return params;
	}

    public LogAnaliseIA findByProcessoAndDocumento(Processo processo, Documento documento) {
		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<>();

		hql.append(" select la ");
		hql.append(" from ").append(clazz.getName()).append(" la ");
		hql.append(" where 1 = 1 ");
		hql.append(" and la.processo.id = :processoId ");
		hql.append(" and la.documento.id = :documentoId ");
		hql.append(" order by la.id desc ");
		params.put("processoId", processo.getId());
		params.put("documentoId", documento.getId());

		Query query = createQuery(hql,params);
		query.setMaxResults(1);

		return (LogAnaliseIA) query.uniqueResult();
    }

	public void updateStatusMotivoProcesso(Processo processo, String statusProcesso, String motivoProcesso) {
		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<>();

		hql.append(" update ").append(clazz.getName()).append(" la ");
		hql.append(" set la.statusProcesso = :statusProcesso, ");
		hql.append(" la.motivoProcesso = :motivoProcesso ");
		hql.append(" where la.processo.id = :processoId ");

		params.put("processoId", processo.getId());
		params.put("statusProcesso", statusProcesso);
		params.put("motivoProcesso", motivoProcesso);
		Query query = createQuery(hql,params);
		query.executeUpdate();
	}

    public List<LogAnaliseIAVO> findToReport(LogAnaliseIAFiltro filtro) {
		List<LogAnaliseIAVO> logAnaliseIAVOS = new ArrayList<>();

		StringBuilder hql = new StringBuilder();

		hql.append(" select la.processo.id ,");
		hql.append(" la.processo.tipoProcesso.nome, ");
		hql.append(" la.documento.id, ");
		hql.append(" la.documento.nome, ");
		hql.append(" la.data, ");
		hql.append(" la.statusDocumento, ");
		hql.append(" la.motivoDocumento, ");
		hql.append(" la.statusProcesso, ");
		hql.append(" la.motivoProcesso, ");
		hql.append(" la.tipificou, ");
		hql.append(" la.ocr, ");
		hql.append(" la.metadados, ");
		hql.append(" im.metaDados as im_metadados, ");
		hql.append(" la.documento.modeloDocumento.descricao ");
		hql.append(" from ").append(LogAnaliseIA.class.getName()).append(" la, ");
		hql.append(" 	").append(Imagem.class.getName()).append(" i, ");
		hql.append(" 	 ").append(ImagemMeta.class.getName()).append(" im ");

		Map<String, Object> params = makeQuery(filtro, hql);

		hql.append(" 	and la.documento.id = i.documento.id ");
		hql.append(" 	and im.imagemId = i.id ");

		hql.append(" order by la.data asc");

		Query query = createQuery(hql.toString(), params);
		List<Object[]> list = query.list();

		for (Object[] obj : list) {
			Long processoId = (Long) obj[0];
			String processoNome = (String) obj[1];
			Long documentoId = (Long) obj[2];
			String documentoNome = (String) obj[3];
			Date data = (Date) obj[4];
			String statusDocumento = (String) obj[5];
			String motivoDocumento = (String) obj[6];
			String statusProcesso = (String) obj[7];
			String motivoProcesso = (String) obj[8];
			boolean tipificou = (boolean) obj[9];
			boolean ocr = (boolean) obj[10];
			String metadados = (String) obj[11];
			JSONObject image_metadados = new JSONObject((String) obj[12]);
			String modeloDocumento =  (String) obj[13];

			String labelTipificacao = "";

			String scoreTipificacao = "";

			String dataDigitalizacao = "";

			if (image_metadados.has(CamposMetadadosTipificacao.DN_TODAS_LABELS.getCampo())){
				labelTipificacao = image_metadados.getString(CamposMetadadosTipificacao.DN_TODAS_LABELS.getCampo());
			}

			if (image_metadados.has(CamposMetadadosTipificacao.DN_PERCENTUAL_ACERTO.getCampo())){
				scoreTipificacao = image_metadados.getString(CamposMetadadosTipificacao.DN_PERCENTUAL_ACERTO.getCampo());
			}

			if (image_metadados.has("dataDigitalizacao")){
				dataDigitalizacao = image_metadados.getString("dataDigitalizacao");
			}



			LogAnaliseIAVO logAnaliseIAVO = new LogAnaliseIAVO();
			logAnaliseIAVO.setProcessoId(processoId);
			logAnaliseIAVO.setProcessoNome(processoNome);
			logAnaliseIAVO.setDocumentoId(documentoId);
			logAnaliseIAVO.setDocumentoNome(documentoNome);
			logAnaliseIAVO.setData(data);
			logAnaliseIAVO.setDataDigitalizacao(dataDigitalizacao);
			logAnaliseIAVO.setStatusDocumento(statusDocumento);
			logAnaliseIAVO.setMotivoDocumento(motivoDocumento);
			logAnaliseIAVO.setStatusProcesso(statusProcesso);
			logAnaliseIAVO.setMotivoProcesso(motivoProcesso);
			logAnaliseIAVO.setTipificou(tipificou);
			logAnaliseIAVO.setOcr(ocr);
			logAnaliseIAVO.setModeloDocumento(modeloDocumento);
			logAnaliseIAVO.setLabelTipificacao(labelTipificacao);
			logAnaliseIAVO.setScoreTipificacao(scoreTipificacao);
			logAnaliseIAVO.setScoreTipificacao(scoreTipificacao);
			logAnaliseIAVO.setMetadados(metadados);

			logAnaliseIAVOS.add(logAnaliseIAVO);

		}

		return logAnaliseIAVOS;

    }

    public List<Object[]> findRelatorioLicenciamento(RelatorioLicenciamentoOCRFiltro filtro) {
		List<Date> meses = filtro.getMeses();

		List<Object> params = new ArrayList<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select lai.tipoProcesso.id, ");
		hql.append(" lai.tipoProcesso.nome, ");

		for(Date mes : meses){
			hql.append(" count(CASE WHEN MONTH(lai.data) = ? and YEAR(lai.data) = ? then data END)");
			params.add(net.wasys.getdoc.mb.utils.DateUtils.getMonth(mes));
			params.add(net.wasys.getdoc.mb.utils.DateUtils.getYear(mes));
			if (meses.indexOf(mes) != meses.size()-1){
				hql.append(", ");
			}
		}
		hql.append(" from ").append(clazz.getName()).append(" lai ");
		hql.append(" where lai.tipificou = true ");
		hql.append(" and lai.ocr = true ");
		hql.append(" and lai.data >= '2021-10-13' "); //Data Inicio do OCR. Os casos de antes dessa data eram simulações.
		hql.append(" group by lai.tipoProcesso.nome, lai.tipoProcesso.id ");
		hql.append(" order by lai.tipoProcesso.nome ");

		Query query = createQuery(hql,params);

		List<Object[]> list = query.list();
		return list;
    }
}
