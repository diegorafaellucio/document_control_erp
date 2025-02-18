package net.wasys.util.faces.component;

import java.io.IOException;

import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;

public class TextLiteral extends UIComponentBase {

	private String text;

	public TextLiteral() {}

	public TextLiteral(String text) {
		this.text = text;
	}

	@Override
	public String getFamily() {
		return "textLiteral";
	}

	@Override
	public void encodeEnd(FacesContext context) throws IOException {
		context.getResponseWriter().append(text);
	}
}