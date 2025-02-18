package net.wasys.getdoc.rest.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.wasys.getdoc.domain.entity.SessaoHttpRequest;
import net.wasys.getdoc.domain.enumeration.Origem;
import net.wasys.getdoc.rest.exception.AnexoRestException;
import net.wasys.getdoc.rest.exception.DadosObrigatorioRequestException;
import net.wasys.getdoc.rest.exception.HTTP401Exception;
import net.wasys.getdoc.rest.exception.ProcessoRestException;
import net.wasys.getdoc.rest.request.RequestJustificarDocumento;
import net.wasys.getdoc.rest.request.vo.RequestImagensDocumento;
import net.wasys.getdoc.rest.response.vo.DocumentoExcluidoResponse;
import net.wasys.getdoc.rest.response.vo.DocumentoUploadResponse;
import net.wasys.getdoc.rest.response.vo.DownloadAnexoResponse;
import net.wasys.getdoc.rest.response.vo.ImagenDocumentoResponse;
import net.wasys.getdoc.rest.service.DocumentoServiceRest;
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

/**
 *
 */
@CrossOrigin
@Controller
@RequestMapping("/documento/v1")
@Api(tags = "/documento", description = "Serviços relacionados ao Documento.")
public class RestDocumentoV1 extends SuperController {

    @Autowired
    private DocumentoServiceRest documentoServiceRest;

    @RequestMapping(
            path = "/upload/{documentoId}",
            consumes = "multipart/form-data",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @ApiOperation(
            value = "Upload de imagem para o documento.",
            response = String.class,
            notes = "Serviço responsável por fazer o upload de imagem para um documento."
    )
    public ResponseEntity passo1Upload(MultipartHttpServletRequest request, @PathVariable Long documentoId, @RequestParam("file") MultipartFile[] multipartFile) throws MessageKeyException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        Set<String> listChecksum = documentoServiceRest.passo1UploadDocumento(sessaoHttpRequest.getUsuario(), documentoId, multipartFile);
        return new ResponseEntity(listChecksum, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/upload/{documentoId}/salvar",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @ApiOperation(
            value = "Salva as imagens que foram uploadadas.",
            response = DocumentoUploadResponse.class,
            notes = "Serviço responsável por confirmar o upload dos documentos, vai de fato persistir as imagens no documento do processo."
    )
    public ResponseEntity passo2Salvar(HttpServletRequest request, @PathVariable Long documentoId, @RequestBody List<String> listChecksum) throws Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        Origem origem = getOrigemFromHeader(request);
		DocumentoUploadResponse documentoUploadResponses = documentoServiceRest.passo2ConfirmaArquivos(sessaoHttpRequest.getUsuario(), documentoId, listChecksum, origem);
        return new ResponseEntity(documentoUploadResponses, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/excluidos/{processoId}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta os documentos excluídos que podem ser adicionados ao processo.",
            response = DocumentoExcluidoResponse.class
    )
    public ResponseEntity getDocumentosExcluidos(HttpServletRequest request, @PathVariable Long processoId) throws DadosObrigatorioRequestException, HTTP401Exception, ProcessoRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<DocumentoExcluidoResponse> documentos = documentoServiceRest.getDocumentosExcluidos(sessaoHttpRequest.getUsuario(), processoId, getImagePath(request));
        return new ResponseEntity(documentos, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/{documentoId}/imagens",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Recupera lista de imagens (id) do documento."
    )
    public ResponseEntity getImagens(HttpServletRequest request, @PathVariable Long documentoId) throws DadosObrigatorioRequestException, HTTP401Exception, AnexoRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        ImagenDocumentoResponse download = documentoServiceRest.getImagens(sessaoHttpRequest.getUsuario(), documentoId);
        return new ResponseEntity(download, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/justificar/{documentoId}/{processoId}",
            method = RequestMethod.PUT,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Justifica um processo."
    )
    public ResponseEntity justificar(HttpServletRequest request, @PathVariable Long documentoId,@PathVariable Long processoId, @RequestBody RequestJustificarDocumento requestJustificarDocumento) throws Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        Boolean ok = documentoServiceRest.justificar(sessaoHttpRequest.getUsuario(),documentoId, processoId, requestJustificarDocumento, getImagePath(request));
        return new ResponseEntity(null, ok ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(
            path = "/download/{imagenId}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
    @ApiOperation(
            value = "Faz o download da imagem do documento."
    )
    public ResponseEntity download(HttpServletRequest request, @PathVariable Long imagenId) throws DadosObrigatorioRequestException, HTTP401Exception, AnexoRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        DownloadAnexoResponse download = documentoServiceRest.download(sessaoHttpRequest.getUsuario(), imagenId);
        return new ResponseEntity(download.getIsr(), download.getRespHeaders(), HttpStatus.OK);
    }

    @RequestMapping(
            path = "/upload/{documentoId}/mobile/salvar",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE}
    )
    @ApiOperation(
            value = "Atualiza as imagens do docucmento.",
            response = DocumentoUploadResponse.class,
            notes = "Serviço responsável por confirmar o upload dos documentos, vai de fato persistir as imagens no documento do processo."
    )
    public ResponseEntity passo2Salvar(HttpServletRequest request, @PathVariable Long documentoId, @RequestBody RequestImagensDocumento requestImagensDocumento) throws Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        boolean ok = documentoServiceRest.passo2MobileConfirmaArquivos(sessaoHttpRequest.getUsuario(), documentoId, requestImagensDocumento);
        return new ResponseEntity(ok, ok ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }
}