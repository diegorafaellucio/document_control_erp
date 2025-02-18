package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.AcaoProcesso;
import net.wasys.getdoc.domain.enumeration.CampoMap;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.vo.CampoDinamicoVO;
import net.wasys.getdoc.domain.vo.RelatorioProdutividadeVO;
import net.wasys.getdoc.domain.vo.filtro.CamposFiltro;
import net.wasys.getdoc.domain.vo.filtro.ProcessoLogFiltro;
import net.wasys.getdoc.domain.vo.filtro.RelatorioAtividadeFiltro;
import net.wasys.getdoc.domain.vo.filtro.RelatorioProdutividadeProuniFiltro;
import net.wasys.util.ddd.HibernateRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.primefaces.model.SortOrder;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.*;

@Repository
@SuppressWarnings("unchecked")
public class ProcessoLogRepository extends HibernateRepository<ProcessoLog> {

	public ProcessoLogRepository() {
		super(ProcessoLog.class);
	}

	public List<ProcessoLog> findByProcesso(Long processoId) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" pl ");
		hql.append(" left outer join fetch pl.usuario ");
		hql.append(" left outer join fetch pl.anexos ");

		hql.append(" where pl.processo.id = ? ");
		params.add(processoId);

		hql.append(" order by pl.id ");

		Query query = createQuery(hql.toString(), params);

		List<ProcessoLog> list = query.list();
		Set<ProcessoLog> set = new LinkedHashSet<>(list);//pra tirar os repetidos
		return new ArrayList<ProcessoLog>(set);
	}

	public ProcessoLog findLastLogByProcesso(Long processoId, Long situacaoId) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" pl ");
		//hql.append(" left outer join fetch pl.anexos ");

		hql.append(" where pl.processo.id = ? ");
		params.add(processoId);

		if(situacaoId != null) {
			hql.append(" and pl.situacao.id = ? ");
			params.add(situacaoId);
		}

		hql.append(" order by pl.id desc ");

		Query query = createQuery(hql.toString(), params);
		query.setMaxResults(1);

		return (ProcessoLog) query.uniqueResult();
	}

	public boolean isImportacao(Long processoId) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select count(*) from ").append(ProcessoLog.class.getName()).append(" pl ");
		hql.append(" where pl.processo.id = ? ");
		params.add(processoId);
		hql.append(" and pl.acao = ? ");
		params.add(AcaoProcesso.IMPORTACAO);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue() > 0;
	}

	public List<ProcessoLog> findStatusByProcesso(Long processoId) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(getStartQuery()).append(" pl ");
		hql.append(" left outer join fetch pl.anexos ");
		hql.append(" left outer join fetch pl.usuario ");
		hql.append(" where pl.statusProcesso is not null ");

		hql.append(" and pl.processo.id = ? ");
		params.add(processoId);

		hql.append(" order by pl.id ");

		Query query = createQuery(hql.toString(), params);

		List<ProcessoLog> list = query.list();
		Set<ProcessoLog> set = new LinkedHashSet<>(list);//pra tirar os repetidos
		return new ArrayList<ProcessoLog>(set);
	}

	public Date getDataPrimeiroEnvioAnalise(Long processoId) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select min(pl.data) from ").append(clazz.getName()).append(" pl ");
		hql.append(" where pl.statusProcesso in ( ?, ? ) ");
		params.add(StatusProcesso.AGUARDANDO_ANALISE);
		params.add(StatusProcesso.EM_ANALISE);
		hql.append(" and pl.processo.id = ? ");
		params.add(processoId);

		Query query = createQuery(hql.toString(), params);

		return (Date) query.uniqueResult();
	}

	public List<RelatorioProdutividadeVO> getRelatorioProdutividadeAnalista(Date dataInicio, Date dataFim, Long tipoProcessoId, List<Situacao> situacaoProcesso, List<CampoDinamicoVO> camposFiltros) {

		StringBuilder hql = new StringBuilder();

		hql.append(" select ");
		hql.append(" 	u.id, ");
		hql.append(" 	u.nome, ");
		hql.append(" 	count(*), ");//atividades
		hql.append(" 	count(distinct m.id), ");//procesos
		hql.append(" 	( ");
		hql.append(" 		select count(*) from Processo m2, net.wasys.getdoc.domain.entity.ProcessoLog ml2 where ");
		hql.append(" 		ml2.processo.id = m2.id and ml2.usuario.id = u.id and m2.dataCriacao >= :dataInicio and m2.dataCriacao < :dataFim and ml2.acao = :CRIACAO ");
		hql.append(" 	), ");//cadastros manuais
		hql.append(" 	( ");
		hql.append(" 		select count(*) from Processo m2, net.wasys.getdoc.domain.entity.ProcessoLog ml2 where ");
		hql.append(" 		ml2.processo.id = m2.id and ml2.usuario.id = u.id and m2.dataCriacao >= :dataInicio and m2.dataCriacao < :dataFim and ml2.acao = :IMPORTACAO ");
		hql.append(" 	), ");//cadastros automaticos
		hql.append(" 	( ");
		hql.append(" 		select count(*) from Processo m2, net.wasys.getdoc.domain.entity.ProcessoLog ml2 where ");
		hql.append(" 		ml2.processo.id = m2.id and ml2.usuario.id = u.id and ml2.data >= :dataInicio and m2.dataCriacao < :dataFim and ml2.acao = :EM_ACOMPANHAMENTO ");
		hql.append(" 	), ");//acompanhamentos
		hql.append(" 	( ");
		hql.append(" 		select count(*) from Processo m2, net.wasys.getdoc.domain.entity.ProcessoLog ml2 where ");
		hql.append(" 		ml2.processo.id = m2.id and ml2.usuario.id = u.id and ml2.data >= :dataInicio and ml2.data < :dataFim and ml2.acao in (:CONCLUSAO, :CANCELAMENTO) ");
		hql.append(" 		and (select count(*) from net.wasys.getdoc.domain.entity.ProcessoLog ml3 where ml3.processo.id = m2.id and ml3.id < ml2.id and ml3.acao = 'EM_ACOMPANHAMENTO') = 0 ");
		hql.append(" 	), ");//finalização analise->fim
		hql.append(" 	( ");
		hql.append(" 		select count(*) from Processo m2, net.wasys.getdoc.domain.entity.ProcessoLog ml2 where ");
		hql.append(" 		ml2.processo.id = m2.id and ml2.usuario.id = u.id and ml2.data >= :dataInicio and ml2.data < :dataFim and ml2.acao in (:CONCLUSAO, :CANCELAMENTO) ");
		hql.append(" 		and (select count(*) from net.wasys.getdoc.domain.entity.ProcessoLog ml3 where ml3.processo.id = m2.id and ml3.id < ml2.id and ml3.acao = :EM_ACOMPANHAMENTO) > 0 ");
		hql.append(" 	) ");//finalização acompanhamento->fim
		hql.append(" from ");
		hql.append(" 	net.wasys.getdoc.domain.entity.ProcessoLog ml, ");
		hql.append(" 	Processo m, ");
		hql.append(" 	Usuario u ");
		hql.append(" where 	1=1 ");
		hql.append(" 	and ml.processo.id = m.id ");
		hql.append(" 	and ml.usuario.id = u.id ");
		hql.append(" 	and ml.data > :dataInicio and ml.data < :dataFim ");

		Map<String, Object> params = criarParametrosRelatorioProdutividade(dataInicio, dataFim, tipoProcessoId, situacaoProcesso);

		if(camposFiltros != null && !camposFiltros.isEmpty()) {
			for (int i=0; i < camposFiltros.size(); i++) {
				CampoDinamicoVO cd = camposFiltros.get(i);
				String nomeGrupo = cd.getNomeGrupo();
				String nomeCampo = cd.getNomeCampo();

				List<String> chavesUnicidade = cd.getChavesUnicidade();
				String valorCampo = cd.getValorCampo();
				if(org.apache.commons.lang3.StringUtils.isBlank(valorCampo) && CollectionUtils.isEmpty(chavesUnicidade)) {
					continue;
				}

				hql.append(" and m.id in ( ");
				hql.append(" 	select cc.grupo.processo.id ");
				hql.append(" 	from ").append(Campo.class.getName()).append(" c ");
				hql.append(" 	where cc.grupo.nome = :nomeGrupo"+i+" ");
				params.put("nomeGrupo"+i,nomeGrupo);
				hql.append(" 	and cc.nome = :nomeCampo"+i+" ");
				params.put("nomeCampo"+i,nomeCampo);
				if(chavesUnicidade != null){
					hql.append(" and ( 1=2 ");
					for(String chaveUnicidade : chavesUnicidade){
						hql.append(" 	or upper(cc.valor) like :chaveUnicidade"+i+" ");
						chaveUnicidade = org.apache.commons.lang3.StringUtils.upperCase(chaveUnicidade);
						params.put("chaveUnicidade"+i,chaveUnicidade);
					}
					hql.append(" )");
				} else{
					hql.append(" and upper(cc.valor) like :valorCampo"+i+" ");
					valorCampo = valorCampo.toUpperCase();
					valorCampo = valorCampo.trim();
					params.put("valorCampo"+i,"%" + valorCampo + "%");
				}
				hql.append(" ) ");
			}
		}

		hql.append(" 	and (select count(*) from Role r where r.usuario.id = ml.usuario.id and r.nome in (:ADMIN, :GESTOR, :ANALISTA)) > 0 ");
		if(tipoProcessoId != null) {
			hql.append(" and m.tipoProcesso.id = :idTipoProcesso ");
		}
		if(situacaoProcesso != null && !situacaoProcesso.isEmpty() ) {
			hql.append(" and m.situacao in (:situacaoProcesso) ");
		}
		hql.append(" group by ");
		hql.append(" 	u.id, ");
		hql.append(" 	u.nome");
		hql.append(" order by ");
		hql.append(" 	u.nome ");

		Query query = createQuery(hql, params);

		List<Object[]> list = query.list();
		List<RelatorioProdutividadeVO> result = fillResult(list);

		return result;
	}

	public List<RelatorioProdutividadeVO> getRelatorioProdutividadeAnalistaCSC(Date dataInicio, Date dataFim, List<CampoDinamicoVO> camposFiltros) {

		StringBuilder hql = new StringBuilder();

		hql.append(" select ");
		hql.append(" 	u.id, ");
		hql.append(" 	u.nome, ");
		hql.append(" 	count(*), ");//atividades
		hql.append(" 	count(distinct m.id), ");//processos
		hql.append(" 	( ");
		hql.append(" 		select count(*) from net.wasys.getdoc.domain.entity.ProcessoLog ml2 where ");
		hql.append(" 		ml2.usuario.id = u.id and ml2.data >= :dataInicio and ml2.data < :dataFim and ml2.acao = (:ALTERACAO_SITUACAO) ");
		hql.append(" 		and ml2.observacao like '%3.1%' and ml2.observacao like '%3.3%' ");
		hql.append(" 	), ");//contagem de situacao 3.1 para 3.3
		hql.append(" 	( ");
		hql.append(" 		select count(*) from net.wasys.getdoc.domain.entity.ProcessoLog ml2 where ");
		hql.append(" 		ml2.usuario.id = u.id and ml2.data >= :dataInicio and ml2.data < :dataFim and ml2.acao = (:ALTERACAO_SITUACAO) ");
		hql.append(" 		and ml2.observacao like '%3.2%' and ml2.observacao like '%3.3%' ");
		hql.append(" 	), ");//contagem de situacao 3.2 para 3.3
		hql.append(" 	( ");
		hql.append(" 		select count(*) from net.wasys.getdoc.domain.entity.ProcessoLog ml2 where ");
		hql.append(" 		ml2.usuario.id = u.id and ml2.data >= :dataInicio and ml2.data < :dataFim and ml2.acao = (:ALTERACAO_SITUACAO) ");
		hql.append(" 		and ml2.observacao like '%3.0%' and ml2.observacao like '%3.3%' ");
		hql.append(" 	) ");//contagem de situacao 3.0 para 3.3
		hql.append(" from ");
		hql.append(" 	net.wasys.getdoc.domain.entity.ProcessoLog ml, ");
		hql.append(" 	Processo m, ");
		hql.append(" 	Usuario u ");
		hql.append(" where 	1=1 ");
		hql.append("	and u.subperfilAtivo.id in (:subperfisIdsCSC)");
		hql.append(" 	and ml.usuario.id = u.id ");
		hql.append(" 	and ml.processo.id = m.id ");
		hql.append(" 	and ml.data > :dataInicio and ml.data < :dataFim ");

		Map<String, Object> params = criarParametrosRelatorioProdutividade(dataInicio, dataFim, null, null);

		if(camposFiltros != null && !camposFiltros.isEmpty()) {
			for (int i=0; i < camposFiltros.size(); i++) {
				CampoDinamicoVO cd = camposFiltros.get(i);
				String nomeGrupo = cd.getNomeGrupo();
				String nomeCampo = cd.getNomeCampo();

				List<String> chavesUnicidade = cd.getChavesUnicidade();
				String valorCampo = cd.getValorCampo();
				if(org.apache.commons.lang3.StringUtils.isBlank(valorCampo) && CollectionUtils.isEmpty(chavesUnicidade)) {
					continue;
				}

				hql.append(" and m.id in ( ");
				hql.append(" 	select cc.grupo.processo.id ");
				hql.append(" 	from ").append(Campo.class.getName()).append(" c ");
				hql.append(" 	where cc.grupo.nome = :nomeGrupo"+i+" ");
				params.put("nomeGrupo"+i,nomeGrupo);
				hql.append(" 	and cc.nome = :nomeCampo"+i+" ");
				params.put("nomeCampo"+i,nomeCampo);
				if(chavesUnicidade != null){
					hql.append(" and ( 1=2 ");
					for(String chaveUnicidade : chavesUnicidade){
						hql.append(" 	or upper(cc.valor) like :chaveUnicidade"+i+" ");
						chaveUnicidade = org.apache.commons.lang3.StringUtils.upperCase(chaveUnicidade);
						params.put("chaveUnicidade"+i,chaveUnicidade);
					}
					hql.append(" )");
				} else{
					hql.append(" and upper(cc.valor) like :valorCampo"+i+" ");
					valorCampo = valorCampo.toUpperCase();
					valorCampo = valorCampo.trim();
					params.put("valorCampo"+i,"%" + valorCampo + "%");
				}
				hql.append(" ) ");
			}
		}

		hql.append(" group by ");
		hql.append(" 	u.id, ");
		hql.append(" 	u.nome");
		hql.append(" order by ");
		hql.append(" 	u.nome ");

		Query query = createQuery(hql, params);

		List<Object[]> list = query.list();
		List<RelatorioProdutividadeVO> result = fillResultCSC(list);

		return result;
	}

	public List<RelatorioProdutividadeVO> getRelatorioProdutividadeAnalistaMedicina(Date dataInicio, Date dataFim, List<CampoDinamicoVO> camposFiltros) {

		StringBuilder hql = new StringBuilder();

		hql.append(" select ");
		hql.append(" 	u.id, ");
		hql.append(" 	u.nome, ");
		hql.append(" 	count(*), ");//atividades
		hql.append(" 	count(distinct m.id), ");//processos
		hql.append(" 	( ");
		hql.append(" 		select count(*) from net.wasys.getdoc.domain.entity.ProcessoLog ml2 where ");
		hql.append(" 		ml2.usuario.id = u.id and ml2.data >= :dataInicio and ml2.data < :dataFim and ml2.acao = (:ALTERACAO_SITUACAO) ");
		hql.append(" 		and ml2.observacao like '%4.0%' and ml2.observacao like '%4.1%' ");
		hql.append(" 	) ");//contagem de situacao 4.0 para 4.1
		hql.append(" from ");
		hql.append(" 	net.wasys.getdoc.domain.entity.ProcessoLog ml, ");
		hql.append(" 	Processo m, ");
		hql.append(" 	Usuario u ");
		hql.append(" where 	1=1 ");
		hql.append("	and u.subperfilAtivo.id in (:subperfisIdsMedicina)");
		hql.append(" 	and ml.usuario.id = u.id ");
		hql.append(" 	and ml.processo.id = m.id ");
		hql.append(" 	and ml.data > :dataInicio and ml.data < :dataFim ");

		Map<String, Object> params = criarParametrosRelatorioProdutividade(dataInicio, dataFim, null, null);

		if(camposFiltros != null && !camposFiltros.isEmpty()) {
			for (int i=0; i < camposFiltros.size(); i++) {
				CampoDinamicoVO cd = camposFiltros.get(i);
				String nomeGrupo = cd.getNomeGrupo();
				String nomeCampo = cd.getNomeCampo();

				List<String> chavesUnicidade = cd.getChavesUnicidade();
				String valorCampo = cd.getValorCampo();
				if(org.apache.commons.lang3.StringUtils.isBlank(valorCampo) && CollectionUtils.isEmpty(chavesUnicidade)) {
					continue;
				}

				hql.append(" and m.id in ( ");
				hql.append(" 	select cc.grupo.processo.id ");
				hql.append(" 	from ").append(Campo.class.getName()).append(" c ");
				hql.append(" 	where cc.grupo.nome = :nomeGrupo"+i+" ");
				params.put("nomeGrupo"+i,nomeGrupo);
				hql.append(" 	and cc.nome = :nomeCampo"+i+" ");
				params.put("nomeCampo"+i,nomeCampo);
				if(chavesUnicidade != null){
					hql.append(" and ( 1=2 ");
					for(String chaveUnicidade : chavesUnicidade){
						hql.append(" 	or upper(cc.valor) like :chaveUnicidade"+i+" ");
						chaveUnicidade = org.apache.commons.lang3.StringUtils.upperCase(chaveUnicidade);
						params.put("chaveUnicidade"+i,chaveUnicidade);
					}
					hql.append(" )");
				} else{
					hql.append(" and upper(cc.valor) like :valorCampo"+i+" ");
					valorCampo = valorCampo.toUpperCase();
					valorCampo = valorCampo.trim();
					params.put("valorCampo"+i,"%" + valorCampo + "%");
				}
				hql.append(" ) ");
			}
		}
		hql.append(" group by ");
		hql.append(" 	u.id, ");
		hql.append(" 	u.nome");
		hql.append(" order by ");
		hql.append(" 	u.nome ");

		Query query = createQuery(hql, params);

		List<Object[]> list = query.list();
		List<RelatorioProdutividadeVO> result = fillResultMedicina(list);

		return result;
	}

	private Map<String, Object> criarParametrosRelatorioProdutividade(Date dataInicio, Date dataFim, Long tipoProcessoId, List<Situacao> situacaoProcesso) {

		Map<String, Object> params = new HashMap<String, Object>();

		params.put("CRIACAO", AcaoProcesso.CRIACAO);
		params.put("IMPORTACAO", AcaoProcesso.IMPORTACAO);
		params.put("EM_ACOMPANHAMENTO", AcaoProcesso.EM_ACOMPANHAMENTO);
		params.put("CONCLUSAO", AcaoProcesso.CONCLUSAO);
		params.put("CANCELAMENTO", AcaoProcesso.CANCELAMENTO);
		params.put("ALTERACAO_SITUACAO", AcaoProcesso.ALTERACAO_SITUACAO);
		params.put("ADMIN", RoleGD.GD_ADMIN.name());
		params.put("GESTOR", RoleGD.GD_GESTOR.name());
		params.put("ANALISTA", RoleGD.GD_ANALISTA.name());
		params.put("subperfisIdsCSC", Subperfil.SUBPERFIS_CSC_IDS);
		params.put("subperfisIdsMedicina", Subperfil.SUBPERFIS_MEDICINA_IDS);
		dataInicio = DateUtils.truncate(dataInicio, Calendar.DAY_OF_MONTH);
		dataFim = DateUtils.addDays(dataFim, 1);
		dataFim = DateUtils.addMilliseconds(dataFim, -1);
		params.put("dataInicio", dataInicio);
		params.put("dataFim", dataFim);
		if(tipoProcessoId != null){
			params.put("idTipoProcesso", tipoProcessoId);
		}
		if(situacaoProcesso != null && !situacaoProcesso.isEmpty() ) {
			params.put("situacaoProcesso", situacaoProcesso);
		}

		return params;
	}

	public List<RelatorioProdutividadeVO> getRelatorioProdutividadeAnalistaNull(Date dataInicio, Date dataFim, Long tipoProcessoId, List<Situacao> situacaoProcesso) {

		StringBuilder hql = new StringBuilder();

		hql.append(" select ");
		hql.append(" 	0, ");
		hql.append(" 	'[vazio]', ");
		hql.append(" 	count(*), ");//atividades
		hql.append(" 	count(distinct m.id), ");//requisicoes
		hql.append(" 	( ");
		hql.append(" 		select count(*) from Processo m2, net.wasys.getdoc.domain.entity.ProcessoLog ml2 where ");
		hql.append(" 		ml2.processo.id = m2.id and ml2.usuario.id is null and m2.dataCriacao >= :dataInicio and m2.dataCriacao < :dataFim and ml2.acao = :CRIACAO ");
		hql.append(" 	), ");//cadastros manuais
		hql.append(" 	( ");
		hql.append(" 		select count(*) from Processo m2, net.wasys.getdoc.domain.entity.ProcessoLog ml2 where ");
		hql.append(" 		ml2.processo.id = m2.id and ml2.usuario.id is null and m2.dataCriacao >= :dataInicio and m2.dataCriacao < :dataFim and ml2.acao = :IMPORTACAO ");
		hql.append(" 	), ");//cadastros automaticos
		hql.append(" 	( ");
		hql.append(" 		select count(*) from Processo m2, net.wasys.getdoc.domain.entity.ProcessoLog ml2 where ");
		hql.append(" 		ml2.processo.id = m2.id and ml2.usuario.id is null and ml2.data >= :dataInicio and m2.dataCriacao < :dataFim and ml2.acao = :EM_ACOMPANHAMENTO ");
		hql.append(" 	), ");//acompanhamentos
		hql.append(" 	( ");
		hql.append(" 		select count(*) from Processo m2, net.wasys.getdoc.domain.entity.ProcessoLog ml2 where ");
		hql.append(" 		ml2.processo.id = m2.id and ml2.usuario.id is null and ml2.data >= :dataInicio and ml2.data < :dataFim and ml2.acao in (:CONCLUSAO, :CANCELAMENTO) ");
		hql.append(" 		and (select count(*) from net.wasys.getdoc.domain.entity.ProcessoLog ml3 where ml3.processo.id = m2.id and ml3.id < ml2.id and ml3.acao = 'EM_ACOMPANHAMENTO') = 0 ");
		hql.append(" 	), ");//finalização analise->fim
		hql.append(" 	( ");
		hql.append(" 		select count(*) from Processo m2, net.wasys.getdoc.domain.entity.ProcessoLog ml2 where ");
		hql.append(" 		ml2.processo.id = m2.id and ml2.usuario.id is null and ml2.data >= :dataInicio and ml2.data < :dataFim and ml2.acao in (:CONCLUSAO, :CANCELAMENTO) ");
		hql.append(" 		and (select count(*) from net.wasys.getdoc.domain.entity.ProcessoLog ml3 where ml3.processo.id = m2.id and ml3.id < ml2.id and ml3.acao = :EM_ACOMPANHAMENTO) > 0 ");
		hql.append(" 	) ");//finalização acompanhamento->fim
		hql.append(" from ");
		hql.append(" 	net.wasys.getdoc.domain.entity.ProcessoLog ml, ");
		hql.append(" 	Processo m ");
		hql.append(" where 	1=1 ");
		hql.append(" 	and ml.processo.id = m.id ");
		hql.append(" 	and ml.usuario.id is null ");
		hql.append(" 	and ml.data > :dataInicio and ml.data < :dataFim ");
		hql.append(" 	and (ml.usuario.id is null or (select count(*) from Role r where r.usuario.id = ml.usuario.id and r.nome in (:ADMIN, :GESTOR, :ANALISTA)) > 0) ");
		if(tipoProcessoId != null) {
			hql.append(" and m.tipoProcesso.id = :idTipoProcesso ");
		}
		if(situacaoProcesso != null && !situacaoProcesso.isEmpty() ) {
			hql.append(" and m.situacao in (:situacaoProcesso) ");
		}
		hql.append(" group by ");
		hql.append(" 	ml.usuario.id ");

		Map<String, Object> params = criarParametrosRelatorioProdutividade(dataInicio, dataFim, tipoProcessoId, situacaoProcesso);

		Query query = createQuery(hql, params);

		List<Object[]> list = query.list();
		List<RelatorioProdutividadeVO> result = fillResult(list);

		return result;
	}

	public List<RelatorioProdutividadeVO> getRelatorioProdutividadeTipoRequisicao(Date dataInicio, Date dataFim, List<CampoDinamicoVO> camposFiltros) {

		StringBuilder hql = new StringBuilder();

		hql.append(" select ");
		hql.append(" 	c.id, ");
		hql.append(" 	c.nome, ");
		hql.append(" 	count(*), ");//atividades
		hql.append(" 	count(distinct m.id), ");//processos
		hql.append(" 	( ");
		hql.append(" 		select count(*) from Processo m2, net.wasys.getdoc.domain.entity.ProcessoLog ml2 where ");
		hql.append(" 		ml2.processo.id = m2.id and m2.tipoProcesso.id = c.id and m2.dataCriacao >= :dataInicio and m2.dataCriacao < :dataFim and ml2.acao = :CRIACAO ");
		hql.append(" 	), ");//cadastros manuais
		hql.append(" 	( ");
		hql.append(" 		select count(*) from Processo m2, net.wasys.getdoc.domain.entity.ProcessoLog ml2 where ");
		hql.append(" 		ml2.processo.id = m2.id and m2.tipoProcesso.id = c.id and m2.dataCriacao >= :dataInicio and m2.dataCriacao < :dataFim and ml2.acao = :IMPORTACAO ");
		hql.append(" 	), ");//cadastros automaticos
		hql.append(" 	( ");
		hql.append(" 		select count(*) from Processo m2, net.wasys.getdoc.domain.entity.ProcessoLog ml2 where ");
		hql.append(" 		ml2.processo.id = m2.id and m2.tipoProcesso.id = c.id and ml2.data >= :dataInicio and m2.dataCriacao < :dataFim and ml2.acao = :EM_ACOMPANHAMENTO ");
		hql.append(" 	), ");//acompanhamentos
		hql.append(" 	( ");
		hql.append(" 		select count(*) from Processo m2, net.wasys.getdoc.domain.entity.ProcessoLog ml2 where ");
		hql.append(" 		ml2.processo.id = m2.id and m2.tipoProcesso.id = c.id and ml2.data >= :dataInicio and ml2.data < :dataFim and ml2.acao in (:CONCLUSAO, :CANCELAMENTO) ");
		hql.append(" 		and (select count(*) from net.wasys.getdoc.domain.entity.ProcessoLog ml3 where ml3.processo.id = m2.id and ml3.id < ml2.id and ml3.acao = :EM_ACOMPANHAMENTO) = 0 ");
		hql.append(" 	), ");//finalização analise->fim
		hql.append(" 	( ");
		hql.append(" 		select count(*) from Processo m2, net.wasys.getdoc.domain.entity.ProcessoLog ml2 where ");
		hql.append(" 		ml2.processo.id = m2.id and m2.tipoProcesso.id = c.id and ml2.data >= :dataInicio and ml2.data < :dataFim and ml2.acao in (:CONCLUSAO, :CANCELAMENTO) ");
		hql.append(" 		and (select count(*) from net.wasys.getdoc.domain.entity.ProcessoLog ml3 where ml3.processo.id = m2.id and ml3.id < ml2.id and ml3.acao = :EM_ACOMPANHAMENTO) > 0 ");
		hql.append(" 	) ");//finalização acompanhamento->fim
		hql.append(" from ");
		hql.append(" 	net.wasys.getdoc.domain.entity.ProcessoLog ml, ");
		hql.append(" 	Processo m, ");
		hql.append(" 	net.wasys.getdoc.domain.entity.TipoProcesso c ");
		hql.append(" where 	1=1 ");
		hql.append(" 	and ml.processo.id = m.id ");
		hql.append(" 	and m.tipoProcesso.id = c.id ");
		hql.append(" 	and ml.data > :dataInicio and ml.data < :dataFim ");

		Map<String, Object> params = criarParametrosRelatorioProdutividade(dataInicio, dataFim, null, null);

		if(camposFiltros != null && !camposFiltros.isEmpty()) {
			for (int i=0; i < camposFiltros.size(); i++) {
				CampoDinamicoVO cd = camposFiltros.get(i);
				String nomeGrupo = cd.getNomeGrupo();
				String nomeCampo = cd.getNomeCampo();

				List<String> chavesUnicidade = cd.getChavesUnicidade();
				String valorCampo = cd.getValorCampo();
				if(org.apache.commons.lang3.StringUtils.isBlank(valorCampo) && CollectionUtils.isEmpty(chavesUnicidade)) {
					continue;
				}

				hql.append(" and m.id in ( ");
				hql.append(" 	select cc.grupo.processo.id ");
				hql.append(" 	from ").append(Campo.class.getName()).append(" c ");
				hql.append(" 	where cc.grupo.nome = :nomeGrupo"+i+" ");
				params.put("nomeGrupo"+i,nomeGrupo);
				hql.append(" 	and cc.nome = :nomeCampo"+i+" ");
				params.put("nomeCampo"+i,nomeCampo);
				if(chavesUnicidade != null){
					hql.append(" and ( 1=2 ");
					for(String chaveUnicidade : chavesUnicidade){
						hql.append(" 	or upper(cc.valor) like :chaveUnicidade"+i+" ");
						chaveUnicidade = org.apache.commons.lang3.StringUtils.upperCase(chaveUnicidade);
						params.put("chaveUnicidade"+i,chaveUnicidade);
					}
					hql.append(" )");
				} else{
					hql.append(" and upper(cc.valor) like :valorCampo"+i+" ");
					valorCampo = valorCampo.toUpperCase();
					valorCampo = valorCampo.trim();
					params.put("valorCampo"+i,"%" + valorCampo + "%");
				}
				hql.append(" ) ");
			}
		}

		hql.append(" 	and (ml.usuario.id is null or (select count(*) from Role r where r.usuario.id = ml.usuario.id and r.nome in (:ADMIN, :GESTOR, :ANALISTA)) > 0) ");
		hql.append(" group by ");
		hql.append(" 	c.id, ");
		hql.append(" 	c.nome ");
		hql.append(" order by ");
		hql.append(" 	c.nome ");

		Query query = createQuery(hql, params);

		List<Object[]> list = query.list();
		List<RelatorioProdutividadeVO> result = fillResult(list);

		return result;
	}

	public List<RelatorioProdutividadeVO> getRelatorioProdutividadeSituacao(Date dataInicio, Date dataFim, List<CampoDinamicoVO> camposFiltros) {

		// TODO

		return null;
/*		Map<String, Object> params = new HashMap<String, Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select ");
		hql.append(" 	c.id, ");
		hql.append(" 	c.nome, ");
		hql.append(" 	count(*), ");//atividades
		hql.append(" 	count(distinct m.id), ");//processos
		hql.append(" 	( ");
		hql.append(" 		select count(*) from Processo m2, net.wasys.getdoc.domain.entity.ProcessoLog ml2 where ");
		hql.append(" 		ml2.processo.id = m2.id and m2.tipoProcesso.id = c.id and m2.dataCriacao >= :dataInicio and m2.dataCriacao < :dataFim and ml2.acao = :CRIACAO ");
		hql.append(" 	), ");//cadastros manuais
		hql.append(" 	( ");
		hql.append(" 		select count(*) from Processo m2, net.wasys.getdoc.domain.entity.ProcessoLog ml2 where ");
		hql.append(" 		ml2.processo.id = m2.id and m2.tipoProcesso.id = c.id and m2.dataCriacao >= :dataInicio and m2.dataCriacao < :dataFim and ml2.acao = :IMPORTACAO ");
		hql.append(" 	), ");//cadastros automaticos
		hql.append(" 	( ");
		hql.append(" 		select count(*) from Processo m2, net.wasys.getdoc.domain.entity.ProcessoLog ml2 where ");
		hql.append(" 		ml2.processo.id = m2.id and m2.tipoProcesso.id = c.id and ml2.data >= :dataInicio and m2.dataCriacao < :dataFim and ml2.acao = :EM_ACOMPANHAMENTO ");
		hql.append(" 	), ");//acompanhamentos
		hql.append(" 	( ");
		hql.append(" 		select count(*) from Processo m2, net.wasys.getdoc.domain.entity.ProcessoLog ml2 where ");
		hql.append(" 		ml2.processo.id = m2.id and m2.tipoProcesso.id = c.id and ml2.data >= :dataInicio and ml2.data < :dataFim and ml2.acao in (:CONCLUSAO, :CANCELAMENTO) ");
		hql.append(" 		and (select count(*) from net.wasys.getdoc.domain.entity.ProcessoLog ml3 where ml3.processo.id = m2.id and ml3.id < ml2.id and ml3.acao = 'EM_ACOMPANHAMENTO') = 0 ");
		hql.append(" 	), ");//finalização analise->fim
		hql.append(" 	( ");
		hql.append(" 		select count(*) from Processo m2, net.wasys.getdoc.domain.entity.ProcessoLog ml2 where ");
		hql.append(" 		ml2.processo.id = m2.id and m2.tipoProcesso.id = c.id and ml2.data >= :dataInicio and ml2.data < :dataFim and ml2.acao in (:CONCLUSAO, :CANCELAMENTO) ");
		hql.append(" 		and (select count(*) from net.wasys.getdoc.domain.entity.ProcessoLog ml3 where ml3.processo.id = m2.id and ml3.id < ml2.id and ml3.acao = :EM_ACOMPANHAMENTO) > 0 ");
		hql.append(" 	) ");//finalização acompanhamento->fim
		hql.append(" from ");
		hql.append(" 	net.wasys.getdoc.domain.entity.ProcessoLog ml, ");
		hql.append(" 	Processo m, ");
		hql.append(" 	net.wasys.getdoc.domain.entity.TipoProcesso c ");
		hql.append(" where 	1=1 ");
		hql.append(" 	and ml.processo.id = m.id ");
		hql.append(" 	and m.tipoProcesso.id = c.id ");
		hql.append(" 	and ml.data > :dataInicio and ml.data < :dataFim ");
		hql.append(" 	and (ml.usuario.id is null or (select count(*) from Role r where r.usuario.id = ml.usuario.id and r.nome in (:ADMIN, :GESTOR, :ANALISTA)) > 0) ");
		hql.append(" group by ");
		hql.append(" 	c.id, ");
		hql.append(" 	c.nome ");
		hql.append(" order by ");
		hql.append(" 	c.nome ");

		params.put("CRIACAO", AcaoProcesso.CRIACAO);
		params.put("IMPORTACAO", AcaoProcesso.IMPORTACAO);
		params.put("EM_ACOMPANHAMENTO", AcaoProcesso.EM_ACOMPANHAMENTO);
		params.put("CONCLUSAO", AcaoProcesso.CONCLUSAO);
		params.put("CANCELAMENTO", AcaoProcesso.CANCELAMENTO);

		params.put("ADMIN", RoleGD.GD_ADMIN.name());
		params.put("GESTOR", RoleGD.GD_GESTOR.name());
		params.put("ANALISTA", RoleGD.GD_ANALISTA.name());

		dataInicio = DateUtils.truncate(dataInicio, Calendar.DAY_OF_MONTH);
		dataFim = DateUtils.addDays(dataFim, 1);
		dataFim = DateUtils.addMilliseconds(dataFim, -1);
		params.put("dataInicio", dataInicio);
		params.put("dataFim", dataFim);

		Query query = createQuery(hql, params);

		List<Object[]> list = query.list();
		List<RelatorioProdutividadeVO> result = fillResult(list);

		return result;*/
	}

	private List<RelatorioProdutividadeVO> fillResult(List<Object[]> list) {

		List<RelatorioProdutividadeVO> result = new ArrayList<RelatorioProdutividadeVO>();

		for (Object[] arr : list) {

			Long canalId = ((Number) arr[0]).longValue();
			String canalDescricao = (String) arr[1];
			Long atividades = (Long) arr[2];
			Long requisicoes = (Long) arr[3];
			Long cadastroManual = (Long) arr[4];
			Long cadastroAutomatio = (Long) arr[5];
			Long emAcompanhamento = (Long) arr[6];
			Long finalizadosAnalise = (Long) arr[7];
			Long finalizadosAcompanhamento = (Long) arr[8];

			RelatorioProdutividadeVO vo = new RelatorioProdutividadeVO();
			vo.setRegistroId(canalId);
			vo.setRegistroDescricao(canalDescricao);
			vo.setAtividades(atividades);
			vo.setRequisicoes(requisicoes);
			vo.setCadastroManual(cadastroManual);
			vo.setCadastroAutomatio(cadastroAutomatio);
			vo.setEmAcompanhamento(emAcompanhamento);
			vo.setFinalizadosAnalise(finalizadosAnalise);
			vo.setFinalizadosAcompanhamento(finalizadosAcompanhamento);

			result.add(vo);
		}
		return result;
	}

	private List<RelatorioProdutividadeVO> fillResultCSC(List<Object[]> list) {

		List<RelatorioProdutividadeVO> result = new ArrayList<RelatorioProdutividadeVO>();

		for (Object[] arr : list) {

			Long canalId = ((Number) arr[0]).longValue();
			String canalDescricao = (String) arr[1];
			Long atividades = (Long) arr[2];
			Long requisicoes = (Long) arr[3];
			Long finalizadosAnalise = (Long) arr[4];
			Long finalizadosAcompanhamento = (Long) arr[5];
			Long finalizadosPreAnalise = (Long) arr[6];

			RelatorioProdutividadeVO vo = new RelatorioProdutividadeVO();
			vo.setRegistroId(canalId);
			vo.setRegistroDescricao(canalDescricao);
			vo.setAtividades(atividades);
			vo.setRequisicoes(requisicoes);
			vo.setFinalizadosAnalise(finalizadosAnalise);
			vo.setFinalizadosAcompanhamento(finalizadosAcompanhamento);
			vo.setFinalizadosPreAnalise(finalizadosPreAnalise);

			result.add(vo);
		}
		return result;
	}

	private List<RelatorioProdutividadeVO> fillResultMedicina(List<Object[]> list) {

		List<RelatorioProdutividadeVO> result = new ArrayList<RelatorioProdutividadeVO>();

		for (Object[] arr : list) {

			Long canalId = ((Number) arr[0]).longValue();
			String canalDescricao = (String) arr[1];
			Long atividades = (Long) arr[2];
			Long requisicoes = (Long) arr[3];
			Long finalizadosAnalise = (Long) arr[4];

			RelatorioProdutividadeVO vo = new RelatorioProdutividadeVO();
			vo.setRegistroId(canalId);
			vo.setRegistroDescricao(canalDescricao);
			vo.setAtividades(atividades);
			vo.setRequisicoes(requisicoes);
			vo.setFinalizadosAnalise(finalizadosAnalise);

			result.add(vo);
		}
		return result;
	}

	public List<Long> findIdsByFiltro(ProcessoLogFiltro filtro) {

		StringBuilder hql = new StringBuilder("select pl.id ");
		hql.append(getStartQuery() + " pl ");

		Map<String, Object> params = makeWhereByFiltro(filtro, hql);

		hql.append(" order by ").append("pl.data desc");

		Query query = createQuery(hql, params);

		query.setFetchSize(100);

		List<Long> list = query.list();
		//tirando os registros repetidos
		Set<Long> set = new LinkedHashSet<>(list);
		list = new ArrayList<>(set);
		return list;
	}

	private Map<String, Object> makeWhereByFiltro(ProcessoLogFiltro filtro, StringBuilder hql) {
		return makeWhereByFiltro(filtro, hql, new LinkedHashMap<>());
	}

	private Map<String, Object> makeWhereByFiltro(ProcessoLogFiltro filtro, StringBuilder hql, Map<String, Object> params) {

		Usuario analista = filtro.getAnalista();
		StatusProcesso[] status = filtro.getStatusArray();
		Date dataInicio = filtro.getDataInicio();
		Date dataFim = filtro.getDataFim();
		AcaoProcesso[] acao = filtro.getAcaoArray();
		List<Long> tipoEvidencia = filtro.getTipoEvidencia();
		Long processoId = filtro.getProcessoId();
		List<Situacao> situacoes = filtro.getSituacao();
		List<Long> situacaoAnteriorId = filtro.getSituacaoAnteriorId();
		boolean filtrarRoles = filtro.getFiltrarRoles();
		Boolean apenasComSituacao = filtro.isApenasComSituacao();
		List<Etapa> etapas = filtro.getEtapas();
		Boolean apenasComEtapa = filtro.getApenasComEtapa();

		hql.append(" where 1=1 ");

		if(dataInicio != null && dataFim != null) {

			dataInicio = DateUtils.truncate(dataInicio, Calendar.DAY_OF_MONTH);

			Calendar dataFimC = Calendar.getInstance();
			dataFimC.setTime(dataFim);
			dataFimC.add(Calendar.DAY_OF_MONTH, 1);
			dataFimC = DateUtils.truncate(dataFimC, Calendar.DAY_OF_MONTH);
			dataFimC.add(Calendar.MILLISECOND, -1);

			dataFim = dataFimC.getTime();

			hql.append(" and ( 1 != 1 ");

			hql.append(" or pl.data between :dataInicio and :dataFim ");
			params.put("dataInicio", dataInicio);
			params.put("dataFim", dataFim);

			hql.append(" ) ");
		}

		if (tipoEvidencia != null) {
			hql.append(" and pl.tipoEvidencia.id = :tipoEvidenciaId ");
			params.put("tipoEvidenciaId", tipoEvidencia);
		}

		if (processoId != null) {
			hql.append(" and pl.processo.id = :processoId ");
			params.put("processoId", processoId);
		}

		if(analista != null) {
			hql.append(" and pl.usuario.id = :analistaId ");
			Long analistaId = analista.getId();
			params.put("analistaId", analistaId);
		}
		else if(filtrarRoles) {
			hql.append(" and (select count(*) from Role r where r.usuario.id = pl.usuario.id and r.nome in (:ROLE_1, :ROLE_2, :ROLE_3)) > 0 ");
			params.put("ROLE_1", RoleGD.GD_ADMIN.name());
			params.put("ROLE_2", RoleGD.GD_GESTOR.name());
			params.put("ROLE_3", RoleGD.GD_ANALISTA.name());
		}

		if(acao != null && acao.length > 0) {
			hql.append(" and pl.acao in ( ");
			int size = acao.length;
			for (int i = 0; i < size; i++) {
				hql.append("'").append(acao[i]).append("'");
				if(i != size - 1) {
					hql.append(", ");
				}
			}
			hql.append(" ) ");
		}

		if(status != null && status.length > 0) {
			hql.append(" and pl.processo.status in ( ");
			for (int i = 0; i < status.length; i++) {
				hql.append(":statusProcesso"+i).append(i == status.length -1 ? "" : ", ");
				StatusProcesso statusProcesso = status[i];
				params.put("statusProcesso"+i, statusProcesso);
			}
			hql.append(" ) ");
		}

		if(situacoes != null && !situacoes.isEmpty()) {
			hql.append(" and pl.situacao.id in ( -1 ");
			for (Situacao situacao : situacoes) {
				Long situacaoId = situacao.getId();
				hql.append(", ").append(situacaoId);
			}
			hql.append(" ) ");
		}

		if(situacaoAnteriorId != null && !situacaoAnteriorId.isEmpty()) {
			hql.append(" and pl.situacaoAnterior.id in ( -1 ");
			for (Long id : situacaoAnteriorId) {
				hql.append(", ").append(id);
			}
			hql.append(" ) ");
		}

		if(apenasComSituacao != null) {
			hql.append(" and pl.situacao.id is " + (apenasComSituacao ? "not" : "") + " null ");
		}

		if(etapas != null && !etapas.isEmpty()) {
			hql.append(" and pl.etapa in (:etapas) ");
			params.put("etapas", etapas);
		}

		if(apenasComEtapa != null) {
			hql.append(" and pl.etapa.id is " + (apenasComEtapa ? "not" : "") + " null ");
		}

		return params;
	}

	public List<ProcessoLog> findByIds(List<Long> ids) {
		StringBuilder hql = new StringBuilder();
		hql.append(getStartQuery());

		hql.append(" p where p.id in (-1");
		for (Long id : ids) {
			hql.append(", ").append(id);
		}
		hql.append(") ");

		hql.append(" order by p.data desc ");

		Query query = createQuery(hql.toString());
		query.setFetchSize(100);

		return query.list();
	}

	public List<ProcessoLog> findByFiltro(ProcessoLogFiltro filtro, Integer inicio, Integer max) {
		StringBuilder hql = new StringBuilder(" select pl ");
		hql.append(getStartQuery() + " pl ");

		Map<String, Object> params = makeWhereByFiltro(filtro, hql);

		String campoOrdem = filtro.getCampoOrdem();
		campoOrdem = StringUtils.isNotBlank(campoOrdem) ? campoOrdem : "pl.data";
		SortOrder ordem = filtro.getOrdem();
		ordem = ordem != null ? ordem : SortOrder.DESCENDING;
		String ordemStr = SortOrder.ASCENDING.equals(ordem) ? " asc " : " desc ";
		hql.append(" order by ").append(campoOrdem).append(" ").append(ordemStr);

		Query query = createQuery(hql, params);

		if(inicio != null && max != null) {
			query.setFirstResult(inicio);
			query.setMaxResults(max);
		}

		query.setFetchSize(100);

		List<ProcessoLog> list = query.list();
		//tirando os registros repetidos
		Set<ProcessoLog> set = new LinkedHashSet<>(list);
		list = new ArrayList<>(set);
		return list;
	}

	public Integer countByFiltro(ProcessoLogFiltro filtro) {
		StringBuilder hql = new StringBuilder("select count(*) from " + clazz.getName() + " pl ");

		Map<String, Object> params = makeWhereByFiltro(filtro, hql);

		Query query = createQuery(hql, params);

		return ((Long) query.uniqueResult()).intValue();
	}

	public Set<Long> getLogsNaoLidos(List<Long> processosIds, AcaoProcesso acao, boolean retorneLogId) {

		List<Object> params = new ArrayList<Object>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select ");
		if (retorneLogId) {
			hql.append(" 	l.id ");
		} else {
			hql.append(" 	l.processo.id ");
		}
		hql.append(" from ");
		hql.append(" 	").append(ProcessoLog.class.getName()).append(" l ");

		hql.append(" where l.processo.id in ( -1 ");
		for (Long processoId : processosIds) {
			hql.append(", ?");
			params.add(processoId);
		}
		hql.append(" ) ");

		hql.append(" and (l.lido is not null and l.lido is false ) ");

		if (acao != null) {
			hql.append(" and l.acao = ? ");
			params.add(acao);
		}

		Query query = createQuery(hql.toString(), params);

		List<Long> list = query.list();
		Set<Long> set = new HashSet<>();

		for (Long obj : list) {
			Long processoId = (Long) obj;
			set.add(processoId);
		}

		return set;
	}

	public List<Situacao> findSituacoesProcesso(Long processoId) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select pl.situacao from ").append(clazz.getName()).append(" pl ");

		hql.append(" where pl.situacao.id is not null ");
		hql.append(" and pl.processo.id = :processoId ");
		params.put("processoId", processoId);

		hql.append(" order by pl.id ");

		Query query = createQuery(hql.toString(), params);

		List<Situacao> list = query.list();
		return list;
	}

	public List<Object[]> findAtividadesByFiltro(RelatorioAtividadeFiltro filtro) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select ");
		hql.append(" pl.processo.id, ");
		hql.append(" ( ");
		hql.append(" 	select c.valor ").append("	from ").append(Campo.class.getName()).append(" c ");
		hql.append(" 	where c.grupo.processo.id = pl.processo.id ");
		hql.append(" 	and c.grupo.nome = :grupoNumero and c.nome = :campoNumero ");
		hql.append(" ), ");
		hql.append(" pl.usuario.nome, ");
		hql.append(" pl.data, ");
		hql.append(" pl.acao, ");
		hql.append(" pl.observacao, ");
		hql.append(" te.descricao, ");
		hql.append(" aa.descricao, ");
		hql.append(" sa.descricao ");
		params.put("grupoNumero", CampoMap.CampoEnum.CPF_CNPJ.getGrupo().getNome());
		params.put("campoNumero", CampoMap.CampoEnum.CPF_CNPJ.getNome());

		hql.append(getStartQuery() + " pl ");
		hql.append(" left outer join pl.tipoEvidencia te ");
		hql.append(" left outer join pl.solicitacao so ");
		hql.append(" left outer join so.subarea sa ");
		hql.append(" left outer join sa.area aa ");

		makeWhereByFiltro(filtro, hql, params);

		hql.append(" order by pl.usuario.id, pl.data ");

		Query query = createQuery(hql, params);

		query.setFetchSize(100);

		List<Object[]> list = query.list();
		return list;
	}

	public Boolean existPendenciaByProcesso(Long processoId) {
		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select count(*) from ").append(ProcessoLog.class.getName()).append(" pl ");
		hql.append(" where pl.processo.id = :processoId ");
		params.put("processoId", processoId);

		hql.append(" and pl.statusProcesso = :status ");
		params.put("status", StatusProcesso.PENDENTE);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue() > 0;
	}

	public boolean existePosteriorA(Date data) {
		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select 1 from ").append(ProcessoLog.class.getName()).append(" pl ");
		hql.append(" where pl.data > :data and situacao.id is not null ");
		params.put("data", data);

		Query query = createQuery(hql.toString(), params);

		query.setFirstResult(0);
		query.setMaxResults(1);

		Object result = query.uniqueResult();
		return result != null;
	}

	public List<Object[]> getRelatorioProdutividadeProuni(RelatorioProdutividadeProuniFiltro filtro) {

		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new LinkedHashMap<>();

		Date dataInicio = filtro.getDataInicio();
		Date dataFim = filtro.getDataFim();

		hql.append("select " );
		hql.append(" p as processo, ");
		hql.append(" ( select max(pl) from ").append(ProcessoLog.class.getName()).append(" pl");
		hql.append(" 	where pl.processo.id = p.id");
		hql.append(" 	and pl.situacao.id = :solicitacao) as log_abertura,");

		hql.append(" (select count(*) from ").append(ProcessoLog.class.getName()).append(" pl ");
		hql.append(" where pl.processo.id = p.id and pl.situacao.id = :solicitacao) as qtd_abertura, ");

		hql.append(" (select count(*) from ").append(Imagem.class.getName()).append(" i");
		hql.append(" where i.documento.processo.id = p.id ) as qtd_anexos, ");

		hql.append(" (select count(*) from ").append(ProcessoLog.class.getName()).append(" pl");
		hql.append(" 		where pl.processo.id = p.id");
		hql.append(" 		and pl.id > (");
		hql.append(" 			select max(pl2.id) from ").append(ProcessoLog.class.getName()).append(" pl2");
		hql.append(" 				where pl2.situacao.id = 360");
		hql.append(" 				and pl2.processo.id = p.id)");
		hql.append(" 				and pl.usuario.id is not null ");
		hql.append(" 				and pl.id <= (");
		hql.append(" 			select max(pl3.id) from ").append(ProcessoLog.class.getName()).append("  pl3");
		hql.append(" 				where pl3.situacao.id = 272");
		hql.append(" 				and pl3.processo.id = p.id))");
		hql.append(" + ");
		hql.append(" (select count(*) from ").append(DocumentoLog.class.getName()).append(" dl");
		hql.append(" 		where dl.usuario.id <> null and dl.documento.processo.id = p.id");
		hql.append(" 		and dl.data between (");
		hql.append("					select max(pl.data) from ").append(ProcessoLog.class.getName()).append("  pl");
		hql.append(" 						where pl.situacao.id = 360");
		hql.append(" 						and pl.processo.id = p.id)");
		hql.append(" 						and (select max(pl2.data) from ").append(ProcessoLog.class.getName()).append("  pl2");
		hql.append(" 								where pl2.situacao.id = 272");
		hql.append(" 								and pl2.processo.id = p.id))");
		hql.append(" 		as qtd_acoes, ");

		hql.append(" (select max(pl) from ").append(ProcessoLog.class.getName()).append(" pl ");
		hql.append(" 		where pl.processo.id = p.id ");
		hql.append("		and pl.usuario.id is not null ");
		hql.append("		and pl.situacao.id = :assinatura) as responsavel, ");

		hql.append(" ( select max(pl) from ").append(ProcessoLog.class.getName()).append(" pl");
		hql.append(" 	where pl.processo.id = p.id ");
		hql.append("	and pl.situacao.id = :assinatura ");
		hql.append(" 	and pl.data > ( select max(pl2.data) from ").append(ProcessoLog.class.getName()).append(" pl2");
		hql.append(" 				where pl2.processo.id = p.id ");
		hql.append("				and pl2.situacao.id = :solicitacao)) as log_fechamento ");

		hql.append(" from ").append(Processo.class.getName()).append(" p");
		hql.append(" where p.tipoProcesso.id = :tipoProcessoId");
		hql.append(" and (select count(*) from ").append(ProcessoLog.class.getName()).append(" pl ");
		hql.append("      where pl.situacao.id in (:assinatura, :solicitacao) and pl.processo.id = p.id) > 0");

		if(dataInicio != null && dataFim != null) {

			dataInicio = DateUtils.truncate(dataInicio, Calendar.DAY_OF_MONTH);

			Calendar dataFimC = Calendar.getInstance();
			dataFimC.setTime(dataFim);
			dataFimC.add(Calendar.DAY_OF_MONTH, 1);
			dataFimC = DateUtils.truncate(dataFimC, Calendar.DAY_OF_MONTH);
			dataFimC.add(Calendar.MILLISECOND, -1);

			dataFim = dataFimC.getTime();

			hql.append(" and ( 1 != 1 ");

			hql.append(" or p.dataCriacao between :dataInicio and :dataFim ");
			params.put("dataInicio", dataInicio);
			params.put("dataFim", dataFim);

			hql.append(" ) ");
		}

		params.put("tipoProcessoId", TipoProcesso.SIS_PROUNI);
		params.put("assinatura", Situacao.COLHER_ASSINATURA_TCB_TR);
		params.put("solicitacao", Situacao.SOLICITAR_EMISSAO_TCB_TR);

		Query query = createQuery(hql, params);
		return query.list();
	}

	public Usuario getAnalistaByProcessoIdAndData(Long processoId, Date data) {
		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select pl.usuario from ").append(ProcessoLog.class.getName()).append(" pl ");
		hql.append(" where pl.processo.id = :processoId ");
		params.put("processoId", processoId);
		hql.append(" and pl.data = :data ");
		if(data == null){
			return	 null;
		}
		params.put("data", data);

		Query query = createQuery(hql.toString(), params);

		query.setFirstResult(0);
		query.setMaxResults(1);

		return (Usuario) query.uniqueResult();
	}

	public Long getTempoTratativa(Date data, Long usuarioId, Long processoId) {
		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select ");
		hql.append(" cast(extract(epoch from ");
		hql.append(" 	age((select max(pl2.data) from processo_log").append(" pl2 ");
		hql.append("		where pl2.acao = :acaoAlteracaoSituacao ");
		hql.append(" 		and pl2.data = :data ");
		hql.append(" 		and pl2.usuario_id = :usuarioId ");
		hql.append(" 		and pl2.processo_id = :processoId ");
		hql.append(" 		), max(pl.data)) *1000) as bigint) from ").append("processo_log").append(" pl ");;
		hql.append(" where pl.acao = :acaoVisualizacao ");
		hql.append(" and pl.data < :data ");
		hql.append(" and pl.usuario_id = :usuarioId ");
		hql.append(" and pl.processo_id = :processoId ");

		params.put("acaoAlteracaoSituacao", AcaoProcesso.ALTERACAO_SITUACAO.name());
		params.put("acaoVisualizacao", AcaoProcesso.VISUALIZOU_PROCESSO.name());
		params.put("data", data);
		params.put("usuarioId", usuarioId);
		params.put("processoId", processoId);

		SQLQuery query = createSQLQuery(hql, params);

		query.setFirstResult(0);
		query.setMaxResults(1);

		Object result = query.uniqueResult();

		return result != null ? ((BigInteger) result).longValue() : 0l;
	}

	public boolean isPodeRegitrarVisualizacao(Long processoId, Long usuarioId) {
		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select count(pl.id) from ").append(ProcessoLog.class.getName()).append(" pl ");
		hql.append(" 	where pl.processo.id = :processoId ");
		hql.append(" 	and pl.acao = :acaoVisualizouProcesso ");
		hql.append(" 	and pl.data > ( select max(pl2.data) from ").append(ProcessoLog.class.getName()).append(" pl2 ");
		hql.append(" 					where pl2.processo.id = :processoId ");
		hql.append(" 					and pl2.acao = :acaoAlteracaoSituacao ) ");
		hql.append(" and pl.usuario.id = :usuarioId");

		params.put("acaoVisualizouProcesso", AcaoProcesso.VISUALIZOU_PROCESSO);
		params.put("processoId", processoId);
		params.put("acaoAlteracaoSituacao", AcaoProcesso.ALTERACAO_SITUACAO);
		params.put("usuarioId", usuarioId);

		Query query = createQuery(hql.toString(), params);

		query.setFirstResult(0);
		query.setMaxResults(1);

		return (Long) query.uniqueResult() == 0L;
	}

	public ProcessoLog findSituacaoAnterior(Processo processo, Situacao situacaoAtual, ProcessoLog processoLogAtual) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select pl from ").append(clazz.getName()).append(" pl ");
		hql.append(" 	left outer join fetch pl.situacaoAnterior ");
		hql.append(" where 1=1 ");
		hql.append(" 	and pl.processo = :processo ");
		hql.append(" 	and pl.situacao = :situacaoAtual ");
		params.put("processo", processo);
		params.put("situacaoAtual", situacaoAtual);

		if (processoLogAtual != null) {
			hql.append(" 	and pl.id < :processoLogAtualId ");
			params.put("processoLogAtualId", processoLogAtual.getId());
		}

		hql.append(" order by pl.id desc ");

		Query query = createQuery(hql, params);
		query.setMaxResults(1);

		return (ProcessoLog) query.uniqueResult();
	}

	public List<Long> findProcessoIdToAjustarEtapa(Date dataInicio, Date dataFim) {

		Map<String, Object> params = new HashMap<>();

		StringBuilder hql = new StringBuilder();
		/*hql.append("select max(pl.id) from ").append(clazz.getName()).append(" pl where pl.data > :date and etapa.id is not null group by pl.processo.id ");
		params.put("date", date);*/

		hql.append(" select distinct(pl.processo.id) from ").append(clazz.getName()).append(" pl ");
		hql.append("	where pl.data between :dataInicio and :dataFim and pl.etapa.id is not null and (");
		hql.append("    	select count(*) from ").append(RelatorioGeralEtapa.class.getName()).append(" rge ");
		hql.append(" 			where rge.relatorioGeral.processoId = pl.processo.id) = 0");

		params.put("dataInicio", dataInicio);
		params.put("dataFim", dataFim);

		Query query = createQuery(hql, params);
		query.setTimeout(20 * 1000);

		return query.list();
	}

	public List<ProcessoLog> findLogToAjustarEtapa(Date dataInicio, Date dataFim) {

		Map<String, Object> params = new HashMap<>();

		StringBuilder hql = new StringBuilder();
		hql.append(startQuery()).append(" pl ");
		hql.append(" where pl.data between :dataInicio and :dataFim ");
		hql.append(" and pl.etapa.id is null ");
		hql.append(" and (select count(*) from ").append(Situacao.class.getName()).append(" s ");
		hql.append(" 		where s.etapa.id is not null and pl.situacao.id = s.id) > 0 ");

		params.put("dataInicio", dataInicio);
		params.put("dataFim", dataFim);

		Query query = createQuery(hql, params);
		query.setTimeout(20 * 1000);

		return query.list();
	}

	public List<ProcessoLog> findLogToAjustarEtapaEPrazoLimiteSituacao(Date dataInicio, Date dataFim) {

		Map<String, Object> params = new HashMap<>();

		StringBuilder hql = new StringBuilder();
		hql.append(startQuery()).append(" pl ");
		hql.append(" where pl.data between :dataInicio and :dataFim ");
		hql.append(" and pl.etapa.id in (:conferenciaIds)");
		hql.append(" and (select count(*) from ").append(Situacao.class.getName()).append(" s ");
		hql.append(" 		where s.etapa.id is not null and pl.situacao.id = s.id) > 0 ");

		params.put("dataInicio", dataInicio);
		params.put("dataFim", dataFim);
		params.put("conferenciaIds", Etapa.CONFERENCIA_BKO);

		Query query = createQuery(hql, params);
		query.setTimeout(20 * 1000);

		return query.list();
	}

	public ProcessoLog findAlteracaoSituacaoAnterior(ProcessoLog processoLogAtual) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select pl from ").append(clazz.getName()).append(" pl ");
		hql.append(" where 1=1 ");
		hql.append(" and pl.id < :processoLogAtualId ");
		hql.append(" and pl.data < :processoLogAtualData ");
		hql.append(" and pl.acao = :acaoAlteracaoSituacao ");
		hql.append(" and pl.processo = :processo ");


		params.put("processoLogAtualId", processoLogAtual.getId());
		params.put("processoLogAtualData", processoLogAtual.getData());
		params.put("acaoAlteracaoSituacao", AcaoProcesso.ALTERACAO_SITUACAO);
		params.put("processo", processoLogAtual.getProcesso());

		hql.append(" order by pl.data desc ");

		Query query = createQuery(hql, params);
		query.setMaxResults(1);

		return (ProcessoLog) query.uniqueResult();
	}

	public ProcessoLog findAlteracaoSituacaoAnteriorWithPrazo(ProcessoLog processoLogAtual) {

		Map<String, Object> params = new LinkedHashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select pl from ").append(clazz.getName()).append(" pl ");
		hql.append(" where 1=1 ");
		hql.append(" and pl.id < :processoLogAtualId ");
		hql.append(" and pl.data < :processoLogAtualData ");
		hql.append(" and pl.acao = :acaoAlteracaoSituacao ");
		hql.append(" and pl.processo = :processo ");
		hql.append(" and pl.prazoLimiteSituacao is not null");


		params.put("processoLogAtualId", processoLogAtual.getId());
		params.put("processoLogAtualData", processoLogAtual.getData());
		params.put("acaoAlteracaoSituacao", AcaoProcesso.ALTERACAO_SITUACAO);
		params.put("processo", processoLogAtual.getProcesso());

		hql.append(" order by pl.data desc ");

		Query query = createQuery(hql, params);
		query.setMaxResults(1);

		return (ProcessoLog) query.uniqueResult();
	}

	public ProcessoLog findLastLogByProcessoAndAcao(Long processoId, AcaoProcesso acao) {

		Map<String, Object> params = new HashMap<>();

		StringBuilder hql = new StringBuilder();
		hql.append(startQuery()).append(" pl ");

		hql.append(" where 1=1 ");
		hql.append(" and pl.processo.id = :processoId ");
		hql.append(" and pl.acao = :acao ");

		hql.append(" order by pl.id desc ");

		params.put("processoId", processoId);
		params.put("acao", acao);

		Query query = createQuery(hql, params);

		query.setMaxResults(1);

		return (ProcessoLog) query.uniqueResult();
	}

	public Map<Long, Date> findMapToAtualizarDataUltimaAtualizacao(Date inicio, Date fim) {

		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select pl.processo.id, max(pl.data) ");
		hql.append(" from ").append(clazz.getName()).append(" pl ");
		hql.append(" where 1=1 ");
		hql.append(" and pl.data between :inicio and :fim ");
		hql.append(" and ( ");
		hql.append("    pl.processo.id in ( ");
		hql.append("        select p.id from ").append(Processo.class.getName()).append(" p where p.dataUltimaAtualizacao is null and pl.processo.id = p.id ");
		hql.append("    ) ");
		hql.append("  or ");
		hql.append("    pl.data > ( ");
		hql.append("        select p.dataUltimaAtualizacao from ").append(Processo.class.getName()).append(" p where pl.processo.id = p.id ");
		hql.append("    ) ");
		hql.append(" ) ");
		hql.append(" group by pl.processo.id ");

		params.put("inicio", inicio);
		params.put("fim", fim);

		Query query = createQuery(hql, params);

		Map<Long, Date> maxDataProcessoLogParaProcessoId = new HashMap<>();
		List<Object[]> list = query.list();
		for (Object[] result : list) {

			Long processoId = (Long) result[0];
			Date ultimaDataProcessoLog = (Date) result[1];

			maxDataProcessoLogParaProcessoId.put(processoId, ultimaDataProcessoLog);
		}

		return maxDataProcessoLogParaProcessoId;
	}


	public ProcessoLog getLastProcessoLogByUsuarioAndData(Usuario usuario, Date data) {
		StringBuilder hql = new StringBuilder();
		Map<String, Object> params = new HashMap<>();

		hql.append(" select pl ").append(" from ").append(clazz.getName()).append(" pl ");
		hql.append(" where pl.id = ");
		hql.append(" 	(select max(pl1.id) ");
		hql.append(" 		from ").append(clazz.getName()).append(" pl1 ");
		hql.append(" 			where pl1.usuario.id = :usuarioId ");
		hql.append(" 			and pl1.data > :data )");

		params.put("usuarioId", usuario.getId());
		params.put("data", data);
		Query query = createQuery(hql,params);

		return (ProcessoLog) query.uniqueResult();
	}
}