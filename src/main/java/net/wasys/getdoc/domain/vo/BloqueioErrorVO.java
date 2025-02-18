package net.wasys.getdoc.domain.vo;

public class BloqueioErrorVO {

	private String key;
	private Object[] args;

	public BloqueioErrorVO(String key, Object... args) {
		this.key = key;
		this.args = args;
	}

	public String getKey() {
		return key;
	}

	public Object[] getArgs() {
		return args;
	}
}
