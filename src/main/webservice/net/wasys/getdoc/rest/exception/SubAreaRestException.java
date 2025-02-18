package net.wasys.getdoc.rest.exception;

import net.wasys.util.ddd.MessageKeyException;

public class SubAreaRestException extends MessageKeyException {

	public SubAreaRestException(String key, Object... args) {
		super(key, args);
	}
}