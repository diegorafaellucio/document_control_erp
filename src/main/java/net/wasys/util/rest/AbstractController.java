package net.wasys.util.rest;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import net.wasys.getdoc.domain.entity.LogAcesso;
import net.wasys.getdoc.domain.service.MessageService;
import net.wasys.util.ddd.MessageKeyException;
import net.wasys.util.rest.dto.ErrorDTO;
import net.wasys.util.servlet.LogAcessoFilter;

public abstract class AbstractController {

	@Autowired protected HttpServletRequest request;
	@Autowired private MessageService messageService;

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorDTO> handleException(Exception e) {

		LogAcesso logAcesso = LogAcessoFilter.getLogAcesso();

		String message = "#LA";
		if(logAcesso != null) {
			message += logAcesso.getId();
			String stackTrace = ExceptionUtils.getStackTrace(e);
			logAcesso.setException(stackTrace);
		}
		message = messageService.getValue("erroInesperado.error", message);

		ErrorDTO errorDTO = new ErrorDTO(HttpStatus.BAD_REQUEST);
		errorDTO.addMessage(message);
		return new ResponseEntity<>(errorDTO, errorDTO.status);
	}

	public boolean ifSecurityOk() {
		//TODO implementar verificacao de seguranca
		return true;
	}
}