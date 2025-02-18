package net.wasys.util.google.cloud;

public class Symbol {

	private String text;
	private TextProperty property;
	private BoundingPoly boundingBox;

	public String getText() {
		return text;
	}

	public TextProperty getProperty() {
		return property;
	}

	public BoundingPoly getBoundingBox() {
		return boundingBox;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setProperty(TextProperty property) {
		this.property = property;
	}

	public void setBoundingBox(BoundingPoly boundingBox) {
		this.boundingBox = boundingBox;
	}
}
