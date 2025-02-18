package net.wasys.util.betaface;

public class GetRecognizeResultResponse extends BetafaceResponse {

	private int intResponse;
	private String stringResponse;
	private String recognizeUid;
	private FacesMatch[] facesMatches;

	public int getIntResponse() {
		return intResponse;
	}

	public void setIntResponse(int intResponse) {
		this.intResponse = intResponse;
	}

	public String getStringResponse() {
		return stringResponse;
	}

	public void setStringResponse(String stringResponse) {
		this.stringResponse = stringResponse;
	}

	public String getRecognizeUid() {
		return recognizeUid;
	}

	public void setRecognizeUid(String recognizeUid) {
		this.recognizeUid = recognizeUid;
	}

	public FacesMatch[] getFacesMatches() {
		return facesMatches;
	}

	public void setFacesMatches(FacesMatch[] facesMatches) {
		this.facesMatches = facesMatches;
	}
}