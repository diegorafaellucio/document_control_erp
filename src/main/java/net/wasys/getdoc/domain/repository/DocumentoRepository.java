package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.GetdocConstants;
import net.wasys.getdoc.GetdocConstants;
import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.*;
import net.wasys.getdoc.domain.vo.RelatorioPastaVermelhaVO;
import net.wasys.getdoc.domain.vo.filtro.DocumentoFiltro;
import net.wasys.getdoc.domain.vo.filtro.RelatorioPastaVermelhaFiltro;
import net.wasys.getdoc.domain.vo.filtro.TaxonomiaFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.LogLevel;
import net.wasys.util.ddd.HibernateRepository;
import net.wasys.util.google.cloud.Image;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Repository
@SuppressWarnings("unchecked")
public class DocumentoRepository extends HibernateRepository<Documento> {

	public DocumentoRepository() {
		super(Documento.class);
	}

	public boolean existsByFiltro(DocumentoFiltro filtro) {
		StringBuilder hql = new StringBuilder("select 1 from " + clazz.getName() + " d ");

		Map<String, Object> params = makeQuery(filtro, hql);

		Query query = createQuery(hql, params);

		query.setMaxResults(1);

		return query.uniqueResult() != null;
	}

	public Integer countByFiltro(DocumentoFiltro filtro) {
		StringBuilder hql = new StringBuilder("select count(*) from " + clazz.getName() + " d ");

		Map<String, Object> params = makeQuery(filtro, hql);

		Query query = createQuery(hql, params);

		return ((Long) query.uniqueResult()).intValue();
	}

	public List<Long> findTiposDocumentosIdsByFiltro(DocumentoFiltro filtro, Integer inicio, Integer max) {

		StringBuilder hql = new StringBuilder();
		hql.append(" select d.tipoDocumento.id from ").append(clazz.getName()).append(" d ");
		Map<String, Object> params = makeQuery(filtro, hql);
		hql.append(" order by d.ordem");

		Query query = createQuery(hql.toString(), params);

		if(inicio != null) {
			query.setFirstResult(inicio);
		}
		if(max != null) {
			query.setMaxResults(max);
		}

		List<Long> list = query.list();
		return list;
	}

	public List<Documento> findByFiltro(DocumentoFiltro filtro, Integer inicio, Integer max) {

		StringBuilder hql = new StringBuilder();
		hql.append(getStartQuery()).append(" d ");

		List<DocumentoFiltro.Fetch> fetch = filtro.getFetch();
		if(fetch != null) {
			for (DocumentoFiltro.Fetch fetch1 : fetch) {
				hql.append(" left outer join fetch d.").append(fetch1.getColumn());
			}
		}

		Map<String, Object> params = makeQuery(filtro, hql);
		hql.append(" order by d.ordem");

		Query query = createQuery(hql.toString(), params);

		if(inicio != null) {
			query.setFirstResult(inicio);
		}
		if(max != null) {
			query.setMaxResults(max);
		}

		List<Documento> list = query.list();
		return list;
	}

	private Map<String, Object> makeQuery(DocumentoFiltro filtro, StringBuilder hql) {
		Map<String, Object> params = new LinkedHashMap<>();

		Date dataDigitalizacaoInicio = filtro.getDataDigitalizacaoInicio();
		Date dataDigitalizacaoFim = filtro.getDataDigitalizacaoFim();
		String nome = filtro.getNome();
		Origem origem = filtro.getOrigem();
		List<StatusDocumento> statusDocumentoList = filtro.getStatusDocumentoList();
		StatusFacial statusFacial = filtro.getStatusFacial();
		StatusOcr statusOcr = filtro.getStatusOcr();
		List<Long> tipoDocumentoIdList = filtro.getTipoDocumentoIdList();
		Processo processo = filtro.getProcesso();
		boolean diferenteDeOutros = filtro.isDiferenteDeOutros();
		boolean diferenteDeTipificando = filtro.isDiferenteDeTipificando();
		List<StatusDocumento> statusDifetenteDeList = filtro.getStatusDifetenteDeList();
		CampoGrupo grupoRelacionado = filtro.getGrupoRelacionado();
		Boolean obrigatorio = filtro.getObrigatorio();
		List<Long> processoList = filtro.getProcessoList();
		List<Long> codsOrigem = filtro.getCodsOrigem();

		hql.append(" where 1=1 ");

		if(processoList != null) {
			hql.append(" and d.processo.id in ( :processoList ) ");
			params.put("processoList", processoList);
		}

		if(dataDigitalizacaoInicio != null && dataDigitalizacaoFim != null) {

			dataDigitalizacaoInicio = DateUtils.truncate(dataDigitalizacaoInicio, Calendar.DAY_OF_MONTH);

			Calendar dataFimC = Calendar.getInstance();
			dataFimC.setTime(dataDigitalizacaoFim);
			dataFimC.add(Calendar.DAY_OF_MONTH, 1);
			dataFimC = DateUtils.truncate(dataFimC, Calendar.DAY_OF_MONTH);
			dataFimC.add(Calendar.MILLISECOND, -1);

			dataDigitalizacaoFim = dataFimC.getTime();

			hql.append(" and ( 1 != 1 ");

			hql.append(" or d.dataDigitalizacao between :dataInicio and :dataFim ");
			params.put("dataDigitalizacaoInicio", dataDigitalizacaoInicio);
			params.put("dataDigitalizacaoFim", dataDigitalizacaoFim);

			hql.append(" ) ");
		}

		if(processo != null) {
			hql.append(" and d.processo = :processo");
			params.put("processo", processo);
		}

		if(StringUtils.isNotBlank(nome)) {
			hql.append(" and d.nome = :nome");
			params.put("nome", nome);
		}

		if(origem != null) {
			hql.append(" and d.origem = :origem");
			params.put("origem", origem);
		}

		if(statusDocumentoList != null && !statusDocumentoList.isEmpty()) {
			hql.append(" and d.status in (:statusDocumentoList)");
			params.put("statusDocumentoList", statusDocumentoList);
		}

		if(statusFacial != null) {
			hql.append(" and d.statusFacial = :statusFacial");
			params.put("statusFacial", statusFacial);
		}

		if(statusOcr != null) {
			hql.append(" and d.statusOcr = :statusOcr");
			params.put("statusOcr", statusOcr);
		}

		if(tipoDocumentoIdList != null && !tipoDocumentoIdList.isEmpty()) {
			hql.append(" and d.tipoDocumento.id in (:tipoDocumentoIdList)");
			params.put("tipoDocumentoIdList", tipoDocumentoIdList);
		}

		if(diferenteDeOutros) {
			hql.append(" and d.nome <> :outros");
			params.put("outros", Documento.NOME_OUTROS);
		}

		if(diferenteDeTipificando) {
			hql.append(" and d.nome <> :tificando");
			params.put("tificando", Documento.NOME_TIFICANDO);
		}

		if(statusDifetenteDeList != null && !statusDifetenteDeList.isEmpty()) {
			hql.append(" and d.status not in (:statusDiferente)");
			params.put("statusDiferente", statusDifetenteDeList);
		}

		if(grupoRelacionado != null) {
			hql.append(" and d.grupoRelacionado = :grupoRelacionado ");
			params.put("grupoRelacionado", grupoRelacionado);
		}

		if (obrigatorio != null) {
			hql.append(" and d.obrigatorio = :obrigatorio ");
			params.put("obrigatorio", obrigatorio);
		}

		if(codsOrigem != null) {
			if(codsOrigem.isEmpty() || codsOrigem.get(0) == 0) {
				hql.append(" and d.tipoDocumento.codOrigem = null ");
			}
			else {
				hql.append(" and d.tipoDocumento.codOrigem in ( :codsOrigem ) ");
				params.put("codsOrigem", codsOrigem);
			}
		}

		return params;
	}

	public List<Documento> findNaoAprovadosByProcesso(Long processoId) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" d");

		hql.append(" where d.processo.id = :processoId ");
		hql.append(" and d.status <> :status");
		hql.append(" order by d.ordem, d.nome ");

		params.put("status", StatusDocumento.APROVADO);
		params.put("processoId", processoId);

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}

	public List<Documento> findByProcessoAndPartOfName(Long processoId, String nome) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());

		hql.append(" where processo.id = :processoId ");
		hql.append(" and nome like :nome");

		params.put("processoId", processoId);
		params.put("nome", "%" + StringUtils.upperCase(nome).trim() + "%");

		hql.append(" order by ordem, nome ");

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}

	public List<Documento> findByProcessoId(Long processoId) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" where processo.id = :processoId ");
		params.put("processoId", processoId);

		hql.append(" order by nome ");

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}

	public List<TipoDocumento> getByProcessoTipoDocumento(Long processoId, List<Long> tipoDocumentoIdList) {

		Map<String, Object> params = new HashMap<>();

		StringBuilder hql = new StringBuilder();
		hql.append(" select td ").append(getStartQuery()).append(" d");
		hql.append(" join " + TipoDocumento.class.getName() + " td on d.tipoDocumento.id = td.id ");
		hql.append(" where d.processo.id = :processoId");
		hql.append(" and d.tipoDocumento.id in (:tipoDocumentoIdList) ");
		hql.append(" group by td");

		params.put("processoId", processoId);
		params.put("tipoDocumentoIdList", tipoDocumentoIdList);

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}

	public boolean temPendenteDigitalizado(List<Long> processosId) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();
		List<StatusDocumento> status = new ArrayList<>();
		status.add(StatusDocumento.DIGITALIZADO);
		status.add(StatusDocumento.APROVADO);
		status.add(StatusDocumento.EXCLUIDO);

		hql.append(" select count(*) from ").append(Documento.class.getName()).append(" d ");
		hql.append(" where d.status not in (:status) ");
		params.put("status", status);
		hql.append(" and obrigatorio = true ");
		hql.append(" and processo.id in ( :listProcessosId )");
		params.put("listProcessosId", processosId);
		hql.append(" and d.nome <> :nomeArquivo ");
		params.put("nomeArquivo", Documento.NOME_OUTROS);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).longValue() > 0;
	}

	public boolean temNaoAprovado(List<Long> processosId) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();
		List<StatusDocumento> status = new ArrayList<>();
		status.add(StatusDocumento.EXCLUIDO);
		status.add(StatusDocumento.APROVADO);

		hql.append(" select count(*) from ").append(Documento.class.getName()).append(" d ");
		hql.append(" where d.status not in (:status) ");
		params.put("status", status);
		hql.append(" and obrigatorio = true ");
		hql.append(" and processo.id in ( :listProcessosId ) ");
		params.put("listProcessosId", processosId);
		hql.append(" and d.nome <> :nomeArquivo ");
		params.put("nomeArquivo", Documento.NOME_OUTROS);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).longValue() > 0;
	}

	public boolean temPendente(List<Long> processosId) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select count(*) from ").append(Documento.class.getName()).append(" d ");
		hql.append(" where d.status not in (:statusNot) ");
		params.put("statusNot", StatusDocumento.EXCLUIDO);
		hql.append(" and d.status in (:status) ");
		params.put("status", StatusDocumento.PENDENTE);
		hql.append(" and obrigatorio = true ");
		hql.append(" and processo.id in ( :listProcessosId ) ");
		params.put("listProcessosId", processosId);
		hql.append(" and d.nome <> :nomeArquivo ");
		params.put("nomeArquivo", Documento.NOME_OUTROS);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).longValue() > 0;
	}

	public List<Long> findIdsToReconhecimentoFacial() {

		StringBuilder hql = new StringBuilder();
		hql.append(" select i.id from ").append(clazz.getName()).append(" i where i.aguardandoReconhecimentoFacial is true order by i.id ");

		Query query = createQuery(hql.toString());

		return query.list();
	}

	public List<Documento> findByIds(List<Long> ids) {

		StringBuilder hql = new StringBuilder();
		hql.append(" from ").append(clazz.getName()).append(" d ");

		hql.append(" where d.id in (-1");
		for (Long id : ids) {
			hql.append(", ").append(id);
		}
		hql.append(") ");

		Query query = createQuery(hql.toString());
		query.setFetchSize(100);

		return query.list();
	}

	public Documento getTipificando(Long processoId) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new LinkedHashMap<>();

		hql.append(" from ").append(clazz.getName()).append(" d ");
		hql.append(" where d.nome = :nome ");
		hql.append(" and d.processo.id = :processoId ");

		params.put("nome", Documento.NOME_TIFICANDO);
		params.put("processoId", processoId);

		Query query = createQuery(hql.toString(), params);
		query.setFetchSize(100);

		return (Documento) query.uniqueResult();
	}

	public List<Long> findIdsToTipificacao() {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select distinct d.id ");
		hql.append(" from ");
		hql.append(" 	 ").append(Documento.class.getName()).append(" d, ");
			hql.append(" 	 ").append(Imagem.class.getName()).append(" i ");
		hql.append(" where ");
		hql.append(" 	i.documento.id = d.id ");
		hql.append(" 	and d.nome = :nome ");
		hql.append(" 	and d.status <> :statusExcluido ");
		hql.append(" 	and (i.existente = null or i.existente = true) ");
		hql.append(" order by d.id ");

		params.put("nome", Documento.NOME_TIFICANDO);
		params.put("statusExcluido", StatusDocumento.EXCLUIDO);

		Query query = createQuery(hql.toString(), params);
		query.setMaxResults(1000);

		return query.list();
	}

	public List<Object[]> findIdsToTipificacaoUsandoImagemMeta() {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new LinkedHashMap<>();

		hql.append(" select i.documento.id, i.id ");
		hql.append(" from ");
		hql.append(" 	").append(Imagem.class.getName()).append(" i, ");
		hql.append(" 	").append(ImagemMeta.class.getName()).append(" im ");
		hql.append(" where im.imagemId = i.id ");
		hql.append(" 	and im.tipificado is false ");
		hql.append(" 	and im.precisaTipificar is true ");
		hql.append(" 	and (i.existente = null or i.existente = true) ");
		hql.append(" order by i.documento.id, i.id ");

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}

	public List<Object[]> findIdsToTipificacaoByFiltro(DocumentoFiltro filtro) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new LinkedHashMap<>();


		Date dataDigitalizacaoInicio = filtro.getDataDigitalizacaoInicio();
		Date dataDigitalizacaoFim = filtro.getDataDigitalizacaoFim();

		hql.append(" select i.documento.id, i.id ");
		hql.append(" from ");
		hql.append(" 	").append(Imagem.class.getName()).append(" i, ");
		hql.append(" 	").append(ImagemMeta.class.getName()).append(" im ");
		hql.append(" where im.imagemId = i.id ");
		hql.append(" 	and i.documento.modeloDocumento.id is null ");
		hql.append(" 	and ( ");
		hql.append(" 		i.documento.tipoDocumento.sempreTipificar is true ");
		hql.append(" 		or i.documento.origem = :origem ");
		hql.append(" 	) ");
		hql.append(" 	and im.tipificado is false ");

		if(dataDigitalizacaoInicio != null && dataDigitalizacaoFim != null) {

			dataDigitalizacaoInicio = DateUtils.truncate(dataDigitalizacaoInicio, Calendar.DAY_OF_MONTH);

			Calendar dataFimC = Calendar.getInstance();
			dataFimC.setTime(dataDigitalizacaoFim);
			dataFimC.add(Calendar.DAY_OF_MONTH, 1);
			dataFimC = DateUtils.truncate(dataFimC, Calendar.DAY_OF_MONTH);
			dataFimC.add(Calendar.MILLISECOND, -1);

			dataDigitalizacaoFim = dataFimC.getTime();

			hql.append(" and ( 1 != 1 ");

			hql.append(" or i.documento.dataDigitalizacao between :dataDigitalizacaoInicio and :dataDigitalizacaoFim ");
			params.put("dataDigitalizacaoInicio", dataDigitalizacaoInicio);
			params.put("dataDigitalizacaoFim", dataDigitalizacaoFim);

			hql.append(" ) ");
		}

		hql.append(" order by i.documento.id, i.id desc");


		params.put("origem", Origem.PORTAL_GRADUCAO);

		Query query = createQuery(hql.toString(), params);
		query.setFetchSize(100);
		query.setMaxResults(100);

		return query.list();
	}

	public List<Long> findIdsToTipificacaoByProcesso(Processo processo) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select d.id from ").append(clazz.getName()).append(" d ");
		hql.append(" where nome = :nome ");
		hql.append(" and status <> :statusExcluido ");
		hql.append(" and d.processo = :processo ");
		hql.append(" order by d.id ");

		params.put("nome", Documento.NOME_TIFICANDO);
		params.put("statusExcluido", StatusDocumento.EXCLUIDO);
		params.put("processo", processo);

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}

	public List<Object[]> findIdsToTipificacao2ByProcesso(Processo processo) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select i.documento.id, i.id ");
		hql.append(" from ");
		hql.append(" 	").append(Imagem.class.getName()).append(" i, ");
		hql.append(" 	").append(ImagemMeta.class.getName()).append(" im ");
		hql.append(" where im.imagemId = i.id ");
		hql.append(" 	and i.documento.modeloDocumento.id is null ");
		hql.append(" 	and i.documento.nome = :nome ");
		hql.append(" 	and im.tipificado is false ");
		hql.append(" 	and i.documento.processo.id = :processo ");
		hql.append(" order by i.documento.id, i.id ");

		params.put("nome", Documento.NOME_TIFICANDO);
		params.put("processo", processo.getId());

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}

	public int countByMetaDados(Map<String, Object> map) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new LinkedHashMap<>();

		hql.append(" select count(*) from ").append(Documento.class.getName()).append(" ");
		hql.append(" where 1<>1 ");
		Set<String> keySet = map.keySet();
		List<String> keyList = new LinkedList<>(keySet);
		for (int i = 0; i < keyList.size(); i++) {
			hql.append(" or metadados like :metadado").append(i);
			String key = keyList.get(i);
			Object valor = map.get(key);
			params.put("metadado" + i, "%\"" + key + "\":" + valor + "%");
		}

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue();
	}

	public Integer getMaxOrdemFromProcesso(Long processoId) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new LinkedHashMap<>();

		hql.append(" select max(d.ordem) from ").append(Documento.class.getName()).append(" d ");
		hql.append(" where processo.id = :processoId ");
		params.put("processoId", processoId);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue();
	}

	public Documento getByMetaDados(Map<String, Object> map) {
		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new LinkedHashMap<>();

		hql.append(" from ").append(clazz.getName()).append(" d ");
		hql.append(" where 1<>1 ");
		Set<String> keySet = map.keySet();
		List<String> keyList = new LinkedList<>(keySet);
		for (int i = 0; i < keyList.size(); i++) {
			hql.append(" or metadados like :metadado").append(i);
			String key = keyList.get(i);
			Object valor = map.get(key);
			params.put("metadado" + i, "%\"" + key + "\":" + valor + "%");
		}

		Query query = createQuery(hql.toString(), params);

		return (Documento) query.uniqueResult();
	}

	public List<Documento> findByNumCandidato(Long numCandidato) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" where processo.id in (");
		hql.append(" 	select c.grupo.processo.id from ").append(Campo.class.getName()).append(" c ");
		hql.append(" 	where c.nome = :campoNumCandidato and upper(c.valor) like :numCandidato ");
		hql.append(" )");
		params.put("campoNumCandidato", CampoMap.CampoEnum.NUM_CANDIDATO.getNome());
		params.put("numCandidato", numCandidato.toString());

		hql.append(" order by processo.id, nome, ordem ");

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}

	public List<Documento> findDocumentosByAlunoFiltro(TaxonomiaFiltro filtro) {

		String cpfCnpj = filtro.getCpfCnpj();
		String matricula = filtro.getMatricula();
		String nome = filtro.getNome();
		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" d");
		hql.append(" where d.processo.id in ((");
		if (StringUtils.isNotBlank(cpfCnpj)){
			hql.append(" 	select p.id from ").append(Aluno.class.getName()).append(" a, ").append(Processo.class.getName()).append(" p ");
			hql.append(" 	where a.cpf = :valor and a.id = p.aluno.id ");
		}else if(StringUtils.isNotBlank(nome)){
			hql.append(" 	select p.id from ").append(Aluno.class.getName()).append(" a, ").append(Processo.class.getName()).append(" p ");
			hql.append(" 	where a.nome = :valor and a.id = p.aluno.id ");
		}else if(StringUtils.isNotBlank(matricula)){
			hql.append(" 	select c.grupo.processo.id from ").append(Campo.class.getName()).append(" c ");
			hql.append(" 	where c.grupo.nome = :candidatoGrupo and c.nome = :campoNome and c.valor like :valor ");
		}
		hql.append(" )) ");
		hql.append(" and d.tipoDocumento.baseRegistro.id is not null ");
		hql.append(" and d.status <> :status ");
		hql.append(" order by d.nome");

		if(StringUtils.isNotBlank(cpfCnpj)) {
			params.put("valor", cpfCnpj);
			params.put("status", StatusDocumento.EXCLUIDO);
		}else if(StringUtils.isNotBlank(nome)){
			nome = nome.toUpperCase();
			params.put("valor", nome);
			params.put("status", StatusDocumento.EXCLUIDO);
		}else if(StringUtils.isNotBlank(matricula)) {
			String nomeGrupo = CampoMap.CampoEnum.MATRICULA.getGrupo().getNome();
			String campoNome = CampoMap.CampoEnum.MATRICULA.getNome();
			params.put("candidatoGrupo", nomeGrupo);
			params.put("campoNome", campoNome);
			params.put("valor", matricula);
			params.put("status", StatusDocumento.EXCLUIDO);
		}

		Query query = createQuery(hql.toString(), params);
		return query.list();
	}

    public Documento getFirstByTipoDocumentoId(Long tipoDocumentoId, Long processoId){

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" d ");
		hql.append(" where d.processo.id = :processoId ");
		hql.append(" and d.tipoDocumento.id = :tipoDocumentoId ");
		hql.append(" order by d.id asc");

		params.put("processoId", processoId);
		params.put("tipoDocumentoId", tipoDocumentoId);

		Query query = createQuery(hql.toString(), params);
		query.setMaxResults(1);
		return (Documento) query.uniqueResult();
	}

	public Documento getFirstByTipoDocumentoIdMembroFamiliar(Long tipoDocumentoId, Long processoId, String membroFamiliar){

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" d ");
		hql.append(" where d.processo.id = :processoId ");
		hql.append(" and d.tipoDocumento.id = :tipoDocumentoId ");
		hql.append(" and upper(d.nome) like :membroFamiliar ");
		hql.append(" order by d.id asc ");

		params.put("processoId", processoId);
		params.put("tipoDocumentoId", tipoDocumentoId);
		params.put("membroFamiliar", "%" + membroFamiliar.toUpperCase() + "%");

		Query query = createQuery(hql.toString(), params);

		query.setMaxResults(1);

		return (Documento) query.uniqueResult();
	}

	public Documento getByNome(Long processoId, String nome) {

		Map<String, Object> params = new HashMap<>();

		StringBuilder hql = new StringBuilder();
		hql.append(getStartQuery()).append(" d ");
		hql.append(" where d.processo.id = :processoId");
		hql.append(" and d.nome = :nome ");

		params.put("processoId", processoId);
		params.put("nome", nome);

		Query query = createQuery(hql.toString(), params);

		return (Documento) query.uniqueResult();
	}

	public Map<StatusDocumento, Long> countByStatusMap(List<Long> processosId ) {

		Map<String, Object> params = new HashMap<>();

		StringBuilder hql = new StringBuilder();
		hql.append(" select d.status, count(*) ");
		hql.append(" from ").append(clazz.getName()).append(" d ");
		hql.append(" where d.processo.id in( :processosId ) ");
		hql.append(" group by d.status ");

		params.put("processosId", processosId);

		Query query = createQuery(hql.toString(), params);
		List<Object[]> list = query.list();

		Map<StatusDocumento, Long> map = new LinkedHashMap<>();
		for (Object[] obj : list) {
			Long quantidade = (Long) obj[1];
			quantidade = quantidade != null ? quantidade : 0l;
			map.put((StatusDocumento) obj[0],quantidade );
		}

		return map;
	}

	public Long getQuantidadeObrigatorio(List<Long> processosId) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select count(*) from ").append(Documento.class.getName()).append(" d ");
		hql.append(" where d.obrigatorio = true ");
		hql.append(" and d.processo.id in ( :listProcessosId ) ");
		params.put("listProcessosId",processosId);

		Query query = createQuery(hql.toString(), params);

		return (Long) query.uniqueResult();
	}

	public List<Long> findToNotificacaoSia() {

		StringBuilder hql = new StringBuilder();
		hql.append(" select d.id from ").append(Documento.class.getName()).append(" d ");

		Map<String, Object> params = makeQueryNotificacaoSia(hql, false, null, null);

		Query query = createQuery(hql.toString(), params);

		List list = query.list();
		return list;
	}

	public List<Documento> findNotificadosSia(Date inicioDataAprovacao, Date fimDataAprovacao) {

		StringBuilder hql = new StringBuilder();
		hql.append(getStartQuery()).append(" d ");

		Map<String, Object> params = makeQueryNotificacaoSia(hql, true, inicioDataAprovacao, fimDataAprovacao);

		Query query = createQuery(hql.toString(), params);
		query.setTimeout(GetdocConstants.QUERY_TIMEOUT);

		return query.list();
	}

	private Map<String, Object> makeQueryNotificacaoSia(StringBuilder hql, boolean isNotificadoSia, Date inicioDataAprovacao, Date fimDataAprovacao) {

		Map<String, Object> params = new HashMap<>();

		hql.append(" where d.notificadoSia is :isNotificadoSia ");
		hql.append(" and d.status = :statusAprovado ");
		hql.append(" and ( ");
		hql.append(" 	d.tipoDocumento.aceiteContrato is true ");
		hql.append(" 	or ( ");
		hql.append(" 		d.processo.situacao.notificarDocumentosSia is true ");
		hql.append(" 	) ");
		hql.append(" ) ");
		hql.append(" and d.tipoDocumento.codOrigem is not null ");
		hql.append(" and d.tipoDocumento.notificarSia is true ");
		hql.append(" and ( ");
		hql.append(" 		d.processo.numCandidato <> '' ");
		hql.append(" 	) ");
		hql.append(" and d.processo.eliminadoOrCancelado is false");

		if (inicioDataAprovacao != null) {

			hql.append(" and (select max(dl.data) from ").append(DocumentoLog.class.getName()).append(" dl where dl.documento.id = d.id and dl.acao = :acaoDocumentoLog) ");

			if (fimDataAprovacao == null) {
				hql.append(" > :dataInicioAprovacao ");
			}
			else {
				hql.append(" between :dataInicioAprovacao and :dataFimAprovacao ");
			}
		}

		hql.append(" order by d.id desc");

		params.put("isNotificadoSia", isNotificadoSia);
		params.put("acaoDocumentoLog", AcaoDocumento.APROVOU);
		params.put("dataInicioAprovacao", inicioDataAprovacao);
		params.put("dataFimAprovacao", fimDataAprovacao);
		params.put("statusAprovado", StatusDocumento.APROVADO);

		return params;
	}

	public boolean isTodosDigitalizados(List<Long> processosId, List<Long> tipoDocIds) {
		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select count(*) from ").append(Documento.class.getName()).append(" d ");
		hql.append(" where d.tipoDocumento.id in ( :tipoDocIds ) ");
		hql.append(" and d.processo.id in ( :listProcessosId ) ");
		hql.append(" and d.status in ( :status )");

		params.put("tipoDocIds", tipoDocIds);
		params.put("listProcessosId", processosId);
		params.put("status", Arrays.asList(StatusDocumento.INCLUIDO, StatusDocumento.PENDENTE));

		Query query = createQuery(hql.toString(), params);

		return (Long) query.uniqueResult() == 0;
	}

	public List<Documento> findByGrupoRelacionado(CampoGrupo grupoRelacionado) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" d ");
		hql.append(" where d.grupoRelacionado = :grupoRelacionado ");

		params.put("grupoRelacionado", grupoRelacionado);

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}

	public int findMaxOrdemByProcesso(Processo processo) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append("select max(d.ordem) from ").append(Documento.class.getName()).append(" d ");
		hql.append(" where d.processo = :processo ");

		params.put("processo", processo);

		Query query = createQuery(hql.toString(), params);

		Object result = query.uniqueResult();
		return result == null ? 0 : ((Number) result).intValue();
	}

	public void atualizarUsaTermo(Date dataInicio, Date dataFim, Long processoId){
		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append("update processo ");
		hql.append(" set usa_termo = case when  ((select count(*) ");
		hql.append("        from documento d ");
		hql.append("                 inner join tipo_documento td on d.tipo_documento_id = td.id");
		hql.append("                 inner join campanha c on c.id = processo.campanha_id ");
		hql.append("                 inner join documento dd on d.processo_id = dd.processo_id and d.id <> dd.id ");
		hql.append("        where d.processo_id = processo.id ");
		hql.append("         and td.termo_pasta_vermelha = true ");
		hql.append("         and d.status = :status ");
		hql.append("         and dd.status <> d.status ");
		hql.append("         and (cast(cast(c.equivalencias as jsonb) ->> cast(d.tipo_documento_id as varchar) as jsonb) @> ");
		hql.append("              cast(cast(dd.tipo_documento_id as varchar) as jsonb))) > 0) then true else false end ");
		hql.append(" where ");
		if (dataInicio != null) {
			hql.append(" processo.id in (select d.processo_id from documento d ");
			hql.append("          inner join documento_log dl on dl.documento_id = d.id ");
			hql.append("          where dl.data between :dataInicio and :dataFim)");
			params.put("dataInicio", dataInicio);
			params.put("dataFim", dataFim);
		}
		if (processoId != null) {
			hql.append("processo.id = :processoId");
			params.put("processoId", processoId);
		}

		params.put("status", "APROVADO");

		Query query = createSQLQuery(hql.toString(), params);

		query.executeUpdate();
	}

    public List<Documento> findByProcessoIdAndCategoriaDocumento(Long processoId, String chaveCategoria) {
		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select d from ").append(clazz.getName()).append(" d, ");
		hql.append(TipoDocumentoCategoria.class.getName()).append(" tdc ");
		hql.append(" where d.processo.id = :processoId ");
		hql.append(" and d.status <> :excluido");
		hql.append(" and d.tipoDocumento.id = tdc.tipoDocumento.id ");
		hql.append(" and tdc.categoriaDocumento.chave = :chaveCategoria ");
		hql.append(" order by d.processo.id, d.nome, d.ordem ");
		params.put("processoId", processoId);
		params.put("excluido", StatusDocumento.EXCLUIDO);
		params.put("chaveCategoria", chaveCategoria);

		Query query = createQuery(hql, params);

		return query.list();
    }

	public int countRelatorioPastaVermelhaByFiltro(RelatorioPastaVermelhaFiltro filtro) {

		StringBuilder hql = new StringBuilder();

		hql.append(" select count(*) from ").append(clazz.getName()).append(" d ");

		Map<String, Object> params = makeQueryRelatorioPastaVermelha(filtro, hql);

		Query query = createQuery(hql, params);

		return ((Number) query.uniqueResult()).intValue();
	}

	public List<RelatorioPastaVermelhaVO> findRelatorioPastaVermelhaByFiltro(RelatorioPastaVermelhaFiltro filtro, Integer inicio, Integer max) {

		StringBuilder hql = new StringBuilder();

		hql.append(" select ");
		hql.append("    p.id as processoId ");
		hql.append("    , s.nome as nomeSituacao ");
		hql.append("    , tp.id as tipoProcessoId ");
		hql.append("    , tp.nome as nomeTipoProcesso ");
		hql.append("    , to_char(p.dataCriacao, 'dd/mm/yyyy') as dataCriacao ");
		hql.append("    , to_char(p.dataEnvioAnalise, 'dd/mm/yyyy') as dataEnvioAnalise ");
		hql.append("    , ( ");
		hql.append("     	select brv.valor ");
		hql.append("    	from ").append(BaseRegistroValor.class.getName()).append(" brv, ");
		hql.append(Campo.class.getName()).append(" c2 ");
		hql.append("    	where c2.grupo.processo.id = p.id ");
		hql.append("    	    and c2.nome = :campoFormaDeIngresso ");
		hql.append("    	    and c2.valor = brv.baseRegistro.chaveUnicidade ");
		hql.append("    	    and brv.baseRegistro.baseInterna.id = :formaIngressoBaseInternaId ");
		hql.append("    	    and brv.nome = brv.baseRegistro.baseInterna.colunaLabel ");
		hql.append("    ) as formaDeIngresso ");
		hql.append("    , (	select c2.valor from  ").append(Campo.class.getName()).append(" c2 ");
		hql.append("  	    where c2.grupo.processo.id = p.id ");
		hql.append("  	    and c2.nome = :campoPeriodoDeIngresso ");
		hql.append("    ) as periodoDeIngresso ");
		hql.append("    , ( ");
		hql.append("     	select brv.valor ");
		hql.append("    	from ").append(BaseRegistroValor.class.getName()).append(" brv, ");
		hql.append(Campo.class.getName()).append(" c2 ");
		hql.append("    	where c2.grupo.processo.id = p.id ");
		hql.append("    	    and c2.nome = :campoRegional ");
		hql.append("    	    and c2.valor = brv.baseRegistro.chaveUnicidade ");
		hql.append("    	    and brv.baseRegistro.baseInterna.id = :regionalBaseInternaId ");
		hql.append("    	    and brv.nome = brv.baseRegistro.baseInterna.colunaLabel ");
		hql.append("        ) as regional ");
		hql.append("    , (	select c2.valor from  ").append(Campo.class.getName()).append(" c2 ");
		hql.append("  	    where c2.grupo.processo.id = p.id ");
		hql.append("  	    and c2.nome = :campoCodInstituicao ");
		hql.append("    ) as codInstituicao ");
		hql.append("    , ( ");
		hql.append("     	select brv.valor ");
		hql.append("    	from ").append(BaseRegistroValor.class.getName()).append(" brv, ");
		hql.append(Campo.class.getName()).append(" c2 ");
		hql.append("    	where c2.grupo.processo.id = p.id ");
		hql.append("    	    and c2.nome = :campoCodInstituicao ");
		hql.append("    	    and c2.valor = brv.baseRegistro.chaveUnicidade ");
		hql.append("    	    and brv.baseRegistro.baseInterna.id = :instituicaoBaseInternaId ");
		hql.append("    	    and brv.nome = brv.baseRegistro.baseInterna.colunaLabel ");
		hql.append("        ) as instituicao ");
		hql.append("    , (	select c2.valor from  ").append(Campo.class.getName()).append(" c2 ");
		hql.append("  	    where c2.grupo.processo.id = p.id ");
		hql.append("  	    and c2.nome = :campoCodCampus ");
		hql.append("    ) as codCampus ");
		hql.append("    , ( ");
		hql.append("     	select brv.valor ");
		hql.append("    	from ").append(BaseRegistroValor.class.getName()).append(" brv, ");
		hql.append(Campo.class.getName()).append(" c2 ");
		hql.append("    	where c2.grupo.processo.id = p.id ");
		hql.append("    	    and c2.nome = :campoCodCampus ");
		hql.append("    	    and c2.valor = brv.baseRegistro.chaveUnicidade ");
		hql.append("    	    and brv.baseRegistro.baseInterna.id = :campusBaseInternaId ");
		hql.append("    	    and brv.nome = brv.baseRegistro.baseInterna.colunaLabel ");
		hql.append("        ) as campus ");
		hql.append("    , (	select c2.valor from  ").append(Campo.class.getName()).append(" c2 ");
		hql.append("  	    where c2.grupo.processo.id = p.id ");
		hql.append("  	    and c2.nome = :campoCodCurso ");
		hql.append("    ) as codCurso ");
		hql.append("    , ( ");
		hql.append("     	select brv.valor ");
		hql.append("    	from ").append(BaseRegistroValor.class.getName()).append(" brv, ");
		hql.append(Campo.class.getName()).append(" c2 ");
		hql.append("    	where c2.grupo.processo.id = p.id ");
		hql.append("    	    and c2.nome = :campoCodCurso ");
		hql.append("    	    and c2.valor = brv.baseRegistro.chaveUnicidade ");
		hql.append("    	    and brv.baseRegistro.baseInterna.id = :cursoBaseInternaId ");
		hql.append("    	    and brv.nome = brv.baseRegistro.baseInterna.colunaLabel ");
		hql.append("        ) as curso ");
		hql.append("    , a.cpf as cpf ");
		hql.append("    , (	select c2.valor from  ").append(Campo.class.getName()).append(" c2 ");
		hql.append("  	    where c2.grupo.processo.id = p.id ");
		hql.append("  	    and c2.nome = :campoMatricula ");
		hql.append("    ) as matricula ");
		hql.append("    , (	select c2.valor from  ").append(Campo.class.getName()).append(" c2 ");
		hql.append("  	    where c2.grupo.processo.id = p.id ");
		hql.append("  	    and c2.nome = :campoNumeroCandidato ");
		hql.append("    ) as numeroCandidato ");
		hql.append("    , (	select c2.valor from  ").append(Campo.class.getName()).append(" c2 ");
		hql.append("  	    where c2.grupo.processo.id = p.id ");
		hql.append("  	    and c2.nome = :campoNumeroInscricao ");
		hql.append("    ) as numeroInscricao ");
		hql.append("    , a.nome as nomeAluno ");
		hql.append("    , (	select c2.valor from  ").append(Campo.class.getName()).append(" c2 ");
		hql.append("  	    where c2.grupo.processo.id = p.id ");
		hql.append("  	    and c2.nome = :campoTelefone ");
		hql.append("    ) as telefone ");
		hql.append("    , (	select c2.valor from  ").append(Campo.class.getName()).append(" c2 ");
		hql.append("  	    where c2.grupo.processo.id = p.id ");
		hql.append("  	    and c2.nome = :campoCelular ");
		hql.append("    ) as celular ");
		hql.append("    , (	select c2.valor from  ").append(Campo.class.getName()).append(" c2 ");
		hql.append("  	    where c2.grupo.processo.id = p.id ");
		hql.append("  	    and c2.nome = :campoEmail ");
		hql.append("    ) as email ");
		hql.append("    , td.codOrigem as codDocumento ");
		hql.append("    , d.nome as nomeDocumento ");
		hql.append("    , d.status as statusDocumento ");
		hql.append("    , to_char(d.dataDigitalizacao, 'dd/mm/yyyy') as dataDigitalizacao ");
		hql.append("    , ( ");
		hql.append("     	select brv.valor ");
		hql.append("    	from ").append(BaseRegistroValor.class.getName()).append(" brv, ");
		hql.append(Campo.class.getName()).append(" c2 ");
		hql.append("    	where c2.grupo.processo.id = p.id ");
		hql.append("    	    and c2.nome = :campoCodCampus ");
		hql.append("    	    and c2.valor = brv.baseRegistro.chaveUnicidade ");
		hql.append("    	    and brv.baseRegistro.baseInterna.id = :campusBaseInternaId ");
		hql.append("    	    and brv.nome = :colunaPoloParceiro ");
		hql.append("        ) as poloParceiro ");
		hql.append("    , p.usaTermo as usaTermo ");
		hql.append("    , i.nome as irregularidade ");
		hql.append("    , (	select c2.valor from  ").append(Campo.class.getName()).append(" c2 ");
		hql.append("  	    where c2.grupo.processo.id = p.id ");
		hql.append("  	    and c2.nome = :campoSituacaoAluno ");
		hql.append("    ) as situacaoAluno ");

		hql.append(" from ").append(clazz.getName()).append(" d ");
		hql.append(" left outer join ").append(Pendencia.class.getName()).append(" pe on pe.id = (select max(pe2.id) from ").append(Pendencia.class.getName()).append(" pe2 where pe2.documento.id = d.id) ");
		hql.append(" left outer join ").append(Irregularidade.class.getName()).append(" i on i.id = pe.irregularidade.id ");
		hql.append(" join d.processo p ");
		hql.append(" join p.situacao s ");
		hql.append(" join p.tipoProcesso tp ");
		hql.append(" join p.aluno a ");
		hql.append(" join d.tipoDocumento td ");

		Map<String, Object> params = makeQueryRelatorioPastaVermelha(filtro, hql);

		params.put("campoFormaDeIngresso", CampoMap.CampoEnum.FORMA_DE_INGRESSO.getNome());
		params.put("formaIngressoBaseInternaId", BaseInterna.FORMA_INGRESSO_ID);
		params.put("campoPeriodoDeIngresso", CampoMap.CampoEnum.PERIODO_DE_INGRESSO.getNome());
		params.put("campoRegional", CampoMap.CampoEnum.REGIONAL.getNome());
		params.put("regionalBaseInternaId", BaseInterna.REGIONAL_ID);
		params.put("campoCodInstituicao", CampoMap.CampoEnum.INSTITUICAO.getNome());
		params.put("instituicaoBaseInternaId", BaseInterna.INSTITUICAO_ID);
		params.put("campoCodCampus", CampoMap.CampoEnum.CAMPUS.getNome());
		params.put("campusBaseInternaId", BaseInterna.CAMPUS_ID);
		params.put("campoCodCurso", CampoMap.CampoEnum.CURSO.getNome());
		params.put("cursoBaseInternaId", BaseInterna.CURSO_ID);
		params.put("campoMatricula", CampoMap.CampoEnum.MATRICULA.getNome());
		params.put("campoNumeroCandidato", CampoMap.CampoEnum.NUM_CANDIDATO.getNome());
		params.put("campoNumeroInscricao", CampoMap.CampoEnum.NUM_INSCRICAO.getNome());
		params.put("campoTelefone", CampoMap.CampoEnum.TELEFONE.getNome());
		params.put("campoCelular", CampoMap.CampoEnum.CELULAR.getNome());
		params.put("campoEmail", CampoMap.CampoEnum.EMAIL.getNome());
		params.put("colunaPoloParceiro", "poloParceiro");
		params.put("campoSituacaoAluno", CampoMap.CampoEnum.SITUACAO_ALUNO.getNome());

		Query query = createQuery(hql, params);

		if (inicio != null) {
			query.setFirstResult(inicio);
		}

		if (max != null) {
			query.setMaxResults(max);
		}

		query.setResultTransformer(Transformers.aliasToBean(RelatorioPastaVermelhaVO.class));
		query.setTimeout((int) TimeUnit.MINUTES.toMillis(35));

		return query.list();
	}

	private Map<String, Object> makeQueryRelatorioPastaVermelha(RelatorioPastaVermelhaFiltro filtro, StringBuilder hql) {

		String periodosIngresso = filtro.getPeriodoIngresso();
		List<StatusProcesso> statusProcessos = filtro.getStatusProcessos();
		List<StatusDocumento> statusDocumentos = filtro.getStatusDocumentos();
		Boolean ignorarFiesProuniConcluido = filtro.getIgnorarFiesProuniConcluido();

		Map<String, Object> params = new LinkedHashMap<>();

		hql.append(" where 1=1 ");
		hql.append("    and d.obrigatorio is true ");
		hql.append("    and d.processo.tipoProcesso.id in (:tiposProcessosIds) ");
		hql.append("    and UPPER(s.nome) not like :finalSituacaoAluno ");

		if (isNotBlank(periodosIngresso)) {
			String[] periodosIngressoContem = periodosIngresso.split(",");
			hql.append("    and d.processo.id in ( ");
			hql.append("       select c.grupo.processo.id from ").append(Campo.class.getName()).append(" c ");
			hql.append("           where c.grupo.nome = :grupoDadosDoCurso ");
			hql.append("           and c.nome = :campoPeriodoDeIngresso ");
			hql.append("           and UPPER(c.valor) in (:periodosIngressoContem) ");
			hql.append("    ) ");
			params.put("periodosIngressoContem", periodosIngressoContem);
		}

		if (isNotEmpty(statusProcessos)) {
			hql.append(" and d.processo.status in (:statusProcesso) ");
		}

		if (isNotEmpty(statusDocumentos)) {
			hql.append(" and d.status in (:statusDocumento) ");
		}

		if (ignorarFiesProuniConcluido != null && ignorarFiesProuniConcluido) {
			hql.append( " and d.processo.numCandidato not in ( ");
			hql.append( "   select p.numCandidato from ").append(Processo.class.getName()).append(" p ");
			hql.append( "   where p.status = :statusProcessoConcluido ");
			hql.append( "       and p.tipoProcesso.id in :idsFiesProuni ");
			hql.append( "       and p.numCandidato is not null ");
			hql.append( " )");
		}

		params.put("statusProcesso", statusProcessos);
		params.put("statusDocumento", statusDocumentos);
		params.put("idsFiesProuni", Arrays.asList(TipoProcesso.SIS_PROUNI, TipoProcesso.SIS_FIES));
		params.put("tiposProcessosIds", Arrays.asList(TipoProcesso.MSV_EXTERNA, TipoProcesso.MSV_INTERNA, TipoProcesso.TRANSFERENCIA_EXTERNA, TipoProcesso.VESTIBULAR, TipoProcesso.ENEM));
		params.put("statusAprovado",StatusDocumento.APROVADO);
		params.put("statusProcessoConcluido", StatusProcesso.CONCLUIDO);
		params.put("grupoDadosDoCurso", CampoMap.CampoEnum.PERIODO_DE_INGRESSO.getGrupo().getNome());
		params.put("campoPeriodoDeIngresso", CampoMap.CampoEnum.PERIODO_DE_INGRESSO.getNome());
		params.put("finalSituacaoAluno", "%" + Situacao.ALUNO);

		return params;
	}

	public void atualizarNotificadoSia(Long documentoId, boolean notificadoSia) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" update documento ");
		hql.append(" 	set notificado_sia = :notificadoSia ");
		hql.append(" where ");
		hql.append(" 	id = :documentoId ");

		params.put("notificadoSia", notificadoSia);
		params.put("documentoId", documentoId);

		Query query = createSQLQuery(hql, params);

		query.executeUpdate();
	}

	public void atualizarModeloDocumento(Long documentoId, Long modeloDocumentoId) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" update documento ");
		hql.append(" 	set modelo_documento_id = :modeloDocumentoId ");
		hql.append(" where ");
		hql.append(" 	id = :documentoId ");

		params.put("modeloDocumentoId", modeloDocumentoId);
		params.put("documentoId", documentoId);

		Query query = createSQLQuery(hql, params);

		query.executeUpdate();
	}

	public List<Object[]> findIdsToTipificacaoFluxoAprovacao(Long processoId) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new LinkedHashMap<>();

		hql.append(" select i.documento.id, i.id ");
		hql.append(" from ");
		hql.append(" 	").append(Imagem.class.getName()).append(" i, ");
		hql.append(" 	").append(ImagemMeta.class.getName()).append(" im ");
		hql.append(" where im.imagemId = i.id ");
		hql.append(" 	and i.documento.grupoModeloDocumento is null ");
		hql.append(" 	and i.documento.status in ( :status ) ");
		hql.append(" 	and i.documento.nome <> :nome ");
		hql.append(" 	and i.documento.processo.id = :processo");
		hql.append(" 	and (i.existente = null or i.existente = true) ");
		hql.append(" order by i.documento.processo.id, i.id asc");

		params.put("nome", Documento.NOME_OUTROS);
		params.put("status", Arrays.asList(StatusDocumento.DIGITALIZADO));
		params.put("processo", processoId);

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}

	public List<Object[]> findIdsToOCrFluxoAprovacao(Long processoId) {
		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new LinkedHashMap<>();

		hql.append(" select i.documento.id, i.id ");
		hql.append(" from ");
		hql.append(" 	").append(Imagem.class.getName()).append(" i, ");
		hql.append(" 	").append(ImagemMeta.class.getName()).append(" im ");
		hql.append(" where im.imagemId = i.id ");
		hql.append(" 	and i.documento.grupoModeloDocumento is not null ");
		hql.append(" 	and i.documento.status in ( :status ) ");
		hql.append(" 	and i.documento.processo.id = :processo");
		hql.append(" order by i.documento.processo.id, i.id asc");

		params.put("status", StatusDocumento.DIGITALIZADO);
		params.put("processo", processoId);

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}

	public List<Long> findIdsByFiltro(DocumentoFiltro filtro, Integer inicio, Integer max) {
		StringBuilder hql = new StringBuilder();

		hql.append(" select d.id from ").append(Documento.class.getName()).append(" d ");

		Map<String, Object> params = makeQuery(filtro, hql);
		hql.append(" order by d.ordem");

		Query query = createQuery(hql.toString(), params);

		if(inicio != null) {
			query.setFirstResult(inicio);
		}
		if(max != null) {
			query.setMaxResults(max);
		}

		List<Long> list = query.list();
		return list;
	}
}