package net.wasys.util.google.cloud;

import java.util.List;

public class Block {

	private BlockType blockType;
	private TextProperty property;
	private BoundingPoly boundingBox;
	
	private List<Paragraph> paragraphs;

	public BlockType getBlockType() {
		return blockType;
	}

	public TextProperty getProperty() {
		return property;
	}

	public BoundingPoly getBoundingBox() {
		return boundingBox;
	}

	public List<Paragraph> getParagraphs() {
		return paragraphs;
	}

	public void setBlockType(BlockType blockType) {
		this.blockType = blockType;
	}

	public void setProperty(TextProperty property) {
		this.property = property;
	}

	public void setBoundingBox(BoundingPoly boundingBox) {
		this.boundingBox = boundingBox;
	}

	public void setParagraphs(List<Paragraph> paragraphs) {
		this.paragraphs = paragraphs;
	}
}
