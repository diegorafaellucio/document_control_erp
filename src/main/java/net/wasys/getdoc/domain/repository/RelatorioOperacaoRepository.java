package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.*;
import net.wasys.getdoc.domain.vo.RelatorioOperacaoVO;
import net.wasys.getdoc.domain.vo.filtro.RelatorioOperacaoFiltro;
import net.wasys.getdoc.mb.utils.DateUtils;
import net.wasys.util.ddd.HibernateRepository;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class RelatorioOperacaoRepository extends HibernateRepository<Documento> {

	public RelatorioOperacaoRepository() {
		super(Documento.class);
	}

	public List<RelatorioOperacaoVO> findRelatorioOperacao(RelatorioOperacaoFiltro filtro){
		Map<String, Object> params = new HashMap<>();
		StringBuilder hql = new StringBuilder();

		hql.append(" select ");

		//  0 - processo Id
		hql.append(" p2.id, ");

		//  1 - situacao
		hql.append(" s.nome, ");

		//  2 - origem
		hql.append(" p2.origem, ");

		//  3 - regional
		hql.append(" ( ");
		hql.append(" 	select brv.valor ");
		hql.append("	from ").append(BaseRegistroValor.class.getName()).append(" brv, ");
		hql.append(Campo.class.getName()).append(" c2 ");
		hql.append("	where c2.grupo.processo.id = p2.id ");
		hql.append("	and c2.nome = :regionalCampoNome ");
		hql.append("	and c2.valor = brv.baseRegistro.chaveUnicidade ");
		hql.append("	and brv.baseRegistro.baseInterna.id = :regionalBaseInternaId ");
		hql.append("	and brv.nome = brv.baseRegistro.baseInterna.colunaLabel ");
		hql.append(" ) as regional, ");

		//  4 - nome campus
		hql.append(" ( ");
		hql.append(" 	select brv.valor ");
		hql.append("	from ").append(BaseRegistroValor.class.getName()).append(" brv, ");
		hql.append(Campo.class.getName()).append(" c2 ");
		hql.append("	where c2.grupo.processo.id = p2.id ");
		hql.append("    and c2.grupo.nome = :grupoDadosDoInscrito");
		hql.append("	and c2.nome = :campusCampoNome ");
		hql.append("	and c2.valor = brv.baseRegistro.chaveUnicidade ");
		hql.append("	and brv.baseRegistro.baseInterna.id = :campusBaseInternaId ");
		hql.append("	and brv.nome = brv.baseRegistro.baseInterna.colunaLabel ");
		hql.append(" ) as campus, ");

		// 5 - forma ingresso
		hql.append(" ( ");
		hql.append(" 	select brv.valor ");
		hql.append("	from ").append(BaseRegistroValor.class.getName()).append(" brv, ");
		hql.append(Campo.class.getName()).append(" c2 ");
		hql.append("	where c2.grupo.processo.id = p2.id ");
		hql.append("    and c2.grupo.nome = :grupoDadosDoInscrito");
		hql.append("	and c2.nome = :formaIngressoCampoNome ");
		hql.append("	and c2.valor = brv.baseRegistro.chaveUnicidade ");
		hql.append("	and brv.baseRegistro.baseInterna.id = :formaIngressoBaseInternaId ");
		hql.append("	and brv.nome = brv.baseRegistro.baseInterna.colunaLabel ");
		hql.append(" ) as forma_ingresso, ");

		// 6 - periodo de ingresso
		hql.append(" (	select c2.valor from  " + CampoGrupo.class.getName() + " cg2, ");
		hql.append(Campo.class.getName() + " c2 ");
		hql.append("  	  where cg2.processo.id = p2.id ");
		hql.append("      and cg2.nome = :grupoDadosDoInscrito");
		hql.append("  	  and c2.grupo.id  = cg2.id ");
		hql.append("  	  and c2.nome = :periodoIngressoCampoNome ");
		hql.append(" ) as periodo_ingresso, ");

		// 7 - documento id
		hql.append(" d.id, ");

		// 8 - nome documento
		hql.append(" d.nome, ");

		// 9 - status
		hql.append(" d.status, ");

		// 10 - total de imagens
		hql.append(" ( ");
		hql.append(" 	select count(*) ");
		hql.append(" 	from ").append(Imagem.class.getName()).append(" i ");
		hql.append(" 	where i.documento.id = d.id ");
		hql.append("  ) as total_imagens, ");

		// 11 - ação
		hql.append(" dl.acao, ");

		// 12 - irregularidade
		hql.append(" i.nome as irregularidade, ");

		// 13 - observacao
		hql.append(" pe.observacao as observacao, ");

		// 14 - data log
		hql.append(" dl.data, ");

		//15 - usuario login
		hql.append(" u.login, ");

		//16 - usuario nome
		hql.append(" u.nome ");


		hql.append(" from ").append(Documento.class.getName()).append(" d ");
		hql.append(" inner join ").append(Processo.class.getName()).append(" p2 on p2.id = d.processo.id ");
		hql.append(" inner join ").append(Situacao.class.getName()).append(" s on p2.situacao.id = s.id ");
		hql.append(" inner join ").append(DocumentoLog.class.getName()).append(" dl on dl.documento.id = d.id ");
		hql.append(" inner join ").append(Usuario.class.getName()).append(" u on dl.usuario.id = u.id ");
		hql.append(" left outer join ").append(Pendencia.class.getName()).append(" pe on pe.id = dl.pendencia.id ");
		hql.append(" left outer join ").append(Irregularidade.class.getName()).append(" i on i.id = pe.irregularidade.id ");
		hql.append(" where d.status not in (:notStatus) and dl.acao in (:acoesDocumento)");
		makeWhere(filtro, hql, params);
		hql.append(" order by p2.id desc ");

		params.put("acoesDocumento", Arrays.asList(AcaoDocumento.REJEITOU, AcaoDocumento.APROVOU, AcaoDocumento.CRIACAO_INCLUIDO));
		params.put("notStatus", Arrays.asList(StatusDocumento.PROCESSANDO, StatusDocumento.EXCLUIDO));

		params.put("regionalBaseInternaId", BaseInterna.REGIONAL_ID);
		params.put("regionalCampoNome", CampoMap.CampoEnum.REGIONAL.getNome());
		params.put("campusBaseInternaId", BaseInterna.CAMPUS_ID);
		params.put("campusCampoNome", CampoMap.CampoEnum.CAMPUS.getNome());
		params.put("periodoDeIngressoNome", CampoMap.CampoEnum.PERIODO_DE_INGRESSO.getNome());
		params.put("formaIngressoCampoNome", CampoMap.CampoEnum.FORMA_DE_INGRESSO.getNome());
		params.put("formaIngressoBaseInternaId", BaseInterna.FORMA_INGRESSO_ID);
		params.put("periodoIngressoCampoNome", CampoMap.CampoEnum.PERIODO_DE_INGRESSO.getNome());

		params.put("acaoEnvioAnalise", AcaoProcesso.ENVIO_ANALISE);
		params.put("acaoAlteracaoSituacao", AcaoProcesso.ALTERACAO_SITUACAO);
		params.put("acaoStatusProcessoEmAnalise", StatusProcesso.EM_ANALISE);
		params.put("acaoStatusProcessoPendente", StatusProcesso.PENDENTE);
		params.put("acaoStatusProcessoConcluido", StatusProcesso.CONCLUIDO);
		params.put("acaoStatusProcessoEmAcompanhamento", StatusProcesso.EM_ACOMPANHAMENTO);
		params.put("grupoDadosDoInscrito", CampoMap.GrupoEnum.DADOS_DO_INSCRITO.getNome());

		Query query = createQuery(hql, params);

		List<Object[]> objs = query.list();

		List<RelatorioOperacaoVO> vos = new ArrayList<>();
		for(Object[] objects : objs){
			RelatorioOperacaoVO vo = new RelatorioOperacaoVO(objects);
			vos.add(vo);
		}

		return vos;
	}



	private Map<String, Object> makeWhere(RelatorioOperacaoFiltro filtro, StringBuilder hql, Map<String, Object> params) {

		Date dataInicio = filtro.getDataInicio();
		Date dataFim = filtro.getDataFim();
		List<Long> tipoProcessoIds = filtro.getTipoProcessoIds();
		List<Long> subperfilIds = filtro.getSubperfilIds();

		if(dataInicio != null && dataFim != null) {
			dataInicio = DateUtils.getFirstTimeOfDay(dataInicio);
			dataFim = DateUtils.getLastTimeOfDay(dataFim);
			hql.append(" and dl.data >= :dataInicioDia ");
			hql.append(" and dl.data <= :dataFimDia ");
			params.put("dataInicioDia", dataInicio);
			params.put("dataFimDia", dataFim);
		}

		if(tipoProcessoIds != null && !tipoProcessoIds.isEmpty()){
			hql.append(" and p2.tipoProcesso.id in ( :tiposProcessoIds ) ");
			params.put("tiposProcessoIds", tipoProcessoIds);
		}

		if(subperfilIds != null && !subperfilIds.isEmpty()){
			hql.append(" and dl.usuario.id in ( ");
			hql.append(" 	select us.usuario.id from ").append(UsuarioSubperfil.class.getName()).append(" us ");
			hql.append(" 	where us.subperfil.id in ( :subperfilIds ) ) ");
			params.put("subperfilIds", subperfilIds);
		}

		return params;
	}
}
