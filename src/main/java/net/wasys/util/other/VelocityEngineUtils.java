package net.wasys.util.other;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Set;

public class VelocityEngineUtils {

	public static void merge(String path, Writer writer, Map<String, Object> model) {

		VelocityEngine engine = new VelocityEngine();
		VelocityContext context = new VelocityContext();

		engine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		engine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		engine.init();

		if (model != null) {

			Set<String> keySet = model.keySet();
			for (String key : keySet) {

				Object value = model.get(key);
				context.put(key, value);
			}
		}

		Template template = engine.getTemplate(path, "UTF-8");
		template.merge(context, writer);
	}

	public static String merge(Map<String, ?> model, String templateStr) {

		VelocityEngine engine = new VelocityEngine();
		engine.setProperty(Velocity.RESOURCE_LOADER, "string");
		engine.addProperty("string.resource.loader.class", StringResourceLoader.class.getName());
		engine.addProperty("string.resource.loader.repository.static", "false");
		engine.init();

		String keyTemplate = "template-" + Math.random();

		StringResourceRepository rep = (StringResourceRepository) engine.getApplicationAttribute(StringResourceLoader.REPOSITORY_NAME_DEFAULT);
		rep.putStringResource(keyTemplate, templateStr);

		VelocityContext context = new VelocityContext();
		for (String key : model.keySet()) {
			Object value = model.get(key);
			context.put(key, value);
		}

		StringWriter writer = new StringWriter();
		Template template = engine.getTemplate(keyTemplate, "UTF-8");
		template.merge(context, writer);

		String content = String.valueOf(writer);
		return content;
	}
}