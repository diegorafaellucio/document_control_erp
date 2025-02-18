package net.wasys.getdoc.rest.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.wasys.getdoc.domain.entity.SessaoHttpRequest;
import net.wasys.getdoc.rest.exception.ModeloDocumentoRestException;
import net.wasys.getdoc.rest.request.vo.RequestCadastroModeloDocumento;
import net.wasys.getdoc.rest.response.vo.DetalhesModeloDocumentoResponse;
import net.wasys.getdoc.rest.response.vo.ModeloDocumentoResponse;
import net.wasys.getdoc.rest.service.ModeloDocumentoServiceRest;
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
@RequestMapping("/modelo-documento/v1")
@Api(tags = "/modelo-documento", description = "Serviços relacionados ao ModeloDocumento.")
public class RestModeloDocumentoV1 extends SuperController {

    @Autowired
    private ModeloDocumentoServiceRest modeloDocumentoServiceRest;

    @RequestMapping(
            path = "/ativos",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta os ModeloDocumento que estão ativos no sistema.",
            response = ModeloDocumentoResponse.class
    )
    public ResponseEntity getAtivos(HttpServletRequest request) {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<ModeloDocumentoResponse> ativos = modeloDocumentoServiceRest.getAtivos(sessaoHttpRequest.getUsuario());
        return new ResponseEntity(ativos, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/{min}/{max}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta todos os ModeloDocumento.",
            response = ModeloDocumentoResponse.class
    )
    public ResponseEntity getModelos(HttpServletRequest request, @PathVariable int min, @PathVariable int max) {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<ModeloDocumentoResponse> modelos = modeloDocumentoServiceRest.getModelos(sessaoHttpRequest.getUsuario(), min, max);
        return new ResponseEntity(modelos, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/detalhar/{id}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Obtém os dados do ModeloDocumento.",
            response = DetalhesModeloDocumentoResponse.class
    )
    public ResponseEntity detalhar(HttpServletRequest request, @PathVariable Long id) throws ModeloDocumentoRestException{
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        DetalhesModeloDocumentoResponse modeloDocumentoResponse = modeloDocumentoServiceRest.detalhar(sessaoHttpRequest.getUsuario(), id);
        return new ResponseEntity(modeloDocumentoResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/cadastrar",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Cadastrar um novo ModeloDocumento.",
            response = DetalhesModeloDocumentoResponse.class
    )
    public ResponseEntity cadastro(HttpServletRequest request, @RequestBody RequestCadastroModeloDocumento requestCadastroModeloDocumento) {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        DetalhesModeloDocumentoResponse modeloDocumentoResponse = modeloDocumentoServiceRest.cadastrar(sessaoHttpRequest.getUsuario(), requestCadastroModeloDocumento);
        return new ResponseEntity(modeloDocumentoResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/editar/{id}",
            method = RequestMethod.PUT,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Editar um ModeloDocumento.",
            response = DetalhesModeloDocumentoResponse.class
    )
    public ResponseEntity editar(HttpServletRequest request, @PathVariable Long id, @RequestBody RequestCadastroModeloDocumento requestCadastroModeloDocumento) {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        DetalhesModeloDocumentoResponse modeloDocumentoResponse = modeloDocumentoServiceRest.editar(sessaoHttpRequest.getUsuario(), id, requestCadastroModeloDocumento);
        return new ResponseEntity(modeloDocumentoResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/excluir/{id}",
            method = RequestMethod.DELETE,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Editar um ModeloDocumento."
    )
    public ResponseEntity excluir(HttpServletRequest request, @PathVariable Long id) {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        boolean ok = modeloDocumentoServiceRest.excluir(sessaoHttpRequest.getUsuario(), id);
        return new ResponseEntity(null, ok ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }
}