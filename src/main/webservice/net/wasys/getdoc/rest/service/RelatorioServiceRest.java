package net.wasys.getdoc.rest.service;

import net.wasys.getdoc.bean.RelatorioAcompanhamentoBean.PadraoLinha;
import net.wasys.getdoc.domain.entity.RelatorioGeral;
import net.wasys.getdoc.domain.entity.TipoProcesso;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.enumeration.PermissaoTP;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.domain.service.RelatorioGeralService;
import net.wasys.getdoc.domain.service.RelatorioProdutividadeService;
import net.wasys.getdoc.domain.service.RelatoriosService;
import net.wasys.getdoc.domain.service.TipoProcessoService;
import net.wasys.getdoc.domain.vo.RelatorioAcompanhamentoVO;
import net.wasys.getdoc.domain.vo.RelatorioProdutividadeVO;
import net.wasys.getdoc.domain.vo.filtro.ProcessoFiltro;
import net.wasys.getdoc.domain.vo.filtro.RelatorioGeralFiltro;
import net.wasys.getdoc.domain.vo.filtro.RelatorioProdutividadeFiltro;
import net.wasys.getdoc.rest.exception.ProcessoRestException;
import net.wasys.getdoc.rest.request.vo.RequestPesquisaRelatorioAcompanhamento;
import net.wasys.getdoc.rest.request.vo.RequestPesquisaRelatorioAcompanhamentoAberto;
import net.wasys.getdoc.rest.request.vo.RequestRelatorioGeral;
import net.wasys.getdoc.rest.request.vo.RequestRelatorioProdutividade;
import net.wasys.getdoc.rest.response.vo.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Novo service criado para centralizar as operaçõs que hoje são feitas no Bean JSF.
 */
@Service
public class RelatorioServiceRest extends SuperServiceRest {

    @Autowired
    private RelatoriosService relatoriosService;
    @Autowired
    private RelatorioGeralService relatorioGeralService;
    @Autowired
    private TipoProcessoService tipoProcessoService;
    @Autowired
    private RelatorioProdutividadeService relatorioProdutividadeService;

    public List<RelatorioAcompanhamentoResponse> getRelatorioAcompanhamento(Usuario usuario, RequestPesquisaRelatorioAcompanhamento requestPesquisaRelatorioAcompanhamento) {

        ProcessoFiltro filtro = new ProcessoFiltro();
        PadraoLinha padraoLinha = PadraoLinha.SITUACOES;
        if(requestPesquisaRelatorioAcompanhamento.getPadraoLinha() != null){
            padraoLinha = requestPesquisaRelatorioAcompanhamento.getPadraoLinha();
        }

        List<RelatorioAcompanhamentoVO> list = null;

        switch (padraoLinha){
            case STATUS:{
                list = relatoriosService.getRelatorioEmAbertoStatus(filtro, false);
                break;
            }
            case ANALISTAS:{
                list = relatoriosService.getRelatorioEmAbertoAnalista(filtro);
                break;
            }
            case MOTIVOS:{
                list = relatoriosService.getRelatorioEmAbertoMotivo(filtro);
                break;
            }
            case AREAS: {
                list = relatoriosService.getRelatorioEmAbertoArea(filtro, usuario);
                break;
            }
            case SITUACOES: {
                list = relatoriosService.getRelatorioEmAbertoSituacao(filtro, true, false);
                break;
            }
        }

        if(CollectionUtils.isEmpty(list)){
            return null;
        }

        List<RelatorioAcompanhamentoResponse> listaRelAcompanhamento = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(list)){
            list.forEach(relatorioAcompanhamentoVO -> {
                listaRelAcompanhamento.add(new RelatorioAcompanhamentoResponse(relatorioAcompanhamentoVO));
            });
        }

        return listaRelAcompanhamento;
    }

    public List<TipoProcessoResponse> getTiposProcesso(Usuario usuario) {
        RoleGD roleGD = usuario.getRoleGD();
        List<PermissaoTP> permissoes = PermissaoTP.getPermissoes(roleGD);
        List<TipoProcesso> tiposProcessos = tipoProcessoService.findAtivos(permissoes);

        List<TipoProcessoResponse> list = new ArrayList<>();
        tiposProcessos.forEach(tipoProcesso -> {

            TipoProcessoResponse tpr = new TipoProcessoResponse();
            tpr.setId(tipoProcesso.getId());
            tpr.setNome(tipoProcesso.getNome());
            list.add(tpr);
        });
        return list;
    }

    public List<RelatorioAcompanhamentoAbertoResponse> getRelatorioAcompanhamentoEmAberto(Usuario usuario, RequestPesquisaRelatorioAcompanhamentoAberto requestPesquisaRelatorioAcompanhamentoAberto) {
        ProcessoFiltro filtro = new ProcessoFiltro();

        if(CollectionUtils.isNotEmpty(requestPesquisaRelatorioAcompanhamentoAberto.getTipoProcesso())){
            List<TipoProcesso> tiposProcesso = tipoProcessoService.findByIds(requestPesquisaRelatorioAcompanhamentoAberto.getTipoProcesso());
            if(CollectionUtils.isEmpty(tiposProcesso)){
                throw new ProcessoRestException("processo.pesquisa.tipoprocesso.nao.localizado");
            }
            filtro.setTiposProcesso(tiposProcesso);
        }

        List<RelatorioAcompanhamentoVO> list = relatoriosService.getRelatorioEmAbertoStatus(filtro, false);
        if(CollectionUtils.isEmpty(list)){
            return null;
        }

        List<RelatorioAcompanhamentoAbertoResponse> listaRelAcompanhamento = new ArrayList<>();
        if(CollectionUtils.isNotEmpty(list)){
            list.forEach(relatorioAcompanhamentoVO -> {
                listaRelAcompanhamento.add(new RelatorioAcompanhamentoAbertoResponse(relatorioAcompanhamentoVO));
            });
        }
        return listaRelAcompanhamento;
    }

    public List<RelatorioGeralResponse> getRelatorioGeral(Usuario usuario, int min, int max, RequestRelatorioGeral requestRelatorioGeral) {
        RelatorioGeralFiltro filtro = new RelatorioGeralFiltro();
        filtro.setTipo((requestRelatorioGeral.getTipo() != null) ? requestRelatorioGeral.getTipo() : RelatorioGeralFiltro.Tipo.PROCESSOS);
        filtro.setDataFim(requestRelatorioGeral.getDataFim());
        filtro.setDataInicio(requestRelatorioGeral.getDataInicio());

        List<RelatorioGeral> byFiltro = relatorioGeralService.findByFiltro(filtro, min, max);
        if(CollectionUtils.isEmpty(byFiltro)){
            return null;
        }

        List<RelatorioGeralResponse> list = new ArrayList<>();
        byFiltro.forEach(relatorioGeral -> {
            list.add(new RelatorioGeralResponse(relatorioGeral));
        });
        return list;
    }

    public List<RelatorioProdutividadeResponse> getRelatorioProdutividade(Usuario usuario, RequestRelatorioProdutividade requestRelatorioProdutividade) {
        RelatorioProdutividadeFiltro filtro = new RelatorioProdutividadeFiltro();
        filtro.setTipo((requestRelatorioProdutividade.getTipo() != null) ? requestRelatorioProdutividade.getTipo() : RelatorioProdutividadeFiltro.Tipo.VISUALIZACAO_POR_ANALISTA);
        filtro.setDataFim(requestRelatorioProdutividade.getDataFim());
        filtro.setDataInicio(requestRelatorioProdutividade.getDataInicio());

        List<RelatorioProdutividadeVO> relatorioProdutividade = relatorioProdutividadeService.getRelatorioProdutividade(filtro);
        if(CollectionUtils.isEmpty(relatorioProdutividade)){
            return null;
        }

        List<RelatorioProdutividadeResponse> list = new ArrayList<>();
        relatorioProdutividade.forEach(rpvo -> {
            list.add(new RelatorioProdutividadeResponse(rpvo));
        });
        return list;
    }
}