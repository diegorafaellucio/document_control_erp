package net.wasys.getdoc.rest.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.wasys.getdoc.domain.entity.SessaoHttpRequest;
import net.wasys.getdoc.rest.exception.FeriadoRestException;
import net.wasys.getdoc.rest.exception.HTTP401Exception;
import net.wasys.getdoc.rest.request.vo.RequestCadastroFeriado;
import net.wasys.getdoc.rest.response.vo.FeriadoResponse;
import net.wasys.getdoc.rest.service.FeriadoServiceRest;
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
@RequestMapping("/feriado/v1")
@Api(tags = "/feriado", description = "Serviços relacionados ao feriado.")
public class RestFeriadoV1 extends SuperController {

    @Autowired
    private FeriadoServiceRest feriadoServiceRest;

    @RequestMapping(
            path = "/{min}/{max}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta os feriados cadastrados.",
            response = FeriadoResponse.class
    )
    public ResponseEntity getAll(HttpServletRequest request, @PathVariable int min, @PathVariable int max) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<FeriadoResponse> list = feriadoServiceRest.getFeriados(sessaoHttpRequest.getUsuario(), min, max);
        return new ResponseEntity(list, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/detalhar/{id}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Obtém os dados do feriado.",
            response = FeriadoResponse.class
    )
    public ResponseEntity detalhar(HttpServletRequest request, @PathVariable Long id) throws HTTP401Exception, FeriadoRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        FeriadoResponse feriadoResponse = feriadoServiceRest.detalhar(sessaoHttpRequest.getUsuario(), id);
        return new ResponseEntity(feriadoResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/cadastrar",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Cadastrar um novo feriado.",
            response = FeriadoResponse.class
    )
    public ResponseEntity cadastrar(HttpServletRequest request, @RequestBody RequestCadastroFeriado requestCadastroFeriado) throws HTTP401Exception, FeriadoRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        FeriadoResponse feriadoResponse = feriadoServiceRest.cadastrar(sessaoHttpRequest.getUsuario(), requestCadastroFeriado);
        return new ResponseEntity(feriadoResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/editar/{id}",
            method = RequestMethod.PUT,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Editar um feriado.",
            response = FeriadoResponse.class
    )
    public ResponseEntity editar(HttpServletRequest request, @PathVariable Long id, @RequestBody RequestCadastroFeriado requestCadastroFeriado) throws HTTP401Exception, FeriadoRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        FeriadoResponse feriadoResponse = feriadoServiceRest.editar(sessaoHttpRequest.getUsuario(), id, requestCadastroFeriado);
        return new ResponseEntity(feriadoResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/excluir/{id}",
            method = RequestMethod.DELETE,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Editar um feriado."
    )
    public ResponseEntity excluir(HttpServletRequest request, @PathVariable Long id) throws HTTP401Exception, FeriadoRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        boolean excluir = feriadoServiceRest.excluir(sessaoHttpRequest.getUsuario(), id);
        return new ResponseEntity(null, excluir ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

}