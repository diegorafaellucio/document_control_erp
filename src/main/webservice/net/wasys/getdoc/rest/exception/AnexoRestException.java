package net.wasys.getdoc.rest.exception;

import net.wasys.util.ddd.MessageKeyException;

public class AnexoRestException extends MessageKeyException {

	public AnexoRestException(String key, Object... args) {
		super(key, args);
	}
}