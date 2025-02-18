package net.wasys.util.google.cloud;

public class EntityAnnotation {

	private String locale;
	private String description;
	private BoundingPoly boundingPoly;

	public String getLocale() {
		return locale;
	}

	public String getDescription() {
		return description;
	}

	public BoundingPoly getBoundingPoly() {
		return boundingPoly;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setBoundingPoly(BoundingPoly boundingPoly) {
		this.boundingPoly = boundingPoly;
	}
}