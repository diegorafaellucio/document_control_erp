package net.wasys.getdoc.rest.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.wasys.getdoc.domain.entity.SessaoHttpRequest;
import net.wasys.getdoc.rest.exception.HTTP401Exception;
import net.wasys.getdoc.rest.response.vo.PermissaoTipoProcessoResponse;
import net.wasys.getdoc.rest.response.vo.SituacaoResponse;
import net.wasys.getdoc.rest.service.PermissaoServiceRest;
import net.wasys.getdoc.rest.service.SituacaoServiceRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 *
 */
@CrossOrigin
@Controller
@RequestMapping("/permissao/v1")
@Api(tags = "/permissao", description = "Serviços relacionados a permissão.")
public class RestPermissaoV1 extends SuperController {

    @Autowired
    private PermissaoServiceRest permissaoServiceRest;

    @RequestMapping(
            path = "/",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta as permissões.",
            response = PermissaoTipoProcessoResponse.class
    )
    public ResponseEntity get(HttpServletRequest request) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<PermissaoTipoProcessoResponse> permissaoTipoProcessoResponses = permissaoServiceRest.get(sessaoHttpRequest.getUsuario());
        return new ResponseEntity(permissaoTipoProcessoResponses, HttpStatus.OK);
    }
}