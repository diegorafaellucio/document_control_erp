package net.wasys.util.betaface;

import java.math.BigDecimal;

public class Point {

	private String name;
	private BigDecimal type;
	private BigDecimal x;
	private BigDecimal y;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BigDecimal getType() {
		return type;
	}

	public void setType(BigDecimal type) {
		this.type = type;
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
}