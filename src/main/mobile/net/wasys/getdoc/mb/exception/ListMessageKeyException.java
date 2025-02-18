package net.wasys.getdoc.mb.exception;

import net.wasys.util.ddd.MessageKeyException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.http.HttpStatus;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 * ListMessageKeyException
 * 30 de jul de 2016 18:12:41
 * @autor Everton Luiz Pascke
 */
public class ListMessageKeyException extends RuntimeException {

	private HttpStatus status;
	private List<MessageKeyException> messages;
	
	public ListMessageKeyException() {
		this(HttpStatus.BAD_REQUEST);
	}
	
	public ListMessageKeyException(HttpStatus status) {
		this.status = status;
	}
	
	public ListMessageKeyException(MessageKeyException... exceptions) {
		this(HttpStatus.BAD_REQUEST, exceptions);
	}
	
	public ListMessageKeyException(HttpStatus status, MessageKeyException... exceptions) {
		this(status);
		if (ArrayUtils.isNotEmpty(exceptions)) {
			for (MessageKeyException exception : exceptions) {
				add(exception);
			}
		}
	}
	
	public boolean isEmpty() {
		return CollectionUtils.isEmpty(messages);
	}
	
	public boolean isNotEmpty() {
		return !isEmpty();
	}
	
	public void add(MessageKeyException exception) {
		if (messages == null) {
			messages = new LinkedList<MessageKeyException>();
		}
		messages.add(exception);
	}
	
	public void addAll(List<MessageKeyException> exceptions) {
		if (CollectionUtils.isNotEmpty(exceptions)) {
			for (MessageKeyException exception : exceptions) {
				add(exception);
			}
		}
	}
	
	public HttpStatus getStatus() {
		return status;
	}
	
	public List<MessageKeyException> getMessages() {
		return messages;
	}
	
	public void setStatus(HttpStatus status) {
		this.status = status;
	}
}
