package net.wasys.getdoc.rest.exception;

import net.wasys.util.ddd.MessageKeyException;

public class TipoDocumentoRestException extends MessageKeyException {

	public TipoDocumentoRestException(String key, Object... args) {
		super(key, args);
	}
}