package net.wasys.getdoc.rest.exception;

import net.wasys.util.ddd.MessageKeyException;

public class FilaRestException extends MessageKeyException {

	public FilaRestException(String key, Object... args) {
		super(key, args);
	}
}