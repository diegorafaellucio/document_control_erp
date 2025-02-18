package net.wasys.getdoc.mb.exception;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.http.HttpStatus;

/**
 * 
 * ListMessageException
 * 04/10/2015 23:26:05
 * @author Everton Luiz Pascke
 */
public class ListMessageException extends RuntimeException {

	private HttpStatus status;
	private List<String> messages;
	
	public ListMessageException() {
		this(HttpStatus.BAD_REQUEST);
	}
	
	public ListMessageException(HttpStatus status) {
		this.status = status;
	}
	
	public ListMessageException(List<String> messages) {
		this(HttpStatus.BAD_REQUEST, messages);
	}
	
	public ListMessageException(HttpStatus status, List<String> messages) {
		this(status);
		this.messages = messages;
	}
	
	public void addMessage(String message) {
		if (messages == null) {
			messages = new ArrayList<String>();
		}
		messages.add(message);
	}
	
	public List<String> getMessages() {
		return messages;
	}
	
	public boolean isEmpty() {
		return CollectionUtils.isEmpty(messages);
	}
	
	public boolean isNotEmpty() {
		return !isEmpty();
	}
	
	public HttpStatus getStatus() {
		return status;
	}
	
	@Override
	public String getMessage() {
		StringBuilder builder = new StringBuilder();
		if (messages != null) {
			for (String message : messages) {
				builder.append(message);
				if (!message.endsWith(".")) {
					builder.append(".");
				}
			}
		}
		return String.valueOf(messages);
	}
}