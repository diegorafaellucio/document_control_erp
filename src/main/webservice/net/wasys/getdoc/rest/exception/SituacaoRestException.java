package net.wasys.getdoc.rest.exception;

import net.wasys.util.ddd.MessageKeyException;

public class SituacaoRestException  extends MessageKeyException {

	public SituacaoRestException(String key, Object... args) {
		super(key, args);
	}
}