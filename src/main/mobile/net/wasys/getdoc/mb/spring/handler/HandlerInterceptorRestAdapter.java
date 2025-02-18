package net.wasys.getdoc.mb.spring.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.reflect.FieldUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.UsuarioService;
import net.wasys.getdoc.mb.controller.MobileController;
import net.wasys.getdoc.mb.enumerator.DeviceHeader;
import net.wasys.getdoc.mb.utils.TypeUtils;

/**
 * 
 * HandlerInterceptorRestAdapter.java
 * @pascke
 */
public class HandlerInterceptorRestAdapter extends HandlerInterceptorAdapter {
		
	@Autowired private UsuarioService usuarioService;
	
	public HandlerInterceptorRestAdapter() {
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
	}
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		String uri = request.getRequestURI();
		if (StringUtils.contains(uri, "/rest/")) {
			if (handler instanceof HandlerMethod) {
				HandlerMethod handlerMethod = (HandlerMethod) handler;
				Object bean = handlerMethod.getBean();
				if (bean instanceof MobileController) {
					Long id = TypeUtils.parse(Long.class, request.getHeader(DeviceHeader.USER_ID.key));
					if (id != null) {
						Usuario usuario = usuarioService.get(id);
						if (usuario != null) {
							FieldUtils.writeField(bean, "usuario", usuario, true);
						}
					}
				}
			}
		}
		return super.preHandle(request, response, handler);
	}

	
	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		
		super.postHandle(request, response, handler, modelAndView);
	}
	
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		
		super.afterCompletion(request, response, handler, ex);
	}
}