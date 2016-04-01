package net.akehurst.application.framework.technology.gui.vertx;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.text.StrSubstitutor;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileProps;
import io.vertx.ext.web.RoutingContext;

public class TemplateStaticHandler extends StaticHandlerImpl {

	public TemplateStaticHandler() {
		this.values = new HashMap<String, String>();
	}

	Map<String, String> values;

	@Override
	protected void sendFile(RoutingContext context, String path, FileProps fileProps) {
		if (path.endsWith(".html")) {
			//TODO maybe need to wrapInTCCLSwitch
			String content = getFileContent(path, context);
			context.response().end(content);
		} else {
			super.sendFile(context, path, fileProps);
		}

	}

	protected String getFileContent(String path, RoutingContext context) {
		Buffer b = Vertx.vertx().fileSystem().readFileBlocking(path);
		byte[] bytes = b.getBytes();
		String fileContent = new String(bytes);

		StrSubstitutor subs = new StrSubstitutor(this.values);
		fileContent = subs.replace(fileContent);

		return fileContent;
	}

	public static TemplateStaticHandler create() {
		return new TemplateStaticHandler();
	}

	public TemplateStaticHandler addVariable(String name, String value) {
		this.values.put(name, value);
		return this;
	}

	public TemplateStaticHandler addVariables(Map<String, String> values) {
		this.values.putAll(values);
		return this;
	}
}
