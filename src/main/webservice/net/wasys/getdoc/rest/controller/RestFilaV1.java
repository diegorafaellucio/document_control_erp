package net.wasys.getdoc.rest.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.wasys.getdoc.domain.entity.SessaoHttpRequest;
import net.wasys.getdoc.rest.exception.FilaRestException;
import net.wasys.getdoc.rest.exception.HTTP401Exception;
import net.wasys.getdoc.rest.request.vo.RequestFilaConfiguracao;
import net.wasys.getdoc.rest.response.vo.FilaConfiguracaoResponse;
import net.wasys.getdoc.rest.service.FilaServiceRest;
import net.wasys.getdoc.rest.service.ParametroServiceRest;
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
@RequestMapping("/fila/v1")
@Api(tags = "/fila", description = "Serviços relacionados a cadastro de configurações da fila.")
public class RestFilaV1 extends SuperController {

    @Autowired
    private FilaServiceRest filaServiceRest;

    @RequestMapping(
            path = "/fila-trabalho",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta os parâmetros da fila de trabalho.",
            response = FilaConfiguracaoResponse.class
    )
    public ResponseEntity getConfiguracoesFilaTrabalho(HttpServletRequest request) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<FilaConfiguracaoResponse> response = filaServiceRest.getFilaTrabalho(sessaoHttpRequest.getUsuario());
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/cadastrar",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Cadastrar uma nova fila.",
            response = FilaConfiguracaoResponse.class
    )
    public ResponseEntity cadastrar(HttpServletRequest request, @RequestBody RequestFilaConfiguracao requestFilaConfiguracao) throws HTTP401Exception, FilaRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        FilaConfiguracaoResponse response = filaServiceRest.cadastrar(sessaoHttpRequest.getUsuario(), requestFilaConfiguracao);
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/editar/{id}",
            method = RequestMethod.PUT,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Editar uma fila.",
            response = FilaConfiguracaoResponse.class
    )
    public ResponseEntity editar(HttpServletRequest request, @PathVariable Long id, @RequestBody RequestFilaConfiguracao requestFilaConfiguracao) throws HTTP401Exception, FilaRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        FilaConfiguracaoResponse response = filaServiceRest.editar(sessaoHttpRequest.getUsuario(), id, requestFilaConfiguracao);
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/excluir/{id}",
            method = RequestMethod.DELETE,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Exclui uma fila.",
            response = Boolean.class
    )
    public ResponseEntity excluir(HttpServletRequest request, @PathVariable Long id) throws HTTP401Exception, FilaRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        boolean excluir = filaServiceRest.excluir(sessaoHttpRequest.getUsuario(), id);
        return new ResponseEntity(null, excluir ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }


}