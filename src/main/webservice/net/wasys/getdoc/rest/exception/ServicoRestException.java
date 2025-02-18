package net.wasys.getdoc.rest.exception;

import net.wasys.util.ddd.MessageKeyException;

public class ServicoRestException extends MessageKeyException {

	public ServicoRestException(String key, Object... args) {
		super(key, args);
	}
}