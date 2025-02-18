package net.wasys.util.rest.jackson;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class XMLMapper extends com.fasterxml.jackson.dataformat.xml.XmlMapper {

	public XMLMapper() {
		SimpleModule module = new SimpleModule();
		module.addDeserializer(Object.class, new FixedUntypedObjectDeserializer());
		setSerializationInclusion(Include.NON_NULL);
		setSerializationInclusion(Include.NON_EMPTY);
		configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		registerModule(module);
	}

	@Override
	public <T> T readValue(String content, Class<T> valueType) {
		try {
			return super.readValue(content, valueType);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}