package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.Resposta;
import net.wasys.getdoc.domain.vo.filtro.TipoDocumentoFiltro;
import net.wasys.util.ddd.HibernateRepository;
import org.apache.commons.lang.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@SuppressWarnings("unchecked")
public class TipoDocumentoRepository extends HibernateRepository<TipoDocumento> {

	private final static long TIMEOUT_CACHE = (1000 * 60 * 10);//10 minutos

	public TipoDocumentoRepository() {
		super(TipoDocumento.class);
	}

	public int countByFiltro(TipoDocumentoFiltro filtro) {
		StringBuilder hql = new StringBuilder();
		hql.append(" select count(*) from ").append(clazz.getName()).append(" td ");

		Map<String, Object> params = makeQuery(filtro, hql);

		Query query = createQuery(hql.toString(), params);
		return ((Number) query.uniqueResult()).intValue();
	}

	public List<TipoDocumento> findByFiltro(TipoDocumentoFiltro filtro, Integer inicio, Integer max) {
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" td ");

		Map<String, Object> params = makeQuery(filtro, hql);
		hql.append(" order by td.ordem ");

		Query query = createQuery(hql.toString(), params);

		if(inicio != null && max != null) {
			query.setFirstResult(inicio);
			query.setMaxResults(max);
		}

		List<TipoDocumento> list = query.list();
		Set<TipoDocumento> set = new LinkedHashSet<>(list);
		return new ArrayList<>(set);
	}

	private Map<String, Object> makeQuery(TipoDocumentoFiltro filtro, StringBuilder hql) {

		Map<String, Object> params = new HashMap<>();

		String nome = filtro.getNome();
		String nomeLike = filtro.getNomeLike();
		Long tipoProcessoId = filtro.getTipoProcessoId();
		Resposta obrigatorio = filtro.getObrigatorio();
		Resposta ativo = filtro.getAtivo();
		Long id = filtro.getId();
		Long codOrigem = filtro.getCodOrigem();

		hql.append(" where 1=1 ");

		if(StringUtils.isNotBlank(nome)) {
			hql.append(" and upper(td.nome) = :nome ");
			params.put("nome", nome.toUpperCase());
		}

		if(StringUtils.isNotBlank(nomeLike)) {
			hql.append(" and upper(td.nome) like :nomeLike ");
			params.put("nomeLike", "%"+ nomeLike.toUpperCase() +"%");
		}

		if(tipoProcessoId != null) {
			hql.append(" and td.tipoProcesso.id = :tipoProcessoId ");
			params.put("tipoProcessoId", tipoProcessoId);
		}

		if(obrigatorio != null && !Resposta.TODOS.equals(obrigatorio)) {
			hql.append(" and td.obrigatorio = :obrigatorio ");
			params.put("obrigatorio", Resposta.SIM.equals(obrigatorio) ? true : false);
		}

		if(ativo != null && !Resposta.TODOS.equals(ativo)) {
			hql.append(" and td.ativo = :ativo ");
			params.put("ativo", Resposta.SIM.equals(ativo) ? true : false);
		}

		if(id != null) {
			hql.append(" and td.id = :id ");
			params.put("id", id);
		}

		if(codOrigem != null) {
			hql.append(" and td.codOrigem = :codOrigem ");
			params.put("codOrigem", codOrigem);
		}

		return params;
	}

	public List<TipoDocumento> findByTipoProcesso(Long tipoProcessoId, Integer inicio, Integer max) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());

		if(tipoProcessoId != null) {
			hql.append(" where tipoProcesso.id = ? ");
			params.add(tipoProcessoId);
		}else{
			hql.append(" where 1=1 ");
		}

		hql.append(" order by ordem ");

		Query query = createQuery(hql, params);

		if(inicio != null) {
			query.setFirstResult(inicio);
		}
		if(max != null) {
			query.setMaxResults(max);
		}

		return query.list();
	}

	public int countByTipoProcesso(Long tipoProcessoId) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select count(*) from " + clazz.getName());
		hql.append(" where tipoProcesso.id = ? ");
		params.add(tipoProcessoId);

		Query query = createQuery(hql, params);

		return ((Number) query.uniqueResult()).intValue();
	}

	public TipoDocumento getAnterior(Long tipoProcessoId, Integer ordem) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" where tipoProcesso.id = ? ");
		params.add(tipoProcessoId);

		hql.append(" and ordem < ? ");
		params.add(ordem);

		hql.append(" order by ordem desc ");

		Query query = createQuery(hql, params);

		query.setMaxResults(1);

		return (TipoDocumento) query.uniqueResult();
	}

	public TipoDocumento getProximo(Long tipoProcessoId, Integer ordem) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" where tipoProcesso.id = ? ");
		params.add(tipoProcessoId);

		hql.append(" and ordem > ? ");
		params.add(ordem);

		hql.append(" order by ordem asc ");

		Query query = createQuery(hql, params);

		query.setMaxResults(1);

		return (TipoDocumento) query.uniqueResult();
	}

	public boolean temTipificavel(Long tipoProcessoId) {

		List<Object> params = new ArrayList<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select count(*) from ").append(TipoDocumento.class.getName()).append(" td ");
		hql.append(" where td.tipoProcesso.id = ? ");
		hql.append(" and td.id in ( ");
		hql.append(" 	select tdm.tipoDocumento.id from ").append(TipoDocumentoModelo.class.getName()).append(" tdm where tdm.tipoDocumento.id = td.id ");
		hql.append(" ) ");
		params.add(tipoProcessoId);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).longValue() > 0;
	}

	public TipoDocumento getByCodOrigem(Long tipoProcessoId, Long codOrgiem) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" where tipoProcesso.id = :tipoProcessoId ");
		params.put("tipoProcessoId", tipoProcessoId);

		if(codOrgiem != null) {
			hql.append(" and codOrigem = :codOrigem ");
			params.put("codOrigem", codOrgiem);
		}

		Query query = createQuery(hql, params);

		return (TipoDocumento) query.uniqueResult();
	}

	public List<ModeloDocumento> findModelosDocumentos(Long tipoDocumentoId) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select tdm.modeloDocumento from ").append(TipoDocumentoModelo.class.getName()).append(" tdm ");
		hql.append(" where tdm.tipoDocumento.id = :tipoDocumentoId ");
		hql.append(" order by tdm.modeloDocumento.descricao ");

		params.put("tipoDocumentoId", tipoDocumentoId);

		Query query = createQuery(hql, params);

		return query.list();
	}

	public void saveOrUpdateModeloDocumento(TipoDocumentoModelo tipoDocumentoModelo) {
		try {
			Session session = getSession();
			session.saveOrUpdate(tipoDocumentoModelo);
			session.flush();
		}
		catch (HibernateException e) {
			handleException(e, tipoDocumentoModelo);
		}
	}

	public void deleteModeloDocumentoById(Long tipoDocumentoId, Long tipoDocumentoModeloId) {
		Query query = createQuery("delete " + TipoDocumentoModelo.class.getName() + " tdm where tdm.tipoDocumento.id = :tipoDocumentoId and tdm.modeloDocumento.id = :tipoDocumentoModeloId");
		query.setParameter("tipoDocumentoId", tipoDocumentoId);
		query.setParameter("tipoDocumentoModeloId", tipoDocumentoModeloId);
		query.executeUpdate();
	}

	public List<TipoDocumento> findByModeloDocumento(Long modeloDocumentoId) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select td from ").append(TipoDocumento.class.getName()).append(" td, ");
		hql.append(TipoDocumentoModelo.class.getName()).append(" tdm ");
		hql.append(" where tdm.tipoDocumento.id = td.id ");
		hql.append(" and tdm.modeloDocumento.id = :modeloDocumentoId ");
		params.put("modeloDocumentoId", modeloDocumentoId);

		Query query = createQuery(hql, params);

		return query.list();
	}

    public List<TipoDocumento> findByReaproveitavel(Long tipoProcessoId) {
		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" where tipoProcesso.id = :tipoProcessoId and codOrigem is not null ");
		params.put("tipoProcessoId", tipoProcessoId);

		Query query = createQuery(hql, params);

		return query.list();
	}

	public List<Long> findIdsBySuperfilAndProcessoId(Subperfil subperfil, Long processoId) {
		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append("select td.id from ").append(clazz.getName()).append(" td ");
		hql.append(" where td.id in ( select d.tipoDocumento.id from ").append(Documento.class.getName()).append(" d");
		hql.append(" where d.processo.id = :processoId");
		hql.append(" and d.tipoDocumento.id in ( select sptd.tipoDocumento.id from ").append(SubperfilTipoDocumento.class.getName()).append(" sptd");
		hql.append(" where sptd.tipoDocumento = d.tipoDocumento and sptd.subperfil = :subperfil))");
		params.put("subperfil", subperfil);
		params.put("processoId", processoId);

		Query query = createQuery(hql, params);
		return query.list();
	}

	public List<Long> findCodSia(List<Long> tiposDocumentosIds) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select td.codOrigem from ").append(clazz.getName()).append(" td ");
		hql.append(" where td.id in (:tiposDocumentosIds) ");

		params.put("tiposDocumentosIds", tiposDocumentosIds);

		Query query = createQuery(hql, params);

		return listCache(params, query, TIMEOUT_CACHE);
	}

	public List<TipoDocumento> findByIds(List<Long> tiposDocumentosIds) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(startQuery()).append(" td ");
		hql.append(" where td.id in (:ids) ");

		params.put("ids", tiposDocumentosIds);

		Query query = createQuery(hql, params);

		return query.list();
	}

	public TipoDocumentoModelo findByTipoDocumentoAndModeloDocumento(TipoDocumento tipoDocumento, ModeloDocumento modeloDocumento) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select tdm from ").append(TipoDocumentoModelo.class.getName()).append(" tdm ");
		hql.append(" where tdm.tipoDocumento = :tipoDocumento ");
		hql.append(" and tdm.modeloDocumento = :modeloDocumento ");

		params.put("tipoDocumento", tipoDocumento);
		params.put("modeloDocumento", modeloDocumento);

		Query query = createQuery(hql, params);

		return (TipoDocumentoModelo) query.uniqueResult();
	}

	public List<ModeloDocumento> findModelosDocumentoToRequisitarExpiracao(Long tipoDocumentoId) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select tdm.modeloDocumento from ").append(TipoDocumentoModelo.class.getName()).append(" tdm ");
		hql.append(" where tdm.tipoDocumento.id = :tipoDocumentoId and tdm.requisitarDataValidadeExpiracao = true");
		hql.append(" order by tdm.modeloDocumento.descricao ");

		params.put("tipoDocumentoId", tipoDocumentoId);

		Query query = createQuery(hql, params);

		return query.list();
	}

	public List<CategoriaDocumento> findCategoriasDocumento(Long tipoDocumentoId) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select tdc.categoriaDocumento from ").append(TipoDocumentoCategoria.class.getName()).append(" tdc ");
		hql.append(" where tdc.tipoDocumento.id = :tipoDocumentoId ");
		hql.append(" order by tdc.categoriaDocumento.descricao ");

		params.put("tipoDocumentoId", tipoDocumentoId);

		Query query = createQuery(hql, params);

		return query.list();
	}

	public List<TipoDocumento> findByCategoriaDocumento(String chaveCategoria) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select td from ").append(TipoDocumento.class.getName()).append(" td, ");
		hql.append(TipoDocumentoCategoria.class.getName()).append(" tdc ");
		hql.append(" where tdc.tipoDocumento.id = td.id ");
		hql.append(" and tdc.categoriaDocumento.chave = :categoriaChave ");
		params.put("categoriaChave", chaveCategoria);

		Query query = createQuery(hql, params);

		return query.list();
	}

	public void saveOrUpdateCategoriaDocumento(TipoDocumentoCategoria tipoDocumentoCategoria) {
		try {
			Session session = getSession();
			session.saveOrUpdate(tipoDocumentoCategoria);
			session.flush();
		}
		catch (HibernateException e) {
			handleException(e, tipoDocumentoCategoria);
		}
	}

	public void deleteCategoriaDocumentoById(Long tipoDocumentoId, Long tipoDocumentoCategoriaId) {
		Query query = createQuery("delete " + TipoDocumentoCategoria.class.getName() + " tdc where tdc.tipoDocumento.id = :tipoDocumentoId and tdc.categoriaDocumento.id = :tipoDocumentoCategoriaId");
		query.setParameter("tipoDocumentoId", tipoDocumentoId);
		query.setParameter("tipoDocumentoCategoriaId", tipoDocumentoCategoriaId);
		query.executeUpdate();
	}

    public List<Long> findIdsByGrupoRelacionadoIdAndProcessoId(Long grupoRelacionadoId, Long processoId) {
		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select d.tipoDocumento.id from ").append(Documento.class.getName()).append(" d ");
		hql.append(" where d.grupoRelacionado.id = :grupoRelacionadoId ");
		hql.append(" and d.processo.id = :processoId ");

		params.put("grupoRelacionadoId", grupoRelacionadoId);
		params.put("processoId", processoId);

		Query query = createQuery(hql, params);

		return query.list();
    }

	public List<GrupoModeloDocumento> findGruposModeloDocumentos(Long tipoDocumentoId) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select tdgd.grupoModeloDocumento from ").append(TipoDocumentoGrupoModeloDocumento.class.getName()).append(" tdgd ");
		hql.append(" where tdgd.tipoDocumento.id = :tipoDocumentoId ");
		hql.append(" order by tdgd.grupoModeloDocumento.descricao ");

		params.put("tipoDocumentoId", tipoDocumentoId);

		Query query = createQuery(hql, params);

		return query.list();
	}

	public void saveOrUpdateGrupoModeloDocumento(TipoDocumentoGrupoModeloDocumento tipoDocumentoGrupoModeloDocumento) {
		try {
			Session session = getSession();
			session.saveOrUpdate(tipoDocumentoGrupoModeloDocumento);
			session.flush();
		}
		catch (HibernateException e) {
			handleException(e, tipoDocumentoGrupoModeloDocumento);
		}
	}

	public void deleteGrupoModeloDocumentoById(Long tipoDocumentoId, Long tipoDocumentoGrupoModeloDocumentoId) {
		Query query = createQuery("delete " + TipoDocumentoGrupoModeloDocumento.class.getName() + " tdgmd where tdgmd.tipoDocumento.id = :tipoDocumentoId and tdgmd.grupoModeloDocumento.id = :tipoDocumentoGrupoModeloDocumentoId");
		query.setParameter("tipoDocumentoId", tipoDocumentoId);
		query.setParameter("tipoDocumentoGrupoModeloDocumentoId", tipoDocumentoGrupoModeloDocumentoId);
		query.executeUpdate();
	}

	public List<GrupoModeloDocumento> findGruposModeloDocumentoToRequisitarExpiracao(Long tipoDocumentoId) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select tdgmd.grupoModeloDocumento from ").append(TipoDocumentoGrupoModeloDocumento.class.getName()).append(" tdgmd ");
		hql.append(" where tdgmd.tipoDocumento.id = :tipoDocumentoId and tdgmd.requisitarDataValidadeExpiracao = true");
		hql.append(" order by tdgmd.grupoModeloDocumento.descricao ");

		params.put("tipoDocumentoId", tipoDocumentoId);

		Query query = createQuery(hql, params);

		return query.list();
	}
}
