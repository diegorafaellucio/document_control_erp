package net.wasys.getdoc.rest.exception;

import net.wasys.util.ddd.MessageKeyException;

public class TipoEvidenciaRestException extends MessageKeyException {

	public TipoEvidenciaRestException(String key, Object... args) {
		super(key, args);
	}
}