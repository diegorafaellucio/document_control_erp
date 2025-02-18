package net.wasys.getdoc.rest.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.wasys.getdoc.domain.entity.SessaoHttpRequest;
import net.wasys.getdoc.rest.exception.HTTP401Exception;
import net.wasys.getdoc.rest.exception.ModeloOcrRestException;
import net.wasys.getdoc.rest.request.vo.RequestCadastroModeloOcr;
import net.wasys.getdoc.rest.response.vo.ListaModeloOcrResponse;
import net.wasys.getdoc.rest.response.vo.ModeloOcrResponse;
import net.wasys.getdoc.rest.service.ModeloOcrServiceRest;
import net.wasys.util.ddd.MessageKeyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

import static org.jpedal.objects.raw.PdfDictionary.min;

@CrossOrigin
@Controller
@RequestMapping("/modelo-ocr/v1")
@Api(tags = "/modelo-ocr", description = "Serviços relacionados ao ModeloOCR.")
public class RestModeloOcrV1 extends SuperController {

    @Autowired
    private ModeloOcrServiceRest modeloOcrServiceRest;

    @RequestMapping(
            path = "/ativos",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta os ModeloOCR que estão ativos no sistema.",
            response = ModeloOcrResponse.class
    )
    public ResponseEntity getAtivos(HttpServletRequest request) {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<ModeloOcrResponse> ativos = modeloOcrServiceRest.getAtivos(sessaoHttpRequest.getUsuario());
        return new ResponseEntity(ativos, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/{min}/{max}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta todos os ModeloOCR.",
            response = ListaModeloOcrResponse.class
    )
    public ResponseEntity getAll(HttpServletRequest request, @PathVariable int min, @PathVariable int max)  throws HTTP401Exception, ModeloOcrRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<ListaModeloOcrResponse> ativos = modeloOcrServiceRest.getAll(sessaoHttpRequest.getUsuario(), min, max);
        return new ResponseEntity(ativos, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/upload",
            consumes = "multipart/form-data",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @ApiOperation(
            value = "Upload de template para o OCR.",
            response = String.class,
            notes = "Serviço responsável por fazer o upload do template que será utilizado para OCR documento."
    )
    public ResponseEntity passo1Upload(MultipartHttpServletRequest request, @RequestParam("file") MultipartFile[] multipartFile) throws MessageKeyException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        Set<String> listChecksum = modeloOcrServiceRest.passo1UploadModeloOcr(sessaoHttpRequest.getUsuario(), multipartFile);
        return new ResponseEntity(listChecksum, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/cadastrar",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Cadastra um novo ModeloOCR.",
            response = ListaModeloOcrResponse.class
    )
    public ResponseEntity cadastrar(HttpServletRequest request, @RequestBody RequestCadastroModeloOcr requestCadastroModeloOcr) throws HTTP401Exception, ModeloOcrRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        ListaModeloOcrResponse modeloOcrResponse = modeloOcrServiceRest.cadastrar(sessaoHttpRequest.getUsuario(), requestCadastroModeloOcr);
        return new ResponseEntity(modeloOcrResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/editar/{id}",
            method = RequestMethod.PUT,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Altera os dados de um ModeloOCR.",
            response = ListaModeloOcrResponse.class
    )
    public ResponseEntity editar(HttpServletRequest request, @PathVariable Long id, @RequestBody RequestCadastroModeloOcr requestCadastroModeloOcr)  throws HTTP401Exception, ModeloOcrRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        ListaModeloOcrResponse modeloOcrResponse = modeloOcrServiceRest.editar(sessaoHttpRequest.getUsuario(), id, requestCadastroModeloOcr);
        return new ResponseEntity(modeloOcrResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/excluir/{id}",
            method = RequestMethod.DELETE,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Exclui um ModeloOCR."
    )
    public ResponseEntity excluir(HttpServletRequest request, @PathVariable Long id) throws HTTP401Exception, ModeloOcrRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        boolean ok = modeloOcrServiceRest.excluir(sessaoHttpRequest.getUsuario(), id);
        return new ResponseEntity(null , ok ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(
            path = "/detalhar/{id}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Obtém os dados de um ModeloOCR.",
            response = ListaModeloOcrResponse.class
    )
    public ResponseEntity detalhar(HttpServletRequest request, @PathVariable Long id)  throws HTTP401Exception, ModeloOcrRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        ListaModeloOcrResponse modeloOcrResponse = modeloOcrServiceRest.detalhar(sessaoHttpRequest.getUsuario(), id);
        return new ResponseEntity(modeloOcrResponse, HttpStatus.OK);
    }
}