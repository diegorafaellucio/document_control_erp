package net.wasys.util.betaface;

public class GetImageInfoResponse extends BetafaceResponse {

	private String checksum;
	private String originalFilename;
	private String uid;
	private Face[] faces;

	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public String getOriginalFilename() {
		return originalFilename;
	}

	public void setOriginalFilename(String originalFilename) {
		this.originalFilename = originalFilename;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public Face[] getFaces() {
		return faces;
	}

	public void setFaces(Face[] faces) {
		this.faces = faces;
	}
}