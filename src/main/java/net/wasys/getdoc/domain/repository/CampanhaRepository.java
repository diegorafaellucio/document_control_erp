package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.Campanha;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.vo.filtro.CampanhaFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

@Repository
@SuppressWarnings("unchecked")
public class CampanhaRepository extends HibernateRepository<Campanha> {

	public CampanhaRepository() {
		super(Campanha.class);
	}

	public List<Campanha> findByTipoProcesso(Long tipoProcessoId, Integer inicio, Integer max) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());

		if(tipoProcessoId != null) {
			hql.append(" where tipoProcesso.id = :tipoProcessoId ");
			params.put("tipoProcessoId", tipoProcessoId);
		}

		hql.append(" order by fimVigencia ");

		Query query = createQuery(hql, params);

		if(inicio != null) {
			query.setFirstResult(inicio);
		}
		if(max != null) {
			query.setMaxResults(max);
		}

		return query.list();
	}

	public Campanha getVigenteByTipoProcesso(Long tipoProcessoId, Date data) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();
		hql.append(getStartQuery());

		if(tipoProcessoId != null) {
			hql.append(" where tipoProcesso.id = :tipoProcessoId ");
			params.put("tipoProcessoId", tipoProcessoId);
		}

		hql.append(" and inicioVigencia <= :dataInicio ");
		hql.append(" and fimVigencia >= :dataFim ");
		params.put("dataInicio", data);
		params.put("dataFim", data);

		hql.append(" order by fimVigencia ");

		Query query = createQuery(hql, params);
		query.setMaxResults(1);
		return (Campanha) query.uniqueResult();
	}

	public Boolean campanhaVigenteByTipoProcesso(Long campanhaId, Long tipoProcessoId, Date inicio, Date fim) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();
		hql.append(getStartQuery());

		Calendar c = Calendar.getInstance();
		c.setTime(fim);
		c.add(Calendar.DAY_OF_MONTH, 1);
		c.add(Calendar.SECOND, -1);
		fim = c.getTime();

		if(tipoProcessoId != null) {
			hql.append(" where tipoProcesso.id = :tipoProcessoId ");
			params.put("tipoProcessoId", tipoProcessoId);
		}

		hql.append(" and id <> :campanhaId");
		params.put("campanhaId", campanhaId);
		hql.append(" and inicioVigencia >= :dataInicio ");
		hql.append(" and fimVigencia <= :dataFim ");
		params.put("dataInicio", inicio);
		params.put("dataFim", fim);

		hql.append(" order by fimVigencia ");

		Query query = createQuery(hql, params);
		query.setMaxResults(1);
		Campanha campanha = (Campanha) query.uniqueResult();
		return campanha != null;
	}

	public int countByTipoProcesso(Long tipoProcessoId) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select count(*) from " + clazz.getName());
		hql.append(" where tipoProcesso.id = :tipoProcessoId ");
		params.put("tipoProcessoId", tipoProcessoId);

		Query query = createQuery(hql, params);

		return ((Number) query.uniqueResult()).intValue();
	}

	public Campanha getByFiltro(CampanhaFiltro filtro) {
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery());

		Map<String, Object> params = makeQuery(filtro, hql);

		hql.append(" order by fimVigencia ");

		Query query = createQuery(hql, params);
		query.setMaxResults(1);

		return (Campanha) query.uniqueResult();
	}

	private Map<String, Object> makeQuery(CampanhaFiltro filtro, StringBuilder hql) {

		Long tipoProcessoId = filtro.getTipoProcessoId();
		String instituicao = filtro.getInstituicao();
		instituicao = DummyUtils.limparCharsChaveUnicidade(instituicao);
		String campus = filtro.getCampus();
		campus = DummyUtils.limparCharsChaveUnicidade(campus);
		String curso = filtro.getCurso();
		curso = DummyUtils.limparCharsChaveUnicidade(curso);
		Date data = filtro.getData();
		String descricao = filtro.getDescricao();
		boolean padrao = filtro.isPadrao();

		Map<String, Object> params = new HashMap<String, Object>();

		hql.append(" where 1=1 ");

		if(tipoProcessoId != null) {
			hql.append(" and tipoProcesso.id = :tipoProcessoId ");
			params.put("tipoProcessoId", tipoProcessoId);
		}

		if(data != null){
			hql.append(" and inicioVigencia <= :dataInicio ");
			hql.append(" and fimVigencia >= :dataFim ");
			params.put("dataInicio", data);
			params.put("dataFim", data);
		}

		if(instituicao != null) {
			hql.append(" and ( ");
			hql.append(" 	upper(instituicoes) like upper(:instituicao) ");
			hql.append(" ) ");
			params.put("instituicao", "%"+instituicao+"%");
		}
		else{
			hql.append(" and instituicoes is null ");
		}

		if(campus != null) {
			hql.append(" and ( ");
			hql.append(" 	upper(campus) like upper(:campus) ");
			hql.append(" ) ");
			params.put("campus", "%"+campus+"%");
		}
		else{
			hql.append(" and campus is null ");
		}

		if(curso != null) {
			hql.append(" and ( ");
			hql.append(" 	upper(cursos) like upper(:curso) ");
			hql.append(" ) ");
			params.put("curso", "%"+curso+"%");
		}
		else{
			hql.append(" and cursos is null ");
		}

		if(descricao != null) {
			hql.append(" and descricao = :descricao ");
			params.put("descricao", descricao);
		}

		if(padrao){
			hql.append(" and padrao = ").append(padrao);
		}

		return params;
	}

	public boolean possuiPadraoByTipoProcessoId(Long tipoProcessoId) {

		Query query = createQuery(" select count(*) from " + clazz.getName() + " where padrao is true and tipoProcesso.id = "+ tipoProcessoId);

		int result = ((Number) query.uniqueResult()).intValue();

		if (result > 0){
			return true;
		}
		return false;
	}

	public List<Campanha> existsConcorrente(Campanha campanha){
		StringBuilder hql = new StringBuilder();
		hql.append(getStartQuery());
		Map<String, Object> params = new HashMap<String, Object>();

		Long campanhaId = campanha.getId();
		TipoProcesso tipoProcesso = campanha.getTipoProcesso();
		Long tipoProcessoId = tipoProcesso.getId();

		String instituicoes = campanha.getInstituicoes();
		String campus = campanha.getCampus();
		String cursos = campanha.getCursos();

		List<String> listInstituicoes = DummyUtils.converterList(instituicoes);
		List<String> listCampus = DummyUtils.converterList(campus);
		List<String> listCursos = DummyUtils.converterList(cursos);

		Date inicioVigencia = campanha.getInicioVigencia();
		Date fimVigencia = campanha.getFimVigencia();

		hql.append(" where 1=1 ");

		if(campanhaId != null) {
			hql.append(" and id <> :id ");
			params.put("id", campanhaId);
		}

		if(tipoProcessoId != null) {
			hql.append(" and tipoProcesso.id = :tipoProcessoId ");
			params.put("tipoProcessoId", tipoProcessoId);
		}

		if((inicioVigencia != null) && (fimVigencia != null)){
			hql.append(" and ( ");
			hql.append("      (inicioVigencia >= :dataInicio and inicioVigencia <= :dataFim ) ");
			hql.append(" or   (fimVigencia    >= :dataInicio and fimVigencia    <= :dataFim ) ");
			hql.append(" or   (inicioVigencia >= :dataInicio and fimVigencia    <= :dataFim ) ");
			hql.append(" or   (fimVigencia    >= :dataInicio and inicioVigencia <= :dataFim ) ");
			hql.append(" ) ");
			params.put("dataInicio", inicioVigencia);
			params.put("dataFim", fimVigencia);
		}
		else{
			hql.append(" and inicioVigencia = null and fimVigencia = null ");
		}

		if((listInstituicoes != null) && (listInstituicoes.size() > 0)) {
			int count = 1;
			hql.append(" and ( ");
			for (String str: listInstituicoes) {
				str = "%" + str + "%";
				hql.append(" 	upper(instituicoes) like upper(:instituicao"+ count + ") ");
				params.put("instituicao" + count, str);
				if (count < listInstituicoes.size()) {
					hql.append(" or ");
				}
				count = count + 1;
			}
			hql.append(" ) ");
		}
		else {
			if((inicioVigencia != null) && (fimVigencia != null)) {
				hql.append(" 	and instituicoes is null ");
			}
		}

		if((listCampus != null) && (listCampus.size() > 0)) {
			int count = 1;
			hql.append(" and ( ");
			for (String str: listCampus) {
				str = "%" + str + "%";
				hql.append(" 	upper(campus) like upper(:campus"+ count + ") ");
				params.put("campus" + count, str);
				if (count < listCampus.size()) {
					hql.append(" or ");
				}
				count = count + 1;
			}
			hql.append(" ) ");
		}
		else {
			if((inicioVigencia != null) && (fimVigencia != null)) {
				hql.append(" 	and campus is null ");
			}
		}

		if((listCursos != null) && (listCursos.size() > 0)) {
			int count = 1;
			hql.append(" and ( ");
			for (String str: listCursos) {
				str = "%" + str + "%";
				hql.append(" 	upper(cursos) like upper(:cursos"+ count + ") ");
				params.put("cursos" + count, str);
				if (count < listCursos.size()) {
					hql.append(" or ");
				}
				count = count + 1;
			}
			hql.append(" ) ");
		}
		else {
			if((inicioVigencia != null) && (fimVigencia != null)) {
				hql.append(" 	and cursos is null ");
			}
		}

		hql.append(" order by fimVigencia ");

		Query query = createQuery(hql, params);

		return query.list();
	}
}
