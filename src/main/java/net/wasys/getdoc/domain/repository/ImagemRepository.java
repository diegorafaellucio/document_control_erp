package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.GetdocConstants;
import net.wasys.getdoc.domain.entity.CampoGrupo;
import net.wasys.getdoc.domain.entity.Documento;
import net.wasys.getdoc.domain.entity.Imagem;
import net.wasys.getdoc.domain.entity.TipoDocumento;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@SuppressWarnings("unchecked")
public class ImagemRepository extends HibernateRepository<Imagem> {

	public ImagemRepository() {
		super(Imagem.class);
	}

	public List<Imagem> findByDocumento(Long documentoId) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" where documento.id = ? ");
		params.add(documentoId);

		hql.append(" order by id ");

		Query query = createQuery(hql, params);

		return query.list();
	}

	public Integer countByDocumento(Long documentoId) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select count(*) from ").append(Imagem.class.getName());
		hql.append(" where documento.id = ? ");
		params.add(documentoId);

		Query query = createQuery(hql, params);

		return ((Number) query.uniqueResult()).intValue();
	}

	public Integer countByDocumento(Long documentoId, Integer versao) {

		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<>();

		hql.append(" select count(*) from ").append(Imagem.class.getName());
		hql.append(" where documento.id = ? ");
		params.add(documentoId);

		if(versao != null) {
			hql.append(" and versao = ? ");
			params.add(versao);
		}

		Query query = createQuery(hql, params);

		return ((Number) query.uniqueResult()).intValue();
	}

	public Integer countByProcessoId(Long processoId) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select count(*) from ").append(Imagem.class.getName());
		hql.append(" where documento.processo.id = :processoId ");
		params.put("processoId", processoId);

		Query query = createQuery(hql, params);

		return ((Number) query.uniqueResult()).intValue();
	}

	public List<Imagem> findByDocumentoVersao(Long documentoId, Integer versao) {

		Map<String,Object> params = new HashMap<String,Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" where documento.id = :documentoId and versao = :versao ");
		params.put("documentoId", documentoId);
		params.put("versao", versao);

		hql.append(" order by id ");

		Query query = createQuery(hql, params);

		return query.list();
	}

	public List<Imagem> findVersaoAtualByDocumento(Long documentoId) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" where documento.id = ? ");
		params.add(documentoId);
		hql.append(" and documento.versaoAtual = versao ");

		hql.append(" order by id ");

		Query query = createQuery(hql, params);

		return query.list();
	}

	public Imagem findVersaoAtualUnicaByDocumento(Long documentoId){
		Imagem imagem= null;
		List<Imagem> list = findVersaoAtualByDocumento(documentoId);
		if (null != list && list.size() > 0) {
			imagem = list.get(0);
		}
		return imagem;
	}

	public List<Long> findIdsByDataDigitalizacao(Date dataInicio, Date dataFim) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<String, Object>();

		hql.append(" select i.id from ").append(clazz.getName()).append(" i ");
		hql.append(" where i.documento.dataDigitalizacao > :dataInicio ");
		hql.append(" and i.documento.dataDigitalizacao < :dataFim ");
		hql.append(" order by id ");

		params.put("dataInicio", dataInicio);
		params.put("dataFim", dataFim);

		Query query = createQuery(hql, params);

		return query.list();
	}

	public List<Imagem> findByIds(List<Long> ids) {

		StringBuilder hql = new StringBuilder();
		hql.append(" from ").append(clazz.getName()).append(" i ");

		hql.append(" where i.id in (-1");
		for (Long id : ids) {
			hql.append(", ").append(id);
		}
		hql.append(") ");

		Query query = createQuery(hql.toString());
		query.setFetchSize(100);

		return query.list();
	}

	public boolean isUnicaExtensao(Long documentoId, Integer versao, List<String> imagemExtensoes) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<>();

		hql.append(" select count(*) from ").append(clazz.getName()).append(" i ");

		hql.append(" where i.documento.id = :documentoId");
		params.put("documentoId", documentoId);

		hql.append(" and i.extensao not in ( '-1' ");
		for (String extensao : imagemExtensoes) {
			hql.append(", '").append(extensao).append("'");
		}
		hql.append(" ) ");

		if(versao != null){
			hql.append(" and i.versao = :versao ");
			params.put("versao", versao);
		}

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue() == 0;
	}

	public boolean isImagem(Long documentoId, Integer versao, Long imagemId) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<>();

		hql.append(" select i.extensao from ").append(clazz.getName()).append(" i ");

		hql.append(" where i.documento.id = :documentoId");
		params.put("documentoId", documentoId);

		hql.append(" and i.id = :imagemId");
		params.put("imagemId", imagemId);

		if(versao != null){
			hql.append(" and i.versao = :versao ");
			params.put("versao", versao);
		}

		Query query = createQuery(hql.toString(), params);

		return GetdocConstants.IMAGEM_EXTENSOES.contains((String) query.uniqueResult());
	}

	public Imagem getPrimeiraImagem(Long documentoId) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<>();

		hql.append(" from ").append(clazz.getName()).append(" i ");

		hql.append(" where i.id = ( ");
		hql.append(" 	select min(i2.id) from ").append(clazz.getName()).append(" i2 ");
		hql.append(" 	where i2.documento.id = :documentoId ");
		hql.append(" 	and i2.documento.versaoAtual = i2.versao ");
		hql.append(" ) ");
		params.put("documentoId", documentoId);

		Query query = createQuery(hql.toString(), params);

		return (Imagem) query.uniqueResult();
	}

	public void excluirByDocumento(Long documentoId) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<>();

		hql.append(" delete from ").append(clazz.getName()).append(" where documento.id = :documentoId");
		params.put("documentoId", documentoId);

		Query query = createQuery(hql.toString(), params);

		query.executeUpdate();
	}

	public List<Long> findIdsToPreparacao() {

		StringBuilder hql = new StringBuilder();
		hql.append(" select i.id from ").append(clazz.getName()).append(" i where i.preparada is false order by i.id ");

		Query query = createQuery(hql.toString());

		return query.list();
	}

	public List<Long> findIdsToFullText() {

		StringBuilder hql = new StringBuilder();
		hql.append(" select i.id from ").append(clazz.getName()).append(" i where i.aguardandoFulltext is true order by i.id ");

		Query query = createQuery(hql.toString());

		return query.list();
	}

	public void atualizaPreparacao(Long documentoId, boolean preparada) {

		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<Object>();

		hql.append(" update ").append(clazz.getName());
		hql.append(" set preparada = ? ");
		hql.append(" where documento.id = ? ");
		params.add(preparada);
		params.add(documentoId);

		Query query = createQuery(hql.toString(), params);

		query.executeUpdate();
	}

	public List<Imagem> findByTipoDocumentoToAmostragem(TipoDocumento tipoDocumento, int max) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<>();

		hql.append(startQuery());
		hql.append(" where documento.tipoDocumento.id = :tipoDocumentoId ");
		hql.append(" and versao = documento.versaoAtual ");
		hql.append(" and documento.nome not like '%ALUNO%' ");
		hql.append(" and documento.nome not like '%COMPOSIÇÃO FAMILIAR%' ");
		hql.append(" and documento.nome <> :nomeOutros ");
		hql.append(" order by id desc ");

		Long tipoDocumentoId = tipoDocumento.getId();
		params.put("tipoDocumentoId", tipoDocumentoId);
		params.put("nomeOutros", Documento.NOME_OUTROS);

		Query query = createQuery(hql.toString(), params);

		query.setMaxResults(max);

		return query.list();
	}

	public boolean campoGrupoTemImagensRelacionadas(CampoGrupo campoGrupo) {

		Map<String,Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select count(i) from ").append(Imagem.class.getName()).append(" i ");
		hql.append(" where 1=1 ");
		hql.append(" and i.documento.grupoRelacionado = :campoGrupo ");

		params.put("campoGrupo", campoGrupo);

		Query query = createQuery(hql, params);

		return ((Number) query.uniqueResult()).intValue() > 0;
	}

	public List<Long> findToVerificarGeral(Long apartirDe, int qtdRegistros) {

		Map<String,Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append("select i.id from ").append(clazz.getName()).append(" i ");
		hql.append(" where i.id > :apartirDe ");
		hql.append(" order by i.id");

		params.put("apartirDe", apartirDe);

		Query query = createQuery(hql, params);

		query.setMaxResults(qtdRegistros);

		return query.list();
	}

	public Long getFirstRegistro() {

		StringBuilder hql = new StringBuilder();

		hql.append("select min(id) from ").append(clazz.getName());

		Query query = createQuery(hql);

		return (Long) query.uniqueResult();
	}

	public boolean exists(Long imagemId) {

		Map<String,Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select 1 from ").append(clazz.getName()).append(" i ");
		hql.append(" where i.id = :imagemId ");

		params.put("imagemId", imagemId);

		Query query = createQuery(hql, params);

		return query.uniqueResult() != null;
	}

	public List<Long> findImagensInexistentes() {
		Map<String,Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select i.id from ").append(clazz.getName()).append(" i ");
		hql.append(" where i.existente is false ");

		Query query = createQuery(hql, params);

		return query.list();
	}
}
