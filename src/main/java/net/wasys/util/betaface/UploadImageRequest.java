package net.wasys.util.betaface;

public class UploadImageRequest extends BetafaceRequest {

	private String detectionFlags;
	private byte[] image;
	private String imageBase64;
	private String originalFilename;
	private String url;

	public String getDetectionFlags() {
		return detectionFlags;
	}

	public void setDetectionFlags(String detectionFlags) {
		this.detectionFlags = detectionFlags;
	}

	public byte[] getImage() {
		return image;
	}

	public void setImage(byte[] image) {
		this.image = image;
	}

	public String getImageBase64() {
		return imageBase64;
	}

	public void setImageBase64(String imageBase64) {
		this.imageBase64 = imageBase64;
	}

	public String getOriginalFilename() {
		return originalFilename;
	}

	public void setOriginalFilename(String originalFilename) {
		this.originalFilename = originalFilename;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
