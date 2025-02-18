package net.wasys.getdoc.rest.exception;

import net.wasys.util.ddd.MessageKeyException;

public class DadosObrigatorioRequestException extends MessageKeyException {

	public DadosObrigatorioRequestException(String key, Object... args) {
		super(key, args);
	}
}