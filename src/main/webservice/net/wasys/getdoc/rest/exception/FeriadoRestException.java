package net.wasys.getdoc.rest.exception;

import net.wasys.util.ddd.MessageKeyException;

public class FeriadoRestException extends MessageKeyException {

	public FeriadoRestException(String key) {
		super(key);
	}

	public FeriadoRestException(String key, Object... args) {
		super(key, args);
	}
}