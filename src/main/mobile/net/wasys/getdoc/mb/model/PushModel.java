package net.wasys.getdoc.mb.model;

import net.wasys.getdoc.mb.enumerator.DeviceSO;

public class PushModel {

	private Type type;
	private Object data;
	private String token;
	private String title;
	private String message;
	private DeviceSO deviceSO;
	
	public enum Type {
		LINK
	}
	
	public PushModel(Type type, DeviceSO deviceSO, String token) {
		this.type = type;
		this.token = token;
		this.deviceSO = deviceSO;
	}
	
	public Type getType() {
		return type;
	}
	
	public DeviceSO getDeviceSO() {
		return deviceSO;
	}
	
	public String getToken() {
		return token;
	}

	public Object getData() {
		return data;
	}

	public String getTitle() {
		return title;
	}

	public String getMessage() {
		return message;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
