package net.wasys.getdoc.bean.vo;

public class NodeDataArrayVO {

	private Long key;
	private String category;
	private String text;
	private String stroke;
	private Boolean visibleRI;
	private Boolean visibleRA;
	private Boolean visibleRD;
	private Object[] descRI;
	private Object[] descRA;
	private Object[] descRD;

	public NodeDataArrayVO() {
		super();
	}

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getStroke() {
		return stroke;
	}

	public void setStroke(String stroke) {
		this.stroke = stroke;
	}

	public Boolean getVisibleRI() {
		return visibleRI;
	}

	public void setVisibleRI(Boolean visibleRI) {
		this.visibleRI = visibleRI;
	}

	public Boolean getVisibleRA() {
		return visibleRA;
	}

	public void setVisibleRA(Boolean visibleRA) {
		this.visibleRA = visibleRA;
	}

	public Boolean getVisibleRD() {
		return visibleRD;
	}

	public void setVisibleRD(Boolean visibleRD) {
		this.visibleRD = visibleRD;
	}

	public Object[] getDescRI() {
		return descRI;
	}

	public void setDescRI(Object[] descRI) {
		this.descRI = descRI;
	}

	public Object[] getDescRA() {
		return descRA;
	}

	public void setDescRA(Object[] descRA) {
		this.descRA = descRA;
	}

	public Object[] getDescRD() {
		return descRD;
	}

	public void setDescRD(Object[] descRD) {
		this.descRD = descRD;
	}
}