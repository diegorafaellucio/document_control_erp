package net.wasys.getdoc.mb.faces.event;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.ServletContext;

import net.wasys.getdoc.mb.bean.MobileBean;
import net.wasys.util.faces.AbstractBean;
import org.apache.commons.lang.StringUtils;
import org.omnifaces.util.Faces;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.UsuarioService;
import net.wasys.getdoc.mb.enumerator.DeviceHeader;
import net.wasys.getdoc.mb.utils.TypeUtils;

public class MobileAuthorizationListener implements PhaseListener {

	/*@Autowired*/
	private UsuarioService usuarioService;

	public MobileAuthorizationListener() {		
		//SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
	}

	@Override
	public PhaseId getPhaseId() {
		return PhaseId.RESTORE_VIEW;
	}

	@Override
	public void afterPhase(PhaseEvent event) {
		String uri = Faces.getRequestURI();
		if (StringUtils.contains(uri, "/mobile/")) {
			String value = Faces.getRequestHeader(DeviceHeader.USER_ID.key);
			Long id = TypeUtils.parse(Long.class, value);
			if (id != null) {
				ServletContext servletContext = Faces.getServletContext();
				WebApplicationContext cc = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
				usuarioService = cc.getBean(UsuarioService.class);
				Usuario usuario = usuarioService.get(id);
				if (usuario != null) {
					Faces.setRequestAttribute(MobileBean.USUARIO_LOGADO_MOBILE_KEY, usuario);
				}
			}
			else {
				Object usuario = Faces.getSessionAttribute(AbstractBean.USUARIO_SESSION_KEY);
				Faces.setRequestAttribute(MobileBean.USUARIO_LOGADO_MOBILE_KEY, usuario);
			}
		}
	}

	@Override
	public void beforePhase(PhaseEvent arg0) {

	}
}
