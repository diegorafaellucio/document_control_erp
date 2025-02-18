package net.wasys.getdoc.rest.exception;

import net.wasys.util.ddd.MessageKeyException;

public class ModeloOcrRestException extends MessageKeyException {

	public ModeloOcrRestException(String key, Object... args) {
		super(key, args);
	}
}