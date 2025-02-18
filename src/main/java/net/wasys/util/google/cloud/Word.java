package net.wasys.util.google.cloud;

import java.util.List;

public class Word {

	private TextProperty property;
	private BoundingPoly boundingBox;
	
	private List<Symbol> symbols;

	public TextProperty getProperty() {
		return property;
	}

	public BoundingPoly getBoundingBox() {
		return boundingBox;
	}

	public List<Symbol> getSymbols() {
		return symbols;
	}

	public void setProperty(TextProperty property) {
		this.property = property;
	}

	public void setBoundingBox(BoundingPoly boundingBox) {
		this.boundingBox = boundingBox;
	}

	public void setSymbols(List<Symbol> symbols) {
		this.symbols = symbols;
	}
}
