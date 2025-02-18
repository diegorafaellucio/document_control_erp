package net.wasys.getdoc.rest.exception;

import net.wasys.util.ddd.MessageKeyException;

public class AreaRestException extends MessageKeyException {

	public AreaRestException(String key, Object... args) {
		super(key, args);
	}
}