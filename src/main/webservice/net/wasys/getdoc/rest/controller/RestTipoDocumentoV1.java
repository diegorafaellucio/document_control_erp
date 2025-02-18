package net.wasys.getdoc.rest.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.wasys.getdoc.domain.entity.SessaoHttpRequest;
import net.wasys.getdoc.rest.exception.DadosObrigatorioRequestException;
import net.wasys.getdoc.rest.exception.HTTP401Exception;
import net.wasys.getdoc.rest.exception.TipoCampoRestException;
import net.wasys.getdoc.rest.exception.TipoProcessoRestException;
import net.wasys.getdoc.rest.request.vo.RequestCadastroTipoDocumento;
import net.wasys.getdoc.rest.response.vo.ModeloOcrResponse;
import net.wasys.getdoc.rest.response.vo.TipoDocumentoResponse;
import net.wasys.getdoc.rest.service.ModeloOcrServiceRest;
import net.wasys.getdoc.rest.service.TipoDocumentoServiceRest;
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
@RequestMapping("/tipo-documento/v1")
@Api(tags = "/tipo-documento", description = "Serviços relacionados ao TipoDocumento.")
public class RestTipoDocumentoV1 extends SuperController {

    @Autowired
    private TipoDocumentoServiceRest tipoDocumentoServiceRest;

    @RequestMapping(
            path = "/{tipoDocumentoId}/editar",
            method = RequestMethod.PUT,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Editar um Tipo Documento.",
            response = TipoDocumentoResponse.class
    )
    public ResponseEntity editarTipoDocumento(HttpServletRequest request, @PathVariable Long tipoDocumentoId, @RequestBody RequestCadastroTipoDocumento requestCadastroTipoDocumento) throws HTTP401Exception, TipoProcessoRestException, TipoCampoRestException, DadosObrigatorioRequestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        TipoDocumentoResponse tipoDocumentoResponse = tipoDocumentoServiceRest.editarTipoDocumento(sessaoHttpRequest.getUsuario(), tipoDocumentoId, requestCadastroTipoDocumento);
        return new ResponseEntity(tipoDocumentoResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/{tipoDocumentoId}",
            method = RequestMethod.DELETE,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Excluir um Tipo Documento."
    )
    public ResponseEntity excluirTipoDocumento(HttpServletRequest request, @PathVariable Long tipoDocumentoId) throws HTTP401Exception, TipoProcessoRestException, TipoCampoRestException, DadosObrigatorioRequestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        boolean ok = tipoDocumentoServiceRest.excluirTipoDocumento(sessaoHttpRequest.getUsuario(), tipoDocumentoId);
        return new ResponseEntity(null, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/detalhar/{tipoDocumentoId}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta os dados de um TipoDocumento.",
            response = TipoDocumentoResponse.class
    )
    public ResponseEntity detalhar(HttpServletRequest request, @PathVariable Long tipoDocumentoId) throws HTTP401Exception, TipoProcessoRestException, TipoCampoRestException, DadosObrigatorioRequestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        TipoDocumentoResponse tipoDocumentoResponse = tipoDocumentoServiceRest.detalhar(sessaoHttpRequest.getUsuario(), tipoDocumentoId);
        return new ResponseEntity(tipoDocumentoResponse, HttpStatus.OK);
    }
}