package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.CampoMap;
import net.wasys.getdoc.domain.enumeration.Origem;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.service.DashboardDiarioService.DashboardDiarioEnum;
import net.wasys.getdoc.domain.vo.DashboardDiarioVO;
import net.wasys.getdoc.domain.vo.DashboardDiarioVO.Pizza;
import net.wasys.getdoc.domain.vo.DashboardDiarioVO.SituacaoPorHora;
import net.wasys.getdoc.domain.vo.filtro.DashboardDiarioFiltro;
import net.wasys.util.DummyUtils;
import net.wasys.util.ddd.HibernateRepository;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class DashboardDiarioRepository extends HibernateRepository<RelatorioGeralSituacao> {

	public DashboardDiarioRepository() {
		super(RelatorioGeralSituacao.class);
	}

	public SituacaoPorHora getSituacaoPorHora (DashboardDiarioFiltro filtro, DashboardDiarioEnum dashboardDiarioEnum, int horaInicio, int horaFim){

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		long diasUteis =  filtro.getDiasUteis();
		double diasUteisDouble = (double) filtro.getDiasUteis();
		Date dataInicioDia = filtro.getDataInicioDia();
		Date dataFimDia = filtro.getDataFimDia();
		Date dataInicioMes = filtro.getDataInicioMes();
		Date dataFimMes = filtro.getDataFimMes();

		String colunaData = "";

		if(DashboardDiarioEnum.INSCRITO.equals(dashboardDiarioEnum)){
			colunaData = "p.dataCriacao";
		}
		else if(DashboardDiarioEnum.EM_CONFERENCIA.equals(dashboardDiarioEnum)){
			colunaData = "p.dataEnvioAnalise";
		}
		else if(DashboardDiarioEnum.APROVADO.equals(dashboardDiarioEnum)){
			colunaData = "p.dataFinalizacao";
		}

		hql.append(" select ");
		hql.append(" count(case when ").append(colunaData).append(" between :dataInicioDia and :dataFimDia then p.id end), ");
		hql.append(" count(case when ").append(colunaData).append(" between :dataInicioMes and :dataFimMes then p.id end), ");

		//no Dia
		for( int i=horaInicio; i<=horaFim; i++){
			hql.append(" count(case when HOUR( ").append(colunaData).append(" ) = " + i + " and ").append(colunaData).append(" between :dataInicioDia and :dataFimDia then p.id end), ");
		}

		//No Mes
		for( int i=horaInicio; i<=horaFim; i++){
			hql.append(" count(case when HOUR( ").append(colunaData).append(" ) = " + i + " and ").append(colunaData).append(" between :dataInicioMes and :dataFimMes then p.id end), ");
		}
		hql.append(" 'fim' ");

		hql.append(" from " + Processo.class.getName()).append(" p ");
		hql.append(" where 1=1 ");
		makeWhere(filtro, hql, params);

		params.put("dataInicioDia", dataInicioDia);
		params.put("dataFimDia", dataFimDia);
		params.put("dataInicioMes", dataInicioMes);
		params.put("dataFimMes", dataFimMes);

		Query query = createQuery(hql, params);
		Object[] result = (Object[]) query.uniqueResult();

		//TODO deixar esse fill mais "limpo"
		long totalDia = (Long) result[0];
		long totalMediaMensal = (Long) result[1] / diasUteis;

		int horasBuscadas = horaFim - horaInicio + 1;
		Object[] qtdPorHoraObj = Arrays.copyOfRange(result, 2, horasBuscadas + 2);
		ArrayList<Long> qtdPorHora = new ArrayList(Arrays.asList(qtdPorHoraObj));


		Object[] qtdPorHoraMediaMensalObj = Arrays.copyOfRange(result, horasBuscadas + 2, horasBuscadas*2 + 2);
		ArrayList<Double> qtdPorHoraMediaMensal = new ArrayList();

		for(Object obj : qtdPorHoraMediaMensalObj){
			long qtdHora = (long) obj;
			double qtdHoraMediaMensal = qtdHora / diasUteisDouble;
			qtdPorHoraMediaMensal.add(qtdHoraMediaMensal);
		}

		SituacaoPorHora situacaoPorHora = new DashboardDiarioVO.SituacaoPorHora();
		situacaoPorHora.setQtdPorHora(qtdPorHora);
		situacaoPorHora.setQtdPorHoraMediaMensal(qtdPorHoraMediaMensal);
		situacaoPorHora.setTotalDia(totalDia);
		situacaoPorHora.setTotalMediaMensal(totalMediaMensal);

		return situacaoPorHora;
	}

	public SituacaoPorHora getPendentePorHora (DashboardDiarioFiltro filtro, int horaInicio, int horaFim){

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		long diasUteis =  filtro.getDiasUteis();
		double diasUteisDouble = (double) filtro.getDiasUteis();
		Date dataInicioDia = filtro.getDataInicioDia();
		Date dataFimDia = filtro.getDataFimDia();
		Date dataInicioMes = filtro.getDataInicioMes();
		Date dataFimMes = filtro.getDataFimMes();

		String colunaData = "p.dataCriacao";

		hql.append(" select ");
		hql.append(" count(case when ").append(colunaData).append(" between :dataInicioDia and :dataFimDia then p.id end), ");
		hql.append(" count(case when ").append(colunaData).append(" between :dataInicioMes and :dataFimMes then p.id end), ");

		//no Dia
		for( int i=horaInicio; i<=horaFim; i++){
			hql.append(" count(case when HOUR( ").append(colunaData).append(" ) = " + i + " and ").append(colunaData).append(" between :dataInicioDia and :dataFimDia then p.id end), ");
		}

		//No Mes
		for( int i=horaInicio; i<=horaFim; i++){
			hql.append(" count(case when HOUR( ").append(colunaData).append(" ) = " + i + " and ").append(colunaData).append(" between :dataInicioMes and :dataFimMes then p.id end), ");
		}
		hql.append(" 'fim' ");

		hql.append(" from " + ProcessoPendencia.class.getName()).append(" p ");
		hql.append(" where 1=1 ");

		List<Long> tiposProcessoId = filtro.getTiposProcessoId();
		Origem origemProcesso = filtro.getOrigemProcesso();
		String regional = filtro.getRegional();
		String campus = filtro.getCampus();
		String curso = filtro.getCurso();

		if(tiposProcessoId != null && !tiposProcessoId.isEmpty()){
			hql.append(" and p.processo.tipoProcesso.id in (:tiposProcessoId) ");
			params.put("tiposProcessoId", tiposProcessoId);
		}

		if(origemProcesso != null){
			hql.append(" and p.processo.origem = :origemProcesso ");
			params.put("origemProcesso", origemProcesso);
		}

		if(StringUtils.isNotBlank(curso) || StringUtils.isNotBlank(campus) || StringUtils.isNotBlank(regional)) {
			hql.append(" and p.processo.id in ");
			hql.append(" ( ");
			hql.append(" 	select c.grupo.processo.id ");
			hql.append(" 	from ").append(Campo.class.getName()).append(" c ");
			hql.append("	where 1=1 ");

			if (StringUtils.isNotBlank(curso)) {
				hql.append(" and c.nome = :campo ");
				hql.append(" and c.valor = :valor ");
				params.put("campo", CampoMap.CampoEnum.CURSO.getNome());
				params.put("valor", curso);
			} else if (StringUtils.isNotBlank(campus)) {
				hql.append(" and c.nome = :campo ");
				hql.append(" and c.valor = :valor ");
				params.put("campo", CampoMap.CampoEnum.CAMPUS.getNome());
				params.put("valor", campus);
			} else if (StringUtils.isNotBlank(regional)) {
				hql.append(" and c.nome = :campo ");
				hql.append(" and c.valor = :valor ");
				params.put("campo", CampoMap.CampoEnum.REGIONAL.getNome());
				params.put("valor", regional);
			}
			hql.append(" ) ");
		}

		params.put("dataInicioDia", dataInicioDia);
		params.put("dataFimDia", dataFimDia);
		params.put("dataInicioMes", dataInicioMes);
		params.put("dataFimMes", dataFimMes);

		Query query = createQuery(hql, params);
		Object[] result = (Object[]) query.uniqueResult();

		long totalDia = (Long) result[0];
		long totalMediaMensal = (Long) result[1] / diasUteis;

		int horasBuscadas = horaFim - horaInicio + 1;
		Object[] qtdPorHoraObj = Arrays.copyOfRange(result, 2, horasBuscadas + 2);
		ArrayList<Long> qtdPorHora = new ArrayList(Arrays.asList(qtdPorHoraObj));


		Object[] qtdPorHoraMediaMensalObj = Arrays.copyOfRange(result, horasBuscadas + 2, horasBuscadas*2 + 2);
		ArrayList<Double> qtdPorHoraMediaMensal = new ArrayList();

		for(Object obj : qtdPorHoraMediaMensalObj){
			long qtdHora = (long) obj;
			double qtdHoraMediaMensal = qtdHora / diasUteisDouble;
			qtdPorHoraMediaMensal.add(qtdHoraMediaMensal);
		}

		SituacaoPorHora situacaoPorHora = new DashboardDiarioVO.SituacaoPorHora();
		situacaoPorHora.setQtdPorHora(qtdPorHora);
		situacaoPorHora.setQtdPorHoraMediaMensal(qtdPorHoraMediaMensal);
		situacaoPorHora.setTotalDia(totalDia);
		situacaoPorHora.setTotalMediaMensal(totalMediaMensal);

		return situacaoPorHora;
	}

	public List<Double> findTempoMedioBySituacao (DashboardDiarioFiltro filtro, StatusProcesso statusProcesso){

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		Date dataInicioDia = filtro.getDataInicioDia();
		Date dataFimDia = filtro.getDataFimDia();
		Date dataInicioMes = filtro.getDataInicioMes();
		Date dataFimMes = filtro.getDataFimMes();

		hql.append(" select ");
		hql.append(" avg(case when HOUR(rgs.dataFim) = 7 and rgs.dataFim between :dataInicioDia and :dataFimDia then rgs.tempo end), ");
		hql.append(" avg(case when HOUR(rgs.dataFim) = 8 and rgs.dataFim between :dataInicioDia and :dataFimDia then rgs.tempo end), ");
		hql.append(" avg(case when HOUR(rgs.dataFim) = 9 and rgs.dataFim between :dataInicioDia and :dataFimDia then rgs.tempo end), ");
		hql.append(" avg(case when HOUR(rgs.dataFim) = 10 and rgs.dataFim between :dataInicioDia and :dataFimDia then rgs.tempo end), ");
		hql.append(" avg(case when HOUR(rgs.dataFim) = 11 and rgs.dataFim between :dataInicioDia and :dataFimDia then rgs.tempo end), ");
		hql.append(" avg(case when HOUR(rgs.dataFim) = 12 and rgs.dataFim between :dataInicioDia and :dataFimDia then rgs.tempo end), ");
		hql.append(" avg(case when HOUR(rgs.dataFim) = 13 and rgs.dataFim between :dataInicioDia and :dataFimDia then rgs.tempo end), ");
		hql.append(" avg(case when HOUR(rgs.dataFim) = 14 and rgs.dataFim between :dataInicioDia and :dataFimDia then rgs.tempo end), ");
		hql.append(" avg(case when HOUR(rgs.dataFim) = 15 and rgs.dataFim between :dataInicioDia and :dataFimDia then rgs.tempo end), ");
		hql.append(" avg(case when HOUR(rgs.dataFim) = 16 and rgs.dataFim between :dataInicioDia and :dataFimDia then rgs.tempo end), ");
		hql.append(" avg(case when HOUR(rgs.dataFim) = 17 and rgs.dataFim between :dataInicioDia and :dataFimDia then rgs.tempo end), ");
		hql.append(" avg(case when HOUR(rgs.dataFim) = 18 and rgs.dataFim between :dataInicioDia and :dataFimDia then rgs.tempo end), ");
		hql.append(" ( ");
		hql.append("	select avg(rgs1.tempo) ");
		hql.append("	from " + RelatorioGeralSituacao.class.getName()).append(" rgs1, ");
		hql.append(		Processo.class.getName()).append(" p ");
		hql.append("	where rgs1.processoId = p.id ");
		hql.append("	and HOUR(rgs1.dataFim) >= 7 ");
		hql.append("	and HOUR(rgs1.dataFim) < 18 ");
		hql.append("	and rgs1.dataFim >= :dataInicioDia ");
		hql.append("	and rgs1.dataFim <= :dataFimDia ");
		hql.append("	and rgs1.situacao.status = :statusProcesso ");
		makeWhere(filtro, hql, params);
		hql.append(" ) as dia, ");
		hql.append(" ( ");
		hql.append("	select avg(rgs2.tempo) ");
		hql.append("	from " + RelatorioGeralSituacao.class.getName()).append(" rgs2, ");
		hql.append(		Processo.class.getName()).append(" p ");
		hql.append("	where rgs2.processoId = p.id ");
		hql.append("	and HOUR(rgs2.dataFim) >= 7 ");
		hql.append("	and HOUR(rgs2.dataFim) < 18 ");
		hql.append("	and rgs2.dataFim >= :dataInicioMes ");
		hql.append("	and rgs2.dataFim <= :dataFimMes ");
		hql.append("	and rgs2.situacao.status = :statusProcesso ");
		makeWhere(filtro, hql, params);
		hql.append(" ) as mes ");
		hql.append(" from " + RelatorioGeralSituacao.class.getName()).append(" rgs, ");
		hql.append(	 Processo.class.getName()).append(" p ");
		hql.append(" where rgs.processoId = p.id ");
		hql.append(" and rgs.situacao.status = :statusProcesso ");
		hql.append(" and rgs.tempo is not null ");
		makeWhere(filtro, hql, params);

		params.put("statusProcesso", statusProcesso);
		params.put("dataInicioDia", dataInicioDia);
		params.put("dataFimDia", dataFimDia);
		params.put("dataInicioMes", dataInicioMes);
		params.put("dataFimMes", dataFimMes);

		Query query = createQuery(hql, params);
		Object[] objs = (Object[]) query.uniqueResult();
		List<Double> result = new ArrayList<>();

		for(Object obj : objs){
			if(obj != null){
				result.add((Double) obj);
			} else{
				result.add(0.0);
			}
		}
		return result;
	}

	public List<Pizza> findPizzaTiposProcessoByStatus(DashboardDiarioFiltro filtro, DashboardDiarioEnum dashboardDiarioEnum){
		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		Date dataInicioDia = filtro.getDataInicioDia();
		Date dataFimDia = filtro.getDataFimDia();

		hql.append(" select p.tipoProcesso.nome, count(*) ");
		hql.append(" from " + Processo.class.getName()).append(" p ");
		hql.append(" where 1=1 ");

		if(DashboardDiarioEnum.INSCRITO.equals(dashboardDiarioEnum)){
			hql.append(" and p.dataCriacao >= :dataInicioDia ");
			hql.append(" and p.dataCriacao <= :dataFimDia ");
		}
		else if(DashboardDiarioEnum.EM_CONFERENCIA.equals(dashboardDiarioEnum)){
			hql.append(" and p.dataEnvioAnalise >= :dataInicioDia ");
			hql.append(" and p.dataEnvioAnalise <= :dataFimDia ");
		}
		else if(DashboardDiarioEnum.CONFERIDO.equals(dashboardDiarioEnum)){
			hql.append(" and p.dataFinalizacaoAnalise >= :dataInicioDia ");
			hql.append(" and p.dataFinalizacaoAnalise <= :dataFimDia ");
		}
		makeWhere(filtro, hql, params);

		hql.append(" group by p.tipoProcesso.nome ");
		hql.append(" order by p.tipoProcesso.nome ");

		params.put("dataInicioDia", dataInicioDia);
		params.put("dataFimDia", dataFimDia);
		Query query = createQuery(hql, params);

		List<Object[]> objs = query.list();
		List<Pizza> result = new ArrayList<>();

		for(Object[] obj : objs){
			String nome = (String) obj[0];
			nome = DummyUtils.limparCharsChaveUnicidade(nome);
			nome = DummyUtils.capitalize(nome);
			long qtd = (Long) obj[1];

			Pizza pizza = new Pizza();
			pizza.setNome(nome);
			pizza.setQuantidade(qtd);

			result.add(pizza);
		}

		return result;
	}

	public List<Pizza> findPendenciaPorDocumento (DashboardDiarioFiltro filtro){
		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		Date dataInicio = filtro.getDataInicioDia();
		Date dataFim = filtro.getDataFimDia();

		hql.append(" select d.nome, count(*) ");
		hql.append(" from " + Pendencia.class.getName()).append(" pe, ");
		hql.append(Documento.class.getName()).append(" d, ");
		hql.append(Processo.class.getName()).append(" p, ");
		hql.append(DocumentoLog.class.getName()).append(" dl ");
		hql.append(" where pe.id = dl.pendencia.id ");
		hql.append(" and d.id = dl.documento.id ");
		hql.append(" and p.id = d.processo.id ");
		hql.append(" and dl.data >= :dataInicioDia ");
		hql.append(" and dl.data <= :dataFimDia ");
		makeWhere(filtro, hql, params);
		hql.append(" group by d.nome ");
		hql.append(" order by count(*) desc ");

		params.put("dataInicioDia", dataInicio);
		params.put("dataFimDia", dataFim);

		Query query = createQuery(hql, params);
		query.setMaxResults(7);

		List<Object[]> objs = query.list();
		List<Pizza> result = new ArrayList<>();

		for(Object[] obj : objs){
			String nome = (String) obj[0];
			long qtd = (Long) obj[1];

			Pizza pizza = new Pizza();
			pizza.setNome(nome);
			pizza.setQuantidade(qtd);
			result.add(pizza);
		}

		return result;
	}

	public List<Pizza> findPendenciaPorIrregularidade (DashboardDiarioFiltro filtro){
		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		Date dataInicio = filtro.getDataInicioDia();
		Date dataFim = filtro.getDataFimDia();

		hql.append(" select i.nome, count(*) ");
		hql.append(" from " + Pendencia.class.getName()).append(" pe, ");
		hql.append(Documento.class.getName()).append(" d, ");
		hql.append(Processo.class.getName()).append(" p, ");
		hql.append(DocumentoLog.class.getName()).append(" dl, ");
		hql.append(Irregularidade.class.getName()).append(" i ");
		hql.append(" where pe.id = dl.pendencia.id ");
		hql.append(" and d.id = dl.documento.id ");
		hql.append(" and p.id = d.processo.id ");
		hql.append(" and i.id = pe.irregularidade.id ");
		hql.append(" and dl.data >= :dataInicioDia ");
		hql.append(" and dl.data <= :dataFimDia ");
		makeWhere(filtro, hql, params);
		hql.append(" group by i.nome ");
		hql.append(" order by count(*) desc ");

		params.put("dataInicioDia", dataInicio);
		params.put("dataFimDia", dataFim);

		Query query = createQuery(hql, params);
		query.setMaxResults(7);

		List<Object[]> objs = query.list();
		List<Pizza> result = new ArrayList<>();

		for(Object[] obj : objs){
			String nome = (String) obj[0];
			long qtd = (Long) obj[1];

			Pizza pizza = new Pizza();
			pizza.setNome(nome);
			pizza.setQuantidade(qtd);
			result.add(pizza);
		}

		return result;
	}

	private Map<String, Object> makeWhere(DashboardDiarioFiltro filtro, StringBuilder hql, Map<String, Object> params) {

		List<Long> tiposProcessoId = filtro.getTiposProcessoId();
		Origem origemProcesso = filtro.getOrigemProcesso();
		String regional = filtro.getRegional();
		String campus = filtro.getCampus();
		String curso = filtro.getCurso();

		if(tiposProcessoId != null && !tiposProcessoId.isEmpty()){
			hql.append(" and p.tipoProcesso.id in (:tiposProcessoId) ");
			params.put("tiposProcessoId", tiposProcessoId);
		}

		if(origemProcesso != null){
			hql.append(" and p.origem = :origemProcesso ");
			params.put("origemProcesso", origemProcesso);
		}

		if(StringUtils.isNotBlank(curso) || StringUtils.isNotBlank(campus) || StringUtils.isNotBlank(regional)) {
			hql.append(" and p.id in ");
			hql.append(" ( ");
			hql.append(" 	select c.grupo.processo.id ");
			hql.append(" 	from ").append(Campo.class.getName()).append(" c ");
			hql.append("	where 1=1 ");

			if (StringUtils.isNotBlank(curso)) {
				hql.append(" and c.nome = :campo ");
				hql.append(" and c.valor = :valor ");
				params.put("campo", CampoMap.CampoEnum.CURSO.getNome());
				params.put("valor", curso);
			} else if (StringUtils.isNotBlank(campus)) {
				hql.append(" and c.nome = :campo ");
				hql.append(" and c.valor = :valor ");
				params.put("campo", CampoMap.CampoEnum.CAMPUS.getNome());
				params.put("valor", campus);
			} else if (StringUtils.isNotBlank(regional)) {
				hql.append(" and c.nome = :campo ");
				hql.append(" and c.valor = :valor ");
				params.put("campo", CampoMap.CampoEnum.REGIONAL.getNome());
				params.put("valor", regional);
			}
			hql.append(" ) ");
		}

		return params;
	}
}
