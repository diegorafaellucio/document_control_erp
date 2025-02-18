package net.wasys.getdoc.rest.exception;

import net.wasys.util.ddd.MessageKeyException;

public class SubPerfilRestException extends MessageKeyException {

	public SubPerfilRestException(String key, Object... args) {
		super(key, args);
	}
}