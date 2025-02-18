package net.wasys.getdoc.rest.exception;

import net.wasys.util.ddd.MessageKeyException;

public class BaseInternaRestException extends MessageKeyException {

	public BaseInternaRestException(String key) {
		super(key);
	}

	public BaseInternaRestException(String key, Object... args) {
		super(key, args);
	}
}