package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.Documento;
import net.wasys.getdoc.domain.entity.DocumentoLog;
import net.wasys.getdoc.domain.entity.Pendencia;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.AcaoDocumento;
import net.wasys.getdoc.domain.enumeration.StatusDocumento;
import net.wasys.getdoc.domain.vo.filtro.DocumentoLogFiltro;
import net.wasys.util.ddd.HibernateRepository;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@SuppressWarnings("unchecked")
public class DocumentoLogRepository extends HibernateRepository<DocumentoLog> {

	public DocumentoLogRepository() {
		super(DocumentoLog.class);
	}

	public List<DocumentoLog> findByProcesso(Long processoId) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());

		hql.append(" where documento.processo.id = ? ");
		params.add(processoId);

		hql.append(" order by id ");

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}

	public List<Documento> findDocumentoByFiltro(DocumentoLogFiltro filtro, Integer inicio, Integer max) {

		StringBuilder hql = new StringBuilder();
		hql.append("select dl.documento from ").append(DocumentoLog.class.getName()).append(" dl ");
		Map<String, Object> params = makeQuery(filtro, hql);
		hql.append(" order by dl.documento.id desc");

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

	public Date getLastDataAlteracao(Long documentoId) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<>();
		hql.append(" select max(dl.data) from ").append(DocumentoLog.class.getName()).append(" dl ");
		hql.append(" where dl.documento.id = :documentoId ");
		params.put("documentoId", documentoId);

		Query query = createQuery(hql.toString(), params);

		return (Date) query.uniqueResult();
	}

	private Map<String, Object> makeQuery(DocumentoLogFiltro filtro, StringBuilder hql) {
		Map<String, Object> params = new LinkedHashMap<>();

		Date dataInicio = filtro.getDataInicio();
		Date dataFim = filtro.getDataFim();
		Usuario usuario = filtro.getUsuario();
		AcaoDocumento acaoDocumento = filtro.getAcaoDocumento();
		List<Long> documentoIds = filtro.getDocumentoIds();
		Pendencia pendencia = filtro.getPendencia();
		String observacao = filtro.getObservacao();
		List<StatusDocumento> statusDifetenteDeList = filtro.getStatusDifetenteDeList();
		boolean diferenteDeOutros = filtro.isDiferenteDeOutros();
		boolean diferenteDeTipificando = filtro.isDiferenteDeTipificando();


		hql.append(" where 1=1 ");

		if(documentoIds != null) {
			hql.append(" and dl.documento.id in ( :documentoIds ) ");
			params.put("documentoIds", documentoIds);
		}

		if(dataInicio != null && dataFim != null) {
			hql.append(" and dl.data between :dataInicio and :dataFim ");
			params.put("dataInicio", dataInicio);
			params.put("dataFim", dataFim);
		}

		if(usuario != null) {
			hql.append(" and dl.usuario.id = :usuarioId");
			params.put("usuarioId", usuario.getId());
		}

		if(acaoDocumento != null) {
			hql.append(" and dl.acaoDocumento = :acaoDocumento");
			params.put("acaoDocumento", acaoDocumento);
		}

		if(pendencia != null) {
			hql.append(" and dl.pendencia.id = :pendenciaId");
			params.put("pendenciaId", pendencia.getId());
		}

		if(StringUtils.isNotBlank(observacao)) {
			hql.append(" and upper(dl.observacao) like upper( :observacao )");
			params.put("observacao", observacao);
		}

		if(diferenteDeOutros) {
			hql.append(" and dl.documento.nome <> :outros");
			params.put("outros", Documento.NOME_OUTROS);
		}

		if(diferenteDeTipificando) {
			hql.append(" and dl.documento.nome <> :tificando");
			params.put("tificando", Documento.NOME_TIFICANDO);
		}

		if(statusDifetenteDeList != null && !statusDifetenteDeList.isEmpty()) {
			hql.append(" and dl.documento.status not in (:statusDiferente)");
			params.put("statusDiferente", statusDifetenteDeList);
		}

		return params;
	}

	public DocumentoLog findLastByDocumentoAndAcaoDocumento(Long documentoId, List<AcaoDocumento> acoesDocumentos) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" dl ");
		hql.append(" 	left outer join fetch dl.usuario ");

		hql.append(" where 1=1 ");
		hql.append(" and dl.documento.id = :documentoId ");
		hql.append(" and dl.acao in (:acoesDocumentos) ");

		params.put("documentoId", documentoId);
		params.put("acoesDocumentos", acoesDocumentos);

		hql.append(" order by dl.id desc ");

		Query query = createQuery(hql.toString(), params);
		query.setMaxResults(1);

		return (DocumentoLog) query.uniqueResult();
	}
}
