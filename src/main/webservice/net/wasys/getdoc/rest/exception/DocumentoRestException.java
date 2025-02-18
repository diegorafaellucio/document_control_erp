package net.wasys.getdoc.rest.exception;

import net.wasys.util.ddd.MessageKeyException;

public class DocumentoRestException extends MessageKeyException {

	public DocumentoRestException(String key, Object... args) {
		super(key, args);
	}
}