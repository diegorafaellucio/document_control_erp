package net.wasys.getdoc.rest.exception;

import net.wasys.util.ddd.MessageKeyException;

public class TextoPadraoRestException extends MessageKeyException {

	public TextoPadraoRestException(String key, Object... args) {
		super(key, args);
	}
}