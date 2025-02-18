package net.wasys.util.google.cloud;

import java.util.List;

public class TextAnnotation {

	private String text;
	private List<Page> pages;
	
	public String getText() {
		return text;
	}
	
	public List<Page> getPages() {
		return pages;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public void setPages(List<Page> pages) {
		this.pages = pages;
	}
}