package net.wasys.getdoc.mb.model;

import net.wasys.util.rest.jackson.ObjectMapper;

public class ViewModel {

	public String title;
	public ToolbarModel toolbar;
	
	public String parse() {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(this);
	}
}
