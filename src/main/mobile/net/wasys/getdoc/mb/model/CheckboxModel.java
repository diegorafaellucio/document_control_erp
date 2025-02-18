package net.wasys.getdoc.mb.model;

public class CheckboxModel<T> {
	
	private T value;
	private boolean checked;
	
	public CheckboxModel(T value) {
		this.value = value;
	}

	public T getValue() {
		return value;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}
}
