package net.wasys.util.google.cloud;

import java.util.ArrayList;
import java.util.List;

public class BatchAnnotateImageRequest {

	private List<AnnotateImageRequest> requests;
	
	public void add(AnnotateImageRequest request) {
		if (requests == null) {
			requests = new ArrayList<>();
		}
		requests.add(request);
	}
	
	public List<AnnotateImageRequest> getRequests() {
		return requests;
	}
	
	public void setRequests(List<AnnotateImageRequest> requests) {
		this.requests = requests;
	}
}
