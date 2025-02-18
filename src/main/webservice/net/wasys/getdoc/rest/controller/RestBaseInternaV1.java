package net.wasys.getdoc.rest.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.wasys.getdoc.domain.entity.SessaoHttpRequest;
import net.wasys.getdoc.domain.vo.filtro.BaseInternaFiltro;
import net.wasys.getdoc.rest.exception.FeriadoRestException;
import net.wasys.getdoc.rest.exception.HTTP401Exception;
import net.wasys.getdoc.rest.request.vo.RequestCadastroBaseInterna;
import net.wasys.getdoc.rest.request.vo.RequestVisualizarBaseInterna;
import net.wasys.getdoc.rest.response.vo.BaseInternaResponse;
import net.wasys.getdoc.rest.response.vo.RegistroValorResponse;
import net.wasys.getdoc.rest.response.vo.SegurosResponse;
import net.wasys.getdoc.rest.service.BaseInternaServiceRest;
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
@RequestMapping("/base-interna/v1")
@Api(tags = "//base-interna", description = "Serviços relacionados ao base interna.")
public class RestBaseInternaV1 extends SuperController {

    @Autowired
    private BaseInternaServiceRest baseInternaServiceRest;

    @RequestMapping(
            path = "/ativas",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta as base internas ativas.",
            response = BaseInternaResponse.class
    )
    public ResponseEntity getAtivas(HttpServletRequest request) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<BaseInternaResponse> list = baseInternaServiceRest.getBaseAtivas();
        return new ResponseEntity(list, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/{min}/{max}",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta as base internas cadastrados.",
            response = BaseInternaResponse.class
    )
    public ResponseEntity getAll(HttpServletRequest request, @PathVariable int min, @PathVariable int max,@RequestBody BaseInternaFiltro baseInternaFiltro) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<BaseInternaResponse> list = baseInternaServiceRest.getAll(baseInternaFiltro, min, max);
        return new ResponseEntity(list, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/detalhar/{id}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Obtém os dados da base interna.",
            response = BaseInternaResponse.class
    )
    public ResponseEntity detalhar(HttpServletRequest request, @PathVariable Long id) throws HTTP401Exception, FeriadoRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        BaseInternaResponse baseInternaResponse = baseInternaServiceRest.detalhar(sessaoHttpRequest.getUsuario(), id);
        return new ResponseEntity(baseInternaResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/cadastrar",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Cadastrar uma base interna.",
            response = BaseInternaResponse.class
    )
    public ResponseEntity cadastrar(HttpServletRequest request, @RequestBody RequestCadastroBaseInterna requestCadastroBaseInterna) throws HTTP401Exception, FeriadoRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        BaseInternaResponse baseInternaResponse = baseInternaServiceRest.cadastrar(sessaoHttpRequest.getUsuario(), requestCadastroBaseInterna);
        return new ResponseEntity(baseInternaResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/editar/{id}",
            method = RequestMethod.PUT,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Editar uma base interna.",
            response = BaseInternaResponse.class
    )
    public ResponseEntity editar(HttpServletRequest request, @PathVariable Long id, @RequestBody RequestCadastroBaseInterna requestCadastroBaseInterna) throws HTTP401Exception, FeriadoRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        BaseInternaResponse baseInternaResponse = baseInternaServiceRest.editar(sessaoHttpRequest.getUsuario(), id, requestCadastroBaseInterna);
        return new ResponseEntity(baseInternaResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/excluir/{id}",
            method = RequestMethod.DELETE,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Exclui uma base interna."
    )
    public ResponseEntity excluir(HttpServletRequest request, @PathVariable Long id) throws HTTP401Exception, FeriadoRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        boolean excluir = baseInternaServiceRest.excluir(sessaoHttpRequest.getUsuario(), id);
        return new ResponseEntity(null, excluir ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(
            path = "/visualizar/{id}/{min}/{max}",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta os valores da base interna.",
            response = RegistroValorResponse.class
    )
    public ResponseEntity getAll(HttpServletRequest request,@PathVariable Long id, @PathVariable int min, @PathVariable int max,@RequestBody RequestVisualizarBaseInterna requestVisualizarBaseInterna) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        RegistroValorResponse response = baseInternaServiceRest.visualizar(sessaoHttpRequest.getUsuario(),id, min, max,requestVisualizarBaseInterna);
        return new ResponseEntity(response, HttpStatus.OK);
    }
}