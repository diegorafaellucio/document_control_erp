package net.wasys.getdoc.rest.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.wasys.getdoc.domain.entity.SessaoHttpRequest;
import net.wasys.getdoc.rest.exception.HTTP401Exception;
import net.wasys.getdoc.rest.exception.IrregularidadeRestException;
import net.wasys.getdoc.rest.request.vo.RequestCadastroIrregularidade;
import net.wasys.getdoc.rest.response.vo.IrregularidadeResponse;
import net.wasys.getdoc.rest.service.IrregularidadeServiceRest;
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
@RequestMapping("/irregularidade/v1")
@Api(tags = "/irregularidade", description = "Serviços relacionados a Irregularidade.")
public class RestIrregularidadeV1 extends SuperController {

    @Autowired
    private IrregularidadeServiceRest irregularidadeServiceRest;

    @RequestMapping(
            path = "/ativas",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta as irregularidades que estão ativas.",
            response = IrregularidadeResponse.class
    )
    public ResponseEntity getAtivas(HttpServletRequest request) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<IrregularidadeResponse> list = irregularidadeServiceRest.getAtivas(sessaoHttpRequest.getUsuario());
        return new ResponseEntity(list, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/{min}/{max}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta todos as irregularidades cadastrados..",
            response = IrregularidadeResponse.class
    )
    public ResponseEntity getAll(HttpServletRequest request, @PathVariable int min, @PathVariable int max) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<IrregularidadeResponse> list = irregularidadeServiceRest.getAll(sessaoHttpRequest.getUsuario(), min, max);
        return new ResponseEntity(list, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/detalhar/{id}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Obtém os dados da uma Irregularidade.",
            response = IrregularidadeResponse.class
    )
    public ResponseEntity detalhar(HttpServletRequest request, @PathVariable Long id) throws HTTP401Exception, IrregularidadeRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        IrregularidadeResponse irregularidadeResponse = irregularidadeServiceRest.detalhar(sessaoHttpRequest.getUsuario(), id);
        return new ResponseEntity(irregularidadeResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/excluir/{id}",
            method = RequestMethod.DELETE,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Excluir uma Irregularidade."
    )
    public ResponseEntity excluir(HttpServletRequest request, @PathVariable Long id) throws HTTP401Exception, IrregularidadeRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        boolean excluir = irregularidadeServiceRest.excluir(sessaoHttpRequest.getUsuario(), id);
        return new ResponseEntity(null, excluir ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(
            path = "/cadastrar",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Cadastrar uma nova Irregularidade.",
            response = IrregularidadeResponse.class
    )
    public ResponseEntity cadastrar(HttpServletRequest request, @RequestBody RequestCadastroIrregularidade requestCadastroIrregularidade) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        IrregularidadeResponse cadastrar = irregularidadeServiceRest.cadastrar(sessaoHttpRequest.getUsuario(), requestCadastroIrregularidade);
        return new ResponseEntity(cadastrar, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/editar/{id}",
            method = RequestMethod.PUT,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Editar uma Irregularidade.",
            response = IrregularidadeResponse.class
    )
    public ResponseEntity editar(HttpServletRequest request, @PathVariable Long id, @RequestBody RequestCadastroIrregularidade requestCadastroIrregularidade) throws HTTP401Exception, IrregularidadeRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        IrregularidadeResponse irregularidadeResponse = irregularidadeServiceRest.editar(sessaoHttpRequest.getUsuario(), id, requestCadastroIrregularidade);
        return new ResponseEntity(irregularidadeResponse, HttpStatus.OK);
    }
}
