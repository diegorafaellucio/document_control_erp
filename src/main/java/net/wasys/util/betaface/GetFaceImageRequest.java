package net.wasys.util.betaface;

public class GetFaceImageRequest extends BetafaceRequest {

	private String faceUid;

	public String getFaceUid() {
		return faceUid;
	}

	public void setFaceUid(String faceUid) {
		this.faceUid = faceUid;
	}
}
