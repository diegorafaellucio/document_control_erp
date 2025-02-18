package net.wasys.getdoc.rest.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.wasys.getdoc.domain.entity.SessaoHttpRequest;
import net.wasys.getdoc.rest.exception.DadosObrigatorioRequestException;
import net.wasys.getdoc.rest.exception.HTTP401Exception;
import net.wasys.getdoc.rest.exception.TipoCampoRestException;
import net.wasys.getdoc.rest.exception.TipoProcessoRestException;
import net.wasys.getdoc.rest.request.vo.RequestCadastroTipoCampo;
import net.wasys.getdoc.rest.request.vo.RequestCadastroTipoCampoGrupo;
import net.wasys.getdoc.rest.response.vo.CampoResponse;
import net.wasys.getdoc.rest.response.vo.GrupoCamposResponse;
import net.wasys.getdoc.rest.response.vo.ListaTipoCampoResponse;
import net.wasys.getdoc.rest.response.vo.TipoCampoGrupoResponse;
import net.wasys.getdoc.rest.service.TipoCampoServiceRest;
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
@RequestMapping("/tipo-campo/v1")
@Api(tags = "/tipo-campo", description = "Serviços relacionados ao TipoCampo.")
public class RestTipoCampoV1 extends SuperController {

    @Autowired
    private TipoCampoServiceRest tipoCampoServiceRest;

    @RequestMapping(
            path = "/tipo-processo/{tipoProcessoId}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta as informações resumidas para popular o combo de tipos de processo.",
            response = ListaTipoCampoResponse.class
    )
    public ResponseEntity getCamposByTipoProcesso(HttpServletRequest request, @PathVariable Long tipoProcessoId) throws HTTP401Exception, TipoProcessoRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        ListaTipoCampoResponse listaTipoCampoResponse = tipoCampoServiceRest.findByTipoProcesso(sessaoHttpRequest.getUsuario(), tipoProcessoId);
        return new ResponseEntity(listaTipoCampoResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/tipo-processo/{tipoProcessoId}/adicionar-campo",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Adiciona um novo campo no tipo de processo.",
            response = CampoResponse.class
    )
    public ResponseEntity adicionarTipoCampo(HttpServletRequest request, @PathVariable Long tipoProcessoId, @RequestBody RequestCadastroTipoCampo requestCadastroTipoCampo) throws HTTP401Exception, TipoProcessoRestException, TipoCampoRestException, DadosObrigatorioRequestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        CampoResponse campoResponse= tipoCampoServiceRest.adicionarTipoCampo(sessaoHttpRequest.getUsuario(), tipoProcessoId, requestCadastroTipoCampo);
        return new ResponseEntity(campoResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/editar-campo/{tipoCampoId}",
            method = RequestMethod.PUT,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Edita um TipoCampoGrupo.",
            response = TipoCampoGrupoResponse.class
    )
    public ResponseEntity editarGrupoCampo(HttpServletRequest request, @PathVariable Long tipoCampoId, @RequestBody RequestCadastroTipoCampo requestCadastroTipoCampo) throws HTTP401Exception, TipoProcessoRestException, TipoCampoRestException, DadosObrigatorioRequestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        CampoResponse campoResponse = tipoCampoServiceRest.editarCampo(sessaoHttpRequest.getUsuario(), tipoCampoId, requestCadastroTipoCampo);
        return new ResponseEntity(campoResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/detalhar-campo/{tipoCampoId}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Obtém os dados de um TipoCampo.",
            response = CampoResponse.class
    )
    public ResponseEntity detalharTipoCampo(HttpServletRequest request, @PathVariable Long tipoCampoId) throws HTTP401Exception, TipoProcessoRestException, TipoCampoRestException, DadosObrigatorioRequestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        CampoResponse campoResponse = tipoCampoServiceRest.detalharTipoCampo(sessaoHttpRequest.getUsuario(), tipoCampoId);
        return new ResponseEntity(campoResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/tipo-processo/{tipoProcessoId}/adicionar-grupo",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Adiciona um novo grupo de campo no tipo de processo.",
            response = TipoCampoGrupoResponse.class
    )
    public ResponseEntity adicionarGrupoCampo(HttpServletRequest request, @PathVariable Long tipoProcessoId, @RequestBody RequestCadastroTipoCampoGrupo requestCadastroGrupoCampo) throws HTTP401Exception, TipoProcessoRestException, TipoCampoRestException, DadosObrigatorioRequestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        TipoCampoGrupoResponse tipoCampoGrupoResponse = tipoCampoServiceRest.adicionarGrupoCampo(sessaoHttpRequest.getUsuario(), tipoProcessoId, requestCadastroGrupoCampo);
        return new ResponseEntity(tipoCampoGrupoResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/editar-grupo/{tipoGrupoCampoId}",
            method = RequestMethod.PUT,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Edita um TipoCampoGrupo.",
            response = TipoCampoGrupoResponse.class
    )
    public ResponseEntity editarGrupoCampo(HttpServletRequest request, @PathVariable Long tipoGrupoCampoId, @RequestBody RequestCadastroTipoCampoGrupo requestCadastroGrupoCampo) throws HTTP401Exception, TipoProcessoRestException, TipoCampoRestException, DadosObrigatorioRequestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        TipoCampoGrupoResponse tipoCampoGrupoResponse = tipoCampoServiceRest.editarGrupoCampo(sessaoHttpRequest.getUsuario(), tipoGrupoCampoId, requestCadastroGrupoCampo);
        return new ResponseEntity(tipoCampoGrupoResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/grupo/{tipoGrupoCampoId}",
            method = RequestMethod.DELETE,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Exclui um TipoCampoGrupo."
    )
    public ResponseEntity excluirGrupoCampo(HttpServletRequest request, @PathVariable Long tipoGrupoCampoId) throws HTTP401Exception, TipoProcessoRestException, TipoCampoRestException, DadosObrigatorioRequestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        boolean ok = tipoCampoServiceRest.excluirGrupoCampo(sessaoHttpRequest.getUsuario(), tipoGrupoCampoId);
        return new ResponseEntity(null, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/campo/{tipoCampoId}",
            method = RequestMethod.DELETE,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Exclui um TipoCampoGrupo."
    )
    public ResponseEntity excluirCampo(HttpServletRequest request, @PathVariable Long tipoCampoId) throws HTTP401Exception, TipoProcessoRestException, TipoCampoRestException, DadosObrigatorioRequestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        boolean ok = tipoCampoServiceRest.excluirCampo(sessaoHttpRequest.getUsuario(), tipoCampoId);
        return new ResponseEntity(null, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/detalhar-grupo/{tipoCampoGrupoId}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Obtém os dados de um TipoCampo.",
            response = TipoCampoGrupoResponse.class
    )
    public ResponseEntity detalharTipoCampoGrupo(HttpServletRequest request, @PathVariable Long tipoCampoGrupoId) throws HTTP401Exception, TipoProcessoRestException, TipoCampoRestException, DadosObrigatorioRequestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        TipoCampoGrupoResponse tipoCampoGrupoResponse  = tipoCampoServiceRest.detalharTipoCampoGrupo(sessaoHttpRequest.getUsuario(), tipoCampoGrupoId);
        return new ResponseEntity(tipoCampoGrupoResponse, HttpStatus.OK);
    }
}