package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.CampoMap;
import net.wasys.getdoc.domain.vo.AlunoVO;
import net.wasys.getdoc.domain.vo.RegistroValorVO;
import net.wasys.getdoc.domain.vo.filtro.AlunoFiltro;
import net.wasys.getdoc.domain.vo.filtro.ProcessoFiltro;
import net.wasys.getdoc.domain.vo.filtro.RelatorioPendenciaDocumentoFiltro;
import net.wasys.getdoc.domain.vo.filtro.TaxonomiaFiltro;
import net.wasys.util.ddd.HibernateRepository;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.primefaces.model.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@SuppressWarnings("unchecked")
public class AlunoRepository extends HibernateRepository<Aluno> {

	@Autowired
	private CampoRepository campoRepository;

	public AlunoRepository() {
		super(Aluno.class);
	}

	public int countByFiltro(AlunoFiltro filtro) {

		StringBuilder hql = new StringBuilder();

		hql.append(" select count(*) ");
		hql.append(" from ").append(clazz.getName()).append(" a ");
		hql.append(" where 1=1 ");

		Map<String, Object> params = makeQuery(filtro, hql);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue();
	}

	public List<Aluno> findByFiltro(AlunoFiltro filtro, Integer inicio, Integer max) {

		StringBuilder hql = new StringBuilder();

		hql.append(" select a from ");
		hql.append(Aluno.class.getName()).append(" a ");
		hql.append(" left outer join ").append(Processo.class.getName()).append(" p on p.aluno.id = a.id ");
		hql.append(" where 1=1 ");

		Map<String, Object> params = makeQuery(filtro, hql);

		makeOrderBy(filtro, hql);

		Query query = createQuery(hql.toString(), params);

		if(inicio != null) {
			query.setFirstResult(inicio);
		}

		if(max != null) {
			query.setMaxResults(max);
		}

		List<Aluno> list = query.list();
		Set set = new LinkedHashSet(list);
		return new ArrayList<>(set);
	}

	private void makeOrderBy(AlunoFiltro filtro, StringBuilder hql) {

		String campoOrdem = filtro.getCampoOrdem();
		if(StringUtils.isNotBlank(campoOrdem)) {

			campoOrdem = campoOrdem.replace("aluno.", "a.");

			SortOrder ordem = filtro.getOrdem();
			String ordemStr = SortOrder.DESCENDING.equals(ordem) ? " desc " : " asc ";

			hql.append(" order by ").append(campoOrdem).append(ordemStr);
		}
	}

	private Map<String, Object> makeQuery(AlunoFiltro filtro, StringBuilder hql) {

		String cpf = filtro.getCpf();
		String nome = filtro.getNome();
		boolean estrangeiro = filtro.getEstrangeiro();

		Map<String, Object> params = new HashMap<>();

		if(StringUtils.isNotBlank(cpf)) {
			hql.append(" and a.cpf = :cpf");
			params.put("cpf", cpf);
		}

		if(StringUtils.isNotBlank(nome)) {
			hql.append(" and upper(a.nome) like :nome");
			params.put("nome", "%"+ nome.toUpperCase() +"%");
		}

		if(estrangeiro) {
			hql.append(" and a.cpf = null ");
		}

		return params;
	}

	public List<Aluno> findAlunoPorNomeComProcessoByFiltro(TaxonomiaFiltro filtro) {

		String nome = filtro.getNome();
		nome = nome.toUpperCase()+"%";
		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();
		hql.append("select  a ");
		hql.append(getStartQuery()).append(" a ");
		hql.append(" 	where a.nome like :valor");
		hql.append(" and exists ( select 1 from ");
		hql.append(Processo.class.getName()).append(" p ");
		hql.append(" where p.aluno.id = a.id ) ");
		hql.append(" order by a.nome");

		params.put("valor", nome);

		Query query = createQuery(hql.toString(), params);
		return query.list();
	}

	public Aluno getByCpfNome(String cpf, String nome) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();
		hql.append(getStartQuery()).append(" a ");

		hql.append(" where 1=1 ");

		if(StringUtils.isNotBlank(cpf)) {
			hql.append(" and a.cpf = :cpf ");
			params.put("cpf", cpf);
		}

		if(StringUtils.isNotBlank(nome)) {
			hql.append(" and upper(a.nome) = upper( :nome ) ");
			params.put("nome", nome);
		}

		hql.append(" order by id ");

		Query query = createQuery(hql.toString(), params);
		query.setMaxResults(1);
		return (Aluno) query.uniqueResult();
	}

	public Aluno getByCpfNomeMae(String cpf, String nome, String nomeMae) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();
		hql.append(getStartQuery()).append(" a ");

		hql.append(" where 1=1 ");

		if(StringUtils.isNotBlank(cpf)) {
			hql.append(" and a.cpf = :cpf ");
			params.put("cpf", cpf);
		}

		if(StringUtils.isNotBlank(nome)) {
			hql.append(" and a.nome = :nome ");
			params.put("nome", nome);
		}

		if(StringUtils.isNotBlank(nomeMae)) {
			hql.append(" and a.mae = :nomeMae ");
			params.put("nomeMae", nomeMae);
		}

		hql.append(" order by id ");

		Query query = createQuery(hql.toString(), params);
		query.setMaxResults(1);
		return (Aluno) query.uniqueResult();
	}
}
