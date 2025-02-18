package net.wasys.getdoc.rest.exception;

import net.wasys.util.ddd.MessageKeyException;

public class SolicitacaoRestException extends MessageKeyException {

	public SolicitacaoRestException(String key, Object... args) {
		super(key, args);
	}
}