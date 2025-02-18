package net.wasys.getdoc.rest.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.wasys.getdoc.domain.entity.SessaoHttpRequest;
import net.wasys.getdoc.rest.exception.HTTP401Exception;
import net.wasys.getdoc.rest.exception.ProcessoRestException;
import net.wasys.getdoc.rest.exception.TextoPadraoRestException;
import net.wasys.getdoc.rest.request.vo.RequestCadastroTextoPadrao;
import net.wasys.getdoc.rest.response.vo.TextoPadraoResponse;
import net.wasys.getdoc.rest.service.TextoPadraoServiceRest;
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
@RequestMapping("/texto-padrao/v1")
@Api(tags = "/texto-padrao", description = "Serviços relacionados ao TextoPadrão.")
public class RestTextoPadraoV1 extends SuperController {

    @Autowired
    private TextoPadraoServiceRest textoPadraoServiceRest;

    @RequestMapping(
            path = "/ativos/{processoId}/{min}/{max}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta os TextoPadrão ativos.",
            response = TextoPadraoResponse.class
    )
    public ResponseEntity getAtivosByProcesso(HttpServletRequest request, @PathVariable Long processoId, @PathVariable int min, @PathVariable int max) throws HTTP401Exception, ProcessoRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<TextoPadraoResponse> list = textoPadraoServiceRest.getAtivosByProcesso(sessaoHttpRequest.getUsuario(), processoId, min, max);
        return new ResponseEntity(list, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/{processoId}/{min}/{max}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta os TextoPadrão ativos.",
            response = TextoPadraoResponse.class
    )
    public ResponseEntity getAllByProcesso(HttpServletRequest request, @PathVariable Long processoId, @PathVariable int min, @PathVariable int max) throws HTTP401Exception, ProcessoRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<TextoPadraoResponse> list = textoPadraoServiceRest.getAllByProcesso(sessaoHttpRequest.getUsuario(), processoId, min, max);
        return new ResponseEntity(list, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/{min}/{max}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta todos os TextoPadrão, independente de processo.",
            response = TextoPadraoResponse.class
    )
    public ResponseEntity getAll(HttpServletRequest request, @PathVariable int min, @PathVariable int max) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<TextoPadraoResponse> list = textoPadraoServiceRest.getAll(sessaoHttpRequest.getUsuario(), min, max);
        return new ResponseEntity(list, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/detalhar/{id}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Detalhar um TextoPadrão.",
            response = TextoPadraoResponse.class
    )
    public ResponseEntity detalhar(HttpServletRequest request, @PathVariable Long id) throws HTTP401Exception, TextoPadraoRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        TextoPadraoResponse textoPadraoResponse = textoPadraoServiceRest.detalhar(sessaoHttpRequest.getUsuario(), id);
        return new ResponseEntity(textoPadraoResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/excluir/{id}",
            method = RequestMethod.DELETE,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Detalhar um TextoPadrão.",
            response = TextoPadraoResponse.class
    )
    public ResponseEntity excluir(HttpServletRequest request, @PathVariable Long id) throws HTTP401Exception, TextoPadraoRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        boolean ok = textoPadraoServiceRest.excluir(sessaoHttpRequest.getUsuario(), id);
        return new ResponseEntity(null, ok ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(
            path = "/cadastrar",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Cadastrar um novo TextoPadrão.",
            response = TextoPadraoResponse.class
    )
    public ResponseEntity cadastrar(HttpServletRequest request, @RequestBody RequestCadastroTextoPadrao requestCadastroTextoPadrao) throws HTTP401Exception, TextoPadraoRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        TextoPadraoResponse textoPadraoResponse = textoPadraoServiceRest.cadastrar(sessaoHttpRequest.getUsuario(), requestCadastroTextoPadrao);
        return new ResponseEntity(textoPadraoResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/editar/{id}",
            method = RequestMethod.PUT,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Cadastrar um novo TextoPadrão.",
            response = TextoPadraoResponse.class
    )
    public ResponseEntity editar(HttpServletRequest request, @PathVariable Long id,  @RequestBody RequestCadastroTextoPadrao requestCadastroTextoPadrao) throws HTTP401Exception, TextoPadraoRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        TextoPadraoResponse textoPadraoResponse = textoPadraoServiceRest.editar(sessaoHttpRequest.getUsuario(), id, requestCadastroTextoPadrao);
        return new ResponseEntity(textoPadraoResponse, HttpStatus.OK);
    }
}
