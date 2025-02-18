package net.wasys.util.rest.jackson;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import net.wasys.util.DummyUtils;

public class JsonDateSerializer extends JsonSerializer<Date> {

	@Override
	public void serialize(Date value, JsonGenerator generator, SerializerProvider serializers) throws IOException, JsonProcessingException {
		if (value == null) {
			generator.writeNull();
		} else {
			generator.writeString(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(value));
		}
	}
}