package net.wasys.getdoc.rest.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.wasys.getdoc.domain.entity.SessaoHttpRequest;
import net.wasys.getdoc.rest.exception.HTTP401Exception;
import net.wasys.getdoc.rest.exception.SituacaoRestException;
import net.wasys.getdoc.rest.request.vo.RequestCadastroSituacao;
import net.wasys.getdoc.rest.response.vo.DetalhesSituacaoResponse;
import net.wasys.getdoc.rest.response.vo.SituacaoListaResponse;
import net.wasys.getdoc.rest.response.vo.SituacaoResponse;
import net.wasys.getdoc.rest.service.SituacaoServiceRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 *
 */
@CrossOrigin
@Controller
@RequestMapping("/situacao/v1")
@Api(tags = "/situacao", description = "Serviços relacionados a situação.")
public class RestSituacaoV1 extends SuperController {

    @Autowired
    private SituacaoServiceRest situacaoServiceRest;

    @RequestMapping(
            path = "/ativas",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta as situações ATIVAS.",
            response = SituacaoResponse.class
    )
    public ResponseEntity ativas(HttpServletRequest request) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<SituacaoResponse> ativas = situacaoServiceRest.findAtivas(sessaoHttpRequest.getUsuario());
        return new ResponseEntity(ativas, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/processo/{tipoProcessoId}/{min}/{max}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta as situações ATIVAS.",
            response = SituacaoListaResponse.class
    )
    public ResponseEntity getSituacoesByTipoProcesso(HttpServletRequest request, @PathVariable Long tipoProcessoId, @PathVariable int min, @PathVariable int max) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<SituacaoListaResponse> ativas = situacaoServiceRest.getSituacoesByTipoProcesso(sessaoHttpRequest.getUsuario(), tipoProcessoId, min, max);
        return new ResponseEntity(ativas, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/detalhar/{id}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Obtém os dados de uma Situação.",
            response = DetalhesSituacaoResponse.class
    )
    public ResponseEntity detalhar(HttpServletRequest request, @PathVariable Long id) throws HTTP401Exception, SituacaoRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        DetalhesSituacaoResponse situacaoResponse = situacaoServiceRest.detalhar(sessaoHttpRequest.getUsuario(), id);
        return new ResponseEntity(situacaoResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/situacoes-tipo-processo/{tipoProcessoId}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta as próximas situações disponíveis.",
            response = SituacaoResponse.class
    )
    public ResponseEntity situacoesByTipoProcesso(HttpServletRequest request, @PathVariable Long tipoProcessoId) throws HTTP401Exception, SituacaoRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<SituacaoResponse> proximas = situacaoServiceRest.situacoesByTipoProcesso(sessaoHttpRequest.getUsuario(), tipoProcessoId);
        return new ResponseEntity(proximas, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/proximas-situacoes/{situacaoAtualId}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta as próximas situações a partir da atual.",
            response = SituacaoResponse.class
    )
    public ResponseEntity getProximasSituacoes(HttpServletRequest request, @PathVariable Long situacaoAtualId) throws HTTP401Exception, SituacaoRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<SituacaoResponse> proximas = situacaoServiceRest.getProximasSituacoes(situacaoAtualId);
        return new ResponseEntity(proximas, HttpStatus.OK);
    }


    @RequestMapping(
            path = "/cadastrar/",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Cadastrar uma nova Situação.",
            response = SituacaoListaResponse.class
    )
    public ResponseEntity cadastrar(HttpServletRequest request, @RequestBody RequestCadastroSituacao requestCadastroSituacao) throws HTTP401Exception, SituacaoRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        SituacaoListaResponse situacaoResponse = situacaoServiceRest.cadastrar(sessaoHttpRequest.getUsuario(), requestCadastroSituacao);
        return new ResponseEntity(situacaoResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/excluir/{id}",
            method = RequestMethod.DELETE,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Excluir uma situação."
    )
    public ResponseEntity excluir(HttpServletRequest request, @PathVariable Long id) throws HTTP401Exception, SituacaoRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        boolean ok  = situacaoServiceRest.excluir(sessaoHttpRequest.getUsuario(), id);
        return new ResponseEntity(null, ok ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(
            path = "/editar/{id}",
            method = RequestMethod.PUT,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Cadastrar uma nova Situação.",
            response = SituacaoListaResponse.class
    )
    public ResponseEntity editar(HttpServletRequest request, @PathVariable Long id, @RequestBody RequestCadastroSituacao requestCadastroSituacao) throws HTTP401Exception, SituacaoRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        SituacaoListaResponse situacaoResponse = situacaoServiceRest.editar(sessaoHttpRequest.getUsuario(), id, requestCadastroSituacao);
        return new ResponseEntity(situacaoResponse, HttpStatus.OK);
    }
}