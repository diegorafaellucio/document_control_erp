package net.wasys.getdoc.rest.exception;

import net.wasys.util.ddd.MessageKeyException;

public class AlunoRestException extends MessageKeyException {

	public AlunoRestException(String key, Object... args) {
		super(key, args);
	}
}