package net.wasys.getdoc.rest.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.wasys.getdoc.domain.entity.SessaoHttpRequest;
import net.wasys.getdoc.rest.exception.AreaRestException;
import net.wasys.getdoc.rest.exception.HTTP401Exception;
import net.wasys.getdoc.rest.request.vo.RequestCadastrarArea;
import net.wasys.getdoc.rest.response.vo.AreaResponse;
import net.wasys.getdoc.rest.response.vo.DetalhesAreaResponse;
import net.wasys.getdoc.rest.response.vo.SubAreaResponse;
import net.wasys.getdoc.rest.service.AreaServiceRest;
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
@RequestMapping("/area/v1")
@Api(tags = "/area", description = "Serviços relacionados a Área.")
public class RestAreaV1 extends SuperController {

    @Autowired
    private AreaServiceRest areaServiceRest;

    @RequestMapping(
            path = "/ativas",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta as áreas que estão ativas.",
            response = AreaResponse.class
    )
    public ResponseEntity getAtivas(HttpServletRequest request) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<AreaResponse> list = areaServiceRest.getAtivas(sessaoHttpRequest.getUsuario());
        return new ResponseEntity(list, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/{min}/{max}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta todos as áreas cadastrados..",
            response = AreaResponse.class
    )
    public ResponseEntity getAll(HttpServletRequest request, @PathVariable int min, @PathVariable int max) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<AreaResponse> list = areaServiceRest.getAll(sessaoHttpRequest.getUsuario(), min, max);
        return new ResponseEntity(list, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/sub-areas/{areaId}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta as subáreas de uma determinada área.",
            response = SubAreaResponse.class
    )
    public ResponseEntity getAll(HttpServletRequest request, @PathVariable Long areaId) throws HTTP401Exception, AreaRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<SubAreaResponse> list = areaServiceRest.getSubAreas(sessaoHttpRequest.getUsuario(), areaId);
        return new ResponseEntity(list, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/cadastrar",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Cadastra uma nova área.",
            response = DetalhesAreaResponse.class
    )
    public ResponseEntity cadastrar(HttpServletRequest request, @RequestBody RequestCadastrarArea requestCadastrarArea) throws HTTP401Exception, AreaRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        DetalhesAreaResponse areaResponse = areaServiceRest.cadastrar(sessaoHttpRequest.getUsuario(), requestCadastrarArea);
        return new ResponseEntity(areaResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/editar/{id}",
            method = RequestMethod.PUT,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Editar uma área.",
            response = DetalhesAreaResponse.class
    )
    public ResponseEntity editar(HttpServletRequest request, @PathVariable Long id, @RequestBody RequestCadastrarArea requestCadastrarArea) throws HTTP401Exception, AreaRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        DetalhesAreaResponse areaResponse = areaServiceRest.editar(sessaoHttpRequest.getUsuario(), id, requestCadastrarArea);
        return new ResponseEntity(areaResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/detalhar/{id}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Obtém os dados de uma área.",
            response = DetalhesAreaResponse.class
    )
    public ResponseEntity detalhar(HttpServletRequest request, @PathVariable Long id) throws HTTP401Exception, AreaRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        DetalhesAreaResponse areaResponse = areaServiceRest.detalhar(sessaoHttpRequest.getUsuario(), id);
        return new ResponseEntity(areaResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/excluir/{id}",
            method = RequestMethod.DELETE,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Excluir uma área."
    )
    public ResponseEntity excluir(HttpServletRequest request, @PathVariable Long id) throws HTTP401Exception, AreaRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        boolean ok = areaServiceRest.excluir(sessaoHttpRequest.getUsuario(), id);
        return new ResponseEntity(null, ok ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }
}
