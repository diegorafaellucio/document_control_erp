package net.wasys.getdoc.rest.exception;

import net.wasys.util.ddd.MessageKeyException;

public class ParametroRestException extends MessageKeyException {

	public ParametroRestException(String key, Object... args) {
		super(key, args);
	}
}