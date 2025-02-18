package net.wasys.getdoc.rest.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.wasys.getdoc.domain.entity.SessaoHttpRequest;
import net.wasys.getdoc.domain.service.ParametroService;
import net.wasys.getdoc.rest.exception.HTTP401Exception;
import net.wasys.getdoc.rest.request.vo.RequestConfiguracaoLayout;
import net.wasys.getdoc.rest.request.vo.RequestHorarioExpedientePermitido;
import net.wasys.getdoc.rest.response.vo.ConfiguracaoLayoutResponse;
import net.wasys.getdoc.rest.response.vo.FilaConfiguracaoResponse;
import net.wasys.getdoc.rest.response.vo.HorarioExpedientePermitidoResponse;
import net.wasys.getdoc.rest.service.ParametroServiceRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

@CrossOrigin
@Controller
@RequestMapping("/configuracoes/v1")
@Api(tags = "/configuracoes", description = "Serviços relacionados as Configuraçoes.")
public class RestConfiguracoesV1 extends SuperController {

    @Autowired
    private ParametroServiceRest parametroServiceRest;

    @RequestMapping(
            path = "/horario-expediente",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta os parâmetros relacionados ao expediente."
    )
    public ResponseEntity getHorarioExpediente(HttpServletRequest request) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        Map<ParametroService.P, String> mapHorarioExpediente = parametroServiceRest.getHorarioExpediente(sessaoHttpRequest.getUsuario());
        return new ResponseEntity(mapHorarioExpediente, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/horario-acesso",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta os parâmetros relacionados aos horários permitidos para acesso."
    )
    public ResponseEntity getHorarioPermitidoAcesso(HttpServletRequest request) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        Map<ParametroService.P, String> mapHorarioExpediente = parametroServiceRest.getHorarioAcesso(sessaoHttpRequest.getUsuario());
        return new ResponseEntity(mapHorarioExpediente, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/horario-expediente-permitido",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta os parâmetros relacionados ao expediente e ao horario permitido em uma unica chamada."
    )
    public ResponseEntity getHorarioExpedientePermitido(HttpServletRequest request) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        HorarioExpedientePermitidoResponse response = parametroServiceRest.getHorarioExpedientePermitido(sessaoHttpRequest.getUsuario());
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/atualizar-expediente-permitido",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Alterar horário do expediente e o horario permitido em uma unica chamada."
    )
    public ResponseEntity alterarHorarioExpedientePermitido(HttpServletRequest request, @RequestBody RequestHorarioExpedientePermitido requestHorarioExpedientePermitido) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        HorarioExpedientePermitidoResponse response = parametroServiceRest.atualizarParametros(sessaoHttpRequest.getUsuario(), requestHorarioExpedientePermitido);
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/cadastrar-customizacao",
           // consumes = "multipart/form-data",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Salva parametros de layout cor e imagem."
    )
    public ResponseEntity cadastrarCustomizacao(HttpServletRequest request, @RequestBody RequestConfiguracaoLayout requestConfiguracaoLayout) throws HTTP401Exception, IOException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        ConfiguracaoLayoutResponse response = parametroServiceRest.cadastrarCustomizacao(sessaoHttpRequest.getUsuario(), requestConfiguracaoLayout);
        return new ResponseEntity(response, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/get-customizacao",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "recupera parametros de layout cor e imagem."
    )
    public ResponseEntity getCustomizacao() throws HTTP401Exception, IOException {

        ConfiguracaoLayoutResponse response = parametroServiceRest.getCustomizacao();
        return new ResponseEntity(response, HttpStatus.OK);
    }

	@RequestMapping(
			path = "/cadastrar-logo",
            consumes = "multipart/form-data",
			method = RequestMethod.POST,
			produces = {MediaType.APPLICATION_JSON_VALUE})
	@ApiOperation(
			value = "Salva parametros de logo"
	)
	public ResponseEntity cadastrarLogo(MultipartHttpServletRequest request, @RequestParam("file") MultipartFile multipartFile) throws HTTP401Exception, IOException {
		SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
		parametroServiceRest.cadastrarLogo(sessaoHttpRequest.getUsuario(), multipartFile);
		return new ResponseEntity(HttpStatus.OK);
	}

    @RequestMapping(
            path = "/set-customizacao-default",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Salva parametros de layout cor e imagem default."
    )
    public ResponseEntity setCustomizacaoDefault(HttpServletRequest request, @RequestBody RequestConfiguracaoLayout requestConfiguracaoLayout) throws HTTP401Exception, IOException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        ConfiguracaoLayoutResponse response = parametroServiceRest.cadastrarCustomizacaoDefault(sessaoHttpRequest.getUsuario(), requestConfiguracaoLayout);
        return new ResponseEntity(response, HttpStatus.OK);
    }

}