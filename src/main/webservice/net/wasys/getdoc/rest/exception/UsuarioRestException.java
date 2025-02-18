package net.wasys.getdoc.rest.exception;

import net.wasys.util.ddd.MessageKeyException;

public class UsuarioRestException extends MessageKeyException {

	public UsuarioRestException(String key, Object... args) {
		super(key, args);
	}
}