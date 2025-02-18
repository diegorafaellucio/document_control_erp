package net.wasys.getdoc.mb;

public class Toast {

	private Type type;
	private String message;
	private String styleClass;

	public enum Type {
		INFO ("info"),
		DANGER ("danger"),
		DEFAULT ("default"),
		PRIMARY ("primary"),
		WARNING ("warning"),
		SUCCESS ("success");
		private String styleClass;
		private Type(String styleClass) {
			this.styleClass = styleClass;
		}
	}

	public Toast() {}

	public Toast(String message) {
		this.message = message;
	}

	public Toast(Type type, String message) {
		this.type = type;
		this.message = message;
		if (type != null) {
			this.styleClass = type.styleClass;
		}
	}

	public Type getType() {
		return type;
	}

	public String getMessage() {
		return message;
	}

	public String getStyleClass() {
		return styleClass;
	}
}
