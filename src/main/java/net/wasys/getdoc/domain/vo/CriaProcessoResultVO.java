package net.wasys.getdoc.domain.vo;

import net.wasys.getdoc.domain.entity.Processo;

public class CriaProcessoResultVO {

	private Processo processo;
	private Exception exception;

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public Processo getProcesso() {
		return processo;
	}

	public void setProcesso(Processo processo) {
		this.processo = processo;
	}
}
