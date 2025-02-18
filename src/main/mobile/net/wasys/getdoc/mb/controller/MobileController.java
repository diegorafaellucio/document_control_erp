package net.wasys.getdoc.mb.controller;

import lombok.extern.slf4j.Slf4j;
import net.wasys.getdoc.domain.entity.Usuario;
import net.wasys.getdoc.domain.service.MessageService;
import net.wasys.getdoc.mb.exception.ArgumentKey;
import net.wasys.getdoc.mb.exception.ListMessageException;
import net.wasys.getdoc.mb.exception.ListMessageKeyException;
import net.wasys.getdoc.mb.exception.MessageException;
import net.wasys.getdoc.mb.http.Trouble;
import net.wasys.util.ddd.MessageKeyException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.annotation.Resource;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * MobileController.java
 * @pascke
 */
public abstract class MobileController {

	protected Usuario usuario;
	
	@Autowired 
	private MessageService messageService;
	
	@Resource(name="resource")
	private ResourceBundleMessageSource messageSource;
	
	protected String getMessage(String key, Object...args) {
		return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Trouble> handleException(Exception exception) {
		exception.printStackTrace();
		HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
		Trouble trouble = getTrouble(status);
		trouble.addMessage(ExceptionUtils.getRootCauseMessage(exception));
		return new ResponseEntity<Trouble>(trouble, status);
	}
	
	@ExceptionHandler(MessageException.class)
	public ResponseEntity<Trouble> handleException(MessageException exception) {
		HttpStatus status = exception.getStatus();
		Trouble trouble = getTrouble(status);
		trouble.addMessage(exception.getMessage());
		return new ResponseEntity<Trouble>(trouble, status);
	}
	
	@ExceptionHandler(ListMessageException.class)
	public ResponseEntity<Trouble> handleException(ListMessageException exception) {
		HttpStatus status = exception.getStatus();
		Trouble trouble = getTrouble(status);
		List<String> messages = exception.getMessages();
		if (CollectionUtils.isNotEmpty(messages)) {
			for (String message : messages) {
				trouble.addMessage(message);
			}
		}
		return new ResponseEntity<Trouble>(trouble, status);
	}
	
	@ExceptionHandler(MessageKeyException.class)
	public ResponseEntity<Trouble> handleException(MessageKeyException exception) {
		HttpStatus status = exception.getStatus();
		Trouble trouble = getTrouble(status);
		String key = exception.getKey();
		Object[] args = exception.getArgs();
		String message = messageService.getValue(key, args);
		trouble.addMessage(message);
		return new ResponseEntity<Trouble>(trouble, status);
	}
	
	@ExceptionHandler(ListMessageKeyException.class)
	public ResponseEntity<Trouble> handleException(ListMessageKeyException exception) {
		HttpStatus status = exception.getStatus();
		Trouble trouble = getTrouble(status);
		List<MessageKeyException> messages = exception.getMessages();
		if (CollectionUtils.isNotEmpty(messages)) {
			for (MessageKeyException messageKeyException : messages) {
				trouble.addMessage(getMessage(messageKeyException));
			}
		}
		return new ResponseEntity<Trouble>(trouble, status);
	}
	
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<Trouble> handleException(ConstraintViolationException exception) {
		HttpStatus status = HttpStatus.BAD_REQUEST;
		Trouble trouble = getTrouble(status);
		String message = ExceptionUtils.getRootCauseMessage(exception);
		Pattern pattern = Pattern.compile("(\\w)+_(?i)(fk|uk)");
		Matcher matcher = pattern.matcher(message);
		if (matcher.find()) {
			String key = "Msg." + matcher.group().toLowerCase();
			String msg = getMessage(key);
			if (StringUtils.isNotBlank(msg)) {
				message = msg;
			}
		}
		trouble.addMessage(message);
		return new ResponseEntity<Trouble>(trouble, status);
	}
	
	private Trouble getTrouble(HttpStatus status) {
		Trouble error = new Trouble(status);
		return error;
	}
	
	private String getMessage(MessageKeyException messageKeyException) {
		String key = messageKeyException.getKey();
		Object[] args = messageKeyException.getArgs();
		if (ArrayUtils.isNotEmpty(args)) {
			Object[] objects = new Object[args.length];
			for (int i = 0; i < args.length; i++) {
				Object arg = args[i];
				if (arg instanceof ArgumentKey) {
					arg = getMessage(key);
				}
				objects[i] = arg;
			}
			args = objects;
		}
		String message = getMessage(key);
		return message;
	}
}