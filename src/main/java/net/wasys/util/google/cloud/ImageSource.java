package net.wasys.util.google.cloud;

public class ImageSource {

	private String imageUri;
	private String gcsImageUri;
	
	public ImageSource() {
		
	}
	
	public ImageSource(String imageUri) {
		this.imageUri = imageUri;
	}

	public String getImageUri() {
		return imageUri;
	}

	public String getGcsImageUri() {
		return gcsImageUri;
	}

	public void setImageUri(String imageUri) {
		this.imageUri = imageUri;
	}

	public void setGcsImageUri(String gcsImageUri) {
		this.gcsImageUri = gcsImageUri;
	}
}