package net.wasys.getdoc.domain.repository;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import net.wasys.getdoc.domain.entity.CampoOcr;
import net.wasys.util.ddd.HibernateRepository;

@Repository
@SuppressWarnings({ "unchecked", "rawtypes" })
public class CampoOcrRepository extends HibernateRepository<CampoOcr> {

	public CampoOcrRepository() {
		super(CampoOcr.class);
	}

	public int countByDocumento(Long documentoId) {

		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<Object>();

		hql.append(" select count(*) from ").append(clazz.getName()).append(" iocr where iocr.imagem.documento.id = ? ");
		params.add(documentoId);

		Query query = createQuery(hql.toString(), params);
		return ((Number)query.uniqueResult()).intValue();
	}

	public List<CampoOcr> findByDocumento(Long documentoId) {

		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<Object>();

		hql.append(" from ").append(clazz.getName()).append(" iocr ");
		hql.append(" where iocr.imagem.documento.id = ? ");
		hql.append(" and iocr.imagem.versao = iocr.imagem.documento.versaoAtual ");
		hql.append(" order by iocr.nome ");

		params.add(documentoId);

		Query query = createQuery(hql.toString(), params);
		List list = query.list();
		return list;
	}

	public int deleteFromImagem(Long imagemId) {

		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<Object>();

		hql.append(" delete from ").append(clazz.getName()).append(" iocr where iocr.imagem.id = ? ");
		params.add(imagemId);

		Query query = createQuery(hql.toString(), params);
		return query.executeUpdate();
	}

	public CampoOcr getByNome(Long processoId, Long documentoId, String documentoNome, String campoNome) {

		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<Object>();

		hql.append(" from ").append(clazz.getName()).append(" iocr ");
		hql.append(" where iocr.imagem.versao = iocr.imagem.documento.versaoAtual ");

		if(processoId != null) {
			hql.append(" and iocr.imagem.documento.processo.id = ?");
			params.add(processoId);
		}

		if(documentoId != null) {
			hql.append(" and iocr.imagem.documento.id = ?");
			params.add(documentoId);
		}

		if(documentoNome != null) {
			hql.append(" and iocr.imagem.documento.nome = ?");
			params.add(documentoNome);
		}

		hql.append(" and iocr.nome = ? ");
		params.add(campoNome);

		Query query = createQuery(hql.toString(), params);
		CampoOcr result = (CampoOcr) query.uniqueResult();
		return result;
	}

	public boolean temOcr(Long documentoId) {

		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<Object>();

		hql.append(" select count(*) from ").append(clazz.getName()).append(" iocr ");

		hql.append(" where iocr.imagem.documento.id = ?");
		params.add(documentoId);

		Query query = createQuery(hql.toString(), params);
		Number resultado = (Number) query.uniqueResult();
		return resultado.intValue() > 0;
	}

	public boolean temNaoChecadoByProcesso(Long processoId) {

		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<Object>();

		hql.append(" select count(*) from ").append(clazz.getName()).append(" iocr ");

		hql.append(" where iocr.imagem.documento.processo.id = ? ");
		params.add(processoId);

		hql.append(" and iocr.usuarioChecagem.id is null ");

		Query query = createQuery(hql.toString(), params);
		Number resultado = (Number) query.uniqueResult();
		return resultado.intValue() > 0;
	}

	public boolean temNaoChecadoByDocumento(Long documentoId) {

		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<Object>();

		hql.append(" select count(*) from ").append(clazz.getName()).append(" iocr ");

		hql.append(" where iocr.imagem.documento.id = ? ");
		params.add(documentoId);

		hql.append(" and iocr.usuarioChecagem.id is null ");

		Query query = createQuery(hql.toString(), params);
		Number resultado = (Number) query.uniqueResult();
		return resultado.intValue() > 0;
	}
}
