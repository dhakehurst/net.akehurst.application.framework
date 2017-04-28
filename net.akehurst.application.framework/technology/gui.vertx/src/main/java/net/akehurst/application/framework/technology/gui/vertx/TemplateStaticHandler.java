/**
 * Copyright (C) 2016 Dr. David H. Akehurst (http://dr.david.h.akehurst.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.akehurst.application.framework.technology.gui.vertx;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.text.StrSubstitutor;

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
		if (path.endsWith(".html") || path.endsWith(".css")) {
			// TODO maybe need to wrapInTCCLSwitch
			final String content = this.getFileContent(path, context);
			context.response().end(content);
		} else {
			super.sendFile(context, path, fileProps);
		}

	}

	protected String getFileContent(final String path, final RoutingContext context) {
		final Buffer b = context.vertx().fileSystem().readFileBlocking(path);
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
