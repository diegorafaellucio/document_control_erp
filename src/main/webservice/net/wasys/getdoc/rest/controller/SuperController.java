package net.wasys.getdoc.rest.controller;

import net.wasys.getdoc.domain.entity.SessaoHttpRequest;
import net.wasys.getdoc.domain.enumeration.Origem;
import net.wasys.getdoc.domain.service.SessaoHttpRequestService;
import net.wasys.getdoc.http.ImagemFilter;
import net.wasys.getdoc.rest.exception.HTTP401Exception;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;

/**
 * 
 * @author jonas.baggio@wasys.com.br
 * @create 24 de jul de 2018 15:18:53
 */
public abstract class SuperController {

	@Autowired private SessaoHttpRequestService sessaoHttpRequestService;

	protected SessaoHttpRequest getSessaoHttpRequest(HttpServletRequest request) throws HTTP401Exception {

		SessaoHttpRequest sessaoHttpRequest = sessaoHttpRequestService.findByJSessionId(request);
		if(sessaoHttpRequest == null){
			throw new HTTP401Exception("http401.exception");
		}
		return sessaoHttpRequest;
	}

	protected String getImagePath(HttpServletRequest request){
		return request.getContextPath() + ImagemFilter.PATH;
	}

	protected Origem getOrigemFromHeader(HttpServletRequest request){
		String so = request.getHeader("so");
		if(StringUtils.isBlank(so)){
			return Origem.WEB;
		}
		switch (so){
			case "ANDROID" :
				return Origem.ANDROID;
			case "IOS":
				return Origem.IOS;
			default:
				return Origem.WEB;
		}
	}
}