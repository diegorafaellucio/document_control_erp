package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.TextoPadrao;
import net.wasys.getdoc.domain.entity.TextoPadraoTipoProcesso;
import net.wasys.getdoc.domain.vo.filtro.TextoPadraoFiltro;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@SuppressWarnings("unchecked")
public class TextoPadraoRepository extends HibernateRepository<TextoPadrao> {

	public TextoPadraoRepository() {
		super(TextoPadrao.class);
	}

	private Map<String, Object> makeQuery(TextoPadraoFiltro filtro, StringBuilder hql) {

		Boolean ativo = filtro.getAtivo();
		Long tipoProcessoId = filtro.getTipoProcessoId();
		List<Long> textoPadraoIds = filtro.getTextoPadraoIds();

		Map<String, Object> params = new HashMap<>();

		hql.append(" where 1=1 ");

		if(ativo != null) {
			hql.append(" and tp.ativo is :ativo ");
			params.put("ativo", ativo);
		}

		if(textoPadraoIds != null) {
			hql.append(" and tp.id in ( :textoPadraoIds ) ");
			params.put("textoPadraoIds", textoPadraoIds);
		}

		if(tipoProcessoId != null) {
			hql.append(" and ( ");
			hql.append(" 	select count(*) from ").append(TextoPadraoTipoProcesso.class.getName()).append(" tptp ");
			hql.append(" 	where tptp.textoPadrao.id = tp.id ");
			hql.append(" 	and tptp.tipoProcesso.id = :tipoProcessoId ");
			hql.append(" ) > 0 ");
			params.put("tipoProcessoId", tipoProcessoId);
		}


		return params;
	}

	public List<TextoPadrao> findByFiltro(TextoPadraoFiltro filtro, Integer inicio, Integer max) {

		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());
		hql.append(" tp ");

		Map<String, Object> params = makeQuery(filtro, hql);

		hql.append(" order by tp.nome ");

		Query query = createQuery(hql.toString(), params);

		if(inicio != null) {
			query.setFirstResult(inicio);
		}

		if(max != null) {
			query.setMaxResults(max);
		}

		return query.list();
	}

	public int count(TextoPadraoFiltro filtro) {

		StringBuilder hql = new StringBuilder();

		hql.append(" select count(*) from ").append(clazz.getName()).append(" tp ");

		Map<String, Object> params = makeQuery(filtro, hql);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue();
	}
}
