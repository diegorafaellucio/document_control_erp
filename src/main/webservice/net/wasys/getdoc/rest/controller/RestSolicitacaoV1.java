package net.wasys.getdoc.rest.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.wasys.getdoc.domain.entity.SessaoHttpRequest;
import net.wasys.getdoc.rest.exception.HTTP401Exception;
import net.wasys.getdoc.rest.request.vo.RequestCadastroSolicitacao;
import net.wasys.getdoc.rest.request.vo.RequestRecusarSolicitacao;
import net.wasys.getdoc.rest.request.vo.RequestRespostaSolicitacao;
import net.wasys.getdoc.rest.response.vo.ListaSolicitacaoResponse;
import net.wasys.getdoc.rest.service.SolicitacaoServiceRest;
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
import java.util.Set;

@CrossOrigin
@Controller
@RequestMapping("/solicitacao/v1")
@Api(tags = "/solicitacao", description = "Serviços relacionados a Solicitação.")
public class RestSolicitacaoV1 extends SuperController {

    @Autowired
    private SolicitacaoServiceRest solicitacaoServiceRest;

    @RequestMapping(
            path = "/processo/{id}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta as solicitações vinculadas ao processo.",
            response = ListaSolicitacaoResponse.class
    )
    public ResponseEntity getAtivas(HttpServletRequest request, @PathVariable Long id) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        ListaSolicitacaoResponse solicitacoes = solicitacaoServiceRest.getSolicitacoes(sessaoHttpRequest.getUsuario(), id);
        return new ResponseEntity(solicitacoes, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/upload-anexo/{processoId}",
            consumes = "multipart/form-data",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @ApiOperation(
            value = "Upload de imagem como anexo do processo.",
            response = String.class,
            notes = "Serviço responsável por fazer o upload de imagem para um processo."
    )
    public ResponseEntity passo1UploadAnexoSolicitacao(MultipartHttpServletRequest request, @PathVariable Long processoId, @RequestParam("file") MultipartFile[] multipartFile) throws MessageKeyException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        Set<String> listChecksum = solicitacaoServiceRest.passo1UploadAnexoSolicitacao(sessaoHttpRequest.getUsuario(), processoId, multipartFile);
        return new ResponseEntity(listChecksum, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/processo/{processoId}/cadastrar",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Cadastra uma nova solicitação.",
            response = ListaSolicitacaoResponse.class
    )
    public ResponseEntity cadastrarSolicitacao(HttpServletRequest request, @PathVariable Long processoId, @RequestBody RequestCadastroSolicitacao requestCadastroSolicitacao) throws Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        boolean ok = solicitacaoServiceRest.cadastrarSolicitacao(sessaoHttpRequest.getUsuario(), processoId, requestCadastroSolicitacao);
        return new ResponseEntity(null, ok ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(
            path = "/resposta/upload-anexo/{solicitacaoId}",
            consumes = "multipart/form-data",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @ApiOperation(
            value = "Upload de imagem como anexo do processo.",
            response = String.class,
            notes = "Serviço responsável por fazer o upload de imagem para um processo."
    )
    public ResponseEntity passo1UploadAnexoRespostaSolicitacao(MultipartHttpServletRequest request, @PathVariable Long solicitacaoId, @RequestParam("file") MultipartFile[] multipartFile) throws MessageKeyException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        Set<String> listChecksum = solicitacaoServiceRest.passo1UploadAnexoRespostaSolicitacao(sessaoHttpRequest.getUsuario(), solicitacaoId, multipartFile);
        return new ResponseEntity(listChecksum, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/registrar-resposta/{solicitacaoId}",
            method = RequestMethod.PUT,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Cadastra uma nova solicitação.",
            response = ListaSolicitacaoResponse.class
    )
    public ResponseEntity registrarResposta(HttpServletRequest request, @PathVariable Long solicitacaoId, @RequestBody RequestRespostaSolicitacao requestRespostaSolicitacao) throws Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        boolean ok = solicitacaoServiceRest.registrarRespostaSolicitacao(sessaoHttpRequest.getUsuario(), solicitacaoId, requestRespostaSolicitacao);
        return new ResponseEntity(null, ok ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(
            path = "/recusar/upload-anexo/{solicitacaoId}",
            consumes = "multipart/form-data",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @ApiOperation(
            value = "Upload de imagem como anexo na recusa.",
            response = String.class,
            notes = "Serviço responsável por fazer o upload de imagem para recusar uma solicitação."
    )
    public ResponseEntity passo1UploadAnexoRecusaSolicitacao(MultipartHttpServletRequest request, @PathVariable Long solicitacaoId, @RequestParam("file") MultipartFile[] multipartFile) throws MessageKeyException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        Set<String> listChecksum = solicitacaoServiceRest.passo1UploadAnexoRecusaSolicitacao(sessaoHttpRequest.getUsuario(), solicitacaoId, multipartFile);
        return new ResponseEntity(listChecksum, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/recusar/{solicitacaoId}",
            method = RequestMethod.PUT,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Recusa uma solicitação.",
            response = ListaSolicitacaoResponse.class
    )
    public ResponseEntity recusarSolicitacao(HttpServletRequest request, @PathVariable Long solicitacaoId, @RequestBody RequestRecusarSolicitacao requestRecusarSolicitacao) throws Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        boolean ok = solicitacaoServiceRest.recusarSolicitacao(sessaoHttpRequest.getUsuario(), solicitacaoId, requestRecusarSolicitacao);
        return new ResponseEntity(null, ok ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

}
