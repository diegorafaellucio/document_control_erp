package net.wasys.getdoc.rest.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.wasys.getdoc.domain.entity.SessaoHttpRequest;
import net.wasys.getdoc.rest.exception.DadosObrigatorioRequestException;
import net.wasys.getdoc.rest.exception.HTTP401Exception;
import net.wasys.getdoc.rest.exception.TipoCampoRestException;
import net.wasys.getdoc.rest.exception.TipoProcessoRestException;
import net.wasys.getdoc.rest.request.vo.RequestCadastroTipoDocumento;
import net.wasys.getdoc.rest.request.vo.RequestCadastroTipoProcesso;
import net.wasys.getdoc.rest.response.vo.*;
import net.wasys.getdoc.rest.service.TipoDocumentoServiceRest;
import net.wasys.getdoc.rest.service.TipoProcessoServiceRest;
import net.wasys.util.ddd.MessageKeyException;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@CrossOrigin
@Controller
@RequestMapping("/tipo-processo/v1")
@Api(tags = "/tipo-processo", description = "Serviços relacionados ao TipoProcesso.")
public class RestTipoProcessoV1 extends SuperController {

    @Autowired
    private TipoProcessoServiceRest tipoProcessoServiceRest;
    @Autowired
    private TipoDocumentoServiceRest tipoDocumentoServiceRest;

    @RequestMapping(
            path = "/combo",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta as informações resumidas para popular o combo de tipos de processo.",
            response = TipoProcessoResponse.class
    )
    public ResponseEntity getInformacoesCombo(HttpServletRequest request) throws Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<TipoProcessoResponse> tipoProcessoResponseList = tipoProcessoServiceRest.getInformacaoResumida(sessaoHttpRequest.getUsuario());
        return new ResponseEntity(CollectionUtils.isNotEmpty(tipoProcessoResponseList) ? tipoProcessoResponseList : null, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/lista/{min}/{max}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta tipos de processo para popular a lista de tipos.",
            response = ListaTipoProcessoResponse.class
    )
    public ResponseEntity getListaTiposProcesso(HttpServletRequest request, @PathVariable int min, @PathVariable int max) throws Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        ListaTipoProcessoResponse listaTipoProcessoResponse = tipoProcessoServiceRest.getLista(sessaoHttpRequest.getUsuario(), min, max);
        return new ResponseEntity(listaTipoProcessoResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/cadastrar",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Cadastra um novo TipoProcesso.",
            response = TipoProcessoResponse.class
    )
    public ResponseEntity cadastrar(HttpServletRequest request, @RequestBody RequestCadastroTipoProcesso requestCadastroTipoProcesso) throws MessageKeyException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        TipoProcessoResponse tipoProcessoResponse = tipoProcessoServiceRest.cadastrar(sessaoHttpRequest.getUsuario(), requestCadastroTipoProcesso);
        return new ResponseEntity(tipoProcessoResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/detalhar/{id}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Obtém os dados do TipoProcesso.",
            response = TipoProcessoResponse.class
    )
    public ResponseEntity detalhar(HttpServletRequest request, @PathVariable Long id) throws MessageKeyException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        TipoProcessoResponse tipoProcessoResponse = tipoProcessoServiceRest.detalhar(sessaoHttpRequest.getUsuario(), id);
        return new ResponseEntity(tipoProcessoResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/editar/{id}",
            method = RequestMethod.PUT,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Atualiza um TipoProcesso.",
            response = TipoProcessoResponse.class
    )
    public ResponseEntity editar(HttpServletRequest request, @PathVariable Long id, @RequestBody RequestCadastroTipoProcesso requestCadastroTipoProcesso) throws MessageKeyException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        TipoProcessoResponse tipoProcessoResponse = tipoProcessoServiceRest.editar(sessaoHttpRequest.getUsuario(), id, requestCadastroTipoProcesso);
        return new ResponseEntity(tipoProcessoResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/excluir/{id}",
            method = RequestMethod.DELETE,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Exclui um TipoProcesso.",
            response = TipoProcessoResponse.class
    )
    public ResponseEntity excluir(HttpServletRequest request, @PathVariable Long id) throws MessageKeyException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        boolean excluir = tipoProcessoServiceRest.excluir(sessaoHttpRequest.getUsuario(), id);
        return new ResponseEntity(null, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/{tipoProcessoId}/adicionar-documento",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Adiciona um novo grupo de campo no tipo de processo.",
            response = TipoCampoGrupoResponse.class
    )
    public ResponseEntity adicionarGrupoCampo(HttpServletRequest request, @PathVariable Long tipoProcessoId, @RequestBody RequestCadastroTipoDocumento requestCadastroTipoDocumento) throws HTTP401Exception, TipoProcessoRestException, TipoCampoRestException, DadosObrigatorioRequestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        TipoDocumentoResponse tipoDocumentoResponse = tipoDocumentoServiceRest.adicionarTipoDocumento(sessaoHttpRequest.getUsuario(), tipoProcessoId, requestCadastroTipoDocumento);
        return new ResponseEntity(tipoDocumentoResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/{tipoProcessoId}/tipos-documento/{min}/{max}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta os tipos de documento.",
            response = ListaTipoDocumentoResponse.class
    )
    public ResponseEntity tiposDocumento(HttpServletRequest request, @PathVariable Long tipoProcessoId, @PathVariable int min, @PathVariable int max) throws HTTP401Exception, TipoProcessoRestException, TipoCampoRestException, DadosObrigatorioRequestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        ListaTipoDocumentoResponse lista = tipoDocumentoServiceRest.getTiposDocumento(sessaoHttpRequest.getUsuario(), tipoProcessoId, min, max);
        return new ResponseEntity(lista, HttpStatus.OK);
    }
}