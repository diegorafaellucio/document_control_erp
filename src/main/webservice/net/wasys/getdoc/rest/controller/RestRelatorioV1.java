package net.wasys.getdoc.rest.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.wasys.getdoc.domain.entity.SessaoHttpRequest;
import net.wasys.getdoc.domain.service.ParametroService;
import net.wasys.getdoc.rest.exception.HTTP401Exception;
import net.wasys.getdoc.rest.request.vo.RequestPesquisaRelatorioAcompanhamento;
import net.wasys.getdoc.rest.request.vo.RequestPesquisaRelatorioAcompanhamentoAberto;
import net.wasys.getdoc.rest.request.vo.RequestRelatorioGeral;
import net.wasys.getdoc.rest.request.vo.RequestRelatorioProdutividade;
import net.wasys.getdoc.rest.response.vo.*;
import net.wasys.getdoc.rest.service.ParametroServiceRest;
import net.wasys.getdoc.rest.service.RelatorioServiceRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@CrossOrigin
@Controller
@RequestMapping("/relatorio/v1")
@Api(tags = "/relatorio", description = "Serviços relacionados aos Relatórios.")
public class RestRelatorioV1 extends SuperController {

    @Autowired
    private RelatorioServiceRest relatorioServiceRest;

    @RequestMapping(
            path = "/acompanhamento",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Relatório de acompanhamento.",
            response = RelatorioAcompanhamentoResponse.class
    )
    public ResponseEntity acompanhamento(HttpServletRequest request, @RequestBody RequestPesquisaRelatorioAcompanhamento requestPesquisaRelatorioAcompanhamento) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<RelatorioAcompanhamentoResponse> relatorioAcompanhamento = relatorioServiceRest.getRelatorioAcompanhamento(sessaoHttpRequest.getUsuario(), requestPesquisaRelatorioAcompanhamento);
        return new ResponseEntity(relatorioAcompanhamento, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/acompanhamento/tipos-processo",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Relatório de acompanhamento.",
            response = TipoProcessoResponse.class
    )
    public ResponseEntity acompanhamentoTiposProcesso(HttpServletRequest request) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<TipoProcessoResponse> tiposProcesso = relatorioServiceRest.getTiposProcesso(sessaoHttpRequest.getUsuario());
        return new ResponseEntity(tiposProcesso, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/acompanhamento-aberto",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Relatório de acompanhamento em aberto.",
            response = RelatorioAcompanhamentoAbertoResponse.class
    )
    public ResponseEntity acompanhamentoEmAberto(HttpServletRequest request, @RequestBody RequestPesquisaRelatorioAcompanhamentoAberto requestPesquisaRelatorioAcompanhamentoAberto) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<RelatorioAcompanhamentoAbertoResponse> relatorioAcompanhamentoAberto = relatorioServiceRest.getRelatorioAcompanhamentoEmAberto(sessaoHttpRequest.getUsuario(), requestPesquisaRelatorioAcompanhamentoAberto);
        return new ResponseEntity(relatorioAcompanhamentoAberto, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/geral/{min}/{max}",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Relatório geral.",
            response = RelatorioGeralResponse.class
    )
    public ResponseEntity geral(HttpServletRequest request, @PathVariable int min, @PathVariable int max, @RequestBody RequestRelatorioGeral requestRelatorioGeral) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<RelatorioGeralResponse> relatorioGeral = relatorioServiceRest.getRelatorioGeral(sessaoHttpRequest.getUsuario(), min, max, requestRelatorioGeral);
        return new ResponseEntity(relatorioGeral, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/produtividade",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Relatório de produtividade.",
            response = RelatorioProdutividadeResponse.class
    )
    public ResponseEntity produtividade(HttpServletRequest request, @RequestBody RequestRelatorioProdutividade requestRelatorioProdutividade) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<RelatorioProdutividadeResponse> relatorioProdutividade = relatorioServiceRest.getRelatorioProdutividade(sessaoHttpRequest.getUsuario(), requestRelatorioProdutividade);
        return new ResponseEntity(relatorioProdutividade, HttpStatus.OK);
    }

}