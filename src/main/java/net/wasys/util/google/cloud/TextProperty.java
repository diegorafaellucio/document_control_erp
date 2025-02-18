package net.wasys.util.google.cloud;

import java.util.List;

public class TextProperty {

	private DetectedBreak detectedBreak;
	private List<DetectedLanguage> detectedLanguages;

	public DetectedBreak getDetectedBreak() {
		return detectedBreak;
	}

	public List<DetectedLanguage> getDetectedLanguages() {
		return detectedLanguages;
	}

	public void setDetectedBreak(DetectedBreak detectedBreak) {
		this.detectedBreak = detectedBreak;
	}

	public void setDetectedLanguages(List<DetectedLanguage> detectedLanguages) {
		this.detectedLanguages = detectedLanguages;
	}
}
