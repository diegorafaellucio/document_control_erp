package net.wasys.getdoc.mb.enumerator;

public enum DeviceHeader {

	USER_ID ("User-ID"),
	DEVICE_SO ("Device-SO"),
	DEVICE_IMEI ("Device-IMEI"),
	DEVICE_MODEL ("Device-Model"),
	DEVICE_WIDTH ("Device-Width"),
	DEVICE_HEIGHT ("Device-Height"),
	DEVICE_SO_VERSION ("Device-SO-Version"),
	DEVICE_APP_VERSION ("Device-App-Version");
	public String key;
	DeviceHeader(String key) {
		this.key = key;
	}
}
