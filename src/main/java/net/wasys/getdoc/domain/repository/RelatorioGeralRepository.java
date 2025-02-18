package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.StatusProcesso;
import net.wasys.getdoc.domain.vo.CampoDinamicoVO;
import net.wasys.getdoc.domain.vo.filtro.RelatorioGeralFiltro;
import net.wasys.util.ddd.HibernateRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Repository
@SuppressWarnings("unchecked")
public class RelatorioGeralRepository extends HibernateRepository<RelatorioGeral> {

	public RelatorioGeralRepository() {
		super(RelatorioGeral.class);
	}

	public List<RelatorioGeral> findByProcessosIds(List<Long> processosIds2) {

		StringBuilder hql = new StringBuilder();
		hql.append(getStartQuery());

		hql.append(" where processoId in (-1");
		for (Long processoId : processosIds2) {
			hql.append(", ").append(processoId);
		}
		hql.append(" ) ");

		Query query = createQuery(hql.toString());

		return query.list();
	}

	public List<RelatorioGeral> findByFiltro(RelatorioGeralFiltro filtro, Integer inicio, Integer max) {

		StringBuilder hql = new StringBuilder();
		hql.append(getStartQuery()).append(" r ");

		List<Object> params = makeQuery(filtro, hql);

		hql.append("order by r.id");

		Query query = createQuery(hql.toString(), params);

		if(inicio != null) {
			query.setFirstResult(inicio);
		}
		if(max != null) {
			query.setMaxResults(max);
		}

		return query.list();
	}

	public List<Long> findIdsByFiltro(RelatorioGeralFiltro filtro) {

		StringBuilder hql = new StringBuilder();
		hql.append(" select r.id from ").append(clazz.getName()).append(" r ");

		List<Object> params = makeQuery(filtro, hql);

		Query query = createQuery(hql.toString(), params);

		return query.list();
	}

	public int countByFiltro(RelatorioGeralFiltro filtro) {

		StringBuilder hql = new StringBuilder();
		hql.append(" select count(*) from ").append(clazz.getName()).append(" r ");

		List<Object> params = makeQuery(filtro, hql);

		Query query = createQuery(hql.toString(), params);

		return ((Number) query.uniqueResult()).intValue();
	}

	private List<Object> makeQuery(RelatorioGeralFiltro filtro, StringBuilder hql) {

		Date dataInicio = filtro.getDataInicio();
		Date dataFim = filtro.getDataFim();
		RelatorioGeralFiltro.ConsiderarData considerarData = filtro.getConsiderarData();
		List<Situacao> situacoes = filtro.getSituacoes();
		List<Long> situacoesIds = filtro.getSituacoesIds();
		List<Long> tiposProcessoIds = filtro.getTiposProcessoIds();
		List<StatusProcesso> statusList = filtro.getStatusList();
		List<CampoDinamicoVO> camposFiltro = filtro.getCamposFiltro();

		Calendar dataFimC = Calendar.getInstance();
		dataFimC.setTime(dataFim);
		dataFimC.add(Calendar.DAY_OF_MONTH, 1);
		dataFimC = DateUtils.truncate(dataFimC, Calendar.DAY_OF_MONTH);
		dataFimC.add(Calendar.MILLISECOND, -1);
		dataFim = dataFimC.getTime();

		List<Object> params = new ArrayList<Object>();
		List<TipoProcesso> tipoProcessoList = filtro.getTiposProcessoList();

		hql.append(" where 1=1 ");

		if(dataFim != null && dataInicio != null) {

			hql.append(" and ( 1 != 1 ");

			if (considerarData.equals(RelatorioGeralFiltro.ConsiderarData.CRIACAO)) {
				hql.append(" or r.dataCriacao between ? and ? ");
				params.add(dataInicio);
				params.add(dataFim);
			}
			if (considerarData.equals(RelatorioGeralFiltro.ConsiderarData.ATUALIZACAO)) {
				hql.append(" or r.dataUltimaAtualizacao between ? and ? ");
				params.add(dataInicio);
				params.add(dataFim);
			}
			if (considerarData.equals(RelatorioGeralFiltro.ConsiderarData.ENVIO_ANALISE)) {
				hql.append(" or r.dataEnvioAnalise between ? and ? ");
				params.add(dataInicio);
				params.add(dataFim);
			}

			hql.append(" ) ");

			if(tipoProcessoList!= null && !tipoProcessoList.isEmpty()){
				hql.append(" and r.tipoProcesso.id in( -1 ");
				for (TipoProcesso tipoProcesso : tipoProcessoList) {
					hql.append(", ?");
					params.add(tipoProcesso.getId());
				}
				hql.append(" ) ");
			}
		}

		if(situacoes != null && !situacoes.isEmpty()) {
			hql.append(" and r.situacao.id in ( -1 ");
			for (Situacao situacaoAdd : situacoes) {
				hql.append(", ?");
				params.add(situacaoAdd.getId());
			}
			hql.append(" )");
		}

		if(tiposProcessoIds != null && !tiposProcessoIds.isEmpty()){
			hql.append(" and r.tipoProcesso.id in( -1 ");
			for (Long tipoProcessoId : tiposProcessoIds) {
				hql.append(", ?");
				params.add(tipoProcessoId);
			}
			hql.append(" ) ");
		}

		if(statusList != null && !statusList.isEmpty()){
			hql.append(" and r.status in ( '-1' ");
			for (StatusProcesso statusProcesso : statusList) {
				hql.append(", ?");
				params.add(statusProcesso);
			}
			hql.append(" ) ");
		}

		if(situacoes != null && !situacoes.isEmpty()) {
			hql.append(" and r.situacao.id in ( -1 ");
			for (Situacao situacaoAdd : situacoes) {
				hql.append(", ?");
				params.add(situacaoAdd.getId());
			}
			hql.append(" )");
		}

		if(situacoesIds != null && !situacoesIds.isEmpty()) {
			hql.append(" and r.situacao.id in ( -1 ");
			for (Long situacaoId : situacoesIds) {
				hql.append(", ?");
				params.add(situacaoId);
			}
			hql.append(" )");
		}

		if(camposFiltro != null && !camposFiltro.isEmpty()) {
			for (CampoDinamicoVO cd : camposFiltro) {
				String nomeGrupo = cd.getNomeGrupo();
				String nomeCampo = cd.getNomeCampo();

				List<String> chavesUnicidade = cd.getChavesUnicidade();
				String valorCampo = cd.getValorCampo();
				if (StringUtils.isBlank(valorCampo) && CollectionUtils.isEmpty(chavesUnicidade)) {
					continue;
				}

				hql.append(" and r.processoId in ( ");
				hql.append(" 	select p.id ");
				hql.append(" 	from  ").append(Processo.class.getName()).append(" p ");
				hql.append(" 	where r.processoId = p.id  ");
				hql.append(" and p.id in ( ");
				hql.append(" 	select c.grupo.processo.id ");
				hql.append(" 	from ").append(Campo.class.getName()).append(" c ");
				hql.append(" 	where c.grupo.nome = ?  ");
				params.add(nomeGrupo);
				hql.append(" 	and c.nome = ? ");
				params.add(nomeCampo);
				if(chavesUnicidade != null){
					hql.append(" and ( 1=2 ");
					for(String chaveUnicidade : chavesUnicidade){
						hql.append(" 	or upper(c.valor) like ? ");
						chaveUnicidade = StringUtils.upperCase(chaveUnicidade);
						params.add(chaveUnicidade);
					}
					hql.append(" )");
					hql.append(" ) ");
				}
				else{
					hql.append(" and upper(c.valor) like ? ");
					valorCampo = valorCampo.toUpperCase();
					valorCampo = valorCampo.trim();
					params.add("%" + valorCampo + "%");
				}
				hql.append(" ) ");
				hql.append(" ) ");
			}
		}
		return params;
	}

	public List<Long> fingIdsByFiltro(RelatorioGeralFiltro filtro) {

		StringBuilder hql = new StringBuilder();

		hql.append(" select id from ").append(clazz.getName()).append(" r ");

		List<Object> params = makeQuery(filtro, hql);

		hql.append(" order by r.dataCriacao ");

		Query query = createQuery(hql.toString(), params);

		query.setFetchSize(100);

		return query.list();
	}

	public List<RelatorioGeral> findByIds(List<Long> ids) {
		StringBuilder hql = new StringBuilder();
		hql.append(getStartQuery());

		hql.append(" where id in (-1");
		for (Long id : ids) {
			hql.append(", ").append(id);
		}
		hql.append(") ");

		hql.append(" order by dataCriacao ");

		Query query = createQuery(hql.toString());
		query.setFetchSize(500);

		return query.list();
	}

	public RelatorioGeral getByProcesso(Long processoId) {
		StringBuilder hql = new StringBuilder();
		List<Object> params = new ArrayList<Object>();
		hql.append(getStartQuery());

		hql.append(" where processoId = ?");
		params.add(processoId);

		Query query = createQuery(hql.toString(), params);

		query.setFirstResult(0);
		query.setMaxResults(1);

		return (RelatorioGeral) query.uniqueResult();
	}
}
