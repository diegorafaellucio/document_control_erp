package net.wasys.getdoc.rest.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.wasys.getdoc.domain.entity.SessaoHttpRequest;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.SessaoHttpRequestService;
import net.wasys.getdoc.domain.service.UsuarioSubperfilService;
import net.wasys.getdoc.rest.exception.HTTP401Exception;
import net.wasys.getdoc.rest.request.vo.RequestCadastrarUsuario;
import net.wasys.getdoc.rest.request.vo.RequestDesativarUsuario;
import net.wasys.getdoc.rest.request.vo.RequestFiltroUsuario;
import net.wasys.getdoc.rest.request.vo.RequestLogin;
import net.wasys.getdoc.rest.response.vo.*;
import net.wasys.getdoc.rest.service.UsuarioServiceRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author jonas.baggio@wasys.com.br
 * @create 24 de jul de 2018 15:18:53
 */
@CrossOrigin
@Controller
@RequestMapping("/usuario/v1")
@Api(tags = "/usuario", description = "Serviços relacionados ao Usuário.")
public class RestUsuarioV1 extends SuperController {

    @Autowired
    private UsuarioServiceRest usuarioServiceRest;
    @Autowired
    private SessaoHttpRequestService sessaoHttpRequestService;
    @Autowired
    private UsuarioSubperfilService usuarioSubperfilService;

    /**
     * @param request
     * @param response
     * @param requestLogin
     * @return
     * @throws Exception
     */
    @RequestMapping(
            path = "/login",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Autentica o usuário.",
            response = LoginResponse.class,
            notes = "Realiza autenticação do usuário através do realm."
    )
    public ResponseEntity login(HttpServletRequest request, @RequestBody RequestLogin requestLogin) throws Exception {

        SessaoHttpRequest sessaoHttpRequest = sessaoHttpRequestService.login(request, requestLogin);
        LoginResponse loginResponse = new LoginResponse(sessaoHttpRequest);
        return new ResponseEntity(loginResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/logoff",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})

    @ApiOperation(
            value = "Logoff do usuário.",
            notes = "Logoff usuario",
            response = UsuarioResponse.class
    )
    public ResponseEntity logoff(HttpServletRequest request) throws Exception {
		SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
		usuarioServiceRest.liberarProcessosBloqueados(sessaoHttpRequest.getUsuario());
        request.getSession().invalidate();
        return new ResponseEntity(HttpStatus.OK);
    }

    /**
     * Consulta os usuários com perfil analista
     *
     * @param request
     * @return
     */
    @RequestMapping(
            path = "/analistas",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta analistas.",
            notes = "Consulta todos os analistas com status ATIVO.",
            response = UsuarioResponse.class
    )
    public ResponseEntity getAnalistas(HttpServletRequest request) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<UsuarioResponse> listAnalistas = usuarioServiceRest.getAnalistas(sessaoHttpRequest.getUsuario());
        return new ResponseEntity(listAnalistas, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/subperfis",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta os subperfis disponíveis.",
            response = SubPerfilResponse[].class
    )
    public ResponseEntity getSubPerfis(HttpServletRequest request) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<SubPerfilResponse> list = usuarioServiceRest.getSubPerfis();
        return new ResponseEntity(list, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/subperfil-ativo",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Retorna o subperfil ativo do usuário logado.",
            response = SubPerfilResponse[].class
    )
    public ResponseEntity getSubPerfisAtivoUsuario(HttpServletRequest request) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        Usuario usuario = sessaoHttpRequest.getUsuario();
        List<SubPerfilResponse> list = usuarioServiceRest.getSubPerfilAtivoUsuario(usuario);
        return new ResponseEntity(list, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/subperfis-usuario/",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Retorna os subperfis do usuário logado.",
            response = SubPerfilResponse[].class
    )
    public ResponseEntity getSubPerfisUsuario(HttpServletRequest request) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        Usuario usuario = sessaoHttpRequest.getUsuario();
        List<SubPerfilResponse> list = usuarioServiceRest.getSubPerfisUsuarioEdit(usuario.getId());
        return new ResponseEntity(list, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/subperfis-usuario-edit/{id}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Retorna os subperfis de um usuário.",
            response = SubPerfilResponse[].class
    )
    public ResponseEntity getSubPerfisUsuarioEdit(@PathVariable Long id) throws HTTP401Exception {
        List<SubPerfilResponse> list = usuarioServiceRest.getSubPerfisUsuarioEdit(id);
        return new ResponseEntity(list, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/set-subperfis-atual/{subperfilId}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Retorna os subperfis do usuário logado.",
            response = SubPerfilResponse[].class
    )
    public ResponseEntity setSubperfilAtual(HttpServletRequest request, Long subperfilId) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        Usuario usuario = sessaoHttpRequest.getUsuario();

        //TODO fazer isso

        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(
            path = "/cadastrar",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Cadastrar novo usuário.",
            notes = "Cadastra um novo usuário."
    )
    public ResponseEntity cadastrar(HttpServletRequest request, @RequestBody RequestCadastrarUsuario requestCadastrarUsuario) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        CadastroUsuarioResponse cadastroUsuarioResponse = usuarioServiceRest.cadastrar(sessaoHttpRequest.getUsuario(), requestCadastrarUsuario);
        return new ResponseEntity(cadastroUsuarioResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/consultar",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consultar usuários á partir de um filtro.",
            response = ListaUsuarioResponse.class
    )
    public ResponseEntity consultar(HttpServletRequest request, @RequestBody RequestFiltroUsuario requestFiltroUsuario) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        ListaUsuarioResponse listaUsuarioResponse = usuarioServiceRest.consultar(sessaoHttpRequest.getUsuario(), requestFiltroUsuario);
        return new ResponseEntity(listaUsuarioResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/detalhar/{id}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Obtém os dados cadastrais de um usuário.",
            response = CadastroUsuarioResponse.class
    )
    public ResponseEntity detalhar(HttpServletRequest request, @PathVariable Long id) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        CadastroUsuarioResponse cadastroUsuarioResponse = usuarioServiceRest.detalhar(sessaoHttpRequest.getUsuario(), id);
        return new ResponseEntity(cadastroUsuarioResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/editar/{id}",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Altera os dados cadastrais do usuário.",
            response = CadastroUsuarioResponse.class
    )
    public ResponseEntity editar(HttpServletRequest request, @PathVariable Long id, @RequestBody RequestCadastrarUsuario requestCadastrarUsuario) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        CadastroUsuarioResponse cadastroUsuarioResponse= usuarioServiceRest.editar(sessaoHttpRequest.getUsuario(), id, requestCadastrarUsuario);
        return new ResponseEntity(cadastroUsuarioResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/excluir/{id}",
            method = RequestMethod.DELETE,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Excluir um usuário.",
            response = CadastroUsuarioResponse.class
    )
    public ResponseEntity excluir(HttpServletRequest request, @PathVariable Long id) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        boolean ok = usuarioServiceRest.excluir(sessaoHttpRequest.getUsuario(), id);
        return new ResponseEntity(null, ok ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(
            path = "/motivos-desativacao",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consultar os motivos para desativação.",
            response = MotivoDesativarUsuarioResponse.class
    )
    public ResponseEntity getMotivosDesativacao(HttpServletRequest request) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        List<MotivoDesativarUsuarioResponse> list = usuarioServiceRest.getMotivosDesativacao(sessaoHttpRequest.getUsuario());
        return new ResponseEntity(list, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/desativar/{id}",
            method = RequestMethod.PUT,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Desativa um usuário.",
            response = CadastroUsuarioResponse.class
    )
    public ResponseEntity desativar(HttpServletRequest request, @PathVariable Long id, @RequestBody RequestDesativarUsuario requestDesativarUsuario) throws Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        CadastroUsuarioResponse cadastroUsuarioResponse = usuarioServiceRest.desativar(sessaoHttpRequest.getUsuario(), id, requestDesativarUsuario);
        return new ResponseEntity(cadastroUsuarioResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/ativar/{id}",
            method = RequestMethod.PUT,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Ativa um usuário.",
            response = CadastroUsuarioResponse.class
    )
    public ResponseEntity ativar(HttpServletRequest request, @PathVariable Long id) throws Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        CadastroUsuarioResponse cadastroUsuarioResponse = usuarioServiceRest.ativar(sessaoHttpRequest.getUsuario(), id);
        return new ResponseEntity(cadastroUsuarioResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/reiniciar-senha/{id}",
            method = RequestMethod.PUT,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Reiniciar a senha de um usuário.",
            response = CadastroUsuarioResponse.class
    )
    public ResponseEntity reinicarSenha(HttpServletRequest request, @PathVariable Long id) throws Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        CadastroUsuarioResponse cadastroUsuarioResponse = usuarioServiceRest.reiniciarSenha(sessaoHttpRequest.getUsuario(), id);
        return new ResponseEntity(cadastroUsuarioResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/atualizar-senha",
            method = RequestMethod.PUT,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Reiniciar a senha de um usuário.",
            response = CadastroUsuarioResponse.class
    )
    public ResponseEntity atualizarSenha(HttpServletRequest request ,@RequestBody String novaSenha) throws Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        CadastroUsuarioResponse cadastroUsuarioResponse = usuarioServiceRest.atualizarSenha(sessaoHttpRequest.getUsuario(), novaSenha);
        return new ResponseEntity(cadastroUsuarioResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/prorrogar-acesso/{id}",
            method = RequestMethod.PUT,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Prorroga o acesso de um usuário.",
            response = CadastroUsuarioResponse.class
    )
    public ResponseEntity prorrogarAcesso(HttpServletRequest request, @PathVariable Long id) throws Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        CadastroUsuarioResponse cadastroUsuarioResponse = usuarioServiceRest.prorrogarAcesso(sessaoHttpRequest.getUsuario(), id);
        return new ResponseEntity(cadastroUsuarioResponse, HttpStatus.OK);
    }

    /**
     * Verifica se o usuario esta logado
     *
     * @param request
     * @return
     */
    @RequestMapping(
            path = "/is-logado",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Consulta se o usuario ainda esta logado.",
            notes = "Consulta se o usuario ainda esta logado.",
            response = UsuarioResponse.class
    )
    public ResponseEntity isLogado(HttpServletRequest request) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        LoginResponse loginResponse = new LoginResponse(sessaoHttpRequest);
        return new ResponseEntity(loginResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/trocar-subperfil",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Realiza a proca do subperfil.",
            response = String.class
    )
    public ResponseEntity trocarSubperfil(HttpServletRequest request, @RequestBody SubPerfilResponse subPerfilResponse) throws HTTP401Exception {
        SessaoHttpRequest sessaoHttpRequest = getSessaoHttpRequest(request);
        CadastroUsuarioResponse cadastroUsuarioResponse = usuarioServiceRest.trocarSubperfil(sessaoHttpRequest.getUsuario(), subPerfilResponse);
        LoginResponse loginResponse = new LoginResponse(sessaoHttpRequest);
        return new ResponseEntity(loginResponse, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/valida-chave-redefinicao-senha/{key}",
            method = RequestMethod.GET,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Valida a chave de redefinição de senha.",
            response = String.class
    )
    public ResponseEntity validaChaveRedefinicaoSenha(HttpServletRequest request, @PathVariable String key) {
        String login = usuarioServiceRest.validaChaveRedefinicaoSenha(key);
        return new ResponseEntity(login, HttpStatus.OK);
    }

    @RequestMapping(
            path = "/redefinir-senha",
            method = RequestMethod.POST,
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ApiOperation(
            value = "Redefine a seha.",
            response = String.class
    )
    public ResponseEntity redefinirSenha(HttpServletRequest request, @RequestBody RequestLogin requestLogin) throws HTTP401Exception {
        String login = usuarioServiceRest.redefinirSenha(requestLogin);
        return new ResponseEntity(login, HttpStatus.OK);
    }
}