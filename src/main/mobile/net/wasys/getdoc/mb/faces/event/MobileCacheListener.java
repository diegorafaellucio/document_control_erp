package net.wasys.getdoc.mb.faces.event;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.ServletContext;

import org.apache.commons.lang.StringUtils;
import org.omnifaces.util.Faces;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import net.wasys.getdoc.domain.service.ParametroService;

public class MobileCacheListener implements PhaseListener {
	
	//@Autowired 
	private ParametroService parametroService;
	
	public MobileCacheListener() {
		//SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
	}
	
	@Override
	public PhaseId getPhaseId() {
		return PhaseId.RENDER_RESPONSE;
	}

	@Override
	public void afterPhase(PhaseEvent event) {
		
	}

	@Override
	public void beforePhase(PhaseEvent event) {
		String uri = Faces.getRequestURI();
		if (StringUtils.contains(uri, "/mobile/")) {
			ServletContext servletContext = Faces.getServletContext();
			WebApplicationContext cc = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
			parametroService = cc.getBean(ParametroService.class);
			Integer number = parametroService.getMobileCacheVersion();
			if (number == null) {
				number = 1;
			}
			Faces.addResponseHeader("CacheVersion", String.valueOf(number));
		}
	}
}
