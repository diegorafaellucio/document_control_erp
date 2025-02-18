package net.wasys.getdoc.rest.exception;

import net.wasys.util.ddd.MessageKeyException;

public class SantanderServiceException extends MessageKeyException {

	public SantanderServiceException(String key, Object... args) {
		super(key, args);
	}
}