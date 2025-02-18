package net.wasys.getdoc.mb.exception;

import org.springframework.http.HttpStatus;

/**
 * 
 * MessageException
 * 31 de jul de 2016 20:05:39
 * @autor Everton Luiz Pascke
 */
public class MessageException extends RuntimeException {

	private HttpStatus status;
	
	public MessageException(String message) {
		super(message);
		this.status = HttpStatus.BAD_REQUEST;
	}
	
	public MessageException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}
	
	public HttpStatus getStatus() {
		return status;
	}
}