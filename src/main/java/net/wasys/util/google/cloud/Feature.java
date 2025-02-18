package net.wasys.util.google.cloud;

public class Feature {

	private Type type;
	private Integer maxResults;
	
	public Feature() {
		
	}
	
	public Feature(Type type) {
		this.type = type;
	}
	
	public Type getType() {
		return type;
	}
	
	public Integer getMaxResults() {
		return maxResults;
	}
	
	public void setType(Type type) {
		this.type = type;
	}
	
	public void setMaxResults(Integer maxResults) {
		this.maxResults = maxResults;
	}
}
