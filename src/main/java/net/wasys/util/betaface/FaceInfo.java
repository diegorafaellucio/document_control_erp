package net.wasys.util.betaface;

import java.math.BigDecimal;

public class FaceInfo {

	private BigDecimal angle;
	private BigDecimal height;
	private String image_uid;
	private String person_name;
	private BigDecimal score;
	private String uid;
	private BigDecimal width;
	private BigDecimal x;
	private BigDecimal y;
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

	public String getImage_uid() {
		return image_uid;
	}

	public void setImage_uid(String image_uid) {
		this.image_uid = image_uid;
	}

	public String getPerson_name() {
		return person_name;
	}

	public void setPerson_name(String person_name) {
		this.person_name = person_name;
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