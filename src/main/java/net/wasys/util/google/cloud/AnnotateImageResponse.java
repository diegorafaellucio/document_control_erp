package net.wasys.util.google.cloud;

import java.util.List;

public class AnnotateImageResponse {

	private TextAnnotation fullTextAnnotation;
	private List<EntityAnnotation> textAnnotations;
	
	public TextAnnotation getFullTextAnnotation() {
		return fullTextAnnotation;
	}
	
	public List<EntityAnnotation> getTextAnnotations() {
		return textAnnotations;
	}
	
	public void setFullTextAnnotation(TextAnnotation fullTextAnnotation) {
		this.fullTextAnnotation = fullTextAnnotation;
	}
	
	public void setTextAnnotations(List<EntityAnnotation> textAnnotations) {
		this.textAnnotations = textAnnotations;
	}
}