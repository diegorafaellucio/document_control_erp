package net.wasys.getdoc.mb.model;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

public class ResultModel {

	public boolean success;
	public Set<String> messages;
	
	public ResultModel() {
		
	}
	
	public ResultModel(boolean success, String... messages) {
		this.success = success;
		if (ArrayUtils.isNotEmpty(messages)) {
			this.messages = new HashSet<>();
			for (String message : messages) {
				this.messages.add(message);
			}
		}
	}
}
