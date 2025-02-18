package net.wasys.getdoc.rest.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.wasys.getdoc.domain.entity.SessaoHttpRequest;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.rest.request.vo.RequestHelloCinq;
import net.wasys.getdoc.rest.response.vo.HelloCinqResponse;
import net.wasys.util.DummyUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

@CrossOrigin
@Controller
@RequestMapping("/cinq/teste/v1")
@Api(tags = "/cinq", description = "Serviços de teste, exclusivos para uso da Cinq.")
public class RestCinqV1 extends SuperController {

    @RequestMapping(path = "/hello", method = RequestMethod.POST, produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(value = "Teste hello cinq", response = HelloCinqResponse.class)
    public ResponseEntity<HelloCinqResponse> cadastrar(HttpServletRequest request, @RequestBody RequestHelloCinq requestHelloCinq) throws Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        Usuario usuario = sessaoHttpRequest.getUsuario();

		HelloCinqResponse helloCinqResponse = new HelloCinqResponse("Teste executado com sucesso pela " + requestHelloCinq.getNome() + " às " + DummyUtils.formatDateTime2(new Date()));
        return new ResponseEntity(helloCinqResponse, HttpStatus.OK);
    }
}