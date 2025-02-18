package net.wasys.util.google.cloud;

import java.util.ArrayList;
import java.util.List;

public class AnnotateImageRequest {

	private Image image;
	private List<Feature> features;

	public void add(Feature feature) {
		if (features == null) {
			features = new ArrayList<>();
		}
		features.add(feature);
	}
	
	public Image getImage() {
		return image;
	}
	
	public List<Feature> getFeatures() {
		return features;
	}

	public void setImage(Image image) {
		this.image = image;
	}

	public void setFeatures(List<Feature> features) {
		this.features = features;
	}
}
