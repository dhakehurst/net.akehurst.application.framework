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
		this.values = new HashMap<>();
	}

	Map<String, String> values;

	@Override
	protected void sendFile(final RoutingContext context, final String path, final FileProps fileProps) {
		if (path.endsWith(".html")) {
			// TODO maybe need to wrapInTCCLSwitch
			final String content = this.getFileContent(path, context);
			context.response().end(content);
		} else {
			super.sendFile(context, path, fileProps);
		}

	}

	protected String getFileContent(final String path, final RoutingContext context) {
		final Buffer b = Vertx.vertx().fileSystem().readFileBlocking(path);
		final byte[] bytes = b.getBytes();
		String fileContent = new String(bytes);

		final StrSubstitutor subs = new StrSubstitutor(this.values);
		fileContent = subs.replace(fileContent);

		return fileContent;
	}

	public static TemplateStaticHandler create() {
		return new TemplateStaticHandler();
	}

	public TemplateStaticHandler addVariable(final String name, final String value) {
		this.values.put(name, value);
		return this;
	}

	public TemplateStaticHandler addVariables(final Map<String, String> values) {
		this.values.putAll(values);
		return this;
	}
}
