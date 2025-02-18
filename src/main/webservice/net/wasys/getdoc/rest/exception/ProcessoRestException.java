package net.wasys.getdoc.rest.exception;

import net.wasys.util.ddd.MessageKeyException;

public class ProcessoRestException extends MessageKeyException {

	public ProcessoRestException(String key) {
		super(key);
	}

	public ProcessoRestException(String key, Object... args) {
		super(key, args);
	}
}