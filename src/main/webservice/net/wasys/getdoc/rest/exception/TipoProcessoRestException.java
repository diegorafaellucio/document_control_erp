package net.wasys.getdoc.rest.exception;

import net.wasys.util.ddd.MessageKeyException;

public class TipoProcessoRestException extends MessageKeyException {

	public TipoProcessoRestException(String key, Object... args) {
		super(key, args);
	}
}