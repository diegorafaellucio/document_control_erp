package net.wasys.util.rest.dto;

import java.util.HashSet;
import java.util.Set;

import org.springframework.http.HttpStatus;

public class ErrorDTO {

	public HttpStatus status;
	public Set<String> messages;

	public ErrorDTO() {
		this.status = HttpStatus.INTERNAL_SERVER_ERROR;
	}

	public ErrorDTO(HttpStatus status) {
		this.status = status;
	}

	public void addMessage(String message) {
		if (messages == null) {
			messages = new HashSet<>();
		}
		messages.add(message);
	}

	public HttpStatus getStatus() {
		return status;
	}

	public void setStatus(HttpStatus status) {
		this.status = status;
	}

	public Set<String> getMessages() {
		return messages;
	}

	public void setMessages(Set<String> messages) {
		this.messages = messages;
	}
}