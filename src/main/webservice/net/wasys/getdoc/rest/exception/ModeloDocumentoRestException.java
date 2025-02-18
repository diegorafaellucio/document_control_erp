package net.wasys.getdoc.rest.exception;

import net.wasys.util.ddd.MessageKeyException;

public class ModeloDocumentoRestException extends MessageKeyException {

	public ModeloDocumentoRestException(String key, Object... args) {
		super(key, args);
	}
}