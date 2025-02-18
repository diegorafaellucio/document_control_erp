package net.wasys.getdoc.domain.repository;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.wasys.getdoc.domain.entity.IrregularidadeTipoDocumento;
import net.wasys.getdoc.domain.entity.TipoDocumento;
import net.wasys.getdoc.domain.vo.filtro.IrregularidadeFiltro;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import net.wasys.getdoc.domain.entity.Irregularidade;
import net.wasys.util.ddd.HibernateRepository;


@Repository
@SuppressWarnings("unchecked")
public class IrregularidadeRepository extends HibernateRepository<Irregularidade> {

	public IrregularidadeRepository() {
		super(Irregularidade.class);
	}

	public List<Irregularidade> findAll(Integer inicio, Integer max) {

		Query query = createQuery(" from " + clazz.getName() + " order by nome ");

		query.setFirstResult(inicio);
		query.setMaxResults(max);

		return query.list();
	}

	public int count() {

		Query query = createQuery(" select count(*) from " + clazz.getName());

		return ((Number) query.uniqueResult()).intValue();
	}

	public List<Irregularidade> findAtivas() {

		Query query = createQuery(" from " + clazz.getName() + " where ativa is true order by nome ");

		return query.list();
	}

	public List<Irregularidade> findAtivasRejeitar() {
		StringBuilder hql = new StringBuilder();

		hql.append(" from " + clazz.getName() + " where ativa is true ");
		hql.append(" and irregularidadePastaAmarela is false order by nome ");

		Query query = createQuery(hql.toString());

		return query.list();
	}

	public List<Irregularidade> findAtivasPastaAmarela() {
		StringBuilder hql = new StringBuilder();

		hql.append(" from " + clazz.getName() + " where ativa is true ");
		hql.append(" and irregularidadePastaAmarela is true order by nome ");

		Query query = createQuery(hql.toString());

		return query.list();
	}

	public Irregularidade getIrregularidadeByNome(String irregularidadeNome) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new LinkedHashMap<>();

		hql.append(getStartQuery()).append(" i ");
		hql.append(" where upper(i.nome) = :nome ");
		params.put("nome", irregularidadeNome);

		Query query = createQuery(hql, params);
		return (Irregularidade) query.uniqueResult();
	}
	public List<Irregularidade> findByFiltro(IrregularidadeFiltro filtro, Integer first, Integer pageSize) {

		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" i ");

		Map<String,Object> params = makeQuery(filtro, hql);

		hql.append(" order by i.nome ");

		Query query = createQuery(hql.toString(), params);

		if(first != null) {
			query.setFirstResult(first);
		}

		if(pageSize != null) {
			query.setMaxResults(pageSize);
		}

		return query.list();
	}

	public int countByFiltro(IrregularidadeFiltro filtro) {
		StringBuilder hql = new StringBuilder();

		hql.append(" select count(*) from ").append(clazz.getName()).append(" i ");

		Map<String,Object> params = makeQuery(filtro, hql);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue();
	}

	private Map<String,Object> makeQuery(IrregularidadeFiltro filtro, StringBuilder hql) {

		String nome = filtro.getNome();
		Boolean ativa = filtro.isAtiva();
		Boolean irregularidadePastaAmarela = filtro.isIrregularidadePastaAmarela();
		TipoDocumento tipoDocumentoEscolhido = filtro.getTipoDocumentoEscolhido();

		Map<String,Object> params = new HashMap<>();

		hql.append(" where 1=1 ");

		if(StringUtils.isNotEmpty(nome)){
			hql.append(" and lower(i.nome) like :nome ");
			nome = StringUtils.lowerCase(nome);
			params.put("nome","%" + nome + "%" );
		}
		if(ativa != null) {
			hql.append(" and i.ativa = :ativa ");
			params.put("ativa", ativa);
		}
		if(irregularidadePastaAmarela != null) {
			hql.append(" and i.irregularidadePastaAmarela = :irregularidadePastaAmarela ");
			params.put("irregularidadePastaAmarela", irregularidadePastaAmarela);
		}

		if(tipoDocumentoEscolhido != null) {
			hql.append(" and i.id in ( ");
			hql.append(" select itp.irregularidade.id from " + IrregularidadeTipoDocumento.class.getName() + " itp ");
			hql.append(" where itp.tipoDocumento.id = :tipoDocumentoId) ");
			params.put("tipoDocumentoId", tipoDocumentoEscolhido.getId());
		}

		return params;
	}
}
