package net.wasys.util.rest.jackson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer;

/*
 * Classe para deserializar multiplos elementos com mesmo nome em um unico array de objetos.
 * Fonte: https://github.com/FasterXML/jackson-dataformat-xml/issues/205
 * Gist: https://gist.github.com/joaovarandas/1543e792ed6204f0cf5fe860cb7d58ed
 * */
@SuppressWarnings("deprecation")
public class FixedUntypedObjectDeserializer extends UntypedObjectDeserializer {

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Object mapObject(JsonParser p, DeserializationContext ctxt) throws IOException {

		String firstKey;

		JsonToken t = p.getCurrentToken();

		if (t == JsonToken.START_OBJECT) {
			firstKey = p.nextFieldName();
		} else if (t == JsonToken.FIELD_NAME) {
			firstKey = p.getCurrentName();
		} else {
			if (t != JsonToken.END_OBJECT) {
				throw ctxt.mappingException(handledType(), p.getCurrentToken());
			}
			firstKey = null;
		}

		// empty map might work; but caller may want to modify... so better
		// just give small modifiable
		LinkedHashMap<String, Object> resultMap = new LinkedHashMap<String, Object>(2);
		if (firstKey == null)
			return resultMap;

		p.nextToken();
		resultMap.put(firstKey, deserialize(p, ctxt));

		// 03-Aug-2016, jpvarandas: handle next objects and create an array
		Set<String> listKeys = new LinkedHashSet<>();

		String nextKey;
		while ((nextKey = p.nextFieldName()) != null) {
			p.nextToken();
			if (resultMap.containsKey(nextKey)) {
				Object listObject = resultMap.get(nextKey);

				if (!(listObject instanceof List)) {
					listObject = new ArrayList<>();
					((List) listObject).add(resultMap.get(nextKey));

					resultMap.put(nextKey, listObject);
				}

				((List) listObject).add(deserialize(p, ctxt));

				listKeys.add(nextKey);

			} else {
				resultMap.put(nextKey, deserialize(p, ctxt));

			}
		}

		return resultMap;
	}
}
