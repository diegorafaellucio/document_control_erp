package net.wasys.getdoc.rest.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.wasys.getdoc.domain.entity.SessaoHttpRequest;
import net.wasys.getdoc.rest.exception.HTTP401Exception;
import net.wasys.getdoc.rest.exception.SubPerfilRestException;
import net.wasys.getdoc.rest.request.vo.RequestCadastroSubperfil;
import net.wasys.getdoc.rest.response.vo.DetalhesSubPerfilResponse;
import net.wasys.getdoc.rest.response.vo.ListaSituacaoTipoProcesso;
import net.wasys.getdoc.rest.response.vo.SubPerfilResponse;
import net.wasys.getdoc.rest.response.vo.SubperfilTipoProcessoResponse;
import net.wasys.getdoc.rest.service.SubperfilServiceRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Set;

@CrossOrigin
@Controller
@RequestMapping("/subperfil/v1")
@Api(tags = "/subperfil", description = "Serviços relacionados ao Subperfil.")
public class RestSubperfilV1 extends SuperController {

    @Autowired
    private SubperfilServiceRest subperfilServiceRest;

    @RequestMapping(
            path = "/{min}/{max}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta os subperfis cadastrados.",
            response = DetalhesSubPerfilResponse.class
    )
    public ResponseEntity getAll(HttpServletRequest request, @PathVariable int min, @PathVariable int max) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<DetalhesSubPerfilResponse> list = subperfilServiceRest.getSubperfis(sessaoHttpRequest.getUsuario(), min, max);
        return new ResponseEntity(list, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/detalhar/{id}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Obtém os dados do feriado.",
            response = DetalhesSubPerfilResponse.class
    )
    public ResponseEntity detalhar(HttpServletRequest request, @PathVariable Long id) throws HTTP401Exception, SubPerfilRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        DetalhesSubPerfilResponse detalhesSubPerfilResponse = subperfilServiceRest.detalhar(sessaoHttpRequest.getUsuario(), id);
        return new ResponseEntity(detalhesSubPerfilResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/situacoes-tipo-processo",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Obtém as situações agrupada por tipo processo.",
            response = SubperfilTipoProcessoResponse.class
    )
    public ResponseEntity getSituacoesTipoProcesso(HttpServletRequest request) throws HTTP401Exception, SubPerfilRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        Set<SubperfilTipoProcessoResponse> situacoesTipoProcesso = subperfilServiceRest.getSituacoesTipoProcesso(sessaoHttpRequest.getUsuario());
        return new ResponseEntity(situacoesTipoProcesso, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/cadastrar",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Cadastrar um novo subperfil.",
            response = DetalhesSubPerfilResponse.class
    )
    public ResponseEntity cadastrar(HttpServletRequest request, @RequestBody RequestCadastroSubperfil requestCadastroSubperfil) throws HTTP401Exception, SubPerfilRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        DetalhesSubPerfilResponse subPerfilResponse = subperfilServiceRest.cadastrar(sessaoHttpRequest.getUsuario(), requestCadastroSubperfil);
        return new ResponseEntity(subPerfilResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/editar/{id}",
            method = RequestMethod.PUT,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Editar um feriado.",
            response = DetalhesSubPerfilResponse.class
    )
    public ResponseEntity editar(HttpServletRequest request, @PathVariable Long id, @RequestBody RequestCadastroSubperfil requestCadastroSubperfil) throws HTTP401Exception, SubPerfilRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        DetalhesSubPerfilResponse subPerfilResponse = subperfilServiceRest.editar(sessaoHttpRequest.getUsuario(), id, requestCadastroSubperfil);
        return new ResponseEntity(subPerfilResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/excluir/{id}",
            method = RequestMethod.DELETE,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Editar um feriado."
    )
    public ResponseEntity excluir(HttpServletRequest request, @PathVariable Long id) throws HTTP401Exception, SubPerfilRestException {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        boolean excluir = subperfilServiceRest.excluir(sessaoHttpRequest.getUsuario(), id);
        return new ResponseEntity(null, excluir ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

}