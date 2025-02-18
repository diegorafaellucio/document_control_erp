package net.wasys.util.google.cloud;

public class DetectedBreak {

	private BreakType type;
	private Boolean isPrefix;
	
	public BreakType getType() {
		return type;
	}
	
	public Boolean getIsPrefix() {
		return isPrefix;
	}
	
	public void setType(BreakType type) {
		this.type = type;
	}
	
	public void setIsPrefix(Boolean isPrefix) {
		this.isPrefix = isPrefix;
	}
}