package net.wasys.util.google.cloud;

import java.util.List;

public class Paragraph {

	private TextProperty property;
	private BoundingPoly boundingBox;
	
	private List<Word> words;

	public TextProperty getProperty() {
		return property;
	}

	public BoundingPoly getBoundingBox() {
		return boundingBox;
	}

	public List<Word> getWords() {
		return words;
	}

	public void setProperty(TextProperty property) {
		this.property = property;
	}

	public void setBoundingBox(BoundingPoly boundingBox) {
		this.boundingBox = boundingBox;
	}

	public void setWords(List<Word> words) {
		this.words = words;
	}
}
