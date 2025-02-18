package net.wasys.util.betaface;

public class FacesMatch {

	private String faceUid;
	private Match[] matches;

	public String getFaceUid() {
		return faceUid;
	}

	public void setFaceUid(String faceUid) {
		this.faceUid = faceUid;
	}

	public Match[] getMatches() {
		return matches;
	}

	public void setMatches(Match[] matches) {
		this.matches = matches;
	}
}
