package net.wasys.getdoc.rest.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.wasys.getdoc.domain.entity.SessaoHttpRequest;
import net.wasys.getdoc.rest.exception.*;
import net.wasys.getdoc.rest.request.vo.RequestFiltrarAnexos;
import net.wasys.getdoc.rest.response.vo.AnexoResponse;
import net.wasys.getdoc.rest.response.vo.DownloadAnexoResponse;
import net.wasys.getdoc.rest.service.AnexoServiceRest;
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
@RequestMapping("/anexo/v1")
@Api(tags = "/anexo", description = "Serviços relacionados ao Anexo.")
public class RestAnexoV1 extends SuperController {

    @Autowired
    private AnexoServiceRest anexoServiceRest;

    @RequestMapping(
            path = "/processo/{processoId}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta os anexos vinculadas ao processo.",
            response = AnexoResponse.class
    )
    public ResponseEntity getAnexos(HttpServletRequest request, @PathVariable Long processoId) throws DadosObrigatorioRequestException, HTTP401Exception, ProcessoRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<AnexoResponse> anexos = anexoServiceRest.getAnexos(sessaoHttpRequest.getUsuario(), processoId);
        return new ResponseEntity(anexos, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/download/{processoLogAnexoId}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE})
    @ApiOperation(
            value = "Faz o download do arquivo do anexo."
    )
    public ResponseEntity download(HttpServletRequest request, @PathVariable Long processoLogAnexoId) throws DadosObrigatorioRequestException, HTTP401Exception, AnexoRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        DownloadAnexoResponse download = anexoServiceRest.download(sessaoHttpRequest.getUsuario(), processoLogAnexoId);
        return new ResponseEntity(download.getIsr(), download.getRespHeaders(), HttpStatus.OK);
    }

    @RequestMapping(
            path = "/processo/{processoId}/filtrar",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta os anexos vinculadas ao processo.",
            response = AnexoResponse.class
    )
    public ResponseEntity getAnexos(HttpServletRequest request, @PathVariable Long processoId, @RequestBody RequestFiltrarAnexos requestFiltrarAnexos) throws DadosObrigatorioRequestException, HTTP401Exception, ProcessoRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<AnexoResponse> anexos = anexoServiceRest.filtrarAnexos(sessaoHttpRequest.getUsuario(), processoId, requestFiltrarAnexos);
        return new ResponseEntity(anexos, HttpStatus.OK);
    }
}