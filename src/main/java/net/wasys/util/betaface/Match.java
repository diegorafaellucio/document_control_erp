package net.wasys.util.betaface;

import java.math.BigDecimal;

public class Match {

	private BigDecimal confidence;
	private String faceUid;
	private boolean isMatch;
	private String personName;

	public BigDecimal getConfidence() {
		return confidence;
	}

	public void setConfidence(BigDecimal confidence) {
		this.confidence = confidence;
	}

	public String getFaceUid() {
		return faceUid;
	}

	public void setFaceUid(String faceUid) {
		this.faceUid = faceUid;
	}

	public boolean getIsMatch() {
		return isMatch;
	}

	public void setIsMatch(boolean isMatch) {
		this.isMatch = isMatch;
	}

	public String getPersonName() {
		return personName;
	}

	public void setPersonName(String personName) {
		this.personName = personName;
	}
}