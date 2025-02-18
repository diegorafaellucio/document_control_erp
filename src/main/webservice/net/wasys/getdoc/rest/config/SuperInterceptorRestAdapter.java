package net.wasys.getdoc.rest.config;

import lombok.extern.slf4j.Slf4j;
import net.wasys.getdoc.domain.entity.SessaoHttpRequest;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.GeralService;
import net.wasys.getdoc.domain.service.SessaoHttpRequestService;
import net.wasys.getdoc.domain.service.UsuarioService;
import net.wasys.getdoc.rest.exception.HTTP401Exception;
import net.wasys.util.faces.AbstractBean;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.security.Principal;

/**
 * @author jonas.baggio@wasys.com.br
 * @create 24 de jul de 2018 15:23:26
 */
public class SuperInterceptorRestAdapter extends HandlerInterceptorAdapter {

    @Autowired private SessaoHttpRequestService sessaoHttpRequestService;
    @Autowired private GeralService geralService;
    @Autowired private UsuarioService usuarioService;

    /**
     * Lista de paths que não precisar ser validados pelo interceptor.
     */
    private final String[] PATH_ABERTO = new String[]{
            "swagger",
            "/login",
            "/webjars",
            "/v2/api-docs",
            "/monitoramento/check",
            "/monitoramento-sia/check",
            "/licenciamento/check",
            "/usuario/autenticar",//autenticação do mobile
            "/get-customizacao",
            "/ocrcallback",
            "/is-logado",
            "/redefinir-senha",
            "/esqueci-minha-senha",
            "/valida-chave-redefinicao-senha"
    };

    public SuperInterceptorRestAdapter() {
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
	}
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws HTTP401Exception {

        //logger.info("========== START [" + request.getPathInfo() + "] ==========");
        request.setAttribute("HORA_INICIO", System.currentTimeMillis());

        /** Só vai validar alguma coisa que seja diferente das excecões mapeadas. */

        if(request.getMethod().equals("OPTIONS")){
            return true;
        }

        for (String path : PATH_ABERTO) {
            if (StringUtils.contains(request.getPathInfo(), path)) {
                return true;
            }
        }

        HttpSession session = request.getSession();
        Usuario usuario = (Usuario) session.getAttribute(AbstractBean.USUARIO_SESSION_KEY);
        if(usuario != null) {
            String login = usuario.getLogin();
            Principal userPrincipal = request.getUserPrincipal();
            if(userPrincipal != null) {
                String userPrincipalName = userPrincipal.getName();
                if(login.equals(userPrincipalName)) {
                    return true;
                }
            }
        }

        SessaoHttpRequest sessaoHttpRequest = sessaoHttpRequestService.findByJSessionId(request);
        if (sessaoHttpRequest == null) {
            throw new HTTP401Exception("http401.exception");
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        super.afterCompletion(request, response, handler, ex);
        Long timeMillis = (Long) request.getAttribute("HORA_INICIO");
        Long tempoRequisicao = System.currentTimeMillis() - timeMillis;
        //logger.info("========== END [" + request.getPathInfo() +"] Tempo: "+tempoRequisicao + " millis ==========");
    }
}