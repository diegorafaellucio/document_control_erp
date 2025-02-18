package net.wasys.getdoc.mb.http;

import java.util.HashSet;
import java.util.Set;

import org.springframework.http.HttpStatus;

/**
 * 
 * Trouble
 * 31 de jul de 2016 18:38:36
 * @autor Everton Luiz Pascke
 */
public class Trouble {

	public HttpStatus status;
	public Set<String> messages;
	
	public Trouble() {
		this.status = HttpStatus.INTERNAL_SERVER_ERROR;
	}
	
	public Trouble(HttpStatus status) {
		this.status = status;
	}
	
	public void addMessage(String message) {
		if (messages == null) {
			messages = new HashSet<>();
		}
		messages.add(message);
	}
}