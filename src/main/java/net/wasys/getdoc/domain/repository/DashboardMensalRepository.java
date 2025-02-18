package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.CampoMap;
import net.wasys.getdoc.domain.enumeration.Origem;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.vo.DashboardMensalVO;
import net.wasys.getdoc.domain.vo.DashboardMensalVO.ListaProcessoVO;
import net.wasys.getdoc.domain.vo.filtro.DashboardMensalFiltro;
import net.wasys.util.ddd.HibernateRepository;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Repository
public class DashboardMensalRepository extends HibernateRepository<RelatorioGeralSituacao> {

	public DashboardMensalRepository() {
		super(RelatorioGeralSituacao.class);
	}

	public ListaProcessoVO getTipoProcessoPorDia (DashboardMensalFiltro filtro, int diaInicio, int diaFim) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		Date dataInicioDia = filtro.getDataInicioDia();
		Date dataFimDia = filtro.getDataFimDia();

		String colunaData = "p.dataEnvioAnalise";

		hql.append(" select ");
		hql.append("(p.tipoProcesso.nome || ',' || count(case when ").append(colunaData).append(" between :dataInicioDia and :dataFimDia then p.id end)) AS tipoProcessoAndReal, ");

		//no Dia
		for (int i = diaInicio; i <= diaFim; i++) {
			hql.append("(p.tipoProcesso.nome || ',' || count(case when DAY( ").append(colunaData).append(" ) = " + i + " and ").append(colunaData).append(" between :dataInicioDia and :dataFimDia then p.id end)) AS tipoProcessoAndCount, ");
		}

		hql.append(" 'fim' ");

		hql.append(" from " + Processo.class.getName()).append(" p ");
		hql.append(" where 1=1 ");
		makeWhere(filtro, hql, params);
		hql.append(" group by p.tipoProcesso.nome");


		params.put("dataInicioDia", dataInicioDia);
		params.put("dataFimDia", dataFimDia);

		Query query = createQuery(hql, params);
		List<Object[]> result = (List<Object[]>) query.list();

		return getTipoProcessoPorDia(result);
	}

	public ListaProcessoVO getTipoProcessoPorMes (DashboardMensalFiltro filtro) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		Calendar cal = Calendar.getInstance();
		cal.setTime(filtro.getDataInicioDia());
		cal.add(Calendar.MONTH, -3);
		Date dataInicioDia = cal.getTime();
		cal = Calendar.getInstance();
		cal.setTime(filtro.getDataFimDia());
		cal.add(Calendar.MONTH, +2);
		Date dataFimDia = cal.getTime();

		String colunaData = "p.dataEnvioAnalise";

		hql.append(" select ");
		//hql.append("(p.tipoProcesso.nome || ',' || count(case when ").append(colunaData).append(" between :dataInicioDia and :dataFimDia then p.id end)) AS tipoProcessoAndReal, ");

		Integer mes = Integer.parseInt(filtro.getMes()) - 2;

		for (int i = 0; i < 4; i++) {
			if(mes > 12){
				mes = (mes - 12);
			}
			else if(mes <= 0){
				mes = (12 + mes);
			}
			hql.append("(p.tipoProcesso.nome || ',' || count(case when MONTH( ").append(colunaData).append(" ) = " + mes + " and ").append(colunaData).append(" between :dataInicioDia and :dataFimDia then p.id end)) AS tipoProcessoAndCount, ");
			mes++;

		}

		hql.append(" 'fim' ");
		hql.append(" from " + Processo.class.getName()).append(" p ");
		hql.append(" where 1=1 ");
		makeWhere(filtro, hql, params);
		hql.append(" group by p.tipoProcesso.nome");


		params.put("dataInicioDia", dataInicioDia);
		params.put("dataFimDia", dataFimDia);

		Query query = createQuery(hql, params);
		List<Object[]> result = (List<Object[]>) query.list();

		return getTipoProcessoPorDia(result);
	}

	private ListaProcessoVO getTipoProcessoPorDia(List<Object[]> result) {

		long totalPeriodo = 0L;
		HashMap<String, List<Long>> map = new HashMap<>();

		for (Object[] objects : result) {
			List<Long> dias = new ArrayList<>();
			for (int i = 0; i < objects.length; i++) {
				String string = (String) objects[i];
				if (i == 0) {
					String[] split = string.split(",");
					String tipoProcessoNome = split[0];
					Long total = new Long(split[1]);
					totalPeriodo += total;
					dias.add(total);
					map.put(tipoProcessoNome, dias);
				}
				else if (!string.contains("fim")) {
					String[] split = string.split(",");
					String tipoProcessoNome = split[0];
					dias.add(new Long(split[1]));
					map.put(tipoProcessoNome, dias);
				}
			}
		}

		ListaProcessoVO tipoProcessoPorDia = new ListaProcessoVO();
		tipoProcessoPorDia.setQtdPorDia(map);
		tipoProcessoPorDia.setTotalPeriodo(totalPeriodo);
		return tipoProcessoPorDia;
	}

	public List<Double> findTempoMedioDiarioBySituacao (DashboardMensalFiltro filtro, StatusProcesso statusProcesso){

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		Date dataInicioDia = filtro.getDataInicioDia();
		Date dataFimDia = filtro.getDataFimDia();
		Date dataInicioMes = filtro.getDataInicioMes();
		Date dataFimMes = filtro.getDataFimMes();

		int diaInicio = 1;
		int diaFim = filtro.getDiasMes()-1;
		String colunaData = "rgs.dataFim";

		hql.append(" select ");
		hql.append("avg( case when ").append(colunaData).append(" between :dataInicioDia and :dataFimDia then rgs.tempo end), ");
		for (int i = diaInicio; i <= diaFim; i++) {
			hql.append("avg( case when DAY( ").append(colunaData).append(" ) = " + i + " and ").append(colunaData).append(" between :dataInicioDia and :dataFimDia then rgs.tempo end)");
			if(i < diaFim){
				hql.append(", ");
			}
		}
		hql.append(" from " + RelatorioGeralSituacao.class.getName()).append(" rgs, ");
		hql.append(	 Processo.class.getName()).append(" p ");
		hql.append(" where rgs.processoId = p.id ");
		hql.append(" and rgs.situacao.status = :statusProcesso ");
		hql.append(" and HOUR(rgs.dataFim) >= 7 ");
		hql.append(" and HOUR(rgs.dataFim) < 18 ");
		hql.append(" and rgs.dataFim >= :dataInicioDia ");
		hql.append(" and rgs.dataFim <= :dataFimDia ");
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
				BigDecimal bd = new BigDecimal(Double.parseDouble(obj.toString())).setScale(2, RoundingMode.HALF_EVEN);
				result.add(bd.doubleValue());
			} else{
				result.add(0.00);
			}
		}

		return result;
	}

	public List<Double> findTempoMedioMensalBySituacao (DashboardMensalFiltro filtro, StatusProcesso statusProcesso){

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		Calendar cal = Calendar.getInstance();
		cal.setTime(filtro.getDataInicioDia());
		cal.add(Calendar.MONTH, -3);
		Date dataInicioDia = cal.getTime();
		cal = Calendar.getInstance();
		cal.setTime(filtro.getDataFimDia());

		cal.setTime(filtro.getDataInicioDia());
		cal.add(Calendar.MONTH, +2);
		Date dataFimDia = cal.getTime();

		Integer mesInicio = Integer.parseInt(filtro.getMes()) - 2 ;
		Integer mesFim = Integer.parseInt(filtro.getMes()) + 1 ;
		String colunaData = "rgs.dataFim";

		hql.append(" select ");
		Integer mes = Integer.parseInt(filtro.getMes()) - 2;
		for (int i = 0; i < 4; i++) {
			if(mes > 12){
				mes = (mes - 12);
			}
			else if(mes <= 0){
				mes = (12 + mes);
			}
			hql.append("avg( case when MONTH( ").append(colunaData).append(" ) = " + mes + " and ").append(colunaData).append(" between :dataInicioDia and :dataFimDia then rgs.tempo end)");
			if(i < 3){
				hql.append(", ");
			}
			mes++;
		}
		hql.append(" from " + RelatorioGeralSituacao.class.getName()).append(" rgs, ");
		hql.append(	 Processo.class.getName()).append(" p ");
		hql.append(" where rgs.processoId = p.id ");
		hql.append(" and rgs.situacao.status = :statusProcesso ");
		hql.append(" and HOUR(rgs.dataFim) >= 7 ");
		hql.append(" and HOUR(rgs.dataFim) < 18 ");
		hql.append(" and rgs.dataFim >= :dataInicioDia ");
		hql.append(" and rgs.dataFim <= :dataFimDia ");
		hql.append(" and rgs.tempo is not null ");
		makeWhere(filtro, hql, params);


		params.put("statusProcesso", statusProcesso);
		params.put("dataInicioDia", dataInicioDia);
		params.put("dataFimDia", dataFimDia);

		Query query = createQuery(hql, params);
		Object[] objs = (Object[]) query.uniqueResult();
		List<Double> result = new ArrayList<>();

		for(Object obj : objs){
			if(obj != null){
				BigDecimal bd = new BigDecimal(Double.parseDouble(obj.toString())).setScale(2, RoundingMode.HALF_EVEN);
				result.add(bd.doubleValue());
			} else{
				result.add(0.00);
			}
		}

		return result;
	}

	private Map<String, Object> makeWhere(DashboardMensalFiltro filtro, StringBuilder hql, Map<String, Object> params) {

		List<Long> tiposProcessoId = filtro.getTiposProcessoId();
		Origem origemProcesso = filtro.getOrigemProcesso();
		String regional = filtro.getRegional();
		String campus = filtro.getCampus();
		String curso = filtro.getCurso();
		boolean tratados = filtro.isTratados();

		if(tratados){
			hql.append(" and p.dataFinalizacaoAnalise > p.dataEnvioAnalise");
		}
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

	public List<Long> getProdutividadePorDia(DashboardMensalFiltro filtro) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		Date dataInicioDia = filtro.getDataInicioDia();
		Date dataFimDia = filtro.getDataFimDia();
		int diaInicio = 1;
		int diaFim = filtro.getDiasMes();
		String colunaData = "ml.data";

		hql.append(" select ");
		hql.append("count(distinct case when ").append(colunaData).append(" between :dataInicioDia and :dataFimDia then p.id end), ");
		for (int i = diaInicio; i <= diaFim; i++) {
			hql.append("count(distinct case when DAY( ").append(colunaData).append(" ) = " + i + " and ").append(colunaData).append(" between :dataInicioDia and :dataFimDia then p.id end)");
			if(i < diaFim){
				hql.append(", ");
			}
		}
		hql.append(" from ");
		hql.append(ProcessoLog.class.getName()).append(" ml, ");
		hql.append(Processo.class.getName()).append(" p ");
		hql.append(" where 	1=1 ");
		makeWhere(filtro, hql, params);
		hql.append(" 	and p.id = ml.processo.id ");
		hql.append(" 	and ml.data > :dataInicioDia and ml.data < :dataFimDia ");
		hql.append(" 	and (ml.usuario.id is null or (select count(*) from Role r where r.usuario.id = ml.usuario.id and r.nome in (:ADMIN, :GESTOR, :ANALISTA)) > 0) ");

		params.put("dataInicioDia", dataInicioDia);
		params.put("dataFimDia", dataFimDia);
		params.put("ADMIN", RoleGD.GD_ADMIN.name());
		params.put("GESTOR", RoleGD.GD_GESTOR.name());
		params.put("ANALISTA", RoleGD.GD_ANALISTA.name());

		Query query = createQuery(hql, params);

		Object[] objects = (Object[]) query.uniqueResult();
		List<Long> list = new ArrayList<>();
		for (Object object : objects) {
			list.add(new Long(object.toString()));
		}

		return list;
	}

	public List<Long> getProdutividadePorMes(DashboardMensalFiltro filtro) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		Calendar cal = Calendar.getInstance();
		cal.setTime(filtro.getDataInicioDia());
		cal.add(Calendar.MONTH, -3);
		Date dataInicioDia = cal.getTime();
		cal = Calendar.getInstance();
		cal.setTime(filtro.getDataFimDia());

		cal.setTime(filtro.getDataInicioDia());
		cal.add(Calendar.MONTH, +2);
		Date dataFimDia = cal.getTime();



		String colunaData = "ml.data";

		hql.append(" select ");
		Integer mes = Integer.parseInt(filtro.getMes()) - 2;
		for (int i = 0; i < 4; i++) {
			if(mes > 12){
				mes = (mes - 12);
			}
			else if(mes <= 0){
				mes = (12 + mes);
			}
			hql.append("count(distinct case when MONTH( ").append(colunaData).append(" ) = " + mes + " and ").append(colunaData).append(" between :dataInicioDia and :dataFimDia then p.id end)");
			if(i < 3){
				hql.append(", ");
			}
			mes++;
		}
		hql.append(" from ");
		hql.append(ProcessoLog.class.getName()).append(" ml, ");
		hql.append(Processo.class.getName()).append(" p ");
		hql.append(" where 	1=1 ");
		makeWhere(filtro, hql, params);
		hql.append(" 	and p.id = ml.processo.id ");
		hql.append(" 	and ml.data > :dataInicioDia and ml.data < :dataFimDia ");
		hql.append(" 	and (ml.usuario.id is null or (select count(*) from Role r where r.usuario.id = ml.usuario.id and r.nome in (:ADMIN, :GESTOR, :ANALISTA)) > 0) ");

		params.put("dataInicioDia", dataInicioDia);
		params.put("dataFimDia", dataFimDia);
		params.put("ADMIN", RoleGD.GD_ADMIN.name());
		params.put("GESTOR", RoleGD.GD_GESTOR.name());
		params.put("ANALISTA", RoleGD.GD_ANALISTA.name());

		Query query = createQuery(hql, params);

		Object[] objects = (Object[]) query.uniqueResult();
		List<Long> list = new ArrayList<>();
		for (Object object : objects) {
			list.add(new Long(object.toString()));
		}

		return list;
	}

	public List<DashboardMensalVO.DashProcessoVO> getProcessos(DashboardMensalFiltro filtro) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		Date dataInicioDia = filtro.getDataInicioDia();
		Date dataFimDia = filtro.getDataFimDia();

		hql.append(" select a.id, ");
		hql.append("       p.id                                                                                           as processo_id, ");
		hql.append("       COALESCE(tp.nome, '')                                                                          as motivo_requisicao, ");
		hql.append("       COALESCE(a.nome, '')                                                                           as nome, ");
		hql.append("       COALESCE(a.nomeSocial, '')                                                                     as nome_social, ");
		hql.append("       COALESCE(a.cpf, '')                                                                            as cpf, ");
		hql.append("       COALESCE(a.passaporte, '')                                                                     as passaporte, ");
		hql.append("       COALESCE(a.identidade, '')                                                                     as identidade, ");
		hql.append("       COALESCE(a.orgaoEmissor, '')                                                                   as orgao_emissor, ");
		hql.append("       COALESCE(to_char(a.dataEmissao, 'DD/MM/YYYY HH24:MI'), '')                                     as data_emissao, ");

		hql.append(" COALESCE(( ");
		hql.append(" 	select brv.valor ");
		hql.append("	from ").append(BaseRegistroValor.class.getName()).append(" brv, ");
		hql.append(		 Campo.class.getName() + " c2 ");
		hql.append("	where c2.grupo.processo.id = p.id ");
		hql.append("	and c2.nome = :cursoCampoNome ");
		hql.append("	and c2.valor = brv.baseRegistro.chaveUnicidade ");
		hql.append("	and brv.baseRegistro.baseInterna.id = :cursoBaseInternaId ");
		hql.append("	and brv.nome = brv.baseRegistro.baseInterna.colunaLabel ");
		hql.append(" ),'') as curso, ");

		hql.append(" COALESCE(( ");
		hql.append(" 	select brv.valor ");
		hql.append("	from ").append(BaseRegistroValor.class.getName()).append(" brv, ");
		hql.append(		 Campo.class.getName() + " c2 ");
		hql.append("	where c2.grupo.processo.id = p.id ");
		hql.append("	and c2.nome = :campusCampoNome ");
		hql.append("	and c2.valor = brv.baseRegistro.chaveUnicidade ");
		hql.append("	and brv.baseRegistro.baseInterna.id = :campusBaseInternaId ");
		hql.append("	and brv.nome = brv.baseRegistro.baseInterna.colunaLabel ");
		hql.append(" ),'') as campus, ");

		hql.append("       COALESCE(to_char(ml.data, 'DD/MM/YYYY HH24:MI'), '')                                          as data_ultima_atualizacao, ");
		hql.append("       COALESCE(to_char(p.dataCriacao, 'DD/MM/YYYY HH24:MI'), '')                                    as data_criacao, ");
		hql.append("       COALESCE(to_char(p.dataEnvioAnalise, 'DD/MM/YYYY HH24:MI'), '')                               as data_envio_analise, ");
		hql.append("       COALESCE(to_char(p.dataFinalizacaoAnalise, 'DD/MM/YYYY HH24:MI'), ");
		hql.append("                '')                                                                                  as data_finalizacao_analise, ");
		hql.append("       COALESCE(to_char(p.dataFinalizacao, 'DD/MM/YYYY HH24:MI'), '')                                as data_finalizacao, ");
		hql.append("       COALESCE(to_char(p.prazoLimiteAnalise, 'DD/MM/YYYY HH24:MI'), '')                             as prazo_limite_analise, ");
		hql.append("       COALESCE(rg.tempoEmAnalise, 0)                                                                as tempo_em_analise, ");
		hql.append("       COALESCE(case when (rg.tempoEmAnalise > p.horasPrazoAnalise) then 'Não' else 'Sim' end, '') 	 as sla_atendido, ");
		hql.append("       COALESCE(p.horasPrazoAnalise, 0)                                                              as sla_vigente, ");
		hql.append("       COALESCE(s.nome, '')                                                                          as situacao, ");
		hql.append("       COALESCE(p.status, '')                                                                        as status, ");
		hql.append("       COALESCE(u.nome, '')                                                                          as analista, ");
		hql.append("       COALESCE(u.login, '')                                                                         as analista_login, ");

		hql.append(" COALESCE(( ");
		hql.append(" 	select brv.valor ");
		hql.append("	from ").append(BaseRegistroValor.class.getName()).append(" brv, ");
		hql.append(		 Campo.class.getName() + " c2 ");
		hql.append("	where c2.grupo.processo.id = p.id ");
		hql.append("	and c2.nome = :modalidadeEnsinoCampoNome ");
		hql.append("	and c2.valor = brv.baseRegistro.chaveUnicidade ");
		hql.append("	and brv.baseRegistro.baseInterna.id = :modalidadeEnsinoBaseInternaId ");
		hql.append("	and brv.nome = brv.baseRegistro.baseInterna.colunaLabel ");
		hql.append(" ),'') as modalidade_ensino, ");

		hql.append(" COALESCE(( ");
		hql.append(" 	select brv.valor ");
		hql.append("	from ").append(BaseRegistroValor.class.getName()).append(" brv, ");
		hql.append(		 Campo.class.getName() + " c2 ");
		hql.append("	where c2.grupo.processo.id = p.id ");
		hql.append("	and c2.nome = :regionalCampoNome ");
		hql.append("	and c2.valor = brv.baseRegistro.chaveUnicidade ");
		hql.append("	and brv.baseRegistro.baseInterna.id = :regionalBaseInternaId ");
		hql.append("	and brv.nome = brv.baseRegistro.baseInterna.colunaLabel ");
		hql.append(" ),'') as regional, ");

		hql.append(" COALESCE(( ");
		hql.append(" 	select brv.valor ");
		hql.append("	from ").append(BaseRegistroValor.class.getName()).append(" brv, ");
		hql.append(		 Campo.class.getName() + " c2 ");
		hql.append("	where c2.grupo.processo.id = p.id ");
		hql.append("	and c2.nome = :formaIngressoCampoNome ");
		hql.append("	and c2.valor = brv.baseRegistro.chaveUnicidade ");
		hql.append("	and brv.baseRegistro.baseInterna.id = :formaIngressoBaseInternaId ");
		hql.append("	and brv.nome = brv.baseRegistro.baseInterna.colunaLabel ");
		hql.append(" ),'') as forma_ingresso, ");

		hql.append(" COALESCE(( ");
		hql.append(" 	select brv.valor ");
		hql.append("	from ").append(BaseRegistroValor.class.getName()).append(" brv, ");
		hql.append(		 Campo.class.getName() + " c2 ");
		hql.append("	where c2.grupo.processo.id = p.id ");
		hql.append("	and c2.nome = :instituicaoCampoNome ");
		hql.append("	and c2.valor = brv.baseRegistro.chaveUnicidade ");
		hql.append("	and brv.baseRegistro.baseInterna.id = :instituicaoBaseInternaId ");
		hql.append("	and brv.nome = brv.baseRegistro.baseInterna.colunaLabel ");
		hql.append(" ),'') as instituicao ");

		hql.append("from  ");
		hql.append(	 ProcessoLog.class.getName()).append(" ml, ");
		hql.append(	 Processo.class.getName()).append(" p, ");
		hql.append(	 Situacao.class.getName()).append(" s, ");
		hql.append(	 TipoProcesso.class.getName()).append(" tp, ");
		hql.append(	 Aluno.class.getName()).append(" a, ");
		hql.append(	 Usuario.class.getName()).append(" u, ");
		hql.append(	 RelatorioGeral.class.getName()).append(" rg ");
		hql.append("where p.id = ml.processo.id ");
		hql.append("  and tp.id = p.tipoProcesso.id ");
		hql.append("  and s.id = p.situacao.id ");
		hql.append("  and a.id = p.aluno.id ");
		hql.append("  and u.id = p.analista.id ");
		hql.append("  and rg.processoId = p.id ");
		makeWhere(filtro, hql, params);
		hql.append(" 	and ml.data > :dataInicioDia and ml.data < :dataFimDia ");
		hql.append(" 	and (ml.usuario.id is null or (select count(*) from ");
		hql.append(	 Role.class.getName()).append(" r ");
		hql.append(		"where r.usuario.id = ml.usuario.id and r.nome in (:ADMIN, :GESTOR, :ANALISTA)) > 0) ");
		hql.append(" order by a.nome");

		params.put("dataInicioDia", dataInicioDia);
		params.put("dataFimDia", dataFimDia);
		params.put("ADMIN", RoleGD.GD_ADMIN.name());
		params.put("GESTOR", RoleGD.GD_GESTOR.name());
		params.put("ANALISTA", RoleGD.GD_ANALISTA.name());
		params.put("campusBaseInternaId", BaseInterna.CAMPUS_ID);
		params.put("campusCampoNome", CampoMap.CampoEnum.CAMPUS.getNome());
		params.put("cursoBaseInternaId", BaseInterna.CURSO_ID);
		params.put("cursoCampoNome", CampoMap.CampoEnum.CURSO.getNome());
		params.put("regionalBaseInternaId", BaseInterna.REGIONAL_ID);
		params.put("regionalCampoNome", CampoMap.CampoEnum.REGIONAL.getNome());
		params.put("instituicaoBaseInternaId", BaseInterna.INSTITUICAO_ID);
		params.put("instituicaoCampoNome", CampoMap.CampoEnum.INSTITUICAO.getNome());
		params.put("formaIngressoBaseInternaId", BaseInterna.FORMA_INGRESSO_ID);
		params.put("formaIngressoCampoNome", CampoMap.CampoEnum.FORMA_DE_INGRESSO.getNome());
		params.put("modalidadeEnsinoBaseInternaId", BaseInterna.MODALIDADE_ENSINO_ID);
		params.put("modalidadeEnsinoCampoNome", CampoMap.CampoEnum.MODALIDADE_ENSINO.getNome());
		params.put("tipoCursoBaseInternaId", BaseInterna.TIPO_CURSO_ID);
		params.put("tipoCursoCampoNome", CampoMap.CampoEnum.TIPO_CURSO.getNome());


		Query query = createQuery(hql, params);

		List<Object[]> objects =  (List<Object[]>) query.list();

		List<DashboardMensalVO.DashProcessoVO> processos = new ArrayList<>();
		DashboardMensalVO.DashProcessoVO processo;
		for(Object[] object : objects){
			processo = new DashboardMensalVO.DashProcessoVO();
			processo.setId(Long.parseLong(object[0].toString()));
			processo.setProcessoId(Long.parseLong(object[1].toString()));
			processo.setMotivoRequisicao(object[2].toString());
			processo.setNome(object[3].toString());
			processo.setNomeSocial(object[4].toString());
			processo.setCpf(object[5].toString());
			processo.setPassaporte(object[6].toString());
			processo.setIdentidade(object[7].toString());
			processo.setOrgaoEmissor(object[8].toString());
			processo.setDataEmissao(object[9].toString());
			processo.setCurso(object[10].toString());
			processo.setCampus(object[11].toString());
			processo.setDataCriacao(object[13].toString());
			processo.setDataEnvioAnalise(object[14].toString());
			processo.setDataFinalizacaoAnalise(object[15].toString());
			processo.setDataFinalizacao(object[16].toString());
			processo.setPrazoLimiteAnalise(object[17].toString());
			processo.setTempoEmAnalise(object[18].toString());
			processo.setSlaAtendido(object[19].toString());
			processo.setSlaPrevisto(object[20].toString());
			processo.setSituacao(object[21].toString());
			processo.setStatus(object[22].toString());
			processo.setAnalista(object[23].toString());
			processo.setAnalistaLogin(object[24].toString());
			processo.setModalidadeEnsino(object[25].toString());
			processo.setRegional(object[26].toString());
			processo.setFormaIngresso(object[27].toString());
			processo.setInstituicao(object[28].toString());
			processos.add(processo);
		}

		return processos;
	}
}
