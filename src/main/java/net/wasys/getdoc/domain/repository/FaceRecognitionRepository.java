package net.wasys.getdoc.domain.repository;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import net.wasys.getdoc.domain.entity.FaceRecognition;
import net.wasys.getdoc.domain.enumeration.FaceRecognitionApi;
import net.wasys.util.ddd.HibernateRepository;

@Repository
public class FaceRecognitionRepository extends HibernateRepository<FaceRecognition> {

	public FaceRecognitionRepository() {
		super(FaceRecognition.class);
	}

	public FaceRecognition getByImagem(Long imagemId, FaceRecognitionApi api) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<>();

		hql.append(" from ").append(clazz.getName()).append(" f ");

		hql.append(" where f.imagem.id = :imagemId ");
		params.put("imagemId", imagemId);

		hql.append(" and f.api = :api ");
		params.put("api", api);

		Query query = createQuery(hql.toString(), params);

		return (FaceRecognition) query.uniqueResult();
	}

	public void delete(Long imagemId, FaceRecognitionApi api) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<>();

		hql.append(" delete from ").append(clazz.getName()).append(" f ");

		hql.append(" where f.imagem.id = :imagemId ");
		params.put("imagemId", imagemId);

		hql.append(" and f.api = :api ");
		params.put("api", api);

		Query query = createQuery(hql.toString(), params);

		query.executeUpdate();
		
		session.flush();
	}
}
