package net.wasys.getdoc.domain.repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import net.wasys.getdoc.domain.entity.Area;
import net.wasys.getdoc.domain.entity.ProcessoLog;
import net.wasys.getdoc.domain.entity.Solicitacao;
import net.wasys.getdoc.domain.entity.Subarea;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.enumeration.StatusSolicitacao;
import net.wasys.getdoc.domain.vo.SolicitacaoVO;
import net.wasys.util.ddd.HibernateRepository;

@Repository
@SuppressWarnings("unchecked")
public class SolicitacaoRepository extends HibernateRepository<Solicitacao> {

	public SolicitacaoRepository() {
		super(Solicitacao.class);
	}

	public List<Solicitacao> findNaoFinalizadosByProcesso(Long processoId) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(" from ").append(Solicitacao.class.getName()).append(" ");
		hql.append(" where processo.id = ? and dataFinalizacao is null ");
		params.add(processoId);

		Query query = createQuery(hql.toString(), params);

		List<Solicitacao> list = query.list();

		return list;
	}

	public List<SolicitacaoVO> findVosByProcesso(Long processoId) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select ");
		hql.append(" 	pl ");
		hql.append(" from ");
		hql.append(" 	").append(ProcessoLog.class.getName()).append(" pl ");
		hql.append(" 	left outer join fetch pl.solicitacao so ");
		hql.append(" 	left outer join fetch so.subarea sa ");
		hql.append(" 	left outer join fetch sa.area a ");
		hql.append(" 	left outer join fetch pl.anexos ");
		hql.append(" where pl.solicitacao is not null and pl.solicitacao.processo.id = ? ");
		params.add(processoId);

		hql.append(" order by pl.solicitacao.id, pl.id ");

		Query query = createQuery(hql.toString(), params);

		List<ProcessoLog> list = query.list();
		Set<ProcessoLog> set = new LinkedHashSet<>(list);//pra tirar os repetidos
		list = new ArrayList<ProcessoLog>(set);

		Map<Solicitacao, SolicitacaoVO> map = new LinkedHashMap<>();

		for (ProcessoLog log : list) {

			Solicitacao solicitacao = log.getSolicitacao();
			Subarea subarea = solicitacao.getSubarea();
			Hibernate.initialize(subarea);
			Area area = subarea.getArea();
			Hibernate.initialize(area);

			SolicitacaoVO vo = map.get(solicitacao);
			vo = vo != null ? vo : new SolicitacaoVO();
			vo.setSolicitacao(solicitacao);
			map.put(solicitacao, vo);

			if(vo.getLogCriacao() == null) {
				vo.setLogCriacao(log);
			} else {
				vo.addLog(log);
			}
		}

		return new ArrayList<>(map.values());
	}

	public int countAguardandoAcaoByProcesso(Long processoId, RoleGD roleGD) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select ");
		hql.append(" 	count(*) ");
		hql.append(" from ");
		hql.append(" 	").append(ProcessoLog.class.getName()).append(" pl ");

		hql.append(" where pl.solicitacao is not null and pl.solicitacao.processo.id = ? ");
		params.add(processoId);

		if(RoleGD.GD_AREA.equals(roleGD)) {
			hql.append(" and pl.solicitacao.dataResposta is null ");
		}else {
			hql.append(" and pl.solicitacao.dataFinalizacao is null and pl.solicitacao.status in (?, ?) ");
			params.add(StatusSolicitacao.RESPONDIDA);
			params.add(StatusSolicitacao.RECUSADA);
		}

		Query query = createQuery(hql.toString(), params);
		return ((Number) query.uniqueResult()).intValue();
	}

	public List<SolicitacaoVO> findVosPendentesByProcesso(Long processoId, RoleGD roleGD) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select ");
		hql.append(" 	pl ");
		hql.append(" from ");
		hql.append(" 	").append(ProcessoLog.class.getName()).append(" pl ");
		hql.append(" 	left outer join fetch pl.solicitacao so ");
		hql.append(" 	left outer join fetch so.subarea sa ");
		hql.append(" 	left outer join fetch sa.area a ");
		hql.append(" 	left outer join fetch pl.anexos ");

		hql.append(" where pl.solicitacao is not null and pl.solicitacao.processo.id = ? ");
		params.add(processoId);

		if(RoleGD.GD_AREA.equals(roleGD)) {
			hql.append(" and pl.solicitacao.dataResposta is null ");
		} else {
			hql.append(" and pl.solicitacao.dataFinalizacao is null ");
		}

		hql.append(" order by pl.solicitacao.id, pl.id ");

		Query query = createQuery(hql.toString(), params);

		List<ProcessoLog> list = query.list();
		Set<ProcessoLog> set = new LinkedHashSet<>(list);//pra tirar os repetidos
		list = new ArrayList<ProcessoLog>(set);
		Map<Solicitacao, SolicitacaoVO> map = new LinkedHashMap<>();

		for (ProcessoLog log : list) {

			Solicitacao solicitacao = log.getSolicitacao();
			Subarea subarea = solicitacao.getSubarea();
			Hibernate.initialize(subarea);
			Area area = subarea.getArea();
			Hibernate.initialize(area);

			SolicitacaoVO vo = map.get(solicitacao);
			vo = vo != null ? vo : new SolicitacaoVO();
			vo.setSolicitacao(solicitacao);
			map.put(solicitacao, vo);

			if(vo.getLogCriacao() == null) {
				vo.setLogCriacao(log);
			} else {
				vo.addLog(log);
			}
		}

		return new ArrayList<>(map.values());
	}

	public Map<Long, String> getAreasPendentesStr(List<Long> processosIds, Area area, Long solicitacaoExcluidaId) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select ");
		hql.append(" 	s.processo.id, ");
		hql.append(" 	s.subarea.area.descricao ");
		hql.append(" from ");
		hql.append(" 	").append(Solicitacao.class.getName()).append(" s ");

		hql.append(" where s.processo.id in ( -1 ");
		for (Long processoId : processosIds) {
			hql.append(", ?");
			params.add(processoId);
		}
		hql.append(" ) ");
		if (solicitacaoExcluidaId != null) {
			hql.append(" and s.id <> ? ");
			params.add(solicitacaoExcluidaId);
		}

		if(area != null) {
			hql.append(" and s.subarea.area.id = ? ");
			Long areaId = area.getId();
			params.add(areaId);
		}

		hql.append(" and s.dataFinalizacao is null ");

		Query query = createQuery(hql.toString(), params);

		List<Object[]> list = query.list();
		Map<Long, Set<String>> map1 = new HashMap<>();

		for (Object[] obj : list) {

			Long processoId = (Long) obj[0];
			String areaDescricao = (String) obj[1];

			Set<String> set = map1.get(processoId);
			set = set != null ? set : new TreeSet<String>();
			map1.put(processoId, set);

			set.add(areaDescricao);
		}

		Map<Long, String> map2 = new HashMap<>();
		Set<Long> keySet = map1.keySet();
		for (Long processoId : keySet) {

			Set<String> set = map1.get(processoId);
			StringBuilder sb = new StringBuilder();
			for (String areaDescricao : set) {
				sb.append(areaDescricao).append(", ");
			}

			String str = sb.toString();
			str = str.replaceAll(", $", "");

			map2.put(processoId, str);
		}

		return map2;
	}

	public Map<Long, Set<StatusSolicitacao>> getStatusPendentes(List<Long> processosIds, Long areaId) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select ");
		hql.append(" 	s.processo.id, ");
		hql.append(" 	s.status ");
		hql.append(" from ");
		hql.append(" 	").append(Solicitacao.class.getName()).append(" s ");

		hql.append(" where s.processo.id in ( -1 ");
		for (Long processoId : processosIds) {
			hql.append(", ?");
			params.add(processoId);
		}
		hql.append(" ) ");

		if(areaId != null) {
			hql.append(" and s.subarea.area.id = ? ");
			params.add(areaId);
		}

		hql.append(" and s.dataFinalizacao is null ");

		Query query = createQuery(hql.toString(), params);

		List<Object[]> list = query.list();
		Map<Long, Set<StatusSolicitacao>> map1 = new HashMap<>();

		for (Object[] obj : list) {

			Long processoId = (Long) obj[0];
			StatusSolicitacao status = (StatusSolicitacao) obj[1];

			Set<StatusSolicitacao> set = map1.get(processoId);
			set = set != null ? set : new TreeSet<StatusSolicitacao>();
			map1.put(processoId, set);

			set.add(status);
		}

		return map1;
	}

	public boolean existsByAreaProcesso(Long processoId, Long areaId) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select ");
		hql.append(" 	count(*) ");
		hql.append(" from ");
		hql.append(" 	").append(Solicitacao.class.getName()).append(" s ");
		hql.append(" where s.processo.id = ? ");
		params.add(processoId);
		hql.append(" and s.subarea.area.id = ? ");
		params.add(areaId);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).longValue() > 0;
	}

	public List<SolicitacaoVO> findAtrasosArea(Date dataCorte) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select ");
		hql.append(" 	pl ");
		hql.append(" from ");
		hql.append(" 	").append(ProcessoLog.class.getName()).append(" pl ");
		hql.append(" where pl.solicitacao is not null ");
		hql.append(" and pl.solicitacao.dataResposta is null ");
		hql.append(" and pl.solicitacao.dataFinalizacao is null ");
		hql.append(" and pl.solicitacao.prazoLimite < ? ");
		hql.append(" and pl.processo.status <> ? ");
		hql.append(" and pl.processo.status <> ? ");
		hql.append(" and pl.processo.status <> ? ");
		params.add(dataCorte);
		params.add(StatusProcesso.EM_ACOMPANHAMENTO);
		params.add(StatusProcesso.CONCLUIDO);
		params.add(StatusProcesso.CANCELADO);

		hql.append(" order by pl.solicitacao.subarea.area.descricao, pl.solicitacao.subarea.descricao, pl.solicitacao.id, pl.id ");

		Query query = createQuery(hql.toString(), params);

		List<ProcessoLog> list = query.list();
		Map<Solicitacao, SolicitacaoVO> map = new LinkedHashMap<>();

		for (ProcessoLog log : list) {

			Solicitacao solicitacao = log.getSolicitacao();
			Subarea subarea = solicitacao.getSubarea();
			Hibernate.initialize(subarea);
			Area area = subarea.getArea();
			Hibernate.initialize(area);

			SolicitacaoVO vo = map.get(solicitacao);
			vo = vo != null ? vo : new SolicitacaoVO();
			vo.setSolicitacao(solicitacao);
			map.put(solicitacao, vo);

			if(vo.getLogCriacao() == null) {
				vo.setLogCriacao(log);
			} else {
				vo.addLog(log);
			}
		}

		return new ArrayList<>(map.values());
	}

	public Date getProximoPrazo(Long processoId, Long areaId) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select ");
		hql.append(" 	min(s.prazoLimite) ");
		hql.append(" from ");
		hql.append(" 	").append(Solicitacao.class.getName()).append(" s ");
		hql.append(" where s.processo.id = ? ");
		params.add(processoId);
		hql.append(" and s.subarea.area.id = ? ");
		params.add(areaId);
		hql.append(" and s.dataResposta is null ");

		Query query = createQuery(hql.toString(), params);

		return (Date) query.uniqueResult();
	}
}
