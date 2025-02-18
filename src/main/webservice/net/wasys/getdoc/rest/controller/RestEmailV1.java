package net.wasys.getdoc.rest.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.wasys.getdoc.domain.entity.SessaoHttpRequest;
import net.wasys.getdoc.rest.exception.DadosObrigatorioRequestException;
import net.wasys.getdoc.rest.exception.HTTP401Exception;
import net.wasys.getdoc.rest.exception.ProcessoRestException;
import net.wasys.getdoc.rest.response.vo.EmailEnviadoResponse;
import net.wasys.getdoc.rest.response.vo.ListaEmailResponse;
import net.wasys.getdoc.rest.service.EmailServiceRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 *
 */
@CrossOrigin
@Controller
@RequestMapping("/email/v1")
@Api(tags = "/email", description = "Serviços relacionados ao envio/recebimento de emails.")
public class RestEmailV1 extends SuperController {

    @Autowired
    private EmailServiceRest emailServiceRest;

    @RequestMapping(
            path = "/consultar/{processoId}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta os emails.",
            response = ListaEmailResponse.class
    )
    public ResponseEntity getEmails(HttpServletRequest request, @PathVariable Long processoId) throws DadosObrigatorioRequestException, HTTP401Exception, ProcessoRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        ListaEmailResponse list = emailServiceRest.getEmails(sessaoHttpRequest.getUsuario(), processoId);
        return new ResponseEntity(list, HttpStatus.OK);
    }
}