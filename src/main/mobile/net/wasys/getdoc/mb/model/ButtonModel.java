package net.wasys.getdoc.mb.model;

public class ButtonModel {

	public Integer id;
	public String src;
	public Position position;
	
	public enum Position {
		LEFT,
		RIGHT
	}
	
	public enum Icon {
		ARROW_LEFT ("arrow-left.png"),
		FILTER ("filter.png"),
		PENCIL ("pencil.png"),
		PLUS ("plus.png"),
		REFRESH ("refresh.png"),
		SEARCH ("search.png"),
		UNDO ("undo.png"),
		CAMERA ("camera.png");
		private String image;
		private Icon(String image) {
			this.image = image;
		}
	}
	
	public ButtonModel(Integer id, Icon icon, Position position) {
		this.id = id;
		this.position = position;
		this.src = String.format("resources/mobile/images/bar/%1$s", icon.image);
	}
}
