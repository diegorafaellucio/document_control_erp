package net.wasys.util.google.cloud;

import java.util.List;

public class BatchAnnotateImageResponse {

	private List<AnnotateImageResponse> responses;
	
	public List<AnnotateImageResponse> getResponses() {
		return responses;
	}
	
	public void setResponses(List<AnnotateImageResponse> responses) {
		this.responses = responses;
	}
}
