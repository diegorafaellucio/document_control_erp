package net.wasys.util.google.cloud;

public class DetectedLanguage {

	private String languageCode;
	private Integer confidence;
	
	public String getLanguageCode() {
		return languageCode;
	}
	
	public Integer getConfidence() {
		return confidence;
	}
	
	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}
	
	public void setConfidence(Integer confidence) {
		this.confidence = confidence;
	}
}