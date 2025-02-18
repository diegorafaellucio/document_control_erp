package net.wasys.util.ocrws;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.wasys.util.rest.dto.ErrorDTO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.wasys.getdoc.domain.service.MessageService;
import net.wasys.util.rest.AbstractController;

public class OcrWsHandlerInterceptorAdapter extends HandlerInterceptorAdapter {

	@Autowired private MessageService messageService;

	public OcrWsHandlerInterceptorAdapter() {
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		String uri = request.getRequestURI();
		if (StringUtils.contains(uri, "/rest/") && handler instanceof HandlerMethod) {

			HandlerMethod handlerMethod = (HandlerMethod) handler;
			Object bean = handlerMethod.getBean();

			if (bean instanceof AbstractController) {

				AbstractController ac = (AbstractController) bean;
				if(!ac.ifSecurityOk()) {

					ErrorDTO errorDTO = new ErrorDTO();
					HttpStatus status = HttpStatus.UNAUTHORIZED;
					errorDTO.status = status;
					String msg = messageService.getValue("falhaAutenticacao.error");
					errorDTO.addMessage(msg);

					response.setContentType(MediaType.APPLICATION_JSON_VALUE);
					response.setStatus(status.value());

					ServletOutputStream outputStream = response.getOutputStream();
					ObjectMapper objectMapper = new ObjectMapper();
					objectMapper.writeValue(outputStream, errorDTO);

					return false;
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