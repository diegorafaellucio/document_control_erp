package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.LoginLog;
import net.wasys.getdoc.domain.entity.Role;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.domain.vo.RelatorioLicenciamentoVO;
import net.wasys.getdoc.domain.vo.filtro.LoginLogFiltro;
import net.wasys.getdoc.domain.vo.filtro.RelatorioLicenciamentoFiltro;
import net.wasys.getdoc.mb.utils.DateUtils;
import net.wasys.util.ddd.HibernateRepository;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class LoginLogRepository extends HibernateRepository<LoginLog> {

	public LoginLogRepository() {
		super(LoginLog.class);
	}

	public int getAtivos(Date inicio, Date fim) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<String, Object>();

		inicio = DateUtils.getFirstTimeOfDay(inicio);
		fim = DateUtils.getLastTimeOfDay(fim);

		hql.append(" select count(distinct ll.usuario.id) ");
		hql.append(" from ").append(clazz.getName()).append(" ll ");
		hql.append(" where ll.dataAcesso between :inicio and :fim ");
		params.put("inicio", inicio);
		params.put("fim", fim);

		Query query = createQuery(hql, params);
		return ((Number) query.uniqueResult()).intValue();
	}

	public List<RelatorioLicenciamentoVO> findRelatorioLicenciamento(RelatorioLicenciamentoFiltro filtro) {
		List<Object> params = new ArrayList<>();
		StringBuilder hql = new StringBuilder();

		List<Date> meses = filtro.getMeses();

		hql.append(" select r.nome ");
		for (Date mes : meses) {
			hql.append(", ");
			hql.append(" count(distinct case when month(ll.dataAcesso) = ? and year(ll.dataAcesso) = ? then ll.usuario.id end) ");
			params.add(DateUtils.getMonth(mes));
			params.add(DateUtils.getYear(mes));
		}
		hql.append(" from ").append(clazz.getName()).append(" ll, ");
		hql.append(Role.class.getName()).append(" r ");
		hql.append(" where ll.usuario.id = r.usuario.id ");

		//evita trazer roles da geral estacio
		hql.append(" and r.nome in ( '-1' ");
		for (RoleGD role : RoleGD.values()) {
			hql.append(", ?");
			params.add(role.name());
		}
		hql.append(" ) ");
		hql.append(" group by r.nome ");
		hql.append(" order by r.nome ");

		Query query = createQuery(hql,params);

		List<Object[]> objs = query.list();
		List<RelatorioLicenciamentoVO> vos = new ArrayList<>();

		for(Object[] obj : objs){
			String nome = (String)obj[0];

			List<Long> qtdPorMes = new ArrayList<>();
			int i = 1;
			while( i < obj.length){
				long qtd = (Long) obj[i];
				qtdPorMes.add(qtd);
				i++;
			}

			RelatorioLicenciamentoVO vo = new RelatorioLicenciamentoVO();
			vo.setNome(nome);
			vo.setQtdPorMes(qtdPorMes);
			vos.add(vo);
		}

		return vos;
	}

	public int countByFiltro(LoginLogFiltro filtro) {
		StringBuilder hql = new StringBuilder();

		hql.append(" select count(*) from ").append(clazz.getName()).append(" ll ");

		Map<String, Object> params = makeQuery(filtro, hql);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue();
	}

	public List<LoginLog> findByFiltro(LoginLogFiltro filtro, Integer first, Integer pageSize) {
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" ll ");

		Map<String, Object> params = makeQuery(filtro, hql);

		hql.append(" order by ll.dataAcesso desc");

		Query query = createQuery(hql.toString(), params);

		if (first != null) {
			query.setFirstResult(first);
		}
		if (pageSize != null) {
			query.setMaxResults(pageSize);
		}

		return query.list();
	}

	public List<Long> findIdsByFiltro(LoginLogFiltro filtro) {
		StringBuilder hql = new StringBuilder();

		hql.append(" select ll.id ").append(getStartQuery()).append(" ll ");

		Map<String, Object> params = makeQuery(filtro, hql);

		Query query = createQuery(hql.toString(), params);

		List<Long> list = query.list();
		return list;
	}

	public List<LoginLog> findByIds(List<Long> ids) {
		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<>();

		hql.append(getStartQuery());
		hql.append(" where id in ( :ids )");
		params.put("ids", ids);
		hql.append(" order by dataAcesso desc ");

		Query query = createQuery(hql.toString(), params);
		query.setFetchSize(500);

		return query.list();
	}

	private Map<String, Object> makeQuery(LoginLogFiltro filtro, StringBuilder hql) {

		Map<String, Object> params = new HashMap<>();

		Date dataInicio = filtro.getDataInicio();
		Date dataFim = filtro.getDataFim();
		List<Long> usuarioIds = filtro.getUsuarioIds();
		RoleGD roleGD = filtro.getRoleGD();
		boolean apenasAtivos = filtro.isApenasAtivos();

		hql.append(" where 1=1 ");

		if(dataInicio != null) {
			hql.append(" and ll.dataAcesso >= :dataInicio ");
			params.put("dataInicio", dataInicio);
		}

		if(dataFim != null) {

			Calendar c = Calendar.getInstance();
			c.setTime(dataFim);
			c.add(Calendar.DAY_OF_MONTH, 1);
			c.add(Calendar.SECOND, -1);
			dataFim = c.getTime();

			hql.append(" and ll.dataAcesso <= :dataFim ");
			params.put("dataFim", dataFim);
		}

		if(!usuarioIds.isEmpty()){
			hql.append(" and ll.usuario.id in ( :usuarioIds ) ");
			params.put("usuarioIds", usuarioIds);
		}

		if(roleGD != null && StringUtils.isNotBlank(roleGD.name())){
			hql.append(" and ll.usuario.id in (select usuario.id from Role rl ");
			hql.append(" where rl.nome = :usuarioRole ) ");
			params.put("usuarioRole", roleGD.name());
		}

		if(apenasAtivos){
			hql.append(" and ll.dataFimAcesso is null");
		}

		return params;
	}

	public LoginLog getLastLoginLogByData(Usuario usuario, Date data){
		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<>();

		hql.append(" select l from ").append(clazz.getName()).append(" l ");
		hql.append("  where l.id = (");
		hql.append(" 	select max(ll.id) ");
		hql.append(" 		from ").append(clazz.getName()).append(" ll ");
		hql.append(" 		where ll.usuario.id = :usuarioId ");
		hql.append(" 		and ll.dataAcesso < :data ");
		hql.append(" 	) ");

		params.put("usuarioId", usuario.getId());
		params.put("data", data);

		Query query = createQuery(hql,params);

		return (LoginLog) query.uniqueResult();
	}
}
