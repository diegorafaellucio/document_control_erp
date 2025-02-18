package net.wasys.getdoc.rest.exception;

import net.wasys.util.ddd.MessageKeyException;

public class IrregularidadeRestException extends MessageKeyException {

	public IrregularidadeRestException(String key, Object... args) {
		super(key, args);
	}
}