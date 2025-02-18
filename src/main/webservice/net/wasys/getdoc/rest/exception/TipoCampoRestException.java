package net.wasys.getdoc.rest.exception;

import net.wasys.util.ddd.MessageKeyException;

public class TipoCampoRestException extends MessageKeyException {

	public TipoCampoRestException(String key, Object... args) {
		super(key, args);
	}
}