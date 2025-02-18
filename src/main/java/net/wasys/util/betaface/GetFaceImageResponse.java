package net.wasys.util.betaface;

public class GetFaceImageResponse extends BetafaceResponse {

	private byte[] faceImage;
	private FaceInfo faceInfo;
	private String uid;

	public byte[] getFaceImage() {
		return faceImage;
	}

	public void setFaceImage(byte[] faceImage) {
		this.faceImage = faceImage;
	}

	public FaceInfo getFaceInfo() {
		return faceInfo;
	}

	public void setFaceInfo(FaceInfo faceInfo) {
		this.faceInfo = faceInfo;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
}