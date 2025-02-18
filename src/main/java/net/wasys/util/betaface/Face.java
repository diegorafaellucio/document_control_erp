package net.wasys.util.betaface;

import java.math.BigDecimal;

public class Face {

	private BigDecimal angle;
	private BigDecimal height;
	private String imageUid;
	private String personName;
	private BigDecimal width;
	private BigDecimal x;
	private BigDecimal y;
	private BigDecimal score;
	private String uid;
	private Tag[] tags;
	private Point[] points;
	private Point[] userPoints;

	public BigDecimal getAngle() {
		return angle;
	}

	public void setAngle(BigDecimal angle) {
		this.angle = angle;
	}

	public BigDecimal getHeight() {
		return height;
	}

	public void setHeight(BigDecimal height) {
		this.height = height;
	}

	public String getImageUid() {
		return imageUid;
	}

	public void setImageUid(String imageUid) {
		this.imageUid = imageUid;
	}

	public String getPersonName() {
		return personName;
	}

	public void setPersonName(String personName) {
		this.personName = personName;
	}

	public BigDecimal getWidth() {
		return width;
	}

	public void setWidth(BigDecimal width) {
		this.width = width;
	}

	public BigDecimal getX() {
		return x;
	}

	public void setX(BigDecimal x) {
		this.x = x;
	}

	public BigDecimal getY() {
		return y;
	}

	public void setY(BigDecimal y) {
		this.y = y;
	}

	public BigDecimal getScore() {
		return score;
	}

	public void setScore(BigDecimal score) {
		this.score = score;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public Tag[] getTags() {
		return tags;
	}

	public void setTags(Tag[] tags) {
		this.tags = tags;
	}

	public Point[] getPoints() {
		return points;
	}

	public void setPoints(Point[] points) {
		this.points = points;
	}

	public Point[] getUserPoints() {
		return userPoints;
	}

	public void setUserPoints(Point[] userPoints) {
		this.userPoints = userPoints;
	}
}