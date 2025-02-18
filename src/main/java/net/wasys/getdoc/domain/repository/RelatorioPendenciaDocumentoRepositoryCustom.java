package net.wasys.getdoc.domain.repository;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.*;
import net.wasys.getdoc.domain.vo.AlunoVO;
import net.wasys.getdoc.domain.vo.filtro.RelatorioPendenciaDocumentoFiltro;
import org.apache.commons.lang.StringUtils;

import java.util.*;

import static net.wasys.util.DummyUtils.formatarChaveUnicidadeBaseInterna;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class RelatorioPendenciaDocumentoRepositoryCustom {

    public static StringBuilder createHqlRelatorio(Map<String, Object> params, RelatorioPendenciaDocumentoFiltro filtro) {

        StringBuilder hql = new StringBuilder();

        List<String> regionais = filtro.getRegionais();
        List<String> campus = filtro.getCampus();
        List<String> cursos = filtro.getCurso();
        String modalidadeEnsinoFiltro = filtro.getModalidadeEnsino();
        String formaIngressoFiltro = filtro.getFormaIngresso();
        AlunoVO alunoVO = filtro.getAluno();
        String cpf = filtro.getCpf();
        String numCandidato = filtro.getNumCandidato();
        String numInscricao = filtro.getNumInscricao();
        Boolean apenasPendentes = filtro.getApenasPendentes();
        Boolean poloParceiro = filtro.getPoloParceiro();
        List<Long> situacaoIds = filtro.getSituacoesIds();
        Boolean situacaoAluno = filtro.getSituacaoAluno();
        List<String> periodosIngresso = filtro.getPeriodosIngresso();
        List<Long> tiposProcessoIds = filtro.getTiposProcessoIds();
        Boolean regionalVazia = filtro.getRegionalVazia();
        List<String> todasRegionais = filtro.getTodasRegionais();
        List<StatusProcesso> statusProcessos = filtro.getStatusProcessos();
        Date inicioDataFinalizacaoAnalise = filtro.getInicioDataFinalizacaoAnalise();

        hql.append(" select ");

        // 0 - cpf
        hql.append(" COALESCE(a.cpf, ''), ");

        // 1 - nome aluno
        hql.append(" a.nome, ");

        // 2 - processo Id
        hql.append(" p2.id, ");

        // 3 - situacao
        hql.append(" s.nome, ");

        // 4 - origem
        hql.append(" p2.origem, ");

        // 5 - nome documento
        hql.append(" d.nome, ");

        // 6 - status
        hql.append(" d.status, ");

        // 7 - observacao
        hql.append(" ( ");
        hql.append(" 	select pe.observacao ");
        hql.append("	from ").append(Pendencia.class.getName()).append(" pe ");
        hql.append("	where pe.id = (select max(pe2.id) ");
        hql.append("		from ").append(Pendencia.class.getName()).append(" pe2 ");
        hql.append("		where pe2.documento.id = d.id) ");
        hql.append(" ) as observacao, ");

        // 8 - total de imagens
        hql.append(" ( ");
        hql.append(" 	select count(*) ");
        hql.append(" 	from ").append(Imagem.class.getName()).append(" i ");
        hql.append(" 	where i.documento.id = d.id ");
        hql.append("  ) as total_imagens, ");

        // 9 - data de inicio de analise
        hql.append(" ( select pl from ").append(ProcessoLog.class.getName()).append(" pl");
        hql.append("   where pl.id = (select max(pl2.id) from ").append(ProcessoLog.class.getName()).append(" pl2");
        hql.append("                  where  pl2.processo.id = p2.id ");
        hql.append("                    and (pl2.acao = :acaoEnvioAnalise) ");
        hql.append("                    and (pl2.statusProcesso = :acaoStatusProcessoEmAnalise))");
        hql.append(") as inicio_ultima_analise, ");

        // 10 - ultima data de analise
        hql.append(" ( select max(pl.data) from ").append(ProcessoLog.class.getName()).append(" pl");
        hql.append("   where pl.processo.id = p2.id ");
        hql.append("     and  (pl.acao = :acaoAlteracaoSituacao) ");
        hql.append("     and ((pl.statusProcesso = :acaoStatusProcessoPendente)          ");
        hql.append("     or   (pl.statusProcesso = :acaoStatusProcessoConcluido)         ");
        hql.append("     or   (pl.statusProcesso = :acaoStatusProcessoEmAcompanhamento)) ");
        hql.append(") as fim_ultima_analise, ");

        // 11 - vezes em análise
        hql.append(" rg.vezesEmAnalise, ");

        // 12 - pendencia
        hql.append(" (select i2.nome from ").append(Irregularidade.class.getName()).append(" i2 ");
        hql.append("    where i2.id = ").append(" (select max(i.id) from ").append(Irregularidade.class.getName()).append(" i, ").append(Pendencia.class.getName()).append(" pe, ").append(DocumentoLog.class.getName()).append(" dl  ");
        hql.append("        where i.id = pe.irregularidade.id and dl.documento.id = d.id and dl.pendencia.id = pe.id and d.status = :statusPendente)) as irregularidade, ");

        // 13 - documento obrigatório
        hql.append(" d.obrigatorio as obrigatorio, ");

        // 14 - Versão do documento
        hql.append(" d.versaoAtual as versao, ");

        // 15 - Pasta Vermelha
        hql.append(" p2.usaTermo as pasta_vermelha, ");

        // 16 - Tipo Processo
        hql.append(" tp.nome as tipoProcessoNome, ");

        // 17 - Pasta Amarela
        hql.append(" i2.irregularidadePastaAmarela as pasta_amarela, ");

        // 18 - modelo documento
        hql.append(" (select md.descricao from ").append(ModeloDocumento.class.getName()).append(" md where d.modeloDocumento.id = md.id) as modelo_documento, ");

        // 19 - Campos Dinamicos
        hql.append(" (select rg.camposDinamicos from ").append(RelatorioGeral.class.getName()).append(" rg ");
        hql.append("    where rg.processoId = p2.id) as campos_dinamicos, ");

        // 20 - status do processo
        hql.append(" p2.status as status_processo, ");

        // 21 - Origem do Documento
        hql.append(" d.origem as origem_documento ");

        hql.append(" from ").append(Aluno.class.getName()).append(" a ");
        hql.append(" inner join ").append(Processo.class.getName()).append(" p2 on a.id = p2.aluno.id    ");
        hql.append(" inner join ").append(Situacao.class.getName()).append(" s on  p2.situacao.id = s.id ");
        hql.append(" inner join ").append(Documento.class.getName()).append(" d on p2.id = d.processo.id ");
        hql.append(" inner join ").append(TipoProcesso.class.getName()).append(" tp on p2.tipoProcesso.id = tp.id ");
        hql.append(" left outer join ").append(RelatorioGeral.class.getName()).append(" rg on p2.id = rg.processoId ");
        hql.append(" left outer join ").append(DocumentoLog.class.getName()).append(" dl on dl.id = (select max(dl2.id) from ").append(DocumentoLog.class.getName()).append(" dl2 where dl2.documento.id = d.id) ");
        hql.append(" left outer join ").append(Pendencia.class.getName()).append(" pe on pe.id = dl.pendencia.id ");
        hql.append(" left outer join ").append(Irregularidade.class.getName()).append(" i on i.id = pe.irregularidade.id ");
        hql.append(" left outer join ").append(Advertencia.class.getName()).append(" ad on ad.id = d.advertencia.id ");
        hql.append(" left outer join ").append(Irregularidade.class.getName()).append(" i2 on i.id = ad.irregularidade.id ");
        hql.append(" where 1=1 ");

        if (apenasPendentes) {

            hql.append(" and d.status in (:docStatusPendentes) ");
            params.put("docStatusPendentes", Arrays.asList(StatusDocumento.INCLUIDO, StatusDocumento.PENDENTE));
        } else {

            hql.append(" and d.status <> :docStatusExcluido ");
            params.put("docStatusExcluido", StatusDocumento.EXCLUIDO);
        }

        if (situacaoIds != null && !situacaoIds.isEmpty()) {
            hql.append(" and p2.situacao.id in ( -1 ");
            int i = 1;
            for (Long situacaoId : situacaoIds) {
                hql.append(", :situacao").append(i);
                params.put("situacao" + i, situacaoId);
                i++;
            }
            hql.append(" )");
        }

        if (StringUtils.isNotBlank(cpf)) {
            hql.append(" and a.cpf = :cpf ");
            params.put("cpf", cpf);
        }

        if (alunoVO != null) {
            hql.append(" and a.id = :idAluno ");

            Long alunoId = alunoVO.getId();
            params.put("idAluno", alunoId);
        }

        if (StringUtils.isNotBlank(numCandidato)) {
            hql.append(" and p2.id in ");
            hql.append(" ( ");
            hql.append("	select c2.grupo.processo.id ");
            hql.append("	from  ").append(Campo.class.getName()).append(" c2 ");
            hql.append("	where c2.nome = :numCandidatoCampoNome ");
            hql.append("    and c2.grupo.nome = :grupoDadosDoInscrito");
            hql.append("	and upper(c2.valor) like :numCandidatoCampoValor ");
            hql.append(" ) ");
            params.put("numCandidatoCampoValor", numCandidato);
        }

        if (StringUtils.isNotBlank(numInscricao)) {
            hql.append(" and p2.id in ");
            hql.append(" ( ");
            hql.append("	select c2.grupo.processo.id ");
            hql.append("	from  ").append(Campo.class.getName()).append(" c2 ");
            hql.append("	where c2.nome = :numInscricaoCampoNome ");
            hql.append("      and c2.grupo.nome = :grupoDadosDoInscrito");
            hql.append("	and upper(c2.valor) like :numInscricaoCampoValor ");
            hql.append(" ) ");
            params.put("numInscricaoCampoValor", numInscricao);
        }

        if (regionalVazia != null && regionalVazia && isNotEmpty(todasRegionais)) {

            hql.append(" and p2.id in ");
            hql.append(" ( ");
            hql.append("	select c2.grupo.processo.id ");
            hql.append("	from  ").append(Campo.class.getName()).append(" c2 ");
            hql.append("	where c2.nome = :regionalCampoNome ");
            hql.append("    and c2.grupo.nome = :grupoDadosDoInscrito");
            hql.append("    and ( 1=1 ");
            for (int i = 0; i < todasRegionais.size(); i++) {

                hql.append("	and (upper(c2.valor) not like (:regional").append(i).append(")) ");
                params.put("regional" + i, formatarChaveUnicidadeBaseInterna(Collections.singletonList(todasRegionais.get(i))).get(0));
            }
            hql.append(" )) ");
        } else if (isNotEmpty(regionais)) {

            hql.append(" and p2.id in ");
            hql.append(" ( ");
            hql.append("	select c2.grupo.processo.id ");
            hql.append("	from  ").append(Campo.class.getName()).append(" c2 ");
            hql.append("	where c2.nome = :regionalCampoNome ");
            hql.append("    and c2.grupo.nome = :grupoDadosDoInscrito");
            hql.append("	and upper(c2.valor) in (:regionais) ");
            hql.append(" ) ");

            params.put("regionais", formatarChaveUnicidadeBaseInterna(regionais));
        }

        if (isNotEmpty(periodosIngresso)) {

            hql.append(" and p2.id in ");
            hql.append(" ( ");
            hql.append("	select c2.grupo.processo.id ");
            hql.append("	from  ").append(Campo.class.getName()).append(" c2 ");
            hql.append("	where c2.nome = :periodoDeIngressoNome ");
            hql.append("    and c2.grupo.nome = :grupoDadosDoInscrito");
            hql.append("	and upper(c2.valor) in (:periodoDeIngressoValor) ");
            hql.append(" ) ");

            params.put("periodoDeIngressoValor", periodosIngresso);
        }

        if (campus != null && !campus.isEmpty()) {

            campus = formatarChaveUnicidadeBaseInterna(campus);

            hql.append(" and ( 1<>1 ");
            int i = 1;
            for (String campus2 : campus) {
                hql.append(" or p2.id in ");
                hql.append(" ( ");
                hql.append("	select c2.grupo.processo.id ");
                hql.append("	from  ").append(Campo.class.getName()).append(" c2 ");
                hql.append("	where c2.nome = :campusCampoNome ");
                hql.append("    and c2.grupo.nome = :grupoDadosDoInscrito");
                hql.append("	and upper(c2.valor) like :campusCampoValor").append(i);
                hql.append(" ) ");

                params.put("campusCampoValor" + i, campus2);

                i++;
            }
            hql.append(" ) ");
        }

        if (poloParceiro != null && poloParceiro.equals(Boolean.TRUE)) {
            hql.append(" and ( 1<>1 ");
            hql.append(" or p2.id in ");
            hql.append(" ( ");
            hql.append("	select c2.grupo.processo.id ");
            hql.append("	from  ").append(Campo.class.getName()).append(" c2 ");
            hql.append(" 	where c2.nome = :campusCampoNome and upper(c2.valor) in ( ");
            hql.append(" 		select br.chaveUnicidade ");
            hql.append(" 		from ").append(BaseRegistro.class.getName()).append(" br, ").append(BaseInterna.class.getName()).append(" bi ");
            hql.append(" 		where br.baseInterna.id = bi.id and br.baseInterna.id = :campusBaseInternaId and ");
            hql.append(" 		br.id in (  ");
            hql.append(" 			select brv.baseRegistro.id from ").append(BaseRegistroValor.class.getName()).append(" brv ");
            hql.append(" 			where brv.nome = :poloParceiroCampoValor and brv.valor = 'Sim'  ");
            hql.append(" 		) ");
            hql.append(" 	) ");
            hql.append(" ) ");

            params.put("poloParceiroCampoValor", TipoCampo.POLO_PARCEIRO);

            hql.append(" ) ");
        }

        if (cursos != null && !cursos.isEmpty()) {

            cursos = formatarChaveUnicidadeBaseInterna(cursos);

            hql.append(" and ( 1<>1 ");
            int i = 1;
            for (String curso : cursos) {
                hql.append(" or p2.id in ");
                hql.append(" ( ");
                hql.append("	select c2.grupo.processo.id ");
                hql.append("	from  ").append(Campo.class.getName()).append(" c2 ");
                hql.append("	where c2.nome = :cursoCampoNome ");
                hql.append("    and c2.grupo.nome = :grupoDadosDoInscrito");
                hql.append("	and upper(c2.valor) like :cursoCampoValor").append(i);
                hql.append(" ) ");

                params.put("cursoCampoValor" + i, curso);

                i++;
            }
            hql.append(" ) ");
        }

        if (isNotBlank(modalidadeEnsinoFiltro)) {
            hql.append(" and p2.id in ");
            hql.append(" ( ");
            hql.append("	select c2.grupo.processo.id ");
            hql.append("	from  ").append(Campo.class.getName()).append(" c2 ");
            hql.append("	where c2.nome = :modalidadeEnsinoCampoNome ");
            hql.append("    and c2.grupo.nome = :grupoDadosDoInscrito");
            hql.append("	and upper(c2.valor) like :modalidadeEnsinoCampoValor ");
            hql.append(" ) ");

            params.put("modalidadeEnsinoCampoValor", modalidadeEnsinoFiltro);
        }

        if (isNotBlank(formaIngressoFiltro)) {
            hql.append(" and p2.id in ");
            hql.append(" ( ");
            hql.append("	select c2.grupo.processo.id ");
            hql.append("	from  ").append(Campo.class.getName()).append(" c2 ");
            hql.append("	where c2.nome = :formaIngressoCampoNome ");
            hql.append("    and c2.grupo.nome = :grupoDadosDoInscrito");
            hql.append("	and upper(c2.valor) like :formaIngressoCampoValor ");
            hql.append(" ) ");

            params.put("formaIngressoCampoValor", formaIngressoFiltro);
        }

        if (situacaoAluno != null && !situacaoAluno) {
            hql.append(" and a.ativa = false ");
        }

        if (isNotEmpty(tiposProcessoIds)) {
            hql.append(" and p2.tipoProcesso.id in (:tiposProcesso) ");
            params.put("tiposProcesso", tiposProcessoIds);
        }

        if (isNotEmpty(statusProcessos)) {
            hql.append(" and p2.status in (:statusProcesso) ");
            params.put("statusProcesso", statusProcessos);
        }

        if (inicioDataFinalizacaoAnalise != null) {
            hql.append(" and p2.dataFinalizacaoAnalise > :inicioDataFinalizacaoAnalise ");
            params.put("inicioDataFinalizacaoAnalise", inicioDataFinalizacaoAnalise);
        }

        hql.append(" order by a.cpf ");
        hql.append(", p2.id ");
        hql.append(", d.nome ");

        params.put("regionalCampoNome", CampoMap.CampoEnum.REGIONAL.getNome());
        params.put("periodoDeIngressoNome", CampoMap.CampoEnum.PERIODO_DE_INGRESSO.getNome());
        params.put("instituicaoCampoNome", CampoMap.CampoEnum.INSTITUICAO.getNome());
        params.put("campusCampoNome", CampoMap.CampoEnum.CAMPUS.getNome());
        params.put("poloParceiroCampoNome", TipoCampo.POLO_PARCEIRO);
        params.put("cursoCampoNome", CampoMap.CampoEnum.CURSO.getNome());
        params.put("formaIngressoCampoNome", CampoMap.CampoEnum.FORMA_DE_INGRESSO.getNome());
        params.put("situacaoAlunoCampoNome", CampoMap.CampoEnum.SITUACAO_ALUNO.getNome());
        params.put("modalidadeEnsinoCampoNome", CampoMap.CampoEnum.MODALIDADE_ENSINO.getNome());
        params.put("numeroInscricaoCampoNome", CampoMap.CampoEnum.NUM_INSCRICAO.getNome());
        params.put("numeroCandidatoCampoNome", CampoMap.CampoEnum.NUM_CANDIDATO.getNome());
        params.put("matriculaCampoNome", CampoMap.CampoEnum.MATRICULA.getNome());
        params.put("tipoCursoCampoNome", CampoMap.CampoEnum.TIPO_CURSO.getNome());
        params.put("periodoIngressoCampoNome", CampoMap.CampoEnum.PERIODO_DE_INGRESSO.getNome());
        params.put("turnoCampoNome", CampoMap.CampoEnum.TURNO.getNome());
        params.put("regionalBaseInternaId", BaseInterna.REGIONAL_ID);
        params.put("instituicaoBaseInternaId", BaseInterna.INSTITUICAO_ID);
        params.put("campusBaseInternaId", BaseInterna.CAMPUS_ID);
        params.put("cursoBaseInternaId", BaseInterna.CURSO_ID);
        params.put("tipoCursoBaseInternaId", BaseInterna.TIPO_CURSO_ID);
        params.put("formaIngressoBaseInternaId", BaseInterna.FORMA_INGRESSO_ID);
        params.put("modalidadeEnsinoBaseInternaId", BaseInterna.MODALIDADE_ENSINO_ID);
        params.put("emailAluno", CampoMap.CampoEnum.EMAIL.getNome());
        params.put("telefoneAluno", CampoMap.CampoEnum.TELEFONE.getNome());

        params.put("acaoEnvioAnalise", AcaoProcesso.ENVIO_ANALISE);
        params.put("acaoAlteracaoSituacao", AcaoProcesso.ALTERACAO_SITUACAO);
        params.put("acaoStatusProcessoEmAnalise", StatusProcesso.EM_ANALISE);
        params.put("acaoStatusProcessoPendente", StatusProcesso.PENDENTE);
        params.put("acaoStatusProcessoConcluido", StatusProcesso.CONCLUIDO);
        params.put("acaoStatusProcessoEmAcompanhamento", StatusProcesso.EM_ACOMPANHAMENTO);
        params.put("grupoDadosDoInscrito", CampoMap.GrupoEnum.DADOS_DO_INSCRITO.getNome());

        params.put("statusPendente", StatusDocumento.PENDENTE);

        return hql;
    }

    public static StringBuilder createHqlRelatorioSisFiesSisProuni(Map<String, Object> params, RelatorioPendenciaDocumentoFiltro filtro) {

        StringBuilder hql = new StringBuilder();
        List<Long> tiposProcessoIds = filtro.getTiposProcessoIds();

        hql.append(" select ");

        // 0 - Processo ID
        hql.append("        p.id as processo_id ");

        // 1 - Data Criação do Processo
        hql.append(",        p.dataCriacao as data_criacao ");

        // 2 - Documento ID
        hql.append(",        d.id as documento_id ");

        // 3 - Situação
        hql.append(",      (select s2.nome from ").append(Situacao.class.getName()).append(" s2 where s2.id = p.situacao.id) as situacao ");

        // 4 - Aluno Nome
        hql.append(",        a.nome as aluno ");

        // 5 - CPF
        hql.append(",        a.cpf ");

        // 6 - Tipo de Processo
        hql.append(",        (select t.nome from ").append(TipoProcesso.class.getName()).append(" t where id = p.tipoProcesso.id) as tipo_processo ");

        // 7 - Documento Nome
        hql.append(",        d.nome as documento ");

        // 8 - Numero Membro
        hql.append(",           d.nome as numero_membro ");

        // 9 - Status Documento
        hql.append(",        d.status as status_documento ");

        // 10 - Irregularidade
        hql.append(",           (select i2.nome from ").append(Irregularidade.class.getName()).append(" i2 ");
        hql.append("                where i2.id = ").append(" (select max(i.id) from ").append(Irregularidade.class.getName()).append(" i, ").append(Pendencia.class.getName()).append(" pe, ").append(DocumentoLog.class.getName()).append(" dl  ");
        hql.append("                    where i.id = pe.irregularidade.id and dl.documento.id = d.id and dl.pendencia.id = pe.id and d.status = :statusPendente)) as irregularidade ");

        // 11 - Observação Irregularidade
        hql.append(",           (select pe2.observacao from ").append(Pendencia.class.getName()).append(" pe2 ");
        hql.append("                where pe2.id = (select max(pe.id) from ").append(Pendencia.class.getName()).append(" pe ");
        hql.append("                                    where pe.documento.id = d.id and d.status = :statusPendente)) as observacao ");

        // 12 - Nome Analista
        hql.append(",           (select pl2.usuario.nome from ").append(ProcessoLog.class.getName()).append(" pl2 where pl2.id = ( ");
        hql.append("                select max(pl.id) from ").append(ProcessoLog.class.getName()).append(" pl ");
        hql.append("                    where pl.usuario is not null and pl.processo.id = p.id and pl.observacao like 'De%')) as analista ");

        // 13 - Login Analista
        hql.append(",           (select pl2.usuario.login from ").append(ProcessoLog.class.getName()).append(" pl2 where pl2.id = ( ");
        hql.append("                select max(pl.id) from ").append(ProcessoLog.class.getName()).append(" pl ");
        hql.append("                    where pl.usuario is not null and pl.processo.id = p.id and pl.observacao like 'De%')) as analista_login ");

        // 14 - Situação Anterior
        hql.append(",       s.nome as situacao_anterior ");

        // 15 - Data Envio Analise
        hql.append(",        p.dataEnvioAnalise as data_envio_analise ");

        // 16 - Data Finalização Analise
        hql.append(",        p.dataFinalizacaoAnalise as data_finalizacao_analise ");

        // 17 - Numero de Paginas
        hql.append(",        (select count(*) from ").append(Imagem.class.getName()).append(" i where i.documento.id = d.id) as n_de_paginas ");

        // 18 - Status do Processo
        hql.append(",           p.status as status_processo ");

        // 19 - Usa termo
        hql.append(",            p.usaTermo as usa_termo ");

        // 20 - Campos Dinamicos
        hql.append(",            (select rg.camposDinamicos from ").append(RelatorioGeral.class.getName()).append(" rg ");
        hql.append("                where rg.processoId = p.id) as campos_dinamicos");

        hql.append("    from ").append(Documento.class.getName()).append(" d, ").append(Processo.class.getName()).append(" p ");
        hql.append("    left outer join ").append(Aluno.class.getName()).append(" a on (a.id = p.aluno.id) ");
        hql.append("    left outer join ").append(Situacao.class.getName()).append(" s on ( ");
        hql.append("            s.id = ( ");
        hql.append("                case when p.situacao.id not in (:situacoesSisfiesSisprouniTransformado) then ");
        hql.append("                    p.situacao.id ");
        hql.append("                else (select pl.situacaoAnterior.id from ").append(ProcessoLog.class.getName()).append(" pl where pl.id = ( ");
        hql.append("                        select max(pl.id) from ").append(ProcessoLog.class.getName()).append(" pl ");
        hql.append("                            where p.id = pl.processo.id and p.situacao.id = pl.situacao.id and pl.situacaoAnterior.id not in (:situacoesSisfiesSisprouniTransformado) ");
        hql.append("                )) end )) ");
        hql.append("    where 1=1 ");

        if (isNotEmpty(tiposProcessoIds)) {
            hql.append(" and p.tipoProcesso.id in (:tiposProcesso) ");
            params.put("tiposProcesso", tiposProcessoIds);
        }

        hql.append("    and d.status not in (:statusExcluido) ");
        hql.append("    and p.id = d.processo.id ");
        hql.append("    and (d.obrigatorio = true or d.status is not :statusIncluido) ");
        hql.append("    and d.nome not like :nomeDocumentoOutros ");
        hql.append("    order by a.cpf, p.id, d.nome ");

        params.put("statusIncluido", StatusDocumento.INCLUIDO);
        params.put("statusExcluido", StatusDocumento.EXCLUIDO);
        params.put("statusPendente", StatusDocumento.PENDENTE);
        params.put("nomeDocumentoOutros", Documento.NOME_OUTROS);

        params.put("statusProcessoPendente", StatusProcesso.PENDENTE);
        params.put("statusProcessoConcluido", StatusProcesso.CONCLUIDO);

        params.put("grupoDadosDoInscrito", CampoMap.GrupoEnum.DADOS_DO_INSCRITO.getNome());
        params.put("grupoDadosDeImportacao", CampoMap.GrupoEnum.DADOS_DE_IMPORTACAO.getNome());

        params.put("regionalBaseInternaId", BaseInterna.REGIONAL_ID);
        params.put("regionalCampoNome", CampoMap.CampoEnum.REGIONAL.getNome());
        params.put("instituicaoBaseInternaId", BaseInterna.INSTITUICAO_ID);
        params.put("instituicaoCampoNome", CampoMap.CampoEnum.INSTITUICAO.getNome());
        params.put("campusBaseInternaId", BaseInterna.CAMPUS_ID);
        params.put("campusCampoNome", CampoMap.CampoEnum.CAMPUS.getNome());
        params.put("tipoCursoBaseInternaId", BaseInterna.TIPO_CURSO_ID);
        params.put("tipoCursoCampoNome", CampoMap.CampoEnum.TIPO_CURSO.getNome());
        params.put("cursoBaseInternaId", BaseInterna.CURSO_ID);
        params.put("cursoCampoNome", CampoMap.CampoEnum.CURSO.getNome());
        params.put("formaIngressoBaseInternaId", BaseInterna.FORMA_INGRESSO_ID);
        params.put("formaIngressoCampoNome", CampoMap.CampoEnum.FORMA_DE_INGRESSO.getNome());
        params.put("numeroInscricaoCampoNome", CampoMap.CampoEnum.NUM_INSCRICAO.getNome());
        params.put("numeroCandidatoCampoNome", CampoMap.CampoEnum.NUM_CANDIDATO.getNome());
        params.put("matriculaCampoNome", CampoMap.CampoEnum.MATRICULA.getNome());
        params.put("periodoIngressoCampoNome", CampoMap.CampoEnum.PERIODO_DE_INGRESSO.getNome());
        params.put("periodoDeIngressoNome", CampoMap.CampoEnum.PERIODO_DE_INGRESSO.getNome());
        params.put("situacoesSisfiesSisprouniTransformado", Situacao.SISFIES_SISPROUNI_TRANSFORMADO_ID);

        return hql;
    }

}
