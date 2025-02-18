package net.wasys.getdoc.rest.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.wasys.getdoc.domain.entity.SessaoHttpRequest;
import net.wasys.getdoc.rest.exception.HTTP401Exception;
import net.wasys.getdoc.rest.request.vo.RequestCadastroTipoEvidencia;
import net.wasys.getdoc.rest.response.vo.TipoEvidenciaResponse;
import net.wasys.getdoc.rest.service.TipoEvidenciaServiceRest;
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
@RequestMapping("/tipo-evidencia/v1")
@Api(tags = "/tipo-evidencia", description = "Serviços relacionados ao TipoEvidencia.")
public class RestTipoEvidenciaV1 extends SuperController {

    @Autowired
    private TipoEvidenciaServiceRest tipoEvidenciaServiceRest;

    @RequestMapping(
            path = "/ativas",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta os tipos de evidências que estão ativos.",
            response = TipoEvidenciaResponse.class
    )
    public ResponseEntity getAtivas(HttpServletRequest request) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<TipoEvidenciaResponse> list = tipoEvidenciaServiceRest.getAtivas(sessaoHttpRequest.getUsuario());
        return new ResponseEntity(list, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/{min}/{max}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta todos os tipos de evidências cadastrados..",
            response = TipoEvidenciaResponse.class
    )
    public ResponseEntity getAll(HttpServletRequest request, @PathVariable int min, @PathVariable int max) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<TipoEvidenciaResponse> list = tipoEvidenciaServiceRest.getAll(sessaoHttpRequest.getUsuario(), min, max);
        return new ResponseEntity(list, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/detalhar/{id}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Ontém os detalhes de um TIpoEvidência.",
            response = TipoEvidenciaResponse.class
    )
    public ResponseEntity detalhar(HttpServletRequest request, @PathVariable Long id) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        TipoEvidenciaResponse tipoEvidencia = tipoEvidenciaServiceRest.detalhar(sessaoHttpRequest.getUsuario(), id);
        return new ResponseEntity(tipoEvidencia, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/excluir/{id}",
            method = RequestMethod.DELETE,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Excluir um TipoEvidência."
    )
    public ResponseEntity excluir(HttpServletRequest request, @PathVariable Long id) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        boolean excluir = tipoEvidenciaServiceRest.excluir(sessaoHttpRequest.getUsuario(), id);
        return new ResponseEntity(null, excluir ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(
            path = "/cadastrar",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Cadastrar um TpoEvidência.",
            response = TipoEvidenciaResponse.class
    )
    public ResponseEntity cadastrar(HttpServletRequest request, @RequestBody RequestCadastroTipoEvidencia requestCadastroTipoEvidencia) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        TipoEvidenciaResponse tipoEvidencia = tipoEvidenciaServiceRest.cadastrar(sessaoHttpRequest.getUsuario(), requestCadastroTipoEvidencia);
        return new ResponseEntity(tipoEvidencia, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/editar/{id}",
            method = RequestMethod.PUT,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Cadastrar um TpoEvidência.",
            response = TipoEvidenciaResponse.class
    )
    public ResponseEntity editar(HttpServletRequest request, @PathVariable Long id, @RequestBody RequestCadastroTipoEvidencia requestCadastroTipoEvidencia) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        TipoEvidenciaResponse tipoEvidencia = tipoEvidenciaServiceRest.editar(sessaoHttpRequest.getUsuario(), id, requestCadastroTipoEvidencia);
        return new ResponseEntity(tipoEvidencia, HttpStatus.OK);
    }
}
