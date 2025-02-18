package net.wasys.util.betaface;

public class RecognizeFacesRequest extends BetafaceRequest {

	private String facesUids;
	private String parameters;
	private String targets;

	public String getFacesUids() {
		return facesUids;
	}

	public void setFacesUids(String facesUids) {
		this.facesUids = facesUids;
	}

	public String getParameters() {
		return parameters;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}

	public String getTargets() {
		return targets;
	}

	public void setTargets(String targets) {
		this.targets = targets;
	}
}
