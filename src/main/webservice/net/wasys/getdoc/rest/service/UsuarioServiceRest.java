package net.wasys.getdoc.rest.service;

import lombok.extern.slf4j.Slf4j;

import net.wasys.getdoc.domain.entity.*;
import net.wasys.getdoc.domain.enumeration.MotivoDesativacaoUsuario;
import net.wasys.getdoc.domain.enumeration.RoleGD;
import net.wasys.getdoc.domain.enumeration.StatusUsuario;
import net.wasys.getdoc.domain.service.*;
import net.wasys.getdoc.domain.vo.filtro.UsuarioFiltro;
import net.wasys.getdoc.rest.exception.AreaRestException;
import net.wasys.getdoc.rest.exception.SubPerfilRestException;
import net.wasys.getdoc.rest.exception.TipoProcessoRestException;
import net.wasys.getdoc.rest.exception.UsuarioRestException;
import net.wasys.getdoc.rest.request.vo.RequestCadastrarUsuario;
import net.wasys.getdoc.rest.request.vo.RequestDesativarUsuario;
import net.wasys.getdoc.rest.request.vo.RequestFiltroUsuario;
import net.wasys.getdoc.rest.request.vo.RequestLogin;
import net.wasys.getdoc.rest.response.vo.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Novo service criado para centralizar as operaçõs que hoje são feitas no Bean JSF.
 */
@Service
public class UsuarioServiceRest extends SuperServiceRest{

    @Autowired private UsuarioService usuarioService;
    @Autowired private SubperfilService subperfilService;
    @Autowired private AreaService areaService;
    @Autowired private TipoProcessoService tipoProcessoService;
    @Autowired private BloqueioProcessoService bloqueioProcessoService;
    @Autowired private GeralService geralService;

    public List<UsuarioResponse> getAnalistas(Usuario usuario) {

        final List<UsuarioResponse> listAnalistas = new ArrayList<>();
        List<Usuario> analistas = usuarioService.findAnalistasAtivos();
        if (analistas != null && CollectionUtils.isNotEmpty(analistas)) {
            analistas.forEach(analista -> {
                listAnalistas.add(new UsuarioResponse(analista));
            });
        }
        return CollectionUtils.isNotEmpty(listAnalistas) ? listAnalistas : null;
    }

    public CadastroUsuarioResponse cadastrar(Usuario usuario, RequestCadastrarUsuario requestCadastrarUsuario) {
        validaRequestParameters(requestCadastrarUsuario);

        Usuario usuarioNovo = new Usuario();
        usuarioNovo = saveOrUpdate(usuario, requestCadastrarUsuario, usuarioNovo);
        return new CadastroUsuarioResponse(usuarioNovo);
    }

    public boolean enviarRedefinicaoDeSenha(String login) {
        if(StringUtils.isNotBlank(login)) {
            try {
                usuarioService.enviarRedefinicaoSenha(login);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        else {
            return false;
        }
    }

    private Usuario saveOrUpdate(Usuario usuarioLogado, RequestCadastrarUsuario requestCadastrarUsuario, Usuario usuarioNovo) {
        usuarioNovo.setNome(requestCadastrarUsuario.getNome());
        usuarioNovo.setLogin(requestCadastrarUsuario.getLogin());
        usuarioNovo.setEmail(requestCadastrarUsuario.getEmail());
        usuarioNovo.setRoleGD(requestCadastrarUsuario.getRoleGd());
        usuarioNovo.setTelefone(requestCadastrarUsuario.getTelefone());
        usuarioNovo.setNotificarAtrasoSolicitacoes(requestCadastrarUsuario.isNotificarAtrasoSolicitacao());
        usuarioNovo.setOrdemAtividadesFixa(requestCadastrarUsuario.isOrdemAtividadeFixa());
        usuarioNovo.setNotificarAtrasoRequisicoes(requestCadastrarUsuario.isNotificarAtrasoRequisicoes());

        List<String> tiposProcessosSelecionados = new ArrayList<>();
        List<String> subperfilsSelecionados = new ArrayList<>();

        if(requestCadastrarUsuario.getRoleGd() == RoleGD.GD_ANALISTA) {

            if(requestCadastrarUsuario.getSubperfilAtivoId() == null){
                throw new SubPerfilRestException("subperfil.nao.informado");
            }

            Subperfil subperfil = subperfilService.get(requestCadastrarUsuario.getSubperfilAtivoId());
            if(subperfil == null){
                throw new SubPerfilRestException("subperfil.nao.localizado.id", requestCadastrarUsuario.getSubperfilAtivoId());
            }
            usuarioNovo.setSubperfilAtivo(subperfil);

            if (CollectionUtils.isNotEmpty(requestCadastrarUsuario.getProcessoId())) {
                requestCadastrarUsuario.getProcessoId().forEach(tipoProcessoId -> {
                    TipoProcesso tipoProcesso = tipoProcessoService.get(tipoProcessoId);
                    if (tipoProcesso == null) {
                        throw new TipoProcessoRestException("tipo.processo.nao.localizado.id", tipoProcessoId);
                    }
                    tiposProcessosSelecionados.add(String.valueOf(tipoProcesso.getId()));
                });
            }
        }

        if(requestCadastrarUsuario.getRoleGd() == RoleGD.GD_GESTOR){
            Area area = areaService.get(requestCadastrarUsuario.getAreaId());
            if(area == null){
                throw new AreaRestException("area.nao.localizada");
            }

            usuarioNovo.setGestorArea(requestCadastrarUsuario.isGestorArea());
            usuarioNovo.setArea(area);
        }

        usuarioService.saveOrUpdate(usuarioNovo, tiposProcessosSelecionados, usuarioLogado, subperfilsSelecionados);
        return usuarioNovo;
    }

    public List<SubPerfilResponse> getSubPerfis() {
        List<SubPerfilResponse> list = new ArrayList<>();

        List<Subperfil> subperfils = subperfilService.findAll();
        if(CollectionUtils.isNotEmpty(subperfils)){
            subperfils.forEach(subperfil -> {
                list.add(new SubPerfilResponse(subperfil));
            });
        }
        return CollectionUtils.isNotEmpty(list) ? list : null;
    }

    public List<SubPerfilResponse> getSubPerfilAtivoUsuario(Usuario usuario) {

        List<SubPerfilResponse> list = new ArrayList<>();
        Subperfil subperfil = usuario.getSubperfilAtivo();
        if(subperfil != null) {
            list.add(new SubPerfilResponse(subperfil));
        }
        return CollectionUtils.isNotEmpty(list) ? list : null;
    }

    public List<SubPerfilResponse> getSubPerfisUsuarioEdit(Long id) {
        Usuario usuarioSalvo = usuarioService.get(id);

        if(usuarioSalvo == null){
            throw new UsuarioRestException("usuario.nao.localizado.com.id", id);
        }

        List<SubPerfilResponse> list = new ArrayList<>();
        Set<UsuarioSubperfil> subperfils = usuarioSalvo.getSubperfils();

        for(UsuarioSubperfil s: subperfils){
            SubPerfilResponse sr = new SubPerfilResponse(s.getSubperfil());
            list.add(sr);
        }

        return CollectionUtils.isNotEmpty(list) ? list : null;
    }

    public List<SubPerfilResponse> getSubPerfilsUsuario(Usuario usuario){

        List<SubPerfilResponse> list = new ArrayList<>();
        Set<UsuarioSubperfil> usuarioSubperfils = usuario.getSubperfils();
        for(UsuarioSubperfil us: usuarioSubperfils){
            Subperfil subperfil = us.getSubperfil();
            list.add(new SubPerfilResponse(subperfil));
        }

        return list;
    }

    public ListaUsuarioResponse consultar(Usuario usuario, RequestFiltroUsuario requestFiltroUsuario) {
        RoleGD roleGd = requestFiltroUsuario.getRoleGd();
        String login = requestFiltroUsuario.getLogin();
        String nome = requestFiltroUsuario.getNome();

        Subperfil subperfil = null;
        Long subperfilId = requestFiltroUsuario.getSubperfilId();
        if(subperfilId != null){
            subperfil = subperfilService.get(subperfilId);
        }

        UsuarioFiltro filtroAtivos = new UsuarioFiltro();
        filtroAtivos.setNome(nome);
        filtroAtivos.setLogin(login);
        filtroAtivos.setRole(roleGd);
        filtroAtivos.setUsuario(usuario);
        filtroAtivos.setSubperfil(subperfil);
        filtroAtivos.setStatus(StatusUsuario.ATIVO);

        UsuarioFiltro filtroBloqueados = filtroAtivos.clone();
        filtroBloqueados.setStatus(StatusUsuario.BLOQUEADO);

        UsuarioFiltro filtroInativos = filtroAtivos.clone();
        filtroInativos.setStatus(StatusUsuario.INATIVO);

        ListaUsuarioResponse listaUsuarioResponse = new ListaUsuarioResponse();

        List<Usuario> ativos = usuarioService.findByFiltro(filtroAtivos);
        ativos.forEach(u -> {
            listaUsuarioResponse.addAtivo(new FiltroUsuarioResponse(u));
        });

        List<Usuario> bloqueados = usuarioService.findByFiltro(filtroBloqueados);
        bloqueados.forEach(u -> {
            listaUsuarioResponse.addBloqueado(new FiltroUsuarioResponse(u));
        });

        List<Usuario> inativos = usuarioService.findByFiltro(filtroInativos);
        inativos.forEach(u -> {
            listaUsuarioResponse.addInativo(new FiltroUsuarioResponse(u));
        });

        return listaUsuarioResponse;
    }

    public CadastroUsuarioResponse detalhar(Usuario usuario, Long id) {
        Usuario usuarioSalvo = usuarioService.get(id);
        if(usuarioSalvo == null){
            throw new UsuarioRestException("usuario.nao.localizado.com.id", id);
        }

        return new CadastroUsuarioResponse(usuarioSalvo);
    }

    public CadastroUsuarioResponse editar(Usuario usuario, Long id, RequestCadastrarUsuario requestCadastrarUsuario) {
        Usuario usuarioSalvo = usuarioService.get(id);
        if(usuarioSalvo == null){
            throw new UsuarioRestException("usuario.nao.localizado.com.id", id);
        }
        usuarioSalvo = saveOrUpdate(usuario, requestCadastrarUsuario, usuarioSalvo);
        return new CadastroUsuarioResponse(usuarioSalvo);
    }

    public boolean excluir(Usuario usuario, Long id) {
        Usuario usuarioSalvo = usuarioService.get(id);
        if(usuarioSalvo == null){
            throw new UsuarioRestException("usuario.nao.localizado.com.id", id);
        }
        usuarioService.excluir(id, usuario);
        return true;
    }

    public List<MotivoDesativarUsuarioResponse> getMotivosDesativacao(Usuario usuario) {
        List<MotivoDesativarUsuarioResponse> list = new ArrayList<>();
        for (MotivoDesativacaoUsuario value : MotivoDesativacaoUsuario.values()) {

            String descricao = messageService.getValue("MotivoDesativacaoUsuario."+value.name()+".label");
            if(StringUtils.isEmpty(descricao)){
                descricao = value.name();
            }

            list.add(new MotivoDesativarUsuarioResponse(value, descricao));
        }
        return list;
    }

    public CadastroUsuarioResponse desativar(Usuario usuario, Long id, RequestDesativarUsuario requestDesativarUsuario) throws Exception {
        Usuario usuarioSalvo = usuarioService.get(id);
        if(usuarioSalvo == null){
            throw new UsuarioRestException("usuario.nao.localizado.com.id", id);
        }
        usuarioSalvo.setMotivoDesativacao(requestDesativarUsuario.getMotivo());
        usuarioService.desativarUsuario(usuarioSalvo, usuario);


        String descricao = messageService.getValue("MotivoDesativacaoUsuario."+requestDesativarUsuario.getMotivo().name()+".label");
        if(StringUtils.isEmpty(descricao)){
            descricao = requestDesativarUsuario.getMotivo().name();
        }

        CadastroUsuarioResponse cadastroUsuarioResponse = new CadastroUsuarioResponse(usuario);
        cadastroUsuarioResponse.setMotivoDesativacao(new MotivoDesativarUsuarioResponse(requestDesativarUsuario.getMotivo(), descricao ));
        return cadastroUsuarioResponse;
    }

    public CadastroUsuarioResponse ativar(Usuario usuario, Long id) throws Exception {
        Usuario usuarioSalvo = usuarioService.get(id);
        if(usuarioSalvo == null){
            throw new UsuarioRestException("usuario.nao.localizado.com.id", id);
        }
        usuarioService.ativarUsuario(usuarioSalvo, usuario);
        CadastroUsuarioResponse cadastroUsuarioResponse = new CadastroUsuarioResponse(usuario);
        return cadastroUsuarioResponse;
    }

    public CadastroUsuarioResponse reiniciarSenha(Usuario usuario, Long id) throws Exception {
        Usuario usuarioSalvo = usuarioService.get(id);
        if(usuarioSalvo == null){
            throw new UsuarioRestException("usuario.nao.localizado.com.id", id);
        }
        usuarioService.reiniciarSenha(usuarioSalvo, usuario);
        CadastroUsuarioResponse cadastroUsuarioResponse = new CadastroUsuarioResponse(usuario);
        return cadastroUsuarioResponse;
    }

    public CadastroUsuarioResponse atualizarSenha(Usuario usuario, String novaSenha) throws Exception {
        String login = usuario.getLogin();
        if(StringUtils.isBlank(login)){
            throw new UsuarioRestException("usuario.login.invalido");
        }
        usuarioService.atualizarSenha(login, novaSenha);
        CadastroUsuarioResponse cadastroUsuarioResponse = new CadastroUsuarioResponse(usuario);
        return cadastroUsuarioResponse;
    }

    public CadastroUsuarioResponse prorrogarAcesso(Usuario usuario, Long id) throws Exception {
        Usuario usuarioSalvo = usuarioService.get(id);
        if(usuarioSalvo == null){
            throw new UsuarioRestException("usuario.nao.localizado.com.id", id);
        }
        usuarioService.prorrogarAcesso(usuarioSalvo, usuario);
        CadastroUsuarioResponse cadastroUsuarioResponse = new CadastroUsuarioResponse(usuario);
        return cadastroUsuarioResponse;
    }

    public CadastroUsuarioResponse trocarSubperfil(Usuario usuario, SubPerfilResponse subPerfilResponse) {
        Long perfilId = subPerfilResponse.getId();
        usuarioService.trocarSubperfil(usuario, perfilId);
        CadastroUsuarioResponse cadastroUsuarioResponse = new CadastroUsuarioResponse(usuario);
        return cadastroUsuarioResponse;
    }

    public void liberarProcessosBloqueados(Usuario usuario) {
        Long id = usuario.getId();
        bloqueioProcessoService.desbloquearByUsuario(id);
    }

    public String validaChaveRedefinicaoSenha(String key) {
        return usuarioService.validaChaveRedefinicaoSenha(key);
    }

    public String redefinirSenha(RequestLogin requestLogin) {
        String login = requestLogin.getLogin();
        String novaSenha = requestLogin.getSenha();

        Usuario usuario = null;
        try {
            usuario = usuarioService.atualizarSenha(login, novaSenha);
        } catch (Exception e) {
            e.printStackTrace();
            throw new UsuarioRestException("erro.redefinir.senha");
        }
        return usuario.getLogin();
    }
}