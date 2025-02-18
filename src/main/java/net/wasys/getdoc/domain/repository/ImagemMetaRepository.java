package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.Origem;
import net.wasys.getdoc.domain.vo.filtro.ImagemMetaFiltro;
import net.wasys.util.ddd.HibernateRepository;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class ImagemMetaRepository extends HibernateRepository<ImagemMeta> {

	public ImagemMetaRepository() {
		super(ImagemMeta.class);
	}

	public ImagemMeta getByImagem(Long imagemId) {

		Map<String,Object> params = new HashMap<String,Object>();
		StringBuilder hql = new StringBuilder();

		hql.append("select i from ").append(clazz.getName()).append(" i ");
		hql.append("where i.imagemId = :imagemId ");
		params.put("imagemId", imagemId);

		Query query = createQuery(hql, params);
		query.setMaxResults(1);

		return (ImagemMeta) query.uniqueResult();
	}



	public List<ImagemMeta> findByFiltro(ImagemMetaFiltro filtro) {

		Map<String,Object> params = new HashMap<String,Object>();
		StringBuilder hql = new StringBuilder();


		hql.append("select im from ").append(clazz.getName()).append(" im ");
		hql.append(" join " + Imagem.class.getName() + " i on i.id = im.imagemId ");
		hql.append(" join " + Documento.class.getName() + " d on d.id = i.documento.id ");


		params = makeQuery(filtro, hql);

		Query query = createQuery(hql.toString(), params);

		return query.list();

	}

	public void updateFullText(Long imagemId, String fullText) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder sql = new StringBuilder();

		sql.append(" update imagem_meta ");
		sql.append(" set full_text = :fullText ");
		sql.append(" where imagem_id = :imagemId ");

		params.put("fullText", fullText);
		params.put("imagemId", imagemId);

		Query query = createSQLQuery(sql, params);
		query.executeUpdate();
	}

	public String getFullText(Long imagemId) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select fullText ");
		hql.append(" from ").append(ImagemMeta.class.getName());
		hql.append(" where imagemId = :imagemId ");

		params.put("imagemId", imagemId);

		Query query = createQuery(hql.toString(), params);
		String fullText = (String) query.uniqueResult();

		return fullText;
	}

	public void atualizarInfoImagemMeta(Long imagemId, long tamanho, Integer width, Integer height) {

		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<Object>();

		hql.append(" update ").append(clazz.getName());

		hql.append(" set tamanho = ? ");
		params.add(tamanho);
		if(width != null) {
			hql.append(" , width = ? ");
			params.add(width);
		}
		if(height != null) {
			hql.append(" , height = ? ");
			params.add(height);
		}

		hql.append(" where imagemId = ? ");
		params.add(imagemId);

		Query query = createQuery(hql.toString(), params);

		query.executeUpdate();
	}

	public ImagemMeta getPrimeiraImagem(Long documentoId) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<>();

		hql.append(" from ").append(clazz.getName()).append(" im ");

		hql.append(" where im.imagemId = ( ");
		hql.append(" 	select min(i.id) from ").append(Imagem.class.getName()).append(" i ");
		hql.append(" 	where i.documento.id = :documentoId ");
		hql.append(" 	and i.documento.versaoAtual = i.versao ");
		hql.append(" ) ");
		params.put("documentoId", documentoId);

		Query query = createQuery(hql.toString(), params);

		return (ImagemMeta) query.uniqueResult();
	}

	private Map<String, Object> makeQuery(ImagemMetaFiltro filtro, StringBuilder sql) {

		Date dataInicio = filtro.getDataInicio();
		Date dataFim = filtro.getDataFim();
		List<ModeloDocumento> modeloDocumentoList = filtro.getModeloDocumentoList();
		List<TipoProcesso> tipoProcessoList = filtro.getTipoProcessoList();
		Boolean tipificado = filtro.getTipificado();
		Origem origem = filtro.getOrigemProcesso();
		Long documentoId = filtro.getDocumentoId();

		Map<String, Object> params = new LinkedHashMap<>();

		sql.append(" where 1=1 ");

		if (dataInicio != null && dataFim != null) {

			dataInicio = DateUtils.truncate(dataInicio, Calendar.DAY_OF_MONTH);

			Calendar dataFimC = Calendar.getInstance();
			dataFimC.setTime(dataFim);
			dataFimC.add(Calendar.DAY_OF_MONTH, 1);
			dataFimC = DateUtils.truncate(dataFimC, Calendar.DAY_OF_MONTH);
			dataFimC.add(Calendar.MILLISECOND, -1);

			dataFim = dataFimC.getTime();

			sql.append(" and ( 1 != 1 ");

			sql.append(" or d.dataDigitalizacao between :dataInicio and :dataFim ");
			params.put("dataInicio", dataInicio);
			params.put("dataFim", dataFim);

			sql.append(" ) ");
		}

		if(modeloDocumentoList != null && !modeloDocumentoList.isEmpty()) {
			sql.append(" and d.modeloDocumento.id in ( '-1' ");
			for (int i = 0; i < modeloDocumentoList.size(); i++) {
				ModeloDocumento modeloDocumento = modeloDocumentoList.get(i);
				Long modeloDocumentoId = modeloDocumento.getId();
				sql.append(", :modeloDocumento" + i);
				params.put("modeloDocumento" + i, modeloDocumentoId);
			}
			sql.append(")");
		}

		if(tipoProcessoList != null && !tipoProcessoList.isEmpty()) {
			sql.append(" and d.processo.id in ( '-1' ");
			for (int i = 0; i < tipoProcessoList.size(); i++) {
				TipoProcesso tipoProcesso = tipoProcessoList.get(i);
				Long tipoProcessoId = tipoProcesso.getId();
				sql.append(", :tipoProcesso" + i);
				params.put("tipoProcesso" + i, tipoProcessoId);
			}
			sql.append(")");
		}

		if(tipificado != null) {
			sql.append(" and im.tipificado is ").append(tipificado);
		}

		if(origem != null) {
			sql.append(" and d.origem = :origem ");
			params.put("origem", origem);
		}

		if(documentoId != null) {
			sql.append(" and d.id = :documentoId ");
			params.put("documentoId", documentoId);
		}

		return params;
	}

	public int atualizarMetadados(Long imagemMetaId, String metaDados) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<>();

		hql.append(" update ").append(clazz.getName()).append(" im ");
		hql.append(" set im.metaDados = :metaDados ");
		hql.append("where im.id = :imagemMetaId ");

		params.put("metaDados", metaDados);
		params.put("imagemMetaId", imagemMetaId);

		Query query = createQuery(hql, params);
		return query.executeUpdate();
	}
}
