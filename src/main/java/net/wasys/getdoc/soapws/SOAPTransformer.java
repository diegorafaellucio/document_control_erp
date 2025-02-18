package net.wasys.getdoc.soapws;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;

public class SOAPTransformer {

	public String transformar(Source source) throws TransformerException, UnsupportedEncodingException {

		String resultado = null;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {

			Transformer tf = TransformerFactory.newInstance().newTransformer();
			tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			try {
				out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes(StandardCharsets.UTF_8));
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			tf.transform(source, new StreamResult(out));
			resultado = out.toString(StandardCharsets.UTF_8.name());
		}
		finally {
			fecharOutputStream(out);
		}

		return resultado;
	}

	private void fecharOutputStream(ByteArrayOutputStream streamResult) {
		try {
			streamResult.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}