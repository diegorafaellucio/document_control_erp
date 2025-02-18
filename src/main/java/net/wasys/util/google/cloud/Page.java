package net.wasys.util.google.cloud;

import java.util.List;

public class Page {

	private Integer width;
	private Integer height;
	private TextProperty property;
	
	private List<Block> blocks;

	public Integer getWidth() {
		return width;
	}

	public Integer getHeight() {
		return height;
	}

	public TextProperty getProperty() {
		return property;
	}

	public List<Block> getBlocks() {
		return blocks;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	public void setProperty(TextProperty property) {
		this.property = property;
	}

	public void setBlocks(List<Block> blocks) {
		this.blocks = blocks;
	}
}
